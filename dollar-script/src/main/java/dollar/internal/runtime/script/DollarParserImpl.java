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
import com.sillelien.dollar.api.BooleanAware;
import com.sillelien.dollar.api.ControlFlowAware;
import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.NumericAware;
import com.sillelien.dollar.api.URIAware;
import com.sillelien.dollar.api.VarInternal;
import com.sillelien.dollar.api.collections.ImmutableList;
import com.sillelien.dollar.api.script.ModuleResolver;
import com.sillelien.dollar.api.time.Scheduler;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.ParserErrorHandler;
import dollar.internal.runtime.script.api.ParserOptions;
import dollar.internal.runtime.script.api.Scope;
import dollar.internal.runtime.script.operators.AssignmentOperator;
import dollar.internal.runtime.script.operators.BlockOperator;
import dollar.internal.runtime.script.operators.CausesOperator;
import dollar.internal.runtime.script.operators.CollectOperator;
import dollar.internal.runtime.script.operators.DefinitionOperator;
import dollar.internal.runtime.script.operators.Func;
import dollar.internal.runtime.script.operators.ParameterOperator;
import dollar.internal.runtime.script.operators.PipeOperator;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sillelien.dollar.api.DollarStatic.*;
import static dollar.internal.runtime.script.DollarLexer.*;
import static dollar.internal.runtime.script.DollarScriptSupport.*;
import static dollar.internal.runtime.script.OperatorPriority.EQ_PRIORITY;
import static dollar.internal.runtime.script.SourceNodeOptions.*;
import static dollar.internal.runtime.script.java.JavaScriptingSupport.compile;
import static dollar.internal.runtime.script.parser.Symbols.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.jparsec.Parsers.*;

@SuppressWarnings({"FeatureEnvy", "OverlyCoupledClass", "unchecked"})
public class DollarParserImpl implements DollarParser {
    @NotNull
    public static final String NAMED_PARAMETER_META_ATTR = "__named_parameter";
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("DollarParser");
    private static final double ONE_DAY = 24.0 * 60.0 * 60.0 * 1000.0;
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


