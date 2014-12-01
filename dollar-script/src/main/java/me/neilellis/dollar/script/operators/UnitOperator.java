/*
 * Copyright (c) 2014 Neil Ellis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.neilellis.dollar.script.operators;

import me.neilellis.dollar.script.Builtins;
import me.neilellis.dollar.script.DollarParser;
import me.neilellis.dollar.script.DollarScriptSupport;
import me.neilellis.dollar.script.ScriptScope;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.functors.Map;

import java.util.Arrays;

import static me.neilellis.dollar.DollarStatic.$void;
import static me.neilellis.dollar.DollarStatic.fix;
import static me.neilellis.dollar.script.DollarScriptSupport.getVariable;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class UnitOperator implements Map<Object[], var> {
    private final ScriptScope scope;
    private DollarParser dollarParser;

    public UnitOperator(DollarParser dollarParser, ScriptScope scope) {
        this.dollarParser = dollarParser;
        this.scope = scope;
    }

    @Override public var map(Object[] objects) {
        return DollarScriptSupport.wrapUnary(scope, () -> dollarParser.inScope("unit", scope, newScope -> {
            if (Builtins.exists(objects[1].toString())) {
                return Builtins.execute(objects[1].toString(), Arrays.asList((var) objects[0]), newScope);
            } else {
                final var defaultValue = $void();
                final var variable = getVariable(newScope, objects[1].toString(), false, defaultValue);
                newScope.setParameter("1", (var) objects[0]);
                return fix(variable, false);
            }
        }));
    }
}
