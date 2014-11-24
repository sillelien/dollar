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
import me.neilellis.dollar.script.ScriptScope;
import me.neilellis.dollar.script.exceptions.VariableNotFoundException;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.functors.Map;

import java.util.Collections;

import static me.neilellis.dollar.DollarStatic.$;
import static me.neilellis.dollar.DollarStatic.fix;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class PipeOperator implements Map<var, Map<? super var, ? extends var>> {
    private final ScriptScope scope;
    private DollarParser dollarParser;

    public PipeOperator(DollarParser dollarParser, ScriptScope scope) {
        this.dollarParser = dollarParser;
        this.scope = scope;
    }

    @Override public Map<? super var, ? extends var> map(var rhs) {
        return lhs -> dollarParser.inScope(scope, newScope -> {
            var lhsFix = fix(lhs, false);
            newScope.setParameter("1", lhsFix);
            Object rhsVal = rhs.$();
            if ((rhsVal instanceof String)) {
                String rhsStr = rhsVal.toString();
                if (Builtins.exists(rhsStr)) {
                    return Builtins.execute(rhsStr, Collections.singletonList(lhsFix),
                                            newScope);
                } else if (scope.has(rhsStr)) {
                    return fix(scope.get(rhsVal.toString()), false);
                } else {
                    throw new VariableNotFoundException(rhsStr);
                }
            } else {
                return $(rhsVal);
            }
        });
    }
}
