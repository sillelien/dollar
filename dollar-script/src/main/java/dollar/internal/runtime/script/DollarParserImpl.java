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

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import dollar.api.ControlFlowAware;
import dollar.api.DollarStatic;
import dollar.api.NumericAware;
import dollar.api.Scope;
import dollar.api.URIAware;
import dollar.api.VarInternal;
import dollar.api.types.DollarFactory;
import dollar.api.types.DollarRange;
import dollar.api.var;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.ParserOptions;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import dollar.internal.runtime.script.operators.AssignmentOperator;
import dollar.internal.runtime.script.operators.CollectOperator;
import dollar.internal.runtime.script.operators.DefinitionOperator;
import dollar.internal.runtime.script.operators.ParameterOperator;
import dollar.internal.runtime.script.operators.PureDefinitionOperator;
import dollar.internal.runtime.script.operators.WindowOperator;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.google.common.io.Files.asCharSource;
import static dollar.api.DollarStatic.$;
import static dollar.api.DollarStatic.$void;
import static dollar.api.scripting.ScriptingSupport.compile;
import static dollar.api.types.meta.MetaConstants.*;
import static dollar.internal.runtime.script.DollarLexer.*;
import static dollar.internal.runtime.script.DollarScriptSupport.*;
import static dollar.internal.runtime.script.Func.*;
import static dollar.internal.runtime.script.OperatorPriority.EQ_PRIORITY;
import static dollar.internal.runtime.script.SourceNodeOptions.NO_SCOPE;
import static dollar.internal.runtime.script.parser.Symbols.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.jparsec.Parsers.*;

