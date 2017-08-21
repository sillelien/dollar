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

import java.util.Objects;

public class SymbolDef implements HasSymbol, Comparable<Object> {
    @NotNull
    private final String symbol;
    private final boolean reserved;

    public SymbolDef(@NotNull String symbol, boolean reserved) {
        this.symbol = symbol;
        this.reserved = reserved;
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (equals(o)) {
            return 0;
        }
        if (o instanceof HasSymbol) {
            return symbol.compareTo(String.valueOf(((HasSymbol) o).symbol()));
        }
        if (o instanceof HasKeyword) {
            return symbol.compareTo(String.valueOf(((HasKeyword) o).keyword()));
        }
        return symbol.compareTo(String.valueOf(o));
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymbolDef symbolDef = (SymbolDef) o;
        return Objects.equals(symbol, symbolDef.symbol);
    }

    public boolean isReserved() {
        return reserved;
    }
}
