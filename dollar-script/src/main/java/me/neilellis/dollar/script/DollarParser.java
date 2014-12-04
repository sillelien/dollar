/*
 * Copyright (c) 2014 Neil Ellis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.neilellis.dollar.script;

import com.google.common.collect.Range;
import com.google.common.io.ByteStreams;
import me.neilellis.dollar.*;
import me.neilellis.dollar.script.java.JavaScriptingSupport;
import me.neilellis.dollar.script.operators.*;
import me.neilellis.dollar.types.DollarFactory;
import org.codehaus.jparsec.*;
import org.codehaus.jparsec.error.ParserException;
import org.codehaus.jparsec.functors.Map;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static me.neilellis.dollar.DollarStatic.*;
import static me.neilellis.dollar.script.DollarLexer.*;
import static me.neilellis.dollar.script.DollarScriptSupport.getVariable;
import static me.neilellis.dollar.script.DollarScriptSupport.wrapReactiveBinary;
import static me.neilellis.dollar.script.OperatorPriority.*;
import static me.neilellis.dollar.types.DollarFactory.fromValue;
import static org.codehaus.jparsec.Parsers.*;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarParser {

    //Lexer

    public static final String NAMED_PARAMETER_META_ATTR = "__named_parameter";


    private final ClassLoader classLoader;
    private final ThreadLocal<List<ScriptScope>> scopes = new ThreadLocal<List<ScriptScope>>() {
        @Override
        protected List<ScriptScope> initialValue() {
            ArrayList<ScriptScope> list = new ArrayList<>();
            list.add(new ScriptScope("ThreadTopLevel"));
            return list;
        }
    };
    private final ParserErrorHandler errorHandler = new ParserErrorHandler();
    private final ConcurrentHashMap<String, var> exports = new ConcurrentHashMap<>();
    private File file;
    private File sourceDir;
    private Parser<?> topLevelParser;

    public DollarParser() {
        classLoader = DollarParser.class.getClassLoader();
    }

    public DollarParser(ClassLoader classLoader, File dir, File mainFile) {
        this.classLoader = classLoader;
        this.sourceDir = dir;
        this.file = mainFile;
    }

    public ScriptScope currentScope() {
        return scopes.get().get(scopes.get().size() - 1);
    }

    public void export(String name, var export) {
        exports.put(name, export);
    }

    public ParserErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public var parse(File file, boolean parallel) throws IOException {
        if (file.getName().endsWith(".md") || file.getName().endsWith(".markdown")) {
            return parseMarkdown(file);
        } else {
            String source = new String(Files.readAllBytes(file.toPath()));
            return parse(new ScriptScope(this, source), source);
        }

    }

    var parseMarkdown(File file) throws IOException {
        PegDownProcessor pegDownProcessor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS);
        RootNode root =
                pegDownProcessor.parseMarkdown(com.google.common.io.Files.toString(file, Charset.forName("utf-8"))
                                                                         .toCharArray());
        root.accept(new CodeExtractionVisitor());
        return $();
    }

//    private Parser<var> arrayElementExpression(Parser<var> expression1, Parser<var> expression2, ScriptScope scope) {
//        return expression1.infixl(term("[").next(expression2).followedBy(term("]")));
//    }

    public var parse(ScriptScope scope, String source) throws IOException {
        addScope(scope);
        try {
            DollarStatic.context().setClassLoader(classLoader);
            scope.setDollarParser(this);
            Parser<?> parser = buildParser(scope);
            List<var> parse = (List<var>) parser.from(TOKENIZER, DollarLexer.IGNORED).parse(source);
            return $(exports);
        } catch (ParserException e) {
            //todo: proper error handling
            if (e.getErrorDetails() != null) {
                final int index = e.getErrorDetails().getIndex();
                final int endIndex = (index < source.length() - 20) ? index + 20 : source.length() - 1;
                if (source.length() > 0 && index > 0 && index < endIndex) {
                    System.err.println(source.substring(index, endIndex));
                }
            }
            scope.handleError(e);
            throw e;
        } finally {
            endScope();
        }
    }

    void addScope(ScriptScope scope) {
        scopes.get().add(scope);
    }

    private Parser<?> buildParser(ScriptScope scope) {
        topLevelParser = script(scope);
        return topLevelParser;
    }

    ScriptScope endScope() {
        return scopes.get().remove(scopes.get().size() - 1);
    }

    private Parser<List<var>> script(ScriptScope scope) {
        return inScope("(script)", scope, newScope -> {
            Parser.Reference<var> ref = Parser.newReference();
            Parser<var> block = block(ref.lazy(), newScope).between(OP_NL("{"), NL_TERM("}"));
            Parser<var> expression = expression(newScope);
            Parser<List<var>> parser = (TERMINATOR_SYMBOL.optional()).next(or(expression, block).followedBy(
                    TERMINATOR_SYMBOL).map(v -> v._fix(false)).many1());
            ref.set(parser.map(DollarStatic::$));
            return parser;
        });
    }

    @SuppressWarnings("ThrowFromFinallyBlock")
    public <T> T inScope(String scopeName, ScriptScope currentScope, Function<ScriptScope, T> r) {
        ScriptScope newScope = new ScriptScope(currentScope, currentScope.getSource(), scopeName);
        addScope(currentScope);
        addScope(newScope);
        try {
            return r.apply(newScope);
        } finally {
            ScriptScope poppedScope = endScope();
            if (poppedScope != newScope) {
                throw new IllegalStateException("Popped wrong scope");
            }
            final ScriptScope poppedScope2 = endScope();
            if (poppedScope2 != currentScope) {
                throw new IllegalStateException("Popped wrong scope");
            }
        }
    }

    private Parser<var> block(Parser<var> parentParser, ScriptScope scope) {
        Parser.Reference<var> ref = Parser.newReference();

        //Now we do the complex part, the following will only return the last value in the
        //block when the block is evaluated, but it will trigger execution of the rest.
        //This gives it functionality like a conventional function in imperative languages
        Parser<var>
                or =
                (
                        or(parentParser, parentParser.between(OP_NL("{"), NL_TERM("}")))
                ).sepBy1(SEMICOLON_TERMINATOR).followedBy(SEMICOLON_TERMINATOR.optional()).map(
                        new BlockOperator(this, scope));
        ref.set(or);
        return or;
    }

    private Parser<var> expression(ScriptScope scope) {

        Parser.Reference<var> ref = Parser.newReference();
        Parser<var> main = ref.lazy()
                              .between(OP("("), OP(")"))
                              .or(or(unitValue(scope), list(ref.lazy(), scope), map(ref.lazy(), scope),
                                     moduleStatement(scope, ref), assertOperator(ref, scope),
                                     collectStatement(ref.lazy(), scope),
                                     whenStatement(ref.lazy()), everyStatement(ref.lazy(), scope),
                                     functionCall(ref, scope),
                                     java(scope), URL, DECIMAL_LITERAL, INTEGER_LITERAL,
                                     STRING_LITERAL,
                                     dollarIdentifier(scope, ref), IDENTIFIER_KEYWORD,
                                     BUILTIN.map(new Map<var, var>() {
                                         public var map(var var) {
                                             return Builtins.execute(var.S(), Arrays.asList(), scope);
                                         }
                                     }),
                                     identifier().followedBy(OP("(").not().peek()).map(new Map<var, var>() {
                                         public var map(var var) {
                                             return getVariable(scope, var.S(), false, null);
                                         }
                                     })))
                              .or(block(ref.lazy(), scope).between(OP_NL("{"), NL_TERM("}")));

        Parser<var> parser = new OperatorTable<var>()
                .infixl(op(new BinaryOp((lhs, rhs) -> $(!lhs.equals(rhs)), scope), "!="), EQUIVALENCE_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> $(lhs.equals(rhs)), scope), "=="), EQUIVALENCE_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> $(lhs.isTrue() && rhs.isTrue()), scope), "&&", "and"),
                        LOGICAL_AND_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> $(lhs.isTrue() || rhs.isTrue()), scope), "||", "or"),
                        LOGICAL_OR_PRIORITY)
                .postfix(pipeOperator(ref, scope), PIPE_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> fromValue(Range.closed(lhs, rhs)), scope), ".."), RANGE_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> $(lhs.compareTo(rhs) < 0), scope), "<"), COMPARISON_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> $(lhs.compareTo(rhs) > 0), scope), ">"), EQUIVALENCE_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> $(lhs.compareTo(rhs) <= 0), scope), "<="), EQUIVALENCE_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> $(lhs.compareTo(rhs) >= 0), scope), ">="), EQUIVALENCE_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> {
                    final var lhsFix = lhs._fix(false);
                    if (lhsFix.isCollection()) {
                        var newValue = lhsFix._fixDeep(false);
                        Long max = rhs.L();
                        for (int i = 1; i < max; i++) {
                            newValue = newValue.$plus(lhs._fixDeep());
                        }
                        return newValue;
                    } else {return lhsFix.$multiply(rhs);}
                }, scope), "*"), MULTIPLY_DIVIDE_PRIORITY)
                .infixl(op(new BinaryOp(NumericAware::$divide, scope), "/"), MULTIPLY_DIVIDE_PRIORITY)
                .infixl(op(new BinaryOp(NumericAware::$modulus, scope), "%"), MULTIPLY_DIVIDE_PRIORITY)
                .infixl(op(new BinaryOp(var::$plus, scope), "+"), PLUS_MINUS_PRIORITY)
                .infixl(op(new BinaryOp(var::$minus, scope), "-"), PLUS_MINUS_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> $(lhs.$S(), rhs), scope), ":"), 30)
                .prefix(op(new UnaryOp(scope, false, i -> {
                    i.out();
                    return $void();
                }), "@@", "print"), LINE_PREFIX_PRIORITY)
                .prefix(op(new UnaryOp(scope, false, i -> {
                    i.debug();
                    return $void();
                }), "!!", "debug"), LINE_PREFIX_PRIORITY)
                .prefix(op(new UnaryOp(scope, false, i -> {
                    i.err();
                    return $void();
                }), "??", "err"), LINE_PREFIX_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> {
                    final var lhsFix = lhs._fixDeep(true);
                    final var rhsFix = rhs._fixDeep(true);
                    if (lhsFix.equals(rhsFix)) {
                        return $(true);
                    } else {
                        throw new AssertionError(lhsFix.$S() + " != " + rhsFix.$S());
                    }
                }, scope), "<=>"), LINE_PREFIX_PRIORITY)
                .prefix(writeOperator(ref, scope), OUTPUT_PRIORITY)
                .prefix(readOperator(), OUTPUT_PRIORITY)
                .prefix(op(new UnaryOp(scope, i -> $(i.isTruthy())), "~", "truthy"), UNARY_PRIORITY)
                .prefix(op(new UnaryOp(scope, var::$size), "#", "size"), UNARY_PRIORITY)
                .prefix(op(new UnaryOp(scope, URIAware::$drain), "<--", "drain"), OUTPUT_PRIORITY)
                .prefix(op(new UnaryOp(scope, URIAware::$all), "<@", "all"), OUTPUT_PRIORITY)
                .infixl(op(new BinaryOp(
                        (lhs, rhs) -> {
                            final var fixLhs = lhs._fixDeep();
                            if (fixLhs.isBoolean() && fixLhs.isFalse()) { return rhs._fix(2, false); } else {
                                return fixLhs;
                            }
                        },
                        scope), "-:", "else"), IF_PRIORITY)
                .prefix(ifOperator(ref, scope), IF_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> rhs.$contains(lhs), scope), "€", "in"), IN_PRIORITY)
                .prefix(op(new UnaryOp(scope, scope::addErrorHandler), "!?#*!", "error"), LINE_PREFIX_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> rhs.$publish(lhs), scope), "*>", "publish"), OUTPUT_PRIORITY)
                .infixl(op(new SubscribeOperator(scope), "<*", "subscribe"), OUTPUT_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> rhs.$write(lhs), scope), ">>"), OUTPUT_PRIORITY)
                .postfix(isOperator(scope), EQUIVALENCE_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> {
                            return lhs.$each(i -> inScope("each", scope, newScope -> {
                                newScope.setParameter("1", i);
                                return rhs._fixDeep(false);
                            }));
                        }, scope), "*|*", "each"),
                        MULTIPLY_DIVIDE_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> {
                    return lhs.$list().stream().reduce((x, y) -> {
                        return inScope("reduce", scope, newScope -> {
                            newScope.setParameter("1", x);
                            newScope.setParameter("2", y);
                            return rhs._fixDeep(false);
                        });
                    }).get();
                }, scope), "*|", "reduce"), MULTIPLY_DIVIDE_PRIORITY)
                .prefix(op(new SimpleReadOperator(scope), "<<"), OUTPUT_PRIORITY)
                .infixl(op(new ListenOperator(scope), "?->", "causes"), CONTROL_FLOW_PRIORITY)
                .infixl(op(new BinaryOp((lhs, rhs) -> {
                    var lambda = DollarFactory.fromLambda(i -> lhs.isTrue() ? fix(rhs, false) : $void());
                    lhs.$listen(i -> {
                        lambda.$notify();
                        return fix(lambda, false);
                    });
                    return lambda;
                }, scope), "?"), CONTROL_FLOW_PRIORITY)
                .infixl(op(new BinaryOp(ControlFlowAware::$choose, scope), "?*", "choose"), CONTROL_FLOW_PRIORITY)
                .infixl(op(new BinaryOp(var::$default, scope), "|", "default"), CONTROL_FLOW_PRIORITY)
                .postfix(memberOperator(ref, scope), MEMBER_PRIORITY)
                .prefix(op(new UnaryOp(scope, v -> $(!v.isTrue())), "!", "not"), UNARY_PRIORITY)
                .postfix(op(new UnaryOp(scope, var::$dec), "--"), INC_DEC_PRIORITY)
                .postfix(op(new UnaryOp(scope, var::$inc), "++"), INC_DEC_PRIORITY)
                .prefix(op(new UnaryOp(scope, var::$negate), "-"), UNARY_PRIORITY)
                .prefix(op(new UnaryOp(scope, var::$stop), "(!)"), SIGNAL_PRIORITY)
                .prefix(op(new UnaryOp(scope, var::$start), "(>)"), SIGNAL_PRIORITY)
                .prefix(op(new UnaryOp(scope, var::$pause), "(=)"), SIGNAL_PRIORITY)
                .prefix(op(new UnaryOp(scope, var::$unpause), "(~)"), SIGNAL_PRIORITY)
                .prefix(op(new UnaryOp(scope, var::$destroy), "(-)"), SIGNAL_PRIORITY)
                .prefix(op(new UnaryOp(scope, var::$create), "(+)"), SIGNAL_PRIORITY)
                .prefix(op(new UnaryOp(scope, var::$state), "(?)"), SIGNAL_PRIORITY)
                .prefix(op(new UnaryOp(scope, true,
                                       v -> {
                                           return v._fixDeep(true);
                                       }), "|:|", "parallel"), SIGNAL_PRIORITY)
                .prefix(op(new UnaryOp(scope, true, v -> {
                    return v._fixDeep(false);
                }), "|..|", "serial"), SIGNAL_PRIORITY)
                .prefix(op(new UnaryOp(scope, v -> DollarFactory.fromFuture(
                        Execution.executeInBackground(i -> fix(v, false)))), "-<", "fork"), SIGNAL_PRIORITY)
                .prefix(forOperator(scope, ref), UNARY_PRIORITY)
                .prefix(whileOperator(scope, ref), UNARY_PRIORITY)
                .postfix(subscriptOperator(ref, scope), MEMBER_PRIORITY)
                .postfix(parameterOperator(ref, scope), MEMBER_PRIORITY)
                .prefix(op(new UnaryOp(scope, true, v -> v._fixDeep(false)), "&", "fix"), 1000)
                .prefix(variableUsageOperator(scope), 1000)
                .postfix(castOperator(scope), UNARY_PRIORITY)
                .prefix(assignmentOperator(scope, ref), ASSIGNMENT_PRIORITY)
                .prefix(declarationOperator(scope, ref), ASSIGNMENT_PRIORITY)
                .build(main);
        ref.set(parser);
        scope.setParser(parser);
        return parser.token().map(new Map<Token, var>() {
            @Override public var map(Token token) {
                final var value = (var) token.value();
                value.setMetaAttribute("__parse_start", String.valueOf(token.index()));
                value.setMetaAttribute("__parse_length", String.valueOf(token.length()));
                return value;
            }
        });

    }

    private Parser<var> unitValue(ScriptScope scope) {
        return array(DECIMAL_LITERAL.or(INTEGER_LITERAL), BUILTIN).map(new UnitOperator(this, scope));
    }

    private Parser<var> list(Parser<var> expression, ScriptScope scope) {
        return OP_NL("[")
                .next(expression.sepBy(COMMA_OR_NEWLINE_TERMINATOR))
                .followedBy(NL_TERM("]")).map(
                        new ListOperator(this, scope));
    }

    private Parser<var> map(Parser<var> expression, ScriptScope scope) {
        Parser<List<var>>
                sequence =
                OP_NL("{").next(expression.sepBy1(COMMA_TERMINATOR))
                          .followedBy(NL_TERM("}"));
        return sequence.map(new MapOperator(this, scope));
    }

    private Parser<var> moduleStatement(ScriptScope scope, Parser.Reference<var> ref) {
        final Parser<Object[]> param = array(IDENTIFIER.followedBy(OP("=")), ref.lazy());

        final Parser<List<var>> parameters =
                KEYWORD("with").optional().next((param).map(objects -> {
                    var result = (var) objects[1];
                    result.setMetaAttribute(NAMED_PARAMETER_META_ATTR, objects[0].toString());
                    return result;
                }).sepBy(OP(",")).between(OP("("), OP(")")));

        Parser<Object[]> sequence = array(KEYWORD("module"), STRING_LITERAL.or(URL), parameters.optional());

        return sequence.map(new ModuleOperator(scope));

    }

    private Parser<var> assertOperator(Parser.Reference<var> ref, ScriptScope scope) {
        return OP(".:").next(
                or(array(STRING_LITERAL.followedBy(OP(":")), ref.lazy()), array(OP(":").optional(), ref.lazy()))
                        .withSource()
                        .map(new AssertOperator(scope)));
    }

    private Parser<var> collectStatement(Parser<var> expression, ScriptScope scope) {
        Parser<Object[]> sequence = KEYWORD_NL("collect")
                .next(array(expression, KEYWORD("until").next(expression).optional(),
                            KEYWORD("unless").next(expression).optional(), expression));
        return sequence.map(new CollectOperator(this, scope));
    }

    private Parser<var> whenStatement(Parser<var> expression) {
        Parser<Object[]> sequence = KEYWORD_NL("when").next(array(expression, expression));
        return sequence.map(new WhenOperator());
    }

    private Parser<var> everyStatement(Parser<var> expression, ScriptScope scope) {
        Parser<Object[]> sequence = KEYWORD_NL("every")
                .next(array(unitValue(scope),
                            KEYWORD("until").next(expression).optional(),
                            KEYWORD("unless").next(expression).optional(),
                            expression));
        return sequence.map(new EveryOperator(this, scope));
    }

    private Parser<var> functionCall(Parser.Reference<var> ref, ScriptScope scope) {
        return array(IDENTIFIER.or(BUILTIN).followedBy(OP("(").peek()), parameterOperator(ref, scope)).map(
                new FunctionCallOperator());
    }

    final Parser<var> java(ScriptScope scope) {
        return token(new TokenMap<String>() {
            @Override
            public String map(Token token) {
                final Object val = token.value();
                if (val instanceof Tokens.Fragment) {
                    Tokens.Fragment c = (Tokens.Fragment) val;
                    if (!c.tag().equals("java")) {
                        return null;
                    }
                    return c.text();
                } else { return null; }
            }

            @Override
            public String toString() {
                return "java";
            }
        }).map(new Map<String, var>() {
            @Override
            public var map(String s) {
                return JavaScriptingSupport.compile($void(), s, scope);
            }
        });
    }

    private static Parser<var> dollarIdentifier(ScriptScope scope, Parser.Reference ref) {
        return OP("$").next(
                array(Terminals.Identifier.PARSER, OP("|").next(ref.lazy()).optional()).between(OP("{"), OP("}"))).map(
                new Map<Object[], var>() {
                    @Override public var map(Object[] objects) {
                        return getVariable(scope, objects[0].toString(), false, (var) objects[1]);
                    }
                });
    }

    <T> Parser<T> op(T value, String name) {
        return op(value, name, null);
    }

    <T> Parser<T> op(T value, String name, String keyword) {
        Parser<?> parser;
        if (keyword == null) {
            parser = OP(name);
        } else {
            parser = OP(name, keyword);

        }
        return parser.token().map(new SourceMapper<>(value));

    }

    private Parser<Map<? super var, ? extends var>> pipeOperator(Parser.Reference<var> ref, ScriptScope scope) {
        return (OP("->").optional()).next(
                Parsers.longest(BUILTIN, IDENTIFIER, functionCall(ref, scope), ref.lazy().between(OP("("), OP(")"))))
                                    .map(new PipeOperator(this, scope));
    }

    private Parser<Map<? super var, ? extends var>> writeOperator(Parser.Reference<var> ref, ScriptScope scope) {
        return array(KEYWORD("write"), ref.lazy(), KEYWORD("block").optional(), KEYWORD("mutate").optional())
                .followedBy(KEYWORD("to").optional())
                .map(new WriteOperator(scope));
    }

    private Parser<Map<? super var, ? extends var>> readOperator() {
        return array(KEYWORD("read"), KEYWORD("block").optional(), KEYWORD("mutate").optional())
                .followedBy(KEYWORD("from").optional())
                .map(new ReadOperator());
    }

    private Parser<Map<var, var>> ifOperator(Parser.Reference<var> ref, ScriptScope scope) {
        return KEYWORD_NL("if").next(ref.lazy()).map(new IfOperator(scope));
    }

    private Parser<Map<? super var, ? extends var>> isOperator(ScriptScope scope) {
        return KEYWORD("is").next(IDENTIFIER.sepBy(OP(","))).map(new IsOperator(scope));
    }

    private Parser<Map<? super var, ? extends var>> memberOperator(Parser.Reference<var> ref, ScriptScope scope) {
        return OP(".").followedBy(OP(".").not())
                      .next(ref.lazy().between(OP("("), OP(")")).or(IDENTIFIER))
                      .map(rhs -> lhs -> wrapReactiveBinary(scope, lhs, rhs, () -> lhs.$(rhs.$S())));
    }

    private Parser<Map<? super var, ? extends var>> forOperator(final ScriptScope scope, Parser.Reference<var> ref) {
        return array(KEYWORD("for"), IDENTIFIER, KEYWORD("in"), ref.lazy()).map(new ForOperator(this, scope));
    }

    private Parser<Map<? super var, ? extends var>> whileOperator(final ScriptScope scope, Parser.Reference<var> ref) {
        return KEYWORD("while").next(ref.lazy()).map(new WhileOperator(this, scope));
    }

    private Parser<Map<? super var, ? extends var>> subscriptOperator(Parser.Reference<var> ref, ScriptScope scope) {
        return OP("[").next(array(ref.lazy().followedBy(OP("]")), OP("=").next(ref.lazy()).optional()))
                      .map(new SubscriptOperator(scope));
    }

    private Parser<Map<? super var, ? extends var>> parameterOperator(Parser.Reference<var> ref, ScriptScope scope) {
        return OP("(").next(
                or(array(IDENTIFIER.followedBy(OP("=")), ref.lazy()), array(OP("=").optional(), ref.lazy())).map(
                        objects -> {
                            //Is it a named parameter
                            if (objects[0] != null) {
                                //yes so let's add the name as metadata to the value
                                var result = (var) objects[1];
                                result.setMetaAttribute(NAMED_PARAMETER_META_ATTR, objects[0].toString());
                                return result;
                            } else {
                                //no, just use the value
                                return (var) objects[1];
                            }
                        }).sepBy(COMMA_TERMINATOR)).followedBy(OP(")")).map(
                new ParameterOperator(this, scope));
    }

    private Parser<Map<? super var, ? extends var>> variableUsageOperator(ScriptScope scope) {
        return or(OP("$").followedBy(OP("(").peek())
                         .map(new VariableUsageOperator(scope)),
                  OP("$").followedBy(INTEGER_LITERAL.peek()).map(
                          lhs -> rhs -> getVariable(scope, rhs.toString(), true, null)));
    }

    private Parser<Map<? super var, ? extends var>> castOperator(ScriptScope scope) {
        return KEYWORD("as").next(IDENTIFIER).map(new CastOperator(scope));
    }

    private Parser<Map<? super var, ? extends var>> assignmentOperator(final ScriptScope scope,
                                                                       Parser.Reference<var> ref) {
        return array(KEYWORD("export").optional(), or(KEYWORD("const"), KEYWORD("volatile")).optional(),
                     IDENTIFIER.between(OP("<"), OP(">")).optional(),
                     ref.lazy().between(OP("("), OP(")")).optional(),
                     OP("$").next(ref.lazy().between(OP("("), OP(")")))
                            .or(IDENTIFIER).or(BUILTIN),
                     or(OP("="), OP("?="), OP("*="))).map(new AssignmentOperator(scope, false));
    }

    private Parser<Map<? super var, ? extends var>> declarationOperator(final ScriptScope scope,
                                                                        Parser.Reference<var> ref) {

        return array(KEYWORD("export").optional(), IDENTIFIER.between(OP("<"), OP(">")).optional(),
                     OP("$").next(ref.lazy().between(OP("("), OP(")")))
                            .or(IDENTIFIER),
                     OP(":=")).map(new DeclarationOperator(scope));
    }

    public var parse(ScriptScope scope, File file, boolean parallel) throws IOException {
        String source = new String(Files.readAllBytes(file.toPath()));
        return parse(new ScriptScope(scope, source, file.getName()), source);
    }

    public var parse(ScriptScope scope, InputStream in, boolean parallel) throws IOException {
        String source = new String(ByteStreams.toByteArray(in));
        return parse(new ScriptScope(scope, source, "(stream)"), source);
    }

    public var parse(InputStream in, boolean parallel) throws IOException {
        String source = new String(ByteStreams.toByteArray(in));
        return parse(new ScriptScope(this, source), source);
    }

    public var parse(String s, boolean parallel) throws IOException {
        return parse(new ScriptScope(this, s), s);
    }

    public var parseMarkdown(String source) throws IOException {
        PegDownProcessor processor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS);
        RootNode rootNode = processor.parseMarkdown(source.toCharArray());
        rootNode.accept(new CodeExtractionVisitor());
        return $();
    }

    public List<ScriptScope> scopes() {
        return scopes.get();
    }

    private class SourceMapper<T> implements Map<Token, T> {
        private final T value;

        public SourceMapper(T value) {this.value = value;}

        @Override
        public T map(Token token) {
            if (value instanceof Operator) {
                ((Operator) value).setSource(() -> {
                    int index = token.index();
                    int length = token.length();
                    String theSource = currentScope().getSource();
                    int end = theSource.indexOf('\n', index + length);
                    int start = index > 10 ? index - 10 : 0;
                    String
                            highlightedSource =
                            "... " +
                            theSource.substring(start, index) +
                            " \u261E " +
                            theSource.substring(index, index + length) +
                            " \u261C " +
                            theSource.substring(index + length, end) +
                            " ...";
                    return highlightedSource.replaceAll("\n", "\\\\n");
                });
            }
            return value;
        }
    }
}
