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
import dollar.internal.runtime.script.OperatorPriority;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static dollar.internal.runtime.script.OperatorPriority.*;
import static java.util.Arrays.asList;

public class Symbols {

    @NotNull
    public static final OpDef PIPE_OPERATOR = new OpDef("|", "pipe", "pipe",
                                                        "The Pipe operator exists to improve method chaining and is used in the " +
                                                                "form `funcA() | funcB` where the first expression is evaluated " +
                                                                "and then the result is passed to the second function and can be " +
                                                                "chained such as `funcA() | funcB | funcC`.",
                                                        false, true, null, null, PIPE_PRIORITY);
    @NotNull
    public static final OpDef WRITE_SIMPLE = new OpDef(">>", null, "write-simple",
                                                       "Performs a simple write to another data item, mostly used to write to a URI. ",
                                                       false, true, null, null, OUTPUT_PRIORITY);
    @NotNull
    public static final OpDef READ_SIMPLE = new OpDef("<<", null, "read-simple",
                                                      "Performs a simple read from another data item, typically this is used with a URI" +
                                                              ".", false, true, null, null, OUTPUT_PRIORITY);
    @NotNull
    public static final OpDef CAUSES = new OpDef("=>", "causes", "causes",
                                                 "The causes operator is used to link a reactive expression to an imperative action. " +
                                                         "The left-hand-side is any expression and the right hand-side is any " +
                                                         "expression that will be evaluated when the left-hand-side is updated " +
                                                         "such as `a+b => {@@ a; @@ b}`.",
                                                 false, true, null, null, CONTROL_FLOW_PRIORITY);

    @NotNull
    public static final OpDef ASSERT = new OpDef(".:", "assert", "assert",
                                                 "The assertion opeartor is used to assert that an expression holds true. It is " +
                                                         "a reactive operator such that it is evaluated when the right-hand-side " +
                                                         "expression changes. so `.: a > 10` is asserting that a is **always** " +
                                                         "greater than 10. To avoid reactive behaviour use the fix operator such " +
                                                         "as `.: &a > 10` which means that when this statement is evaluated the " +
                                                         "value of a is compared with 10 - if __at this point__ it is not greater" +
                                                         " than 10 then the assertion will fail. ",
                                                 false, true, null, null, LINE_PREFIX_PRIORITY);
    @NotNull
    public static final OpDef LT_EQUALS = new OpDef("<=", null, "less-than-equal",
                                                    "The standard `<=` operator, it uses Comparable#compareTo and will work with" +
                                                            " any Dollar data type, including strings, ranges, lists etc.",
                                                    false, true, null, null, EQ_PRIORITY);
    @NotNull
    public static final OpDef GT_EQUALS = new OpDef(">=", null, "greater-than-equal", "The standard `>=` operator, it uses " +
                                                                                              "Comparable#compareTo and will work with" +
                                                                                              " any Dollar data type, including strings, ranges, lists etc.",
                                                    false, true, null, null, EQ_PRIORITY);

    @NotNull
    public static final SymbolDef LEFT_PAREN = new SymbolDef("(", false);

    @NotNull
    public static final SymbolDef RIGHT_PAREN = new SymbolDef(")", false);

    @NotNull
    public static final OpDef DEC = new OpDef("--", "dec", "decrement",
                                              "Returns the right-hand-side decremented. Note the right-hand-side is not changed " +
                                                      "so `--a` does not not decrement `a`, it __returns__ `a` **decremented**",
                                              false,
                                              true, "('--'|'dec') <expression>", null, INC_DEC_PRIORITY);

