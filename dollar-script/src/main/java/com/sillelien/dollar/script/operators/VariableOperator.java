/*
 * Copyright (c) 2014-2015 Neil Ellis
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

package com.sillelien.dollar.script.operators;

import com.sillelien.dollar.api.var;
import com.sillelien.dollar.script.DollarScriptSupport;
import com.sillelien.dollar.script.UnaryOp;
import com.sillelien.dollar.script.api.Scope;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VariableOperator extends UnaryOp {


    public VariableOperator(Scope scope) {
        super("variable-operator", scope, null);
    }


    @Override
    public var map(@NotNull var from) {

        var lambda = DollarScriptSupport.wrapReactive(scope, () -> {
            String key = from.$S();
            boolean numeric = from.number();

            List<Scope> scopes = new ArrayList<>(scope.getDollarParser().scopes());
            Collections.reverse(scopes);
            for (Scope scriptScope : scopes) {
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
        }, source, operation, from);
        scope.listen(from.$S(), lambda);
        lambda.setMetaAttribute("variable", from.$S());
        return lambda;

    }

}
