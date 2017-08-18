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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.sillelien.dollar.api.DollarStatic.$;
import static com.sillelien.dollar.api.DollarStatic.$void;
import static dollar.internal.runtime.script.DollarLexer.*;
import static dollar.internal.runtime.script.DollarScriptSupport.*;
import static dollar.internal.runtime.script.OperatorPriority.EQ_PRIORITY;
import static dollar.internal.runtime.script.SourceNodeOptions.NO_SCOPE;
import static dollar.internal.runtime.script.java.JavaScriptingSupport.compile;
import static dollar.internal.runtime.script.parser.Symbols.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.jparsec.Parsers.*;

@SuppressWarnings("FeatureEnvy")
public class DollarParserImpl implements DollarParser {
    @NotNull
    public static final String NAMED_PARAMETER_META_ATTR = "__named_parameter";
    @Nullable
    public static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("DollarParser");

    @NotNull
    private final ClassLoader classLoader;
    @NotNull
    private final ParserErrorHandler errorHandler = new ParserErrorHandlerImpl();
    @NotNull
    private final Map<String, var> exports = new ConcurrentHashMap<>();
    @NotNull
    private final ParserOptions options;

    @Nullable
    private String file;

    public DollarParserImpl(@NotNull ParserOptions options) {
        this.options = options;
        classLoader = DollarParser.class.getClassLoader();
    }

    public DollarParserImpl(@NotNull ParserOptions options, @NotNull ClassLoader classLoader) {
        this.options = options;
        this.classLoader = classLoader;

    }

    private Parser<var> dollarIdentifier(@NotNull Parser.Reference ref) {
        //noinspection unchecked
        return OP(DOLLAR).next(
                array(Terminals.Identifier.PARSER, OP(DEFAULT).next(ref.lazy()).optional(null)).between(
                        OP(LEFT_BRACE),
                        OP(RIGHT_BRACE))).token().map(
                t -> {
                    Object[] objects = (Object[]) t.value();
                    return getVariable(false, objects[0].toString(), false, (var) objects[1], t,
                                       this);
                });
    }


