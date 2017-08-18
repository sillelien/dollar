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

package dollar.internal.runtime.script.operators;

import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.SourceNodeOptions;
import dollar.internal.runtime.script.api.DollarParser;
import org.jetbrains.annotations.NotNull;
import org.jparsec.Token;
import org.jparsec.functors.Map;

public class CastOperator implements Map<Token, Map<? super var, ? extends var>> {

    @NotNull
    private DollarParser parser;

    public CastOperator(@NotNull DollarParser parser, boolean pure) {
        this.parser = parser;
    }

    @NotNull
    @Override
    public Map<? super var, ? extends var> map(@NotNull Token token) {
        var rhs = (var) token.value();
        return lhs -> DollarScriptSupport.reactiveNode(
                "as", SourceNodeOptions.NO_SCOPE, parser, token, lhs,
                args -> lhs.$as(Type.valueOf(rhs.toString().toUpperCase()))
        );
    }
}
