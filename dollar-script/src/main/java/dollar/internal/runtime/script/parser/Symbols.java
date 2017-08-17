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

import com.google.common.io.Files;
import dollar.internal.runtime.script.HasKeyword;
import dollar.internal.runtime.script.HasSymbol;
import dollar.internal.runtime.script.OperatorPriority;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static dollar.internal.runtime.script.OperatorPriority.*;
import static dollar.internal.runtime.script.parser.OpDefType.*;
import static java.util.Arrays.asList;

public class Symbols {

    @NotNull
    public static final OpDef PIPE_OPERATOR = new OpDef(BINARY, "|", "pipe", "pipe",
                                                        false, true, null, PIPE_PRIORITY);
    @NotNull
    public static final OpDef WRITE_SIMPLE = new OpDef(BINARY, ">>", null, "write-simple",
                                                       false, true, null, OUTPUT_PRIORITY);
    @NotNull
    public static final OpDef READ_SIMPLE = new OpDef(PREFIX, "<<", null, "read-simple",
                                                      false, true, null, OUTPUT_PRIORITY);
    @NotNull
    public static final OpDef CAUSES = new OpDef(BINARY, "=>", "causes", "causes",
                                                 false, true, null, CONTROL_FLOW_PRIORITY);

    @NotNull
    public static final OpDef ASSERT = new OpDef(PREFIX, ".:", "assert", "assert",
                                                 false, true, null, LINE_PREFIX_PRIORITY);
    @NotNull
    public static final OpDef LT_EQUALS = new OpDef(BINARY, "<=", null, "less-than-equal",
                                                    false, true, null, EQ_PRIORITY);
    @NotNull
    public static final OpDef GT_EQUALS = new OpDef(BINARY, ">=", null, "greater-than-equal",
                                                    false, true, null, EQ_PRIORITY);

    @NotNull
    public static final SymbolDef LEFT_PAREN = new SymbolDef("(", false);

    @NotNull
    public static final SymbolDef RIGHT_PAREN = new SymbolDef(")", false);

    @NotNull
    public static final OpDef DEC = new OpDef(PREFIX, "--", "dec", "decrement",
                                              false,
                                              true, null, INC_DEC_PRIORITY);

    @NotNull
    public static final OpDef INC = new OpDef(PREFIX, "++", "inc", "increment",
                                              false, true, null, INC_DEC_PRIORITY);

    @NotNull
    public static final OpDef DEFINITION = new OpDef(OpDefType.ASSIGNMENT, ":=", null,
                                                     "declaration",
                                                     false,
                                                     true,
                                                     "( [export] [const] <variable-name> ':=' <expression>) | ( def " +
                                                             "<variable-name> <expression )",
                                                     ASSIGNMENT_PRIORITY);
    @NotNull
    public static final OpDef FIX = new OpDef(PREFIX, "&", "fix", "fix",
                                              false,
                                              false, null, FIX_PRIORITY);


    @NotNull
    public static final OpDef MEMBER = new OpDef(BINARY, ".", null, "member",
                                                 false,
                                                 true,
                                                 null, MEMBER_PRIORITY);

    @NotNull
    public static final OpDef LT = new OpDef(BINARY, "<", "less-than", "less-than",
                                             false, true,
                                             null, COMP_PRIORITY);

    @NotNull
    public static final OpDef GT = new OpDef(BINARY, ">", "greater-than", "greater-than",
                                             false, true,
                                             null, COMP_PRIORITY);
    @NotNull
    public static final OpDef NOT = new OpDef(PREFIX, "!", "not", "not", false,
                                              true, null, UNARY_PRIORITY);

    @NotNull
    public static final OpDef OR = new OpDef(BINARY, "||", "or", "or",
                                             false,
                                             true, null,
                                             OperatorPriority.LOGICAL_OR_PRIORITY);

    @NotNull
    public static final OpDef AND = new OpDef(BINARY, "&&", "and", "and",
                                              false, true,
                                              null, OperatorPriority.AND_PRIORITY);

    @NotNull
    public static final OpDef MULTIPLY = new OpDef(BINARY, "*", "multiply", "multiply",
                                                   false, true,
                                                   null, MULTIPLY_DIVIDE_PRIORITY);

