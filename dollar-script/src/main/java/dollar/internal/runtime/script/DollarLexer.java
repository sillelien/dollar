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

import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.var;
import org.apache.commons.lang.StringEscapeUtils;
import org.jparsec.*;
import org.jparsec.functors.Map;
import org.jparsec.pattern.CharPredicates;
import org.jparsec.pattern.Pattern;
import org.jparsec.pattern.Patterns;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;

import static com.sillelien.dollar.api.DollarStatic.*;
import static org.jparsec.Parsers.or;
import static org.jparsec.Parsers.token;

class DollarLexer {
    public static final Terminals
            KEYWORDS =
            Terminals.operators("out", "err", "debug", "fix", "causes", "when", "if", "then", "for", "each", "fail",
                                "assert", "switch", "choose", "not", "dollar", "fork", "join", "print", "default",
                                "debug", "error", "filter", "every", "until", "unless", "and", "or",
                                "dispatch", "send", "publish", "subscribe", "emit", "drain",
                                "all", "import", "reduce", "truthy", "is", "else", "const", "in", "true", "false",
                                "yes", "no", "void", "error", "to", "from", "size", "as",
                                "while", "collect", "module", "include", "export", "with", "parallel", "serial",
                                "fork", "null", "volatile", "read", "write", "block", "mutate", "pure","impure", "variant",
                                "variance", "pluripotent", "vary", "varies", "infinity","var","readonly","def");
    public static final Terminals
            OPERATORS =
            Terminals.operators("|", ">>", "<<", "->", "=>", ".:", "<=", ">=", "<-", "(", ")", "--", "++", ".", ":",
                                "<", ">", "?", "?:", "!", "!!", ">&", "{", "}", ",", "$", "=", ";", "[", "]", "??",
                                "!!", "*>", "==", "!=", "+", "-", "\n", ":=", "&", "&=", "@", "+>", "<+", "*>",
                                "<*", "*|", "|*", "*|*", "|>", "<|", "&>", "<&", "?>", "<?",
                                "?->", "<=>","<->", "<$", "-_-",
                                "::", "/", "%", "*", "&&", "||", "<--", "<++", "\u2357", "~", "?$?", "-:", "..", "?..?",
                                "€", "@@", "<@", "@>", "#", "!?#*!", "?*", "(!)", "(>)", "(=)", "(~)", "(-)", "(+)",
                                "(?)", "?->", "|:|", "|..|", "-<", "?=", "*=");

    public static final Map<String, Tokens.Fragment> BACKTICK_QUOTE_STRING = new Map<String, Tokens.Fragment>() {
        public Tokens.Fragment map(@NotNull String text) {
            return Tokens.fragment(tokenizeBackTick(text), "java");
        }

        @NotNull @Override
        public String toString() {
            return "JAVA_STRING";
        }
    };


