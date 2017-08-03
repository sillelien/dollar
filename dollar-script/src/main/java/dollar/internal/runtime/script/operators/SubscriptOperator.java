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
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.jparsec.functors.Map;

import java.util.Arrays;
import java.util.concurrent.Callable;

public class SubscriptOperator implements Map<Token, Map<? super var, ? extends var>> {

    private DollarParser parser;

    public SubscriptOperator(DollarParser parser) {
        this.parser = parser;
    }

    @Nullable @Override public Map<? super var, ? extends var> map(@NotNull Token token) {
        Object[] rhs = (Object[]) token.value();
        return lhs -> {
            if (rhs[1] == null) {
                return DollarScriptSupport.createReactiveNode(() -> lhs.$get(
                        ((var) rhs[0])), token, "subscript", lhs, (var) rhs[0], parser);
            } else {
                Callable<var> callable = () -> lhs.$set((var) rhs[0], rhs[1]);
                return DollarScriptSupport.createNode(callable, token,
                                                    Arrays.asList(lhs, (var) rhs[0], (var) rhs[1]),
                                                    "subscript-assignment", parser);
            }
        };
    }
}
