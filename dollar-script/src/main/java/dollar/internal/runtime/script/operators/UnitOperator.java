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
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.api.DollarParser;
import org.jetbrains.annotations.NotNull;
import org.jparsec.Token;
import org.jparsec.functors.Map;

import java.util.Arrays;
import java.util.concurrent.Callable;

import static com.sillelien.dollar.api.DollarStatic.$void;
import static com.sillelien.dollar.api.DollarStatic.fix;
import static dollar.internal.runtime.script.DollarScriptSupport.getVariable;
import static dollar.internal.runtime.script.DollarScriptSupport.inScope;

public class UnitOperator implements Map<Token, var> {
    private final DollarParser parser;
    private final boolean pure;

    public UnitOperator(DollarParser dollarParser, boolean pure) {
        this.parser = dollarParser;
        this.pure = pure;
    }

    @Override public var map(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();
        Callable<var> callable = () -> inScope(pure, "unit", newScope -> {
            if (Builtins.exists(objects[1].toString())) {
                return Builtins.execute(objects[1].toString(), Arrays.asList((var) objects[0]), pure);
            } else {
                final var defaultValue = $void();
                final var variable = getVariable(pure, objects[1].toString(), false, defaultValue, token, parser);
                newScope.setParameter("1", (var) objects[0]);
                return fix(variable, false);
            }
        });
        return DollarScriptSupport.toLambda(callable, token, Arrays.asList((var) objects[0], (var) objects[1]),
                                            "unit", parser);
    }
}
