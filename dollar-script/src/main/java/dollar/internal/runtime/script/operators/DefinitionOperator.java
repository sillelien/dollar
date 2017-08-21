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

import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.SourceNodeOptions;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

import static com.sillelien.dollar.api.DollarStatic.$;
import static com.sillelien.dollar.api.DollarStatic.$void;
import static dollar.internal.runtime.script.DollarScriptSupport.*;
import static dollar.internal.runtime.script.parser.Symbols.DEFINITION;

public class DefinitionOperator implements Function<Token, Function<? super var, ? extends var>> {
    private final boolean pure;
    @NotNull
    private final DollarParser parser;

    public DefinitionOperator(boolean pure, @NotNull DollarParser parser) {
        this.pure = pure;
        this.parser = parser;
    }

    @Nullable
    public Function<? super var, ? extends var> apply(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();
//        final String constraintSource;
//        if (objects[1] instanceof var) {
//            constraintSource = ((var) objects[1]).source().getSourceSegment();
//        } else {
//            constraintSource = null;
//        }
        Scope scope = currentScope();

        final Object exportObj;

        exportObj = objects[0];

        return (Function<var, var>) v -> {
            var value;
            Object variableNameObj;
            final Object typeConstraintObj;
            if (objects[2] != null && objects[2].toString().equals("def")) {
                variableNameObj = objects[3];
                if (objects.length == 5) {
                    value = (var) objects[4];
                } else {
                    value = v;
                }
                typeConstraintObj = objects[1];

            } else {
                variableNameObj = objects[3];
                if (objects.length == 6) {
                    value = (var) objects[5];
                } else {
                    value = v;
                }
                typeConstraintObj = objects[2];

            }
            var constraint;
            String constraintSource;
            if (typeConstraintObj != null) {
                constraint = DollarScriptSupport.node("definition-constraint", pure, SourceNodeOptions.NEW_SCOPE,
                                                      token, new
                                                                     ArrayList<>(),
                                                      parser, i
                                                                      -> {
                            final Type type = Type.valueOf(
                                    typeConstraintObj.toString().toUpperCase());
                            var it = scope.getParameter("it");
                            return $(it.is(type));
                        }, DEFINITION);
                constraintSource = typeConstraintObj.toString().toUpperCase();
            } else {
                constraint = null;
                constraintSource = null;
            }
            final String variableName = variableNameObj.toString();

            var node = DollarScriptSupport.node(
                    DEFINITION.name(), pure, SourceNodeOptions.NO_SCOPE, Arrays.asList(
                            constrain(scope, value, constraint, constraintSource)), token, parser,
                    args -> {
                        setVariableDefinition(currentScope(), parser, token, pure, true,
                                              variableName,
                                              value,
                                              constraint,
                                              constraintSource
                        );
                        if (exportObj != null && exportObj.toString().equals("export")) {
                            parser.export(variableName, DollarScriptSupport.node(
                                    DEFINITION.name(), pure, SourceNodeOptions.NO_SCOPE, Arrays.asList(value), token, parser,
                                    exportArgs -> value, DEFINITION));
                        }
                        return $void();
                    },
                    DEFINITION);

            node.$listen(i -> scope.notify(variableName));
            return node;
        };
    }
}
