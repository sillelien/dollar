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

import me.neilellis.dollar.Type;
import me.neilellis.dollar.script.DollarScriptSupport;
import me.neilellis.dollar.script.ScriptScope;
import me.neilellis.dollar.script.exceptions.DollarScriptException;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.functors.Map;

import static me.neilellis.dollar.DollarStatic.$;
import static me.neilellis.dollar.DollarStatic.fix;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class AssignmentOperator implements Map<Object[], Map<? super var, ? extends var>> {
    private final ScriptScope scope;

    public AssignmentOperator(ScriptScope scope) {this.scope = scope;}

    public Map<? super var, ? extends var> map(Object[] objects) {

        return new Map<var, var>() {
            public var map(var rhs) {
                var constraint;
                if (objects[2] != null) {
                    final Type type = Type.valueOf(objects[2].toString().toUpperCase());
                    constraint = DollarFactory.fromLambda(i -> {
                        return $(scope.getDollarParser().currentScope().getParameter("it")
                                      .is(type) &&
                                 (objects[3] == null || ((var) objects[3]).isTrue()));
                    });
                } else {
                    constraint = (var) objects[3];

                }
                final String varName = objects[4].toString();
                return DollarScriptSupport.wrapBinary(scope, () -> {
                    var useConstraint;
                    if (constraint != null) {
                        useConstraint = constraint;
                    } else {
                        useConstraint = scope.getConstraint(varName);
                    }
                    return scope.getDollarParser().inScope(scope, newScope -> {
                        final var rhsFixed = fix(rhs, false);
                        if (useConstraint != null) {
                            newScope.setParameter("it", rhsFixed);
                            newScope.setParameter("previous", scope.get(varName));
                            if (useConstraint.isFalse()) {
                                newScope.handleError(new DollarScriptException(
                                        "Constraint failed for variable " + varName + ""));
                            }
                        }
                        if (objects[0] != null) {
                            scope.getDollarParser().export(varName, rhsFixed);
                        }
                        return scope.set(varName, rhsFixed, (objects[1] != null),
                                         constraint);
                    });
                });
            }
        };
    }
}
