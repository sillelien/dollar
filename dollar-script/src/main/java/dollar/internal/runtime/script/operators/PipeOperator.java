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
import dollar.internal.runtime.script.Builtins;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.Scope;
import dollar.internal.runtime.script.api.exceptions.VariableNotFoundException;
import org.jparsec.functors.Map;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

import static com.sillelien.dollar.api.DollarStatic.$;

public class PipeOperator implements Map<var, Map<? super var, ? extends var>> {
    private final Scope scope;
    private final DollarParser dollarParser;
    private final boolean pure;

    public PipeOperator(DollarParser dollarParser, Scope scope, boolean pure) {
        this.dollarParser = dollarParser;
        this.scope = scope;
        this.pure = pure;
    }

    @Override public Map<? super var, ? extends var> map(@NotNull var rhs) {
        return lhs -> dollarParser.inScope(pure, "pipe", scope, newScope -> {
            var lhsFix = lhs._fix(false);
            newScope.setParameter("1", lhsFix);
            Object rhsVal = rhs.toJavaObject();
            if ((rhsVal instanceof String)) {
                String rhsStr = rhsVal.toString();
                if (rhs.getMetaAttribute("__builtin") != null) {
                    return Builtins.execute(rhsStr, Collections.singletonList(lhsFix),
                                            newScope, pure);
                } else if (newScope.has(rhsStr)) {
                    return newScope.get(rhsVal.toString())._fix(2, false);
                } else {
                    throw new VariableNotFoundException(rhsStr, newScope);
                }
            } else {
                return $(rhsVal);
            }
        });
    }
}