    public static final Parser<?> TERMINATOR_SYMBOL = or(OP("\n"), OP(";")).many1();
    public static final Parser<?> COMMA_TERMINATOR = Parsers.sequence(OP(","), OP("\n").many());
    public static final Parser<?> COMMA_OR_NEWLINE_TERMINATOR = or(OP(","), OP("\n")).many1();
    public static final Parser<?>
            SEMICOLON_TERMINATOR =
            or(OP(";").followedBy(OP("\n").many()), OP("\n").many1());
    public static final Parser<Void> IGNORED =
            or(Scanners.JAVA_LINE_COMMENT, Scanners.JAVA_BLOCK_COMMENT, Scanners.among(" \t\r").many1(),
               Scanners.lineComment("#!")).skipMany();
    public static final Parser<var> IDENTIFIER = identifier();
    public static final Parser<var> IDENTIFIER_KEYWORD = identifierKeyword();
    public static final Parser<var>
            STRING_LITERAL =
            Terminals.StringLiteral.PARSER.map((o) -> DollarFactory.fromStringValue(
                    o));
    public static final Parser<var>
            DECIMAL_LITERAL =
            Terminals.DecimalLiteral.PARSER.map(s -> s.contains(".") ? $(Double.parseDouble(s)) : $(Long.parseLong(s))
            );
    public static final Parser<var> INTEGER_LITERAL = Terminals.IntegerLiteral.PARSER.map(
            s -> {
                if (new BigDecimal(s).compareTo(new BigDecimal(Long.MAX_VALUE)) > 0) {
                    return $(Double.parseDouble(s));
                } else if (new BigDecimal(s).compareTo(new BigDecimal(-Long.MAX_VALUE)) < 0) {
                    return $(Double.parseDouble(s));
                } else {
                    return $(Long.parseLong(s));
                }
            });
    public static final Parser<var> BUILTIN = token(new TokenTagMap("builtin")).map(s -> {
        var v = $(s);
        v.setMetaAttribute("__builtin", s);
        return v;
    });
    public static final Parser<var> URL = token(new TokenTagMap("uri")).map(DollarFactory::fromURI);
    public static final Parser<?> TOKENIZER =
            or(DollarLexer.url(),
               OPERATORS.tokenizer(),
               DollarLexer.decimal(),
               java(),
               Scanners.DOUBLE_QUOTE_STRING.map(new Map<String, String>() {
                   public String map(@NotNull String text) {
                       return tokenizeDoubleQuote(text);
                   }

                   @NotNull @Override public String toString() {
                       return "DOUBLE_QUOTE_STRING";
                   }
               }),
               Scanners.SINGLE_QUOTE_STRING.map(new Map<String, String>() {
                   public String map(@NotNull String text) {
                       return tokenizeSingleQuote(text);
                   }

                   @NotNull @Override public String toString() {
                       return "SINGLE_QUOTE_STRING";
                   }
               }),
               Terminals.IntegerLiteral.TOKENIZER,
               Parsers.longest(DollarLexer.builtin(), KEYWORDS.tokenizer(), Terminals.Identifier.TOKENIZER));

    static String tokenizeDoubleQuote(@NotNull String text) {
        return StringEscapeUtils.unescapeJava(text.substring(1, text.length() - 1));

    }

    static String tokenizeSingleQuote(@NotNull String text) {
        return StringEscapeUtils.unescapeJava(text.substring(1, text.length() - 1));
    }

    public static Parser<?> url() {
        return (
                Scanners.pattern(Patterns.isChar(CharPredicates.IS_ALPHA_).many1()
                                         .next(Patterns.isChar(':').next(Patterns.among("=\"").not())
                                                       .next(Patterns.among("-._~:/?#@!$&'*+,;=%")
                                                                     .or(Patterns.isChar(
                                                                                 CharPredicates.IS_ALPHA_NUMERIC_)
                                                                     )
                                                                     .many1()
                                                       )), "uri").source()
        ).map(new Map<String, Tokens.Fragment>() {
            public Tokens.Fragment map(String text) {
                return Tokens.fragment(text, "uri");
            }

            @NotNull @Override
            public String toString() {
                return "URI";
            }
        });
    }

    public static Parser<?> java() {
        Parser<?> quote = Scanners.isChar('`');
        return Scanners.pattern(Patterns.regex("((``)|[^`])*"), "Java Snippet")
                       .between(quote, quote)
                       .source()
                       .map(BACKTICK_QUOTE_STRING);
    }

