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

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class Symbols {

    @NotNull
    public static final OpDef PIPE_OPERATOR = new OpDef("|", "pipe", "pipe", "Pipe operator");
    @NotNull
    public static final OpDef WRITE = new OpDef(">>", null, "write-simple",
                                                "write-simple");
    @NotNull
    public static final OpDef READ = new OpDef("<<", null, "read-simple",
                                               "read-simple");
    @NotNull
    public static final OpDef CAUSES = new OpDef("=>", "causes", "causes", "causes");

    @NotNull
    public static final OpDef ASSERT = new OpDef(".:", "assert", "assert", "assert");
    @NotNull
    public static final OpDef LT_EQUALS = new OpDef("<=", null, "less-than-equal", "less-than-equal");
    @NotNull
    public static final OpDef GT_EQUALS = new OpDef(">=", null, "greater-than-equal", "greater-than-equal");

    @NotNull
    public static final SymbolDef LEFT_PAREN = new SymbolDef("(");

    @NotNull
    public static final SymbolDef RIGHT_PAREN = new SymbolDef(")");

    @NotNull
    public static final OpDef DEC = new OpDef("--", "dec", "decrement", "decrement");

    @NotNull
    public static final OpDef INC = new OpDef("++", "inc", "increment", "increment");

    @NotNull
    public static final OpDef DEFINITION = new OpDef(":=", null, "declaration",
                                                     "declaration");
    @NotNull
    public static final OpDef FIX = new OpDef("&", "fix", "fix", "fix");

    @NotNull
    public static final OpDef RESERVED_OPERATOR_1 = new OpDef("...", null, null,
                                                              null);
    @NotNull
    public static final OpDef MEMBER = new OpDef(".", null, "member", "member");

    @NotNull
    public static final OpDef LT = new OpDef("<", "less-than", "less-than", "less-than");

    @NotNull
    public static final OpDef GT = new OpDef(">", "greater-than", "greater-than",
                                             "greater-than");
    @NotNull
    public static final OpDef NOT = new OpDef("!", "not", "not", "not");

    @NotNull
    public static final SymbolDef LEFT_BRACE = new SymbolDef("{");

    @NotNull
    public static final SymbolDef RIGHT_BRACE = new SymbolDef("}");

    @NotNull
    public static final SymbolDef SEMI_COLON = new SymbolDef(";");

    @NotNull
    public static final SymbolDef LEFT_BRACKET = new SymbolDef("[");

    @NotNull
    public static final SymbolDef RIGHT_BRACKET = new SymbolDef("]");

    @NotNull
    public static final OpDef OR = new OpDef("||", "or", "or", "or");

    @NotNull
    public static final OpDef AND = new OpDef("&&", "and", "and", "and");

    @NotNull
    public static final OpDef MULTIPLY = new OpDef("*", "multiply", "multiply", "multiply");

    @NotNull
    public static final OpDef DIVIDE = new OpDef("/", "divide", "divide", "divide");

    @NotNull
    public static final OpDef START = new OpDef("|>", "start", "start", "start");

    @NotNull
    public static final OpDef STOP = new OpDef("<|", "stop", "stop", "stop");

    @NotNull
    public static final OpDef PLUS = new OpDef("+", "plus", "plus", "plus");

    @NotNull
    public static final OpDef NEGATE = new OpDef("-", "negate", "negate", "negate");

    @NotNull
    public static final SymbolDef NEWLINE = new SymbolDef("\n");

    @NotNull
    public static final OpDef PAUSE = new OpDef("||>", "pause", "pause", "pause");

    @NotNull
    public static final OpDef UNPAUSE = new OpDef("<||", "unpause", "unpause", "unpause");

    @NotNull
    public static final OpDef DESTROY = new OpDef("<|||", "destroy", "destroy", "destroy");

    @NotNull
    public static final OpDef CREATE = new OpDef("|||>", "create", "create", "create");

    @NotNull
    public static final OpDef STATE = new OpDef("<|>", "state", "state", "state");

    @NotNull
    public static final OpDef DEFAULT = new OpDef(":-", "default", "default", "default");

    @NotNull
    public static final OpDef PRINT = new OpDef("@@", "print", "print", "print");

    @NotNull
    public static final OpDef PARALLEL = new OpDef("|:|", "parallel", "parallel", "parallel");

    @NotNull
    public static final OpDef SERIAL = new OpDef("|..|", "serial", "serial",
                                                 "serial");
    @NotNull
    public static final OpDef FORK = new OpDef("-<", "fork", "fork", "fork");

    @NotNull
    public static final OpDef RANGE = new OpDef("..", "range", "range", "range");

    @NotNull
    public static final OpDef ERROR = new OpDef("!?#*!", "error", "error", "error");

    @NotNull
    public static final OpDef SIZE = new OpDef("#", "size", "size", "size");

    @NotNull
    public static final OpDef MOD = new OpDef("%", "mod", "modulus", "modulus");

    @NotNull
    public static final OpDef ERR = new OpDef("??", "err", "err", "err");

    @NotNull
    public static final OpDef DEBUG = new OpDef("!!", "debug", "debug", "debug");

    @NotNull
    public static final OpDef ASSERT_EQ_REACT = new OpDef("<=>",
                                                          "assert-equals-reactive",
                                                          "assert-equals-reactive",
                                                          "assert-equals");
    @NotNull
    public static final OpDef ASSERT_EQ_UNREACT = new OpDef("<->", "assert-equals",
                                                            "assert-equals",
                                                            "assert-equals");
    @NotNull
    public static final OpDef EACH = new OpDef("=>>", "each", "each", "each");

    @NotNull
    public static final OpDef PUBLISH = new OpDef("*>", "publish", "publish", "publish");

    @NotNull
    public static final OpDef SUBSCRIBE = new OpDef("<*", "subscribe", "subscribe", "subscribe");

    @NotNull
    public static final OpDef EQUALITY = new OpDef("==", "equal", "equal", "equal");

    @NotNull
    public static final OpDef INEQUALITY_OPERATOR = new OpDef("!=", "not-equal", "not-equal",
                                                              "not-equal");
    @NotNull
    public static final OpDef ASSIGNMENT = new OpDef("=", null, "assign", "assign");

    @NotNull
    public static final SymbolDef DOLLAR = new SymbolDef("$");

    @NotNull
    public static final OpDef SUBSCRIBE_ASSIGN = new OpDef("*=", "subscribe-assign",
                                                           "subscribe-assign",
                                                           "subscribe-assign");
    @NotNull
    public static final OpDef LISTEN_ASSIGN = new OpDef("?=", null, "listen-assign",
                                                        "listen-assign");

    @NotNull
    public static final OpDef IN = new OpDef("€", "in", "in", "in");

    @NotNull
    public static final OpDef DRAIN = new OpDef("<-<", "drain", "drain", "drain");

    @NotNull
    public static final OpDef RESERVED_OPERATOR_2 = new OpDef("->", null, null,
                                                              null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_3 = new OpDef("<-", null, null,
                                                              null);
    @NotNull
    public static final OpDef PAIR = new OpDef(":", "pair", "pair", "pair");

    @NotNull
    public static final OpDef IF_OPERATOR = new OpDef("???", "if", "if", "if");

    @NotNull
    public static final OpDef LISTEN = new OpDef("?", "listen", "listen", "listen");

    @NotNull
    public static final OpDef RESERVED_OPERATOR_4 = new OpDef("?:", null, null,
                                                              null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_5 = new OpDef("@", null, null,
                                                              null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_6 = new OpDef("::", null, null,
                                                              null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_7 = new OpDef("&=", null, null,
                                                              null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_8 = new OpDef("+>", null, null,
                                                              null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_9 = new OpDef("<+", null, null,
                                                              null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_10 = new OpDef("|*", null, null,
                                                               null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_11 = new OpDef("&>", null, null,
                                                               null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_12 = new OpDef("<&", null, null,
                                                               null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_13 = new OpDef("?>", null, null,
                                                               null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_14 = new OpDef("<?", null, null,
                                                               null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_15 = new OpDef(">->", null,
                                                               null,
                                                               null);
    @NotNull
    public static final OpDef REDUCE = new OpDef(">>=", "reduce", "reduce", "reduce");

    @NotNull
    public static final OpDef CHOOSE = new OpDef("?*", "choose", "choose", "choose");

    @NotNull
    public static final OpDef ALL = new OpDef("<@", "all", "all", "all");

    @NotNull
    public static final OpDef RESERVED_OPERATOR_16 = new OpDef("@>", null, null,
                                                               null);
    @NotNull
    public static final OpDef ELSE = new OpDef("-:", "else", "else", "else");

    @NotNull
    public static final OpDef RESERVED_OPERATOR_17 = new OpDef("?..?", null,
                                                               null,
                                                               null);
    @NotNull
    public static final OpDef TRUTHY = new OpDef("~", "thruthy", "thruthy", "thruthy");

    @NotNull
    public static final OpDef RESERVED_OPERATOR_18 = new OpDef("?$?", null,
                                                               null,
                                                               null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_19 = new OpDef("<$", null, null,
                                                               null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_20 = new OpDef("<=<", null,
                                                               null,
                                                               null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_21 = new OpDef("<++", null,
                                                               null,
                                                               null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_22 = new OpDef("-_-", null,
                                                               null,
                                                               null);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_23 = new OpDef(">&", null, null,
                                                               null);
    @NotNull
    public static final List<String> SYMBOL_STRINGS = new ArrayList<>();

    @NotNull
    public static final List<String> KEYWORDS = new ArrayList<>();

    @NotNull
    public static final SymbolDef COMMA = new SymbolDef(",");

    @NotNull
    public static final KeywordDef TRUE = new KeywordDef("true");

    @NotNull
    public static final KeywordDef FALSE = new KeywordDef("false");

    @NotNull
    public static final KeywordDef YES = new KeywordDef("yes");
    @NotNull
    public static final KeywordDef NO = new KeywordDef("no");
    @NotNull
    public static final KeywordDef NULL = new KeywordDef("null");
    @NotNull
    public static final KeywordDef VOID = new KeywordDef("void");
    @NotNull
    public static final KeywordDef INFINITY = new KeywordDef("infinity");
    @NotNull
    public static final KeywordDef PURE = new KeywordDef("pure");
    @NotNull
    public static final KeywordDef EXPORT = new KeywordDef("export");
    @NotNull
    public static final KeywordDef CONST = new KeywordDef("const");
    @NotNull
    public static final KeywordDef VAR = new KeywordDef("var");
    @NotNull
    public static final KeywordDef VOLATILE = new KeywordDef("volatile");
    @NotNull
    public static final KeywordDef COLLECT = new KeywordDef("collect");
    @NotNull
    public static final KeywordDef UNTIL = new KeywordDef("until");
    @NotNull
    public static final KeywordDef UNLESS = new KeywordDef("unless");
    @NotNull
    public static final KeywordDef WHEN = new KeywordDef("when");
    @NotNull
    public static final KeywordDef IS = new KeywordDef("is");
    @NotNull
    public static final KeywordDef FOR = new KeywordDef("for");
    @NotNull
    public static final KeywordDef WHILE = new KeywordDef("while");
    @NotNull
    public static final KeywordDef AS = new KeywordDef("as");
    @NotNull
    public static final KeywordDef DEF = new KeywordDef("def");
    @NotNull
    public static final KeywordDef WITH = new KeywordDef("with");
    @NotNull
    public static final KeywordDef MODULE = new KeywordDef("module");
    @NotNull
    public static final KeywordDef EVERY = new KeywordDef("every");
    @NotNull
    public static final KeywordDef READ_KEYWORD = new KeywordDef("read");
    @NotNull
    public static final KeywordDef WRITE_KEYWORD = new KeywordDef("write");
    @NotNull
    public static final KeywordDef BLOCK = new KeywordDef("block");
    @NotNull
    public static final KeywordDef MUTATE = new KeywordDef("mutate");
    @NotNull
    public static final KeywordDef TO = new KeywordDef("to");
    @NotNull
    public static final KeywordDef FROM = new KeywordDef("from");
    @NotNull
    private static final List<Object> tokens =
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
                    new KeywordDef("pluripotent"),
                    new KeywordDef("readonly"),
                    new KeywordDef("join"),
                    new KeywordDef("fail"),
                    new KeywordDef("switch"),
                    new KeywordDef("dollar"),
                    new KeywordDef("filter"),
                    new KeywordDef("dispatch"),
                    new KeywordDef("send"),
                    new KeywordDef("emit"),
                    new KeywordDef("import"),
                    new KeywordDef("include"),
                    new KeywordDef("impure"),
                    new KeywordDef("variant"),
                    new KeywordDef("vary"),
                    new KeywordDef("varies"),
                    new KeywordDef("lambda"),
                    new KeywordDef("closure"),
                    new KeywordDef("scope"),
                    new KeywordDef("dump"),
                    new KeywordDef("trace"),
                    new KeywordDef("measure"),
                    new KeywordDef("unit"),

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
        try {
            for (Object symbol : tokens) {
                if (symbol instanceof HasSymbol)
                    SYMBOL_STRINGS.add(((HasSymbol) symbol).symbol());
            }
//            System.err.println(SYMBOL_STRINGS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (Object symbol : tokens) {
                if (symbol instanceof HasKeyword) {
                    String keyword = ((HasKeyword) symbol).keyword();
                    if (keyword != null) {
                        KEYWORDS.add(keyword);
                    }
                }
            }
//            System.err.println(KEYWORDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
