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
import com.sillelien.dollar.api.VarInternal;
import com.sillelien.dollar.api.execution.DollarExecutor;
import com.sillelien.dollar.api.plugin.Plugins;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.ParserErrorHandler;
import dollar.internal.runtime.script.api.ParserOptions;
import dollar.internal.runtime.script.api.Scope;
import dollar.internal.runtime.script.java.JavaScriptingSupport;
import dollar.internal.runtime.script.operators.AssertOperator;
import dollar.internal.runtime.script.operators.AssignmentOperator;
import dollar.internal.runtime.script.operators.BlockOperator;
import dollar.internal.runtime.script.operators.CastOperator;
import dollar.internal.runtime.script.operators.CausesOperator;
import dollar.internal.runtime.script.operators.CollectOperator;
import dollar.internal.runtime.script.operators.DefinitionOperator;
import dollar.internal.runtime.script.operators.EveryOperator;
import dollar.internal.runtime.script.operators.ForOperator;
import dollar.internal.runtime.script.operators.Func;
import dollar.internal.runtime.script.operators.FunctionCallOperator;
import dollar.internal.runtime.script.operators.IfOperator;
import dollar.internal.runtime.script.operators.IsOperator;
import dollar.internal.runtime.script.operators.MapOperator;
import dollar.internal.runtime.script.operators.ModuleOperator;
import dollar.internal.runtime.script.operators.ParameterOperator;
import dollar.internal.runtime.script.operators.PipeOperator;
import dollar.internal.runtime.script.operators.ReadOperator;
import dollar.internal.runtime.script.operators.SubscriptOperator;
import dollar.internal.runtime.script.operators.UnitOperator;
import dollar.internal.runtime.script.operators.VariableUsageOperator;
import dollar.internal.runtime.script.operators.WhenOperator;
import dollar.internal.runtime.script.operators.WhileOperator;
import dollar.internal.runtime.script.operators.WriteOperator;
import dollar.internal.runtime.script.parser.OpDef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.OperatorTable;
import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Terminals;
import org.jparsec.Token;
import org.jparsec.TokenMap;
import org.jparsec.Tokens;
import org.jparsec.error.ParserException;
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

import static com.sillelien.dollar.api.DollarStatic.$;
import static com.sillelien.dollar.api.DollarStatic.$void;
import static dollar.internal.runtime.script.DollarLexer.*;
import static dollar.internal.runtime.script.DollarScriptSupport.*;
import static dollar.internal.runtime.script.OperatorPriority.*;
import static dollar.internal.runtime.script.SourceNodeOptions.NO_SCOPE;
import static dollar.internal.runtime.script.parser.Symbols.*;
import static java.util.Collections.emptyList;
import static org.jparsec.Parsers.*;

public class DollarParserImpl implements DollarParser {
    public static final String NAMED_PARAMETER_META_ATTR = "__named_parameter";
    @Nullable
    public static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);
    //Lexer
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("DollarParser");
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

    public DollarParserImpl(@NotNull ParserOptions options, @NotNull ClassLoader classLoader, @NotNull File dir) {
        this.options = options;
        this.classLoader = classLoader;
        this.sourceDir = dir;
    }

    private Parser<var> dollarIdentifier(@NotNull Parser.Reference ref, boolean pure) {
        return OP(DOLLAR).next(
                array(Terminals.Identifier.PARSER, OP(DEFAULT).next(ref.lazy()).optional()).between(
                        OP(LEFT_BRACE),
                        OP(RIGHT_BRACE))).token().map(
                t -> {
                    Object[] objects = (Object[]) t.value();
                    return getVariable(pure, objects[0].toString(), false, (var) objects[1], t,
                                       this);
                });
    }


    @Override
    public void export(@NotNull String name, @NotNull var export) {
//        System.err.println("Exporting " + name);
        export.setMetaObject("scopes", new ArrayList<Scope>(DollarScriptSupport.scopes()));
        exports.put(name, export);
    }

    @Override
    @NotNull
    public ParserErrorHandler getErrorHandler() {
        return errorHandler;
    }


    @NotNull
    @Override
    public ParserOptions options() {
        return options;
    }

