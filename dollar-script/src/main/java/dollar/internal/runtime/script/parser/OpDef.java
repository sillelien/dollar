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

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.vdurmont.emoji.EmojiParser;
import dollar.internal.runtime.script.HasKeyword;
import dollar.internal.runtime.script.HasSymbol;
import dollar.internal.runtime.script.SourceNodeOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import static dollar.internal.runtime.script.SourceNodeOptions.*;

@SuppressWarnings("CallToPrintStackTrace")
public class OpDef implements HasSymbol, HasKeyword, Comparable<Object> {
    @Nullable
    private final String symbol;
    @Nullable
    private final String keyword;
    @NotNull
    private final String name;

    private final boolean reserved;
    private final boolean reactive;
    private final int priority;
    @NotNull
    private final OpDefType type;
    @Nullable
    private final Boolean pure;
    @Nullable
    private String emoji;
    @Nullable
    private String bnf;
    @NotNull
    private final SourceNodeOptions nodeOptions;

    public OpDef(@NotNull OpDefType type,
                 @Nullable String symbol,
                 @Nullable String keyword,
                 @NotNull String name,
                 boolean reserved,
                 boolean reactive,
                 @Nullable String bnf,
                 int priority,
                 @Nullable Boolean pure, @NotNull SourceNodeOptions nodeOptions, @Nullable String emoji) {
        this.type = type;
        if (emoji != null) {
            this.emoji = EmojiParser.parseToUnicode(emoji);
        }
        this.symbol = symbol;
        this.keyword = keyword;
        this.name = name;
        this.reserved = reserved;
        this.reactive = reactive;
        this.bnf = bnf;
        this.priority = priority;
        this.pure = pure;
        this.nodeOptions = nodeOptions;

        if (!reserved && (priority == 0)) {
            throw new AssertionError("Priority must be > 0");
        }
    }

    @Nullable
    public String keyword() {
        return keyword;
    }

    @NotNull
    public String name() {
        return name;
    }

    @NotNull
    public String emoji() {
        return emoji;
    }

    public boolean validForPure(boolean pure) {
        return !pure || (this.pure && pure);
    }

    @Nullable
    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (equals(o)) {
            return 0;
        }
        if ((o instanceof OpDef) && (keyword != null)) {
            return keyword.compareTo(String.valueOf(((OpDef) o).keyword()));
        }
        if ((o instanceof OpDef) && (name != null)) {
            return name.compareTo(((OpDef) o).name());
        }

