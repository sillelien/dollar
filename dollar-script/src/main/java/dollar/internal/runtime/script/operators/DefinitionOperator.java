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
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.function.Function;

import static com.sillelien.dollar.api.DollarStatic.$;
import static com.sillelien.dollar.api.DollarStatic.$void;
import static dollar.internal.runtime.script.DollarScriptSupport.*;
import static dollar.internal.runtime.script.SourceNodeOptions.NEW_SCOPE;
import static dollar.internal.runtime.script.SourceNodeOptions.NO_SCOPE;
import static dollar.internal.runtime.script.parser.Symbols.DEFINITION;
import static java.util.Collections.singletonList;

public class DefinitionOperator implements Function<Token, Function<? super var, ? extends var>> {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("DefinitionOperator");
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

        boolean pureDef = objects[0] != null;
        exportObj = objects[1];

        return (Function<var, var>) v -> {
            var value;
            var variableName;
            final var typeConstraintObj;
            if ((objects.length == 5) && "def".equals(objects[3].toString())) {
                variableName = (var) objects[4];
                value = v;
                typeConstraintObj = (var) objects[2];

            } else if (objects.length == 6) {
                variableName = (var) objects[4];
                value = v;
                typeConstraintObj = (var) objects[3];
            } else {
                throw new AssertionError("Invalid objects length");
            }

            @Nullable var constraint;
            @Nullable String constraintSource;

            boolean createPureVar = pureDef || this.pure;
            if (createPureVar) {
                log.info("Creating pure variable {}", variableName);
            } else {
                log.info("Creating impure variable {}", variableName);
            }
            if (typeConstraintObj != null) {
                constraint = node(DEFINITION, "definition-constraint", createPureVar, NEW_SCOPE,
                                  token, new ArrayList<>(), parser,
                                  i -> $(scope.parameter("it").is(Type.of(typeConstraintObj))));
                constraintSource = typeConstraintObj.$S().toUpperCase();
            } else {
                constraint = null;
                constraintSource = null;
            }

            var node = node(DEFINITION, createPureVar, NO_SCOPE,
                            singletonList(constrain(scope, value, constraint, constraintSource)), token, parser,
                            i -> {
                                String key = variableName.$S();

                                setVariableDefinition(currentScope(), parser, token, createPureVar, true,
                                                      key, value, constraint, constraintSource);

                                if ((exportObj != null) && "export".equals(exportObj.toString())) {
                                    parser.export(key,
                                                  node(DEFINITION, createPureVar, NO_SCOPE,
                                                       singletonList(value), token, parser,
                                                       exportArgs -> value));
                                }
                                return $void();
                            }
            );

            node.$listen(i -> scope.notify(variableName.$S()));
            return node;
        };
    }
}
