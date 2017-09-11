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
import dollar.internal.runtime.script.SourceCode;
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
import static dollar.internal.runtime.script.parser.Symbols.DEFINITION;
import static java.util.Collections.singletonList;

public class PureDefinitionOperator implements Function<Token, var> {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("DefinitionOperator");
    private final boolean def;
    @NotNull
    private final DollarParser parser;

    public PureDefinitionOperator(@NotNull DollarParser parser, boolean def) {
        this.parser = parser;
        this.def = def;
    }

    @Override
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
            Type type = Type.of(typeConstraintObj);
            constraint = node(DEFINITION, "definition-constraint", true, NEW_SCOPE, parser,
                              new SourceCode(currentScope(), token), null, new ArrayList<>(),
                              i -> $(scope.parameter("it").getValue().is(type)));
            constraintSource = typeConstraintObj.$S().toUpperCase();
            checkLearntType(token, type, value, MIN_PROBABILITY);

        } else {
            constraint = null;
            constraintSource = null;
        }

        var node = node(DEFINITION, true, parser, token, singletonList(constrain(scope, value, constraint, constraintSource)),
                        i -> Func.definitionFunc(token, (exportObj != null), value, varName, constraint, constraintSource, parser,
                                                 true, true)
        );

        node.$listen(i -> scope.notify(varName.$S()));
        return node;

    }


}