    @Override
    public void export(@NotNull String name, @NotNull var export) {
//        System.err.println("Exporting " + name);
        export.setMetaObject("scopes", new ArrayList<>(DollarScriptSupport.scopes()));
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

    @Override
    @NotNull
    public var parse(@NotNull ScriptScope scope, @NotNull String source) throws Exception {
        var v = DollarScriptSupport.inScope(false, scope, newScope -> {
            DollarStatic.context().setClassLoader(classLoader);
            Parser<?> parser = script();
            try {
                parser.from(TOKENIZER, DollarLexer.IGNORED).parse(source);
            } catch (Exception e) {
                getErrorHandler().handleTopLevel(e, null, (file != null) ? new File(file) : null);

            }
//            parse._fixDeep(false);
//            newScope.destroy();
            return $(exports);
        });
        if (v != null) {
            return v;
        } else {
            throw new AssertionError("parse should not return null");
        }

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


    @NotNull
    private Parser<var> script() throws Exception {
        log.debug("Starting Parse Phase");


        Parser.Reference<var> ref = Parser.newReference();
//        Parser<var> block = block(ref.lazy(), false).between(OP_NL(LEFT_BRACE), NL_OP(RIGHT_BRACE));
        Parser<var> expression = expression(false);
        Parser<var> parser = (TERMINATOR_SYMBOL.optional(null)).next(expression.followedBy(
                TERMINATOR_SYMBOL).many1()).map(l -> {
            log.debug("Ended Parse Phase");
            log.debug("Starting Runtime Phase");
            for (int i = 0; i < (l.size() - 1); i++) {
                l.get(i)._fixDeep(false);
//              System.err.println(fixed);
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
                                 .followedBy(SEMICOLON_TERMINATOR.optional(null))
                                 .map(var -> var).token().map(new BlockOperator(this, pure));
        ref.set(or);
        return or;
    }

    @NotNull
    private Parser<var> expression(final boolean pure) throws Exception {
        Parser<var> main;
        Parser.Reference<var> ref = Parser.newReference();
        if (pure) {
            //noinspection unchecked
            main = ref.lazy()
                           .between(OP(LEFT_PAREN), OP(RIGHT_PAREN))
                           .or(Parsers.or(unitValue(true),
                                          list(ref.lazy(), true),
                                          serialFunc(ref.lazy(), true),
                                          assertOperator(ref, true),
                                          collectStatement(ref.lazy(), true),
                                          whenStatement(ref.lazy(), true),
                                          functionCall(),
                                          DECIMAL_LITERAL,
                                          INTEGER_LITERAL,
                                          STRING_LITERAL,
                                          IDENTIFIER_KEYWORD,
                                          variableRef(true),
                                          builtin(true)))
                           .or(block(ref.lazy(), true).between(OP_NL(LEFT_BRACE), NL_OP(RIGHT_BRACE)));
        } else {
            //noinspection unchecked
            main = ref.lazy()
                           .between(OP(LEFT_PAREN), OP(RIGHT_PAREN))
                           .or(Parsers.or(unitValue(false),
                                          list(ref.lazy(), false),
                                          serialFunc(ref.lazy(), false),
                                          pureDeclarationOperator(ref),
                                          KEYWORD(PURE).next(expression(true)),
                                          moduleStatement(ref),
                                          assertOperator(ref, false),
                                          collectStatement(ref.lazy(), false),
                                          whenStatement(ref.lazy(), false),
                                          everyStatement(ref.lazy()),
                                          functionCall(),
                                          java(),
                                          URL,
                                          DECIMAL_LITERAL,
                                          INTEGER_LITERAL,
                                          STRING_LITERAL,
                                          dollarIdentifier(ref),
                                          IDENTIFIER_KEYWORD,
                                          builtin(false),
                                          variableRef(false)))
                           .or(block(ref.lazy(), false).between(OP_NL(LEFT_BRACE), NL_OP(RIGHT_BRACE)));
        }

        OperatorTable<var> table = new OperatorTable<>();
        table = infixl(pure, table, INEQUALITY_OPERATOR, Func::inequality);
        table = infixl(pure, table, EQUALITY, Func::equality);
        table = infixl(pure, table, AND, Func::and);
        table = infixl(pure, table, OR, Func::orFunc);
        table = infixl(pure, table, RANGE, Func::range);
        table = infixl(pure, table, LT, Func::lt);
        table = infixl(pure, table, GT, Func::gt);
        table = infixl(pure, table, LT_EQUALS, Func::lte);
        table = infixl(pure, table, GT_EQUALS, Func::gte);
        table = infixl(pure, table, MULTIPLY, Func::multiply);
        table = infixl(pure, table, DIVIDE, NumericAware::$divide);
        table = infixl(pure, table, MOD, NumericAware::$modulus);
        table = infixl(pure, table, PLUS, var::$plus);
        table = infixl(pure, table, MINUS, var::$minus);
        table = infixl(pure, table, PAIR, Func::pair);
        table = infixl(pure, table, ELSE, Func::elseFunc);
        table = infixl(pure, table, IN, Func::inFunc);
        table = infixl(pure, table, EACH, (lhs, rhs) -> Func.each(pure, lhs, rhs));
        table = infixl(pure, table, REDUCE, (lhs, rhs) -> Func.reduce(pure, lhs, rhs));
        table = infixl(pure, table, WHEN_OP, Func::listenFunc);
        table = infixl(pure, table, CHOOSE, ControlFlowAware::$choose);
        table = infixl(pure, table, DEFAULT, var::$default);

        table = infixlReactive(pure, table, ASSERT_EQ_REACT, Func::assertEquals);
        table = infixlUnReactive(pure, table, ASSERT_EQ_UNREACT, Func::assertEquals);


        table = postfix(pure, table, DEC, var::$dec);
        table = postfix(pure, table, INC, var::$inc);

        table = prefix(pure, table, NEGATE, var::$negate);
        table = prefix(pure, table, NOT, Func::notFunc);
        table = prefix(pure, table, ERROR, Func::error);
        table = prefix(pure, table, TRUTHY, Func::truthy);
        table = prefix(pure, table, SIZE, var::$size);


        table = prefixUnReactive(pure, table, FIX, VarInternal::_fixDeep);
        table = prefixUnReactive(pure, table, PARALLEL, Func::parallelFunc);
        table = prefixUnReactive(pure, table, SERIAL, Func::serialFunc);


        //More complex expression syntax
        table = table.infixl(op(CAUSES, new CausesOperator(pure, this)), CAUSES.priority());

        table = table.postfix(pipeOperator(ref, pure), PIPE_OPERATOR.priority());
        table = table.postfix(isOperator(pure), EQ_PRIORITY);
        table = table.postfix(memberOperator(ref), MEMBER.priority());
        table = table.postfix(subscriptOperator(ref, pure), SUBSCRIPT_OP.priority());
        table = table.postfix(parameterOperator(ref, pure), PARAM_OP.priority());
        table = table.postfix(castOperator(pure), CAST.priority());

        table = table.prefix(ifOperator(ref, pure), IF_OPERATOR.priority());
        table = table.prefix(forOperator(ref, pure), FOR_OP.priority());
        table = table.prefix(whileOperator(ref, pure), WHILE_OP.priority());
        table = table.prefix(variableUsageOperator(pure), 1000);
        table = table.prefix(assignmentOperator(ref, pure), ASSIGNMENT.priority());
        table = table.prefix(definitionOperator(ref, pure), DEFINITION.priority());

        if (!pure) {
            table = infixl(false, table, PUBLISH, Func::publishFunc);
            table = infixl(false, table, SUBSCRIBE, Func::subscribeFunc);
            table = infixl(false, table, WRITE_SIMPLE, Func::writeFunc);
            table = prefix(false, table, READ_SIMPLE, Func::readFunc);

            table = prefix(false, table, DRAIN, URIAware::$drain);
            table = prefix(false, table, ALL, URIAware::$all);
            table = prefix(false, table, PRINT, Func::outFunc);
            table = prefix(false, table, DEBUG, Func::debugFunc);
            table = prefix(false, table, ERR, Func::errFunc);
            table = prefix(false, table, STOP, var::$stop);
            table = prefix(false, table, START, var::$start);
            table = prefix(false, table, PAUSE, var::$pause);
            table = prefix(false, table, UNPAUSE, var::$unpause);
            table = prefix(false, table, DESTROY, var::$destroy);
            table = prefix(false, table, CREATE, var::$create);
            table = prefix(false, table, STATE, var::$state);
            table = prefix(false, table, FORK, Func::fork);

            table = table.prefix(writeOperator(ref), WRITE_OP.priority());
            table = table.prefix(readOperator(), READ_OP.priority());
        }
        Parser<var> parser = table.build(main);
        ref.set(parser);
        return parser;

    }

    @NotNull
    private OperatorTable<var> postfix(boolean pure,
                                       @NotNull OperatorTable<var> table,
                                       @NotNull OpDef o,
                                       @NotNull Function<var, var> f2) {
        return table.postfix(op(o, new UnaryOp(this, o, f2, pure)), o.priority());
    }

    @NotNull
    private OperatorTable<var> infixlReactive(boolean pure,
                                              @NotNull OperatorTable<var> table,
                                              @NotNull OpDef o,
                                              @NotNull BiFunction<var, var, var> f) {
        return table.infixl(op(o, new BinaryOp(false, o, this, f, pure)),
                            o.priority());
    }

    @NotNull
    private OperatorTable<var> infixlUnReactive(boolean pure,
                                                @NotNull OperatorTable<var> table,
                                                @NotNull OpDef o,
                                                @NotNull BiFunction<var, var, var> f) {
        return table.infixl(op(o, new BinaryOp(true, o, this, f, pure)),
                            o.priority());
    }

    @NotNull
    private OperatorTable<var> prefixUnReactive(boolean pure,
                                                @NotNull OperatorTable<var> table,
                                                @NotNull OpDef o,
                                                @NotNull Function<var, var> f) {
        return table.prefix(op(o, new UnaryOp(true, f, o, this, pure)),
                            o.priority());
    }

    @NotNull
    private OperatorTable<var> prefix(boolean pure,
                                      @NotNull OperatorTable<var> table,
                                      @NotNull OpDef o,
                                      @NotNull Function<var, var> f) {
        return table.prefix(op(o, new UnaryOp(this, o, f, pure)), o.priority());
    }

    @NotNull
    private OperatorTable<var> infixl(boolean pure,
                                      @NotNull OperatorTable<var> table,
                                      @NotNull OpDef opdef,
                                      @NotNull BiFunction<var, var, var> func) {
        return table.infixl(op(opdef, new BinaryOp(this, opdef, func, pure)), opdef.priority());
    }

    @NotNull
    private Parser<var> builtin(boolean pure) {
        return BUILTIN
                       .token()
                       .map(token -> reactiveNode("builtin", NO_SCOPE,
                                                  this, token,
                                                  (var) token.value(),
                                                  args -> Builtins.execute(token.toString(), emptyList(), pure)));
    }

    @NotNull
    private Parser<var> variableRef(boolean pure) {
        return identifier().followedBy(OP(LEFT_PAREN).not().peek())
                       .token()
                       .map(token -> getVariable(pure, token.value().toString(),
                                                 false, null,
                                                 token, this));
    }

    private Parser<var> unitValue(boolean pure) {
        return array(DECIMAL_LITERAL.or(INTEGER_LITERAL), BUILTIN).token().map(
                new UnitOperator(this, pure));
    }

    private Parser<var> list(@NotNull Parser<var> expression, boolean pure) {
        return OP_NL(LEFT_BRACKET)
                       .next(expression.sepBy(COMMA_OR_NEWLINE_TERMINATOR))
                       .followedBy(COMMA_OR_NEWLINE_TERMINATOR.optional(null))
                       .followedBy(NL_OP(RIGHT_BRACKET)).token().map(
                        new ListOperator(this, pure));
    }

    private Parser<var> serialFunc(@NotNull Parser<var> expression, boolean pure) {
        return OP_NL(LEFT_BRACE).next(expression.sepBy(COMMA_TERMINATOR))
                       .followedBy(COMMA_TERMINATOR.optional(null))
                       .followedBy(NL_OP(RIGHT_BRACE)).token()
                       .map(new MapOperator(this, pure));
    }

    private Parser<var> moduleStatement(@NotNull Parser.Reference<var> ref) {
        final Parser<Object[]> param = array(IDENTIFIER.followedBy(OP(ASSIGNMENT)), ref.lazy());

        final Parser<List<var>> parameters =
                KEYWORD(WITH).optional(null).next((param).map(objects -> {
                    var result = (var) objects[1];
                    result.setMetaAttribute(NAMED_PARAMETER_META_ATTR, objects[0].toString());
                    return result;
                }).sepBy(OP(COMMA)).between(OP(LEFT_PAREN), OP(RIGHT_PAREN)));

        return array(KEYWORD(MODULE), STRING_LITERAL.or(URL),
                     parameters.optional(null)).token().map(new ModuleOperator(this));

    }

    private Parser<var> assertOperator(@NotNull Parser.Reference<var> ref, boolean pure) {
        return OP(ASSERT).next(or(array(STRING_LITERAL.followedBy(OP(PAIR)), ref.lazy()),
                                  array(OP(PAIR).optional(null), ref.lazy()))
                                       .token()
                                       .map(new AssertOperator(this, pure)));
    }

    private Parser<var> collectStatement(@NotNull Parser<var> expression, boolean pure) {
        return KEYWORD_NL(COLLECT)
                       .next(
                               array(expression,
                                     KEYWORD(UNTIL).next(expression).optional(null),
                                     KEYWORD(UNLESS).next(expression).optional(null),
                                     expression)
                       )
                       .token()
                       .map(new CollectOperator(this, pure));
    }

    private Parser<var> whenStatement(@NotNull Parser<var> expression, boolean pure) {
        return KEYWORD_NL(WHEN)
                       .next(array(expression, expression))
                       .token()
                       .map(new WhenOperator(this, pure));
    }

    private Parser<var> everyStatement(@NotNull Parser<var> expression) {
        return KEYWORD_NL(EVERY)
                       .next(array(unitValue(false),
                                   KEYWORD(UNTIL).next(
                                           expression).optional(null),
                                   KEYWORD(UNLESS).next(
                                           expression).optional(null),
                                   expression))
                       .token()
                       .map(new EveryOperator(this, false));
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
                    if (!"java".equals(c.tag())) {
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
        }).token().map(token -> createNode("java", NO_SCOPE,
                                           this,
                                           token,
                                           singletonList($void()),
                                           in -> compile($void(), (String) token.value())));
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
//        return parser.token().inFunc(new SourceMapper<>(value));
//
//    }

    private Parser<Function<? super var, ? extends var>> pipeOperator(@NotNull Parser.Reference<var> ref,
                                                                      boolean pure) {
        //noinspection unchecked
        return (OP(PIPE_OPERATOR).optional(null)).next(
                longest(BUILTIN, IDENTIFIER, functionCall().postfix(parameterOperator(ref, pure)),
                        ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN))))
                       .token().map(new PipeOperator(this, pure));
    }

    private Parser<Function<? super var, ? extends var>> writeOperator(@NotNull Parser.Reference<var> ref) {
        return array(KEYWORD(WRITE_KEYWORD), ref.lazy(), KEYWORD(BLOCK).optional(null),
                     KEYWORD(MUTATE).optional(null))
                       .followedBy(KEYWORD(TO).optional(null))
                       .token()
                       .map(new WriteOperator(this));
    }

    private Parser<Function<? super var, ? extends var>> readOperator() {
        return array(KEYWORD(READ_KEYWORD), KEYWORD(BLOCK).optional(null), KEYWORD(MUTATE).optional(null)).followedBy(
                KEYWORD(FROM).optional(null))
                       .token()
                       .map(new ReadOperator(this));
    }

    private Parser<Function<var, var>> ifOperator(@NotNull Parser.Reference<var> ref, boolean pure) {
        return KEYWORD_NL(IF_OPERATOR)
                       .next(ref.lazy())
                       .token()
                       .map(new IfOperator(this, pure));
    }

    private Parser<Function<? super var, ? extends var>> isOperator(boolean pure) {
        return KEYWORD(IS)
                       .next(IDENTIFIER.sepBy(OP(COMMA)))
                       .token()
                       .map(new IsOperator(this, pure));
    }

    private Parser<Function<? super var, ? extends var>> memberOperator(@NotNull Parser.Reference<var> ref) {
        return OP(MEMBER).followedBy(OP(MEMBER).not())
                       .next(ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN)).or(IDENTIFIER))
                       .token().map(rhs ->
                                            (Function<var, var>) lhs ->
                                                                         reactiveNode("member", NO_SCOPE,
                                                                                      this, rhs, lhs,
                                                                                      (var) rhs.value(),
                                                                                      args -> lhs.$(rhs.value().toString())
                                                                         ));
    }

    private Parser<Function<? super var, ? extends var>> forOperator(final @NotNull Parser.Reference<var> ref, boolean pure) {
        return array(KEYWORD(FOR), IDENTIFIER, KEYWORD(IN), ref.lazy())
                       .token()
                       .map(new ForOperator(this, pure));
    }

    private Parser<Function<? super var, ? extends var>> whileOperator(final @NotNull Parser.Reference<var> ref, boolean pure) {
        return KEYWORD(WHILE)
                       .next(ref.lazy())
                       .token()
                       .map(new WhileOperator(this, pure));
    }

    private Parser<Function<? super var, ? extends var>> subscriptOperator(@NotNull Parser.Reference<var> ref, boolean pure) {
        return OP(LEFT_BRACKET)
                       .next(array(ref.lazy().followedBy(OP(RIGHT_BRACKET)), OP(ASSIGNMENT).next(ref.lazy()).optional(null)
                       ))
                       .token()
                       .map(new SubscriptOperator(this, pure));
    }

    private Parser<Function<? super var, ? extends var>> parameterOperator(@NotNull Parser.Reference<var> ref,
                                                                           boolean pure) {
        return OP(LEFT_PAREN).next(
                or(array(IDENTIFIER.followedBy(OP(ASSIGNMENT)), ref.lazy()),
                   array(OP(ASSIGNMENT).optional(null), ref.lazy())).map(
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

    private Parser<Function<? super var, ? extends var>> variableUsageOperator(boolean pure) {
        return or(OP(DOLLAR).followedBy(OP(LEFT_PAREN).peek())
                          .token()
                          .map(new VariableUsageOperator(pure, this, false)),
                  OP(DOLLAR).followedBy(INTEGER_LITERAL.peek())
                          .token()
                          .map(lhs -> rhs -> getVariable(pure, rhs.toString(), true, null, lhs, this)));
    }

    private Parser<Function<? super var, ? extends var>> castOperator(boolean pure) {
        return KEYWORD(AS).next(IDENTIFIER).token().map(new CastOperator(this, pure));
    }

    private Parser<Function<? super var, ? extends var>> assignmentOperator(
                                                                                   @NotNull Parser.Reference<var> ref,
                                                                                   boolean pure) {
        return array(KEYWORD(EXPORT).optional(null),
                     or(KEYWORD(CONST), KEYWORD(VOLATILE), KEYWORD(VAR)).optional(null),
                     IDENTIFIER.between(OP(LT), OP(GT)).optional(null),
                     ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN)).optional(null),
                     OP(DOLLAR).next(ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN))).or(IDENTIFIER).or(BUILTIN),
                     or(OP(ASSIGNMENT), OP(LISTEN_ASSIGN), OP(SUBSCRIBE_ASSIGN))
        ).token().map(new AssignmentOperator(false, pure, this));
    }