    @NotNull
    public static final OpDef INC = new OpDef("++", "inc", "increment",
                                              "Returns the right-hand-side incremented. Note the right-hand-side is not changed so `--a` does not not decrement `a`, it __returns__ `a` **incremented**",
                                              false, true, "('++'|'inc') <expression>", null, INC_DEC_PRIORITY);

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
                                                     " [export] [const] <variable-name> ':=' <expression> OR def <variable-name> <expression",
                                                     null, ASSIGNMENT_PRIORITY);
    @NotNull
    public static final OpDef FIX = new OpDef("&", "fix", "fix",
                                              "Converts a reactive expression into a fixed value. It fixes the value at the point the fix operator is executed. No reactive events will be passed from the right-hand-side expression.",
                                              false,
                                              false, "('&' | '--') fix <expression>", null, FIX_PRIORITY);


    @NotNull
    public static final OpDef MEMBER = new OpDef(".", null, "member",
                                                 "The membership or `.` operator accesses the member of a map by it's key.", false,
                                                 true,
                                                 "<expression<", null, MEMBER_PRIORITY);

    @NotNull
    public static final OpDef LT = new OpDef("<", "less-than", "less-than",
                                             "The standard `<` operator, it uses Comparable#compareTo and will work with" +
                                                     " any Dollar data type, including strings, ranges, lists etc.", false, true,
                                             null, null, COMP_PRIORITY);

    @NotNull
    public static final OpDef GT = new OpDef(">", "greater-than", "greater-than",
                                             "The standard `>` operator, it uses Comparable#compareTo and will work with" +
                                                     " any Dollar data type, including strings, ranges, lists etc.", false, true,
                                             null, null, COMP_PRIORITY);
    @NotNull
    public static final OpDef NOT = new OpDef("!", "not", "not", "Returns the negation of the right-hand-side expression.", false,
                                              true, "('!'|'not') <expression>", null, UNARY_PRIORITY);

    @NotNull
    public static final OpDef OR = new OpDef("||", "or", "or", "Returns the logical 'or' of two expressions, e.g. `a || b`. " +
                                                                       "Just like in Java it will shortcut, so that if the " +
                                                                       "left-hand-side is true the right-hand-side is never " +
                                                                       "evaluated.",
                                             false,
                                             true, "<expression> ('||'|'or') <expression>", null,
                                             OperatorPriority.LOGICAL_OR_PRIORITY);

    @NotNull
    public static final OpDef AND = new OpDef("&&", "and", "and",
                                              "Returns the logical 'and' of two expressions, e.g. `a && b`. Just like in Java it will shortcut, " +
                                                      "so that if the left-hand-side is false the right-hand-side is never " +
                                                      "evaluated.",
                                              false, true,
                                              "<expression> ('&&'|'and') <expression>", null, OperatorPriority.AND_PRIORITY);

    @NotNull
    public static final OpDef MULTIPLY = new OpDef("*", "multiply", "multiply",
                                                   "Returns the product of two values. If the left-hand-side is scalar (non " +
                                                           "collection) then a straightforward multiplication will take place. If" +
                                                           " the left-hand-side is a collection and it is multiplied by `n`, e.g." +
                                                           " `{a=a+1} * 3` it will be added (`+`) to itself `n` times i.e. " +
                                                           "`{a=a+1} + {a=a+1} + {a=a+1}`.",
                                                   false, true,
                                                   "<expression> '*'|'multiply'", null, MULTIPLY_DIVIDE_PRIORITY);

    @NotNull
    public static final OpDef DIVIDE = new OpDef("/", "divide", "divide",
                                                 "Divides one value by another.", false, true,
                                                 null, null, MULTIPLY_DIVIDE_PRIORITY);
    @NotNull
    public static final OpDef NEGATE = new OpDef("-", "negate", "negate",
                                                 "Negates a value.", false, true,
                                                 "('-'|'negate') <expression>", null, PLUS_MINUS_PRIORITY);


    @NotNull
    public static final OpDef START = new OpDef("|>", "start", "start",
                                                "Starts a service described typically by a URI.",
                                                true, true,
                                                "('|>'|'start') <expression>", null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef STOP = new OpDef("<|", "stop", "stop",
                                               "Stops a service described typically by a URI.", false,
                                               true,
                                               "('<|'|'stop') <expression>", null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef PAUSE = new OpDef("||>", "pause", "pause",
                                                "Pauses a service described typically by a URI.",
                                                false, true,
                                                "('||>'|'pause') <expression>", null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef UNPAUSE = new OpDef("<||", "unpause", "unpause",
                                                  "Un-pauses a service described typically by a URI.", false, true,
                                                  "('<||'|'unpause') <expression>", null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef DESTROY = new OpDef("<|||", "destroy", "destroy",
                                                  "destroy", false, true,
                                                  "('<|||'|'destroy') <expression>", null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef CREATE = new OpDef("|||>", "create", "create",
                                                 "Creates a service described typically by a URI.", false, true,
                                                 "('|||>'|'create') <expression>", null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef STATE = new OpDef("<|>", "state", "state",
                                                "Returns the state of a service described typically by a URI.", false, true,
                                                "('<|>'|'state') <expression>", null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef PLUS = new OpDef("+", "plus", "plus",
                                               "Appends or adds two values.", false, true,
                                               "<expression> ('+'|'plus') <expression>", null, PLUS_MINUS_PRIORITY);

    @NotNull
    public static final OpDef DEFAULT = new OpDef(":-", "default", "default",
                                                  "If the left-hand-side is VOID this returns the right-hand-side, otherwise " +
                                                          "returns the left-hand-side.",
                                                  false,
                                                  true,
                                                  "<expression> (':-'|'default') <expression>",
                                                  null, MEMBER_PRIORITY);

    @NotNull
    public static final OpDef PRINT = new OpDef("@@", "print", "print",
                                                "Sends the right-hand-side expression to stdout.", false, true,
                                                "('@@'|'print') <expression>", null, LINE_PREFIX_PRIORITY);

    @NotNull
    public static final OpDef PARALLEL = new OpDef("|:|", "parallel", "parallel",
                                                   "Causes the right-hand-side expression to be evaluated in parallel, most " +
                                                           "useful in conjunction with list blocks.",
                                                   false, false,
                                                   "('|:|'|'parallel') <expression>", null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef SERIAL = new OpDef("|..|", "serial", "serial",
                                                 "Causes the right-hand-side expression to be evaluated in serial, most useful " +
                                                         "in conjunction with list blocks.",
                                                 false, false,
                                                 "('|..|'|'serial') <expression>", null, SIGNAL_PRIORITY);
    @NotNull
    public static final OpDef FORK = new OpDef("-<", "fork", "fork",
                                               "Executes the right-hand-side in a seperate thread returning a 'future'. Any attempt to make use of the returned value from this operator will block until that thread finishes.",
                                               false,
                                               true,
                                               ("('-<'|'fork') <expression>"), null, SIGNAL_PRIORITY);

    @NotNull
    public static final OpDef RANGE = new OpDef("..", "range", "range",
                                                "Creates a RANGE between the two values specified.",
                                                false, true,
                                                "<expression> '..' <expression>",
                                                null, OperatorPriority.RANGE_PRIORITY);

    @NotNull
    public static final OpDef ERROR = new OpDef("?->", "error", "error",
                                                "The right-hand-side is executed if an error occurs in the current scope.",
                                                false, true,
                                                "('?->'|'error') <expression>",
                                                null, LINE_PREFIX_PRIORITY);

    @NotNull
    public static final OpDef SIZE = new OpDef("#", "size", "size",
                                               "Returns the size of non-scalar types or the length of a string.", false, true,
                                               "('#'|'size') <expression>", null, UNARY_PRIORITY);

    @NotNull
    public static final OpDef MOD = new OpDef("%", "mod", "modulus",
                                              "Returns the remainder (modulus) of the division of the left-hand-side by the " +
                                                      "right-hand-side.", false, true,
                                              "<expression> ('%'|'mod') <expression>", null, MULTIPLY_DIVIDE_PRIORITY);

    @NotNull
    public static final OpDef ERR = new OpDef("??", "err", "err",
                                              "Sends the result of the right-hand-side to `stderr`.", false,
                                              true,
                                              "('??'|'err') <expression>", null, LINE_PREFIX_PRIORITY);

    @NotNull
    public static final OpDef DEBUG = new OpDef("!!", "debug", "debug",
                                                "Sends the result of the right-hand-side to the debug log.",
                                                false, true,
                                                "('!!'|'debug') <expression>", null, LINE_PREFIX_PRIORITY);

    @NotNull
    public static final OpDef ASSERT_EQ_REACT = new OpDef("<=>",
                                                          null,
                                                          "assert-equals-reactive",
                                                          "Asserts that the left-hand-side is **always** equal to the right-hand-side.",
                                                          false, true,
                                                          "<expression> '<=>' <expression>", null, LINE_PREFIX_PRIORITY);
    @NotNull
    public static final OpDef ASSERT_EQ_UNREACT = new OpDef("<->", "assert-equals",
                                                            "assert-equals",
                                                            "Asserts that at the point of execution that the left-hand-side is equal to the right-hand-side.",
                                                            false, false,
                                                            "<expression> <-> <expression>", null, LINE_PREFIX_PRIORITY);
    @NotNull
    public static final OpDef EACH = new OpDef("=>>", "each", "each", "each", false, true, null, null, MULTIPLY_DIVIDE_PRIORITY);

    @NotNull
    public static final OpDef PUBLISH = new OpDef("*>", "publish", "publish", "publish", false, true, null, null, OUTPUT_PRIORITY);

    @NotNull
    public static final OpDef SUBSCRIBE = new OpDef("<*", "subscribe", "subscribe", "subscribe", false, true, null, null,
                                                    OUTPUT_PRIORITY);

    @NotNull
    public static final OpDef EQUALITY = new OpDef("==", "equal", "equal", "equal", false, true, null, null, EQ_PRIORITY);

    @NotNull
    public static final OpDef INEQUALITY_OPERATOR = new OpDef("!=", "not-equal", "not-equal",
                                                              "not-equal", false, true, null,
                                                              null, EQ_PRIORITY);
    @NotNull
    public static final OpDef ASSIGNMENT = new OpDef("=", null, "assign", "assign", false, true, null, null, ASSIGNMENT_PRIORITY);

    @NotNull
    public static final OpDef SUBSCRIBE_ASSIGN = new OpDef("*=", null,
                                                           "subscribe-assign",
                                                           "subscribe-assign", false, true, null, null, ASSIGNMENT_PRIORITY);
    @NotNull
    public static final OpDef LISTEN_ASSIGN = new OpDef("?=", null, "listen-assign",
                                                        "listen-assign", false, true, null, null, ASSIGNMENT_PRIORITY);

    @NotNull
    public static final OpDef IN = new OpDef("€", "in", "in", "in", false, true, null, null, IN_PRIORITY);

    @NotNull
    public static final OpDef DRAIN = new OpDef("<-<", "drain", "drain", "drain", false, true, null, null, OUTPUT_PRIORITY);


    @NotNull
    public static final OpDef PAIR = new OpDef(":", "pair", "pair", "pair", false, true, null, null, PAIR_PRIORITY);

    @NotNull
    public static final OpDef IF_OPERATOR = new OpDef("???", "if", "if", "if", false, true, null, null, IF_PRIORITY);

    @NotNull
    public static final OpDef LISTEN = new OpDef("?", "listen", "listen", "listen", false, true, null, null, CONTROL_FLOW_PRIORITY);

    @NotNull
    public static final OpDef REDUCE = new OpDef(">>=", "reduce", "reduce", "reduce", false, true, null, null,
                                                 MULTIPLY_DIVIDE_PRIORITY);

    @NotNull
    public static final OpDef CHOOSE = new OpDef("?*", "choose", "choose", "choose", false, true, null, null,
                                                 CONTROL_FLOW_PRIORITY);

    @NotNull
    public static final OpDef ALL = new OpDef("<@", "all", "all", "all", false, true, null, null, OUTPUT_PRIORITY);

    @NotNull
    public static final OpDef ELSE = new OpDef("-:", "else", "else", "else", false, true, null, null, IF_PRIORITY);

    @NotNull
    public static final OpDef TRUTHY = new OpDef("~", "truthy", "truthy",
                                                 "The truthy operator `~` converts any value to a boolean by applying the rule that: void is false, 0 is false, \"\" is false, empty list is false, empty map is false - all else is true.",
                                                 false,
                                                 true,
                                                 "('~'|'truthy') <expression>",
                                                 ".: ~ [1,2,3]\n.: ! ~ []\n.: ~ \"anything\"\n.: ! ~ \"\"\n.: ~ 1\n.: ! ~ 0\n.: ! ~ {void}\n.:  ~ {\"a\" : 1}\n.: ! ~ void",
                                                 UNARY_PRIORITY);

    @NotNull
    public static final OpDef CAST = new OpDef(null, null, "cast", "cast operator", false, true, null, null, UNARY_PRIORITY);

    @NotNull
    public static final OpDef FOR_OP = new OpDef(null, "for", "for", "for operator", false, true, null, null,
                                                 UNARY_PRIORITY);

    @NotNull
    public static final OpDef WHILE_OP = new OpDef(null, "while", "while", "while operator", false, true, null, null,
                                                   UNARY_PRIORITY);

    @NotNull
    public static final OpDef SUBSCRIPT_OP = new OpDef(null, null, "subscript", "subscript operator", false, true, null,
                                                       null,
                                                       MEMBER_PRIORITY);

    @NotNull
    public static final OpDef PARAM_OP = new OpDef(null, null, "parameter", "parameter operator", false, true, null,
                                                   null,
                                                   MEMBER_PRIORITY);

    @NotNull
    public static final OpDef WRITE_OP = new OpDef(null, "write", "write", "write operator", false, true, null,
                                                   null,
                                                   OUTPUT_PRIORITY);


    @NotNull
    public static final OpDef READ_OP = new OpDef(null, "read", "read", "read operator", false, true, null,
                                                  null,
                                                  OUTPUT_PRIORITY);



    @NotNull
    public static final OpDef RESERVED_OPERATOR_1 = new OpDef("...", null, null,
                                                              null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_2 = new OpDef("->", null, null,
                                                              null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_3 = new OpDef("<-", null, null,
                                                              null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_4 = new OpDef("?:", null, null,
                                                              null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_5 = new OpDef("@", null, null,
                                                              null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_6 = new OpDef("::", null, null,
                                                              null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_7 = new OpDef("&=", null, null,
                                                              null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_8 = new OpDef("+>", null, null,
                                                              null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_9 = new OpDef("<+", null, null,
                                                              null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_10 = new OpDef("|*", null, null,
                                                               null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_11 = new OpDef("&>", null, null,
                                                               null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_12 = new OpDef("<&", null, null,
                                                               null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_13 = new OpDef("?>", null, null,
                                                               null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_14 = new OpDef("<?", null, null,
                                                               null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_15 = new OpDef(">->", null,
                                                               null,
                                                               null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_16 = new OpDef("@>", null, null,
                                                               null, true, true, null, null, 0);

    @NotNull
    public static final OpDef RESERVED_OPERATOR_17 = new OpDef("?..?", null,
                                                               null,
                                                               null, true, true, null, null, 0);


    @NotNull
    public static final OpDef RESERVED_OPERATOR_18 = new OpDef("?$?", null,
                                                               null,
                                                               null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_19 = new OpDef("<$", null, null,
                                                               null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_20 = new OpDef("<=<", null,
                                                               null,
                                                               null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_21 = new OpDef("<++", null,
                                                               null,
                                                               null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_22 = new OpDef("-_-", null,
                                                               null,
                                                               null, true, true, null, null, 0);
    @NotNull
    public static final OpDef RESERVED_OPERATOR_23 = new OpDef(">&", null, null,
                                                               null, true, true, null, null, 0);
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
        System.out.println("The following operator symbols are reserved:\n");
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
        System.out.println("> " + tokens.stream().filter(
                i -> i instanceof OpDef && ((OpDef) i).symbol() != null && ((OpDef) i).isReserved()).map(
                i -> ((OpDef) i).symbol()).sorted().collect(Collectors.joining(", ")));
        System.out.println();
        System.out.println("### Symbols\n");
        System.out.println("The following general symbols are reserved:\n");
        System.out.println("> " + tokens.stream().filter(i -> i instanceof SymbolDef && ((SymbolDef) i).isReserved()).map(
                i -> ((SymbolDef) i).symbol()).sorted().collect(Collectors.joining(", ")));
        System.out.println();
        System.out.println("## Appendix D - Operator Precedence");

        System.out.println();
        System.out.println("All operators by precedence, highest precedence ([associativity](https://en.wikipedia" +
                                   ".org/wiki/Operator_associativity)) first.");
        System.out.println(

        );
        System.out.printf("|%-30s|%-15s|%-10s|%n", "Name", "Keyword", "Operator");
        System.out.printf("|%-30s|%-15s|%-10s|%n", "-------", "-------", "-------");
        ArrayList<OpDef> sortedOps = new ArrayList<>(OPERATORS);
        sortedOps.sort(new Comparator<OpDef>() {
            @Override
            public int compare(OpDef o1, OpDef o2) {
                return o2.priority() - o1.priority();
            }
        });
        for (OpDef operator : sortedOps) {
            System.out.printf("|%-30s|%-15s| %-9s|%n", operator.name(), operator.keyword() != null ? operator.keyword() : " ",
                              operator.symbol() != null ? ("`" + operator.symbol() + "`") : " ");
        }
    }


}