public class DollarParserImpl implements DollarParser {
    @NotNull
    public static final String NAMED_PARAMETER_META_ATTR = "__named_parameter";
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("DollarParser");
    @NotNull
    private final ClassLoader classLoader;
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
        export.meta(SCOPES, new ArrayList<>(DollarScriptSupport.scopes()));
        exports.put(name, export);
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
                ErrorHandlerFactory.instance().handleTopLevel(e, null,
                                                              (file != null) ? new File(file) : null);

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
            return parse(new ScriptScope(source, file, true, parallel), source);
        }

    }

    @Override
    @NotNull
    public var parse(@NotNull InputStream in, boolean parallel, @NotNull Scope scope) throws Exception {
        String source = new String(ByteStreams.toByteArray(in));
        return parse(new ScriptScope(scope, "(stream)", source, "(stream-scope)", true, parallel), source);
    }

    @Override
    @NotNull
    public var parse(@NotNull InputStream in, @NotNull String file, boolean parallel) throws Exception {
        this.file = file;
        String source = new String(ByteStreams.toByteArray(in));
        return parse(new ScriptScope(source, new File(file), true, parallel), source);
    }

    @Override
    @NotNull
    public var parse(@NotNull String source, boolean parallel) throws Exception {
        return parse(new ScriptScope(source, "(string)", true, parallel), source);
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
    public var parseMarkdown(@NotNull File file) throws IOException {
        PegDownProcessor pegDownProcessor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS);
        RootNode root =
                pegDownProcessor.parseMarkdown(asCharSource(file, Charset.forName("utf-8"))
                                                       .read().toCharArray());
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
                expressions.get(i).$fixDeep(false);
//              System.err.println(fixed);
            }
            var resultVar = expressions.get(expressions.size() - 1);
            var fixedResult = resultVar.$fixDeep(false);
            log.debug("Ended Runtime Phase");
            return fixedResult;
        });
        ref.set(parser);
        return parser;

    }


    @NotNull
    private Parser<var> expression(final boolean pure) throws Exception {
        Parser<var> main;
        Parser.Reference<var> ref = Parser.newReference();
        if (pure) {
            main = ref.lazy()
                           .between(OP(LEFT_PAREN), OP(RIGHT_PAREN))
                           .or(Parsers.or(unitExpression(true),
                                          listExpression(ref.lazy(), true),
                                          mapExpression(ref.lazy(), true),
                                          assertExpression(ref, true),
                                          collectExpression(ref, true),
                                          windowExpression(ref, true),
                                          whenExpression(ref.lazy(), true),
                                          functionCall(true),
                                          rangeExpression(ref, true),
                                          DECIMAL_LITERAL,
                                          INTEGER_LITERAL,
                                          STRING_LITERAL,
                                          IDENTIFIER_KEYWORD,
                                          variableRef(true),
                                          builtin(true)))
                           .or(blockExpression(ref.lazy(), true).between(OP_NL(LEFT_BRACE), NL_OP(RIGHT_BRACE)));
        } else {
            main = ref.lazy()
                           .between(OP(LEFT_PAREN), OP(RIGHT_PAREN))
                           .or(Parsers.or(unitExpression(false),
                                          listExpression(ref.lazy(), false),
                                          mapExpression(ref.lazy(), false),
                                          KEYWORD(PURE).next(expression(true)),
                                          moduleExpression(ref),
                                          assertExpression(ref, false),
                                          collectExpression(ref, false),
                                          windowExpression(ref, false),
                                          whenExpression(ref.lazy(), false),
                                          everyExpression(ref.lazy(), false),
                                          functionCall(false),
                                          scriptExpression(ref, false),
                                          pureDefinitionOperator(ref),
                                          rangeExpression(ref, false),
                                          URL,
                                          DECIMAL_LITERAL,
                                          INTEGER_LITERAL,
                                          STRING_LITERAL,
                                          dollarIdentifier(ref),
                                          IDENTIFIER_KEYWORD,
                                          builtin(false),
                                          variableRef(false)))
                           .or(blockExpression(ref.lazy(), false).between(OP_NL(LEFT_BRACE), NL_OP(RIGHT_BRACE)));
        }

        OperatorTable<var> table = new OperatorTable<>();

        table = infixl(pure, table, PLUS, var::$plus);
        table = infixl(pure, table, MINUS, var::$minus);
        table = infixl(pure, table, CHOOSE, ControlFlowAware::$choose);
        table = infixl(pure, table, DEFAULT, var::$default);
        table = infixl(pure, table, DIVIDE, NumericAware::$divide);
        table = infixl(pure, table, MOD, NumericAware::$modulus);


        table = infixl(pure, table, INEQUALITY_OPERATOR, var::$notEquals);
        table = infixl(pure, table, EQUALITY, var::$equals);
        table = infixl(pure, table, AND, DollarStatic::$and);
        table = infixl(pure, table, OR, DollarStatic::$or);
        table = infixl(pure, table, LT, DollarStatic::$lt);
        table = infixl(pure, table, GT, DollarStatic::$gt);
        table = infixl(pure, table, LT_EQUALS, DollarStatic::$lte);
        table = infixl(pure, table, GT_EQUALS, DollarStatic::$gte);
        table = infixl(pure, table, MULTIPLY, Func::multiplyFunc);
        table = infixl(pure, table, PAIR, Func::pairFunc);
        table = infixl(pure, table, ELSE, Func::elseFunc);
        table = infixl(pure, table, IN, Func::inFunc);
        table = infixl(pure, table, WHEN, Func::listenFunc);

        table = infixl(pure, table, EACH, (lhs, rhs) -> eachFunc(pure, lhs, rhs));
        table = infixl(pure, table, REDUCE, (lhs, rhs) -> reduceFunc(pure, lhs, rhs));
        table = infixl(pure, table, CAUSES, (lhs, rhs) -> causesFunc(pure, lhs, rhs));


        table = infixlReactive(pure, table, ASSERT_EQ_REACT, Func::assertEqualsFunc);
        table = infixlUnReactive(pure, table, ASSERT_EQ_UNREACT, Func::assertEqualsFunc);


        table = postfix(pure, table, DEC, var::$dec);
        table = postfix(pure, table, INC, var::$inc);

        table = prefix(pure, table, NEGATE, var::$negate);
        table = prefix(pure, table, SIZE, var::$size);

        table = prefix(pure, table, NOT, DollarStatic::$not);
        table = prefix(pure, table, ERROR, Func::errorFunc);
        table = prefix(pure, table, TRUTHY, DollarStatic::$truthy);

        table = postfix(pure, table, MIN, v -> v.$min(currentScope().parallel()));
        table = postfix(pure, table, MAX, v -> v.$max(currentScope().parallel()));
        table = postfix(pure, table, SUM, v -> v.$sum(currentScope().parallel()));
        table = postfix(pure, table, PRODUCT, v -> v.$product(currentScope().parallel()));
        table = postfix(pure, table, SPLIT, var::$list);
        table = prefix(pure, table, SORT, v -> v.$sort(currentScope().parallel()));
        table = postfix(pure, table, REVERSE, var -> var.$reverse(currentScope().parallel()));
        table = postfix(pure, table, UNIQUE, v -> v.$unique(currentScope().parallel()));
        table = postfix(pure, table, AVG, v -> v.$avg(currentScope().parallel()));

        table = prefixUnReactive(pure, table, FIX, VarInternal::$fixDeep);

        table = table.prefix(parallelOperator(ref, pure), PARALLEL.priority());
        table = table.prefix(serialOperator(ref, pure), SERIAL.priority());
        //More complex expression syntax
        table = table.postfix(pipeOperator(ref, pure), PIPE_OP.priority());
        table = table.postfix(isOperator(pure), EQ_PRIORITY);
        table = table.postfix(memberOperator(ref, pure), MEMBER.priority());
        table = table.postfix(subscriptOperator(ref, pure), SUBSCRIPT_OP.priority());
        table = table.postfix(castOperator(pure), CAST.priority());
        table = table.postfix(parameterOperator(ref, pure), PARAM_OP.priority());

        table = table.prefix(ifOperator(ref, pure), IF_OP.priority());
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

            table = prefix(false, table, PRINT, DollarStatic::$out);
            table = prefix(false, table, DEBUG, DollarStatic::$debug);
            table = prefix(false, table, ERR, DollarStatic::$err);
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
                                       @NotNull OpDef operator,
                                       @NotNull Function<var, var> f2) {

        return table.postfix(
                DollarLexer.OPERATORS.token(operator.symbol()).token().map(new SourceMapper<>(new DollarUnaryOperator(this,
                                                                                                                      operator,
                                                                                                                      f2, pure))),
                operator.priority()).
                                            prefix(DollarLexer.KEYWORDS.token(operator.keyword()).token().map(
                                                    new SourceMapper<>(new DollarUnaryOperator(this,
                                                                                               operator, f2, pure))),
                                                   operator.priority());
    }

    @NotNull
    private OperatorTable<var> infixlReactive(boolean pure,
                                              @NotNull OperatorTable<var> table,
                                              @NotNull OpDef operator,
                                              @NotNull BiFunction<var, var, var> f) {
        return table.infixl(op(operator, new BinaryOp(false, operator, this, f, pure)),
                            operator.priority());
    }

    @NotNull
    private OperatorTable<var> infixlUnReactive(boolean pure,
                                                @NotNull OperatorTable<var> table,
                                                @NotNull OpDef operator,
                                                @NotNull BiFunction<var, var, var> f) {
        return table.infixl(op(operator, new BinaryOp(true, operator, this, f, pure)),
                            operator.priority());
    }

    @NotNull
    private OperatorTable<var> prefixUnReactive(boolean pure,
                                                @NotNull OperatorTable<var> table,
                                                @NotNull OpDef operator,
                                                @NotNull Function<var, var> f) {
        return table.prefix(op(operator, new DollarUnaryOperator(true, f, operator, this, pure)),
                            operator.priority());
    }

    @NotNull
    private OperatorTable<var> prefix(boolean pure,
                                      @NotNull OperatorTable<var> table,
                                      @NotNull OpDef operator,
                                      @NotNull Function<var, var> f) {
        return table.prefix(op(operator, new DollarUnaryOperator(this, operator, f, pure)), operator.priority());
    }

    @NotNull
    private OperatorTable<var> infixl(boolean pure,
                                      @NotNull OperatorTable<var> table,
                                      @NotNull OpDef operator,
                                      @NotNull BiFunction<var, var, var> func) {
        return table.infixl(op(operator, new BinaryOp(this, operator, func, pure)), operator.priority());
    }

    @NotNull
    private Parser<var> builtin(boolean pure) {
        return BUILTIN.token()
                       .map(token -> reactiveNode(BUILTIN_OP, pure, (var) token.value(), token, this,
                                                  args -> Builtins.execute(token.toString(), emptyList(), pure)));
    }

    private Parser<var> dollarIdentifier(@NotNull Parser.Reference<var> ref) {
        return OP(DOLLAR).next(
                array(Terminals.Identifier.PARSER, OP(DEFAULT).next(ref.lazy()).optional(null)).between(
                        OP(LEFT_BRACE),
                        OP(RIGHT_BRACE)
                )
        )
                       .token()
                       .map(token -> {
                           Object[] objects = (Object[]) token.value();
                           String varName = objects[0].toString();
                           var defaultVal = (var) objects[1];
                           return variableNode(false, varName, false, defaultVal, token, this);
                       });
    }


    @NotNull
    private Parser<var> variableRef(boolean pure) {
        return identifier().followedBy(OP(LEFT_PAREN).not().peek())
                       .token()
                       .map(token -> variableNode(pure, token.toString(), token, this));
    }

    private Parser<var> unitExpression(boolean pure) {
        return array(DECIMAL_LITERAL.or(INTEGER_LITERAL), BUILTIN)
                       .token()
                       .map(
                               token -> {
                                   Object[] objects = (Object[]) token.value();
                                   var quantity = (var) objects[0];
                                   var unit = (var) objects[1];
                                   return node(UNIT_OP, pure, this, token, asList(quantity, unit),
                                               i -> {
                                                   String unitName = unit.toString();
                                                   if (Builtins.exists(unitName)) {
                                                       return Builtins.execute(unitName, singletonList(quantity), pure);
                                                   } else {
                                                       final var variable = variableNode(pure, unitName, token, this);
                                                       currentScope().parameter("1", quantity);
                                                       return DollarScriptSupport.fix(variable);
                                                   }
                                               });
                               });
    }

    private Parser<var> listExpression(@NotNull Parser<var> expression, boolean pure) {
        return OP_NL(LEFT_BRACKET)
                       .next(expression.sepBy(COMMA_OR_NEWLINE_TERMINATOR))
                       .followedBy(COMMA_OR_NEWLINE_TERMINATOR.optional(null))
                       .followedBy(NL_OP(RIGHT_BRACKET))
                       .token()
                       .map(
                               token -> {
                                   List<var> entries = (List<var>) token.value();
                                   final var node = node(LIST_OP, pure, this, token, entries,
                                                         vars -> {
                                                             log.info("Fixing list {}",
                                                                      currentScope().parallel() ? "parallel" : "serial");
                                                             return DollarFactory.fromList(entries).$fix(1,
                                                                                                         currentScope().parallel());
                                                         }
                                   );
                                   entries.forEach(entry -> entry.$listen(i -> node.$notify()));
                                   return node;

                               });
    }

    private Parser<var> rangeExpression(@NotNull Parser.Reference<var> expression, boolean pure) {
        return array(or(OP(LEFT_BRACKET), OP(LEFT_PAREN)),//0
                     expression.lazy().optional(null),//1
                     OP(RANGE),//2
                     expression.lazy().optional(null),//3
                     or(OP(RIGHT_BRACKET), OP(RIGHT_PAREN))//4
        ).token()
                       .map(
                               token -> {
                                   Object[] objects = (Object[]) token.value();
                                   var upperBound = (var) objects[3];
                                   var lowerBound = (var) objects[1];
                                   return node(RANGE, pure, this, token, Arrays.asList(lowerBound, upperBound),
                                               args -> {
                                                   boolean closedLeft = objects[0].toString().equals(LEFT_BRACKET.symbol());
                                                   boolean closedRight = objects[4].toString().equals(RIGHT_BRACKET.symbol());
                                                   boolean lowerBounds = lowerBound != null;
                                                   boolean upperBounds = upperBound != null;
                                                   if (!closedLeft && !closedRight && (lowerBound != null) && (upperBound != null)
                                                               && lowerBound.$unwrap().equals(upperBound.$unwrap())) {
                                                       throw new DollarScriptException("Cannot create an open range with " +
                                                                                               "identical upper and lower " +
                                                                                               "bounds", new SourceSegmentValue
                                                                                                                 (currentScope(),
                                                                                                                  token));
                                                   } else {
                                                       return DollarFactory.wrap(
                                                               new DollarRange(ImmutableList.of(), lowerBounds, upperBounds,
                                                                               closedLeft, closedRight, lowerBound, upperBound));
                                                   }
                                               });

                               });
    }

    @NotNull
    private Parser<var> mapExpression(@NotNull Parser<var> expression, boolean pure) {
        return OP_NL(LEFT_BRACE)
                       .next(expression.sepBy(COMMA_TERMINATOR))
                       .followedBy(COMMA_TERMINATOR.optional(null))
                       .followedBy(NL_OP(RIGHT_BRACE))
                       .token()
                       .map(token -> {
                           List<var> o = (List<var>) token.value();
                           final var node = node(MAP_OP, pure, this, token, o, i -> mapFunc(o));
                           o.forEach(entry -> entry.$listen(i -> node.$notify()));
                           return node;
                       });
    }

    @NotNull
    private Parser<var> blockExpression(@NotNull Parser<var> parentParser, boolean pure) throws Exception {
        Parser.Reference<var> ref = Parser.newReference();
        //Now we do the complex part, the following will only return the last value in the
        //block when the block is evaluated, but it will trigger execution of the rest.
        //This gives it functionality like a conventional function in imperative languages
        Parser<var> or = (or(parentParser, parentParser.between(OP_NL(LEFT_BRACE), NL_OP(RIGHT_BRACE))))
                                 .sepBy1(SEMICOLON_TERMINATOR)
                                 .followedBy(SEMICOLON_TERMINATOR.optional(null))
                                 .map(var -> var)
                                 .token()
                                 .map(token -> node(BLOCK_OP, pure, this, token, (List<var>) token.value(),
                                                    i -> blockFunc((List<var>) token.value())));
        ref.set(or);
        return or;
    }

    private Parser<var> moduleExpression(@NotNull Parser.Reference<var> ref) {
        final Parser<Object[]> param = array(IDENTIFIER.followedBy(OP(ASSIGNMENT)), ref.lazy());

        final Parser<List<var>> parameters =
                KEYWORD(WITH).optional(null).next((param).map(objects -> {
                    var result = (var) objects[1];
                    result.metaAttribute(NAMED_PARAMETER_META_ATTR, objects[0].toString());
                    return result;
                }).sepBy(OP(COMMA)).between(OP(LEFT_PAREN), OP(RIGHT_PAREN)));

        return array(KEYWORD(MODULE_OP), STRING_LITERAL.or(URL),
                     parameters.optional(null)).token()
                       .map(token -> {
                           Object[] objects = (Object[]) token.value();
                           return node(MODULE_OP, false, this, token, emptyList(),
                                       i -> moduleFunc(this, ((var) objects[1]).$S(), (Iterable<var>) objects[2]));

                       });

    }

    private Parser<var> assertExpression(@NotNull Parser.Reference<var> ref, boolean pure) {
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
                                           return reactiveNode(ASSERT, pure, condition, token, this,
                                                               i -> assertFunc(message, condition));
                                       }));
    }

    private Parser<var> collectExpression(@NotNull Parser.Reference<var> ref, boolean pure) {
        return KEYWORD_NL(COLLECT_OP)
                       .next(
                               array(ref.lazy(),
                                     KEYWORD(UNTIL).next(ref.lazy()).optional(null),
                                     KEYWORD(UNLESS).next(ref.lazy()).optional(null),
                                     ref.lazy())
                       )
                       .token()
                       .map(new CollectOperator(this, pure));
    }


    private Parser<var> windowExpression(@NotNull Parser.Reference<var> ref, boolean pure) {
        return KEYWORD_NL(WINDOW_OP)
                       .next(
                               array(ref.lazy(),//0
                                     KEYWORD(OVER).next(ref.lazy()),//1
                                     KEYWORD(PERIOD).next(ref.lazy()).optional(null),//2
                                     KEYWORD(UNLESS).next(ref.lazy()).optional(null),//3
                                     KEYWORD(UNTIL).next(ref.lazy()).optional(null),//4
                                     ref.lazy())//5

                       )
                       .token()
                       .map(new WindowOperator(this, pure));
    }


    private Parser<var> whenExpression(@NotNull Parser<var> expression, boolean pure) {
        return KEYWORD_NL(WHEN)
                       .next(array(expression, expression))
                       .token()
                       .map(token -> {
                           assert WHEN.validForPure(pure);
                           Object[] objects = (Object[]) token.value();
                           var lhs = (var) objects[0];
                           var rhs = (var) objects[1];
                           var lambda = node(WHEN, pure, this, token, asList(lhs, rhs),
                                             i -> lhs.isTrue() ? $((Object) rhs.toJavaObject()) : $void()
                           );
                           lhs.$listen(i -> lhs.isTrue() ? $((Object) rhs.toJavaObject()) : $void());
                           return lambda;
                       });
    }

    private Parser<var> everyExpression(@NotNull Parser<var> expression, boolean pure) {
        return KEYWORD_NL(EVERY_OP)
                       .next(array(unitExpression(false),
                                   KEYWORD(UNTIL).next(expression).optional(null),
                                   KEYWORD(UNLESS).next(expression).optional(null),
                                   expression))
                       .token()
                       .map(token -> {
                           assert EVERY_OP.validForPure(pure);
                           final AtomicInteger count = new AtomicInteger(-1);
                           Object[] objects = (Object[]) token.value();
                           var durationVar = (var) objects[0];
                           var until = (var) objects[1];
                           var unless = (var) objects[2];
                           var block = (var) objects[3];
                           return reactiveNode(EVERY_OP, false, block, token, this,
                                               i -> everyFunc(count, durationVar, until, unless, block));

                       });
    }

    private Parser<var> functionCall(boolean pure) {
        return array(IDENTIFIER.or(BUILTIN).followedBy(OP(LEFT_PAREN).peek()))
                       .token()
                       .map(token -> {
                           Object[] objects = (Object[]) token.value();
                           var functionName = (var) objects[0];
                           return node(FUNCTION_NAME_OP, pure, this, token, singletonList(functionName), i -> functionName);
                       });
    }

    final Parser<var> scriptExpression(@NotNull Parser.Reference<var> ref, boolean pure) {
        return array(or(BUILTIN).or(IDENTIFIER),
                     token(new BacktickScriptMap()))
                       .token()
                       .map((Token token) -> {
                                Object[] objects = (Object[]) token.value();
                           return node(SCRIPT_OP, pure,
                                       this, token, singletonList($void()),
                                            i -> compile(String.valueOf(objects[0]), String.valueOf(objects[1]),
                                                         currentScope()));
                            }
                       );
    }


    private Parser<Function<? super var, ? extends var>> pipeOperator(@NotNull Parser.Reference<var> ref, boolean pure) {
        return (OP(PIPE_OP).optional(null)).next(
                longest(BUILTIN,
                        IDENTIFIER,
                        functionCall(pure).postfix(parameterOperator(ref, pure)),
                        ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN))
                )).token()
                       .map(token -> {
                           assert PIPE_OP.validForPure(pure);
                           var rhs = (var) token.value();
                           return lhs -> reactiveNode(PIPE_OP, pure, rhs, token, this, i -> pipeFunc(this, pure, token, rhs, lhs)
                           );
                       });
    }

    private Parser<Function<? super var, ? extends var>> writeOperator(@NotNull Parser.Reference<var> ref) {
        return array(KEYWORD(WRITE_OP),
                     ref.lazy(),
                     KEYWORD(BLOCK).optional(null),
                     KEYWORD(MUTATE).optional(null)
        ).followedBy(KEYWORD(TO).optional(null))
                       .token()
                       .map(token -> {
                           assert WRITE_OP.validForPure(false);
                           Object[] objects = (Object[]) token.value();
                           var lhs = (var) objects[1];
                           boolean blocking = objects[2] != null;
                           boolean mutating = objects[3] != null;
                           return rhs -> reactiveNode(WRITE_OP, false, token, lhs, rhs, this,
                                                      i -> rhs.$write(lhs, blocking, mutating)
                           );
                       });
    }

    private Parser<Function<? super var, ? extends var>> readOperator() {
        return array(KEYWORD(READ_OP),
                     KEYWORD(BLOCK).optional(null),
                     KEYWORD(MUTATE).optional(null)
        ).followedBy(KEYWORD(FROM).optional(null))
                       .token()
                       .map(token -> {
                           assert READ_OP.validForPure(false);
                           Object[] objects = (Object[]) token.value();
                           boolean blocking = objects[1] != null;
                           boolean mutating = objects[2] != null;
                           return rhs -> {
                               List<var> in = asList((var) objects[1], (var) objects[2], rhs);
                               return node(READ_OP, false, this, token, in, i -> rhs.$read(blocking, mutating)
                               );
                           };
                       });
    }

    private Parser<Function<var, var>> ifOperator(@NotNull Parser.Reference<var> ref, boolean pure) {
        return KEYWORD_NL(IF_OP)
                       .next(ref.lazy())
                       .token()
                       .map(token -> {
                           var lhs = (var) token.value();
                           assert IF_OP.validForPure(pure);
                           return rhs -> node(IF_OP, pure, this, token, asList(lhs, rhs), i -> ifFunc(pure, lhs, rhs));
                       });
    }


    private Parser<Function<var, var>> parallelOperator(@NotNull Parser.Reference<var> ref, boolean pure) {
        return OP(PARALLEL)
                       .token()
                       .map(token -> rhs -> {
                           assert PARALLEL.validForPure(pure);
                           return node(PARALLEL, pure, this, token, singletonList(rhs), ns -> rhs.$fix(2, true));
                       });
    }

    private Parser<Function<var, var>> serialOperator(@NotNull Parser.Reference<var> ref, boolean pure) {
        return OP(SERIAL)
                       .token()
                       .map(token -> rhs -> {
                           assert SERIAL.validForPure(pure);
                           return node(SERIAL, pure, this, token, singletonList(rhs), ns -> rhs.$fix(2, false));
                       });
    }

    private Parser<Function<? super var, ? extends var>> isOperator(boolean pure) {
        return KEYWORD(IS_OP)
                       .next(IDENTIFIER.sepBy(OP(COMMA)))
                       .token()
                       .map(token -> lhs -> {
                                assert IS_OP.validForPure(pure);
                           return reactiveNode(IS_OP, pure, lhs, token, this,
                                               i -> isFunc(lhs, (List<var>) token.value())

                           );
                            }
                       );
    }

    private Parser<Function<? super var, ? extends var>> memberOperator(@NotNull Parser.Reference<var> ref, boolean pure) {
        return OP(MEMBER).followedBy(OP(MEMBER).not())
                       .next(ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN)).or(IDENTIFIER))
                       .token()
                       .map(rhs -> lhs -> {
                           assert MEMBER.validForPure(pure);
                           return reactiveNode(MEMBER, pure, rhs, lhs, (var) rhs.value(), this,
                                               i -> lhs.$(rhs.toString())
                           );
                       });
    }

    private Parser<Function<? super var, ? extends var>> forOperator(final @NotNull Parser.Reference<var> ref, boolean pure) {

        return array(KEYWORD(FOR_OP), IDENTIFIER, KEYWORD(IN), ref.lazy())
                       .token()
                       .map(token -> {
                           assert FOR_OP.validForPure(pure);
                           Object[] objects = (Object[]) token.value();
                           String varName = objects[1].toString();
                           var iterable = (var) objects[3];
                           return rhs -> reactiveNode(FOR_OP, pure, rhs, token, this,
                                                      i -> forFunc(pure, varName, iterable, rhs));
                       });
    }

    private Parser<Function<? super var, ? extends var>> whileOperator(final @NotNull Parser.Reference<var> ref, boolean pure) {

        return KEYWORD(WHILE_OP)
                       .next(ref.lazy())
                       .token()
                       .map(token -> {
                           assert WHILE_OP.validForPure(pure);
                           var lhs = (var) token.value();
                           return rhs -> node(WHILE_OP, pure, this, token, asList(lhs, rhs),
                                              i -> whileFunc(pure, lhs, rhs));
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
                           assert SUBSCRIPT_OP.validForPure(pure);
                           Object[] objects = (Object[]) token.value();
                           var expression = (var) objects[0];
                           var subscript = (var) objects[1];

                           return lhs -> {
                               SourceSegmentValue source = new SourceSegmentValue(currentScope(), token);
                               if (subscript == null) {
                                   return reactiveNode(SUBSCRIPT_OP, SUBSCRIPT_OP.name() + "-read", pure, NO_SCOPE, this, source,
                                                       lhs, expression, args -> lhs.$get(expression));
                               } else {
                                   return node(SUBSCRIPT_OP, SUBSCRIPT_OP.name() + "-write", pure, NO_SCOPE, this, source,
                                               asList(lhs, expression, subscript),
                                               i -> lhs.$set(expression, subscript));
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
                            assert PARAM_OP.validForPure(pure);
                            //Is it a named parameter
                            if (objects[0] != null) {
                                //yes so let's add the name as metadata to the value
                                var result = (var) objects[1];
                                result.metaAttribute(NAMED_PARAMETER_META_ATTR, objects[0].toString());
                                return result;
                            } else {
                                //no, just use the value
                                return (var) objects[1];
                            }
                        }).sepBy(COMMA_TERMINATOR)).followedBy(OP(RIGHT_PAREN))
                       .token().map(new ParameterOperator(this, pure));
    }

    private Parser<Function<? super var, ? extends var>> variableUsageOperator(boolean pure) {
        assert VAR_USAGE_OP.validForPure(pure);
        return or(
                OP(DOLLAR)
                        .followedBy(OP(LEFT_PAREN).peek())
                        .token()
                        .map(token -> rhs -> node(VAR_USAGE_OP, pure, this, token, singletonList(rhs),
                                                  i -> variableNode(pure, rhs.$S(), token, this))),
                OP(DOLLAR)
                        .followedBy(INTEGER_LITERAL.peek())
                        .token()
                        .map(lhs -> rhs -> variableNode(pure, rhs.toString(), true, null, lhs, this)));
    }

    private Parser<Function<? super var, ? extends var>> castOperator(boolean pure) {
        return KEYWORD(AS)
                       .next(IDENTIFIER)
                       .token()
                       .map(token -> lhs -> {
                                assert CAST.validForPure(pure);
                           return reactiveNode(CAST, pure, lhs, token, this, i -> castFunc(lhs, token.toString())
                           );
                            }
                       );
    }

    private Parser<Function<? super var, ? extends var>> assignmentOperator(@NotNull Parser.Reference<var> ref,
                                                                            boolean pure) {
        assert ASSIGNMENT.validForPure(pure);

        return array(KEYWORD(EXPORT).optional(null),//0
                     or(
                             KEYWORD(CONST),
                             KEYWORD(VOLATILE),
                             KEYWORD(VAR)
                     ).optional(null),//1
                     IDENTIFIER.between(OP(LT), OP(GT)).optional(null),//2
                     ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN)).optional(null).token().map((Token token) -> {
                         if (token.value() != null) {
                             ((var) token.value()).meta(CONSTRAINT_SOURCE, new SourceSegmentValue(currentScope(), token));
                             return token.value();
                         } else {
                             return null;
                         }
                     }),//3
                     OP(DOLLAR).next(ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN))).or(IDENTIFIER).or(BUILTIN), //4
                     or(
                             OP(ASSIGNMENT).map(i -> {
                                 var var = $(i.toString());
                                 var.metaAttribute(ASSIGNMENT_TYPE, "assign");
                                 return var;
                             }),
                             OP(WHEN).next(ref.lazy()).followedBy(OP(ASSIGNMENT)).map(var -> {
                                 var.metaAttribute(ASSIGNMENT_TYPE, "when");
                                 return var;
                             }),
                             OP(SUBSCRIBE_ASSIGN).map(i -> {
                                 var var = $(i.toString());
                                 var.metaAttribute(ASSIGNMENT_TYPE, "subscribe");
                                 return var;
                             })
                     )//5
        ).token().map(new AssignmentOperator(pure, this));
    }


    private Parser<var> pureDefinitionOperator(@NotNull Parser.Reference<var> ref) throws Exception {
        assert PURE_OP.validForPure(true);

        return KEYWORD(PURE).next(or(
                array(
                        KEYWORD(EXPORT).optional(null),//0
                        IDENTIFIER.between(OP(LT), OP(GT)).optional(null),//1
                        OP(DOLLAR).next(ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN))).or(IDENTIFIER).token().map(
                                (Token token) -> {
                                    ((var) token.value()).meta(CONSTRAINT_SOURCE, new SourceSegmentValue(currentScope(), token));
                                    return token.value();
                                }), //2
                        OP(DEFINITION),//3
                        expression(true)//4


                ).token().map(new PureDefinitionOperator(this, false)),

                KEYWORD(EXPORT).optional(null), //0
                IDENTIFIER.between(OP(LT), OP(GT)).optional(null), //1
                KEYWORD(DEF), //2
                IDENTIFIER, //3
                expression(true)//4


                                  ).token().map(new PureDefinitionOperator(this, true))

        );
    }

    private Parser<Function<? super var, ? extends var>> definitionOperator(@NotNull Parser.Reference<var> ref,
                                                                            boolean pure) throws Exception {

        return or(
                array(
                        KEYWORD(EXPORT).optional(null),//0
                        KEYWORD(CONST).optional(null), //1
                        IDENTIFIER.between(OP(LT), OP(GT)).optional(null),//2
                        (OP(DOLLAR).next(ref.lazy().between(OP(LEFT_PAREN), OP(RIGHT_PAREN))).or(IDENTIFIER))
                                .token().map((Token token) -> {
                            ((var) token.value()).meta(CONSTRAINT_SOURCE, new SourceSegmentValue(currentScope(), token));
                            return token.value();
                        }), //3
                        OP(DEFINITION) //4

                ).token().map(new DefinitionOperator(pure, this, false)),
                array(
                        KEYWORD(EXPORT).optional(null), //0
                        IDENTIFIER.between(OP(LT), OP(GT)).optional(null), //1
                        KEYWORD(DEF), //2
                        IDENTIFIER //3

                ).token().map(new DefinitionOperator(pure, this, true))

        );
    }


    private <T> Parser<T> op(@NotNull OpDef def, @NotNull T value) {
        return OP(def).token().map(new SourceMapper<>(value));

    }

    private static class BacktickScriptMap implements TokenMap<String> {
        @Nullable
        @Override
        public String map(@NotNull Token token) {
            final Object val = token.value();
            if (val instanceof Tokens.Fragment) {
                Tokens.Fragment c = (Tokens.Fragment) val;
                if (!"backtick".equals(c.tag())) {
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
            return "backtick";
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
