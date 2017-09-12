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

package dollar.internal.runtime.script.parser;

import dollar.api.Type;
import dollar.api.types.DollarFactory;
import dollar.api.var;
import dollar.internal.runtime.script.api.HasKeyword;
import dollar.internal.runtime.script.api.HasSymbol;
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.Terminals;
import org.jparsec.Token;
import org.jparsec.TokenMap;
import org.jparsec.Tokens;
import org.jparsec.pattern.CharPredicates;
import org.jparsec.pattern.Pattern;
import org.jparsec.pattern.Patterns;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dollar.api.DollarStatic.*;
import static dollar.api.types.meta.MetaConstants.IS_BUILTIN;
import static dollar.internal.runtime.script.Builtins.exists;
import static dollar.internal.runtime.script.parser.Symbols.*;
import static java.lang.Character.isAlphabetic;
import static org.jparsec.Parsers.or;
import static org.jparsec.Parsers.token;
import static org.jparsec.Tokens.fragment;
import static org.jparsec.pattern.CharPredicates.IS_DIGIT;
import static org.jparsec.pattern.Patterns.*;

@SuppressWarnings({"UtilityClassCanBeEnum", "UtilityClassCanBeSingleton"})
final class DollarLexer {
    @NotNull
    static final Parser<var> BUILTIN = token(new TokenTagMap("builtin")).map(s -> {
        var v = $(s);
        v.metaAttribute(IS_BUILTIN, s);
        return v;
    });
    @NotNull
    static final Parser<var>
            DECIMAL_LITERAL =
            Terminals.DecimalLiteral.PARSER.map(s -> s.contains(".") ? $(Double.parseDouble(s)) : $(Long.parseLong(s))
            );
    @NotNull
    static final Parser<var> IDENTIFIER = identifier();
    @NotNull
    static final Parser<Void> IGNORED =
            or(Scanners.JAVA_LINE_COMMENT, Scanners.JAVA_BLOCK_COMMENT, Scanners.among(" \t\r").many1(),
               Scanners.lineComment("#!")).skipMany();
    @NotNull
    static final Parser<var> INTEGER_LITERAL = Terminals.IntegerLiteral.PARSER.map(
            s -> {
                if ((new BigDecimal(s).compareTo(new BigDecimal(Long.MAX_VALUE)) > 0)
                            || (new BigDecimal(s).compareTo(new BigDecimal(-Long.MAX_VALUE)) < 0)) {
                    return $(Double.parseDouble(s));
                } else {
                    return $(Long.parseLong(s));
                }
            });
    @NotNull
    static final Terminals KEYWORDS = Terminals.operators(Symbols.KEYWORD_STRINGS);
    @NotNull
    static final Parser<var> IDENTIFIER_KEYWORD = identifierKeyword();
    @NotNull
    static final Terminals OPERATORS = Terminals.operators(Symbols.SYMBOL_STRINGS);
    @NotNull
    static final Parser<?> COMMA_OR_NEWLINE_TERMINATOR = Parsers.or(OP(COMMA), OP(NEWLINE)).many1();
    @NotNull
    static final Parser<?> COMMA_TERMINATOR = Parsers.sequence(OP(COMMA), OP(NEWLINE).many());
    @NotNull
    static final Parser<?> SEMICOLON_TERMINATOR = or(OP(SEMI_COLON).followedBy(OP(NEWLINE).many()), OP(NEWLINE).many1());
    @NotNull
    static final Parser<var>
            STRING_LITERAL =
            Terminals.StringLiteral.PARSER.map(DollarFactory::fromStringValue);
    @NotNull
    static final Parser<?> TERMINATOR_SYMBOL = Parsers.or(OP(NEWLINE), OP(SEMI_COLON)).many1();
    @NotNull
    static final Parser<var> URL = token(new TokenTagMap("uri")).map(DollarFactory::fromURI);
    @NotNull
    private static final Function<String, Tokens.Fragment> BACKTICK_QUOTE_STRING = new Function<String, Tokens.Fragment>() {
        @Override
        public Tokens.Fragment apply(@NotNull String text) {
            return fragment(tokenizeBackTick(text), "backtick");
        }

        @NotNull
        @Override
        public String toString() {
            return "BACKTICK_STRING";
        }
    };


    @NotNull
    static final Parser<?> TOKENIZER =
            or(DollarLexer.url(),
               OPERATORS.tokenizer(),
               DollarLexer.decimal(),
               script(),
               Scanners.DOUBLE_QUOTE_STRING.map(new Function<String, String>() {
                   @Override
                   @NotNull
                   public String apply(@NotNull String text) {
                       return tokenizeDoubleQuote(text);
                   }

                   @NotNull
                   @Override
                   public String toString() {
                       return "DOUBLE_QUOTE_STRING";
                   }
               }),
               Scanners.SINGLE_QUOTE_STRING.map(new Function<String, String>() {
                   @Override
                   @NotNull
                   public String apply(@NotNull String text) {
                       return tokenizeSingleQuote(text);
                   }

                   @NotNull
                   @Override
                   public String toString() {
                       return "SINGLE_QUOTE_STRING";
                   }
               }),
               Terminals.IntegerLiteral.TOKENIZER,
               Parsers.longest(DollarLexer.builtin(), KEYWORDS.tokenizer(), Terminals.Identifier.TOKENIZER));

    @NotNull
    static Parser<?> KEYWORD(HasKeyword... names) {
        return KEYWORDS.token(keywordsToString(names));
    }

    static Parser<?> KEYWORD_NL(HasKeyword... names) {
        return KEYWORDS.token(keywordsToString(names)).followedBy(OP(NEWLINE).many());
    }

