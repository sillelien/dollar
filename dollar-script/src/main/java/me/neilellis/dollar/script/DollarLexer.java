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

import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.*;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.pattern.CharPredicates;
import org.codehaus.jparsec.pattern.Patterns;

import static me.neilellis.dollar.DollarStatic.$;
import static me.neilellis.dollar.DollarStatic.$void;
import static org.codehaus.jparsec.Parsers.or;
import static org.codehaus.jparsec.Parsers.token;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarLexer {
    static final Terminals
            KEYWORDS =
            Terminals.operators("out", "err", "debug", "fix", "causes", "when", "if", "then", "for", "each", "fail",
                                "assert", "switch", "choose", "not", "dollar", "fork", "join", "print", "default",
                                "debug", "error", "filter", "sleep", "secs", "sec", "minute", "minutes", "hr", "hrs",
                                "milli", "millis", "every", "until", "unless", "and", "or", "dispatch", "send", "give",
                                "receive", "peek", "poll", "push", "pop", "publish", "subscribe", "emit", "drain",
                                "all", "import", "reduce", "truthy", "is", "else", "const", "in", "true", "false",
                                "yes", "no", "void", "error", "to", "from", "async", "stateless", "size", "as",
                                "while", "collect");
    static final Terminals
            OPERATORS =
            Terminals.operators("|", ">>", "<<", "->", "=>", ".:", "<=", ">=", "<-", "(", ")", "--", "++", ".", ":",
                                "<", ">", "?", "?:", "!", "!!", ">&", "{", "}", ",", "$", "=", ";", "[", "]", "??",
                                "!!", "*>", "==", "!=", "+", "-", "\n", "${", ":=", "&", "&=", "@", "+>", "<+", "*>",
                                "<*", "*|", "|*", "*|*", "|>", "<|", "&>", "<&", "?>", "<?", "?->", "<=>", "<$", "-_-",
                                "::", "/", "%", "*", "&&", "||", "<--", "<++", "\u2357", "~", "?$?", "-:", "..", "?..?",
                                "€", "@@", "<@", "@>", "#", "!?#*!", "?*");

    static final Parser<?> TERMINATOR_SYMBOL = Parsers.or(OP("\n"), OP(";")).many1();
    static final Parser<?> COMMA_TERMINATOR = Parsers.sequence(OP(","), OP("\n").many());
    static final Parser<?> COMMA_OR_NEWLINE_TERMINATOR = Parsers.or(OP(","), OP("\n")).many1();
    static final Parser<?> SEMICOLON_TERMINATOR = Parsers.or(OP(";"), OP("\n").many1());
    static final Parser<Void> IGNORED =
            Parsers.or(Scanners.JAVA_LINE_COMMENT, Scanners.JAVA_BLOCK_COMMENT, Scanners.among(" \t\r").many1(),
                       Scanners.lineComment("#!")).skipMany();
    static final Map<String, Tokens.Fragment> BACKTICK_QUOTE_STRING = new Map<String, Tokens.Fragment>() {
        public Tokens.Fragment map(String text) {
            return Tokens.fragment(tokenizeBackTick(text), "java");
        }

        @Override
        public String toString() {
            return "JAVA_STRING";
        }
    };

    static final Parser<?> TOKENIZER =
            or(DollarLexer.url(),
               OPERATORS.tokenizer(),
               DollarLexer.decimal(),
               java(),
               Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER,
               Terminals.StringLiteral.SINGLE_QUOTE_TOKENIZER,
               Terminals.IntegerLiteral.TOKENIZER,
               Parsers.longest(KEYWORDS.tokenizer(), Terminals.Identifier.TOKENIZER));
    //Grammar
    static final Parser<var> IDENTIFIER = identifier();
    static final Parser<var> IDENTIFIER_KEYWORD = identifierKeyword();
    static final Parser<var> STRING_LITERAL = Terminals.StringLiteral.PARSER.map(DollarStatic::$);
    static final Parser<var>
            DECIMAL_LITERAL =
            Terminals.DecimalLiteral.PARSER.map(s -> s.contains(".") ? $(Double.parseDouble(s)) : $(Long.parseLong(s))
            );
    static final Parser<var> INTEGER_LITERAL = Terminals.IntegerLiteral.PARSER.map(s -> $(Long.parseLong(s)));
    static final Parser<var> URL = token(new TokenMap<String>() {
        @Override
        public String map(Token token) {
            final Object val = token.value();
            if (val instanceof Tokens.Fragment) {
                Tokens.Fragment c = (Tokens.Fragment) val;
                if (!c.tag().equals("uri")) {
                    return null;
                }
                return c.text();
            } else { return null; }
        }

        @Override
        public String toString() {
            return "URI";
        }
    }).map(DollarFactory::fromURI);

    static Parser<?> url() {
        return (
                Scanners.pattern(Patterns.isChar(CharPredicates.IS_ALPHA_).many1()
                                         .next(Patterns.isChar(':').next(Patterns.among("=\"").not())
                                                       .next(Patterns.among("-._~:/?#[]@!$&'()*+,;=%")
                                                                     .or(Patterns.isChar(
                                                                                 CharPredicates.IS_ALPHA_NUMERIC_)
                                                                     )
                                                                     .many1()
                                                       )), "uri").source()
        ).map(new Map<String, Tokens.Fragment>() {
            public Tokens.Fragment map(String text) {
                return Tokens.fragment(text, "uri");
            }

            @Override
            public String toString() {
                return "URI";
            }
        });
    }

    static Parser<?> java() {
        Parser<?> quote = Scanners.isChar('`');
        return Scanners.pattern(Patterns.regex("((``)|[^`])*"), "Java Snippet")
                       .between(quote, quote)
                       .source()
                       .map(BACKTICK_QUOTE_STRING);
    }

    static String tokenizeBackTick(String text) {
        int end = text.length() - 1;
        StringBuilder buf = new StringBuilder();
        for (int i = 1; i < end; i++) {
            char c = text.charAt(i);
            if (c != '`') {
                buf.append(c);
            } else {
                buf.append('`');
                i++;
            }
        }
        return buf.toString();
    }

    static Parser<?> decimal() {
        return Scanners.pattern(
                Patterns.INTEGER.next(Patterns.isChar('.').next(Patterns.many1(CharPredicates.IS_DIGIT))), "decimal")
                       .source()
                       .map(new Map<String, Tokens.Fragment>() {
                           public Tokens.Fragment map(String text) {
                               return Tokens.fragment(text, Tokens.Tag.DECIMAL);
                           }

                           @Override
                           public String toString() {
                               return String.valueOf(Tokens.Tag.DECIMAL);
                           }
                       });
    }

    static Parser<?> OP(String name, String keyword) {
        return OPERATORS.token(name).or(KEYWORDS.token(keyword));
    }

    static Parser<?> OP_NLS(String... names) {
        return OP("\n").many().followedBy(OPERATORS.token(names).followedBy(OP("\n").many()));
    }

    static Parser<?> OP(String name) {
        return OPERATORS.token(name);
    }

    static Parser<?> OP_NL(String... names) {
        return OPERATORS.token(names).followedBy(OP("\n").many());
    }

    static Parser<?> KEYWORD_NL(String... names) {
        return KEYWORDS.token(names).followedBy(OP("\n").many());
    }

    static Parser<?> NL_TERM(String... names) {
        return OP("\n").many().followedBy(OPERATORS.token(names));
    }

    private static Parser<String> identifierTokenizer() {
        return Scanners.pattern(Patterns.isChar(CharPredicates.IS_ALPHA_).many1(), "identifier").source();

    }

    static Parser<var> identifier() {
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
        return or(KEYWORD("true"), KEYWORD("false"), KEYWORD("yes"), KEYWORD("no"), KEYWORD("void"))
                .map(new Map<Object, var>() {
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

    static Parser<?> KEYWORD(String... names) {
        return KEYWORDS.token(names);
    }
}
