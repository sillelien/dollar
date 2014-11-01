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
import java.util.function.Function;
import java.util.stream.Collectors;

import static me.neilellis.dollar.DollarStatic.$;
import static me.neilellis.dollar.DollarStatic.$void;
import static me.neilellis.dollar.types.DollarFactory.fromLambda;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarParser {


    static final Terminals OPERATORS = Terminals.operators("|", ">>", "<<", "->", "=>", "<=", "<-", "(", ")", "--", "++", ".", ":", "<", ">", "?", "?:", "!", "!!", ">&", "{", "}", ",", "$", "=", ";", "[", "]", "`", "``", "??", "!!", "*>", "==", "!=", "+", "-", "\n", "$(", "${", ":=", "&", "&=", "<>", "+>", "<+", "*>", "<*", "*|", "|*", "*|*", "|>", "<|", "&>", "?->", "<=>");

    static final Terminals KEYWORDS = Terminals.operators("fix", "effects", "when", "if", "then", "for", "each", "fail", "assert", "switch", "choose", "not", "dollar", "fork", "join", "print", "default", "debug", "error");

    static final Parser<?> TOKENIZER =
            Parsers.or(url(), OPERATORS.tokenizer(), Terminals.StringLiteral.DOUBLE_QUOTE_TOKENIZER, Terminals.StringLiteral.SINGLE_QUOTE_TOKENIZER, Terminals.DecimalLiteral.TOKENIZER, Terminals.IntegerLiteral.TOKENIZER, Parsers.longest(KEYWORDS.tokenizer(), Terminals.Identifier.TOKENIZER));

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
    static final Parser<?> COMMA_OR_NEWLINE_TERMINATOR = Parsers.or(term(","), term("\n")).many1();
    static final Parser<?> SEMICOLON_TERMINATOR = Parsers.or(term(";"), term("\n"));
    private final ClassLoader classLoader;
    private CopyOnWriteArrayList<ScriptScope> scopes = new CopyOnWriteArrayList<>();
    private Parser<?> topLevelParser;

    public DollarParser() {
        classLoader = DollarParser.class.getClassLoader();
    }

    public DollarParser(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    static Parser<?> term(String name, String keyword) {
        return OPERATORS.token(name).or(KEYWORDS.token(keyword));
    }

    static Parser<?> term(String name) {
        return OPERATORS.token(name);
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

    private static Parser<var> parameters(Parser<var> expression) {
        Parser<List<var>> sequence = term("(").next(expression.sepBy(COMMA_TERMINATOR)).followedBy(term(")"));
        return sequence.map(DollarStatic::$);
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

    private  Parser<var> list(Parser<var> expression, ScriptScope scope) {
        Parser<List<var>> sequence = termFollowedByNewlines("[").next(expression.sepBy(COMMA_OR_NEWLINE_TERMINATOR)).followedBy(termPreceededByNewlines("]"));
        return sequence.map(i -> withinNewScope(scope, newScope -> DollarStatic.$(i)));
    }

    private  Parser<var> map(Parser<var> expression, ScriptScope scope) {
        Parser<List<var>> sequence = termFollowedByNewlines("{").next(expression.sepBy1(COMMA_TERMINATOR)).followedBy(termPreceededByNewlines("}"));
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

    private  Parser<var> block(Parser<var> parentParser, ScriptScope scope) {
        Parser.Reference<var> ref = Parser.newReference();

        Parser<List<var>> listParser = (Parsers.or(parentParser, ref.lazy().between(termFollowedByNewlines("{"), termPreceededByNewlines("}"))).followedBy(SEMICOLON_TERMINATOR)).many1();

        //Now we do the complex part, the following will only return the last value in the
        //block when the block is evaluated, but it will trigger execution of the rest.
        //This gives it functionality like a conventional function in imperative languages
        Parser<var> or = listParser.map(l -> fromLambda(delayed -> withinNewScope(scope, newScope -> {
                    if (l.size() > 0) {
                        for (int i = 0; i < l.size() - 1; i++) {
                            l.get(i).$();
                        }

                        return l.get(l.size() - 1);
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
                    Parser<var> block = block(ref.lazy(), newScope).between(termFollowedByNewlines("{"), termPreceededByNewlines("}"));
                    Parser<var> expression = expression(newScope).map(i -> i != null ? i : null);
                    Parser<List<var>> parser = (TERMINATOR_SYMBOL.optional()).next(Parsers.or(expression, block).followedBy(TERMINATOR_SYMBOL).map(i -> $((Object) i.$())).many1());
                    ref.set(parser.map(DollarStatic::$));
                    return parser;

                }

        );
    }

    private Parser<var> expression(ScriptScope scope) {

        Parser.Reference<var> ref = Parser.newReference();
        Parser<var> main = ref.lazy().between(term("("), term(")")).or(Parsers.or(list(ref.lazy(), scope), map(ref.lazy(), scope).map(DollarStatic::$), URL, DECIMAL_LITERAL, INTEGER_LITERAL, STRING_LITERAL, IDENTIFIER)).or(block(ref.lazy(), scope).between(termFollowedByNewlines("{"), termPreceededByNewlines("}")));

        Parser<var> unit = Parsers.array(main, parameters(ref.lazy()).optional()).map(objects -> withinNewScope(scope, newScope -> {
            if (objects.length == 1 || objects[1] == null) {
                return (var) objects[0];
            } else {
                List<var> params = ((var) objects[1]).toList();
                int count = 0;
                for (var param : params) {
                    newScope.set(String.valueOf(++count), param);
                }
                var result = newScope.get(objects[0].toString());
//                var result = $((Object) ((var) objects[0]).$());
                System.out.println("Result " + result.$S());
                return $((Object) result.$());
            }
        }));
//        ScopedVarBinaryOperator value = new ScopedVarBinaryOperator((lhs, rhs) -> rhs, scope);
        Parser<var> parser = new OperatorTable<var>()
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(!lhs.equals(rhs))), "!="), 100)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(lhs.equals(rhs))
                ), "=="), 100)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> withinNewScope(scope, newScope -> {
                    newScope.set("1", lhs);
                    if (!rhs.isString()) {
                        var result = $((Object) rhs.$());
                        return result;
                    } else {
                        return lhs.$pipe(rhs.$S());
                    }
                })
                ), "|"), 80)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$plus(rhs)), "+"), 200)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$minus(rhs)), "-"), 200)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> $(lhs.$S(), rhs)), ":"), 30)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> scope.set(lhs.$S(), $().$read(new File(rhs.$S())))), "<"), 50)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$write(new File(rhs.$S()))), ">"), 50)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.isTrue() ? rhs : lhs), "->"), 50)
                .prefix(op(new ScopedVarUnaryOperator(false, i -> {
                    i.out();
                    return $void();
                }), ">>"), 5)
                .prefix(op(new ScopedVarUnaryOperator(false, i -> {
                    i.out();
                    return $void();/*reserved for debug instead of stdout messages*/
                }), "!!"), 5)
                .prefix(op(new ScopedVarUnaryOperator(false, i -> {
                    i.err();
                    return $void();
                }), "??"), 5)
                .prefix(op(new ScopedVarUnaryOperator(false, v -> {
                    if (v.isTrue()) {
                        return v;
                    } else {
                        throw new AssertionError();
                    }
                }), "=>"), 5)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> {
                    if (lhs.equals(rhs)) {
                        return lhs;
                    } else {
                        throw new AssertionError(lhs.$S() + " != " + rhs.$S());
                    }
                }), "<=>"), 5)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$save(rhs.$S())), "|>"), 50)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$load(rhs.$S())), "<|"), 50)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> rhs.$receive(lhs)), "+>"), 50)
