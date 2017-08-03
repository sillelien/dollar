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

import java.util.Arrays;
import java.util.concurrent.Callable;

import static com.sillelien.dollar.api.DollarStatic.$;

public class IfOperator implements Map<Token, Map<var, var>> {

    private DollarParser parser;

    public IfOperator(DollarParser parser) {
        this.parser = parser;
    }

    @Override public Map<var, var> map(@NotNull Token token) {
        var lhs = (var) token.value();
        return rhs -> {
            Callable<var> callable = () -> {
                final var lhsFix = lhs._fixDeep();
                if (lhsFix.isBoolean() && lhsFix.isTrue()) {
                    return rhs._fix(2, false);
                } else {
                    return $(false);
                }
            };
            return DollarScriptSupport.createNode( callable, token,
                                                Arrays.asList(lhs, rhs),
                                                "if", parser);
        };
    }
}
