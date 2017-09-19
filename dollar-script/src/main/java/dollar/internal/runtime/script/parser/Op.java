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
import com.google.common.base.MoreObjects;
import com.google.common.io.CharStreams;
import com.vdurmont.emoji.EmojiParser;
import dollar.api.Type;
import dollar.api.Value;
import dollar.internal.runtime.script.api.HasKeyword;
import dollar.internal.runtime.script.api.HasSymbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Function;

import static dollar.internal.runtime.script.parser.SourceNodeOptions.*;

public class Op implements HasSymbol, HasKeyword, Comparable<Object> {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(Op.class);
    @Nullable
    private final String keyword;
    @NotNull
    private final String name;
    @NotNull
    private final SourceNodeOptions nodeOptions;
    private final int priority;
    @Nullable
    private final Boolean pure;
    private final boolean reactive;
    private final boolean reserved;
    @Nullable
    private final String symbol;
    @NotNull
    private final OpType type;
    @Nullable
    private final Function<Value[], Type> typeFunction;
    @Nullable
    private String bnf;
    @Nullable
    private String emoji;

    public Op(@NotNull OpType type,
              @Nullable String symbol,
              @Nullable String keyword,
              @NotNull String name,
              boolean reserved,
              boolean reactive,
              @Nullable String bnf,
              int priority,
              @Nullable Boolean pure,
              @NotNull SourceNodeOptions nodeOptions,
              @Nullable String emoji,
              @Nullable Function<Value[], Type> typeFunction) {
        this.type = type;
        this.typeFunction = typeFunction;
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
                        " ![Parallel Execution](https://img.shields.io/badge/order-parallel-blue.svg?style=flat-square)");
            } else {
                stringBuilder.append(
                        " ![Serial Execution](https://img.shields.io/badge/order-serial-green.svg?style=flat-square)");
            }
        } else {
            stringBuilder.append(
                    " ![Inherited Execution](https://img.shields.io/badge/order-inherited-lightgrey.svg?style=flat-square)");
        }
        stringBuilder.append("\n\n");
        if (bnf == null) {
            if (type == OpType.PREFIX) {
                bnf = "" + bnfSymbol() + " <expression>";
            }
            if (type == OpType.POSTFIX) {
                bnf = "<expression> " + bnfSymbol() + "";
            }
            if (type == OpType.BINARY) {
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
    public int compareTo(@NotNull Object o) {
        if (equals(o)) {
            return 0;
        }
        if ((o instanceof Op) && (keyword != null)) {
            return keyword.compareTo(String.valueOf(((Op) o).keyword()));
        }
        if ((o instanceof Op) && (name != null)) {
            return name.compareTo(((Op) o).name());
        }

        return name.compareTo(String.valueOf(o));
    }

    @NotNull
    public String emoji() {
        return emoji;
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(symbol, keyword, name, reserved, reactive, priority, type, pure, nodeOptions,
                                                       typeFunction, emoji, bnf);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Op op = (Op) o;
        return reserved == op.reserved &&
                       reactive == op.reactive &&
                       priority == op.priority &&
                       com.google.common.base.Objects.equal(symbol, op.symbol) &&
                       com.google.common.base.Objects.equal(keyword, op.keyword) &&
                       com.google.common.base.Objects.equal(name, op.name) &&
                       type == op.type &&
                       com.google.common.base.Objects.equal(pure, op.pure) &&
                       com.google.common.base.Objects.equal(nodeOptions, op.nodeOptions) &&
                       com.google.common.base.Objects.equal(typeFunction, op.typeFunction) &&
                       com.google.common.base.Objects.equal(emoji, op.emoji) &&
                       com.google.common.base.Objects.equal(bnf, op.bnf);
    }

    @NotNull
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                       .add("symbol", symbol)
                       .add("keyword", keyword)
                       .add("name", name)
                       .add("reserved", reserved)
                       .add("reactive", reactive)
                       .add("priority", priority)
                       .add("type", type)
                       .add("pure", pure)
                       .add("nodeOptions", nodeOptions)
                       .add("typeFunction", typeFunction)
                       .add("emoji", emoji)
                       .add("bnf", bnf)
                       .toString();
    }

    @NotNull
    public String helpText() {
        return asMarkdown();
    }

    public boolean isReserved() {
        return reserved;
    }

    @Override
    @Nullable
    public String keyword() {
        return keyword;
    }

    @NotNull
    public String name() {
        return name;
    }

    @NotNull
    public SourceNodeOptions nodeOptions() {
        return nodeOptions;
    }

    public int priority() {
        return priority;
    }

    @Nullable
    public Boolean pure() {
        return pure;
    }

    public boolean reactive() {
        return reactive;
    }

    @Nullable
    @Override
    public String symbol() {
        return symbol;
    }

    @NotNull
    public OpType type() {
        return type;
    }

    @Nullable
    public Type typeFor(@NotNull Value... Values) {
        if (typeFunction == null) {
            return null;
        }
        try {
            return typeFunction.apply(Values);
        } catch (Exception e) {
            log.error(type + ":" + e.getMessage(), e);
            throw e;
        }
    }

    public boolean validForPure(boolean pure) {
        return !pure || (this.pure && pure);
    }
}
