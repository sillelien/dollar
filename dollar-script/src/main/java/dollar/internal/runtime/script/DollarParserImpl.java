/*
 *    Copyright (c) 2014-2017 Neil Ellis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dollar.internal.runtime.script;

import com.google.common.io.ByteStreams;
import com.sillelien.dollar.api.ControlFlowAware;
import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.NumericAware;
import com.sillelien.dollar.api.URIAware;
import com.sillelien.dollar.api.collections.Range;
import com.sillelien.dollar.api.execution.DollarExecutor;
import com.sillelien.dollar.api.plugin.Plugins;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.types.ErrorType;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.ParserErrorHandler;
import dollar.internal.runtime.script.api.ParserOptions;
import dollar.internal.runtime.script.api.Scope;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import dollar.internal.runtime.script.api.exceptions.DollarScriptFailureException;
import dollar.internal.runtime.script.java.JavaScriptingSupport;
import dollar.internal.runtime.script.operators.AssertOperator;
import dollar.internal.runtime.script.operators.AssignmentOperator;
import dollar.internal.runtime.script.operators.BlockOperator;
import dollar.internal.runtime.script.operators.CastOperator;
import dollar.internal.runtime.script.operators.CollectOperator;
import dollar.internal.runtime.script.operators.DefinitionOperator;
import dollar.internal.runtime.script.operators.EveryOperator;
import dollar.internal.runtime.script.operators.ForOperator;
import dollar.internal.runtime.script.operators.FunctionCallOperator;
import dollar.internal.runtime.script.operators.IfOperator;
import dollar.internal.runtime.script.operators.IsOperator;
import dollar.internal.runtime.script.operators.ListenOperator;
import dollar.internal.runtime.script.operators.MapOperator;
import dollar.internal.runtime.script.operators.ModuleOperator;
import dollar.internal.runtime.script.operators.ParameterOperator;
import dollar.internal.runtime.script.operators.PipeOperator;
import dollar.internal.runtime.script.operators.ReadOperator;
import dollar.internal.runtime.script.operators.SimpleReadOperator;
import dollar.internal.runtime.script.operators.SubscribeOperator;
import dollar.internal.runtime.script.operators.SubscriptOperator;
import dollar.internal.runtime.script.operators.UnitOperator;
import dollar.internal.runtime.script.operators.VariableUsageOperator;
import dollar.internal.runtime.script.operators.WhenOperator;
import dollar.internal.runtime.script.operators.WhileOperator;
import dollar.internal.runtime.script.operators.WriteOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.OperatorTable;
import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Terminals;
import org.jparsec.Token;
import org.jparsec.TokenMap;
import org.jparsec.Tokens;
import org.jparsec.functors.Map;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

import static com.sillelien.dollar.api.DollarStatic.*;
import static com.sillelien.dollar.api.types.DollarFactory.fromValue;
import static dollar.internal.runtime.script.DollarLexer.*;
import static dollar.internal.runtime.script.DollarScriptSupport.*;
import static dollar.internal.runtime.script.OperatorPriority.*;
import static java.util.Collections.emptyList;
import static org.jparsec.Parsers.*;

public class DollarParserImpl implements DollarParser {
    public static final String NAMED_PARAMETER_META_ATTR = "__named_parameter";

    //Lexer
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("DollarParser");
    @Nullable
    private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);
    private final ClassLoader classLoader;
    private final ParserErrorHandler errorHandler = new ParserErrorHandlerImpl();
    private final ConcurrentHashMap<String, var> exports = new ConcurrentHashMap<>();
    private final ParserOptions options;
    private String file;
    private File sourceDir;
    private Parser<?> topLevelParser;

    public DollarParserImpl(ParserOptions options) {
        this.options = options;
        classLoader = DollarParser.class.getClassLoader();
    }

    public DollarParserImpl(ParserOptions options, ClassLoader classLoader, File dir) {
        this.options = options;
        this.classLoader = classLoader;
        this.sourceDir = dir;
    }

    private Parser<var> dollarIdentifier(@NotNull Parser.Reference ref, boolean pure) {
        return OP("$").next(
                array(Terminals.Identifier.PARSER, OP("|").next(ref.lazy()).optional()).between(
                        OP("{"),
                        OP("}"))).token().map(
                t -> {
                    Object[] objects = (Object[]) t.value();
                    return getVariable(pure, objects[0].toString(), false, (var) objects[1], t,
                                       this);
                });
    }


    @Override
    public void export(@NotNull String name, @NotNull var export) {
        System.err.println("Exporting " + name);
        export.setMetaObject("scopes", new ArrayList<Scope>(DollarScriptSupport.scopes()));
        exports.put(name, export);
    }

    @Override
    @NotNull
    public ParserErrorHandler getErrorHandler() {
        return errorHandler;
    }


    @Override
    public ParserOptions options() {
        return options;
    }

//    private Parser<var> arrayElementExpression(Parser<var> expression1, Parser<var> expression2, Script) {
//        return expression1.infixl(term("[").next(expression2).followedBy(term("]")));
//    }

    @Override
    @NotNull
    public var parse(ScriptScope scope, @NotNull String source) throws Exception {
        return DollarScriptSupport.inScope(false, scope, newScope -> {
            DollarStatic.context().setClassLoader(classLoader);
            Parser<?> parser = buildParser(false, scope);
            var parse = (var) parser.from(TOKENIZER, DollarLexer.IGNORED).parse(source);
//            parse._fixDeep(false);
//            newScope.destroy();
            return $(exports);
        });

    }

    @Override
    @NotNull
    public var parse(@NotNull File file, boolean parallel) throws Exception {

        this.file = file.getAbsolutePath();
        if (file.getName().endsWith(".md") || file.getName().endsWith(".markdown")) {
            return parseMarkdown(file);
        } else {
            String source = new String(Files.readAllBytes(file.toPath()));
            return parse(new ScriptScope(source, file, true), source);
        }

    }

    @Override
    @NotNull
    public var parse(@NotNull InputStream in, boolean parallel, Scope scope) throws Exception {
        String source = new String(ByteStreams.toByteArray(in));
        return parse(new ScriptScope(scope, "(stream)", source, "(stream-scope)", true), source);
    }

    @Override
    @NotNull
    public var parse(InputStream in, String file, boolean parallel) throws Exception {
        this.file = file;
        String source = new String(ByteStreams.toByteArray(in));
        return parse(new ScriptScope(source, new File(file), true), source);
    }

    @Override
    @NotNull
    public var parse(@NotNull String source, boolean parallel) throws Exception {
        return parse(new ScriptScope(source, "(string)", true), source);
    }

    @Override
    @NotNull
    public var parseMarkdown(@NotNull String source) {
        PegDownProcessor processor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS);
        RootNode rootNode = processor.parseMarkdown(source.toCharArray());
        rootNode.accept(new CodeExtractionVisitor());
        return $();
    }


    @NotNull
    private var parseMarkdown(File file) throws IOException {
        PegDownProcessor pegDownProcessor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS);
        RootNode root =
                pegDownProcessor.parseMarkdown(
                        com.google.common.io.Files.toString(file, Charset.forName("utf-8"))
                                .toCharArray());
        root.accept(new CodeExtractionVisitor());
        return $();
    }


    private Parser<?> buildParser(@NotNull boolean pure, Scope scope) throws Exception {
        topLevelParser = script(scope, pure);
        return topLevelParser;
    }


    private Parser<var> script(Scope scope, @NotNull boolean pure) throws Exception {
        log.debug("Starting Parse Phase");


        Parser.Reference<var> ref = Parser.newReference();
        Parser<var> block = block(ref.lazy(), pure).between(OP_NL("{"), NL_OP("}"));
        Parser<var> expression = expression(null, pure);
        Parser<var> parser = (TERMINATOR_SYMBOL.optional()).next(or(expression).followedBy(
                TERMINATOR_SYMBOL).many1()).map(l -> {
            log.debug("Ended Parse Phase");
            log.debug("Starting Runtime Phase");
            for (int i = 0; i < l.size() - 1; i++) {
                var fixed = l.get(i)._fixDeep(false);
                System.err.println(fixed);
            }
            var resultVar = l.get(l.size() - 1);
            var fixedResult = resultVar._fixDeep(false);
            log.debug("Ended Runtime Phase");
            return fixedResult;
        });
        ref.set(parser);
        return parser;

    }

    private Parser<var> block(@NotNull Parser<var> parentParser, boolean pure) throws Exception {
        Parser.Reference<var> ref = Parser.newReference();
        //Now we do the complex part, the following will only return the last value in the
        //block when the block is evaluated, but it will trigger execution of the rest.
        //This gives it functionality like a conventional function in imperative languages
        Parser<var>
                or = (or(parentParser, parentParser.between(OP_NL("{"), NL_OP("}"))))
                             .sepBy1(SEMICOLON_TERMINATOR).followedBy(
                        SEMICOLON_TERMINATOR.optional())
                             .map(var -> var).token().map(new BlockOperator(this, pure));
        ref.set(or);
        return or;
    }

    private Parser<var> expression(@Nullable Parser.Reference<var> ref,
                                   @NotNull final boolean pure) throws Exception {
        Parser<var> main;
        if (ref == null) {
            ref = Parser.newReference();
        }
        if (!pure) {
            main = ref.lazy()
                           .between(OP("("), OP(")"))
                           .or(or(unitValue(pure), list(ref.lazy(), pure), map(ref.lazy(), pure),
                                  pureDeclarationOperator(ref, pure),
                                  KEYWORD("pure").next(
                                          expression(null,
                                                     true)),
                                  moduleStatement(ref), assertOperator(ref, pure),
                                  collectStatement(ref.lazy(), pure),
                                  whenStatement(ref.lazy(), pure), everyStatement(ref.lazy(), pure),
                                  functionCall(ref, pure),
                                  java(), URL, DECIMAL_LITERAL, INTEGER_LITERAL,
                                  STRING_LITERAL,
                                  dollarIdentifier(ref, pure), IDENTIFIER_KEYWORD,
                                  BUILTIN.token().map(new Map<Token, var>() {
                                      public var map(@NotNull Token token) {
                                          final var v = (var) token.value();
                                          return createReactiveNode(
                                                  false, v.toHumanString(), DollarParserImpl.this, token,
                                                  v,
                                                  args -> Builtins.execute(v.toHumanString(), Arrays
                                                                                                      .asList(),
                                                                           pure)
                                          );
                                      }
                                  }),
                                  identifier().followedBy(OP("(").not().peek()).token().map(
                                          new Map<Token, var>() {
                                              public var map(@NotNull Token token) {
//                                    log.debug("GET (parsing) " + currentScope());
//                                    new Throwable().printStackTrace(System.err);
                                                  return getVariable(pure, token.value().toString(),
                                                                     false, null, token,
                                                                     DollarParserImpl.this);
                                              }
                                          })))
                           .or(block(ref.lazy(), pure).between(OP_NL("{"), NL_OP("}")));
        } else {
            main = ref.lazy()
                           .between(OP("("), OP(")"))
                           .or(or(unitValue(pure),
                                  list(ref.lazy(), pure),
                                  map(ref.lazy(), pure),
                                  assertOperator(ref, pure),
                                  collectStatement(ref.lazy(), pure),
                                  whenStatement(ref.lazy(), pure),
                                  functionCall(ref, pure),
                                  DECIMAL_LITERAL,
                                  INTEGER_LITERAL,
                                  STRING_LITERAL,
                                  IDENTIFIER_KEYWORD,
                                  identifier().followedBy(OP("(").not().peek()).token().map(
                                          new Map<Token, var>() {
                                              public var map(@NotNull Token token) {
                                                  return getVariable(pure, token.value().toString(),
                                                                     false, null,
                                                                     token, DollarParserImpl.this);
                                              }
                                          }),
                                  BUILTIN.token().map(new Map<Token, var>() {
                                      public var map(@NotNull Token token) {
                                          final var v = (var) token.value();
                                          return createReactiveNode(
                                                  false, v.toHumanString(), DollarParserImpl.this, token,
                                                  v,
                                                  args -> Builtins.execute(v.toHumanString(),
                                                                           emptyList(), pure)
                                          );
                                      }
                                  })))
                           .or(block(ref.lazy(), pure).between(OP_NL("{"), NL_OP("}")));
        }

        OperatorTable<var> table = new OperatorTable<var>()
                                           .infixl(op(new BinaryOp(this, "not-equal",
                                                                   (lhs, rhs) -> $(
                                                                           !lhs.equals(rhs))),
                                                      "!="),
                                                   EQUIVALENCE_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "equal", (lhs, rhs) -> $(
                                                   lhs.equals(rhs))), "=="), EQUIVALENCE_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "and", (lhs, rhs) -> $(
                                                   lhs.isTrue() && rhs.isTrue())), "&&", "and"),
                                                   LOGICAL_AND_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "or", (lhs, rhs) -> $(
                                                   lhs.isTrue() || rhs.isTrue())), "||", "or"),
                                                   LOGICAL_OR_PRIORITY)
                                           .postfix(pipeOperator(ref, pure), PIPE_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "range",
                                                                   (lhs, rhs) -> fromValue(
                                                                           new Range(lhs, rhs))),
                                                      ".."),
                                                   RANGE_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "less-than",
                                                                   (lhs, rhs) -> $(
                                                                           lhs.compareTo(rhs) < 0)),
                                                      "<"),
                                                   COMPARISON_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "greater-than",
                                                                   (lhs, rhs) -> $(
                                                                           lhs.compareTo(rhs) > 0)),
                                                      ">"),
                                                   EQUIVALENCE_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "less-than-equal",
                                                                   (lhs, rhs) -> $(lhs.compareTo(
                                                                           rhs) <= 0)), "<="),
                                                   EQUIVALENCE_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "greater-than-equal",
                                                                   (lhs, rhs) -> $(lhs.compareTo(
                                                                           rhs) >= 0)), ">="),
                                                   EQUIVALENCE_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "multiply", (lhs, rhs) -> {
                                               final var lhsFix = lhs._fix(false);
                                               if (Arrays.asList("block","map","list").contains(lhs.getMetaAttribute("operation"))) {
                                                   var newValue = lhsFix._fixDeep(false);
                                                   Long max = rhs.toLong();
                                                   for (int i = 1; i < max; i++) {
                                                       newValue = newValue.$plus(lhs._fixDeep());
                                                   }
                                                   return newValue;
                                               } else {
                                                   return lhsFix.$multiply(rhs);
                                               }
                                           }), "*"), MULTIPLY_DIVIDE_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "divide",
                                                                   NumericAware::$divide), "/"),
                                                   MULTIPLY_DIVIDE_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "modulus",
                                                                   NumericAware::$modulus), "%"),
                                                   MULTIPLY_DIVIDE_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "plus", var::$plus), "+"),
                                                   PLUS_MINUS_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "minus", var::$minus),
                                                      "-"), PLUS_MINUS_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "pair",
                                                                   (lhs, rhs) -> $(lhs.$S(), rhs)),
                                                      ":"), 30)

                                           .infixl(assertEqualsOp(false, "<=>"),
                                                   LINE_PREFIX_PRIORITY)
                                           .infixl(assertEqualsOp(true, "<->"),
                                                   LINE_PREFIX_PRIORITY)
                                           .prefix(op(
                                                   new UnaryOp("truthy", i -> $(i.truthy()), this),
                                                   "~", "truthy"), UNARY_PRIORITY)
                                           .prefix(op(new UnaryOp("size", var::$size, this), "#",
                                                      "size"), UNARY_PRIORITY)
                                           .infixl(op(new BinaryOp(
                                                                          this, "else",
                                                                          (lhs, rhs) -> {
                                                                              final var fixLhs = lhs._fixDeep();
                                                                              if (fixLhs.isBoolean() && fixLhs.isFalse()) {
                                                                                  return rhs._fix(2,
                                                                                                  false);
                                                                              } else {
                                                                                  return fixLhs;
                                                                              }
                                                                          }), "-:", "else"),
                                                   IF_PRIORITY)
                                           .prefix(ifOperator(ref), IF_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "in",
                                                                   (lhs, rhs) -> rhs.$contains(
                                                                           lhs)), "€", "in"),
                                                   IN_PRIORITY)
                                           .prefix(op(new UnaryOp("error",
                                                                  v -> currentScope().addErrorHandler(
                                                                          v), this), "!?#*!",
                                                      "error"), LINE_PREFIX_PRIORITY)
                                           .postfix(isOperator(), EQUIVALENCE_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "each", (lhs, rhs) -> {
                                                       return lhs.$each(i -> inSubScope(false, pure, "each",
                                                                                        newScope -> {
                                                                                            newScope.setParameter(
                                                                                                    "1",
                                                                                                    i[0]);
                                                                                            return rhs._fixDeep(
                                                                                                    false);
                                                                                        }));
                                                   }), "*|*", "each"),
                                                   MULTIPLY_DIVIDE_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "reduce", (lhs, rhs) -> {
                                               return lhs.$list().$stream(false).reduce((x, y) -> {
                                                   try {
                                                       return (var) inSubScope(false, pure,
                                                                               "reduce",
                                                                               newScope -> {
                                                                                   newScope.setParameter(
                                                                                           "1", x);
                                                                                   newScope.setParameter(
                                                                                           "2", y);
                                                                                   return rhs._fixDeep(
                                                                                           false);
                                                                               });
                                                   } catch (Exception e) {
                                                       throw new DollarScriptException(e);
                                                   }
                                               }).get();
                                           }), "*|", "reduce"), MULTIPLY_DIVIDE_PRIORITY)
                                           .infixl(op(new ListenOperator(pure, this), "?->",
                                                      "causes"), CONTROL_FLOW_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "listen",
                                                                   (lhs, rhs) -> lhs.isTrue() ? fix(
                                                                           rhs, false) : $void()),
                                                      "?"),
                                                   CONTROL_FLOW_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "choose",
                                                                   ControlFlowAware::$choose), "?*",
                                                      "choose"),
                                                   CONTROL_FLOW_PRIORITY)
                                           .infixl(op(new BinaryOp(this, "default", var::$default),
                                                      "|", "default"), CONTROL_FLOW_PRIORITY)
                                           .postfix(memberOperator(ref), MEMBER_PRIORITY)
                                           .prefix(op(new UnaryOp("not", v -> $(!v.isTrue()), this),
                                                      "!", "not"), UNARY_PRIORITY)
                                           .postfix(op(new UnaryOp("dec", var::$dec, this), "--"),
                                                    INC_DEC_PRIORITY)
                                           .postfix(op(new UnaryOp("inc", var::$inc, this), "++"),
                                                    INC_DEC_PRIORITY)
                                           .prefix(op(new UnaryOp("negate", var::$negate, this),
                                                      "-"), UNARY_PRIORITY)
                                           .prefix(op(new UnaryOp(true, v -> v._fixDeep(true),
                                                                  "parallel", this), "|:|",
                                                      "parallel"),
                                                   SIGNAL_PRIORITY)
                                           .prefix(op(new UnaryOp(true, v -> v._fixDeep(false),
                                                                  "serial", this), "|..|",
                                                      "serial"),
                                                   SIGNAL_PRIORITY)
                                           .prefix(forOperator(ref, pure), UNARY_PRIORITY)
                                           .prefix(whileOperator(ref, pure), UNARY_PRIORITY)
                                           .postfix(subscriptOperator(ref), MEMBER_PRIORITY)
                                           .postfix(parameterOperator(ref, pure), MEMBER_PRIORITY)
                                           .prefix(op(
                                                   new UnaryOp(true, v -> v._fixDeep(false),
                                                               "fix",
                                                               this), "&", "fix"), 1000)
                                           .postfix(castOperator(), UNARY_PRIORITY)
                                           .prefix(variableUsageOperator(pure), 1000)
                                           .prefix(assignmentOperator(ref, pure),
                                                   ASSIGNMENT_PRIORITY)
                                           .prefix(definitionOperator(ref, pure),
                                                   ASSIGNMENT_PRIORITY);

        if (!pure) {
            table = table.prefix(op(new UnaryOp(false, i -> {
                i.out();
                return $void();
            }, "print", this), "@@", "print"), LINE_PREFIX_PRIORITY)
                            .prefix(op(new UnaryOp(false, i -> {
                                i.debug();
                                return $void();
                            }, "debug", this), "!!", "debug"), LINE_PREFIX_PRIORITY)
                            .prefix(op(new UnaryOp(false, i -> {
                                i.err();
                                return $void();
                            }, "err", this), "??", "err"), LINE_PREFIX_PRIORITY)
                            .prefix(writeOperator(ref), OUTPUT_PRIORITY)
                            .prefix(readOperator(), OUTPUT_PRIORITY)
                            .prefix(op(new UnaryOp("stop", var::$stop, this), "(!)"),
                                    SIGNAL_PRIORITY)
                            .prefix(op(new UnaryOp("start", var::$start, this), "(>)"),
                                    SIGNAL_PRIORITY)
                            .prefix(op(new UnaryOp("pause", var::$pause, this), "(=)"),
                                    SIGNAL_PRIORITY)
                            .prefix(op(new UnaryOp("unpause", var::$unpause, this), "(~)"),
                                    SIGNAL_PRIORITY)
                            .prefix(op(new UnaryOp("destroy", var::$destroy, this), "(-)"),
                                    SIGNAL_PRIORITY)
                            .prefix(op(new UnaryOp("create", var::$create, this), "(+)"),
                                    SIGNAL_PRIORITY)
                            .prefix(op(new UnaryOp("state", var::$state, this), "(?)"),
                                    SIGNAL_PRIORITY)
                            .prefix(op(new UnaryOp("fork", v -> DollarFactory.fromFuture(
                                    executor.executeInBackground(() -> fix(v, false))), this), "-<",
                                       "fork"),
                                    SIGNAL_PRIORITY)
                            .infixl(op(new BinaryOp(this, "fork", (lhs, rhs) -> rhs.$publish(lhs)),
                                       "*>",
                                       "publish"), OUTPUT_PRIORITY)
                            .infixl(op(new SubscribeOperator(pure, this), "<*", "subscribe"),
                                    OUTPUT_PRIORITY)
                            .infixl(op(new BinaryOp(this, "write-simple",
                                                    (lhs, rhs) -> rhs.$write(lhs)), ">>"),
                                    OUTPUT_PRIORITY)
                            .prefix(op(new SimpleReadOperator(this), "<<"), OUTPUT_PRIORITY)
                            .prefix(op(new UnaryOp("drain", URIAware::$drain, this), "<--",
                                       "drain"), OUTPUT_PRIORITY)
                            .prefix(op(new UnaryOp("all", URIAware::$all, this), "<@", "all"),
                                    OUTPUT_PRIORITY);

        }
        Parser<var> parser = table.build(main);

        ref.set(parser.map(new Function<var, var>() {
            @Override
            public var apply(var var) {
                return var;
            }
        }));
        return parser;

    }

    @NotNull
    private Parser<BinaryOp> assertEqualsOp(boolean immediate, String opStr) {
        return op(new BinaryOp(immediate, "assert-equal", this, (lhs, rhs) -> {

            final var lhsFix = lhs._fixDeep(false);
            final var rhsFix = rhs._fixDeep(false);
            if (lhsFix.equals(rhsFix)) {
                return $(true);
            } else {
                throw new DollarScriptFailureException(ErrorType.ASSERTION,
                                                       lhsFix.toDollarScript() +
                                                               " != " +
                                                               rhsFix.toDollarScript());
            }
        }), opStr);
    }

    private Parser<var> unitValue(boolean pure) {
        return array(DECIMAL_LITERAL.or(INTEGER_LITERAL), BUILTIN).token().map(
                new UnitOperator(this, pure));
    }

    private Parser<var> list(@NotNull Parser<var> expression, boolean pure) {
        return OP_NL("[")
                       .next(expression.sepBy(COMMA_OR_NEWLINE_TERMINATOR))
                       .followedBy(COMMA_OR_NEWLINE_TERMINATOR.optional())
                       .followedBy(NL_OP("]")).token().map(
                        new ListOperator(this, pure));
    }

    private Parser<var> map(@NotNull Parser<var> expression, boolean pure) {
        Parser<List<var>>
                sequence =
                OP_NL("{").next(expression.sepBy(COMMA_TERMINATOR))
                        .followedBy(COMMA_TERMINATOR.optional())
                        .followedBy(NL_OP("}"));
        return sequence.token().map(new MapOperator(this, pure));
    }

    private Parser<var> moduleStatement(@NotNull Parser.Reference<var> ref) {
        final Parser<Object[]> param = array(IDENTIFIER.followedBy(OP("=")), ref.lazy());

        final Parser<List<var>> parameters =
                KEYWORD("with").optional().next((param).map(objects -> {
                    var result = (var) objects[1];
                    result.setMetaAttribute(NAMED_PARAMETER_META_ATTR, objects[0].toString());
                    return result;
                }).sepBy(OP(",")).between(OP("("), OP(")")));

        return array(KEYWORD("module"), STRING_LITERAL.or(URL),
                     parameters.optional()).token().map(new ModuleOperator(this));

    }

    private Parser<var> assertOperator(@NotNull Parser.Reference<var> ref, boolean pure) {
        return OP(".:").next(
                or(array(STRING_LITERAL.followedBy(OP(":")), ref.lazy()),
                   array(OP(":").optional(), ref.lazy()))
                        .token()
                        .map(new AssertOperator(this)));
    }

    private Parser<var> collectStatement(Parser<var> expression, boolean pure) {
        Parser<Object[]> parser = KEYWORD_NL("collect")
                                          .next(
                                                  array(expression,
                                                        KEYWORD("until").next(expression).optional(),
                                                        KEYWORD("unless").next(expression).optional(),
                                                        expression)
                                          );
        return parser.token().map(new CollectOperator(this, pure));
    }

    private Parser<var> whenStatement(Parser<var> expression, boolean pure) {
        Parser<Object[]> sequence = KEYWORD_NL("when").next(array(expression, expression));
        return sequence.token().map(new WhenOperator(this));
    }

    private Parser<var> everyStatement(Parser<var> expression, boolean pure) {
        Parser<Object[]> sequence = KEYWORD_NL("every")
                                            .next(array(unitValue(pure),
                                                        KEYWORD("until").next(
                                                                expression).optional(),
                                                        KEYWORD("unless").next(
                                                                expression).optional(),
                                                        expression));
        return sequence.token().map(new EveryOperator(this, pure));
    }

    private Parser<var> functionCall(@NotNull Parser.Reference<var> ref, boolean pure) {
        return array(IDENTIFIER.or(BUILTIN).followedBy(OP("(").peek()))
                       .token().map(new FunctionCallOperator(this));
    }

    final Parser<var> java() {
        return token(new TokenMap<String>() {
            @Nullable
            @Override
            public String map(@NotNull Token token) {
                final Object val = token.value();
                if (val instanceof Tokens.Fragment) {
                    Tokens.Fragment c = (Tokens.Fragment) val;
                    if (!c.tag().equals("java")) {
                        return null;
                    }
                    return c.text();
                } else {
                    return null;
                }
            }

            @NotNull
            @Override
            public String toString() {
                return "java";
            }
        }).token().map(new Function<Token, var>() {
            @Override
            public var apply(Token token) {
                return DollarScriptSupport.createNode(false, "java", DollarParserImpl.this, token,
                                                      Arrays.asList($void()),
                                                      in -> JavaScriptingSupport.compile($void(),
                                                                                         (String) token
                                                                                                          .value()));
            }
        });
    }

    private <T> Parser<T> op(T value, String name) {
        return op(value, name, null);
    }

    private <T> Parser<T> op(T value, String name, @Nullable String keyword) {
        Parser<?> parser;
        if (keyword == null) {
            parser = OP(name);
        } else {
            parser = OP(name, keyword);

        }
        return parser.token().map(new SourceMapper<>(value));

    }

    private Parser<Map<? super var, ? extends var>> pipeOperator(@NotNull Parser.Reference<var> ref,
                                                                 boolean pure) {
        return (OP("->").optional(null)).next(
                Parsers.longest(BUILTIN, IDENTIFIER, functionCall(ref, pure).postfix
                                                                                     (parameterOperator(
                                                                                             ref,
                                                                                             pure)
                                                                                     ),
                                ref.lazy().between(OP("("), OP(")"))))
                       .token().map(new PipeOperator(this, pure));
    }

    private Parser<Map<? super var, ? extends var>> writeOperator(@NotNull Parser.Reference<var> ref) {
        return array(KEYWORD("write"), ref.lazy(), KEYWORD("block").optional(),
                     KEYWORD("mutate").optional())
                       .followedBy(KEYWORD("to").optional())
                       .token().map(new WriteOperator(this));
    }

    private Parser<Map<? super var, ? extends var>> readOperator() {
        return array(KEYWORD("read"), KEYWORD("block").optional(), KEYWORD("mutate").optional())
                       .followedBy(KEYWORD("from").optional()).
                                                                      token().map(
                        new ReadOperator(this));
    }

    private Parser<Map<var, var>> ifOperator(@NotNull Parser.Reference<var> ref) {
        return KEYWORD_NL("if").next(ref.lazy()).token().map(new IfOperator(this));
    }

    private Parser<Map<? super var, ? extends var>> isOperator() {
        return KEYWORD("is").next(IDENTIFIER.sepBy(OP(","))).token().map(new IsOperator(this));
    }

    private Parser<Map<? super var, ? extends var>> memberOperator(@NotNull Parser.Reference<var> ref) {
        return OP(".").followedBy(OP(".").not())
                       .next(ref.lazy().between(OP("("), OP(")")).or(IDENTIFIER))
                       .token().map(new Function<Token, Map<? super var, ? extends var>>() {

                    @Override
                    public Map<? super var, ? extends var> apply(Token rhs) {

                        return new Map<var, var>() {

                            @Override
                            public var map(var lhs) {
                                return createReactiveNode(
                                        false, "." + rhs.toString(), DollarParserImpl.this, rhs, lhs,
                                        (var) rhs.value(), args -> lhs.$(rhs.value().toString())
                                );
                            }
                        };
                    }
                });
    }

    private Parser<Map<? super var, ? extends var>> forOperator(final @NotNull Parser.Reference<var> ref,
                                                                boolean pure) {
        return array(KEYWORD("for"), IDENTIFIER, KEYWORD("in"), ref.lazy()).token().map(
                new ForOperator(this, pure));
    }

    private Parser<Map<? super var, ? extends var>> whileOperator(final @NotNull Parser.Reference<var> ref,
                                                                  boolean pure) {
        return KEYWORD("while").next(ref.lazy()).token().map(new WhileOperator(this, pure));
    }

    private Parser<Map<? super var, ? extends var>> subscriptOperator(@NotNull Parser.Reference<var> ref) {
        return OP("[").next(
                array(ref.lazy().followedBy(OP("]")), OP("=").next(ref.lazy()).optional()))
                       .token().map(new SubscriptOperator(this));
    }

    private Parser<Map<? super var, ? extends var>> parameterOperator(@NotNull Parser.Reference<var> ref,
                                                                      boolean pure) {
        return OP("(").next(
                or(array(IDENTIFIER.followedBy(OP("=")), ref.lazy()),
                   array(OP("=").optional(), ref.lazy())).map(
                        objects -> {
                            //Is it a named parameter
                            if (objects[0] != null) {
                                //yes so let's add the name as metadata to the value
                                var result = (var) objects[1];
                                result.setMetaAttribute(NAMED_PARAMETER_META_ATTR,
                                                        objects[0].toString());
                                return result;
                            } else {
                                //no, just use the value
                                return (var) objects[1];
                            }
                        }).sepBy(COMMA_TERMINATOR)).followedBy(OP(")")).token().map(
                new ParameterOperator(this, pure));
    }

    private Parser<Map<? super var, ? extends var>> variableUsageOperator(boolean pure) {
        return or(OP("$").followedBy(OP("(").peek()).token()
                          .map(new VariableUsageOperator(pure, this, false)),
                  OP("$").followedBy(INTEGER_LITERAL.peek()).token().map(
                          (Token lhs) -> {
                              return new Map<var, var>() {
                                  @Override
                                  public var map(var rhs) {
                                      return getVariable(pure, rhs.toString(), true, null, lhs,
                                                         DollarParserImpl.this);
                                  }
                              };
                          }));
    }

    private Parser<Map<? super var, ? extends var>> castOperator() {
        return KEYWORD("as").next(IDENTIFIER).token().map(new CastOperator(this));
    }

    private Parser<Map<? super var, ? extends var>> assignmentOperator(
                                                                              @NotNull Parser.Reference<var> ref,
                                                                              boolean pure) {
        return array(KEYWORD("export").optional(),
                     or(KEYWORD("const"), KEYWORD("volatile"), KEYWORD("var")).optional(),
                     IDENTIFIER.between(OP("<"), OP(">")).optional(),
                     ref.lazy().between(OP("("), OP(")")).optional(),
                     OP("$").next(ref.lazy().between(OP("("), OP(")"))).or(IDENTIFIER).or(BUILTIN),
                     or(OP("="), OP("?="), OP("*="))
        ).token().map(new AssignmentOperator(false, pure, this));
    }

    private Parser<Map<? super var, ? extends var>> definitionOperator(
                                                                              @NotNull Parser.Reference<var> ref,
                                                                              boolean pure) {

        return
                or(
                        array(
                                KEYWORD("export").optional(null),
                                or(KEYWORD("const")).optional(null),
                                IDENTIFIER.between(OP("<"), OP(">")).optional(),
                                OP("$").next(ref.lazy().between(OP("("), OP(")"))).or(IDENTIFIER),
                                OP(":=")
                        ),
                        array(KEYWORD("export").optional(),
                              IDENTIFIER.between(OP("<"), OP(">")).optional(), KEYWORD("def"),
                              IDENTIFIER)
                ).token().map(new DefinitionOperator(pure, this));
    }

    private Parser<var> pureDeclarationOperator(
                                                       @NotNull Parser.Reference<var> ref,
                                                       boolean pure) throws Exception {

        return KEYWORD("pure").next(
                or(
                        array(
                                KEYWORD("export").optional(null),
                                or(KEYWORD("const")).optional(null),
                                IDENTIFIER.between(OP("<"), OP(">")).optional(),
                                OP("$").next(ref.lazy().between(OP("("), OP(")"))).or(IDENTIFIER),
                                OP(":="), expression(null, true)
                        ),
                        array(KEYWORD("export").optional(),
                              IDENTIFIER.between(OP("<"), OP(">")).optional(), KEYWORD("def"),
                              IDENTIFIER, expression(null, true))
                )
        ).token().map(
                new DefinitionOperator(true, this)).map(
                new Map<Map<? super var, ? extends var>, var>() {
                    @Override
                    public var map(@NotNull Map<? super var, ? extends var> map) {
                        return map.map(null);
                    }
                });
    }

    private class SourceMapper<T> implements Map<Token, T> {
        private final T value;


        SourceMapper(T value) {
            this.value = value;
        }

        @Override
        public T map(Token token) {
            if (value instanceof Operator) {
                ((Operator) value).setSource(new SourceSegmentValue(currentScope(), token));
            }
            return value;
        }
    }
}
