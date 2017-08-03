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

import com.sillelien.dollar.api.Scope;
import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.api.DollarParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.jparsec.functors.Map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

import static com.sillelien.dollar.api.DollarStatic.$;
import static dollar.internal.runtime.script.DollarScriptSupport.currentScope;

public class DefinitionOperator implements Map<Token, Map<? super var, ? extends var>> {
    private final boolean pure;
    private DollarParser parser;

    public DefinitionOperator(boolean pure, DollarParser parser) {
        this.pure = pure;
        this.parser = parser;
    }

    @Nullable
    public Map<? super var, ? extends var> map(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();
//        final String constraintSource;
//        if (objects[1] instanceof var) {
//            constraintSource = ((var) objects[1])._source().getSourceSegment();
//        } else {
//            constraintSource = null;
//        }
        Scope scope = currentScope();

        final Object exportObj;
        final Object typeConstraintObj;

        exportObj = objects[0];
        typeConstraintObj = objects[1];

        return new Map<var, var>() {
            @NotNull
            public var map(var v) {
                var value;
                Object variableNameObj;
                if (objects[2] != null && objects[2].toString().equals("def")) {
                    variableNameObj = objects[3];
                    if (objects.length == 5) {
                        value = (var) objects[4];
                    } else {
                        value = (var) v;
                    }

                } else {
                    variableNameObj = objects[2];
                    if (objects.length == 5) {
                        value = (var) objects[4];
                    } else {
                        value = (var) v;
                    }
                }
                var constraint;
                String constraintSource;
                if (typeConstraintObj != null) {
                    constraint =
                            DollarScriptSupport.createNode(token, i -> {
                                final Type type = Type.valueOf(typeConstraintObj.toString().toUpperCase());
                                var it = scope.getParameter("it");
                                return $(it.is(type));
                            }, new ArrayList<>(), "definition", parser);
                    constraintSource= typeConstraintObj.toString().toUpperCase();
                } else {
                    constraint = null;
                    constraintSource= null;
                }
                final String variableName = variableNameObj.toString();
                Callable action = () -> DollarScriptSupport.setVariable(scope, variableName, value, true, constraint, constraintSource, false, false, pure, true, token, parser);

                var node = DollarScriptSupport.createNode(action, token, Arrays.asList(DollarScriptSupport.constrain(scope, value, constraint, constraintSource)), "assignment", parser);

                node.$listen(i -> scope.notify(variableName));
                if (exportObj != null && exportObj.equals("export")) {
                    parser.export(variableName, node);
                }
                return node;
            }
        };
    }
}
