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
import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.URIAware;
import me.neilellis.dollar.script.exceptions.VariableNotFoundException;
import me.neilellis.dollar.script.java.JavaScriptingSupport;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.*;
import org.codehaus.jparsec.functors.Map;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;
import time.Scheduler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static me.neilellis.dollar.DollarStatic.$;
import static me.neilellis.dollar.DollarStatic.$void;
import static me.neilellis.dollar.script.Lexical.term;
import static me.neilellis.dollar.types.DollarFactory.fromLambda;
import static org.codehaus.jparsec.Parsers.array;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarParser {


    //Lexer

    public static final int MEMBER_PRIORITY = 500;
    public static final int ASSIGNMENT_PRIORITY = 2;
    public static final int UNARY_PRIORITY = 400;
    public static final int INC_DEC_PRIORITY = 400;
    public static final int LINE_PREFIX_PRIORITY = 0;
    public static final int PIPE_PRIORITY = 150;
    public static final int EQUIVALENCE_PRIORITY = 100;
    public static final int PLUS_MINUS_PRIORITY = 200;
    public static final int OUTPUT_PRIORITY = 50;
    public static final int CAST_PRIORITY = 80;
    public static final int CONTROL_FLOW_PRIORITY = 50;
    public static final int MULTIPLY_DIVIDE_PRIORITY = 300;
    public static final String NAMED_PARAMETER_META_ATTR = "__named_parameter";
    public static final int LOGICAL_AND_PRIORITY = 70;
    public static final int LOGICAL_OR_PRIORITY = 60;
    static final Terminals OPERATORS = Terminals.operators("|", ">>", "<<", "->", "=>", "<=", "<-", "(", ")", "--", "++", ".", ":", "<", ">", "?", "?:", "!", "!!", ">&", "{", "}", ",", "$", "=", ";", "[", "]", "??", "!!", "*>", "==", "!=", "+", "-", "\n", "$(", "${", ":=", "&", "&=", "<>", "+>", "<+", "*>", "<*", "*|", "|*", "*|*", "|>", "<|", "&>", "<&", "?>", "<?", "?->", "<=>", "<$", "$>", "-_-", "::", "/", "%", "*", "&&", "||", "<--", "<++", "\u2357");
    static final Terminals KEYWORDS = Terminals.operators("out", "err", "debug", "fix", "causes", "when", "if", "then", "for", "each", "fail", "assert", "switch", "choose", "not", "dollar", "fork", "join", "print", "default", "debug", "error", "filter", "sleep", "secs", "sec", "minute", "minutes", "hr", "hrs", "milli", "millis", "every", "until", "unless", "uri", "and", "or", "dispatch", "send", "give", "receive", "peek", "poll", "push", "pop", "publish", "subscribe", "emit", "drain", "receive all", "import");
    static final Parser<?> TOKENIZER =
            Parsers.or(Lexical.url(), OPERATORS.tokenizer(), Lexical.decimal(), Lexical.java(), Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER, Terminals.StringLiteral.SINGLE_QUOTE_TOKENIZER, Terminals.IntegerLiteral.TOKENIZER, Parsers.longest(KEYWORDS.tokenizer(), Terminals.Identifier.TOKENIZER));


    //Grammar
    static final Parser<var> IDENTIFIER = identifier();
    static final Parser<var> STRING_LITERAL = Terminals.StringLiteral.PARSER.map(DollarStatic::$);
    static final Parser<var> DECIMAL_LITERAL = Terminals.DecimalLiteral.PARSER.map(s ->
                    s.contains(".") ? $(Double.parseDouble(s)) : $(Long.parseLong(s))
    );
    static final Parser<var> INTEGER_LITERAL = Terminals.IntegerLiteral.PARSER.map(s -> $(Long.parseLong(s)));
    static final Parser<var> URL = Parsers.token(new TokenMap<String>() {
        @Override
        public String map(Token token) {
            final Object val = token.value();
            if (val instanceof Tokens.Fragment) {
                Tokens.Fragment c = (Tokens.Fragment) val;
                if (!c.tag().equals("uri")) {
                    return null;
                }
                return c.text();
            } else return null;
        }

        @Override
        public String toString() {
            return "URI";
        }
    }).map(DollarFactory::fromURI);
    private final ClassLoader classLoader;
    private ThreadLocal<List<ScriptScope>> scopes = new ThreadLocal<List<ScriptScope>>() {
        @Override
        protected List<ScriptScope> initialValue() {
            ArrayList<ScriptScope> list = new ArrayList<>();
            list.add(new ScriptScope("ThreadTopLevel"));
            return list;
        }
    };
    private Parser<?> topLevelParser;
    private ParserErrorHandler errorHandler = new ParserErrorHandler();

    public DollarParser() {
        classLoader = DollarParser.class.getClassLoader();
    }

    public DollarParser(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    private static Parser<var> identifier() {
        return Terminals.Identifier.PARSER.map(i -> {
            switch (i) {
                case "true":
                    return $(true);
                case "false":
                    return $(false);
                case "yes":
                    return $(true);
                case "no":
                    return $(false);
                case "void":
                    return $void();
                default:
                    return $(i);
            }
        });
    }

    private static Parser<var> parameters(Parser<var> expression) {
        Parser<List<var>> sequence = term("(").next(array(IDENTIFIER.followedBy(term(":")).optional(), expression).map(new Map<Object[], var>() {
            @Override
            public var map(Object[] objects) {
                if (objects[0] != null) {
                    var result = (var) objects[1];
                    result.setMetaAttribute(NAMED_PARAMETER_META_ATTR, objects[0].toString());
                    return result;
                }
                return (var) objects[1];
            }
        }).sepBy(Lexical.COMMA_TERMINATOR)).followedBy(term(")"));
        return sequence.map(DollarStatic::$);
    }

    final Parser<var> java(ScriptScope scope) {
        return Parsers.token(new TokenMap<String>() {
            @Override
            public String map(Token token) {
                final Object val = token.value();
                if (val instanceof Tokens.Fragment) {
                    Tokens.Fragment c = (Tokens.Fragment) val;
                    if (!c.tag().equals("java")) {
                        return null;
                    }
                    return c.text();
                } else return null;
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
            parser = term(name);
        } else {
            parser = term(name, keyword);

        }
        return parser.token().map(new Map<Token, T>() {
            @Override
            public T map(Token token) {
                if (value instanceof Operator) {
                    ((Operator) value).setSource(() -> {
                        int index = token.index();
                        int length = token.length();
                        String theSource = currentScope().getSource();
                        int end = theSource.indexOf('\n', index + length);
                        int start = index > 10 ? index - 10 : 0;
                        String highlightedSource = "... " + theSource.substring(start, index) + " \u261E " + theSource.substring(index, index + length) + " \u261C " + theSource.substring(index + length, end) + " ...";
                        return highlightedSource.replaceAll("\n", "\\\\n");
                    });
                }
                return value;
            }
        });

    }


//    private Parser<var> arrayElementExpression(Parser<var> expression1, Parser<var> expression2, ScriptScope scope) {
//        return expression1.infixl(term("[").next(expression2).followedBy(term("]")));
//    }


    private Parser<var> ifStatement(Parser<var> expression, ScriptScope scope) {
        Parser<Object[]> sequence = Lexical.keywordFollowedByNewlines("if").next(array(expression, expression));
        return sequence.map(objects -> {
            var lhs = ((var) objects[0]);
            var rhs = (var) objects[1];
            var lambda = DollarFactory.fromLambda(i -> lhs.isTrue() ? $((Object) rhs.$()) : $void());
            return lambda;
        });
    }

    private Parser<var> whenStatement(Parser<var> expression, ScriptScope scope) {
        Parser<Object[]> sequence = Lexical.keywordFollowedByNewlines("when").next(array(expression, expression));
        return sequence.map(objects -> {
            var lhs = (var) objects[0];
            var rhs = (var) objects[1];
            var lambda = DollarFactory.fromLambda(i -> lhs.isTrue() ? $((Object) rhs.$()) : $void());
            lhs.$listen(i -> {
                lambda.$notify(i);
                if (i.isTrue()) {
                    return $((Object) rhs.$());
                }
                return $void();
            });
            return lambda;
        });
    }


    private Parser<var> eachStatement(Parser<var> expression, ScriptScope scope) {
        Parser<Object[]> sequence = Lexical.keywordFollowedByNewlines("each").next(array(expression, expression));
        return sequence.map(objects -> {
            var lhs = (var) objects[0];
            var lambda = DollarFactory.fromLambda(i -> lhs.$each(j -> {
                scope.set("1", j);
                return $((Object) ((var) objects[1]).$());
            }));
            lhs.$listen(lambda::$notify);
            return lambda;
        });
    }


    private Parser<var> everyStatement(Parser<var> expression, ScriptScope scope) {
        Parser<Object[]> sequence = Lexical.keywordFollowedByNewlines("every").next(array(expression, Lexical.keyword("secs", "sec", "minute", "minutes", "hr", "hrs", "milli", "millis"), Lexical.keyword("until").next(expression).optional(), Lexical.keyword("unless").next(expression).optional(), expression));
        return sequence.map(objects -> {
            final int[] count = new int[]{-1};
            Scheduler.schedule(i -> {
                count[0]++; // William Gibson
//                System.out.println("COUNT "+count[0]);
                return withinNewScope(scope, newScope -> {
//                    System.err.println(newScope);
                    newScope.set("1", $(count[0]));
                    if (objects[2] instanceof var && ((var) objects[2]).isTrue()) {
                        Scheduler.cancel(i.$S());
                        return i;
                    } else if (objects[3] instanceof var && ((var) objects[3]).isTrue()) {
                        return $void();
                    } else {
                        return $((Object) ((var) objects[4]).$());
                    }
                });
            }, ((var) objects[0]).L(), objects[1].toString());
            return $void();
        });
    }


    private Parser<var> list(Parser<var> expression, ScriptScope scope) {
        Parser<List<var>> sequence = Lexical.termFollowedByNewlines("[").next(expression.sepBy(Lexical.COMMA_OR_NEWLINE_TERMINATOR)).followedBy(Lexical.termPreceededByNewlines("]"));
        return sequence.map(i -> withinNewScope(scope, newScope -> DollarStatic.$(i)));
    }

    private Parser<var> map(Parser<var> expression, ScriptScope scope) {
        Parser<List<var>> sequence = Lexical.termFollowedByNewlines("{").next(expression.sepBy1(Lexical.COMMA_TERMINATOR)).followedBy(Lexical.termPreceededByNewlines("}"));
        return sequence.map(o -> withinNewScope(scope, newScope -> {
            var current = null;
            for (var v : o) {
                if (current != null) {
                    current = current.$plus(v);
                } else {
                    current = v;
                }
            }
            return current;
        }));
    }

    private Parser<var> block(Parser<var> parentParser, ScriptScope scope) {
        Parser.Reference<var> ref = Parser.newReference();

        Parser<List<var>> listParser = (Parsers.or(parentParser, ref.lazy().between(Lexical.termFollowedByNewlines("{"), Lexical.termPreceededByNewlines("}"))).followedBy(Lexical.SEMICOLON_TERMINATOR)).many1();

        //Now we do the complex part, the following will only return the last value in the
        //block when the block is evaluated, but it will trigger execution of the rest.
        //This gives it functionality like a conventional function in imperative languages
        Parser<var> or = listParser.map(l -> fromLambda(delayed -> withinNewScope(scope, newScope -> {
                    if (l.size() > 0) {
                        for (int i = 0; i < l.size() - 1; i++) {
                            l.get(i).$S();
                        }

                        return l.get(l.size() - 1);
//                        return $(l);
                    } else {
                        return $void();
                    }
                })
        ));
        ref.set(or);
        return or;
    }

    private Parser<List<var>> script(ScriptScope scope) {
        return withinNewScope(scope, newScope -> {
                    Parser.Reference<var> ref = Parser.newReference();
                    Parser<var> block = block(ref.lazy(), newScope).between(Lexical.termFollowedByNewlines("{"), Lexical.termPreceededByNewlines("}"));
                    Parser<var> expression = expression(newScope).map(i -> i != null ? i : null);
                    Parser<List<var>> parser = (Lexical.TERMINATOR_SYMBOL.optional()).next(Parsers.or(expression, block).followedBy(Lexical.TERMINATOR_SYMBOL).map(i -> $((Object) i.$())).many1());
                    ref.set(parser.map(DollarStatic::$));
                    return parser;

                }

        );
    }

    private Parser<var> expression(ScriptScope scope) {

        Parser.Reference<var> ref = Parser.newReference();
        Parser<var> main = ref.lazy().between(term("("), term(")")).or(Parsers.or(ifStatement(ref.lazy(), scope), list(ref.lazy(), scope), map(ref.lazy(), scope), eachStatement(ref.lazy(), scope), whenStatement(ref.lazy(), scope), everyStatement(ref.lazy(), scope), java(scope), URL, DECIMAL_LITERAL, INTEGER_LITERAL, STRING_LITERAL, IDENTIFIER)).or(block(ref.lazy(), scope).between(Lexical.termFollowedByNewlines("{"), Lexical.termPreceededByNewlines("}")));

        Parser<var> unit = main.withSource().map(varWithSource -> {
            var value = varWithSource.getValue();
            value.setMetaAttribute("source", varWithSource.getSource());
            return value;
        });


//        ScopedVarBinaryOperator value = new ScopedVarBinaryOperator((lhs, rhs) -> rhs, scope);
        Parser<var> parser = new OperatorTable<var>()
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(!lhs.equals(rhs)), scope), "!="), EQUIVALENCE_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(lhs.equals(rhs)),
                        scope), "=="), EQUIVALENCE_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(lhs.isTrue() && rhs.isTrue()),
                        scope), "&&"), LOGICAL_AND_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(lhs.isTrue() || rhs.isTrue()),
                        scope), "||"), LOGICAL_OR_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> withinNewScope(scope, newScope -> {
                    newScope.setImmediate("1", lhs);
                    Object rhsVal = rhs.$();
                    if ((rhsVal instanceof String)) {
                        String rhsStr = rhsVal.toString();
                        if (Builtins.exists(rhsStr)) {
                            return Builtins.execute(rhsStr, Collections.singletonList(lhs), newScope);
                        } else {
                            throw new VariableNotFoundException(rhsStr);
                        }
                    } else {
                        return $((Object) rhsVal);
                    }
                }),
                        scope), "|"), PIPE_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$multiply(rhs), scope), "*"), MULTIPLY_DIVIDE_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$divide(rhs), scope), "/"), MULTIPLY_DIVIDE_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$modulus(rhs), scope), "%"), MULTIPLY_DIVIDE_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$plus(rhs), scope), "+"), PLUS_MINUS_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$minus(rhs), scope), "-"), PLUS_MINUS_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(lhs.$S(), rhs), scope), ":"), 30)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.isTrue() ? rhs : lhs, scope), "?"), CONTROL_FLOW_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(false, i -> {
                    i.out();
                    return $void();
                }), ">>", "out"), LINE_PREFIX_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(false, i -> {
                    i.out();
                    return $void();/*reserved for debug instead of stdout messages*/
                }), "!!", "debug"), LINE_PREFIX_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(false, i -> {
                    i.err();
                    return $void();
                }), "??", "err"), LINE_PREFIX_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(false, v -> {
                    if (v.isTrue()) {
                        return v;
                    } else {
                        throw new AssertionError();
                    }
                }), "=>", "assert"), LINE_PREFIX_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> {
                    if (lhs.equals(rhs)) {
                        return lhs;
                    } else {
                        throw new AssertionError(lhs.$S() + " != " + rhs.$S());
                    }
                }, scope), "<=>"), LINE_PREFIX_PRIORITY)

                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> rhs.$dispatch(lhs), scope), "?>", "dispatch"), OUTPUT_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, URIAware::$peek), "<?", "peek"), OUTPUT_PRIORITY)

                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> rhs.$give(lhs), scope), "&>", "give"), OUTPUT_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, URIAware::$poll), "<&", "poll"), OUTPUT_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, URIAware::$drain), "<--", "drain"), OUTPUT_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, URIAware::$all), "<++", "receive all"), OUTPUT_PRIORITY)

                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> rhs.$push(lhs), scope), "->", "push"), OUTPUT_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, URIAware::$pop), "<-", "pop"), OUTPUT_PRIORITY)

                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> rhs.$publish(lhs), scope), "*>", "publish"), OUTPUT_PRIORITY)
                .infixl(op(new SubscribeOperator(scope), "<*", "subscribe"), OUTPUT_PRIORITY)

                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> rhs.$send(lhs), scope), "+>", "send"), OUTPUT_PRIORITY)
                .prefix(op(new ReceiveOperator(scope), "<+", "receive"), OUTPUT_PRIORITY)

                .infixl(op(new ListenOperator(scope), "<$", "causes"), CONTROL_FLOW_PRIORITY)

                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> rhs.$notify(lhs), scope), "$>", "emit"), OUTPUT_PRIORITY)