    private Parser<Function<? super var, ? extends var>> definitionOperator(@NotNull Parser.Reference<var> ref, boolean pure) {

        return
                or(
                        array(
                                KEYWORD(EXPORT).optional(null),
                                KEYWORD(CONST).optional(null),
                                IDENTIFIER.between(OP(LT), OP(GT)).optional(null),
                                OP(DOLLAR).next(ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN))).or(IDENTIFIER),
                                OP(DEFINITION)
                        ),
                        array(KEYWORD(EXPORT).optional(null), IDENTIFIER.between(OP(LT), OP(GT)).optional(null), KEYWORD(DEF),
                              IDENTIFIER)
                ).token().map(new DefinitionOperator(pure, this));
    }

    private Parser<var> pureDeclarationOperator(@NotNull Parser.Reference<var> ref) throws Exception {

        return KEYWORD(PURE).next(
                or(
                        array(
                                KEYWORD(EXPORT).optional(null),
                                KEYWORD(CONST).optional(null),
                                IDENTIFIER.between(OP(LT), OP(GT)).optional(null),
                                OP(DOLLAR).next(ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN))
                                ).or(IDENTIFIER),
                                OP(DEFINITION), expression(true)
                        ),
                        array(KEYWORD(EXPORT).optional(null),
                              IDENTIFIER.between(OP(LT), OP(GT)).optional(null), KEYWORD(DEF),
                              IDENTIFIER, expression(true))
                )
        ).token().map(new DefinitionOperator(true, this)).map(map -> map.apply(null));
    }

    private class SourceMapper<T> implements Function<Token, T> {
        @NotNull
        private final T value;


        SourceMapper(@NotNull T value) {
            this.value = value;
        }

        @NotNull
        @Override
        public T apply(@NotNull Token token) {
            if (value instanceof Operator) {
                ((Operator) value).setSource(new SourceSegmentValue(currentScope(), token));
            }
            return value;
        }
    }
}
