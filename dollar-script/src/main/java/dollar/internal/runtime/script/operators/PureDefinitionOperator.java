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

import dollar.api.Scope;
import dollar.api.Type;
import dollar.api.var;
import dollar.internal.runtime.script.Func;
import dollar.internal.runtime.script.api.DollarParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.function.Function;

import static dollar.api.DollarStatic.$;
import static dollar.internal.runtime.script.DollarScriptSupport.*;
import static dollar.internal.runtime.script.SourceNodeOptions.NEW_SCOPE;
import static dollar.internal.runtime.script.SourceNodeOptions.NO_SCOPE;
import static dollar.internal.runtime.script.parser.Symbols.DEFINITION;
import static java.util.Collections.singletonList;

public class PureDefinitionOperator implements Function<Token, var> {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("DefinitionOperator");
    @NotNull
    private final DollarParser parser;
    private final boolean def;

    public PureDefinitionOperator(@NotNull DollarParser parser, boolean def) {
        this.parser = parser;
        this.def = def;
    }

    @Nullable
    public var apply(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();
        if (objects == null) {
            return null;
        }
        Scope scope = currentScope();
        final Object exportObj = objects[0];


        var value;
        var varName;
        final var typeConstraintObj;
        if (def) {
            varName = (var) objects[3];
            value = (var) objects[4];
            typeConstraintObj = (var) objects[1];

        } else {
            varName = (var) objects[4];
            value = (var) objects[4];
            typeConstraintObj = (var) objects[1];
        }

        @Nullable var constraint;
        @Nullable String constraintSource;

        log.info("Creating pure variable {}", varName);
        if (typeConstraintObj != null) {
            constraint = node(DEFINITION, "definition-constraint", true, NEW_SCOPE,
                              token, new ArrayList<>(), parser,
                              i -> $(scope.parameter("it").is(Type.of(typeConstraintObj))));
            constraintSource = typeConstraintObj.$S().toUpperCase();
        } else {
            constraint = null;
            constraintSource = null;
        }

        var node = node(DEFINITION, true, NO_SCOPE,
                        singletonList(constrain(scope, value, constraint, constraintSource)), token, parser,
                        i -> Func.definitionFunc(token, (exportObj != null), value, varName, constraint, constraintSource, parser,
                                                 true, true)
        );

        node.$listen(i -> scope.notify(varName.$S()));
        return node;

    }


}
