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

import me.neilellis.dollar.script.DollarScriptSupport;
import me.neilellis.dollar.script.ScriptScope;
import me.neilellis.dollar.script.UnaryOp;
import me.neilellis.dollar.var;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class VariableOperator extends UnaryOp {


    public VariableOperator(ScriptScope scope) {
        super(scope, null);
    }


    @Override
    public var map(var from) {

        var lambda = DollarScriptSupport.wrapReactiveUnary(scope, from, () -> {
            String key = from.$S();
            boolean numeric = from.isNumber();

            List<ScriptScope> scopes = new ArrayList<ScriptScope>(scope.getDollarParser().scopes());
            Collections.reverse(scopes);
            for (ScriptScope scriptScope : scopes) {
                if (numeric) {
                    if (scriptScope.hasParameter(key)) {
                        return scriptScope.getParameter(key);
                    }
                } else {
                    if (scriptScope.has(key)) {
                        return scriptScope.get(key);
                    }
                }
            }

            if (numeric) {
                return scope.getParameter(key);
            }
            return scope.get(key);
        });
        scope.listen(from.$S(), lambda);
        lambda.setMetaAttribute("variable", from.$S());
        return lambda;

    }

}