//    private Parser<var> arrayElementExpression(Parser<var> expression1, Parser<var> expression2, Script) {
//        return expression1.infixl(term(LEFT_BRACKET).next(expression2).followedBy(term(RIGHT_BRACKET)));
//    }

    @Override
    @NotNull
    public var parse(@NotNull ScriptScope scope, @NotNull String source) throws Exception {
        return DollarScriptSupport.inScope(false, scope, newScope -> {
            DollarStatic.context().setClassLoader(classLoader);
            Parser<?> parser = buildParser(false, scope);
            try {
                var parse = (var) parser.from(TOKENIZER, DollarLexer.IGNORED).parse(source);
            } catch (ParserException e) {
                getErrorHandler().handleTopLevel(e, null, file != null ? new File(file) : null);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
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
    public var parse(@NotNull InputStream in, boolean parallel, @NotNull Scope scope) throws Exception {
        String source = new String(ByteStreams.toByteArray(in));
        return parse(new ScriptScope(scope, "(stream)", source, "(stream-scope)", true), source);
    }

    @Override
    @NotNull
    public var parse(@NotNull InputStream in, @NotNull String file, boolean parallel) throws Exception {
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
    private var parseMarkdown(@NotNull File file) throws IOException {
        PegDownProcessor pegDownProcessor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS);
        RootNode root =
                pegDownProcessor.parseMarkdown(
                        com.google.common.io.Files.toString(file, Charset.forName("utf-8"))
                                .toCharArray());
        root.accept(new CodeExtractionVisitor());
        return $();
    }


    private Parser<?> buildParser(@NotNull boolean pure, @NotNull Scope scope) throws Exception {
        topLevelParser = script(scope, pure);
        return topLevelParser;
    }


    @NotNull
    private Parser<var> script(@NotNull Scope scope, @NotNull boolean pure) throws Exception {
        log.debug("Starting Parse Phase");


        Parser.Reference<var> ref = Parser.newReference();
        Parser<var> block = block(ref.lazy(), pure).between(OP_NL(LEFT_BRACE), NL_OP(RIGHT_BRACE));
        Parser<var> expression = expression(null, pure);
        Parser<var> parser = (TERMINATOR_SYMBOL.optional()).next(or(expression).followedBy(
                TERMINATOR_SYMBOL).many1()).map(l -> {
            log.debug("Ended Parse Phase");
            log.debug("Starting Runtime Phase");
            for (int i = 0; i < l.size() - 1; i++) {
                var fixed = l.get(i)._fixDeep(false);
//                System.err.println(fixed);
            }
            var resultVar = l.get(l.size() - 1);
            var fixedResult = resultVar._fixDeep(false);
            log.debug("Ended Runtime Phase");
            return fixedResult;
        });
        ref.set(parser);
        return parser;

    }

    @NotNull
    private Parser<var> block(@NotNull Parser<var> parentParser, boolean pure) throws Exception {
        Parser.Reference<var> ref = Parser.newReference();
        //Now we do the complex part, the following will only return the last value in the
        //block when the block is evaluated, but it will trigger execution of the rest.
        //This gives it functionality like a conventional function in imperative languages
        Parser<var> or = (or(parentParser, parentParser.between(OP_NL(LEFT_BRACE), NL_OP(RIGHT_BRACE))))
                                 .sepBy1(SEMICOLON_TERMINATOR)
                                 .followedBy(SEMICOLON_TERMINATOR.optional())
                                 .map(var -> var).token().map(new BlockOperator(this, pure));
        ref.set(or);
        return or;
    }

    @NotNull
    private Parser<var> expression(@Nullable Parser.Reference<var> ref,
                                   final boolean pure) throws Exception {
        Parser<var> main;
        if (ref == null) {
            ref = Parser.newReference();
        }
        if (!pure) {
            main = ref.lazy()
                           .between(OP(LEFT_PAREN), OP(RIGHT_PAREN))
                           .or(or(unitValue(pure), list(ref.lazy(), pure), map(ref.lazy(), pure),
                                  pureDeclarationOperator(ref, pure),
                                  KEYWORD(PURE).next(expression(null, true)),
                                  moduleStatement(ref), assertOperator(ref, pure),
                                  collectStatement(ref.lazy(), pure),
                                  whenStatement(ref.lazy(), pure), everyStatement(ref.lazy(), pure),
                                  functionCall(),
                                  java(), URL, DECIMAL_LITERAL, INTEGER_LITERAL,
                                  STRING_LITERAL,
                                  dollarIdentifier(ref, pure), IDENTIFIER_KEYWORD,
                                  builtin(pure),
                                  variableRef(pure)))
                           .or(block(ref.lazy(), pure).between(OP_NL(LEFT_BRACE), NL_OP(RIGHT_BRACE)));
        } else {
            main = ref.lazy()
                           .between(OP(LEFT_PAREN), OP(RIGHT_PAREN))
                           .or(or(unitValue(pure),
                                  list(ref.lazy(), pure),
                                  map(ref.lazy(), pure),
                                  assertOperator(ref, pure),
                                  collectStatement(ref.lazy(), pure),
                                  whenStatement(ref.lazy(), pure),
                                  functionCall(),
                                  DECIMAL_LITERAL,
                                  INTEGER_LITERAL,
                                  STRING_LITERAL,
                                  IDENTIFIER_KEYWORD,
                                  variableRef(pure),
                                  builtin(pure)))
                           .or(block(ref.lazy(), pure).between(OP_NL(LEFT_BRACE), NL_OP(RIGHT_BRACE)));
        }

        OperatorTable<var> table = new OperatorTable<var>();
        table = table.infixl(op(INEQUALITY_OPERATOR, new BinaryOp(this, INEQUALITY_OPERATOR, Func::inequality)),
                             INEQUALITY_OPERATOR.priority())
                        .infixl(op(EQUALITY, new BinaryOp(this, EQUALITY, Func::equality)), EQUALITY.priority())
                        .infixl(op(AND, new BinaryOp(this, AND, Func::and)), AND.priority())
                        .infixl(op(OR, new BinaryOp(this, OR, Func::orFunc)), OR.priority())
                        .postfix(pipeOperator(ref, pure), PIPE_OPERATOR.priority())
                        .infixl(op(RANGE, new BinaryOp(this, RANGE, Func::range)), RANGE.priority())
                        .infixl(op(LT, new BinaryOp(this, LT, Func::lt)), LT.priority())
                        .infixl(op(GT, new BinaryOp(this, GT, Func::gt)), GT.priority())
                        .infixl(op(LT_EQUALS, new BinaryOp(this, LT_EQUALS, Func::lte)), LT_EQUALS.priority())
                        .infixl(op(GT_EQUALS, new BinaryOp(this, GT_EQUALS, Func::gte)), GT_EQUALS.priority())
                        .infixl(op(MULTIPLY, new BinaryOp(this, MULTIPLY, Func::multiply)), MULTIPLY.priority())
                        .infixl(op(DIVIDE, new BinaryOp(this, DIVIDE, NumericAware::$divide)), DIVIDE.priority())
                        .infixl(op(MOD, new BinaryOp(this, MOD, NumericAware::$modulus)), MOD.priority())
                        .infixl(op(PLUS, new BinaryOp(this, PLUS, var::$plus)), PLUS.priority())
                        .infixl(op(MINUS, new BinaryOp(this, MINUS, var::$minus)), MINUS.priority())
                        .infixl(op(PAIR, new BinaryOp(this, PAIR, Func::pair)), PAIR.priority())
                        .infixl(op(ASSERT_EQ_REACT, new BinaryOp(false, ASSERT_EQ_REACT, this, Func::assertEquals)),
                                ASSERT_EQ_REACT.priority())
                        .infixl(op(ASSERT_EQ_UNREACT, new BinaryOp(true, ASSERT_EQ_UNREACT, this, Func::assertEquals)),
                                ASSERT_EQ_UNREACT.priority())
                        .prefix(op(TRUTHY, new UnaryOp(this, TRUTHY, Func::truthy)), TRUTHY.priority())
                        .prefix(op(SIZE, new UnaryOp(this, SIZE, var::$size)), SIZE.priority())
                        .infixl(op(ELSE, new BinaryOp(this, ELSE, Func::elseFunc)), ELSE.priority())
                        .prefix(ifOperator(ref), IF_OPERATOR.priority())
                        .infixl(op(IN, new BinaryOp(this, IN, (lhs, rhs) -> rhs.$contains(lhs))), IN.priority())
                        .prefix(op(ERROR, new UnaryOp(this, ERROR, Func::error)), ERROR.priority())
                        .postfix(isOperator(), EQ_PRIORITY)
                        .infixl(op(EACH, new BinaryOp(this, EACH, (lhs, rhs) -> Func.each(pure, lhs, rhs))),
                                EACH.priority())
                        .infixl(op(REDUCE, new BinaryOp(this, REDUCE, (lhs, rhs) -> Func.reduce(pure, lhs, rhs))),
                                REDUCE.priority())
                        .infixl(op(CAUSES, new CausesOperator(pure, this)), CAUSES.priority())
                        .infixl(op(LISTEN, new BinaryOp(this, LISTEN, Func::listenFunc)), LISTEN.priority())
                        .infixl(op(CHOOSE, new BinaryOp(this, CHOOSE, ControlFlowAware::$choose)), CHOOSE.priority())
                        .infixl(op(DEFAULT, new BinaryOp(this, DEFAULT, var::$default)), DEFAULT.priority())
                        .postfix(memberOperator(ref), MEMBER.priority())
                        .prefix(op(NOT, new UnaryOp(this, NOT, Func::notFunc)), UNARY_PRIORITY)
                        .postfix(op(DEC, new UnaryOp(this, DEC, var::$dec)), INC_DEC_PRIORITY)
                        .postfix(op(INC, new UnaryOp(this, INC, var::$inc)), INC_DEC_PRIORITY)
                        .prefix(op(NEGATE, new UnaryOp(this, NEGATE, var::$negate)), NEGATE.priority())
                        .prefix(op(PARALLEL, new UnaryOp(true, Func::parallelFunc, PARALLEL, this)), PARALLEL.priority())
                        .prefix(op(SERIAL, new UnaryOp(true, v -> v._fixDeep(false), SERIAL, this)), SERIAL.priority())
                        .prefix(forOperator(ref, pure), FOR_OP.priority())
                        .prefix(whileOperator(ref, pure), WHILE_OP.priority())
                        .postfix(subscriptOperator(ref), SUBSCRIPT_OP.priority())
                        .postfix(parameterOperator(ref, pure), PARAM_OP.priority())
                        .prefix(op(FIX, new UnaryOp(true, VarInternal::_fixDeep, FIX, this)), FIX.priority())
                        .postfix(castOperator(), CAST.priority())
                        .prefix(variableUsageOperator(pure), 1000)
                        .prefix(assignmentOperator(ref, pure), ASSIGNMENT.priority())
                        .prefix(definitionOperator(ref, pure), DEFINITION.priority());

        if (!pure) {
            table = table.prefix(op(PRINT, new UnaryOp(false, Func::outFunc, PRINT, this)), PRINT.priority())
                            .prefix(op(DEBUG, new UnaryOp(false, Func::debugFunc, DEBUG, this)), DEBUG.priority())
                            .prefix(op(ERR, new UnaryOp(false, Func::errFunc, ERR, this)), ERR.priority())
                            .prefix(writeOperator(ref), WRITE_OP.priority())
                            .prefix(readOperator(), READ_OP.priority())
                            .prefix(op(STOP, new UnaryOp(this, STOP, var::$stop)), STOP.priority())
                            .prefix(op(START, new UnaryOp(this, START, var::$start)), START.priority())
                            .prefix(op(PAUSE, new UnaryOp(this, PAUSE, var::$pause)), PAUSE.priority())
                            .prefix(op(UNPAUSE, new UnaryOp(this, UNPAUSE, var::$unpause)), UNPAUSE.priority())
                            .prefix(op(DESTROY, new UnaryOp(this, DESTROY, var::$destroy)), DESTROY.priority())
                            .prefix(op(CREATE, new UnaryOp(this, CREATE, var::$create)), CREATE.priority())
                            .prefix(op(STATE, new UnaryOp(this, STATE, var::$state)), STATE.priority())
                            .prefix(op(FORK, new UnaryOp(this, FORK, Func::fork)), FORK.priority())
                            .infixl(op(PUBLISH, new BinaryOp(this, PUBLISH, Func::publishFunc)), PUBLISH.priority())
                            .infixl(op(SUBSCRIBE, new BinaryOp(this, SUBSCRIBE, (lhs, rhs) -> Func.subscribeFunc(pure, lhs, rhs))),
                                    SUBSCRIBE.priority())
                            .infixl(op(WRITE_SIMPLE, new BinaryOp(this, WRITE_SIMPLE, Func::writeFunc)), WRITE_SIMPLE.priority())
                            .prefix(op(READ_SIMPLE, new UnaryOp(this, READ_SIMPLE, Func::readFunc)), READ_SIMPLE.priority())
                            .prefix(op(DRAIN, new UnaryOp(this, DRAIN, URIAware::$drain)), DRAIN.priority())
                            .prefix(op(ALL, new UnaryOp(this, ALL, URIAware::$all)), ALL.priority());

        }
        Parser<var> parser = table.build(main);
        ref.set(parser);
        return parser;

    }

    @NotNull
    private Parser<var> builtin(@NotNull boolean pure) {
        return BUILTIN.token().map((Map<Token, var>) token -> {
            final var v = (var) token.value();
            return createReactiveNode(v.toHumanString(), NO_SCOPE, DollarParserImpl.this, token, v,
                                      args -> Builtins.execute(v.toHumanString(), emptyList(), pure)
            );
        });
    }

    @NotNull
    private Parser<var> variableRef(@NotNull boolean pure) {
        return identifier().followedBy(OP(LEFT_PAREN).not().peek()).token().map(
                new Map<Token, var>() {
                    @Nullable
                    public var map(@NotNull Token token) {
                        return getVariable(pure, token.value().toString(),
                                           false, null,
                                           token, DollarParserImpl.this);
                    }
                });
    }

    private Parser<var> unitValue(boolean pure) {
        return array(DECIMAL_LITERAL.or(INTEGER_LITERAL), BUILTIN).token().map(
                new UnitOperator(this, pure));
    }

    private Parser<var> list(@NotNull Parser<var> expression, boolean pure) {
        return OP_NL(LEFT_BRACKET)
                       .next(expression.sepBy(COMMA_OR_NEWLINE_TERMINATOR))
                       .followedBy(COMMA_OR_NEWLINE_TERMINATOR.optional())
                       .followedBy(NL_OP(RIGHT_BRACKET)).token().map(
                        new ListOperator(this, pure));
    }

    private Parser<var> map(@NotNull Parser<var> expression, boolean pure) {
        return OP_NL(LEFT_BRACE).next(expression.sepBy(COMMA_TERMINATOR))
                       .followedBy(COMMA_TERMINATOR.optional())
                       .followedBy(NL_OP(RIGHT_BRACE)).token()
                       .map(new MapOperator(this, pure));
    }

    private Parser<var> moduleStatement(@NotNull Parser.Reference<var> ref) {
        final Parser<Object[]> param = array(IDENTIFIER.followedBy(OP(ASSIGNMENT)), ref.lazy());

        final Parser<List<var>> parameters =
                KEYWORD(WITH).optional().next((param).map(objects -> {
                    var result = (var) objects[1];
                    result.setMetaAttribute(NAMED_PARAMETER_META_ATTR, objects[0].toString());
                    return result;
                }).sepBy(OP(COMMA)).between(OP(LEFT_PAREN), OP(RIGHT_PAREN)));

        return array(KEYWORD(MODULE), STRING_LITERAL.or(URL),
                     parameters.optional()).token().map(new ModuleOperator(this));

    }

    private Parser<var> assertOperator(@NotNull Parser.Reference<var> ref, boolean pure) {
        return OP(ASSERT).next(or(array(STRING_LITERAL.followedBy(OP(PAIR)), ref.lazy()),
                                  array(OP(PAIR).optional(), ref.lazy()))
                                       .token()
                                       .map(new AssertOperator(this)));
    }

    private Parser<var> collectStatement(Parser<var> expression, boolean pure) {
        return KEYWORD_NL(COLLECT)
                       .next(
                               array(expression,
                                     KEYWORD(UNTIL).next(expression).optional(),
                                     KEYWORD(UNLESS).next(expression).optional(),
                                     expression)
                       )
                       .token()
                       .map(new CollectOperator(this, pure));
    }

    private Parser<var> whenStatement(Parser<var> expression, boolean pure) {
        return KEYWORD_NL(WHEN)
                       .next(array(expression, expression))
                       .token()
                       .map(new WhenOperator(this));
    }

    private Parser<var> everyStatement(Parser<var> expression, boolean pure) {
        return KEYWORD_NL(EVERY)
                       .next(array(unitValue(pure),
                                   KEYWORD(UNTIL).next(
                                           expression).optional(null),
                                   KEYWORD(UNLESS).next(
                                           expression).optional(null),
                                   expression))
                       .token()
                       .map(new EveryOperator(this, pure));
    }

    private Parser<var> functionCall() {
        return array(IDENTIFIER.or(BUILTIN).followedBy(OP(LEFT_PAREN).peek()))
                       .token()
                       .map(new FunctionCallOperator(this));
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
            @NotNull
            @Override
            public var apply(@NotNull Token token) {
                return DollarScriptSupport.createNode("java", NO_SCOPE, DollarParserImpl.this, token,
                                                      Arrays.asList($void()),
                                                      in -> JavaScriptingSupport.compile($void(),
                                                                                         (String) token
                                                                                                          .value()));
            }
        });
    }


    private <T> Parser<T> op(@NotNull OpDef def, @NotNull T value) {
        return OP(def).token().map(new SourceMapper<>(value));

    }

//    private <T> Parser<T> op(T value, String name, @Nullable String keyword) {
//        Parser<?> parser;
//        if (keyword == null) {
//            parser = OP(name);
//        } else {
//            parser = OP(name, keyword);
//
//        }
//        return parser.token().map(new SourceMapper<>(value));
//
//    }

    private Parser<Map<? super var, ? extends var>> pipeOperator(@NotNull Parser.Reference<var> ref,
                                                                 boolean pure) {
        return (OP(PIPE_OPERATOR).optional(null)).next(
                Parsers.longest(BUILTIN, IDENTIFIER, functionCall().postfix(parameterOperator(ref, pure)),
                                ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN))))
                       .token().map(new PipeOperator(this, pure));
    }

    private Parser<Map<? super var, ? extends var>> writeOperator(@NotNull Parser.Reference<var> ref) {
        return array(KEYWORD(WRITE_KEYWORD), ref.lazy(), KEYWORD(BLOCK).optional(),
                     KEYWORD(MUTATE).optional())
                       .followedBy(KEYWORD(TO).optional())
                       .token()
                       .map(new WriteOperator(this));
    }

    private Parser<Map<? super var, ? extends var>> readOperator() {
        return array(KEYWORD(READ_KEYWORD), KEYWORD(BLOCK).optional(), KEYWORD(MUTATE).optional()).followedBy(
                KEYWORD(FROM).optional())
                       .token()
                       .map(new ReadOperator(this));
    }

    private Parser<Map<var, var>> ifOperator(@NotNull Parser.Reference<var> ref) {
        return KEYWORD_NL(IF_OPERATOR)
                       .next(ref.lazy())
                       .token()
                       .map(new IfOperator(this));
    }

    private Parser<Map<? super var, ? extends var>> isOperator() {
        return KEYWORD(IS)
                       .next(IDENTIFIER.sepBy(OP(COMMA)))
                       .token()
                       .map(new IsOperator(this));
    }

    private Parser<Map<? super var, ? extends var>> memberOperator(@NotNull Parser.Reference<var> ref) {
        return OP(MEMBER).followedBy(OP(MEMBER).not())
                       .next(ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN)).or(IDENTIFIER))
                       .token().map(new Function<Token, Map<? super var, ? extends var>>() {

                    @NotNull
                    @Override
                    public Map<? super var, ? extends var> apply(@NotNull Token rhs) {

                        return new Map<var, var>() {

                            @NotNull
                            @Override
                            public var map(@NotNull var lhs) {
                                return createReactiveNode(
                                        "." + rhs.toString(), NO_SCOPE, DollarParserImpl.this, rhs, lhs,
                                        (var) rhs.value(), args -> lhs.$(rhs.value().toString())
                                );
                            }
                        };
                    }
                });
    }

    private Parser<Map<? super var, ? extends var>> forOperator(final @NotNull Parser.Reference<var> ref, boolean pure) {
        return array(KEYWORD(FOR), IDENTIFIER, KEYWORD(IN), ref.lazy())
                       .token()
                       .map(new ForOperator(this, pure));
    }

    private Parser<Map<? super var, ? extends var>> whileOperator(final @NotNull Parser.Reference<var> ref, boolean pure) {
        return KEYWORD(WHILE)
                       .next(ref.lazy())
                       .token()
                       .map(new WhileOperator(this, pure));
    }

    private Parser<Map<? super var, ? extends var>> subscriptOperator(@NotNull Parser.Reference<var> ref) {
        return OP(LEFT_BRACKET)
                       .next(array(
                               ref.lazy().followedBy(OP(RIGHT_BRACKET)), OP(ASSIGNMENT).next(ref.lazy()).optional()
                       ))
                       .token()
                       .map(new SubscriptOperator(this));
    }

    private Parser<Map<? super var, ? extends var>> parameterOperator(@NotNull Parser.Reference<var> ref,
                                                                      boolean pure) {
        return OP(LEFT_PAREN).next(
                or(array(IDENTIFIER.followedBy(OP(ASSIGNMENT)), ref.lazy()),
                   array(OP(ASSIGNMENT).optional(), ref.lazy())).map(
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
                        }).sepBy(COMMA_TERMINATOR)).followedBy(OP(RIGHT_PAREN)).token().map(
                new ParameterOperator(this, pure));
    }

    private Parser<Map<? super var, ? extends var>> variableUsageOperator(boolean pure) {
        return or(OP(DOLLAR).followedBy(OP(LEFT_PAREN).peek())
                          .token()
                          .map(new VariableUsageOperator(pure, this, false)),
                  OP(DOLLAR).followedBy(INTEGER_LITERAL.peek())
                          .token()
                          .map(
                                  (Token lhs) -> {
                                      return new Map<var, var>() {
                                          @Nullable
                                          @Override
                                          public var map(@NotNull var rhs) {
                                              return getVariable(pure, rhs.toString(), true, null, lhs,
                                                                 DollarParserImpl.this);
                                          }
                                      };
                                  }));
    }

    private Parser<Map<? super var, ? extends var>> castOperator() {
        return KEYWORD(AS).next(IDENTIFIER).token().map(new CastOperator(this));
    }

    private Parser<Map<? super var, ? extends var>> assignmentOperator(
                                                                              @NotNull Parser.Reference<var> ref,
                                                                              boolean pure) {
        return array(KEYWORD(EXPORT).optional(),
                     or(KEYWORD(CONST), KEYWORD(VOLATILE), KEYWORD(VAR)).optional(),
                     IDENTIFIER.between(OP(LT), OP(GT)).optional(),
                     ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN)).optional(),
                     OP(DOLLAR).next(ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN))).or(IDENTIFIER).or(BUILTIN),
                     or(OP(ASSIGNMENT), OP(LISTEN_ASSIGN), OP(SUBSCRIBE_ASSIGN))
        ).token().map(new AssignmentOperator(false, pure, this));
    }

    private Parser<Map<? super var, ? extends var>> definitionOperator(@NotNull Parser.Reference<var> ref, boolean pure) {

        return
                or(
                        array(
                                KEYWORD(EXPORT).optional(null),
                                or(KEYWORD(CONST)).optional(null),
                                IDENTIFIER.between(OP(LT), OP(GT)).optional(),
                                OP(DOLLAR).next(ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN))).or(IDENTIFIER),
                                OP(DEFINITION)
                        ),
                        array(KEYWORD(EXPORT).optional(), IDENTIFIER.between(OP(LT), OP(GT)).optional(), KEYWORD(DEF), IDENTIFIER)
                ).token().map(new DefinitionOperator(pure, this));
    }

    private Parser<var> pureDeclarationOperator(
                                                       @NotNull Parser.Reference<var> ref,
                                                       boolean pure) throws Exception {

        return KEYWORD(PURE).next(
                or(
                        array(
                                KEYWORD(EXPORT).optional(null),
                                or(KEYWORD(CONST)).optional(null),
                                IDENTIFIER.between(OP(LT), OP(GT)).optional(),
                                OP(DOLLAR).next(ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN))).or(IDENTIFIER),
                                OP(DEFINITION), expression(null, true)
                        ),
                        array(KEYWORD(EXPORT).optional(),
                              IDENTIFIER.between(OP(LT), OP(GT)).optional(), KEYWORD(DEF),
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
        @NotNull
        private final T value;


        SourceMapper(@NotNull T value) {
            this.value = value;
        }

        @NotNull
        @Override
        public T map(@NotNull Token token) {
            if (value instanceof Operator) {
                ((Operator) value).setSource(new SourceSegmentValue(currentScope(), token));
            }
            return value;
        }
    }
}
