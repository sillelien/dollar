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

package dollar.internal.runtime.script;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OperatorDefinition implements HasSymbol, HasKeyword {
    @NotNull
    private String symbol;
    @Nullable
    private String keyword;
    @Nullable
    private String name;
    @Nullable
    private String description;

    public OperatorDefinition(@NotNull String symbol,
                              @Nullable String keyword,
                              @Nullable String name,
                              @Nullable String description) {

        this.symbol = symbol;
        this.keyword = keyword;
        this.name = name;
        this.description = description;
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

    @NotNull
    @Override
    public String symbol() {
        return symbol;
    }
}
