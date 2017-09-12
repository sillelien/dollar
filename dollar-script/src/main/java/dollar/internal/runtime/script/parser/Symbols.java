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
import dollar.api.Type;
import dollar.api.var;
import dollar.internal.runtime.script.Builtins;
import dollar.internal.runtime.script.api.HasKeyword;
import dollar.internal.runtime.script.api.HasSymbol;
import dollar.internal.runtime.script.api.OperatorPriority;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dollar.internal.runtime.script.api.OperatorPriority.*;
import static dollar.internal.runtime.script.parser.OpType.*;
import static dollar.internal.runtime.script.parser.SourceNodeOptions.*;
import static java.util.Arrays.asList;

public final class Symbols {


    @NotNull
    public static final Function<var[], Type> ANY_TYPE_F = i -> Type._ANY;
    @NotNull
    public static final KeywordDef AS = new KeywordDef("as", false, null, null);
    @NotNull
    public static final Op AVG = new Op(POSTFIX, "[%]", "avg", "avg",
                                        false, true,
                                        null, PLUS_MINUS_PRIORITY, true, NO_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final KeywordDef BLOCK = new KeywordDef("block", false, null, null);
    @NotNull
    public static final Op BLOCK_OP = new Op(COLLECTION, null, null, "block",
                                             false, true,
                                             "'{' ( <expression> ';' ) * [ <expression> ] '}'",
                                             NO_PRIORITY, null, SCOPE_WITH_CLOSURE, null, i -> Type._BLOCK);
    @NotNull
    public static final Function<var[], Type> BOOL_TYPE_F = i -> Type._BOOLEAN;
    @NotNull
    public static final Op AND = new Op(BINARY, "&&", "and", "and",
                                        false, true,
                                        null, OperatorPriority.AND_PRIORITY, true, NO_SCOPE, null, BOOL_TYPE_F);
    @NotNull
    public static final Op ASSERT_EQ_REACT = new Op(BINARY, "<=>",
                                                    null,
                                                    "assert-equals-reactive",
                                                    false, true,
                                                    null, LINE_PREFIX_PRIORITY, true, NO_SCOPE, null, BOOL_TYPE_F);
    @NotNull
    public static final Op ASSERT_EQ_UNREACT = new Op(BINARY, "<->", null,
                                                      "assert-equals",
                                                      false, false,
                                                      null, LINE_PREFIX_PRIORITY, true, NO_SCOPE, "left_right_arrow",
                                                      BOOL_TYPE_F);
    @NotNull
    public static final Op BUILTIN_OP = new Op(OTHER, null, null, "builtin",
                                               false, true,
                                               "<name> (<parameter>)*",
                                               NO_PRIORITY, null, NO_SCOPE, null, i -> Builtins.type(i[0].$S()));
    @NotNull
    public static final Op CAST = new Op(POSTFIX, null, null, "cast", false, true, "<expression> 'as' <type>",
                                         UNARY_PRIORITY, true, NO_SCOPE, null, i -> Type.of(i[1]));
    @NotNull
    public static final Op CHOOSE = new Op(BINARY, "?*", "choose", "choose", false, true, null,
                                           CONTROL_FLOW_PRIORITY, true, NO_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final Op CLASS_OP = new Op(OTHER, null, "class", "class",
                                             false, true,
                                             "'class' <identifier> <expression>",
                                             NO_PRIORITY, null, NO_SCOPE, null, i -> Type.of(i[0]));
    @NotNull
    public static final SymbolDef COMMA = new SymbolDef(",", false);
    @NotNull
    public static final KeywordDef CONST = new KeywordDef("const", false,
                                                          "Mark a variable definition as a constant, i.e. readonly.", null);
    @NotNull
    public static final KeywordDef DEF = new KeywordDef("def", false, null, null);
    @NotNull
    public static final SymbolDef DOLLAR = new SymbolDef("$", false);
    @NotNull
    public static final Op EQUALITY = new Op(BINARY, "==", null, "equal", false, true, null, EQ_PRIORITY, true, NO_SCOPE,
                                             null, BOOL_TYPE_F);
    @NotNull
    public static final KeywordDef EVERY = new KeywordDef("every", false, null, null);
    @NotNull
    public static final KeywordDef EXPORT = new KeywordDef("export", false, "Export a variable at the point of definition.", null);
    @NotNull
    public static final KeywordDef FALSE = new KeywordDef("false", false, "Boolean false.", null);
    @NotNull
    public static final Function<var[], Type> FIRST_TYPE_F = vars -> vars[0].$type();
    @NotNull
    public static final Op DEC = new Op(PREFIX, "--", null, "decrement",
                                        false,
                                        true, null, INC_DEC_PRIORITY, true, NO_SCOPE, null, FIRST_TYPE_F);
    @NotNull
    public static final Op DEFAULT = new Op(BINARY, ":-", "default", "default",
                                            false,
                                            true,
                                            null,
                                            MEMBER_PRIORITY, true, NO_SCOPE, null, FIRST_TYPE_F);
    @NotNull
    public static final Op DEFINITION = new Op(OpType.ASSIGNMENT, ":=", null,
                                               "declaration",
                                               false,
                                               true,
                                               "( [export] [const] <variable-name> ':=' <expression>) | ( def " +
                                                       "<variable-name> <expression )",
                                               ASSIGNMENT_PRIORITY, true, NO_SCOPE, null, FIRST_TYPE_F);
    @NotNull
    public static final Op FIX = new Op(PREFIX, "&", "fix", "fix",
                                        false,
                                        false, null, FIX_PRIORITY, true, NO_SCOPE, null, FIRST_TYPE_F);
    @NotNull
    public static final KeywordDef FOR = new KeywordDef("for", false, null, null);
    /*
    [1..3]<- <=> [3..1] //reverse
    [1..3][/] <=> [1,2,3] //split ($list)
    [1,2,3][<] <=> 1 //min ($min)
    [1,2,3][>] <=> 3 //max ($min)
    [1,2,3][+] <=> 6 //sum ($plus)
    [1,2,3][%] <=> 2 //mean ($plus)/($size)
    [1,2,3][*] <=> 6 //product ($multiply)

     */
    @NotNull
    public static final Op FORK = new Op(PREFIX, "-<", "fork", "fork",
                                         false,
                                         true,
                                         null, SIGNAL_PRIORITY, false, NO_SCOPE, null, FIRST_TYPE_F);
    @NotNull
    public static final KeywordDef FROM = new KeywordDef("from", false, null, null);
    @NotNull
    public static final Op FUNCTION_NAME_OP = new Op(OTHER, null, null, "function-call",
                                                     false, true,
                                                     "'<builtin-name> | <variable-name>",
                                                     NO_PRIORITY, null, NO_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final Op GT = new Op(BINARY, ">", null, "greater-than",
                                       false, true,
                                       null, COMP_PRIORITY, true, NO_SCOPE, null, BOOL_TYPE_F);
    @NotNull
    public static final Op GT_EQUALS = new Op(BINARY, ">=", null, "greater-than-equal",
                                              false, true, null, EQ_PRIORITY, true, NO_SCOPE, null, BOOL_TYPE_F);
    @NotNull
    public static final Op IF_OP = new Op(BINARY, null, "if", "if", false, true, null, IF_PRIORITY, true, NO_SCOPE, null,
                                          FIRST_TYPE_F);
    @NotNull
    public static final Op IN = new Op(BINARY, "€", "in", "in", false, true, null, IN_PRIORITY, true, NO_SCOPE, "\u2208",
                                       BOOL_TYPE_F);
    @NotNull
    public static final Op INC = new Op(PREFIX, "++", null, "increment",
                                        false, true, null, INC_DEC_PRIORITY, true, NO_SCOPE, null, FIRST_TYPE_F);
    @NotNull
    public static final Op INEQUALITY_OPERATOR = new Op(BINARY, "!=", null, "not-equal",
                                                        false, true, null,
                                                        EQ_PRIORITY, true, NO_SCOPE, null, BOOL_TYPE_F);
    @NotNull
    public static final KeywordDef INFINITY = new KeywordDef("infinity", false, null, null);
    @NotNull
    public static final Function<var[], Type> INTEGER_TYPE_F = i -> Type._INTEGER;
    @NotNull
    public static final KeywordDef IS = new KeywordDef("is", false, null, null);
    @NotNull
    public static final Op IS_OP = new Op(BINARY, null, "is", "is", false, true, null, IF_PRIORITY, true, NO_SCOPE, null,
                                          BOOL_TYPE_F);
    @NotNull
    public static final List<String> KEYWORD_STRINGS;
    @NotNull
    public static final SymbolDef LEFT_BRACE = new SymbolDef("{", false);
    @NotNull
    public static final SymbolDef LEFT_BRACKET = new SymbolDef("[", false);
    @NotNull
    public static final SymbolDef LEFT_PAREN = new SymbolDef("(", false);
    @NotNull
    public static final Function<var[], Type> LIST_TYPE_F = i -> Type._LIST;
    @NotNull
    public static final Op ALL = new Op(PREFIX, "<@", "all", "all", false, true, null, OUTPUT_PRIORITY, false, NO_SCOPE,
                                        null, LIST_TYPE_F);
    @NotNull
    public static final Op DRAIN = new Op(PREFIX, "<-<", "drain", "drain", false, true, null, OUTPUT_PRIORITY, false,
                                          NO_SCOPE, null, LIST_TYPE_F);
    @NotNull
    public static final Op EACH = new Op(BINARY, "=>>", "each", "each", false, true, null,
                                         MULTIPLY_DIVIDE_PRIORITY, true, NO_SCOPE, null, LIST_TYPE_F);
    @NotNull
    public static final Op LIST_OP = new Op(COLLECTION, null, null, "list",
                                            false, true,
                                            "'[' ( <expression> ',' ) * [ <expression> ] ']'",
                                            NO_PRIORITY, null, SCOPE_WITH_CLOSURE, null, LIST_TYPE_F);
    @NotNull
    public static final Op LT = new Op(BINARY, "<", null, "less-than",
                                       false, true,
                                       null, COMP_PRIORITY, true, NO_SCOPE, null, BOOL_TYPE_F);
    @NotNull
    public static final Op LT_EQUALS = new Op(BINARY, "<=", null, "less-than-equal",
                                              false, true, null, EQ_PRIORITY, true, NO_SCOPE, null, BOOL_TYPE_F);
    @NotNull
    public static final Function<var[], Type> MAP_TYPE_F = i -> Type._MAP;
    @NotNull
    public static final Op MAP_OP = new Op(COLLECTION, null, null, "map",
                                           false, true,
                                           "'{' ( <expression> ',' ) * [ <expression> ] '}'",
                                           NO_PRIORITY, null, SCOPE_WITH_CLOSURE, null, MAP_TYPE_F);
    @NotNull
    public static final Op MAX = new Op(POSTFIX, "[>]", "max", "max",
                                        false, true,
                                        null, PLUS_MINUS_PRIORITY, true, NO_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final Op MEMBER = new Op(BINARY, ".", null, "member",
                                           false,
                                           true,
                                           null, MEMBER_PRIORITY, true, NO_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final Op MIN = new Op(POSTFIX, "[<]", "min", "min",
                                        false, true,
                                        null, PLUS_MINUS_PRIORITY, true, NO_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final Op MOD = new Op(BINARY, "%", null, "modulus",
                                        false, true,
                                        null, MULTIPLY_DIVIDE_PRIORITY, true, NO_SCOPE, null, FIRST_TYPE_F);
    @NotNull
    public static final Op MODULE_OP = new Op(OTHER, null, "module", "module",
                                              false, true,
                                              "module <name> (<parameter>)*",
                                              NO_PRIORITY, false, NEW_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final KeywordDef MUTATE = new KeywordDef("mutate", false, null, null);
    @NotNull
    public static final Op NEGATE = new Op(PREFIX, "-", null, "negate",
                                           false, true,
                                           null, PLUS_MINUS_PRIORITY, true, NO_SCOPE, null, FIRST_TYPE_F);
    @NotNull
    public static final SymbolDef NEWLINE = new SymbolDef("\n", false);
    @NotNull
    public static final Op NEW_OP = new Op(PREFIX, null, "new", "new",
                                           false, true,
                                           "'new' <identifier> (<parameters>)",
                                           NO_PRIORITY, null, NEW_SCOPE, null, i -> Type.of(i[0]));
    @NotNull
    public static final KeywordDef NO = new KeywordDef("no", false, "Boolean false.", null);
    @NotNull
    public static final Op NOT = new Op(PREFIX, "!", "not", "not", false,
                                        true, null, UNARY_PRIORITY, true, NO_SCOPE, null, BOOL_TYPE_F);
    @NotNull
    public static final KeywordDef NULL = new KeywordDef("null", false, "A NULL value of ANY type.", null);
    @NotNull
    public static final List<Op> OPERATORS;
    @NotNull
    public static final Op OR = new Op(BINARY, "||", "or", "or",
                                       false,
                                       true, null,
                                       OperatorPriority.LOGICAL_OR_PRIORITY, true, NO_SCOPE, null, BOOL_TYPE_F);
    @NotNull
    public static final KeywordDef OVER = new KeywordDef("over", false, null, null);
    @NotNull
    public static final Op PAIR = new Op(BINARY, ":", null, "pair", false, true, null, PAIR_PRIORITY, true, NO_SCOPE, null,
                                         MAP_TYPE_F);
    @NotNull
    public static final Op PARALLEL = new Op(PREFIX, "|:|", "parallel", "parallel",
                                             false, false,
                                             null, SIGNAL_PRIORITY, true, NEW_PARALLEL_SCOPE, ":vertical_traffic_light:",
                                             FIRST_TYPE_F);
    @NotNull
    public static final Op PARAM_OP = new Op(POSTFIX, null, null, "parameter", false, true,
                                             "( <expression> | <builtin-name> | <function-name> ) '(' " +
                                                     "( <expression> | <name> '=' <expression> )* ')'",
                                             MEMBER_PRIORITY, true, SCOPE_WITH_CLOSURE, null, ANY_TYPE_F);
    @NotNull
    public static final KeywordDef PERIOD = new KeywordDef("period", false, null, null);
    @NotNull
    public static final Op PIPE_OP = new Op(BINARY, "|", null, "pipe",
                                            false, true, null, PIPE_PRIORITY, true, NEW_SCOPE, null, FIRST_TYPE_F);
    @NotNull
    public static final Op PRODUCT = new Op(POSTFIX, "[*]", "product", "product",
                                            false, true,
                                            null, PLUS_MINUS_PRIORITY, true, NO_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final KeywordDef PURE = new KeywordDef("pure", false, "The start of a pure expression.", "pure <expression>");
    @NotNull
    public static final Op PURE_OP = new Op(PREFIX, null, "pure", "pure",
                                            false, true,
                                            null,
                                            NO_PRIORITY, true, NO_SCOPE, null, FIRST_TYPE_F);
    @NotNull
    public static final Function<var[], Type> RANGE_TYPE_F = i -> Type._RANGE;
    @NotNull
    public static final Op RANGE = new Op(BINARY, "..", null, "range",
                                          false, true,
                                          null,
                                          OperatorPriority.RANGE_PRIORITY, true, NO_SCOPE, null, RANGE_TYPE_F);
    @NotNull
    public static final Op READ_OP = new Op(PREFIX, null, "read", "read",
                                            false, true,
                                            "'read' ['block'] ['mutate'] ['from'] <expression>",
                                            OUTPUT_PRIORITY, false, NO_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final Op READ_SIMPLE = new Op(PREFIX, "<<", null, "read-simple",
                                                false, true, null, OUTPUT_PRIORITY, false, NO_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final Op REDUCE = new Op(BINARY, ">>=", "reduce", "reduce", false, true, null,
                                           MULTIPLY_DIVIDE_PRIORITY, true, NO_SCOPE, null, FIRST_TYPE_F);
    @NotNull
    public static final Op REVERSE = new Op(POSTFIX, "<-", "reversed", "reversed",
                                            false, true,
                                            null, REVERSE_PRIORITY, true, NO_SCOPE, null, FIRST_TYPE_F);
    @NotNull
    public static final Function<var[], Type> RHS_TYPE_F = i -> i[1].$type();
    @NotNull
    public static final Op ASSIGNMENT = new Op(OpType.ASSIGNMENT, "=", null, "assign", false, true, null,
                                               ASSIGNMENT_PRIORITY, true, NO_SCOPE, null, RHS_TYPE_F);
    @NotNull
    public static final Op CAUSES = new Op(BINARY, "=>", "causes", "causes",
                                           false, true, null, CONTROL_FLOW_PRIORITY, true, NO_SCOPE, null, RHS_TYPE_F);
    @NotNull
    public static final Op DIVIDE = new Op(BINARY, "/", null, "divide",
                                           false, true,
                                           null, MULTIPLY_DIVIDE_PRIORITY, true, NO_SCOPE, null, RHS_TYPE_F);
    @NotNull
    public static final Op ELSE = new Op(BINARY, null, "else", "else", false, true, null, IF_PRIORITY, true, NO_SCOPE, null,
                                         RHS_TYPE_F);
    @NotNull
    public static final Op MINUS = new Op(BINARY, "-", null, "minus",
                                          false, true,
                                          null, PLUS_MINUS_PRIORITY, true, NO_SCOPE, null, RHS_TYPE_F);
    @NotNull
    public static final Op MULTIPLY = new Op(BINARY, "*", null, "multiply",
                                             false, true,
                                             null, MULTIPLY_DIVIDE_PRIORITY, true, NO_SCOPE, null, RHS_TYPE_F);
    @NotNull
    public static final Op PLUS = new Op(BINARY, "+", null, "plus",
                                         false, true,
                                         null, PLUS_MINUS_PRIORITY, true, NO_SCOPE, null, RHS_TYPE_F);
    @NotNull
    public static final SymbolDef RIGHT_BRACE = new SymbolDef("}", false);
    @NotNull
    public static final SymbolDef RIGHT_BRACKET = new SymbolDef("]", false);
    @NotNull
    public static final SymbolDef RIGHT_PAREN = new SymbolDef(")", false);
    @NotNull
    public static final Op SCRIPT_OP = new Op(OTHER, null, null,
                                              "script", false, false,
                                              " <language-name> ``<script-code>`` ",
                                              NO_PRIORITY, false, NO_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final SymbolDef SEMI_COLON = new SymbolDef(";", false);
    @NotNull
    public static final Op SERIAL = new Op(PREFIX, "|..|", "serial", "serial",
                                           false, false,
                                           null, SIGNAL_PRIORITY, true, NEW_SERIAL_SCOPE, ":traffic_light:", FIRST_TYPE_F);
    @NotNull
    public static final Op SIZE = new Op(PREFIX, "#", null, "size",
                                         false, true,
                                         null, UNARY_PRIORITY, true, NO_SCOPE, ":hash:", INTEGER_TYPE_F);
    @NotNull
    public static final Op SORT = new Op(PREFIX, "->", "sorted", "sorted",
                                         false, true,
                                         null, SORT_PRIORITY, true, NO_SCOPE, null, FIRST_TYPE_F);
    @NotNull
    public static final Op SPLIT = new Op(POSTFIX, "[/]", "split", "split",
                                          false, true,
                                          null, PLUS_MINUS_PRIORITY, true, NO_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final Op STATE = new Op(PREFIX, "<|>", "state", "state",
                                          false, true,
                                          null, SIGNAL_PRIORITY, false, NO_SCOPE, ":keycap_asterisk:", ANY_TYPE_F);
    @NotNull
    public static final Op SUBSCRIBE = new Op(BINARY, "<*", "subscribe", "subscribe", false, true, null,
                                              OUTPUT_PRIORITY, false, NO_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final Op SUBSCRIBE_ASSIGN = new Op(OpType.ASSIGNMENT, "*=", null,
                                                     "subscribe-assign",
                                                     false, true, null, ASSIGNMENT_PRIORITY, true, NO_SCOPE, null,
                                                     ANY_TYPE_F);
    @NotNull
    public static final Op SUBSCRIPT_OP = new Op(POSTFIX, null, null, "subscript", false, true,
                                                 "( <expression> '[' <index-expression>|<key-expression> ']' ) | " +
                                                         "( <expression> '.' (<index-expression>|<key-expression>) )",
                                                 MEMBER_PRIORITY, true, NO_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final Op SUM = new Op(POSTFIX, "[+]", "sum", "sum",
                                        false, true,
                                        null, PLUS_MINUS_PRIORITY, true, NO_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final List<String> SYMBOL_STRINGS;
    @NotNull
    public static final KeywordDef THIS = new KeywordDef("this", true, null, null);
    @NotNull
    public static final KeywordDef TO = new KeywordDef("to", false, null, null);
    @NotNull
    public static final KeywordDef TRUE = new KeywordDef("true", false, "Boolean true.", null);
    @NotNull
    public static final Op TRUTHY = new Op(PREFIX, "~", null, "truthy",
                                           false,
                                           true,
                                           null,
                                           UNARY_PRIORITY, true, NO_SCOPE, null, BOOL_TYPE_F);
    @NotNull
    public static final Op UNIQUE = new Op(POSTFIX, "[!]", "unique", "unique",
                                           false, true,
                                           null, PLUS_MINUS_PRIORITY, true, NO_SCOPE, null, FIRST_TYPE_F);
    @NotNull
    public static final Op UNIT_OP = new Op(POSTFIX, null, null, "unit",
                                            false, true,
                                            "<numeric> <unit-name>",
                                            NO_PRIORITY, null, NEW_SCOPE, null, ANY_TYPE_F);
    @NotNull
    public static final KeywordDef UNLESS = new KeywordDef("unless", false, null, null);
    @NotNull
    public static final KeywordDef UNTIL = new KeywordDef("until", false, null, null);
    @NotNull
    public static final KeywordDef VAR = new KeywordDef("var", false, "Marks a variable as variable i.e. not readonly.", null);
    @NotNull
    public static final Op VAR_USAGE_OP = new Op(CONTROL_FLOW, null, null, "var-usage", false, true, null, NO_PRIORITY, true,
                                                 NO_SCOPE, null,
                                                 FIRST_TYPE_F);
    @NotNull
    public static final KeywordDef VOID = new KeywordDef("void", false, "A VOID value.", null);
    @NotNull
    public static final Function<var[], Type> VOID_TYPE_F = i -> Type._VOID;
    @NotNull
    public static final Op ASSERT = new Op(PREFIX, ".:", "assert", "assert",
                                           false, true, null, LINE_PREFIX_PRIORITY, true, NO_SCOPE, "\u2234", VOID_TYPE_F);
    @NotNull
    public static final Op ASSIGNMENT_CONSTRAINT = new Op(OpType.ASSIGNMENT, null, null, "assignment-constraint", false,
                                                          true, null, ASSIGNMENT_PRIORITY, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    public static final Op COLLECT_OP = new Op(CONTROL_FLOW, null,
                                               "collect", "collect",
                                               false, false,
                                               "collect <expression> [ 'until' <expression> ] [ 'unless' <expression> ] <expression>",
                                               NO_PRIORITY, true, NEW_SCOPE, null, VOID_TYPE_F);
    @NotNull
    public static final Op CREATE = new Op(PREFIX, "|||>", "create", "create",
                                           false, true,
                                           null, SIGNAL_PRIORITY, false, NO_SCOPE, ":fast_forward:", VOID_TYPE_F);
    @NotNull
    public static final Op DEBUG = new Op(PREFIX, "!!", "debug", "debug",
                                          false, true,
                                          null, LINE_PREFIX_PRIORITY, false, NO_SCOPE, "‼️:bangbang:", VOID_TYPE_F);
    @NotNull
    public static final Op DESTROY = new Op(PREFIX, "<|||", "destroy", "destroy",
                                            false, true,
                                            null, SIGNAL_PRIORITY, false, NO_SCOPE, ":rewind:", VOID_TYPE_F);
    @NotNull
    public static final Op ERR = new Op(PREFIX, "!?", "err", "err",
                                        false,
                                        true,
                                        null, LINE_PREFIX_PRIORITY, false, NO_SCOPE, ":interrobang:", VOID_TYPE_F);
    @NotNull
    public static final Op ERROR = new Op(PREFIX, "?->", "error", "error",
                                          false, true,
                                          null,
                                          LINE_PREFIX_PRIORITY, true, NO_SCOPE, ":fire:", VOID_TYPE_F);
    @NotNull
    public static final Op EVERY_OP = new Op(CONTROL_FLOW, null, "every",
                                             "every", false, false,
                                             "every <duration> <expression>",
                                             NO_PRIORITY, false, NEW_SCOPE, null, VOID_TYPE_F);
    @NotNull
    public static final Op FOR_OP = new Op(CONTROL_FLOW, null, "for", "for", false, true,
                                           "for <variable-name> <iterable-expression> <expression>",
                                           UNARY_PRIORITY, true, NEW_SCOPE, null, VOID_TYPE_F);
    @NotNull
    public static final Op OUT = new Op(PREFIX, "@@", "print", "print",
                                        false, true,
                                        null, LINE_PREFIX_PRIORITY, false, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    public static final Op PAUSE = new Op(PREFIX, "||>", "pause", "pause",
                                          false, true,
                                          null, SIGNAL_PRIORITY, false, NO_SCOPE, ":double_vertical_bar:", VOID_TYPE_F);
    @NotNull
    public static final Op PRINT = new Op(PREFIX, null, "print", "print",
                                          false, true,
                                          null, LINE_PREFIX_PRIORITY, false, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    public static final Op PUBLISH = new Op(BINARY, "*>", "publish", "publish", false, true, null,
                                            OUTPUT_PRIORITY, false, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    public static final Op START = new Op(PREFIX, "|>", "start", "start",
                                          false, true,
                                          "('|>'|'start') <expression>", SIGNAL_PRIORITY, true, NO_SCOPE, ":arrow_forward:",
                                          VOID_TYPE_F);
    @NotNull
    public static final Op STOP = new Op(PREFIX, "<|", "stop", "stop",
                                         false, true,
                                         null, SIGNAL_PRIORITY, false, NO_SCOPE, ":black_square_for_stop:", VOID_TYPE_F);
    @NotNull
    public static final Op UNPAUSE = new Op(PREFIX, "<||", "unpause", "unpause",
                                            false, true,
                                            null, SIGNAL_PRIORITY, false, NO_SCOPE,
                                            ":black_right_pointing_triangle_with_double_vertical_bar:", VOID_TYPE_F);
    @NotNull
    public static final KeywordDef
            VOLATILE = new KeywordDef("volatile", false,
                                      "Marks a variable as volatile, i.e. it can be accessed by multiple threads.",
                                      null);
    @NotNull
    public static final Op WHEN = new Op(BINARY, "?", "when", "when", false, true, null,
                                         CONTROL_FLOW_PRIORITY, true, NEW_SCOPE, null, RHS_TYPE_F);
    @NotNull
    public static final Op WHEN_ASSIGN = new Op(OpType.ASSIGNMENT, null, null, "when-assign",
                                                false, true,
                                                "('var'|'volatile') [<type-assertion>] <variable-name> '?' <condition-expression> '='<assignment-expression>",
                                                ASSIGNMENT_PRIORITY, true, NO_SCOPE, null, RHS_TYPE_F);
    @NotNull
    public static final Op WHILE_OP = new Op(CONTROL_FLOW, null, "while", "while", false, true,
                                             "while <condition> <expression>",
                                             UNARY_PRIORITY, true, NEW_SCOPE, null, VOID_TYPE_F);
    @NotNull
    public static final Op WINDOW_OP = new Op(CONTROL_FLOW, null,
                                              "window", "window",
                                              false, false,
                                              "window <expression> 'over' <duration-expression> [ 'period' <duration-expression> ] [ 'unless' <expression> ] [ 'until' <expression> ]  <window-expression>",
                                              NO_PRIORITY, true, NEW_SCOPE, null, VOID_TYPE_F);
    @NotNull
    public static final KeywordDef WITH = new KeywordDef("with", false, null, null);
    @NotNull
    public static final Op WRITE_OP = new Op(CONTROL_FLOW, null, "write", "write",
                                             false, true,
                                             "'write' ['block'] ['mutate'] ['to'] <expression>",
                                             OUTPUT_PRIORITY, false, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    public static final Op WRITE_SIMPLE = new Op(BINARY, ">>", null, "write-simple",
                                                 false, true, null, OUTPUT_PRIORITY, false, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    public static final KeywordDef YES = new KeywordDef("yes", false, "Boolean true.", null);
    @NotNull
    private static final KeywordDef ABSTRACT = new KeywordDef("abstract", true, null, null);
    @NotNull
    private static final KeywordDef AWAIT = new KeywordDef("await", true, null, null);
    @NotNull
    private static final KeywordDef BREAK = new KeywordDef("break", true, null, null);
    @NotNull
    private static final KeywordDef CASE = new KeywordDef("case", true, null, null);
    @NotNull
    private static final KeywordDef CATCH = new KeywordDef("catch", true, null, null);
    @NotNull
    private static final KeywordDef CLOSURE = new KeywordDef("closure", true, null, null);
    @NotNull
    private static final KeywordDef CONTINUE = new KeywordDef("continue", true, null, null);
    @NotNull
    private static final KeywordDef DISPATCH = new KeywordDef("dispatch", true, null, null);
    @NotNull
    private static final KeywordDef DO = new KeywordDef("do", true, null, null);
    @NotNull
    private static final KeywordDef DUMP = new KeywordDef("dump", true, null, null);
    @NotNull
    private static final KeywordDef EMIT = new KeywordDef("emit", true, null, null);
    @NotNull
    private static final KeywordDef ENUM = new KeywordDef("enum", true, null, null);
    @NotNull
    private static final KeywordDef EXTENDS = new KeywordDef("extends", true, null, null);
    @NotNull
    private static final KeywordDef FAIL = new KeywordDef("fail", true, null, null);
    @NotNull
    private static final KeywordDef FILTER = new KeywordDef("filter", true, null, null);
    @NotNull
    private static final KeywordDef FINAL = new KeywordDef("final", true, null, null);
    @NotNull
    private static final KeywordDef FINALLY = new KeywordDef("finally", true, null, null);
    @NotNull
    private static final KeywordDef FLOAT = new KeywordDef("float", true, null, null);
    @NotNull
    private static final KeywordDef GOTO = new KeywordDef("goto", true, null, null);
    @NotNull
    private static final KeywordDef IMPLEMENTS = new KeywordDef("implements", true, null, null);
    @NotNull
    private static final KeywordDef IMPORT = new KeywordDef("import", true, null, null);
    @NotNull
    private static final KeywordDef IMPURE = new KeywordDef("impure", true, null, null);
    @NotNull
    private static final KeywordDef INCLUDE = new KeywordDef("include", true, null, null);
    @NotNull
    private static final KeywordDef INSTANCEOF = new KeywordDef("instanceof", true, null, null);
    @NotNull
    private static final KeywordDef INTERFACE = new KeywordDef("interface", true, null, null);
    @NotNull
    private static final KeywordDef JOIN = new KeywordDef("join", true, null, null);
    @NotNull
    private static final List<KeywordDef> KEYWORDS;
    @NotNull
    private static final KeywordDef LAMBDA = new KeywordDef("lambda", true, null, null);
    @NotNull
    private static final KeywordDef LOAD = new KeywordDef("load", true, null, null);
    @NotNull
    private static final KeywordDef MEASURE = new KeywordDef("measure", true, null, null);
    @NotNull
    private static final KeywordDef NATIVE = new KeywordDef("native", true, null, null);
    @NotNull
    private static final KeywordDef PACKAGE = new KeywordDef("package", true, null, null);
    @NotNull
    private static final KeywordDef PLURIPOTENT = new KeywordDef("pluripotent", true, null, null);
    @NotNull
    private static final KeywordDef PRIVATE = new KeywordDef("private", true, null, null);
    @NotNull
    private static final KeywordDef PROTECTED = new KeywordDef("protected", true, null, null);
    @NotNull
    private static final KeywordDef PUBLIC = new KeywordDef("public", true, null, null);
    @NotNull
    private static final KeywordDef READONLY = new KeywordDef("readonly", true, null, null);
    @NotNull
    private static final Op RESERVED_OPERATOR_1 = new Op(RESERVED, "...", null, "RESERVED_OPERATOR_1",
                                                         true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_10 = new Op(RESERVED, "|*", null, "RESERVED_OPERATOR_1",
                                                          true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_11 = new Op(RESERVED, "&>", null, "RESERVED_OPERATOR_1",
                                                          true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_12 = new Op(RESERVED, "<&", null, "RESERVED_OPERATOR_1",
                                                          true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_13 = new Op(RESERVED, "?>", null, "RESERVED_OPERATOR_1",
                                                          true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_14 = new Op(RESERVED, "<?", null, "RESERVED_OPERATOR_1",
                                                          true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_15 = new Op(RESERVED, ">->", null,
                                                          "RESERVED_OPERATOR_1",
                                                          true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_16 = new Op(RESERVED, "@>", null, "RESERVED_OPERATOR_1",
                                                          true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_17 = new Op(RESERVED, "?..?", null,
                                                          "RESERVED_OPERATOR_1",
                                                          true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_18 = new Op(RESERVED, "?$?", null,
                                                          "RESERVED_OPERATOR_1",
                                                          true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_19 = new Op(RESERVED, "<$", null, "RESERVED_OPERATOR_1",
                                                          true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_2 = new Op(RESERVED, "->", null, "RESERVED_OPERATOR_1",
                                                         true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_20 = new Op(RESERVED, "<=<", null,
                                                          "RESERVED_OPERATOR_1",
                                                          true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_21 = new Op(RESERVED, "<++", null,
                                                          "RESERVED_OPERATOR_1",
                                                          true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_22 = new Op(RESERVED, "-_-", null,
                                                          "RESERVED_OPERATOR_1",
                                                          true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_23 = new Op(RESERVED, ">&", null, "RESERVED_OPERATOR_1",
                                                          true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_3 = new Op(RESERVED, "<-", null, "RESERVED_OPERATOR_1",
                                                         true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_4 = new Op(RESERVED, "?:", null, "RESERVED_OPERATOR_1",
                                                         true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_5 = new Op(RESERVED, "@", null, "RESERVED_OPERATOR_1",
                                                         true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_6 = new Op(RESERVED, "::", null, "RESERVED_OPERATOR_1",
                                                         true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_7 = new Op(RESERVED, "&=", null, "RESERVED_OPERATOR_1",
                                                         true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_8 = new Op(RESERVED, "+>", null, "RESERVED_OPERATOR_1",
                                                         true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final Op RESERVED_OPERATOR_9 = new Op(RESERVED, "<+", null, "RESERVED_OPERATOR_1",
                                                         true, true, null, 0, true, NO_SCOPE, null, VOID_TYPE_F);
    @NotNull
    private static final KeywordDef RETURN = new KeywordDef("return", true, null, null);
    @NotNull
    private static final KeywordDef SAVE = new KeywordDef("save", true, null, null);
    @NotNull
    private static final KeywordDef SCOPE = new KeywordDef("scope", true, null, null);
    @NotNull
    private static final KeywordDef SEND = new KeywordDef("send", true, null, null);
    @NotNull
    private static final KeywordDef SHORT = new KeywordDef("short", true, null, null);
    @NotNull
    private static final KeywordDef STATIC = new KeywordDef("static", true, null, null);
    @NotNull
    private static final KeywordDef SUPER = new KeywordDef("super", true, null, null);
    @NotNull
    private static final KeywordDef SWITCH = new KeywordDef("switch", true, null, null);
    @NotNull
    private static final List<SymbolDef> SYMBOLS;
    @NotNull
    private static final KeywordDef SYNCHRONIZED = new KeywordDef("synchronized", true, null, null);
    @NotNull
    private static final KeywordDef THROW = new KeywordDef("throw", true, null, null);
    @NotNull
    private static final KeywordDef THROWS = new KeywordDef("throws", true, null, null);
    @NotNull
    private static final KeywordDef TRACE = new KeywordDef("trace", true, null, null);
    @NotNull
    private static final KeywordDef TRANSIENT = new KeywordDef("transient", true, null, null);
    @NotNull
    private static final KeywordDef TRY = new KeywordDef("try", true, null, null);
    @NotNull
    private static final KeywordDef UNIT = new KeywordDef("unit", true, null, null);
    @NotNull
    private static final KeywordDef VARIANT = new KeywordDef("variant", true, null, null);
    @NotNull
    private static final KeywordDef VARIES = new KeywordDef("varies", true, null, null);
    @NotNull
    private static final KeywordDef VARY = new KeywordDef("vary", true, null, null);
    @NotNull
    private static final KeywordDef WAIT = new KeywordDef("wait", true, null, null);
    @NotNull
    private static final List<? extends Comparable<?>> tokens =
            asList(
                    //simple perators,
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
                    IF_OP,
                    IN,
                    INC,
                    INEQUALITY_OPERATOR,
                    LEFT_BRACE,
                    LEFT_BRACKET,
                    LEFT_PAREN,
                    LT_EQUALS,
                    LT,
                    WHEN_ASSIGN,
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
                    PIPE_OP,
                    PLUS,
//                    PRINT,
                    OUT,
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
                    COMMA,
                    TRUE,
                    FALSE,
                    YES,
                    NO,
                    NULL,

                    //List/Set operators
                    AVG,
                    MAX,
                    MIN,
                    PRODUCT,
                    REVERSE,
                    SORT,
                    SPLIT,
                    SUM,
                    UNIQUE,

                    //complex operators
                    BLOCK_OP,
                    BUILTIN_OP,
                    CAST,
                    CLASS_OP,
                    COLLECT_OP,
                    FOR_OP,
                    IS_OP,
                    SCRIPT_OP,
                    LIST_OP,
                    MAP_OP,
                    MODULE_OP,
                    NEW_OP,
                    PARAM_OP,
                    PURE_OP,
                    READ_OP,
                    SUBSCRIPT_OP,
                    UNIT_OP,
                    WHEN,
                    WHILE_OP,
                    WINDOW_OP,
                    WRITE_OP,

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
                    RESERVED_OPERATOR_23,


                    //keywords
                    OVER,
                    PERIOD,
                    VOID,
                    INFINITY,
                    PURE,
                    EXPORT,
                    CONST,
                    VAR,
                    VOLATILE,
                    UNTIL,
                    UNLESS,
                    IS,
                    FOR,
                    AS,
                    DEF,
                    WITH,
                    EVERY,
                    BLOCK,
                    MUTATE,
                    TO,
                    FROM,


                    //Reserved Keywords
                    IMPORT,
                    IMPURE,
                    FAIL,
                    INCLUDE,
                    JOIN,
                    RETURN,
                    PACKAGE,
                    THIS,
                    READONLY,
                    SEND,
                    DISPATCH,
                    SUPER,
                    THROW,
                    ENUM,
                    IMPLEMENTS,
                    INSTANCEOF,
                    INTERFACE,
                    SCOPE,
                    TRANSIENT,
                    EXTENDS,
                    STATIC,

                    ABSTRACT,
                    AWAIT,
                    BREAK,
                    CASE,
                    CATCH,
                    CLOSURE,
                    CONTINUE,
                    DO,
                    DUMP,
                    EMIT,
                    FILTER,
                    FINAL,
                    FINALLY,
                    FLOAT,
                    GOTO,
                    LAMBDA,
                    LOAD,
                    MEASURE,
                    NATIVE,
                    PLURIPOTENT,
                    PRIVATE,
                    PROTECTED,
                    PUBLIC,
                    SAVE,
                    SHORT,
                    SWITCH,
                    SYNCHRONIZED,
                    THROWS,
                    TRACE,
                    TRY,
                    UNIT,
                    VARIANT,
                    VARIES,
                    VARY,
                    WAIT

            );

    static {

        OPERATORS = tokens.stream().filter(i -> (i instanceof Op) && !((Op) i).isReserved()).map(
                i -> (Op) i).collect(Collectors.toList());
        OPERATORS.sort(Comparator.comparing(Op::name));

        KEYWORDS = tokens.stream().filter(i -> (i instanceof KeywordDef) && !((KeywordDef) i).isReserved()).map(
                i -> (KeywordDef) i).sorted().collect(Collectors.toList());

        SYMBOLS = tokens.stream().filter(i -> (i instanceof SymbolDef) && !((SymbolDef) i).isReserved()).map(
                i -> (SymbolDef) i).sorted().collect(Collectors.toList());


        SYMBOL_STRINGS = tokens.stream().filter(
                symbol -> (symbol instanceof HasSymbol) && (((HasSymbol) symbol).symbol() != null)).map(
                symbol -> ((HasSymbol) symbol).symbol()).sorted().collect(Collectors.toList());


        KEYWORD_STRINGS = tokens.stream().filter(symbol -> symbol instanceof HasKeyword).map(
                symbol -> ((HasKeyword) symbol).keyword()).filter(
                Objects::nonNull).sorted().collect(Collectors.toList());
    }

    public static void main(@NotNull String[] args) {
        System.out.println("## Appendix A - Operators");
        for (Op operator : OPERATORS) {
            System.out.println(operator.asMarkdown());
        }
        System.out.println("## Appendix B - Keywords");
        for (KeywordDef keyword : KEYWORDS) {
            System.out.println(keyword.asMarkdown());
        }
        System.out.println("## Appendix C - Reserved Keywords, Operators and Symbols");
        System.out.println("### Keywords\n");
        System.out.println("The following keywords are reserved:\n");
        System.out.println("> " + tokens.stream().filter(i -> (i instanceof KeywordDef) && ((KeywordDef) i).isReserved()).map(
                i -> ((HasKeyword) i).keyword()).sorted().collect(Collectors.joining(", ")));
        System.out.println();

        System.out.println("### Operators\n");
        System.out.println("The following operator keywords are reserved:\n");
        System.out.println("> " + tokens.stream().filter(
                i -> (i instanceof Op) && (((HasKeyword) i).keyword() != null) && ((Op) i).isReserved()).map(
                i -> ((HasKeyword) i).keyword()).sorted().collect(Collectors.joining(", ")));
        System.out.println();
        System.out.println("The following operator symbols are reserved:\n");
        System.out.println("> `" + tokens.stream().filter(
                i -> (i instanceof Op) && (((HasSymbol) i).symbol() != null) && ((Op) i).isReserved()).map(
                i -> ((HasSymbol) i).symbol()).sorted().collect(Collectors.joining(", ")) + " `");
        System.out.println();
        System.out.println("### Symbols\n");
        System.out.println("The following general symbols are reserved:\n");
        System.out.println("> `" + tokens.stream().filter(i -> (i instanceof SymbolDef) && ((SymbolDef) i).isReserved()).map(
                i -> ((HasSymbol) i).symbol()).sorted().collect(Collectors.joining(", ")) + " `");
        System.out.println();
        System.out.println("## Appendix D - Operator Precedence");

        System.out.println();
        System.out.println("All operators by precedence, highest precedence ([associativity](https://en.wikipedia" +
                                   ".org/wiki/Operator_associativity)) first.");
        System.out.println(

        );
        System.out.printf("|%-30s|%-15s|%-10s|%-10s|%n", "Name", "Keyword", "Operator", "Type");
        System.out.printf("|%-30s|%-15s|%-10s|%-10s|%n", "-------", "-------", "-------", "-------");
        List<Op> sortedOps = new ArrayList<>(OPERATORS);
        sortedOps.sort((o1, o2) -> Integer.compare(o2.priority(), o1.priority()));
        for (Op op : sortedOps) {
            System.out.printf("|%-30s|%-15s| %-9s|%-10s|%n", "[" + op.name() + "](#op-" + op.name() + ")",
                              (op.keyword() != null) ? ("`" + op.keyword() + "`") : " ",
                              (op.symbol() != null) ? ("`" + op.symbol() + "`") : " ", op.type().humanName());
        }
    }

    public static void mainx(@NotNull String[] args) {
        for (Op operator : OPERATORS) {

            File file = new File("src/main/resources/examples/op", operator.name() + ".ds");
            try {
                if (!file.exists()) {
                    System.out.println("Creating " + operator.name() + ".ds");
                    Files.write("".getBytes(), file);
                }
            } catch (IOException e) {
//                log.error(e.getMessage(), e);
            }

            File mdFile = new File("src/main/resources/examples/op", operator.name() + ".md");
            try {
                if (!mdFile.exists()) {
                    System.out.println("Creating " + operator.name() + ".md");
                    Files.write("".getBytes(), mdFile);
                }
            } catch (IOException e) {
//                log.error(e.getMessage(), e);
            }
        }
    }


}
