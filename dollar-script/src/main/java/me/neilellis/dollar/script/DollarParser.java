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
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.*;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.pattern.CharPredicates;
import org.codehaus.jparsec.pattern.Patterns;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;
import org.pegdown.ast.RootNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static me.neilellis.dollar.DollarStatic.$;
import static me.neilellis.dollar.DollarStatic.$void;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarParser {


    static final Terminals OPERATORS = Terminals.operators("|", ">>", "<<", "->", "=>", "<=", "<-", "(", ")", "--", "++", ".", ":", "<", ">", "?", "?:", "!", "!!", ">&", "{", "}", ",", "$", "=", ";", "[", "]", "`", "``", "??", "!!", "*>", "==", "!=", "+", "-", "\n", "$(", "${", ":=", "&", "&=", "<>", "+>", "<+", "*>", "<*", "*|", "|*", "*|*", "|>", "<|", "&>");
    static final Parser<?> TOKENIZER =
            Parsers.or(url(), Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER, Terminals.StringLiteral.SINGLE_QUOTE_TOKENIZER, Terminals.DecimalLiteral.TOKENIZER, Terminals.IntegerLiteral.TOKENIZER, Terminals.Identifier.TOKENIZER, OPERATORS.tokenizer());
    static final Parser<var> IDENTIFIER = Terminals.Identifier.PARSER.map(s -> $(s));
    static final Parser<var> STRING_LITERAL = Terminals.StringLiteral.PARSER.map(s -> $(s));
    static final Parser<var> DECIMAL_LITERAL = Terminals.DecimalLiteral.PARSER.map(s ->
                    s.contains(".") ? $(Double.parseDouble(s)) : $(Long.parseLong(s))
    );
    static final Parser<var> INTEGER_LITERAL = Terminals.IntegerLiteral.PARSER.map(s -> $(Long.parseLong(s)));
    static final Parser<var> URL = Terminals.StringLiteral.PARSER.map(url -> $(url));
    static final Parser<Void> IGNORED =
            Parsers.or(Scanners.JAVA_LINE_COMMENT, Scanners.JAVA_BLOCK_COMMENT, Scanners.among(" \t\r").many1(), Scanners.lineComment("#")).skipMany();
    static final Parser<?> TERMINATOR_SYMBOL = Parsers.or(term("\n"), term(";")).many1();
    static final Parser<?> COMMA_TERMINATOR = Parsers.sequence(term(","), term("\n").optional());
    static final Parser<?> SEMICOLON_TERMINATOR = Parsers.or(term(";"), term("\n"));
    private final ClassLoader classLoader;
    private CopyOnWriteArrayList<ScriptScope> scopes = new CopyOnWriteArrayList<ScriptScope>();
    private Parser<?> topLevelParser;

    public DollarParser() {
        classLoader = DollarParser.class.getClassLoader();
    }

    public DollarParser(ClassLoader classLoader) {

        this.classLoader = classLoader;

    }

    static <T> Parser<T> op(String name, T value, ScriptScope scope) {
        return op(false, name, value, scope);
    }

    static <T> Parser<T> op(boolean skip, String name, T value, ScriptScope scope) {
        Parser<?> parser = term(name);
        return parser.token().map(new Map<Token, T>() {
            @Override
            public T map(Token token) {
                if (value instanceof Operator) {
                    ((Operator) value).setSource(() -> {
                        int index = token.index();
                        int length = token.length();
                        String theSource = scope.getSource();
                        int end = theSource.indexOf('\n', index + length);
                        int start = index > 10 ? index - 10 : 0;
                        String highlightedSource = "... " + theSource.substring(start, index) + " >>> " + theSource.substring(index, index + length) + " <<< " + theSource.substring(index + length, end) + " ...";
                        return highlightedSource.replaceAll("\n", "\\\\n");
                    });
                }
                return value;
            }
        });

    }

    static Parser<?> term(String... names) {
        return OPERATORS.token(names);
    }

    static Parser<?> termAndNewlines(String... names) {
        return term("\n").many().followedBy(OPERATORS.token(names).followedBy(term("\n").many()));
    }

    static Parser<?> termFollowedByNewlines(String... names) {
        return OPERATORS.token(names).followedBy(term("\n").many());
    }

    static Parser<?> termPreceededByNewlines(String... names) {
        return term("\n").many().followedBy(OPERATORS.token(names));
    }

    private static Parser<var> list(ScriptScope scope, Parser<var> expression) {
        Parser<List<var>> sequence = term("[").next(expression.sepBy(COMMA_TERMINATOR)).followedBy(term("]"));
        return sequence.map(DollarStatic::$);
    }

    private static Parser<var> parameters(ScriptScope scope, Parser<var> expression) {
        Parser<List<var>> sequence = term("(").next(expression.sepBy(COMMA_TERMINATOR)).followedBy(term(")"));
        return sequence.map(DollarStatic::$);
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

    private static Parser<var> lambdarize(ScriptScope scope, Parser<var> expression) {
        return expression.token().map(token -> {
//            scope.setLambdaUnderConstruction(true);
//            Parser<var> parser = ;
//            scope.setLambdaUnderConstruction(false);
            int index = token.index();
            int length = token.length();
            String source = scope.getSource().substring(index, index + length);
            return DollarFactory.fromLambda(v -> scope.getDollarParser().parseExpression(scope, source));
        });
    }

    private static Parser<var> map(ScriptScope scope, Parser<var> expression) {
        Parser<List<var>> sequence = termFollowedByNewlines("{").next(expression.sepBy(COMMA_TERMINATOR)).followedBy(termPreceededByNewlines("}"));
        return sequence.map(o -> {
            var current = null;
            for (var v : o) {
                if (current != null) {
                    current = current.$plus(v);
                } else {
                    current = v;
                }
            }
            return current;
        });
    }
//
//    private static Parser<var> lambda(ScriptScope scope, Parser<var> expression) {
//        Parser<var> parser = term("&").next(expression);
//        return parser;
//    }

    private static Parser<var> block(ScriptScope scope, Parser<var> parentParser) {
        Parser.Reference<var> ref = Parser.newReference();

        Parser<var> or = (Parsers.or(parentParser, ref.lazy().between(termFollowedByNewlines("{"), termPreceededByNewlines("}"))).followedBy(SEMICOLON_TERMINATOR)).many1().map(DollarStatic::$);
        ref.set(or);
        return or;
    }

    private static Parser<List<var>> script(ScriptScope scope) {
        Parser.Reference<var> ref = Parser.newReference();
        ScriptScope newScope = new ScriptScope(scope, scope.getSource());
        Parser<var> block = block(new ScriptScope(newScope, newScope.getSource()), ref.lazy()).between(termFollowedByNewlines("{"), termPreceededByNewlines("}"));
        Parser<var> expression = expression(scope).map(i -> i != null ? i : null);
        Parser<List<var>> parser = (TERMINATOR_SYMBOL.optional()).next(Parsers.or(expression, block).followedBy(TERMINATOR_SYMBOL).map(i -> $((Object) i.$())).many1());
        ref.set(parser.map(DollarStatic::$));
        return parser;
    }

    private static Parser<var> expression(ScriptScope scope) {
        Parser.Reference<var> ref = Parser.newReference();
        Parser<var> main = ref.lazy().between(term("("), term(")")).or(Parsers.or(list(scope, ref.lazy()), map(scope, ref.lazy()).map(DollarStatic::$), URL, DECIMAL_LITERAL, INTEGER_LITERAL, STRING_LITERAL, IDENTIFIER)).or(block(new ScriptScope(scope, scope.getSource()), ref.lazy()).between(termFollowedByNewlines("{"), termPreceededByNewlines("}")));

        Parser<var> unit = Parsers.array(main, parameters(scope, ref.lazy()).optional()).map(objects -> {
            if (objects.length == 1 || objects[1] == null) {
                return (var) objects[0];
            } else {
                List<var> params = ((var) objects[1]).toList();
                int count = 0;
                for (var param : params) {
                    scope.set(String.valueOf(++count), param);
                }
                return $((Object) ((var) objects[0]).$());
            }
        });
//        ScopedVarBinaryOperator value = new ScopedVarBinaryOperator((lhs, rhs) -> rhs, scope);
        Parser<var> parser = new OperatorTable<var>()
                .infixl(op("!=", new ScopedVarBinaryOperator((lhs, rhs) -> $(!lhs.equals(rhs)), scope), scope), 100)
                .infixl(op("==", new ScopedVarBinaryOperator((lhs, rhs) -> $(lhs.equals(rhs)), scope), scope), 100)
                .infixl(op("|", new ScopedVarBinaryOperator((lhs, rhs) -> {
                    if (!rhs.isString()) {
                        scope.set("1", lhs);
                        return $((Object) rhs.$());
                    } else {
                        return lhs.$pipe(rhs.$S());
                    }
                }, scope
                ), scope), 80)
                .infixl(op("+", new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$plus(rhs), scope), scope), 200)
                .infixl(op("-", new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$minus(rhs), scope), scope), 200)
                .infixl(op(":", new ScopedVarBinaryOperator((lhs, rhs) -> $(lhs.$S(), rhs), scope), scope), 30)
                .infixl(op("<", new ScopedVarBinaryOperator((lhs, rhs) -> scope.set(lhs.$S(), $().$read(new File(rhs.$S()))), scope), scope), 50)
                .infixl(op(">", new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$write(new File(rhs.$S())), scope), scope), 50)
                .infixl(op("->", new ScopedVarBinaryOperator((lhs, rhs) -> lhs.isTrue() ? rhs : lhs, scope), scope), 50)
                .prefix(op(true, ">>", new ScopedVarUnaryOperator(false, i -> {
                    i.out();
                    return $void();
                }, scope), scope), 5)
                .prefix(op(true, "!!", new ScopedVarUnaryOperator(false, i -> {
                    i.out();
                    return $void();/*reserved for info instead of debug messages*/
                }, scope), scope), 5)
                .prefix(op(true, "??", new ScopedVarUnaryOperator(false, i -> {
                    i.err();
                    return $void();
                }, scope), scope), 5)
                .prefix(op("=>", new ScopedVarUnaryOperator(false, v -> {
                    if (v.isTrue()) {
                        return v;
                    } else {
                        throw new AssertionError();
                    }
                }, scope), scope), 5)
                .infixl(op("|>", new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$save(rhs.$S()), scope), scope), 50)
                .infixl(op("<|", new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$load(rhs.$S()), scope), scope), 50)
                .infixl(op("->", new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$send(rhs.$S()), scope), scope), 50)
                .infixl(op("&>", new ScopedVarBinaryOperator((lhs, rhs) -> {
                    lhs.$dispatch(rhs.$S());
                    return lhs;
                }, scope), scope), 50)
                .infixl(op("+>", new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$send(rhs.$S()), scope), scope), 50)
                .infixl(op("*>", new ScopedVarBinaryOperator((lhs, rhs) -> {
                    lhs.$publish(rhs.$S());
                    return lhs;
                }, scope), scope), 50)
                .infixl(op("?", new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$choose(rhs), scope), scope), 50)
                .infixl(op("?:", new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$default(rhs), scope), scope), 50)