    @NotNull
    public static final OpDef DIVIDE = new OpDef(BINARY, "/", "divide", "divide",
                                                 false, true,
                                                 null, MULTIPLY_DIVIDE_PRIORITY);
    @NotNull
    public static final OpDef NEGATE = new OpDef(PREFIX, "-", "negate", "negate",
                                                 false, true,
                                                 null, PLUS_MINUS_PRIORITY);

    @NotNull
    public static final OpDef MINUS = new OpDef(BINARY, "-", "minus", "minus",
                                                false, true,
                                                null, PLUS_MINUS_PRIORITY);


    @NotNull
    public static final OpDef START = new OpDef(PREFIX, "|>", "start", "start",
                                                false, true,
                                                "('|>'|'start') <expression>", SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef STOP = new OpDef(PREFIX, "<|", "stop", "stop",
                                               false, true,
                                               null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef PAUSE = new OpDef(PREFIX, "||>", "pause", "pause",
                                                false, true,
                                                null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef UNPAUSE = new OpDef(PREFIX, "<||", "unpause", "unpause",
                                                  false, true,
                                                  null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef DESTROY = new OpDef(PREFIX, "<|||", "destroy", "destroy",
                                                  false, true,
                                                  null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef CREATE = new OpDef(PREFIX, "|||>", "create", "create",
                                                 false, true,
                                                 null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef STATE = new OpDef(PREFIX, "<|>", "state", "state",
                                                false, true,
                                                null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef PLUS = new OpDef(BINARY, "+", "plus", "plus",
                                               false, true,
                                               null, PLUS_MINUS_PRIORITY);

    @NotNull
    public static final OpDef DEFAULT = new OpDef(BINARY, ":-", "default", "default",
                                                  false,
                                                  true,
                                                  null,
                                                  MEMBER_PRIORITY);

    @NotNull
    public static final OpDef PRINT = new OpDef(PREFIX, "@@", "print", "print",
                                                false, true,
                                                null, LINE_PREFIX_PRIORITY);

    @NotNull
    public static final OpDef PARALLEL = new OpDef(PREFIX, "|:|", "parallel", "parallel",
                                                   false, false,
                                                   null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef SERIAL = new OpDef(PREFIX, "|..|", "serial", "serial",
                                                 false, false,
                                                 null, SIGNAL_PRIORITY);
    @NotNull
    public static final OpDef FORK = new OpDef(PREFIX, "-<", "fork", "fork",
                                               false,
                                               true,
                                               null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef RANGE = new OpDef(BINARY, "..", "range", "range",
                                                false, true,
                                                null,
                                                OperatorPriority.RANGE_PRIORITY);

    @NotNull
    public static final OpDef ERROR = new OpDef(PREFIX, "?->", "error", "error",
                                                false, true,
                                                null,
                                                LINE_PREFIX_PRIORITY);

    @NotNull
    public static final OpDef SIZE = new OpDef(PREFIX, "#", "size", "size",
                                               false, true,
                                               null, UNARY_PRIORITY);

    @NotNull
    public static final OpDef MOD = new OpDef(BINARY, "%", "mod", "modulus",
                                              false, true,
                                              null, MULTIPLY_DIVIDE_PRIORITY);

    @NotNull
    public static final OpDef ERR = new OpDef(PREFIX, "??", "err", "err",
                                              false,
                                              true,
                                              null, LINE_PREFIX_PRIORITY);

    @NotNull
    public static final OpDef DEBUG = new OpDef(PREFIX, "!!", "debug", "debug",
                                                false, true,
                                                null, LINE_PREFIX_PRIORITY);

    @NotNull
    public static final OpDef ASSERT_EQ_REACT = new OpDef(BINARY, "<=>",
                                                          null,
                                                          "assert-equals-reactive",
                                                          false, true,
                                                          null, LINE_PREFIX_PRIORITY);
    @NotNull
    public static final OpDef ASSERT_EQ_UNREACT = new OpDef(BINARY, "<->", "assert-equals",
                                                            "assert-equals",
                                                            false, false,
                                                            null, LINE_PREFIX_PRIORITY);
    @NotNull
    public static final OpDef EACH = new OpDef(BINARY, "=>>", "each", "each", false, true, null,
                                               MULTIPLY_DIVIDE_PRIORITY);

    @NotNull
    public static final OpDef PUBLISH = new OpDef(BINARY, "*>", "publish", "publish", false, true, null,
                                                  OUTPUT_PRIORITY);

    @NotNull
    public static final OpDef SUBSCRIBE = new OpDef(BINARY, "<*", "subscribe", "subscribe", false, true, null,
                                                    OUTPUT_PRIORITY);

    @NotNull
    public static final OpDef EQUALITY = new OpDef(BINARY, "==", "equal", "equal", false, true, null, EQ_PRIORITY);

    @NotNull
    public static final OpDef INEQUALITY_OPERATOR = new OpDef(BINARY, "!=", "not-equal", "not-equal",
                                                              false, true, null,
                                                              EQ_PRIORITY);
    @NotNull
    public static final OpDef ASSIGNMENT = new OpDef(OpDefType.ASSIGNMENT, "=", null, "assign", false, true, null,
                                                     ASSIGNMENT_PRIORITY);

    @NotNull
    public static final OpDef SUBSCRIBE_ASSIGN = new OpDef(OpDefType.ASSIGNMENT, "*=", null,
                                                           "subscribe-assign",
                                                           false, true, null, ASSIGNMENT_PRIORITY);
    @NotNull
    public static final OpDef LISTEN_ASSIGN = new OpDef(OpDefType.ASSIGNMENT, "?=", null, "listen-assign",
                                                        false, true, null, ASSIGNMENT_PRIORITY);

    @NotNull
    public static final OpDef IN = new OpDef(BINARY, "€", "in", "in", false, true, null, IN_PRIORITY);

    @NotNull
    public static final OpDef DRAIN = new OpDef(PREFIX, "<-<", "drain", "drain", false, true, null,
                                                OUTPUT_PRIORITY);


    @NotNull
    public static final OpDef PAIR = new OpDef(BINARY, ":", "pair", "pair", false, true, null, PAIR_PRIORITY);

    @NotNull
    public static final OpDef IF_OPERATOR = new OpDef(BINARY, "???", "if", "if", false, true, null, IF_PRIORITY);

    @NotNull
    public static final OpDef WHEN_OP = new OpDef(BINARY, "?", "when", "when", false, true, null,
                                                  CONTROL_FLOW_PRIORITY);

    @NotNull
    public static final OpDef REDUCE = new OpDef(BINARY, ">>=", "reduce", "reduce", false, true, null,
                                                 MULTIPLY_DIVIDE_PRIORITY);

    @NotNull
    public static final OpDef CHOOSE = new OpDef(BINARY, "?*", "choose", "choose", false, true, null,
                                                 CONTROL_FLOW_PRIORITY);

    @NotNull
    public static final OpDef ALL = new OpDef(PREFIX, "<@", "all", "all", false, true, null, OUTPUT_PRIORITY);

    @NotNull
    public static final OpDef ELSE = new OpDef(BINARY, "-:", "else", "else", false, true, null, IF_PRIORITY);

    @NotNull
    public static final OpDef TRUTHY = new OpDef(PREFIX, "~", "truthy", "truthy",
                                                 false,
                                                 true,
                                                 null,
                                                 UNARY_PRIORITY);

    @NotNull
    public static final OpDef CAST = new OpDef(POSTFIX, null, null, "cast", false, true, null,
                                               UNARY_PRIORITY);

    @NotNull
    public static final OpDef FOR_OP = new OpDef(CONTROL_FLOW, null, "for", "for", false, true,
                                                 "for <variable-name> <iterable-expression> <expression>",
                                                 UNARY_PRIORITY);

    @NotNull
    public static final OpDef WHILE_OP = new OpDef(CONTROL_FLOW, null, "while", "while", false, true,
                                                   "while <condition> <expression>",
                                                   UNARY_PRIORITY);

    @NotNull
    public static final OpDef SUBSCRIPT_OP = new OpDef(POSTFIX, null, null, "subscript", false, true,
                                                       "( <expression> '[' <index-expression>|<key-expression> ']' ) | " +
                                                               "( <expression> '.' (<index-expression>|<key-expression>) )",
                                                       MEMBER_PRIORITY);

    @NotNull
    public static final OpDef PARAM_OP = new OpDef(POSTFIX, null, null, "parameter", false, true,
                                                   "( <expression> | <builtin-name> | <function-name> ) '(' " +
                                                           "( <expression> | <name> '=' <expression> )* ')'",
                                                   MEMBER_PRIORITY);

    @NotNull
    public static final OpDef WRITE_OP = new OpDef(CONTROL_FLOW, null, "write", "write",
                                                   false, true,
                                                   "'write' ['block'] ['mutate'] ['to'] <expression>",
                                                   OUTPUT_PRIORITY);


    @NotNull
    public static final OpDef READ_OP = new OpDef(PREFIX, null, "read", "read",
                                                  false, true,
                                                  "'read' ['block'] ['mutate'] ['from'] <expression>",
                                                  OUTPUT_PRIORITY);


    @NotNull
    public static final OpDef RESERVED_OPERATOR_1 = new OpDef(RESERVED, "...", null, null,
                                                              true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_2 = new OpDef(RESERVED, "->", null, null,
                                                              true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_3 = new OpDef(RESERVED, "<-", null, null,
                                                              true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_4 = new OpDef(RESERVED, "?:", null, null,
                                                              true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_5 = new OpDef(RESERVED, "@", null, null,
                                                              true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_6 = new OpDef(RESERVED, "::", null, null,
                                                              true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_7 = new OpDef(RESERVED, "&=", null, null,
                                                              true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_8 = new OpDef(RESERVED, "+>", null, null,
                                                              true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_9 = new OpDef(RESERVED, "<+", null, null,
                                                              true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_10 = new OpDef(RESERVED, "|*", null, null,
                                                               true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_11 = new OpDef(RESERVED, "&>", null, null,
                                                               true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_12 = new OpDef(RESERVED, "<&", null, null,
                                                               true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_13 = new OpDef(RESERVED, "?>", null, null,
                                                               true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_14 = new OpDef(RESERVED, "<?", null, null,
                                                               true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_15 = new OpDef(RESERVED, ">->", null,
                                                               null,
                                                               true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_16 = new OpDef(RESERVED, "@>", null, null,
                                                               true, true, null, 0);

    @NotNull
    public static final OpDef RESERVED_OPERATOR_17 = new OpDef(RESERVED, "?..?", null,
                                                               null,
                                                               true, true, null, 0);


    @NotNull
    public static final OpDef RESERVED_OPERATOR_18 = new OpDef(RESERVED, "?$?", null,
                                                               null,
                                                               true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_19 = new OpDef(RESERVED, "<$", null, null,
                                                               true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_20 = new OpDef(RESERVED, "<=<", null,
                                                               null,
                                                               true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_21 = new OpDef(RESERVED, "<++", null,
                                                               null,
                                                               true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_22 = new OpDef(RESERVED, "-_-", null,
                                                               null,
                                                               true, true, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_23 = new OpDef(RESERVED, ">&", null, null,
                                                               true, true, null, 0);
    @NotNull
    public static final List<String> SYMBOL_STRINGS;

    @NotNull
    public static final List<String> KEYWORD_STRINGS;


    @NotNull
    public static final SymbolDef DOLLAR = new SymbolDef("$", false);

    @NotNull
    public static final SymbolDef COMMA = new SymbolDef(",", false);

    @NotNull
    public static final SymbolDef NEWLINE = new SymbolDef("\n", false);

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
    public static final KeywordDef TRUE = new KeywordDef("true", false, "Boolean true.", null);

    @NotNull
    public static final KeywordDef FALSE = new KeywordDef("false", false, "Boolean false.", null);

    @NotNull
    public static final KeywordDef YES = new KeywordDef("yes", false, "Boolean true.", null);
    @NotNull
    public static final KeywordDef NO = new KeywordDef("no", false, "Boolean false.", null);
    @NotNull
    public static final KeywordDef NULL = new KeywordDef("null", false, "A NULL value of ANY type.", null);
    @NotNull
    public static final KeywordDef VOID = new KeywordDef("void", false, "A VOID value.", null);
    @NotNull
    public static final KeywordDef INFINITY = new KeywordDef("infinity", false, null, null);
    @NotNull
    public static final KeywordDef PURE = new KeywordDef("pure", false, "The start of a pure expression.", "pure <expression>");
    @NotNull
    public static final KeywordDef EXPORT = new KeywordDef("export", false, "Export a variable at the point of definition.", null);
    @NotNull
    public static final KeywordDef CONST = new KeywordDef("const", false,
                                                          "Mark a variable definition as a constant, i.e. readonly.", null);
    @NotNull
    public static final KeywordDef VAR = new KeywordDef("var", false, "Marks a variable as variable i.e. not readonly.", null);
    @NotNull
    public static final KeywordDef VOLATILE = new KeywordDef("volatile", false,
                                                             "Marks a variable as volatile, i.e. it can be accessed by multiple threads.",
                                                             null);
    @NotNull
    public static final KeywordDef COLLECT = new KeywordDef("collect", false, null, null);
    @NotNull
    public static final KeywordDef UNTIL = new KeywordDef("until", false, null, null);
    @NotNull
    public static final KeywordDef UNLESS = new KeywordDef("unless", false, null, null);
    @NotNull
    public static final KeywordDef WHEN = new KeywordDef("when", false, null, null);
    @NotNull
    public static final KeywordDef IS = new KeywordDef("is", false, null, null);
    @NotNull
    public static final KeywordDef FOR = new KeywordDef("for", false, null, null);
    @NotNull
    public static final KeywordDef WHILE = new KeywordDef("while", false, null, null);
    @NotNull
    public static final KeywordDef AS = new KeywordDef("as", false, null, null);
    @NotNull
    public static final KeywordDef DEF = new KeywordDef("def", false, null, null);
    @NotNull
    public static final KeywordDef WITH = new KeywordDef("with", false, null, null);
    @NotNull
    public static final KeywordDef MODULE = new KeywordDef("module", false, null, null);
    @NotNull
    public static final KeywordDef EVERY = new KeywordDef("every", false, null, null);
    @NotNull
    public static final KeywordDef READ_KEYWORD = new KeywordDef("read", false, null, null);
    @NotNull
    public static final KeywordDef WRITE_KEYWORD = new KeywordDef("write", false, null, null);
    @NotNull
    public static final KeywordDef BLOCK = new KeywordDef("block", false, null, null);
    @NotNull
    public static final KeywordDef MUTATE = new KeywordDef("mutate", false, null, null);
    @NotNull
    public static final KeywordDef TO = new KeywordDef("to", false, null, null);
    @NotNull
    public static final KeywordDef FROM = new KeywordDef("from", false, null, null);
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
                    MINUS,
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
                    READ_SIMPLE,
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
                    WRITE_SIMPLE,
                    WHEN_OP,
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

                    CAST,
                    FOR_OP,
                    WHILE_OP,
                    SUBSCRIPT_OP,
                    PARAM_OP,
                    WRITE_OP,
                    READ_OP,

                    //Reserved Keywords
                    new KeywordDef("pluripotent", true, null, null),
                    new KeywordDef("readonly", true, null, null),
                    new KeywordDef("join", true, null, null),
                    new KeywordDef("fail", true, null, null),
                    new KeywordDef("switch", true, null, null),
                    new KeywordDef("dollar", true, null, null),
                    new KeywordDef("filter", true, null, null),
                    new KeywordDef("dispatch", true, null, null),
                    new KeywordDef("send", true, null, null),
                    new KeywordDef("emit", true, null, null),
                    new KeywordDef("import", true, null, null),
                    new KeywordDef("include", true, null, null),
                    new KeywordDef("impure", true, null, null),
                    new KeywordDef("variant", true, null, null),
                    new KeywordDef("vary", true, null, null),
                    new KeywordDef("varies", true, null, null),
                    new KeywordDef("lambda", true, null, null),
                    new KeywordDef("closure", true, null, null),
                    new KeywordDef("scope", true, null, null),
                    new KeywordDef("dump", true, null, null),
                    new KeywordDef("trace", true, null, null),
                    new KeywordDef("measure", true, null, null),
                    new KeywordDef("unit", true, null, null),
                    new KeywordDef("wait", true, null, null),
                    new KeywordDef("await", true, null, null),
                    new KeywordDef("save", true, null, null),
                    new KeywordDef("load", true, null, null),

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
        OPERATORS.sort(Comparator.comparing(OpDef::name));

        KEYWORDS = tokens.stream().filter(i -> i instanceof KeywordDef && !((KeywordDef) i).isReserved()).map(
                i -> (KeywordDef) i).sorted().collect(Collectors.toList());

        SYMBOLS = tokens.stream().filter(i -> i instanceof SymbolDef && !((SymbolDef) i).isReserved()).map(
                i -> (SymbolDef) i).sorted().collect(Collectors.toList());


        SYMBOL_STRINGS = tokens.stream().filter(symbol -> symbol instanceof HasSymbol && ((HasSymbol) symbol).symbol() != null).map(
                symbol -> ((HasSymbol) symbol).symbol()).sorted().collect(Collectors.toList());


        KEYWORD_STRINGS = tokens.stream().filter(symbol -> symbol instanceof HasKeyword).map(
                symbol -> ((HasKeyword) symbol).keyword()).filter(
                Objects::nonNull).sorted().collect(Collectors.toList());
    }

    public static void mainx(@NotNull String[] args) {
        for (OpDef operator : OPERATORS) {

            File file = new File("src/main/resources/examples/op", operator.name() + ".ds");
            try {
                if (!file.exists()) {
                    Files.write("".getBytes(), file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public static void main(@NotNull String[] args) {
        System.out.println("## Appendix A - Operators");
        for (OpDef operator : OPERATORS) {
            System.out.println(operator.asMarkdown());
        }
        System.out.println("## Appendix B - Keywords");
        for (KeywordDef keyword : KEYWORDS) {
            System.out.println(keyword.asMarkdown());
        }
        System.out.println("## Appendix C - Reserved Keywords, Operators and Symbols");
        System.out.println("### Keywords\n");
        System.out.println("The following keywords are reserved:\n");
        System.out.println("> " + tokens.stream().filter(i -> i instanceof KeywordDef && ((KeywordDef) i).isReserved()).map(
                i -> ((KeywordDef) i).keyword()).sorted().collect(Collectors.joining(", ")));
        System.out.println();

        System.out.println("### Operators\n");
        System.out.println("The following operator keywords are reserved:\n");
        System.out.println("> " + tokens.stream().filter(
                i -> i instanceof OpDef && ((OpDef) i).keyword() != null && ((OpDef) i).isReserved()).map(
                i -> ((OpDef) i).keyword()).sorted().collect(Collectors.joining(", ")));
        System.out.println();
        System.out.println("The following operator symbols are reserved:\n");
        System.out.println("> `" + tokens.stream().filter(
                i -> i instanceof OpDef && ((OpDef) i).symbol() != null && ((OpDef) i).isReserved()).map(
                i -> ((OpDef) i).symbol()).sorted().collect(Collectors.joining(", ")) + " `");
        System.out.println();
        System.out.println("### Symbols\n");
        System.out.println("The following general symbols are reserved:\n");
        System.out.println("> `" + tokens.stream().filter(i -> i instanceof SymbolDef && ((SymbolDef) i).isReserved()).map(
                i -> ((SymbolDef) i).symbol()).sorted().collect(Collectors.joining(", ")) + " `");
        System.out.println();
        System.out.println("## Appendix D - Operator Precedence");

        System.out.println();
        System.out.println("All operators by precedence, highest precedence ([associativity](https://en.wikipedia" +
                                   ".org/wiki/Operator_associativity)) first.");
        System.out.println(

        );
        System.out.printf("|%-30s|%-15s|%-10s|%-10s|%n", "Name", "Keyword", "Operator", "Type");
        System.out.printf("|%-30s|%-15s|%-10s|%-10s|%n", "-------", "-------", "-------", "-------");
        ArrayList<OpDef> sortedOps = new ArrayList<>(OPERATORS);
        sortedOps.sort(new Comparator<OpDef>() {
            @Override
            public int compare(OpDef o1, OpDef o2) {
                return o2.priority() - o1.priority();
            }
        });
        for (OpDef operator : sortedOps) {
            System.out.printf("|%-30s|%-15s| %-9s|%-10s|%n", operator.name(), operator.keyword() != null ? ("`" + operator.keyword()
                                                                                                                    + "`")
                                                                                      : " ",
                              operator.symbol() != null ? ("`" + operator.symbol() + "`") : " ", operator.type().humanName());
        }
    }


}
