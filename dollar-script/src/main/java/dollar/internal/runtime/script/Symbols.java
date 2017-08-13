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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class Symbols {

    @NotNull
    public static final OperatorDefinition PIPE_OPERATOR = new OperatorDefinition("|", "pipe", "pipe", "Pipe operator");
    @NotNull
    public static final OperatorDefinition WRITE = new OperatorDefinition(">>", "write-simple", "write-simple",
                                                                          "write-simple");
    @NotNull
    public static final OperatorDefinition READ = new OperatorDefinition("<<", "read-simple", "read-simple",
                                                                         "read-simple");
    @NotNull
    public static final OperatorDefinition CAUSES = new OperatorDefinition("=>", "causes", "causes", "causes");
    @NotNull
    public static final OperatorDefinition ASSERT = new OperatorDefinition(".:", "assert", "assert", "assert");
    @NotNull
    public static final OperatorDefinition LT_EQUALS = new OperatorDefinition("<=", "less-than-equal", "less-than-equal",
                                                                              "less-than-equal");
    @NotNull
    public static final OperatorDefinition GT_EQUALS = new OperatorDefinition(">=", "greater-than-equal",
                                                                              "greater-than-equal",
                                                                              "greater-than-equal");
    @NotNull
    public static final SymbolDefinition LEFT_PAREN = new SymbolDefinition("(");
    @NotNull
    public static final SymbolDefinition RIGHT_PAREN = new SymbolDefinition(")");
    @NotNull
    public static final OperatorDefinition DEC = new OperatorDefinition("--", "dec", "decrement", "decrement");
    @NotNull
    public static final OperatorDefinition INC = new OperatorDefinition("++", "inc", "increment", "increment");
    @NotNull
    public static final OperatorDefinition DEFINITION = new OperatorDefinition(":=", null, "declaration",
                                                                               "declaration");
    @NotNull
    public static final OperatorDefinition FIX_OPERATOR = new OperatorDefinition("&", "fix", "fix", "fix");
    @NotNull
    public static final OperatorDefinition FIX = new OperatorDefinition("&", "fix", "fix", "fix");
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_1 = new OperatorDefinition("...", null, null,
                                                                                        null);
    @NotNull
    public static final OperatorDefinition MEMBER = new OperatorDefinition(".", null, "member", "member");
    @NotNull
    public static final OperatorDefinition LT = new OperatorDefinition("<", "less-than", "less-than", "less-than");
    @NotNull
    public static final OperatorDefinition GT = new OperatorDefinition(">", "greater-than", "greater-than",
                                                                       "greater-than");
    @NotNull
    public static final OperatorDefinition NOT = new OperatorDefinition("!", "not", "not", "not");
    @NotNull
    public static final SymbolDefinition LEFT_BRACE = new SymbolDefinition("{");
    @NotNull
    public static final SymbolDefinition RIGHT_BRACE = new SymbolDefinition("}");
    @NotNull
    public static final SymbolDefinition SEMI_COLON = new SymbolDefinition(";");
    @NotNull
    public static final SymbolDefinition LEFT_BRACKET = new SymbolDefinition("[");
    @NotNull
    public static final SymbolDefinition RIGHT_BRACKET = new SymbolDefinition("]");
    @NotNull
    public static final OperatorDefinition OR = new OperatorDefinition("||", "or", "or", "or");
    @NotNull
    public static final OperatorDefinition AND = new OperatorDefinition("&&", "and", "and", "and");
    @NotNull
    public static final OperatorDefinition MULTIPLY = new OperatorDefinition("*", "multiply", "multiply", "multiply");
    @NotNull
    public static final OperatorDefinition DIVIDE = new OperatorDefinition("/", "divide", "divide", "divide");
    @NotNull
    public static final OperatorDefinition START = new OperatorDefinition("|>", "start", "start", "start");
    @NotNull
    public static final OperatorDefinition STOP = new OperatorDefinition("<|", "stop", "stop", "stop");
    @NotNull
    public static final OperatorDefinition PLUS = new OperatorDefinition("+", "plus", "plus", "plus");
    @NotNull
    public static final OperatorDefinition NEGATE = new OperatorDefinition("-", "negate", "negate", "negate");
    @NotNull
    public static final SymbolDefinition NEWLINE = new SymbolDefinition("\n");
    @NotNull
    public static final OperatorDefinition PAUSE = new OperatorDefinition("||>", "pause", "pause", "pause");
    @NotNull
    public static final OperatorDefinition UNPAUSE = new OperatorDefinition("<||", "unpause", "unpause", "unpause");
    @NotNull
    public static final OperatorDefinition DESTROY = new OperatorDefinition("<|||", "destroy", "destroy", "destroy");
    @NotNull
    public static final OperatorDefinition CREATE = new OperatorDefinition("|||>", "create", "create", "create");
    @NotNull
    public static final OperatorDefinition STATE = new OperatorDefinition("<|>", "state", "state", "state");
    @NotNull
    public static final OperatorDefinition DEFAULT = new OperatorDefinition(":-", "default", "default", "default");
    @NotNull
    public static final OperatorDefinition PRINT = new OperatorDefinition("@@", "print", "print", "print");
    @NotNull
    public static final OperatorDefinition PARALLEL = new OperatorDefinition("|:|", "parallel", "parallel", "parallel");
    @NotNull
    public static final OperatorDefinition SERIAL = new OperatorDefinition("|..|", "serial", "serial",
                                                                           "serial");
    @NotNull
    public static final OperatorDefinition FORK = new OperatorDefinition("-<", "fork", "fork", "fork");
    @NotNull
    public static final OperatorDefinition RANGE = new OperatorDefinition("..", "range", "range", "range");
    @NotNull
    public static final OperatorDefinition ERROR = new OperatorDefinition("!?#*!", "error", "error", "error");
    @NotNull
    public static final OperatorDefinition SIZE = new OperatorDefinition("#", "size", "size", "size");
    @NotNull
    public static final OperatorDefinition MOD = new OperatorDefinition("%", "mod", "modulus", "modulus");
    @NotNull
    public static final OperatorDefinition ERR = new OperatorDefinition("??", "err", "err", "err");
    @NotNull
    public static final OperatorDefinition DEBUG = new OperatorDefinition("!!", "debug", "debug", "debug");
    @NotNull
    public static final OperatorDefinition ASSERT_EQ_REACT = new OperatorDefinition("<=>",
                                                                                    "assert-equals-reactive",
                                                                                    "assert-equals-reactive",
                                                                                    "assert-equals");
    @NotNull
    public static final OperatorDefinition ASSERT_EQ_UNREACT = new OperatorDefinition("<->", "assert-equals",
                                                                                      "assert-equals",
                                                                                      "assert-equals");
    @NotNull
    public static final OperatorDefinition EACH = new OperatorDefinition("=>>", "each", "each", "each");
    @NotNull
    public static final OperatorDefinition PUBLISH = new OperatorDefinition("*>", "publish", "publish", "publish");
    @NotNull
    public static final OperatorDefinition SUBSCRIBE = new OperatorDefinition("<*", "subscribe", "subscribe", "subscribe");
    @NotNull
    public static final OperatorDefinition EQUALITY = new OperatorDefinition("==", "equal", "equal", "equal");
    @NotNull
    public static final OperatorDefinition INEQUALITY_OPERATOR = new OperatorDefinition("!=", "not-equal", "not-equal",
                                                                                        "not-equal");
    @NotNull
    public static final OperatorDefinition ASSIGNMENT = new OperatorDefinition("=", null, "assign", "assign");
    @NotNull
    public static final SymbolDefinition DOLLAR = new SymbolDefinition("$");
    @NotNull
    public static final OperatorDefinition SUBSCRIBE_ASSIGN = new OperatorDefinition("*=", "subscribe-assign",
                                                                                     "subscribe-assign",
                                                                                     "subscribe-assign");
    @NotNull
    public static final OperatorDefinition LISTEN_ASSIGN = new OperatorDefinition("?=", null, "listen-assign",
                                                                                  "listen-assign");
    @NotNull
    public static final OperatorDefinition CAUSE_OPERATOR = new OperatorDefinition("=>", null, "causes", "causes");
    @NotNull
    public static final OperatorDefinition IN = new OperatorDefinition("€", "in", "in", "in");
    @NotNull
    public static final OperatorDefinition DRAIN = new OperatorDefinition("<-<", "drain", "drain", "drain");
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_2 = new OperatorDefinition("->", null, null,
                                                                                        null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_3 = new OperatorDefinition("<-", null, null,
                                                                                        null);
    @NotNull
    public static final OperatorDefinition PAIR = new OperatorDefinition(":", "pair", "pair", "pair");
    @NotNull
    public static final OperatorDefinition IF_OPERATOR = new OperatorDefinition("???", "if", "if", "if");
    @NotNull
    public static final OperatorDefinition LISTEN = new OperatorDefinition("?", "listen", "listen", "listen");
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_4 = new OperatorDefinition("?:", null, null,
                                                                                        null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_5 = new OperatorDefinition("@", null, null,
                                                                                        null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_6 = new OperatorDefinition("::", null, null,
                                                                                        null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_7 = new OperatorDefinition("&=", null, null,
                                                                                        null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_8 = new OperatorDefinition("+>", null, null,
                                                                                        null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_9 = new OperatorDefinition("<+", null, null,
                                                                                        null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_10 = new OperatorDefinition("|*", null, null,
                                                                                         null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_11 = new OperatorDefinition("&>", null, null,
                                                                                         null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_12 = new OperatorDefinition("<&", null, null,
                                                                                         null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_13 = new OperatorDefinition("?>", null, null,
                                                                                         null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_14 = new OperatorDefinition("<?", null, null,
                                                                                         null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_15 = new OperatorDefinition(">->", null,
                                                                                         null,
                                                                                         null);
    @NotNull
    public static final OperatorDefinition REDUCE = new OperatorDefinition(">>=", "reduce", "reduce", "reduce");
    @NotNull
    public static final OperatorDefinition CHOOSE = new OperatorDefinition("?*", "choose", "choose", "choose");
    @NotNull
    public static final OperatorDefinition ALL = new OperatorDefinition("<@", "all", "all", "all");
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_16 = new OperatorDefinition("@>", null, null,
                                                                                         null);
    @NotNull
    public static final OperatorDefinition ELSE = new OperatorDefinition("-:", "else", "else", "else");
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_17 = new OperatorDefinition("?..?", null,
                                                                                         null,
                                                                                         null);
    @NotNull
    public static final OperatorDefinition TRUTHY = new OperatorDefinition("~", "thruthy", "thruthy", "thruthy");
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_18 = new OperatorDefinition("?$?", null,
                                                                                         null,
                                                                                         null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_19 = new OperatorDefinition("<$", null, null,
                                                                                         null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_20 = new OperatorDefinition("<=<", null,
                                                                                         null,
                                                                                         null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_21 = new OperatorDefinition("<++", null,
                                                                                         null,
                                                                                         null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_22 = new OperatorDefinition("-_-", null,
                                                                                         null,
                                                                                         null);
    @NotNull
    public static final OperatorDefinition RESERVED_OPERATOR_23 = new OperatorDefinition(">&", null, null,
                                                                                         null);
    @NotNull
    public static final List<String> SYMBOL_STRINGS = new ArrayList<>();
    @NotNull
    public static final List<String> KEYWORDS = new ArrayList<>();
    @NotNull
    public static final SymbolDefinition COMMA = new SymbolDefinition(",");
    @NotNull
    public static final KeywordDefinition TRUE = new KeywordDefinition("true");
    @NotNull
    public static final KeywordDefinition FALSE = new KeywordDefinition("false");
    @NotNull
    private static final List<Object> symbols =
            asList(
                    //                   new OperatorDefinition("\u2357"),
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
                    new KeywordDefinition("yes"),
                    new KeywordDefinition("no"),
                    new KeywordDefinition("null"),
                    new KeywordDefinition("pluripotent"),
                    new KeywordDefinition("void"),
                    new KeywordDefinition("infinity"),
                    new KeywordDefinition("pure"),
                    new KeywordDefinition("export"),
                    new KeywordDefinition("const"),
                    new KeywordDefinition("var"),
                    new KeywordDefinition("volatile"),
                    new KeywordDefinition("collect"),
                    new KeywordDefinition("until"),
                    new KeywordDefinition("unless"),
                    new KeywordDefinition("when"),
                    new KeywordDefinition("is"),
                    new KeywordDefinition("for"),
                    new KeywordDefinition("while"),
                    new KeywordDefinition("as"),
                    new KeywordDefinition("def"),
                    new KeywordDefinition("with"),
                    new KeywordDefinition("module"),
                    new KeywordDefinition("every"),
                    new KeywordDefinition("read"),
                    new KeywordDefinition("write"),
                    new KeywordDefinition("block"),
                    new KeywordDefinition("mutate"),
                    new KeywordDefinition("to"),
                    new KeywordDefinition("from"),


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
        try {
            for (Object symbol : symbols) {
                if (symbol instanceof HasSymbol)
                    SYMBOL_STRINGS.add(((HasSymbol) symbol).symbol());
            }
            System.err.println(SYMBOL_STRINGS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (Object symbol : symbols) {
                if (symbol instanceof HasKeyword) {
                    String keyword = ((HasKeyword) symbol).keyword();
                    if (keyword != null) {
                        KEYWORDS.add(keyword);
                    }
                }
            }
            System.err.println(KEYWORDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /* RESERVE ALL OF THESE
    "out", "err", "debug", "fix", "causes", "when", "if", "then", "for", "each", "fail",
                                "assert", "switch", "choose", "not", "dollar", "fork", "join", "print", "default",
                                "debug", "error", "filter", "every", "until", "unless", "and", "or",
                                "dispatch", "send", "publish", "subscribe", "emit", "drain",
                                "all", "import", "reduce", "truthy", "is", "else", "const", "in", "true", "false",
                                "yes", "no", "void", "error", "to", "from", "size", "as",
                                "while", "collect", "module", "include", "export", "with", "parallel", "serial",
                                "fork", "null", "volatile", "read", "write", "block", "mutate", "pure", "impure", "variant",
                                "variance", "pluripotent", "vary", "varies", "infinity", "var", "readonly", "def"
     */

}
