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

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Tokens;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.pattern.CharPredicates;
import org.codehaus.jparsec.pattern.Patterns;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class Lexical {
    static final Parser<?> TERMINATOR_SYMBOL = Parsers.or(term("\n"), term(";")).many1();
    static final Parser<?> COMMA_TERMINATOR = Parsers.sequence(term(","), term("\n").many());
    static final Parser<?> COMMA_OR_NEWLINE_TERMINATOR = Parsers.or(term(","), term("\n")).many1();
    static final Parser<?> SEMICOLON_TERMINATOR = Parsers.or(term(";"), term("\n").many1());
    static final Parser<Void> IGNORED =
            Parsers.or(Scanners.JAVA_LINE_COMMENT, Scanners.JAVA_BLOCK_COMMENT, Scanners.among(" \t\r").many1(), Scanners.lineComment("#!")).skipMany();
    static final Map<String, Tokens.Fragment> BACKTICK_QUOTE_STRING = new Map<String, Tokens.Fragment>() {
        public Tokens.Fragment map(String text) {
            return Tokens.fragment(tokenizeBackTick(text), "java");
        }

        @Override
        public String toString() {
            return "JAVA_STRING";
        }
    };

    static Parser<?> url() {
        return (Scanners.pattern(Patterns.isChar(CharPredicates.IS_ALPHA_).many1()
                .next(Patterns.isChar(':').next(Patterns.among("=\"").not())
                        .next(Patterns.among("-._~:/?#[]@!$&'()*+,;=%").or(Patterns.isChar(CharPredicates.IS_ALPHA_NUMERIC_)
                                ).many1()
                        )), "uri").source()).map(new Map<String, Tokens.Fragment>() {
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
        return Scanners.pattern(Patterns.regex("((``)|[^`])*"), "Java Snippet").between(quote, quote).source().map(BACKTICK_QUOTE_STRING);
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
        return Scanners.pattern(Patterns.INTEGER.next(Patterns.isChar('.').next(Patterns.many1(CharPredicates.IS_DIGIT))), "decimal").source().map(new Map<String, Tokens.Fragment>() {
            public Tokens.Fragment map(String text) {
                return Tokens.fragment(text, Tokens.Tag.DECIMAL);
            }

            @Override
            public String toString() {
                return String.valueOf(Tokens.Tag.DECIMAL);
            }
        });
    }

    static Parser<?> term(String name, String keyword) {
        return DollarParser.OPERATORS.token(name).or(DollarParser.KEYWORDS.token(keyword));
    }

    static Parser<?> term(String name) {
        return DollarParser.OPERATORS.token(name);
    }

    static Parser<?> keyword(String... names) {
        return DollarParser.KEYWORDS.token(names);
    }

    static Parser<?> termAndNewlines(String... names) {
        return term("\n").many().followedBy(DollarParser.OPERATORS.token(names).followedBy(term("\n").many()));
    }

    static Parser<?> termFollowedByNewlines(String... names) {
        return DollarParser.OPERATORS.token(names).followedBy(term("\n").many());
    }

    static Parser<?> keywordFollowedByNewlines(String... names) {
        return DollarParser.KEYWORDS.token(names).followedBy(term("\n").many());
    }

    static Parser<?> termPreceededByNewlines(String... names) {
        return term("\n").many().followedBy(DollarParser.OPERATORS.token(names));
    }

    private static Parser<String> identifierTokenizer() {
        return Scanners.pattern(Patterns.isChar(CharPredicates.IS_ALPHA_).many1(), "identifier").source();

    }
}
