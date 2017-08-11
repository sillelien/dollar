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

import static com.sillelien.dollar.api.DollarStatic.fix;
import static dollar.internal.runtime.script.DollarScriptSupport.currentScope;

public class ForOperator implements Map<Token, Map<? super var, ? extends var>> {
    private final DollarParser dollarParser;
    private final boolean pure;

    public ForOperator(DollarParser dollarParser, boolean pure) {
        this.dollarParser = dollarParser;
        this.pure = pure;
    }

    public Map<? super var, ? extends var> map(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();
        String constraintSource = null;
        return rhs -> {
            return DollarScriptSupport.createReactiveNode(true, false, "for", dollarParser, token, rhs, args -> {
                return ((var) objects[3]).$each(i -> {
                    currentScope().set(objects[1].toString(),
                                       fix(i[0], false),
                                       false, null,
                                       constraintSource, false,
                                       false,
                                       pure);
                    return rhs._fixDeep(false);
                });
            });
        };
    }
}
