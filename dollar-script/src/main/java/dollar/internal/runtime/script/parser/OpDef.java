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
import dollar.internal.runtime.script.HasKeyword;
import dollar.internal.runtime.script.HasSymbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class OpDef implements HasSymbol, HasKeyword, Comparable<Object> {
    @Nullable
    private String symbol;
    @Nullable
    private String keyword;
    @Nullable
    private String name;

    private boolean reserved;
    private boolean reactive;
    private int priority;
    @NotNull
    private OpDefType type;

    @Nullable
    private String bnf;

    public OpDef(@NotNull OpDefType type, @Nullable String symbol,
                 @Nullable String keyword,
                 @Nullable String name,
                 boolean reserved,
                 boolean reactive,
                 @Nullable String bnf,
                 int priority) {
        this.type = type;

        this.symbol = symbol;
        this.keyword = keyword;
        this.name = name;
        this.reserved = reserved;
        this.reactive = reactive;
        this.bnf = bnf;
        this.priority = priority;
        if (!reserved && priority == 0) {
            throw new AssertionError("Priority must be > 0");
        }
    }

    @Nullable
    public String keyword() {
        return keyword;
    }

    @Nullable
    public String name() {
        return name;
    }


    @Nullable
    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (this.equals(o)) {
            return 0;
        }
        if (o instanceof OpDef && keyword != null) {
            return keyword.compareTo(String.valueOf(((OpDef) o).keyword()));
        }
        if (o instanceof OpDef && name != null) {
            return name.compareTo(String.valueOf(((OpDef) o).name()));
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
        if (this.symbol == null) {
            if (this.keyword != null) {
                stringBuilder.append("### `").append(keyword).append("`");
            } else {
                stringBuilder.append("### ").append(name);
            }
        } else {
            if (keyword != null) {
                stringBuilder.append("### `").append(keyword).append("` or `").append(symbol).append("`");
            } else {
                stringBuilder.append("### `").append(symbol).append("` (").append(name).append(")");
            }
        }
        stringBuilder.append("      {#op-" + name + "}").append("\n\n");
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
            String filename = "/examples/op/" + this.name + ".md";
            InputStream resourceAsStream = getClass().getResourceAsStream(filename);
            if (resourceAsStream != null) {
                stringBuilder.append("\n\n");
                stringBuilder.append(
                        CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream(filename), Charsets.UTF_8)));
                stringBuilder.append("\n\n");
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new AssertionError(e);
        }

        try {
            String filename = "/examples/op/" + this.name + ".ds";
            InputStream resourceAsStream = getClass().getResourceAsStream(filename);
            if (resourceAsStream != null) {

                stringBuilder.append("```\n");
                stringBuilder.append(CharStreams.toString(
                        new InputStreamReader(getClass().getResourceAsStream(filename), Charsets.UTF_8)));

                stringBuilder.append("```\n");
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new AssertionError(e);
        }


        return stringBuilder.toString();
    }

    @NotNull
    private String bnfSymbol() {
        if (symbol != null && keyword != null) {
            return "('" + symbol + "'|" + "'" + keyword + "')";
        }
        if (symbol == null && keyword != null) {
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
        if (o == null || getClass() != o.getClass()) return false;
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

    public OpDefType type() {
        return type;
    }
}
