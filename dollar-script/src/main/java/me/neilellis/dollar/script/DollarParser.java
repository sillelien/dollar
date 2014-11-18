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
import me.neilellis.dollar.script.exceptions.DollarScriptException;
import me.neilellis.dollar.script.exceptions.VariableNotFoundException;
import me.neilellis.dollar.script.java.JavaScriptingSupport;
import me.neilellis.dollar.types.DollarFactory;
import org.codehaus.jparsec.*;
import org.codehaus.jparsec.error.ParserException;
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

import static me.neilellis.dollar.DollarStatic.*;
import static me.neilellis.dollar.script.Lexical.keyword;
import static me.neilellis.dollar.script.Lexical.term;
import static me.neilellis.dollar.types.DollarFactory.fromLambda;
import static org.codehaus.jparsec.Parsers.array;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarParser {


    //Lexer

    public static final int MEMBER_PRIORITY = 500;
    public static final int ASSIGNMENT_PRIORITY = 10;
    public static final int UNARY_PRIORITY = 400;
    public static final int INC_DEC_PRIORITY = 400;
    public static final int IN_PRIORITY = 400;
    public static final int LINE_PREFIX_PRIORITY = 0;
    public static final int PIPE_PRIORITY = 150;
    public static final int EQUIVALENCE_PRIORITY = 100;
    public static final int PLUS_MINUS_PRIORITY = 200;
    public static final int OUTPUT_PRIORITY = 50;
    public static final int IF_PRIORITY = 20;
    public static final int CAST_PRIORITY = 80;
    public static final int CONTROL_FLOW_PRIORITY = 50;
    public static final int MULTIPLY_DIVIDE_PRIORITY = 300;
    public static final int RANGE_PRIORITY = 600;
    public static final String NAMED_PARAMETER_META_ATTR = "__named_parameter";
    public static final int LOGICAL_AND_PRIORITY = 70;
    public static final int LOGICAL_OR_PRIORITY = 60;
    static final Terminals OPERATORS = Terminals.operators("|", ">>", "<<", "->", "=>", ".:", "<=", ">=", "<-", "(", ")", "--", "++", ".", ":", "<", ">", "?", "?:", "!", "!!", ">&", "{", "}", ",", "$", "=", ";", "[", "]", "??", "!!", "*>", "==", "!=", "+", "-", "\n", "${", ":=", "&", "&=", "@", "+>", "<+", "*>", "<*", "*|", "|*", "*|*", "|>", "<|", "&>", "<&", "?>", "<?", "?->", "<=>", "<$", "$>", "-_-", "::", "/", "%", "*", "&&", "||", "<--", "<++", "\u2357", "~", "?$?", "???", "..", "?..?", "€", "@@", "<@", "@>", "#");
    static final Terminals KEYWORDS = Terminals.operators("out", "err", "debug", "fix", "causes", "when", "if", "then", "for", "map", "fail", "assert", "switch", "choose", "not", "dollar", "fork", "join", "print", "default", "debug", "error", "filter", "sleep", "secs", "sec", "minute", "minutes", "hr", "hrs", "milli", "millis", "every", "until", "unless", "uri", "and", "or", "dispatch", "send", "give", "receive", "peek", "poll", "push", "pop", "publish", "subscribe", "emit", "drain", "all", "import", "reduce", "truthy", "is", "else", "const", "in", "true", "false", "yes", "no", "void", "error", "to", "from", "async", "stateless", "size");
    static final Parser<?> TOKENIZER =
            Parsers.or(Lexical.url(), OPERATORS.tokenizer(), Lexical.decimal(), Lexical.java(), Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER, Terminals.StringLiteral.SINGLE_QUOTE_TOKENIZER, Terminals.IntegerLiteral.TOKENIZER, Parsers.longest(KEYWORDS.tokenizer(), Terminals.Identifier.TOKENIZER));


    //Grammar
    static final Parser<var> IDENTIFIER = identifier();
    static final Parser<var> IDENTIFIER_KEYWORD = identifierKeyword();
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

    static final Parser<var> dollarIdentifier(ScriptScope scope) {
        return term("$").next(Terminals.Identifier.PARSER).map(i -> {
            return DollarFactory.fromLambda(j -> getVariable(scope, i, false));
        });
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

    private static Parser<var> identifierKeyword() {
        return Parsers.or(keyword("true"), keyword("false"), keyword("yes"), keyword("no"), keyword("void")).map(new Map<Object, var>() {
            @Override
            public var map(Object i) {
                switch (i.toString()) {
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
            }
        });
    }


    static var getVariable(ScriptScope scope, String key, boolean numeric) {

        var lambda = DollarFactory.fromLambda(v -> {
            try {
                List<ScriptScope> scopes = new ArrayList<ScriptScope>(scope.getDollarParser().scopes());
                Collections.reverse(scopes);
                for (ScriptScope scriptScope : scopes) {
                    if (numeric) {
                        if (scriptScope.hasParameter(key)) {
                            return scriptScope.getParameter(key);
                        }
                    } else {
                        if (scriptScope.has(key)) {
                            return scriptScope.get(key);
                        }
                    }
                }
            } catch (AssertionError e) {
                return scope.getDollarParser().getErrorHandler().handle(scope, key, e);
            } catch (DollarScriptException e) {
                return scope.getDollarParser().getErrorHandler().handle(scope, key, e);
            } catch (Exception e) {
                return scope.getDollarParser().getErrorHandler().handle(scope, key, e);
            }
            if (numeric) {
                return scope.getParameter(key);
            }
            if (!scope.has(key)) {
                throw new VariableNotFoundException(key);
            }
            return scope.get(key);
        });
        scope.listen(key, lambda);
        lambda.setMetaAttribute("variable", key);
        return lambda;


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


    private Parser<var> everyStatement(Parser<var> expression, ScriptScope scope) {
        Parser<Object[]> sequence = Lexical.keywordFollowedByNewlines("every").next(array(expression, Lexical.keyword("secs", "sec", "minute", "minutes", "hr", "hrs", "milli", "millis"), Lexical.keyword("until").next(expression).optional(), Lexical.keyword("unless").next(expression).optional(), expression));
        return sequence.map(objects -> {
            final int[] count = new int[]{-1};
            Scheduler.schedule(i -> {
                count[0]++; // William Gibson
//                System.out.println("COUNT "+count[0]);
                return withinNewScope(scope, newScope -> {
//                    System.err.println(newScope);
                    newScope.setParameter("1", $(count[0]));
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
        Parser<var> main = ref.lazy().between(term("("), term(")")).or(Parsers.or(list(ref.lazy(), scope), map(ref.lazy(), scope), whenStatement(ref.lazy(), scope), everyStatement(ref.lazy(), scope), java(scope), URL, DECIMAL_LITERAL, INTEGER_LITERAL, STRING_LITERAL, dollarIdentifier(scope), IDENTIFIER_KEYWORD, functionCall(ref, scope), IDENTIFIER.map(new Map<var, var>() {
            @Override
            public var map(var var) {
                return DollarFactory.fromLambda(i -> getVariable(scope, var.toString(), false));
            }
        }))).or(block(ref.lazy(), scope).between(Lexical.termFollowedByNewlines("{"), Lexical.termPreceededByNewlines("}")));

        Parser<var> unit = main;


//        ScopedVarBinaryOperator value = new ScopedVarBinaryOperator((lhs, rhs) -> rhs, scope);

        Parser<var> parser = new OperatorTable<var>()
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(!lhs.equals(rhs)), scope), "!="), EQUIVALENCE_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(lhs.equals(rhs)),
                        scope), "=="), EQUIVALENCE_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(lhs.isTrue() && rhs.isTrue()),
                        scope), "&&"), LOGICAL_AND_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(lhs.isTrue() || rhs.isTrue()),
                        scope), "||"), LOGICAL_OR_PRIORITY)
                .postfix(pipeOperator(ref, scope), PIPE_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> {
                    return DollarFactory.fromValue(Range.closed(lhs, rhs));
                }, scope), ".."), RANGE_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(lhs.compareTo(rhs) < 0), scope), "<"), EQUIVALENCE_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(lhs.compareTo(rhs) > 0), scope), ">"), EQUIVALENCE_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(lhs.compareTo(rhs) <= 0), scope), "<="), EQUIVALENCE_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(lhs.compareTo(rhs) >= 0), scope), ">="), EQUIVALENCE_PRIORITY)
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
                }), "@@", "print"), LINE_PREFIX_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(false, i -> {
                    i.debug();
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
                }), ".:", "assert"), LINE_PREFIX_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> {
                    if (lhs.equals(rhs)) {
                        return lhs;
                    } else {
                        throw new AssertionError(lhs.$S() + " != " + rhs.$S());
                    }
                }, scope), "<=>"), LINE_PREFIX_PRIORITY)

                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> rhs.$dispatch(lhs), scope), "?>", "dispatch"), OUTPUT_PRIORITY)
                .prefix(sendOperator(ref, scope), OUTPUT_PRIORITY)
                .prefix(receiveOperator(ref, scope), OUTPUT_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, i -> $(i.isTruthy())), "~", "truthy"), UNARY_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, i -> i.$size()), "#", "size"), UNARY_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> rhs.$give(lhs), scope), "&>", "give"), OUTPUT_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, URIAware::$poll), "<&"), OUTPUT_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, URIAware::$drain), "<--", "drain"), OUTPUT_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, URIAware::$all), "<@", "all"), OUTPUT_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> {
                    if (lhs.isBoolean() && lhs.isFalse()) {
                        return rhs;
                    } else {
                        return lhs;
                    }
                }, scope), "???", "else"), IF_PRIORITY)
                .prefix(ifOperator(ref), IF_PRIORITY)

                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> rhs.$contains(lhs), scope), "?..?", "in"), IN_PRIORITY)

                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> rhs.$push(lhs), scope), "+>"), OUTPUT_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, URIAware::$pop), "<+"), OUTPUT_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, rhs -> scope.addErrorHandler(rhs)), "€", "error"), LINE_PREFIX_PRIORITY)

                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> rhs.$publish(lhs), scope), "*>", "publish"), OUTPUT_PRIORITY)
                .infixl(op(new SubscribeOperator(scope), "<*", "subscribe"), OUTPUT_PRIORITY)

                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> rhs.$send(lhs), scope), ">>"), OUTPUT_PRIORITY)
                .postfix(isOperator(ref, scope), EQUIVALENCE_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$each(i -> withinNewScope(scope, newScope -> {
                    newScope.setParameter("1", i);
                    return fix(rhs);
                })), scope), "*|*", "map"), MULTIPLY_DIVIDE_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> {
                    return lhs.toList().stream().reduce((x, y) -> {
                        return withinNewScope(scope, newScope -> {
                            newScope.setParameter("1", x);
                            newScope.setParameter("2", y);
                            return fix(rhs);
                        });
                    }).get();
                }, scope), "*|", "reduce"), MULTIPLY_DIVIDE_PRIORITY)
                .prefix(op(new ReceiveOperator(scope), "<<"), OUTPUT_PRIORITY)

                .infixl(op(new ListenOperator(scope), "=>", "causes"), CONTROL_FLOW_PRIORITY)

                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$notify(rhs), scope), "<-", "emit"), OUTPUT_PRIORITY)


