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

import dollar.internal.runtime.script.HasKeyword;
import dollar.internal.runtime.script.HasSymbol;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class Symbols {

    @NotNull
    public static final OpDef PIPE_OPERATOR = new OpDef("|", "pipe", "pipe",
                                                        "The Pipe operator exists to improve method chaining and is used in the " +
                                                                "form `funcA() | funcB` where the first expression is evaluated " +
                                                                "and then the result is passed to the second function and can be " +
                                                                "chained such as `funcA() | funcB | funcC`.",
                                                        false, true, null);
    @NotNull
    public static final OpDef WRITE = new OpDef(">>", null, "write-simple",
                                                "Performs a simple write to another data item, mostly used to write to a URI. ",
                                                false, true, null);
    @NotNull
    public static final OpDef READ = new OpDef("<<", null, "read-simple",
                                               "Performs a simple read from another data item, typically this is used with a URI" +
                                                       ".", false, true, null);
    @NotNull
    public static final OpDef CAUSES = new OpDef("=>", "causes", "causes",
                                                 "The causes operator is used to link a reactive expression to an imperative action. " +
                                                         "The left-hand-side is any expression and the right hand-side is any " +
                                                         "expression that will be evaluated when the left-hand-side is updated " +
                                                         "such as `a+b => {@@ a; @@ b}`.",
                                                 false, true, null);

    @NotNull
    public static final OpDef ASSERT = new OpDef(".:", "assert", "assert",
                                                 "The assertion opeartor is used to assert that an expression holds true. It is " +
                                                         "a reactive operator such that it is evaluated when the right-hand-side " +
                                                         "expression changes. so `.: a > 10` is asserting that a is **always** " +
                                                         "greater than 10. To avoid reactive behaviour use the fix operator such " +
                                                         "as `.: &a > 10` which means that when this statement is evaluated the " +
                                                         "value of a is compared with 10 - if __at this point__ it is not greater" +
                                                         " than 10 then the assertion will fail. ",
                                                 false, true, null);
    @NotNull
    public static final OpDef LT_EQUALS = new OpDef("<=", null, "less-than-equal",
                                                    "The standard `<=` operator, it uses Comparable#compareTo and will work with" +
                                                            " any Dollar data type, including strings, ranges, lists etc.",
                                                    false, true, null);
    @NotNull
    public static final OpDef GT_EQUALS = new OpDef(">=", null, "greater-than-equal", "The standard `>=` operator, it uses " +
                                                                                              "Comparable#compareTo and will work with" +
                                                                                              " any Dollar data type, including strings, ranges, lists etc.",
                                                    false, true, null);

    @NotNull
    public static final SymbolDef LEFT_PAREN = new SymbolDef("(", false);

    @NotNull
    public static final SymbolDef RIGHT_PAREN = new SymbolDef(")", false);

    @NotNull
    public static final OpDef DEC = new OpDef("--", "dec", "decrement",
                                              "Returns the right-hand-side decremented. Note the right-hand-side is not changed " +
                                                      "so `--a` does not not decrement `a`, it __returns__ `a` **decremented**",
                                              false,
                                              true, "('--'|'dec') <expression>");

    @NotNull
    public static final OpDef INC = new OpDef("++", "inc", "increment",
                                              "Returns the right-hand-side incremented. Note the right-hand-side is not changed so `--a` does not not decrement `a`, it __returns__ `a` **incremented**",
                                              false, true, "('++'|'inc') <expression>");

    @NotNull
    public static final OpDef DEFINITION = new OpDef(":=", null,
                                                     "declaration",
                                                     "Declares a variable to have a value, this is declarative and reactive such" +
                                                             " that saying `const a := b + 1` means that `a` always equals `b+1` " +
                                                             "no matter the value of b. The shorthand `def` is the same as `const " +
                                                             "<variable-name> :=` so `def a {b+1}` is the same as `const a := b +" +
                                                             " 1` but is syntactically better when declaring function like " +
                                                             "variables.",
                                                     false,
                                                     true,
                                                     " [export] [const] <variable-name> ':=' <expression> OR def <variable-name> <expression");
    @NotNull
    public static final OpDef FIX = new OpDef("&", "fix", "fix",
                                              "Converts a reactive expression into a fixed value. It fixes the value at the point the fix operator is executed. No reactive events will be passed from the right-hand-side expression.",
                                              false,
                                              false, "('&' | '--') fix <expression>");


    @NotNull
    public static final OpDef MEMBER = new OpDef(".", null, "member",
                                                 "The membership or `.` operator accesses the member of a map by it's key.", false,
                                                 true,
                                                 null);

    @NotNull
    public static final OpDef LT = new OpDef("<", "less-than", "less-than",
                                             "The standard `<` operator, it uses Comparable#compareTo and will work with" +
                                                     " any Dollar data type, including strings, ranges, lists etc.", false, true,
                                             null);

    @NotNull
    public static final OpDef GT = new OpDef(">", "greater-than", "greater-than",
                                             "The standard `>` operator, it uses Comparable#compareTo and will work with" +
                                                     " any Dollar data type, including strings, ranges, lists etc.", false, true,
                                             null);
    @NotNull
    public static final OpDef NOT = new OpDef("!", "not", "not", "Returns the negation of the right-hand-side expression.", false,
                                              true, "('!'|'not') <expression>");

    @NotNull
    public static final OpDef OR = new OpDef("||", "or", "or", "Returns the logical 'or' of two expressions, e.g. `a || b`. " +
                                                                       "Just like in Java it will shortcut, so that if the " +
                                                                       "left-hand-side is true the right-hand-side is never " +
                                                                       "evaluated.",
                                             false,
                                             true, "<expression> ('||'|'or') <expression>");

    @NotNull
    public static final OpDef AND = new OpDef("&&", "and", "and",
                                              "Returns the logical 'and' of two expressions, e.g. `a && b`. Just like in Java it will shortcut, " +
                                                      "so that if the left-hand-side is true the right-hand-side is never evaluated.",
                                              false, true, "<expression> ('&&'|'and') <expression>");

    @NotNull
    public static final OpDef MULTIPLY = new OpDef("*", "multiply", "multiply",
                                                   "Returns the product of two values. If the left-hand-side is scalar (non " +
                                                           "collection) then a straightforward multiplication will take place. If" +
                                                           " the left-hand-side is a collection and it is multiplied by `n`, e.g." +
                                                           " `{a=a+1} * 3` it will be added (`+`) to itself `n` times i.e. " +
                                                           "`{a=a+1} + {a=a+1} + {a=a+1}`.",
                                                   false, true, "<expression> '*'|'multiply'");

    @NotNull
    public static final OpDef DIVIDE = new OpDef("/", "divide", "divide", "divide", false, true, null);

    @NotNull
    public static final OpDef START = new OpDef("|>", "start", "start", "start", false, false, null);

    @NotNull
    public static final OpDef STOP = new OpDef("<|", "stop", "stop", "stop", false, false, null);

    @NotNull
    public static final OpDef PLUS = new OpDef("+", "plus", "plus", "plus", false, true, null);

    @NotNull
    public static final OpDef NEGATE = new OpDef("-", "negate", "negate", "negate", false, true, null);

    @NotNull
    public static final SymbolDef NEWLINE = new SymbolDef("\n", false);

    @NotNull
    public static final OpDef PAUSE = new OpDef("||>", "pause", "pause", "pause", false, false, null);

    @NotNull
    public static final OpDef UNPAUSE = new OpDef("<||", "unpause", "unpause", "unpause", false, false, null);

    @NotNull
    public static final OpDef DESTROY = new OpDef("<|||", "destroy", "destroy", "destroy", false, false, null);

    @NotNull
    public static final OpDef CREATE = new OpDef("|||>", "create", "create", "create", false, false, null);

    @NotNull
    public static final OpDef STATE = new OpDef("<|>", "state", "state", "state", false, false, null);

    @NotNull
    public static final OpDef DEFAULT = new OpDef(":-", "default", "default", "default", false, true, null);

    @NotNull
    public static final OpDef PRINT = new OpDef("@@", "print", "print", "print", false, true, null);

    @NotNull
    public static final OpDef PARALLEL = new OpDef("|:|", "parallel", "parallel", "parallel", false, true, null);

    @NotNull
    public static final OpDef SERIAL = new OpDef("|..|", "serial", "serial",
                                                 "serial", false, true, null);
    @NotNull
    public static final OpDef FORK = new OpDef("-<", "fork", "fork", "fork", false, true, null);

    @NotNull
    public static final OpDef RANGE = new OpDef("..", "range", "range", "range", false, true, null);

    @NotNull
    public static final OpDef ERROR = new OpDef("!?#*!", "error", "error", "error", false, true, null);

    @NotNull
    public static final OpDef SIZE = new OpDef("#", "size", "size", "size", false, true, null);

    @NotNull
    public static final OpDef MOD = new OpDef("%", "mod", "modulus", "modulus", false, true, null);

    @NotNull
    public static final OpDef ERR = new OpDef("??", "err", "err", "err", false, true, null);

    @NotNull
    public static final OpDef DEBUG = new OpDef("!!", "debug", "debug", "debug", false, true, null);

    @NotNull
    public static final OpDef ASSERT_EQ_REACT = new OpDef("<=>",
                                                          "assert-equals-reactive",
                                                          "assert-equals-reactive",
                                                          "assert-equals", false, true, null);
    @NotNull
    public static final OpDef ASSERT_EQ_UNREACT = new OpDef("<->", "assert-equals",
                                                            "assert-equals",
                                                            "assert-equals", false, true, null);
    @NotNull
    public static final OpDef EACH = new OpDef("=>>", "each", "each", "each", false, true, null);

    @NotNull
    public static final OpDef PUBLISH = new OpDef("*>", "publish", "publish", "publish", false, true, null);

    @NotNull
    public static final OpDef SUBSCRIBE = new OpDef("<*", "subscribe", "subscribe", "subscribe", false, true, null);

    @NotNull
    public static final OpDef EQUALITY = new OpDef("==", "equal", "equal", "equal", false, true, null);

    @NotNull
    public static final OpDef INEQUALITY_OPERATOR = new OpDef("!=", "not-equal", "not-equal",
                                                              "not-equal", false, true, null);
    @NotNull
    public static final OpDef ASSIGNMENT = new OpDef("=", null, "assign", "assign", false, true, null);

    @NotNull
    public static final SymbolDef DOLLAR = new SymbolDef("$", false);

    @NotNull
    public static final OpDef SUBSCRIBE_ASSIGN = new OpDef("*=", "subscribe-assign",
                                                           "subscribe-assign",
                                                           "subscribe-assign", false, true, null);
    @NotNull
    public static final OpDef LISTEN_ASSIGN = new OpDef("?=", null, "listen-assign",
                                                        "listen-assign", false, true, null);

    @NotNull
    public static final OpDef IN = new OpDef("€", "in", "in", "in", false, true, null);

    @NotNull
    public static final OpDef DRAIN = new OpDef("<-<", "drain", "drain", "drain", false, true, null);


    @NotNull
    public static final OpDef PAIR = new OpDef(":", "pair", "pair", "pair", false, true, null);

    @NotNull
    public static final OpDef IF_OPERATOR = new OpDef("???", "if", "if", "if", false, true, null);

    @NotNull
    public static final OpDef LISTEN = new OpDef("?", "listen", "listen", "listen", false, true, null);

    @NotNull
    public static final OpDef REDUCE = new OpDef(">>=", "reduce", "reduce", "reduce", false, true, null);

    @NotNull
    public static final OpDef CHOOSE = new OpDef("?*", "choose", "choose", "choose", false, true, null);

    @NotNull
    public static final OpDef ALL = new OpDef("<@", "all", "all", "all", false, true, null);

    @NotNull
    public static final OpDef ELSE = new OpDef("-:", "else", "else", "else", false, true, null);

    @NotNull
    public static final OpDef TRUTHY = new OpDef("~", "thruthy", "thruthy", "thruthy", false, true, null);

    @NotNull
    public static final OpDef RESERVED_OPERATOR_1 = new OpDef("...", null, null,
                                                              null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_2 = new OpDef("->", null, null,
                                                              null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_3 = new OpDef("<-", null, null,
                                                              null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_4 = new OpDef("?:", null, null,
                                                              null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_5 = new OpDef("@", null, null,
                                                              null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_6 = new OpDef("::", null, null,
                                                              null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_7 = new OpDef("&=", null, null,
                                                              null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_8 = new OpDef("+>", null, null,
                                                              null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_9 = new OpDef("<+", null, null,
                                                              null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_10 = new OpDef("|*", null, null,
                                                               null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_11 = new OpDef("&>", null, null,
                                                               null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_12 = new OpDef("<&", null, null,
                                                               null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_13 = new OpDef("?>", null, null,
                                                               null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_14 = new OpDef("<?", null, null,
                                                               null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_15 = new OpDef(">->", null,
                                                               null,
                                                               null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_16 = new OpDef("@>", null, null,
                                                               null, true, true, null);

    @NotNull
    public static final OpDef RESERVED_OPERATOR_17 = new OpDef("?..?", null,
                                                               null,
                                                               null, true, true, null);


    @NotNull
    public static final OpDef RESERVED_OPERATOR_18 = new OpDef("?$?", null,
                                                               null,
                                                               null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_19 = new OpDef("<$", null, null,
                                                               null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_20 = new OpDef("<=<", null,
                                                               null,
                                                               null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_21 = new OpDef("<++", null,
                                                               null,
                                                               null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_22 = new OpDef("-_-", null,
                                                               null,
                                                               null, true, true, null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_23 = new OpDef(">&", null, null,
                                                               null, true, true, null);
    @NotNull
    public static final List<String> SYMBOL_STRINGS;

    @NotNull
    public static final List<String> KEYWORD_STRINGS;

    @NotNull
    public static final SymbolDef COMMA = new SymbolDef(",", false);


    @NotNull
    public static final SymbolDef LEFT_BRACE = new SymbolDef("{", false);

    @NotNull
    public static final SymbolDef RIGHT_BRACE = new SymbolDef("}", false);

    @NotNull
    public static final SymbolDef SEMI_COLON = new SymbolDef(";", false);

    @NotNull
    public static final SymbolDef LEFT_BRACKET = new SymbolDef("[", false);

    @NotNull
    public static final SymbolDef RIGHT_BRACKET = new SymbolDef("]", false);


    @NotNull
    public static final KeywordDef TRUE = new KeywordDef("true", false, null);

    @NotNull
    public static final KeywordDef FALSE = new KeywordDef("false", false, null);

    @NotNull
    public static final KeywordDef YES = new KeywordDef("yes", false, null);
    @NotNull
    public static final KeywordDef NO = new KeywordDef("no", false, null);
    @NotNull
    public static final KeywordDef NULL = new KeywordDef("null", false, null);
    @NotNull
    public static final KeywordDef VOID = new KeywordDef("void", false, null);
    @NotNull
    public static final KeywordDef INFINITY = new KeywordDef("infinity", false, null);
    @NotNull
    public static final KeywordDef PURE = new KeywordDef("pure", false, null);
    @NotNull
    public static final KeywordDef EXPORT = new KeywordDef("export", false, null);
    @NotNull
    public static final KeywordDef CONST = new KeywordDef("const", false, null);
    @NotNull
    public static final KeywordDef VAR = new KeywordDef("var", false, null);
    @NotNull
    public static final KeywordDef VOLATILE = new KeywordDef("volatile", false, null);
    @NotNull
    public static final KeywordDef COLLECT = new KeywordDef("collect", false, null);
    @NotNull
    public static final KeywordDef UNTIL = new KeywordDef("until", false, null);
    @NotNull
    public static final KeywordDef UNLESS = new KeywordDef("unless", false, null);
    @NotNull
    public static final KeywordDef WHEN = new KeywordDef("when", false, null);
    @NotNull
    public static final KeywordDef IS = new KeywordDef("is", false, null);
    @NotNull
    public static final KeywordDef FOR = new KeywordDef("for", false, null);
    @NotNull
    public static final KeywordDef WHILE = new KeywordDef("while", false, null);
    @NotNull
    public static final KeywordDef AS = new KeywordDef("as", false, null);
    @NotNull
    public static final KeywordDef DEF = new KeywordDef("def", false, null);
    @NotNull
    public static final KeywordDef WITH = new KeywordDef("with", false, null);
    @NotNull
    public static final KeywordDef MODULE = new KeywordDef("module", false, null);
    @NotNull
    public static final KeywordDef EVERY = new KeywordDef("every", false, null);
    @NotNull
    public static final KeywordDef READ_KEYWORD = new KeywordDef("read", false, null);
    @NotNull
    public static final KeywordDef WRITE_KEYWORD = new KeywordDef("write", false, null);
    @NotNull
    public static final KeywordDef BLOCK = new KeywordDef("block", false, null);
    @NotNull
    public static final KeywordDef MUTATE = new KeywordDef("mutate", false, null);
    @NotNull
    public static final KeywordDef TO = new KeywordDef("to", false, null);
    @NotNull
    public static final KeywordDef FROM = new KeywordDef("from", false, null);
    @NotNull
    public static final List<KeywordDef> KEYWORDS;
    @NotNull
    public static final List<SymbolDef> SYMBOLS;
    @NotNull
    public static final List<OpDef> OPERATORS;
    @NotNull
    private static final List<? extends Comparable> tokens =
            asList(
                    //                   new OpDef("\u2357"),
                    ALL,
                    AND,
                    ASSERT_EQ_REACT,
                    ASSERT_EQ_UNREACT,
                    ASSERT,
                    ASSIGNMENT,
                    CAUSES,
                    CHOOSE,
                    PAIR,
                    SIZE,
                    CREATE,
                    DEBUG,
                    DEC,
                    DEFAULT,
                    DEFINITION,
                    DESTROY,
                    DIVIDE,
                    DOLLAR,
                    DRAIN,
                    EACH,
                    ELSE,
                    EQUALITY,
                    ERROR,
                    ERR,
                    FIX,
                    FORK,
                    GT_EQUALS,
                    GT,
                    IF_OPERATOR,
                    IN,
                    INC,
                    INEQUALITY_OPERATOR,
                    LEFT_BRACE,
                    LEFT_BRACKET,
                    LEFT_PAREN,
                    LT_EQUALS,
                    LT,
                    LISTEN_ASSIGN,
                    MEMBER,
                    NEGATE,
                    MOD,
                    MULTIPLY,
                    NEWLINE,
                    NOT,
                    OR,
                    PARALLEL,
                    PAUSE,
                    PIPE_OPERATOR,
                    PLUS,
                    PRINT,
                    PUBLISH,
                    RANGE,
                    READ,
                    REDUCE,
                    RIGHT_BRACE,
                    RIGHT_BRACKET,
                    RIGHT_PAREN,
                    SEMI_COLON,
                    SERIAL,
                    START,
                    STATE,
                    STOP,
                    SUBSCRIBE_ASSIGN,
                    SUBSCRIBE,
                    TRUTHY,
                    UNPAUSE,
                    WRITE,
                    LISTEN,
                    COMMA,
                    TRUE,
                    FALSE,
                    YES,
                    NO,
                    NULL,

                    VOID,
                    INFINITY,
                    PURE,
                    EXPORT,
                    CONST,
                    VAR,
                    VOLATILE,
                    COLLECT,
                    UNTIL,
                    UNLESS,
                    WHEN,
                    IS,
                    FOR,
                    WHILE,
                    AS,
                    DEF,
                    WITH,
                    MODULE,
                    EVERY,
                    READ_KEYWORD,
                    WRITE_KEYWORD,
                    BLOCK,
                    MUTATE,
                    TO,
                    FROM,

                    //Reserved Keywords
                    new KeywordDef("pluripotent", true, null),
                    new KeywordDef("readonly", true, null),
                    new KeywordDef("join", true, null),
                    new KeywordDef("fail", true, null),
                    new KeywordDef("switch", true, null),
                    new KeywordDef("dollar", true, null),
                    new KeywordDef("filter", true, null),
                    new KeywordDef("dispatch", true, null),
                    new KeywordDef("send", true, null),
                    new KeywordDef("emit", true, null),
                    new KeywordDef("import", true, null),
                    new KeywordDef("include", true, null),
                    new KeywordDef("impure", true, null),
                    new KeywordDef("variant", true, null),
                    new KeywordDef("vary", true, null),
                    new KeywordDef("varies", true, null),
                    new KeywordDef("lambda", true, null),
                    new KeywordDef("closure", true, null),
                    new KeywordDef("scope", true, null),
                    new KeywordDef("dump", true, null),
                    new KeywordDef("trace", true, null),
                    new KeywordDef("measure", true, null),
                    new KeywordDef("unit", true, null),
                    new KeywordDef("wait", true, null),
                    new KeywordDef("await", true, null),
                    new KeywordDef("save", true, null),
                    new KeywordDef("load", true, null),

                    //Reserved Operators
                    RESERVED_OPERATOR_1,
                    RESERVED_OPERATOR_2,
                    RESERVED_OPERATOR_3,
                    RESERVED_OPERATOR_4,
                    RESERVED_OPERATOR_5,
                    RESERVED_OPERATOR_6,
                    RESERVED_OPERATOR_7,
                    RESERVED_OPERATOR_8,
                    RESERVED_OPERATOR_9,
                    RESERVED_OPERATOR_10,
                    RESERVED_OPERATOR_11,
                    RESERVED_OPERATOR_12,
                    RESERVED_OPERATOR_13,
                    RESERVED_OPERATOR_14,
                    RESERVED_OPERATOR_15,
                    RESERVED_OPERATOR_16,
                    RESERVED_OPERATOR_17,
                    RESERVED_OPERATOR_18,
                    RESERVED_OPERATOR_19,
                    RESERVED_OPERATOR_20,
                    RESERVED_OPERATOR_21,
                    RESERVED_OPERATOR_22,
                    RESERVED_OPERATOR_23

            );

    static {

        OPERATORS = tokens.stream().filter(i -> i instanceof OpDef && !((OpDef) i).isReserved()).map(
                i -> (OpDef) i).sorted().collect(Collectors.toList());

        KEYWORDS = tokens.stream().filter(i -> i instanceof KeywordDef && !((KeywordDef) i).isReserved()).map(
                i -> (KeywordDef) i).sorted().collect(Collectors.toList());

        SYMBOLS = tokens.stream().filter(i -> i instanceof SymbolDef && !((SymbolDef) i).isReserved()).map(
                i -> (SymbolDef) i).sorted().collect(Collectors.toList());


        SYMBOL_STRINGS = tokens.stream().filter(symbol -> symbol instanceof HasSymbol).map(
                symbol -> ((HasSymbol) symbol).symbol()).sorted().collect(Collectors.toList());


        KEYWORD_STRINGS = tokens.stream().filter(symbol -> symbol instanceof HasKeyword).map(
                symbol -> ((HasKeyword) symbol).keyword()).filter(
                Objects::nonNull).sorted().collect(Collectors.toList());
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(@NotNull String[] args) {
        for (Comparable token : tokens) {
            if (token instanceof KeywordDef) {
                if (!((KeywordDef) token).isReserved()) {
                    System.out.println(((KeywordDef) token).asMarkdown());
                }
            }
            if (token instanceof OpDef) {
                if (!((OpDef) token).isReserved()) {
                    System.out.println(((OpDef) token).asMarkdown());
                }
            }
        }
    }


}
