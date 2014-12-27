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

import com.google.common.io.ByteStreams;
import me.neilellis.dollar.*;
import me.neilellis.dollar.collections.Range;
import me.neilellis.dollar.execution.DollarExecutor;
import me.neilellis.dollar.plugin.Plugins;
import me.neilellis.dollar.script.exceptions.DollarScriptFailureException;
import me.neilellis.dollar.script.java.JavaScriptingSupport;
import me.neilellis.dollar.script.operators.*;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.types.ErrorType;
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
import static me.neilellis.dollar.script.DollarScriptSupport.wrapReactive;
import static me.neilellis.dollar.script.OperatorPriority.*;
import static me.neilellis.dollar.types.DollarFactory.fromValue;
import static org.codehaus.jparsec.Parsers.*;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarParser {

    //Lexer

    public static final String NAMED_PARAMETER_META_ATTR = "__named_parameter";
    private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);


    private final ClassLoader classLoader;
    private final ThreadLocal<List<Scope>> scopes = new ThreadLocal<List<Scope>>() {
        @Override
        protected List<Scope> initialValue() {
            ArrayList<Scope> list = new ArrayList<>();
            list.add(new ScriptScope("ThreadTopLevel"));
            return list;
        }
    };
    private final ParserErrorHandler errorHandler = new ParserErrorHandler();
    private final ConcurrentHashMap<String, var> exports = new ConcurrentHashMap<>();
    private String file;
    private File sourceDir;
    private Parser<?> topLevelParser;

    public DollarParser() {
        classLoader = DollarParser.class.getClassLoader();
    }

    public DollarParser(ClassLoader classLoader, File dir) {
        this.classLoader = classLoader;
        this.sourceDir = dir;
    }

    private static Parser<var> dollarIdentifier(Scope scope, Parser.Reference ref, boolean pure) {
        return OP("$").next(
                array(Terminals.Identifier.PARSER, OP("|").next(ref.lazy()).optional()).between(OP("{"),
                                                                                                OP("}"))).token().map(
                t -> {
                    Object[] objects = (Object[]) t.value();
                    return getVariable(pure, scope, objects[0].toString(), false, (var) objects[1],
                                       new SourceSegmentValue(scope, t));
                });
    }

    public Scope currentScope() {
        return scopes.get().get(scopes.get().size() - 1);
    }

    public void export(String name, var export) {
        exports.put(name, export);
    }

    public ParserErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @SuppressWarnings("ThrowFromFinallyBlock")
    public <T> T inScope(boolean pure, String scopeName, Scope currentScope, Function<Scope, T> r) {
        Scope newScope;
        if (pure) {
            newScope = new PureScope(currentScope, currentScope.getSource(), scopeName, file);
        } else {
            if ((currentScope instanceof PureScope)) {
                throw new IllegalStateException("trying to switch to an impure scope in a pure scope.");
            }
            newScope = new ScriptScope(currentScope, file, currentScope.getSource(), scopeName);
        }
        addScope(currentScope);
        addScope(newScope);
        try {
            return r.apply(newScope);
        } finally {
            Scope poppedScope = endScope();
            if (poppedScope != newScope) {
                throw new IllegalStateException("Popped wrong scope");
            }
            final Scope poppedScope2 = endScope();
            if (poppedScope2 != currentScope) {
                throw new IllegalStateException("Popped wrong scope");
            }
        }
    }

