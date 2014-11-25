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

import me.neilellis.dollar.script.DollarParser;
import me.neilellis.dollar.script.DollarScriptSupport;
import me.neilellis.dollar.script.ScriptScope;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.functors.Map;

import static me.neilellis.dollar.DollarStatic.fix;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class ForOperator implements Map<Object[], Map<? super var, ? extends var>> {
    private final ScriptScope scope;
    private DollarParser dollarParser;

    public ForOperator(DollarParser dollarParser, ScriptScope scope) {
        this.dollarParser = dollarParser;
        this.scope = scope;
    }

    public Map<? super var, ? extends var> map(Object[] objects) {
        return rhs -> {
            return DollarScriptSupport.wrapReactiveUnary(scope, rhs, () -> {
                return dollarParser.inScope(scope, newScope -> {
                    return ((var) objects[3]).$each(i -> {
                        newScope.set(objects[1].toString(), fix(i, false), false, null);
                        return fix(rhs, false);
                    });
                });
            });
        };
    }
}