//                .infixl(op("*>", new ScopedVarBinaryOperator((lhs, rhs) -> {
//                    lhs.$publish(rhs.$S());
//                    return lhs;
//                }, scope), scope), 50)

                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$choose(rhs), scope), "?->", "choose"), CONTROL_FLOW_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$default(rhs), scope), "?:", "default"), CONTROL_FLOW_PRIORITY)
                .postfix(term(".").next(ref.lazy().between(term("("), term(")")).or(IDENTIFIER)).map(rhs -> lhs -> {
                    return linkBinaryInToOut(DollarFactory.fromLambda(i -> lhs.$(rhs.$S())), lhs, rhs);
                }), MEMBER_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, v -> $(!v.isTrue())), "!", "not"), UNARY_PRIORITY)
                .postfix(op(new ScopedVarUnaryOperator(scope, var::$dec), "--"), INC_DEC_PRIORITY)
                .postfix(op(new ScopedVarUnaryOperator(scope, var::$inc), "++"), INC_DEC_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, var::$negate), "-"), UNARY_PRIORITY)
                .prefix(importOperator(scope), UNARY_PRIORITY)
                .postfix(subscriptOperator(ref), MEMBER_PRIORITY)
                .postfix(parameterOperator(ref, scope), MEMBER_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(true, v -> $((Object) v.$())), "<>", "fix"), 1000)
                .prefix(op(new VariableOperator(scope), "$"), 1000)
                .prefix(op(new ScopedVarUnaryOperator(false, v -> DollarFactory.fromURI(v.$S())), "::", "uri"), CAST_PRIORITY)
                .prefix(op(new SleepOperator(scope), "-_-", "sleep"), ASSIGNMENT_PRIORITY)
                .infixr(op(new AssignmentOperator(scope), "="), ASSIGNMENT_PRIORITY)
                .infixl(op(new DeclarationOperator(scope), ":="), ASSIGNMENT_PRIORITY)
                .build(unit);
        ref.set(parser);

        scope.setParser(parser);

        return parser;
    }

    private Parser<Map<? super var, ? extends var>> subscriptOperator(Parser.Reference<var> ref) {
        return term("[").next(
                array(ref.lazy().followedBy(term("]")),
                        term("=").next(ref.lazy())
                                .optional()
                )
        )
                .map(rhs -> lhs -> {
                    if (rhs[1] != null) {
                        var lambda = DollarFactory.fromLambda(i -> lhs.$((var) rhs[0], rhs[1]));
                        return linkBinaryInToOut(lambda, lhs, (var) rhs[0], (var) rhs[1]);
                    } else {
                        var lambda = DollarFactory.fromLambda(i -> lhs.$(((var) rhs[0])));
                        return linkBinaryInToOut(lambda, lhs, (var) rhs[0]);
                    }
                });
    }


    private Parser<ScopedVarUnaryOperator> importOperator(ScriptScope scope) {
        return op(new ScopedVarUnaryOperator(scope, i -> {
            String importName = i.$S();
            String[] parts = importName.split(":", 2);
            if (parts.length < 2) {
                throw new IllegalArgumentException("Import " + importName + " needs to have a scheme");
            }
            try {
                return DollarFactory.fromLambda(in -> PipeableResolver.resolveModule(parts[0]).resolve(parts[1], scope.getDollarParser().currentScope()).pipe(in));
            } catch (Exception e) {
                return DollarStatic.logAndRethrow(e);
            }

        }), "\u2357", "import");
    }

    private Parser<Map<? super var, ? extends var>> parameterOperator(Parser.Reference<var> ref, ScriptScope scope) {
        return term("(").next(array(IDENTIFIER.followedBy(term(":")).optional(), ref.lazy()).map(objects -> {
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
        }).sepBy(Lexical.COMMA_TERMINATOR)).followedBy(term(")")).map(rhs -> lhs -> {

            var lambda = DollarFactory.fromLambda(i -> withinNewScope(scope, newScope -> {
                //Add the special $* value for all the parameters
                newScope.set("*", $(rhs));
                int count = 0;
                for (var param : rhs) {
                    newScope.set(String.valueOf(++count), param);
                    //If the parameter is a named parameter then use the name (set as metadata
                    //on the value).
                    if (param.getMetaAttribute(NAMED_PARAMETER_META_ATTR) != null) {
                        newScope.set(param.getMetaAttribute(NAMED_PARAMETER_META_ATTR), param);
                    }
                }
                var result;
                if (lhs.isLambda()) {
                    result = $((Object) lhs.$());
                } else {
                    String lhsString = lhs.toString();
                    //The lhs is a string, so let's see if it's a builtin function
                    //if not then assume it's a variable.
                    if (Builtins.exists(lhsString)) {
                        result = Builtins.execute(lhsString, rhs, newScope);
                    } else {
                        result = $((Object) newScope.get(lhsString).$());
                    }
                }

                return result;
            }));
            //reactive links
            lhs.$listen(i -> lambda.$notify($((Object) lambda.$())));
            for (var param : rhs) {
                param.$listen(i -> lambda.$notify($((Object) lambda.$())));
            }
            return lambda;
        });
    }

    private var linkBinaryInToOut(var lambda, var... values) {
        for (var value : values) {
            value.$listen(i -> lambda.$notify(lambda.$()));
        }
        return lambda;
    }

    public void addScope(ScriptScope scope) {
        scopes.get().add(scope);
    }

    public <T> T withinNewScope(ScriptScope currentScope, Function<ScriptScope, T> r) {
        ScriptScope newScope = new ScriptScope(currentScope, currentScope.getSource());
        addScope(newScope);
        try {
            return r.apply(newScope);
        } finally {
            ScriptScope poppedScope = endScope();
            if (poppedScope != newScope) {
                throw new IllegalStateException("Popped wrong scope");
            }
        }
    }

    public ScriptScope endScope() {
        return scopes.get().remove(scopes.get().size() - 1);
    }

    public ScriptScope currentScope() {
        return scopes.get().get(scopes.get().size() - 1);
    }

    public var parse(File file) throws IOException {
        if (file.getName().endsWith(".md") || file.getName().endsWith(".markdown")) {
            return parseMarkdown(file);
        } else {
            String source = new String(Files.readAllBytes(file.toPath()));
            return parse(new ScriptScope(this, source), source);
        }

    }

    public var parseMarkdown(File file) throws IOException {
        PegDownProcessor pegDownProcessor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS);
        RootNode rootNode = pegDownProcessor.parseMarkdown(com.google.common.io.Files.toString(file, Charset.forName("utf-8")).toCharArray());
        rootNode.accept(new CodeExtractionVisitor());
        return $();
    }

    public var parseMarkdown(String source) throws IOException {
        PegDownProcessor pegDownProcessor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS);
        RootNode rootNode = pegDownProcessor.parseMarkdown(source.toCharArray());
        rootNode.accept(new CodeExtractionVisitor());
        return $();
    }

    public var parse(ScriptScope scope, File file) throws IOException {
        String source = new String(Files.readAllBytes(file.toPath()));
        return parse(new ScriptScope(scope, source), source);
    }

    public var parse(ScriptScope scope, InputStream in) throws IOException {
        String source = new String(ByteStreams.toByteArray(in));
        return parse(new ScriptScope(scope, source), source);
    }

    public var parse(InputStream in) throws IOException {
        String source = new String(ByteStreams.toByteArray(in));
        return parse(new ScriptScope(this, source), source);
    }

    public var parse(String s) throws IOException {
        return parse(new ScriptScope(this, s), s);
    }

    public var parse(ScriptScope scope, String source) throws IOException {
        addScope(new ScriptScope(scope, source));
        try {
            DollarStatic.context().setClassLoader(classLoader);
            scope.setDollarParser(this);
            Parser<?> parser = buildParser(new ScriptScope(scope, source));
            List<var> parse = (List<var>) parser.from(TOKENIZER, Lexical.IGNORED).parse(source);
            return $(parse.stream().map(i -> i.$()).collect(Collectors.toList()));
        } finally {
            endScope();
        }
    }


    public var parseExpression(ScriptScope scope, String source) throws IOException {
        DollarStatic.context().setClassLoader(classLoader);
        scope.setDollarParser(this);
        addScope(new ScriptScope(scope, source));
        try {
            topLevelParser = expression(scope);

            return (var) topLevelParser.from(TOKENIZER, Lexical.IGNORED).parse(source);
        } finally {
            endScope();
        }
    }

    private Parser<?> buildParser(ScriptScope scope) {
        topLevelParser = script(scope);
        return topLevelParser;
    }


    public List<ScriptScope> scopes() {
        return scopes.get();
    }

    public ParserErrorHandler getErrorHandler() {
        return errorHandler;
    }
}