//                .infixl(op(",", new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$append(rhs), scope)), 10)
                .infixl(op(".", new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$(rhs.$S()), scope), scope), 50)
                .postfix(op("--", new ScopedVarUnaryOperator(var::$dec, scope), scope), 100)
                .postfix(op("++", new ScopedVarUnaryOperator(var::$inc, scope), scope), 100)
                        //The newline operator
//                .infixl(((Parsers.or(term(";"),term("\n")).many1()).retn(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$plus(rhs), scope))), 1)
//                .prefix((term("\n").many1().retn(new ScopedVarUnaryOperator(v-> v, scope))), 1)
//                .postfix((term("\n").many1().retn(new ScopedVarUnaryOperator(v-> v, scope))), 1)
                .prefix(op("<>", new ScopedVarUnaryOperator(true, v -> $((Object) v.$()), scope), scope), 1000)
                .prefix(op("$", new ScopedVarUnaryOperator(v -> scope.get(v.$S()), scope), scope), 1000)
                .infixr(op("=", new ScopedVarBinaryOperator(false, (lhs, rhs) -> {
                    var evalled = $((Object) rhs.$());
                    scope.set(lhs.$S(), evalled);
                    return evalled;
                }, scope), scope), 2)
                .infixl(op(":=", new ScopedVarBinaryOperator(false, (lhs, rhs) -> {
                    scope.set(lhs.$S(), rhs);
                    return lhs;
                }, scope), scope), 2)
                .build(unit);
        ref.set(parser);

        scope.setParser(parser);

        return parser;
    }

    private static Parser<String> identifierTokenizer() {
        return Scanners.pattern(Patterns.isChar(CharPredicates.IS_ALPHA_).many1(), "identifier").source();

    }

    private static Parser<String> url() {
        return Scanners.pattern(Patterns.isChar(CharPredicates.IS_ALPHA_).many1()
                .next(Patterns.isChar(':')
                        .next(Patterns.among("-._~:/?#[]@!$&'()*+,;=%").or(Patterns.isChar(CharPredicates.IS_ALPHA_NUMERIC_)
                                ).many1()
                        )), "url").source();
    }

    public void addScope(ScriptScope scope) {
        scope.setParent(scopes.get(scopes.size() - 1));
        scopes.add(scope);
    }

    public ScriptScope currentScope() {
        return scopes.get(scopes.size() - 1);
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
        DollarStatic.context().setClassLoader(classLoader);
        scope.setDollarParser(this);
        Parser<?> parser = buildParser(new ScriptScope(scope, source));
        List<var> parse = (List<var>) parser.from(TOKENIZER, IGNORED).parse(source);
        return $(parse.stream().map(i -> i.$()).collect(Collectors.toList()));
    }


    public var parseExpression(ScriptScope scope, String source) throws IOException {
        DollarStatic.context().setClassLoader(classLoader);
        scope.setDollarParser(this);
        topLevelParser = expression(new ScriptScope(scope, source));
        return (var) topLevelParser.from(TOKENIZER, IGNORED).parse(source);
    }

    private Parser<?> buildParser(ScriptScope scope) {
        topLevelParser = script(scope);
        return topLevelParser;
    }


    public <T> T notFound(String variable, ScriptScope scriptScope) {
        throw new Error(variable + " not found ");
    }
}
