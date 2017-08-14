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

import com.sillelien.dollar.api.collections.ImmutableList;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.api.DollarParser;
import org.jetbrains.annotations.NotNull;
import org.jparsec.Token;
import org.jparsec.functors.Map;

import java.util.List;

class ListOperator implements Map<Token, var> {
    private final DollarParser dollarParser;
    private final boolean pure;

    public ListOperator(DollarParser parser, boolean pure) {
        this.dollarParser = parser;
        this.pure = pure;
    }

    @NotNull
    @Override
    public var map(@NotNull Token t) {
        List<var> o = (List<var>) t.value();
        final var lambda = DollarScriptSupport.createNode("list", SourceNodeOptions.SCOPE_WITH_CLOSURE, t, o, dollarParser,
                                                          vars -> DollarFactory.fromList(new ImmutableList<>(o))
        );
        for (var v : o) {
            v.$listen(i -> lambda.$notify());
        }
        return lambda;

    }
}
