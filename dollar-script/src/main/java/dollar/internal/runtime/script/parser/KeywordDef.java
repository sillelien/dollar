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
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class KeywordDef implements HasKeyword, Comparable<Object> {
    @NotNull
    private String keyword;
    private boolean reserved;
    @Nullable
    private String description;
    private String bnf;

    public KeywordDef(@NotNull String keyword, boolean reserved, @Language("md") @Nullable String description, String bnf) {
        this.keyword = keyword;
        this.reserved = reserved;
        this.description = description;
        this.bnf = bnf;
    }

    @Override
    public String keyword() {
        return keyword;
    }

    @Override
    public int compareTo(@NotNull Object o) {
        if (this.equals(o)) {
            return 0;
        }
        if (o instanceof HasKeyword) {
            return keyword.compareTo(String.valueOf(((HasKeyword) o).keyword()));
        }
        if (o instanceof HasSymbol) {
            return keyword.compareTo(String.valueOf(((HasSymbol) o).symbol()));
        }
        return keyword.compareTo(String.valueOf(o));
    }


    public boolean isReserved() {
        return reserved;
    }

    @NotNull
    public String asMarkdown() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("### ").append(keyword).append("\n\n");
        if (description != null) {
            stringBuilder.append(description).append("\n\n\n");
        }
        return stringBuilder.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyword);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KeywordDef that = (KeywordDef) o;
        return Objects.equals(keyword, that.keyword);
    }
}