//                .infixl(op("&>", new ScopedVarBinaryOperator((lhs, rhs) -> {
//                    lhs.$dispatch(rhs.$S());
//                    return lhs;
//                }, scope), scope), 50)
//                .infixl(op("+>", new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$send(rhs.$S()), scope), scope), 50)
//                .infixl(op("*>", new ScopedVarBinaryOperator((lhs, rhs) -> {
//                    lhs.$publish(rhs.$S());
//                    return lhs;
//                }, scope), scope), 50)
                .infixl(op(new ListenOperator(scope), "?", "effects"), 50)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$choose(rhs)), "?->", "choose"), 50)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$default(rhs)), "?:", "default"), 50)
                .infixl(op(new ScopedVarBinaryOperator((lhs, rhs) -> lhs.$(rhs.$S())), "."), 200)
                .postfix(op(new ScopedVarUnaryOperator(scope, var::$dec), "--"), 100)
                .postfix(op(new ScopedVarUnaryOperator(scope, var::$inc), "++"), 100)
                .prefix(op(new ScopedVarUnaryOperator(true, v -> $((Object) v.$())), "<>", "fix"), 1000)
                .prefix(op(new VariableOperator(scope), "$"), 1000)
                .infixr(op(new AssignmentOperator(scope), "="), 2)
                .infixl(op(new ScopedVarBinaryOperator(false, (lhs, rhs) -> {
                    scope.set(lhs.$S(), rhs);
                    return lhs;
                }), ":="), 2)
                .build(unit);
        ref.set(parser);

        scope.setParser(parser);

        return parser;
    }

    public void addScope(ScriptScope scope) {
        scopes.add(scope);
    }

    public <T> T withinNewScope(ScriptScope currentScope, Function<ScriptScope, T> r) {
        ScriptScope newScope = new ScriptScope(currentScope, currentScope.getSource());
        addScope(newScope);
        try {
            return r.apply(newScope);
        } catch (Exception e) {
            throw new Error(e);
        } finally {
            ScriptScope poppedScope = endScope();
            if (poppedScope != newScope) {
                throw new IllegalStateException("Popped wrong scope");
            }
        }
    }

    public ScriptScope endScope() {
        return scopes.remove(scopes.size() - 1);
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
        addScope(new ScriptScope(scope, source));
        try {
            DollarStatic.context().setClassLoader(classLoader);
            scope.setDollarParser(this);
            Parser<?> parser = buildParser(new ScriptScope(scope, source));
            List<var> parse = (List<var>) parser.from(TOKENIZER, IGNORED).parse(source);
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
            return (var) topLevelParser.from(TOKENIZER, IGNORED).parse(source);
        } finally {
            endScope();
        }
    }

    private Parser<?> buildParser(ScriptScope scope) {
        topLevelParser = script(scope);
        return topLevelParser;
    }


    public <T> T notFound(String variable) {
        throw new Error(variable + " not found in " + currentScope());
    }
}
