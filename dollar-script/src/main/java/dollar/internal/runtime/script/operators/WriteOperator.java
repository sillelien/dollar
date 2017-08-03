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

import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.api.DollarParser;
import org.jetbrains.annotations.NotNull;
import org.jparsec.Token;
import org.jparsec.functors.Map;

public class WriteOperator implements Map<Token, Map<? super var, ? extends var>> {

    private DollarParser parser;

    public WriteOperator(DollarParser parser) {
        this.parser = parser;
    }

    @NotNull
    @Override
    public Map<? super var, ? extends var> map(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();
        return new Map<var, var>() {
            @Override
            public var map(@NotNull var rhs) {
                return DollarScriptSupport.createReactiveNode(
                        () -> rhs.$write((var) objects[1],
                                objects[2] != null,
                                objects[3] != null
                        ), token, "write:" + objects[2] + ":" + objects[3], (var) objects[1], rhs, parser);
            }
        };
    }
}
