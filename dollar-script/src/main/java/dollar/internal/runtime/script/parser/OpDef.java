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
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class OpDef implements HasSymbol, HasKeyword, Comparable<Object> {
    @NotNull
    private String symbol;
    @Nullable
    private String keyword;
    @Nullable
    private String name;
    @Nullable
    private String description;

    private boolean reserved;

    public OpDef(@NotNull String symbol,
                 @Nullable String keyword,
                 @Nullable String name,
                 @Nullable String description, boolean reserved) {

        this.symbol = symbol;
        this.keyword = keyword;
        this.name = name;
        this.description = description;
        this.reserved = reserved;
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
    public String description() {
        return description;
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
        if (o instanceof HasSymbol && symbol != null) {
            return symbol.compareTo(String.valueOf(((HasSymbol) o).symbol()));
        }
        if (o instanceof HasKeyword && keyword != null) {
            return keyword.compareTo(String.valueOf(((HasKeyword) o).keyword()));
        }

        return symbol.compareTo(String.valueOf(o));
    }

    public boolean isReserved() {
        return reserved;
    }

    public String asMarkdown() {
        StringBuilder stringBuilder = new StringBuilder();
        if (this.keyword != null) {
            stringBuilder.append("### ").append(keyword);
        } else {
            stringBuilder.append("### ").append(name);
        }
        if (symbol != null) {
            stringBuilder.append(" (").append(symbol).append(")\n\n");
        } else {
            stringBuilder.append("\n\n");

        }
        if (description != null) {
            stringBuilder.append(description).append("\n\n\n");
        }
        return stringBuilder.toString();
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
}