    static Parser<?> NL_OP(HasSymbol... names) {
        return OP(NEWLINE).many().followedBy(OPERATORS.token(symbolsToString(names)));
    }

    @NotNull
    static Parser<?> OP(@NotNull Op op) {
        if (op.symbol() == null) {
            return KEYWORDS.token(op.keyword());
        }
        if (op.keyword() == null) {
            return OPERATORS.token(op.symbol());
        }
        return OPERATORS.token(op.symbol()).or(KEYWORDS.token(op.keyword()));
    }

    @NotNull
    static Parser<?> OP(@NotNull HasSymbol symbol) {
        return OPERATORS.token(symbolsToString(symbol));
    }

    static Parser<?> OP_NL(HasSymbol... names) {
        return OPERATORS.token(symbolsToString(names)).followedBy(OP(NEWLINE).many());
    }

    private static Parser<?> builtin() {
        //noinspection OverlyComplexAnonymousInnerClass
        return new Pattern() {
            @Override
            public int match(@NotNull CharSequence src, int begin, int end) {
                int i = begin;
                //noinspection StatementWithEmptyBody
                while ((i < end) && isAlphabetic(src.charAt(i))) i++;
                final String name = src.subSequence(begin, i).toString();
                return exists(name) ? (i - begin) : Pattern.MISMATCH;
            }
        }.toScanner("builtin").source()
                       .map(new Function<String, Tokens.Fragment>() {
                           @Override
                           @NotNull
                           public Tokens.Fragment apply(@NotNull String text) {
                               return fragment(text, "builtin");
                           }

                           @NotNull
                           @Override
                           public String toString() {
                               return "builtin";
                           }
                       });
    }

// --Commented out by Inspection START (04/12/14 18:27):
//    static Parser<?> OP_NLS(String... names) {
//        return OP("\n").many().followedBy(OPERATORS.token(names).followedBy(OP("\n").many()));
//    }
// --Commented out by Inspection STOP (04/12/14 18:27)

    private static Parser<?> decimal() {
        return
                INTEGER.next(isChar('.').next(many1(IS_DIGIT))
                                     .next(
                                             sequence(
                                                     among("eE"), among("+-").optional(), INTEGER)
                                                     .optional()
                                     )
                ).toScanner("decimal")
                        .source()
                        .map(new Function<String, Tokens.Fragment>() {
                            @Override
                            @NotNull
                            public Tokens.Fragment apply(@NotNull String text) {
                                return fragment(text, Tokens.Tag.DECIMAL);
                            }

                            @NotNull
                            @Override
                            public String toString() {
                                return String.valueOf(Tokens.Tag.DECIMAL);
                            }
                        });
    }

    static Parser<var> identifier() {
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

    private static Parser<var> identifierKeyword() {
        return or(KEYWORD(TRUE), KEYWORD(FALSE), KEYWORD(YES), KEYWORD(NO), KEYWORD(NULL), KEYWORD(VOID),
                  KEYWORD(INFINITY))
                       .map(i -> {
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
                       });
    }

    @NotNull
    private static String[] keywordsToString(@NotNull HasKeyword... names) {
        return Arrays.stream(names).map(HasKeyword::keyword).collect(Collectors.toList()).toArray(new String[names.length]);
    }

//    public static Parser<String> identifierTokenizer() {
//        return isChar(CharPredicates.IS_ALPHA_).many1().toScanner("identifier").source();
//
//    }

    public static Parser<?> script() {
        Parser<?> quote = Scanners.isChar('`');
        return Patterns.regex("((``)|[^`])*").toScanner("Java Snippet")
                       .between(quote, quote)
                       .source()
                       .map(BACKTICK_QUOTE_STRING);
    }

    @NotNull
    private static String[] symbolsToString(@NotNull HasSymbol... names) {
        return Arrays.stream(names).map(HasSymbol::symbol).collect(Collectors.toList()).toArray(new String[names.length]);
    }

    @SuppressWarnings("AssignmentToForLoopParameter")
    @NotNull
    private static String tokenizeBackTick(@NotNull CharSequence text) {
        int end = text.length() - 1;
        StringBuilder buf = new StringBuilder();
        for (int i = 1; i < end; i++) {
            char c = text.charAt(i);
            if (c == '`') {
                buf.append('`');
                i++;
            } else {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    @NotNull
    private static String tokenizeDoubleQuote(@NotNull String text) {
        return StringEscapeUtils.unescapeJava(text.substring(1, text.length() - 1));

    }

    @NotNull
    private static String tokenizeSingleQuote(@NotNull String text) {
        return StringEscapeUtils.unescapeJava(text.substring(1, text.length() - 1));
    }

    private static Parser<?> url() {
        return (
                       isChar(CharPredicates.IS_ALPHA_)
                               .many1()
                               .next(isChar(':')
                                             .next(among("=\"").not())
                                             .next(among("-._~:/?#@!$&'*+,;=%").or(
                                                     isChar(CharPredicates.IS_ALPHA_NUMERIC_)).many1()
                                             )
                               ).toScanner("uri").source()
        ).map(new Function<String, Tokens.Fragment>() {
            @Override
            @NotNull
            public Tokens.Fragment apply(@NotNull String text) {
                return fragment(text, "uri");
            }

            @NotNull
            @Override
            public String toString() {
                return "URI";
            }
        });
    }

    public static final class TokenTagMap implements TokenMap<String> {
        @NotNull
        private final String tag;

        private TokenTagMap(@NotNull String tag) {this.tag = tag;}

        @Nullable
        @Override
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

        @NotNull
        @Override
        public String toString() {
            return tag;
        }
    }
}
