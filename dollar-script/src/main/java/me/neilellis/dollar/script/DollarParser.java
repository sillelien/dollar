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
import me.neilellis.dollar.var;
import org.codehaus.jparsec.*;
import org.codehaus.jparsec.pattern.CharPredicates;
import org.codehaus.jparsec.pattern.Patterns;

import java.io.*;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarParser {
    static final Parser<?> TOKENIZER =
            Parsers.or(Terminals.DecimalLiteral.TOKENIZER, Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER, Terminals.StringLiteral.SINGLE_QUOTE_TOKENIZER, OPERATORS.tokenizer(), url());
    static final Parser<var> IDENTIFIER = Terminals.Identifier.PARSER.map(s -> DollarStatic.$(s));
    static final Parser<var> STRING_LITERAL = Terminals.StringLiteral.PARSER.map(s -> DollarStatic.$(s));
    static final Parser<var> DECIMAL_LITERAL = Terminals.DecimalLiteral.PARSER.map(s -> DollarStatic.$(Double.parseDouble(s)));
    static final Parser<var> INTEGER_LITERAL = Terminals.IntegerLiteral.PARSER.map(s -> DollarStatic.$(Long.parseLong(s)));
    static final Parser<var> URL = url().map(url -> DollarStatic.$(url));
    private static final Terminals OPERATORS = Terminals.operators("|", ">>", "<<", "->", "=>", "<=", "<-", "(", ")", "--", "++", ".", ":", "<", ">", "?", "?:", "!", "!!", ">&", "{", "}", ",");
    private static final Parser<Void> IGNORED =
            Parsers.or(Scanners.JAVA_LINE_COMMENT, Scanners.JAVA_BLOCK_COMMENT, Scanners.WHITESPACES, Scanners.lineComment("#")).skipMany();
    private final ClassLoader classLoader;

    public DollarParser() {
        classLoader = DollarParser.class.getClassLoader();
    }

    public DollarParser(ClassLoader classLoader) {

        this.classLoader = classLoader;
    }

    static <T> Parser<T> op(String name, T value) {
        return term(name).retn(value);
    }

    static Parser<?> term(String... names) {
        return OPERATORS.token(names);
    }

    private static Parser<var> block() {
        Parser.Reference<var> ref = Parser.newReference();
        Parser<var> parser = ref.lazy().between(term("{"), term("}")).or(expression());
        ref.set(parser);
        return parser;
    }

//    private static Parser<Class<? extends Pipeable>> classRef() {
//        Scanners.string("${");
//        Parser<Class<? extends Pipeable>> result = Scanners.IDENTIFIER.map(new Map<String, Class<? extends Pipeable>>() {
//            @Override
//            public Class<? extends Pipeable> map(String s) {
//                try {
//                    return (Class<? extends Pipeable>) Class.forName(s);
//                } catch (ClassNotFoundException e) {
//                    throw new Error(e);
//                }
//            }
//        });
//        Scanners.string("}");
//        return result;
//    }


    private static Parser<var> expression() {
        Parser.Reference<var> ref = Parser.newReference();
        Parser<var> unit = ref.lazy().between(term("{"), term("}")).or(Parsers.or(INTEGER_LITERAL, STRING_LITERAL, DECIMAL_LITERAL, URL));

        Parser<var> parser = new OperatorTable<var>()
                .infixl(op("|", BinaryOperator.PIPE), 80)
                .infixl(op(":", BinaryOperator.PAIR), 30)
                .infixl(op("<", BinaryOperator.READ), 50)
                .infixl(op(">", BinaryOperator.WRITE), 50)
                .prefix(op("!", UnaryOperator.OUT), 5)
                .prefix(op("!!", UnaryOperator.ERR), 5)
                .infixl(op(">>", BinaryOperator.LOAD), 50)
                .infixl(op("<<", BinaryOperator.SAVE), 50)
                .infixl(op("->", BinaryOperator.SEND), 50)
                .infixl(op(">&", BinaryOperator.DISPATCH), 50)
                .infixl(op("<-", BinaryOperator.SEND), 50)
                .infixl(op("=>", BinaryOperator.PUB), 50)
//                .infixl(op("<=", BinaryOperator.SUB), 500)
                .infixl(op("?", BinaryOperator.CHOOSE), 50)
                .infixl(op("?:", BinaryOperator.ELVIS), 50)
                .infixl(op(",", BinaryOperator.APPEND), 10)
                .infixl(op(".", BinaryOperator.GET), 50)
                .postfix(op("--", UnaryOperator.DEC), 100)
                .postfix(op("++", UnaryOperator.DEC), 100)
                .build(unit);
        ref.set(parser);
        return parser;
    }

    private static Parser<String> url() {
        return Scanners.pattern(Patterns.isChar(CharPredicates.IS_ALPHA_).next(Patterns.isChar(CharPredicates.IS_ALPHA_NUMERIC_).many()
                .next(Patterns.isChar(':'))
                .next(Patterns.among("-._~:/?#[]@!$&'()*+,;=%").or(Patterns.isChar(CharPredicates.IS_ALPHA_NUMERIC_)
                        ).many()
                )), "url").source();
    }

    public var parse(File file) throws IOException {
        DollarStatic.context().setClassLoader(classLoader);
        Parser<var> parser = buildParser();
        return parser.from(TOKENIZER, IGNORED).parse(new FileReader(file));
    }

    public var parse(InputStream in) throws IOException {
        DollarStatic.context().setClassLoader(classLoader);
        Parser<var> parser = buildParser();
        return parser.from(TOKENIZER, IGNORED).parse(new InputStreamReader(in));
    }

    public var parse(String s) throws IOException {
        DollarStatic.context().setClassLoader(classLoader);
        Parser<var> parser = buildParser();
        return parser.from(TOKENIZER, IGNORED).parse(s);
    }

    private Parser<var> buildParser() {
        return expression();
    }

}