        return name.compareTo(String.valueOf(o));
    }

    public boolean isReserved() {
        return reserved;
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    @NotNull
    public String asMarkdown() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("###");
//        if (emoji != null) {
//            stringBuilder.append(" ").append(emoji);
//        }
        if (symbol == null) {
            if (keyword != null) {
                stringBuilder.append(" `").append(keyword).append("`");
            } else {
                stringBuilder.append(" ").append(name);
            }
        } else {
            if (keyword != null) {
                stringBuilder.append(" `").append(keyword).append("` or `").append(symbol).append("`");
            } else {
                stringBuilder.append(" `").append(symbol).append("` (").append(name).append(")");
            }
        }
        stringBuilder.append(" {#op-").append(name).append("}").append("\n\n");
        if (reactive) {
            stringBuilder.append("![reactive](https://img.shields.io/badge/reactivity-reactive-green.svg?style=flat-square)");
        } else {
            stringBuilder.append("![non-reactive](https://img.shields.io/badge/reactivity-fixed-blue.svg?style=flat-square)");
        }
        if ((pure != null))
            if (pure) {
                stringBuilder.append(" ![pure](https://img.shields.io/badge/function-pure-green.svg?style=flat-square)");
            } else {
                stringBuilder.append(" ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square)");
            }
        else {
            stringBuilder.append(" ![impure](https://img.shields.io/badge/function-impure-blue.svg?style=flat-square)");
        }
        if (nodeOptions.equals(NO_SCOPE)) {
            stringBuilder.append(" ![No Scope](https://img.shields.io/badge/scope-inherited-lightgrey.svg?style=flat-square)");
        } else if (nodeOptions.equals(NEW_SCOPE)) {
            stringBuilder.append(" ![New Scope](https://img.shields.io/badge/scope-new-blue.svg?style=flat-square)");
        } else if (nodeOptions.equals(SCOPE_WITH_CLOSURE)) {
            stringBuilder.append(
                    " ![New Scope](https://img.shields.io/badge/scope-new%20with%20closure-green.svg?style=flat-square)");
        } else if (nodeOptions.equals(NEW_PARALLEL_SCOPE)) {
            stringBuilder.append(" ![New Parallel Scope](https://img.shields.io/badge/scope-new%20parallel-red" +
                                         ".svg?style=flat-square)");
        } else if (nodeOptions.equals(NEW_SERIAL_SCOPE)) {
            stringBuilder.append(" ![New Serial Scope](https://img.shields.io/badge/scope-new%20serial-yellow" +
                                         ".svg?style=flat-square)");
        }
        if (nodeOptions.isParallel() != null) {
            if (nodeOptions.isParallel()) {
                stringBuilder.append(
                        " ![Parallel Execution](https://img.shields.io/badge/evaluation-parallel-blue.svg?style=flat-square)");
            } else {
                stringBuilder.append(
                        " ![Serial Execution](https://img.shields.io/badge/evaluation-serial-green.svg?style=flat-square)");
            }
        } else {
            stringBuilder.append(
                    " ![Inherited Execution](https://img.shields.io/badge/evaluation-inherited-lightgrey.svg?style=flat-square)");
        }
        stringBuilder.append("\n\n");
        if (bnf == null) {
            if (type == OpDefType.PREFIX) {
                bnf = "" + bnfSymbol() + " <expression>";
            }
            if (type == OpDefType.POSTFIX) {
                bnf = "<expression> " + bnfSymbol() + "";
            }
            if (type == OpDefType.BINARY) {
                bnf = "<expression> " + bnfSymbol() + " <expression>";
            }
        }
        if (bnf != null) {
            stringBuilder.append("**`").append(bnf).append("`**{: style=\"font-size: 60%\"}\n\n");
        }
        try {
            String filename = "/examples/op/" + name + ".md";
            InputStream resourceAsStream = getClass().getResourceAsStream(filename);
            if (resourceAsStream != null) {
                stringBuilder.append("\n\n");
                stringBuilder.append(
                        CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream(filename), Charsets.UTF_8)));
                stringBuilder.append("\n\n");
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            throw new AssertionError(e);
        }

        try {
            String filename = "/examples/op/" + name + ".ds";
            InputStream resourceAsStream = getClass().getResourceAsStream(filename);
            if (resourceAsStream != null) {

                stringBuilder.append("```\n");
                stringBuilder.append(CharStreams.toString(new InputStreamReader(resourceAsStream, Charsets.UTF_8)));
                stringBuilder.append("```\n");
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        } catch (RuntimeException e) {
            e.printStackTrace(System.err);
            throw new AssertionError(e);
        }

        stringBuilder.append("\n___\n");

        return stringBuilder.toString();
    }

    @NotNull
    private String bnfSymbol() {
        if ((symbol != null) && (keyword != null)) {
            return "('" + symbol + "'|" + "'" + keyword + "')";
        }
        if ((symbol == null) && (keyword != null)) {
            return "'" + keyword + "'";
        }
        if (symbol != null) {
            return "'" + symbol + "'";
        }
        return "";

    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, keyword);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;
        OpDef opDef = (OpDef) o;
        return Objects.equals(symbol, opDef.symbol) &&
                       Objects.equals(keyword, opDef.keyword);
    }

    public boolean reactive() {
        return reactive;
    }

    public int priority() {
        return priority;
    }

    @NotNull
    public OpDefType type() {
        return type;
    }

    public Boolean pure() {
        return pure;
    }

    public String helpText() {
        return asMarkdown();
    }

    public SourceNodeOptions nodeOptions() {
        return nodeOptions;
    }
}
