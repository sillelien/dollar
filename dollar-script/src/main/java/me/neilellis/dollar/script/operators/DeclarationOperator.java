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
import me.neilellis.dollar.script.ScriptScope;
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
    private final ScriptScope scope;

    public DeclarationOperator(ScriptScope scope) {this.scope = scope;}

    public Map<? super var, ? extends var> map(Object[] objects) {

        return new Map<var, var>() {
            public var map(var rhs) {
                var constraint;
                if (objects[0] != null) {
                    constraint =
                            DollarFactory.fromLambda(i -> {
                                final Type type = Type.valueOf(objects[0].toString().toUpperCase());
                                var it = scope.getParameter("it");
                                boolean constraintValue = objects[1] == null || ((var) objects[1]).isTrue();
                                return $(it.is(type) && constraintValue);
                            });
                } else {
                    constraint = (var) objects[1];
                }
                Pipeable action = i -> scope.set(objects[2].toString(), rhs, false, constraint);
                try {
                    action.pipe($void());
                } catch (Exception e) {
                    throw new DollarScriptException(e);
                }
                rhs.$listen(action);
                return $void();
            }
        };
    }
}