//    private Parser<var> arrayElementExpression(Parser<var> expression1, Parser<var> expression2, ScriptScope scope) {
//        return expression1.infixl(term("[").next(expression2).followedBy(term("]")));
//    }

    public var parse(File file, boolean parallel) throws IOException {

        this.file = file.getAbsolutePath();
        if (file.getName().endsWith(".md") || file.getName().endsWith(".markdown")) {
            return parseMarkdown(file);
        } else {
            String source = new String(Files.readAllBytes(file.toPath()));
            return parse(new ScriptScope(this, source, file.getName()), source);
        }

    }

    public var parse(Scope scope, String source) throws IOException {
        addScope(scope);
        try {
            DollarStatic.context().setClassLoader(classLoader);
            scope.setDollarParser(this);
            Parser<?> parser = buildParser(scope, false);
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

    public var parse(Scope scope, File file, boolean parallel) throws IOException {
        String source = new String(Files.readAllBytes(file.toPath()));
        this.file = file.getAbsolutePath();
        return parse(new ScriptScope(scope, file.getName(), source, file.getName()), source);
    }

    public var parse(Scope scope, InputStream in, boolean parallel) throws IOException {
        String source = new String(ByteStreams.toByteArray(in));
        return parse(new ScriptScope(scope, "(stream)", source, "(stream)"), source);
    }

    public var parse(InputStream in, String file, boolean parallel) throws IOException {
        this.file = file;
        String source = new String(ByteStreams.toByteArray(in));
        return parse(new ScriptScope(this, source, file), source);
    }

    public var parse(String s, boolean parallel) throws IOException {
        return parse(new ScriptScope(this, s, "(string)"), s);
    }

    public var parseMarkdown(String source) throws IOException {
        PegDownProcessor processor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS);
        RootNode rootNode = processor.parseMarkdown(source.toCharArray());
        rootNode.accept(new CodeExtractionVisitor());
        return $();
    }

    public List<Scope> scopes() {
        return scopes.get();
    }

    private class SourceMapper<T> implements Map<Token, T> {
        private final T value;


        public SourceMapper(T value) {this.value = value;}

        @Override
        public T map(Token token) {
            if (value instanceof Operator) {
                ((Operator) value).setSource(new SourceSegmentValue(currentScope(), token));
            }
            return value;
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

    void addScope(Scope scope) {
        scopes.get().add(scope);
    }

    private Parser<?> buildParser(Scope scope, boolean pure) {
        topLevelParser = script(scope, pure);
        return topLevelParser;
    }

    Scope endScope() {
        return scopes.get().remove(scopes.get().size() - 1);
    }

    private Parser<List<var>> script(Scope scope, boolean pure) {
        return inScope(pure, "(script)", scope, newScope -> {
            Parser.Reference<var> ref = Parser.newReference();
            Parser<var> block = block(ref.lazy(), newScope, pure).between(OP_NL("{"), NL_OP("}"));
            Parser<var> expression = expression(null, newScope, pure);
            Parser<List<var>> parser = (TERMINATOR_SYMBOL.optional()).next(or(expression, block).followedBy(
                    TERMINATOR_SYMBOL).map(v -> v._fix(false)).many1());
            ref.set(parser.map(DollarStatic::$));
            return parser;
        });
    }

    private Parser<var> block(Parser<var> parentParser, Scope scope, boolean pure) {
        Parser.Reference<var> ref = Parser.newReference();

        //Now we do the complex part, the following will only return the last value in the
        //block when the block is evaluated, but it will trigger execution of the rest.
        //This gives it functionality like a conventional function in imperative languages
        Parser<var>
                or =
                (
                        or(parentParser, parentParser.between(OP_NL("{"), NL_OP("}")))
                ).sepBy1(SEMICOLON_TERMINATOR).followedBy(SEMICOLON_TERMINATOR.optional()).token().map(
                        new BlockOperator(this, scope, pure));
        ref.set(or);
        return or;
    }

    private Parser<var> expression(Parser.Reference<var> ref, final Scope scope, boolean pure) {
        Parser<var> main;
        if (ref == null) {
            ref = Parser.newReference();
        }
        if (!pure) {
            main = ref.lazy()
                      .between(OP("("), OP(")"))
                      .or(or(unitValue(scope, pure), list(ref.lazy(), scope, pure), map(ref.lazy(), scope, pure),
                             pureDeclarationOperator(scope, ref, pure),
                             KEYWORD("pure").next(
                                     expression(null, new PureScope(scope, scope.getSource(), "pure-scope", file),
                                                true)),
                             moduleStatement(scope, ref), assertOperator(ref, scope, pure),
                             collectStatement(ref.lazy(), scope, pure),
                             whenStatement(ref.lazy(), pure, scope), everyStatement(ref.lazy(), scope, pure),
                             functionCall(ref, scope, pure),
                             java(scope), URL, DECIMAL_LITERAL, INTEGER_LITERAL,
                             STRING_LITERAL,
                             dollarIdentifier(scope, ref, pure), IDENTIFIER_KEYWORD,
                             BUILTIN.token().map(new Map<Token, var>() {
                                 public var map(Token token) {
                                     final var v = (var) token.value();
                                     return wrapReactive(scope,
                                                         () -> Builtins.execute(v.toHumanString(), Arrays.asList(),
                                                                                scope,
                                                                                pure),
                                                         new SourceSegmentValue(scope, token), v.toHumanString(), v
                                     );
                                 }
                             }),
                             identifier().followedBy(OP("(").not().peek()).token().map(new Map<Token, var>() {
                                 public var map(Token token) {
                                     return getVariable(pure, scope, token.value().toString(), false, null,
                                                        new SourceSegmentValue(scope, token));
                                 }
                             })))
                      .or(block(ref.lazy(), scope, pure).between(OP_NL("{"), NL_OP("}")));
        } else {
            main = ref.lazy()
                      .between(OP("("), OP(")"))
                      .or(or(unitValue(scope, pure),
                             list(ref.lazy(), scope, pure),
                             map(ref.lazy(), scope, pure),
                             assertOperator(ref, scope, pure),
                             collectStatement(ref.lazy(), scope, pure),
                             whenStatement(ref.lazy(), pure, scope),
                             functionCall(ref, scope, pure),
                             DECIMAL_LITERAL,
                             INTEGER_LITERAL,
                             STRING_LITERAL,
                             IDENTIFIER_KEYWORD,
                             identifier().followedBy(OP("(").not().peek()).token().map(new Map<Token, var>() {
                                 public var map(Token token) {
                                     return getVariable(pure, scope, token.value().toString(), false, null,
                                                        new SourceSegmentValue(scope, token));
                                 }
                             }),
                             BUILTIN.token().map(new Map<Token, var>() {
                                 public var map(Token token) {
                                     final var v = (var) token.value();
                                     return wrapReactive(scope,
                                                         () -> Builtins.execute(v.toHumanString(), Arrays.asList(),
                                                                                scope,
                                                                                pure),
                                                         new SourceSegmentValue(scope, token), v.toHumanString(), v
                                     );
                                 }
                             })))
                      .or(block(ref.lazy(), scope, pure).between(OP_NL("{"), NL_OP("}")));
        }

        OperatorTable<var> table = new OperatorTable<var>()
                .infixl(op(new BinaryOp("not-equal", (lhs, rhs) -> $(!lhs.equals(rhs)), scope), "!="),
                        EQUIVALENCE_PRIORITY)
                .infixl(op(new BinaryOp("equal", (lhs, rhs) -> $(lhs.equals(rhs)), scope), "=="), EQUIVALENCE_PRIORITY)
                .infixl(op(new BinaryOp("and", (lhs, rhs) -> $(lhs.isTrue() && rhs.isTrue()), scope), "&&", "and"),
                        LOGICAL_AND_PRIORITY)
                .infixl(op(new BinaryOp("or", (lhs, rhs) -> $(lhs.isTrue() || rhs.isTrue()), scope), "||", "or"),
                        LOGICAL_OR_PRIORITY)
                .postfix(pipeOperator(ref, scope, pure), PIPE_PRIORITY)
                .infixl(op(new BinaryOp("range", (lhs, rhs) -> fromValue(new Range(lhs, rhs)), scope), ".."),
                        RANGE_PRIORITY)
                .infixl(op(new BinaryOp("less-than", (lhs, rhs) -> $(lhs.compareTo(rhs) < 0), scope), "<"),
                        COMPARISON_PRIORITY)
                .infixl(op(new BinaryOp("greater-than", (lhs, rhs) -> $(lhs.compareTo(rhs) > 0), scope), ">"),
                        EQUIVALENCE_PRIORITY)
                .infixl(op(new BinaryOp("less-than-equal", (lhs, rhs) -> $(lhs.compareTo(rhs) <= 0), scope), "<="),
                        EQUIVALENCE_PRIORITY)
                .infixl(op(new BinaryOp("greater-than-equal", (lhs, rhs) -> $(lhs.compareTo(rhs) >= 0), scope), ">="),
                        EQUIVALENCE_PRIORITY)
                .infixl(op(new BinaryOp("multiply", (lhs, rhs) -> {
                    final var lhsFix = lhs._fix(false);
                    if (lhsFix.collection()) {
                        var newValue = lhsFix._fixDeep(false);
                        Long max = rhs.toLong();
                        for (int i = 1; i < max; i++) {
                            newValue = newValue.$plus(lhs._fixDeep());
                        }
                        return newValue;
                    } else {return lhsFix.$multiply(rhs);}
                }, scope), "*"), MULTIPLY_DIVIDE_PRIORITY)
                .infixl(op(new BinaryOp("divide", NumericAware::$divide, scope), "/"), MULTIPLY_DIVIDE_PRIORITY)
                .infixl(op(new BinaryOp("modulus", NumericAware::$modulus, scope), "%"), MULTIPLY_DIVIDE_PRIORITY)
                .infixl(op(new BinaryOp("plus", var::$plus, scope), "+"), PLUS_MINUS_PRIORITY)
                .infixl(op(new BinaryOp("minus", var::$minus, scope), "-"), PLUS_MINUS_PRIORITY)
                .infixl(op(new BinaryOp("pair", (lhs, rhs) -> $(lhs.$S(), rhs), scope), ":"), 30)

                .infixl(op(new BinaryOp("assert-equal", (lhs, rhs) -> {

                    final var lhsFix = lhs._fixDeep(true);
                    final var rhsFix = rhs._fixDeep(true);
                    if (lhsFix.equals(rhsFix)) {
                        return $(true);
                    } else {
                        throw new DollarScriptFailureException(ErrorType.ASSERTION, lhsFix.toDollarScript() +
                                                                                    " != " +
                                                                                    rhsFix.toDollarScript());
                    }
                }, scope), "<=>"), LINE_PREFIX_PRIORITY)
                .prefix(op(new UnaryOp("truthy", scope, i -> $(i.truthy())), "~", "truthy"), UNARY_PRIORITY)
                .prefix(op(new UnaryOp("size", scope, var::$size), "#", "size"), UNARY_PRIORITY)
                .infixl(op(new BinaryOp(
                        "else", (lhs, rhs) -> {
                    final var fixLhs = lhs._fixDeep();
                    if (fixLhs.isBoolean() && fixLhs.isFalse()) { return rhs._fix(2, false); } else {
                        return fixLhs;
                    }
                },
                        scope), "-:", "else"), IF_PRIORITY)
                .prefix(ifOperator(ref, scope), IF_PRIORITY)
                .infixl(op(new BinaryOp("in", (lhs, rhs) -> rhs.$contains(lhs), scope), "€", "in"), IN_PRIORITY)
                .prefix(op(new UnaryOp("error", scope, scope::addErrorHandler), "!?#*!", "error"), LINE_PREFIX_PRIORITY)
                .postfix(isOperator(scope), EQUIVALENCE_PRIORITY)
                .infixl(op(new BinaryOp("each", (lhs, rhs) -> {
                            return lhs.$each(i -> inScope(pure, "each", scope, newScope -> {
                                newScope.setParameter("1", i);
                                return rhs._fixDeep(false);
                            }));
                        }, scope), "*|*", "each"),
                        MULTIPLY_DIVIDE_PRIORITY)
                .infixl(op(new BinaryOp("reduce", (lhs, rhs) -> {
                    return lhs.$list().stream().reduce((x, y) -> {
                        return inScope(pure, "reduce", scope, newScope -> {
                            newScope.setParameter("1", x);
                            newScope.setParameter("2", y);
                            return rhs._fixDeep(false);
                        });
                    }).get();
                }, scope), "*|", "reduce"), MULTIPLY_DIVIDE_PRIORITY)
                .infixl(op(new ListenOperator(scope, pure), "?->", "causes"), CONTROL_FLOW_PRIORITY)
                .infixl(op(new BinaryOp("listen", (lhs, rhs) -> lhs.isTrue() ? fix(rhs, false) : $void(), scope), "?"),
                        CONTROL_FLOW_PRIORITY)
                .infixl(op(new BinaryOp("choose", ControlFlowAware::$choose, scope), "?*", "choose"),
                        CONTROL_FLOW_PRIORITY)
                .infixl(op(new BinaryOp("default", var::$default, scope), "|", "default"), CONTROL_FLOW_PRIORITY)
                .postfix(memberOperator(ref, scope), MEMBER_PRIORITY)
                .prefix(op(new UnaryOp("not", scope, v -> $(!v.isTrue())), "!", "not"), UNARY_PRIORITY)
                .postfix(op(new UnaryOp("dec", scope, var::$dec), "--"), INC_DEC_PRIORITY)
                .postfix(op(new UnaryOp("inc", scope, var::$inc), "++"), INC_DEC_PRIORITY)
                .prefix(op(new UnaryOp("negate", scope, var::$negate), "-"), UNARY_PRIORITY)
                .prefix(op(new UnaryOp(scope, true, v -> v._fixDeep(true), "parallel"), "|:|", "parallel"),
                        SIGNAL_PRIORITY)
                .prefix(op(new UnaryOp(scope, true, v -> v._fixDeep(false), "serial"), "|..|", "serial"),
                        SIGNAL_PRIORITY)
                .prefix(forOperator(scope, ref, pure), UNARY_PRIORITY)
                .prefix(whileOperator(scope, ref, pure), UNARY_PRIORITY)
                .postfix(subscriptOperator(ref, scope), MEMBER_PRIORITY)
                .postfix(parameterOperator(ref, scope, pure), MEMBER_PRIORITY)
                .prefix(op(new UnaryOp(scope, true, v -> v._fixDeep(false), "fix"), "&", "fix"), 1000)
                .postfix(castOperator(scope), UNARY_PRIORITY)
                .prefix(variableUsageOperator(scope, pure), 1000)
                .prefix(assignmentOperator(scope, ref, pure), ASSIGNMENT_PRIORITY)
                .prefix(declarationOperator(scope, ref, pure), ASSIGNMENT_PRIORITY);

        if (!pure) {
            table = table.prefix(op(new UnaryOp(scope, false, i -> {
                i.out();
                return $void();
            }, "print"), "@@", "print"), LINE_PREFIX_PRIORITY)
                         .prefix(op(new UnaryOp(scope, false, i -> {
                             i.debug();
                             return $void();
                         }, "debug"), "!!", "debug"), LINE_PREFIX_PRIORITY)
                         .prefix(op(new UnaryOp(scope, false, i -> {
                             i.err();
                             return $void();
                         }, "err"), "??", "err"), LINE_PREFIX_PRIORITY)
                         .prefix(writeOperator(ref, scope), OUTPUT_PRIORITY)
                         .prefix(readOperator(scope), OUTPUT_PRIORITY)
                         .prefix(op(new UnaryOp("stop", scope, var::$stop), "(!)"), SIGNAL_PRIORITY)
                         .prefix(op(new UnaryOp("start", scope, var::$start), "(>)"), SIGNAL_PRIORITY)
                         .prefix(op(new UnaryOp("pause", scope, var::$pause), "(=)"), SIGNAL_PRIORITY)
                         .prefix(op(new UnaryOp("unpause", scope, var::$unpause), "(~)"), SIGNAL_PRIORITY)
                         .prefix(op(new UnaryOp("destroy", scope, var::$destroy), "(-)"), SIGNAL_PRIORITY)
                         .prefix(op(new UnaryOp("create", scope, var::$create), "(+)"), SIGNAL_PRIORITY)
                         .prefix(op(new UnaryOp("state", scope, var::$state), "(?)"), SIGNAL_PRIORITY)
                         .prefix(op(new UnaryOp("fork", scope, v -> DollarFactory.fromFuture(
                                         executor.executeInBackground(() -> fix(v, false)))), "-<", "fork"),
                                 SIGNAL_PRIORITY)
                         .infixl(op(new BinaryOp("fork", (lhs, rhs) -> rhs.$publish(lhs), scope), "*>",
                                    "publish"), OUTPUT_PRIORITY)
                         .infixl(op(new SubscribeOperator(scope, pure), "<*", "subscribe"), OUTPUT_PRIORITY)
                         .infixl(op(new BinaryOp("write-simple", (lhs, rhs) -> rhs.$write(lhs), scope), ">>"),
                                 OUTPUT_PRIORITY)
                         .prefix(op(new SimpleReadOperator(scope), "<<"), OUTPUT_PRIORITY)
                         .prefix(op(new UnaryOp("drain", scope, URIAware::$drain), "<--", "drain"), OUTPUT_PRIORITY)
                         .prefix(op(new UnaryOp("all", scope, URIAware::$all), "<@", "all"), OUTPUT_PRIORITY);

        }
        Parser<var> parser = table.build(main);
        ref.set(parser);
        return parser;

    }


    private Parser<var> unitValue(Scope scope, boolean pure) {
        return array(DECIMAL_LITERAL.or(INTEGER_LITERAL), BUILTIN).token().map(new UnitOperator(this, scope, pure));
    }

    private Parser<var> list(Parser<var> expression, Scope scope, boolean pure) {
        return OP_NL("[")
                .next(expression.sepBy(COMMA_OR_NEWLINE_TERMINATOR))
                .followedBy(COMMA_OR_NEWLINE_TERMINATOR.optional())
                .followedBy(NL_OP("]")).token().map(
                        new ListOperator(this, scope, pure));
    }

    private Parser<var> map(Parser<var> expression, Scope scope, boolean pure) {
        Parser<List<var>>
                sequence =
                OP_NL("{").next(expression.sepBy(COMMA_TERMINATOR))
                          .followedBy(COMMA_TERMINATOR.optional())
                          .followedBy(NL_OP("}"));
        return sequence.token().map(new MapOperator(this, scope, pure));
    }

    private Parser<var> moduleStatement(Scope scope, Parser.Reference<var> ref) {
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

    private Parser<var> assertOperator(Parser.Reference<var> ref, Scope scope, boolean pure) {
        return OP(".:").next(
                or(array(STRING_LITERAL.followedBy(OP(":")), ref.lazy()), array(OP(":").optional(), ref.lazy()))
                        .token()
                        .map(new AssertOperator(scope)));
    }

    private Parser<var> collectStatement(Parser<var> expression, Scope scope, boolean pure) {
        Parser<Object[]> sequence = KEYWORD_NL("collect")
                .next(array(expression, KEYWORD("until").next(expression).optional(),
                            KEYWORD("unless").next(expression).optional(), expression));
        return sequence.map(new CollectOperator(this, scope, pure));
    }

    private Parser<var> whenStatement(Parser<var> expression, boolean pure, Scope scope) {
        Parser<Object[]> sequence = KEYWORD_NL("when").next(array(expression, expression));
        return sequence.token().map(new WhenOperator(scope));
    }

    private Parser<var> everyStatement(Parser<var> expression, Scope scope, boolean pure) {
        Parser<Object[]> sequence = KEYWORD_NL("every")
                .next(array(unitValue(scope, pure),
                            KEYWORD("until").next(expression).optional(),
                            KEYWORD("unless").next(expression).optional(),
                            expression));
        return sequence.map(new EveryOperator(this, scope, pure));
    }

    private Parser<var> functionCall(Parser.Reference<var> ref, Scope scope, boolean pure) {
        return array(IDENTIFIER.or(BUILTIN).followedBy(OP("(").peek()), parameterOperator(ref, scope, pure)).map(
                new FunctionCallOperator());
    }

    final Parser<var> java(Scope scope) {
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

    private Parser<Map<? super var, ? extends var>> pipeOperator(Parser.Reference<var> ref, Scope scope,
                                                                 boolean pure) {
        return (OP("->").optional()).next(
                Parsers.longest(BUILTIN, IDENTIFIER, functionCall(ref, scope, pure),
                                ref.lazy().between(OP("("), OP(")"))))
                                    .map(new PipeOperator(this, scope, pure));
    }

    private Parser<Map<? super var, ? extends var>> writeOperator(Parser.Reference<var> ref, Scope scope) {
        return array(KEYWORD("write"), ref.lazy(), KEYWORD("block").optional(), KEYWORD("mutate").optional())
                .followedBy(KEYWORD("to").optional())
                .token().map(new WriteOperator(scope));
    }

    private Parser<Map<? super var, ? extends var>> readOperator(Scope scope) {
        return array(KEYWORD("read"), KEYWORD("block").optional(), KEYWORD("mutate").optional())
                .followedBy(KEYWORD("from").optional()).
                        token().map(new ReadOperator(scope));
    }

    private Parser<Map<var, var>> ifOperator(Parser.Reference<var> ref, Scope scope) {
        return KEYWORD_NL("if").next(ref.lazy()).token().map(new IfOperator(scope));
    }

    private Parser<Map<? super var, ? extends var>> isOperator(Scope scope) {
        return KEYWORD("is").next(IDENTIFIER.sepBy(OP(","))).token().map(new IsOperator(scope));
    }

    private Parser<Map<? super var, ? extends var>> memberOperator(Parser.Reference<var> ref, Scope scope) {
        return OP(".").followedBy(OP(".").not())
                      .next(ref.lazy().between(OP("("), OP(")")).or(IDENTIFIER))
                      .token().map(
                        (Token rhs) -> lhs -> wrapReactive(scope, () -> lhs.$(rhs.value().toString()),
                                                           new SourceSegmentValue(scope, rhs), "." + rhs.toString(),
                                                           lhs, (var)
                                        rhs.value()
                        ));
    }

    private Parser<Map<? super var, ? extends var>> forOperator(final Scope scope, Parser.Reference<var> ref,
                                                                boolean pure) {
        return array(KEYWORD("for"), IDENTIFIER, KEYWORD("in"), ref.lazy()).token().map(
                new ForOperator(this, scope, pure));
    }

    private Parser<Map<? super var, ? extends var>> whileOperator(final Scope scope, Parser.Reference<var> ref,
                                                                  boolean pure) {
        return KEYWORD("while").next(ref.lazy()).token().map(new WhileOperator(this, scope, pure));
    }

    private Parser<Map<? super var, ? extends var>> subscriptOperator(Parser.Reference<var> ref, Scope scope) {
        return OP("[").next(array(ref.lazy().followedBy(OP("]")), OP("=").next(ref.lazy()).optional()))
                      .token().map(new SubscriptOperator(scope));
    }

    private Parser<Map<? super var, ? extends var>> parameterOperator(Parser.Reference<var> ref, Scope scope,
                                                                      boolean pure) {
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
                        }).sepBy(COMMA_TERMINATOR)).followedBy(OP(")")).token().map(
                new ParameterOperator(this, scope, pure));
    }

    private Parser<Map<? super var, ? extends var>> variableUsageOperator(Scope scope, boolean pure) {
        return or(OP("$").followedBy(OP("(").peek()).token()
                         .map(new VariableUsageOperator(scope, pure)),
                  OP("$").followedBy(INTEGER_LITERAL.peek()).token().map(
                          (Token lhs) -> {
                              return rhs -> getVariable(pure, scope, rhs.toString(), true, null,
                                                        new SourceSegmentValue(scope, lhs));
                          }));
    }

    private Parser<Map<? super var, ? extends var>> castOperator(Scope scope) {
        return KEYWORD("as").next(IDENTIFIER).token().map(new CastOperator(scope));
    }

    private Parser<Map<? super var, ? extends var>> assignmentOperator(final Scope scope,
                                                                       Parser.Reference<var> ref, boolean pure) {
        return array(KEYWORD("export").optional(), or(KEYWORD("const"), KEYWORD("volatile")).optional(),
                     IDENTIFIER.between(OP("<"), OP(">")).optional(),
                     ref.lazy().between(OP("("), OP(")")).optional(),
                     OP("$").next(ref.lazy().between(OP("("), OP(")")))
                            .or(IDENTIFIER).or(BUILTIN),
                     or(OP("="), OP("?="), OP("*="))).token().map(new AssignmentOperator(scope, false, pure));
    }


    private Parser<Map<? super var, ? extends var>> declarationOperator(final Scope scope,
                                                                        Parser.Reference<var> ref, boolean pure) {

        return array(KEYWORD("export").optional(), IDENTIFIER.between(OP("<"), OP(">")).optional(),
                     OP("$").next(ref.lazy().between(OP("("), OP(")")))
                            .or(IDENTIFIER),
                     OP(":=")).token().map(new DeclarationOperator(scope, pure));
    }

    private Parser<var> pureDeclarationOperator(final Scope scope,
                                                Parser.Reference<var> ref, boolean pure) {

        return KEYWORD("pure").next(array(KEYWORD("export").optional(), IDENTIFIER.between(OP("<"), OP(">")).optional(),
                                          OP("$").next(ref.lazy().between(OP("("), OP(")")))
                                                 .or(IDENTIFIER),
                                          OP(":="), expression(null, scope, true)).token().map(
                new DeclarationOperator(scope, true))).map(new Map<Map<? super var, ? extends var>, var>() {
            @Override public var map(Map<? super var, ? extends var> map) {
                return map.map(null);
            }
        });
    }
}