    @Override
    public void export(@NotNull String name, @NotNull var export) {
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
            } catch (RuntimeException e) {
                getErrorHandler().handleTopLevel(e, null, (file != null) ? new File(file) : null);

            }
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
                TERMINATOR_SYMBOL).many1()).map(expressions -> {
            log.debug("Ended Parse Phase");
            log.debug("Starting Runtime Phase");
            for (int i = 0; i < (expressions.size() - 1); i++) {
                expressions.get(i)._fixDeep(false);
//              System.err.println(fixed);
            }
            var resultVar = expressions.get(expressions.size() - 1);
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
                                          mapFunc(ref.lazy(), true),
                                          assertOperator(ref, true),
                                          collectStatement(ref.lazy(), true),
                                          whenStatement(ref.lazy(), true),
                                          functionCall(true),
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
                                          mapFunc(ref.lazy(), false),
                                          pureDeclarationOperator(ref),
                                          KEYWORD(PURE).next(expression(true)),
                                          moduleStatement(ref),
                                          assertOperator(ref, false),
                                          collectStatement(ref.lazy(), false),
                                          whenStatement(ref.lazy(), false),
                                          everyStatement(ref.lazy()),
                                          functionCall(false),
                                          java(false),
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

        table = infixl(pure, table, PLUS, var::$plus);
        table = infixl(pure, table, MINUS, var::$minus);
        table = infixl(pure, table, CHOOSE, ControlFlowAware::$choose);
        table = infixl(pure, table, DEFAULT, var::$default);
        table = infixl(pure, table, DIVIDE, NumericAware::$divide);
        table = infixl(pure, table, MOD, NumericAware::$modulus);


        table = infixl(pure, table, INEQUALITY_OPERATOR, Func::inequalityFunc);
        table = infixl(pure, table, EQUALITY, Func::equalityFunc);
        table = infixl(pure, table, AND, Func::andFunc);
        table = infixl(pure, table, OR, Func::orFunc);
        table = infixl(pure, table, RANGE, Func::rangeFunc);
        table = infixl(pure, table, LT, Func::lt);
        table = infixl(pure, table, GT, Func::gt);
        table = infixl(pure, table, LT_EQUALS, Func::lte);
        table = infixl(pure, table, GT_EQUALS, Func::gte);
        table = infixl(pure, table, MULTIPLY, Func::multiplyFunc);
        table = infixl(pure, table, PAIR, Func::pairFunc);
        table = infixl(pure, table, ELSE, Func::elseFunc);
        table = infixl(pure, table, IN, Func::inFunc);
        table = infixl(pure, table, WHEN_OP, Func::listenFunc);

        table = infixl(pure, table, EACH, (lhs, rhs) -> Func.eachFunc(pure, lhs, rhs));
        table = infixl(pure, table, REDUCE, (lhs, rhs) -> Func.reduceFunc(pure, lhs, rhs));

        table = infixlReactive(pure, table, ASSERT_EQ_REACT, Func::assertEqualsFunc);
        table = infixlUnReactive(pure, table, ASSERT_EQ_UNREACT, Func::assertEqualsFunc);


        table = postfix(pure, table, DEC, var::$dec);
        table = postfix(pure, table, INC, var::$inc);

        table = prefix(pure, table, NEGATE, var::$negate);
        table = prefix(pure, table, SIZE, var::$size);

        table = prefix(pure, table, NOT, Func::notFunc);
        table = prefix(pure, table, ERROR, Func::errorFunc);
        table = prefix(pure, table, TRUTHY, Func::truthyFunc);


        table = prefixUnReactive(pure, table, FIX, VarInternal::_fixDeep);
        table = prefixUnReactive(pure, table, PARALLEL, Func::parallelFunc);
        table = prefixUnReactive(pure, table, SERIAL, Func::serialFunc);


        //More complex expression syntax
        table = table.infixl(op(CAUSES, new CausesOperator(pure, this)), CAUSES.priority());

        table = table.postfix(pipeOperator(ref, pure), PIPE_OPERATOR.priority());
        table = table.postfix(isOperator(pure), EQ_PRIORITY);
        table = table.postfix(memberOperator(ref, pure), MEMBER.priority());
        table = table.postfix(subscriptOperator(ref, pure), SUBSCRIPT_OP.priority());
        table = table.postfix(castOperator(pure), CAST.priority());
        table = table.postfix(parameterOperator(ref, pure), PARAM_OP.priority());

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
            table = prefix(false, table, STOP, var::$stop);
            table = prefix(false, table, START, var::$start);
            table = prefix(false, table, PAUSE, var::$pause);
            table = prefix(false, table, UNPAUSE, var::$unpause);
            table = prefix(false, table, DESTROY, var::$destroy);
            table = prefix(false, table, CREATE, var::$create);
            table = prefix(false, table, STATE, var::$state);

            table = prefix(false, table, PRINT, Func::outFunc);
            table = prefix(false, table, DEBUG, Func::debugFunc);
            table = prefix(false, table, ERR, Func::errFunc);
            table = prefix(false, table, FORK, Func::forkFunc);

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
                       .map(token -> reactiveNode("builtin", pure, NO_SCOPE,
                                                  this, token,
                                                  (var) token.value(),
                                                  args -> Builtins.execute(token.toString(), emptyList(), pure)));
    }

    private Parser<var> dollarIdentifier(@NotNull Parser.Reference<var> ref) {
        //noinspection unchecked
        return OP(DOLLAR).next(
                array(Terminals.Identifier.PARSER, OP(DEFAULT).next(ref.lazy()).optional(null)).between(
                        OP(LEFT_BRACE),
                        OP(RIGHT_BRACE)))
                       .token().map(
                        t -> {
                            Object[] objects = (Object[]) t.value();
                            return variableNode(false, objects[0].toString(),
                                                false, (var) objects[1], t, this);
                        });
    }


    @NotNull
    private Parser<var> variableRef(boolean pure) {
        return identifier().followedBy(OP(LEFT_PAREN).not().peek())
                       .token()
                       .map(token -> variableNode(pure, token.value().toString(),
                                                  false, null,
                                                  token, this));
    }

    private Parser<var> unitValue(boolean pure) {
        return array(DECIMAL_LITERAL.or(INTEGER_LITERAL), BUILTIN)
                       .token()
                       .map(
                               token -> {
                                   Object[] objects = (Object[]) token.value();
                                   var quantity = (var) objects[0];
                                   var unit = (var) objects[1];
                                   return node("unit", pure,
                                               NEW_SCOPE, this, token,
                                               asList(quantity, unit), i -> {
                                               if (Builtins.exists(unit.toString())) {
                                                   return Builtins.execute(objects[1].toString(),
                                                                           singletonList(quantity), pure);
                                               } else {
                                                   final var defaultValue = $void();
                                                   final var variable = variableNode(pure, unit.toString(),
                                                                                     false,
                                                                                     defaultValue, token, this);
                                                   currentScope().setParameter("1", quantity);
                                                   return fix(variable, false);
                                               }
                                           });
                               });
    }

    private Parser<var> list(@NotNull Parser<var> expression, boolean pure) {
        return OP_NL(LEFT_BRACKET)
                       .next(expression.sepBy(COMMA_OR_NEWLINE_TERMINATOR))
                       .followedBy(COMMA_OR_NEWLINE_TERMINATOR.optional(null))
                       .followedBy(NL_OP(RIGHT_BRACKET))
                       .token()
                       .map(
                               token -> {
                                   List<var> members = (List<var>) token.value();
                                   final var node = node("list", pure, SCOPE_WITH_CLOSURE,
                                                         token, members, this,
                                                         vars -> DollarFactory.fromList(new ImmutableList<>(members))
                                   );
                                   for (var v : members) {
                                       v.$listen(i -> node.$notify());
                                   }
                                   return node;

                               });
    }

    @NotNull
    private Parser<var> mapFunc(@NotNull Parser<var> expression, boolean pure) {
        return OP_NL(LEFT_BRACE)
                       .next(expression.sepBy(COMMA_TERMINATOR))
                       .followedBy(COMMA_TERMINATOR.optional(null))
                       .followedBy(NL_OP(RIGHT_BRACE))
                       .token()
                       .map(token -> {
                           List<var> o = (List<var>) token.value();
                           final var node = node("serial", pure, SCOPE_WITH_CLOSURE,
                                                 token, o, this,
                                                 i -> {
                                                     if (o.size() == 1) {
                                                         return DollarFactory.blockCollection(o);
                                                     } else {
                                                         var parallel = i[0];
                                                         Stream<var> stream = parallel.isTrue() ? o.stream().parallel() : o.stream();
                                                         return $(stream.map(v -> v._fix(parallel.isTrue()))
                                                                          .collect(Collectors.toConcurrentMap(
                                                                                  v -> v.pair() ? v.$pairKey() : v.$S(),
                                                                                  v -> v.pair() ? v.$pairValue() : v)));
                                                     }
                                                 });
                           for (var value : o) {
                               value.$listen(i -> node.$notify());
                           }
                           return node;
                       });
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
                     parameters.optional(null)).token()
                       .map(token -> {
                           Object[] objects = (Object[]) token.value();
                           String moduleName = ((var) objects[1]).$S();
                           @SuppressWarnings("unchecked") final Iterable<var> params = (Iterable<var>) objects[2];
                           return node("module", false, NEW_SCOPE, this, token, emptyList(),
                                       in -> {
                                           String[] parts = moduleName.split(":", 2);
                                           if (parts.length < 2) {
                                               throw new IllegalArgumentException("Module " + moduleName + " needs to have a scheme");
                                           }
                                           Map<String, var> paramMap = new HashMap<>();
                                           if (params != null) {
                                               for (var param1 : params) {
                                                   paramMap.put(param1.getMetaAttribute(NAMED_PARAMETER_META_ATTR), param1);
                                               }
                                           }
                                           try {
                                               return ModuleResolver
                                                              .resolveModule(parts[0])
                                                              .resolve(parts[1], currentScope(), this)
                                                              .pipe($(paramMap))
                                                              ._fix(true);

                                           } catch (Exception e) {
                                               return currentScope().handleError(e);
                                           }

                                       });

                       });

    }

    private Parser<var> assertOperator(@NotNull Parser.Reference<var> ref, boolean pure) {
        return OP(ASSERT)
                       .next(
                               or(
                                       array(STRING_LITERAL.followedBy(OP(PAIR)), ref.lazy()),
                                       array(OP(PAIR).optional(null), ref.lazy()
                                       )
                               )
                                       .token()
                                       .map(token -> {
                                           Object[] objects = (Object[]) token.value();
                                           var message = (var) objects[0];
                                           var condition = (var) objects[1];
                                           return reactiveNode("assert", pure, NO_SCOPE,
                                                               this, token, condition,
                                                               args -> Func.assertFunc(message, condition));
                                       }));
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
                       .map(token -> {
                           Object[] objects = (Object[]) token.value();
                           var lhs = (var) objects[0];
                           var rhs = (var) objects[1];
                           var lambda = node(WHEN.keyword(), pure, NEW_SCOPE, token,
                                             asList(lhs, rhs), this,
                                             i -> lhs.isTrue() ? $((Object) rhs.toJavaObject()) : $void()
                           );
                           lhs.$listen(i -> lhs.isTrue() ? $((Object) rhs.toJavaObject()) : $void());
                           return lambda;
                       });
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
                       .map(token -> {
                           final AtomicInteger count = new AtomicInteger(-1);
                           Object[] objects = (Object[]) token.value();
                           return reactiveNode("every", false, NEW_SCOPE,
                                               this, token, (var) objects[3],
                                               args -> {
                                                   Scope scope = currentScope();
                                                   Double duration = ((var) objects[0]).toDouble();
                                                   assert duration != null;
                                                   Scheduler.schedule(i -> {
                                                       count.incrementAndGet();
                                                       return inScope(true, scope, newScope -> {
                                                           try {
                                                               newScope.setParameter("1", $(count.get()));
                                                               if ((objects[1] instanceof var) && ((BooleanAware) objects[1]).isTrue()) {
                                                                   Scheduler.cancel(i[0].$S());
                                                                   return i[0];
                                                               } else if ((objects[2] instanceof var) && ((BooleanAware) objects[2]).isTrue()) {
                                                                   return $void();
                                                               } else {
                                                                   return ((VarInternal) objects[3])._fixDeep();
                                                               }
                                                           } catch (RuntimeException e) {
                                                               return scope.handleError(e);
                                                           }

                                                       });
                                                   }, ((long) (duration * ONE_DAY)));
                                                   return $void();
                                               });

                       });
    }

    private Parser<var> functionCall(boolean pure) {
        return array(IDENTIFIER.or(BUILTIN).followedBy(OP(LEFT_PAREN).peek()))
                       .token()
                       .map(token -> {
                           Object[] objects = (Object[]) token.value();
                           var functionName = (var) objects[0];
                           return node("function-name", pure, NO_SCOPE, this, token,
                                       singletonList(functionName),
                                       args -> functionName);
                       });
    }

    final Parser<var> java(boolean pure) {
        return token(new JavaTokenMap())
                       .token()
                       .map(token -> node("java", pure, NO_SCOPE, this, token,
                                          singletonList($void()),
                                          in -> compile($void(),
                                                        (String) token.value())
                            )
                       );
    }


    private Parser<Function<? super var, ? extends var>> pipeOperator(@NotNull Parser.Reference<var> ref,
                                                                      boolean pure) {
        //noinspection unchecked
        return (OP(PIPE_OPERATOR).optional(null)).next(
                longest(BUILTIN, IDENTIFIER, functionCall(pure).postfix(parameterOperator(ref, pure)),
                        ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN))))
                       .token().map(new PipeOperator(this, pure));
    }

    private Parser<Function<? super var, ? extends var>> writeOperator(@NotNull Parser.Reference<var> ref) {
        return array(KEYWORD(WRITE_KEYWORD),
                     ref.lazy(),
                     KEYWORD(BLOCK).optional(null),
                     KEYWORD(MUTATE).optional(null)
        ).followedBy(KEYWORD(TO).optional(null))
                       .token()
                       .map(token -> {
                           Object[] objects = (Object[]) token.value();
                           var lhs = (var) objects[1];
                           boolean blocking = objects[2] != null;
                           boolean mutating = objects[3] != null;
                           return rhs -> reactiveNode("write", false, NO_SCOPE,
                                                      this, token, (var) lhs, rhs,
                                                      args -> rhs.$write(lhs, blocking, mutating)
                           );
                       });
    }

    private Parser<Function<? super var, ? extends var>> readOperator() {
        return array(KEYWORD(READ_KEYWORD),
                     KEYWORD(BLOCK).optional(null),
                     KEYWORD(MUTATE).optional(null)
        ).followedBy(KEYWORD(FROM).optional(null))
                       .token()
                       .map(token -> {
                           Object[] objects = (Object[]) token.value();
                           boolean blocking = objects[1] != null;
                           boolean mutating = objects[2] != null;
                           return rhs -> {
                               List<var> in = asList((var) objects[1], (var) objects[2], rhs);
                               return node("read", false, NO_SCOPE,
                                           this, token, in,
                                           i -> rhs.$read(blocking, mutating)
                               );
                           };
                       });
    }

    private Parser<Function<var, var>> ifOperator(@NotNull Parser.Reference<var> ref, boolean pure) {
        return KEYWORD_NL(IF_OPERATOR)
                       .next(ref.lazy())
                       .token()
                       .map(token -> {
                           var lhs = (var) token.value();
                           return rhs -> node("if", pure,
                                              NO_SCOPE, this,
                                              token, asList(lhs, rhs),
                                              i -> Func.ifFunc(lhs, rhs));
                       });
    }

    @SuppressWarnings("unchecked")
    private Parser<Function<? super var, ? extends var>> isOperator(boolean pure) {
        return KEYWORD(IS)
                       .next(IDENTIFIER.sepBy(OP(COMMA)))
                       .token()
                       .map(token -> lhs -> reactiveNode("is", pure,
                                                         NO_SCOPE, this, token, lhs,
                                                         args -> Func.isFunc(lhs, (List<var>) token.value())

                            )
                       );
    }

    private Parser<Function<? super var, ? extends var>> memberOperator(@NotNull Parser.Reference<var> ref, boolean pure) {
        return OP(MEMBER).followedBy(OP(MEMBER).not())
                       .next(ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN)).or(IDENTIFIER))
                       .token()
                       .map(rhs -> lhs -> reactiveNode("member", pure, NO_SCOPE,
                                                       this, rhs, lhs,
                                                       (var) rhs.value(),
                                                       args -> lhs.$(rhs.toString())
                       ));
    }

    private Parser<Function<? super var, ? extends var>> forOperator(final @NotNull Parser.Reference<var> ref, boolean pure) {
        return array(KEYWORD(FOR), IDENTIFIER, KEYWORD(IN), ref.lazy())
                       .token()
                       .map(token -> {
                           Object[] objects = (Object[]) token.value();
                           String varName = objects[1].toString();
                           var iterable = (var) objects[3];
                           return rhs -> reactiveNode("for", pure, NEW_SCOPE, this, token, rhs,
                                                      args -> Func.forFunc(pure, varName, iterable, rhs));
                       });
    }

    private Parser<Function<? super var, ? extends var>> whileOperator(final @NotNull Parser.Reference<var> ref, boolean pure) {
        return KEYWORD(WHILE)
                       .next(ref.lazy())
                       .token()
                       .map(token -> {
                           var lhs = (var) token.value();
                           return rhs -> node("while", pure, NEW_SCOPE,
                                              this, token, asList(lhs, rhs),
                                              i -> Func.whileFunc(lhs, rhs));
                       });
    }

    private Parser<Function<? super var, ? extends var>> subscriptOperator(@NotNull Parser.Reference<var> ref, boolean pure) {
        return OP(LEFT_BRACKET).next(
                array(
                        ref.lazy().followedBy(OP(RIGHT_BRACKET)),
                        OP(ASSIGNMENT)
                                .next(ref.lazy()).optional(null))
        )
                       .token()
                       .map(token -> {
                           Object[] rhs = (Object[]) token.value();
                           return lhs -> {
                               if (rhs[1] == null) {
                                   return reactiveNode("subscript", pure, NO_SCOPE,
                                                       this, token, lhs, (var) rhs[0],
                                                       args -> lhs.$get(((var) rhs[0])));
                               } else {
                                   return node("subscript-assignment",
                                               pure, NO_SCOPE, this, token,
                                               asList(lhs, (var) rhs[0], (var) rhs[1]),
                                               i -> lhs.$set((var) rhs[0], rhs[1]));
                               }
                           };
                       });
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
                        }).sepBy(COMMA_TERMINATOR)).followedBy(OP(RIGHT_PAREN))
                       .token().map(new ParameterOperator(this, pure));
    }

    private Parser<Function<? super var, ? extends var>> variableUsageOperator(boolean pure) {
        return or(
                OP(DOLLAR)
                        .followedBy(OP(LEFT_PAREN).peek())
                        .token()
                        .map(token -> rhs -> node("variable-usage",
                                                  pure, NO_SCOPE, this,
                                                  token, singletonList(rhs),
                                                  i -> variableNode(pure, rhs.toString(),
                                                                    false, $void(), token, this))),
                OP(DOLLAR)
                        .followedBy(INTEGER_LITERAL.peek())
                        .token()
                        .map(lhs -> rhs -> variableNode(pure, rhs.toString(), true, null, lhs, this)));
    }

    private Parser<Function<? super var, ? extends var>> castOperator(boolean pure) {
        return KEYWORD(AS)
                       .next(IDENTIFIER)
                       .token()
                       .map(token -> lhs -> reactiveNode("as", pure, NO_SCOPE,
                                                         this, token, lhs,
                                                         i -> Func.castFunc(lhs, token.toString())
                            )
                       );
    }

    private Parser<Function<? super var, ? extends var>> assignmentOperator(@NotNull Parser.Reference<var> ref,
                                                                            boolean pure) {
        return array(KEYWORD(EXPORT).optional(null),
                     or(
                             KEYWORD(CONST),
                             KEYWORD(VOLATILE),
                             KEYWORD(VAR)
                     ).optional(null),
                     IDENTIFIER.between(OP(LT), OP(GT)).optional(null),
                     ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN)).optional(null),
                     OP(DOLLAR).next(
                             ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN))
                     ).or(IDENTIFIER).or(BUILTIN),
                     or(
                             OP(ASSIGNMENT),
                             OP(LISTEN_ASSIGN),
                             OP(SUBSCRIBE_ASSIGN))
        ).token().map(new AssignmentOperator(pure, this));
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

    private <T> Parser<T> op(@NotNull OpDef def, @NotNull T value) {
        return OP(def).token().map(new SourceMapper<>(value));

    }

    private static class JavaTokenMap implements TokenMap<String> {
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