//                .infixl(op("*>", new ScopedVarBinaryOperator((lhs, rhs) -> {
//                    lhs.$publish(rhs.$S());
//                    return lhs;
//                }, scope), scope), 50)

                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$choose(rhs), scope), "?->", "choose"), CONTROL_FLOW_PRIORITY)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$default(rhs), scope), "?:", "default"), CONTROL_FLOW_PRIORITY)
                .postfix(term(".").followedBy(term(".").not()).next(ref.lazy().between(term("("), term(")")).or(IDENTIFIER)).map(rhs -> lhs -> {
                    return linkBinaryInToOut(DollarFactory.fromLambda(i -> lhs.$get(rhs.$S())), lhs, rhs);
                }), MEMBER_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, v -> $(!v.isTrue())), "!", "not"), UNARY_PRIORITY)
                .postfix(op(new ScopedVarUnaryOperator(scope, var::$dec), "--"), INC_DEC_PRIORITY)
                .postfix(op(new ScopedVarUnaryOperator(scope, var::$inc), "++"), INC_DEC_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(scope, var::$negate), "-"), UNARY_PRIORITY)
                .prefix(forOperator(scope, ref), UNARY_PRIORITY)
                .prefix(importOperator(scope), UNARY_PRIORITY)
                .postfix(subscriptOperator(ref), MEMBER_PRIORITY)
                .postfix(parameterOperator(ref, scope), MEMBER_PRIORITY)
                .prefix(op(new ScopedVarUnaryOperator(true, v -> $((Object) v.$())), "@", "fix"), 1000)
                .prefix(variableUsageOperator(ref, scope), 1000)
                .prefix(op(new ScopedVarUnaryOperator(false, v -> DollarFactory.fromURI(v.$S())), "::", "uri"), CAST_PRIORITY)
                .prefix(op(new SleepOperator(scope), "-_-", "sleep"), ASSIGNMENT_PRIORITY)
                .prefix(assignmentOperator(scope, ref), ASSIGNMENT_PRIORITY)
                .prefix(declarationOperator(scope, ref), ASSIGNMENT_PRIORITY)
                .build(unit);
        ref.set(parser);

        scope.setParser(parser);

        return parser;
    }

    private Parser<Map<var, var>> ifOperator(Parser.Reference<var> ref) {
        return Lexical.keywordFollowedByNewlines("if").next(ref.lazy()).map(lhs -> new Map<var, var>() {
            @Override
            public var map(var rhs) {
                final var lambda = DollarFactory.fromLambda(i -> {
                    if (lhs.isBoolean() && lhs.isTrue()) {
                        return rhs;
                    } else {
                        return $(false);
                    }
                });
                rhs.$listen(i -> lambda.$notify(lambda));
                return lambda;
            }
        });
    }

    private Parser<Map<? super var, ? extends var>> assignmentOperator(final ScriptScope scope, Parser.Reference<var> ref) {
        return Parsers.array(keyword("const").optional(), IDENTIFIER.between(term("<"), term(">")).optional(), ref.lazy().between(term("("), term(")")).optional(), term("$").next(ref.lazy().between(term("("), term(")"))).or(IDENTIFIER), term("=")).map(new Map<Object[], Map<? super var, ? extends var>>() {
            public Map<? super var, ? extends var> map(Object[] objects) {

                return new Map<var, var>() {
                    public var map(var rhs) {
                        var constraint;
                        if (objects[1] != null) {
                            constraint = DollarFactory.fromLambda(i -> {
                                return $(scope.getParameter("it").is(TypeAware.Type.valueOf(objects[1].toString().toUpperCase())) && (objects[2] == null || ((var) objects[2]).isTrue()));
                            });
                        } else {
                            constraint = (var) objects[2];

                        }
                        final var lambda = DollarFactory.fromLambda(i -> scope.set(objects[3].toString(), fix(rhs), (objects[0] != null), constraint));
                        return lambda;
                    }
                };
            }
        });
    }

    private Parser<Map<? super var, ? extends var>> declarationOperator(final ScriptScope scope, Parser.Reference<var> ref) {
        return Parsers.array(IDENTIFIER.between(term("<"), term(">")).optional(), ref.lazy().between(term("("), term(")")).optional(), term("$").next(ref.lazy().between(term("("), term(")"))).or(IDENTIFIER), term(":=")).map(new Map<Object[], Map<? super var, ? extends var>>() {
            public Map<? super var, ? extends var> map(Object[] objects) {

                return new Map<var, var>() {
                    public var map(var rhs) {
                        var constraint;
                        if (objects[0] != null) {
                            constraint = DollarFactory.fromLambda(i -> $(scope.getParameter("it").is(TypeAware.Type.valueOf(objects[0].toString().toUpperCase())) && (objects[1] == null || ((var) objects[1]).isTrue())));
                        } else {
                            constraint = (var) objects[1];

                        }
                        Pipeable action = i -> scope.set(objects[2].toString(), rhs, false, constraint);
                        try {
                            action.pipe($void());
                        } catch (Exception e) {
                            throw new DollarScriptException(e);
                        }
                        rhs.$listen(action);
                        return $void();
                    }
                };
            }
        });
    }


    private Parser<Map<? super var, ? extends var>> forOperator(final ScriptScope scope, Parser.Reference<var> ref) {
        return Parsers.array(keyword("for"), IDENTIFIER, keyword("in"), ref.lazy()).map(new Map<Object[], Map<? super var, ? extends var>>() {
            public Map<? super var, ? extends var> map(Object[] objects) {
                return rhs -> {
                    final var lambda = DollarFactory.fromLambda(l -> {
                        return withinNewScope(scope, newScope -> {
                            return ((var) objects[3]).$each(i -> {
                                newScope.set(objects[1].toString(), fix(i), false, null);
                                return fix(rhs);
                            });
                        });
                    });
                    return lambda;
                };
            }
        });
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


    private Parser<Map<? super var, ? extends var>> pipeOperator(Parser.Reference<var> ref, ScriptScope scope) {
        return term("|").next(
                IDENTIFIER.or(ref.lazy().between(term("("), term(")")))
        )
                .map(rhs -> lhs -> withinNewScope(scope, newScope -> {
                    var lhsFix = fix(lhs);
                    newScope.setParameter("1", lhsFix);
                    Object rhsVal = rhs.$();
                    if ((rhsVal instanceof String)) {
                        String rhsStr = rhsVal.toString();
                        if (Builtins.exists(rhsStr)) {
                            return Builtins.execute(rhsStr, Collections.singletonList(lhsFix), newScope);
                        } else if (scope.has(rhsStr)) {
                            return fix(scope.get(rhsVal.toString()));
                        } else {
                            throw new VariableNotFoundException(rhsStr);
                        }
                    } else {
                        return $(rhsVal);
                    }
                }));
    }


    private Parser<Map<? super var, ? extends var>> variableUsageOperator(Parser.Reference<var> ref, ScriptScope scope) {
        return Parsers.or(term("$").followedBy(term("(").peek())
                .map(lhs -> rhs -> {
                    return DollarFactory.fromLambda(i -> getVariable(scope, rhs.toString(), false));
                }), term("$").followedBy(INTEGER_LITERAL.peek())
                .map(lhs -> rhs -> {
                    return DollarFactory.fromLambda(i -> getVariable(scope, rhs.toString(), true));
                }));
    }


    private Parser<Map<? super var, ? extends var>> receiveOperator(Parser.Reference<var> ref, ScriptScope scope) {
        return Parsers.array(keyword("receive"), keyword("async").optional(), keyword("stateless").optional()).followedBy(keyword("from").optional()).map(new Map<Object[], Map<? super var, ? extends var>>() {
            @Override
            public Map<? super var, ? extends var> map(Object[] objects) {
                return new Map<var, var>() {
                    @Override
                    public var map(var rhs) {
                        return DollarFactory.fromLambda(i -> rhs.$receive(objects[1] == null, objects[2] == null));
                    }
                };
            }
        });
    }

    private Parser<Map<? super var, ? extends var>> sendOperator(Parser.Reference<var> ref, ScriptScope scope) {
        return Parsers.array(keyword("send"), ref.lazy(), keyword("async").optional(), keyword("stateless").optional()).followedBy(keyword("to").optional()).map(new Map<Object[], Map<? super var, ? extends var>>() {
            @Override
            public Map<? super var, ? extends var> map(Object[] objects) {
                return new Map<var, var>() {
                    @Override
                    public var map(var rhs) {
                        return DollarFactory.fromLambda(i -> rhs.$send((var) objects[1], objects[2] == null, objects[3] == null));
                    }
                };
            }
        });
    }


    private Parser<Map<? super var, ? extends var>> isOperator(Parser.Reference<var> ref, ScriptScope scope) {
        return keyword("is").next(IDENTIFIER.sepBy(term(",")))
                .map(rhs -> lhs -> DollarFactory.fromLambda(i -> {
                    for (var value : rhs) {
                        if (lhs.is(TypeAware.Type.valueOf(value.$S().toUpperCase()))) {
                            return $(true);
                        }
                    }
                    return $(false);
                }));
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

    private Parser<var> functionCall(Parser.Reference<var> ref, ScriptScope scope) {
        return Parsers.array(IDENTIFIER, parameterOperator(ref, scope)).map(new Map<Object[], var>() {
            @Override
            public var map(Object[] objects) {
                return ((Map<? super var, ? extends var>) objects[1]).map((var) objects[0]);
            }
        });
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
                newScope.setParameter("*", $(rhs));
                int count = 0;
                for (var param : rhs) {
                    newScope.setParameter(String.valueOf(++count), param);
                    //If the parameter is a named parameter then use the name (set as metadata
                    //on the value).
                    if (param.getMetaAttribute(NAMED_PARAMETER_META_ATTR) != null) {
                        newScope.set(param.getMetaAttribute(NAMED_PARAMETER_META_ATTR), param, true, null);
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