    @NotNull public static String tokenizeBackTick(@NotNull String text) {
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

    public static Parser<?> decimal() {
        return Scanners.pattern(
                Patterns.INTEGER.next(Patterns.isChar('.').next(Patterns.many1(CharPredicates.IS_DIGIT)).next(
                        Patterns.sequence(Patterns.among("eE"), Patterns.among("+-").optional(), Patterns.INTEGER)
                                .optional())),
                "decimal")
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


    public static Parser<?> OP(String name, String keyword) {
        return OPERATORS.token(name).or(KEYWORDS.token(keyword));
    }

// --Commented out by Inspection START (04/12/14 18:27):
//    static Parser<?> OP_NLS(String... names) {
//        return OP("\n").many().followedBy(OPERATORS.token(names).followedBy(OP("\n").many()));
//    }
// --Commented out by Inspection STOP (04/12/14 18:27)

    public static Parser<?> OP_NL(String... names) {
        return OPERATORS.token(names).followedBy(OP("\n").many());
    }

    public static Parser<?> OP(String name) {
        return OPERATORS.token(name);
    }

    public static Parser<?> KEYWORD_NL(String... names) {
        return KEYWORDS.token(names).followedBy(OP("\n").many());
    }

    public static Parser<?> NL_OP(String... names) {
        return OP("\n").many().followedBy(OPERATORS.token(names));
    }

    public static Parser<String> identifierTokenizer() {
        return Scanners.pattern(Patterns.isChar(CharPredicates.IS_ALPHA_).many1(), "identifier").source();

    }

    public static Parser<?> builtin() {
        return Scanners.pattern(new Pattern() {
            @Override public int match(@NotNull CharSequence src, int begin, int end) {
                int i = begin;
                //noinspection StatementWithEmptyBody
                for (; i < end && Character.isAlphabetic(src.charAt(i)); i++) {
                    //
                }
                final String name = src.subSequence(begin, i).toString();
                if (Builtins.exists(name)) {
                    return i - begin;
                } else {
                    return Pattern.MISMATCH;
                }
            }
        }, "builtin").source()
                       .map(new Map<String, Tokens.Fragment>() {
                           public Tokens.Fragment map(String text) {
                               return Tokens.fragment(text, "builtin");
                           }

                           @NotNull @Override
                           public String toString() {
                               return "builtin";
                           }
                       });
    }


    public static Parser<var> identifier() {
        return Terminals.Identifier.PARSER.map(i -> {
            switch (i) {
                case "true":
                    return DollarFactory.TRUE;
                case "false":
                    return DollarFactory.FALSE;
                case "yes":
                    return DollarFactory.TRUE;
                case "no":
                    return DollarFactory.FALSE;
                case "infinity":
                    return DollarFactory.INFINITY;
                case "void":
                    return $void();
                case "null":
                    return $null(Type._ANY);
                default:
                    return $(i);
            }
        });
    }


    public static Parser<var> identifierKeyword() {
        return or(KEYWORD("true"), KEYWORD("false"), KEYWORD("yes"), KEYWORD("no"), KEYWORD("null"), KEYWORD("void"),
                  KEYWORD("infinity"))
                .map(new Map<Object, var>() {
                    @NotNull @Override
                    public var map(@NotNull Object i) {
                        switch (i.toString()) {
                            case "true":
                                return DollarFactory.TRUE;
                            case "false":
                                return DollarFactory.FALSE;
                            case "yes":
                                return DollarFactory.TRUE;
                            case "no":
                                return DollarFactory.FALSE;
                            case "null":
                                return DollarFactory.newNull(Type._ANY);
                            case "infinity":
                                return DollarFactory.INFINITY;
                            case "void":
                                return DollarFactory.VOID;
                            default:
                                return $(i);
                        }
                    }
                });
    }

    public static Parser<?> KEYWORD(String... names) {
        return KEYWORDS.token(names);
    }

    public static class TokenTagMap implements TokenMap<String> {
        private final String tag;

        private TokenTagMap(String tag) {this.tag = tag;}

        @Nullable @Override
        public String map(@NotNull Token token) {
            final Object val = token.value();
            if (val instanceof Tokens.Fragment) {
                Tokens.Fragment c = (Tokens.Fragment) val;
                if (!c.tag().equals(tag)) {
                    return null;
                }
                return c.text();
            } else { return null; }
        }

        @Override
        public String toString() {
            return tag;
        }
    }
}
