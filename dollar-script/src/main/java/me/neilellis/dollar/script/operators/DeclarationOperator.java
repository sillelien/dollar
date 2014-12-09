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

import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.Type;
import me.neilellis.dollar.script.Scope;
import me.neilellis.dollar.script.exceptions.DollarScriptException;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.functors.Map;

import static me.neilellis.dollar.DollarStatic.$;
import static me.neilellis.dollar.DollarStatic.$void;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DeclarationOperator implements Map<Object[], Map<? super var, ? extends var>> {
    private final Scope scope;
    private boolean pure;

    public DeclarationOperator(Scope scope, boolean pure) {
        this.scope = scope;
        this.pure = pure;
    }

    public Map<? super var, ? extends var> map(Object[] objects) {

        return new Map<var, var>() {
            public var map(var v) {
                var value;
                if (objects.length == 5) {
                    //Pure prefix in action here so objects[4] is the pure expression instead of the parameter v
                    // passed in
                    value = (var) objects[4];
                } else {
                    value = v;
                }
                var constraint;
                if (objects[1] != null) {
                    constraint =
                            DollarFactory.fromLambda(i -> {
                                final Type type = Type.valueOf(objects[1].toString().toUpperCase());
                                var it = scope.getParameter("it");
                                return $(it.is(type));
                            });
                } else {
                    constraint = null;
                }
                final String variableName = objects[2].toString();
                Pipeable action = i -> scope.set(variableName, value, pure, constraint, false, false, pure);
                try {
                    action.pipe($void());
                } catch (Exception e) {
                    throw new DollarScriptException(e);
                }
                value.$listen(i -> scope.notify(variableName));
                if (objects[0] != null) {
                    scope.getDollarParser().export(variableName, value);
                }
                return $void();
            }
        };
    }
}
