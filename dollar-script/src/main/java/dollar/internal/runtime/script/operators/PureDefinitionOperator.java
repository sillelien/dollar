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
import dollar.api.SubType;
import dollar.api.Type;
import dollar.api.Value;
import dollar.api.VarKey;
import dollar.api.script.DollarParser;
import dollar.internal.runtime.script.SimpleSubType;
import dollar.internal.runtime.script.parser.Func;
import dollar.internal.runtime.script.parser.SourceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.function.Function;

import static dollar.api.DollarStatic.$;
import static dollar.internal.runtime.script.DollarUtilFactory.util;
import static dollar.internal.runtime.script.api.DollarUtil.MIN_PROBABILITY;
import static dollar.internal.runtime.script.parser.SourceNodeOptions.NEW_SCOPE;
import static dollar.internal.runtime.script.parser.Symbols.DEFINITION;
import static java.util.Collections.singletonList;

public class PureDefinitionOperator implements Function<Token, Value> {
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
    public Value apply(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();
        if (objects == null) {
            return null;
        }
        Scope scope = util().scope();
        final Object exportObj = objects[0];


        Value value;
        Value valueName;
        final Value typeConstraintObj;
        if (def) {
            valueName = (Value) objects[3];
            value = (Value) objects[4];
            typeConstraintObj = (Value) objects[1];

        } else {
            valueName = (Value) objects[4];
            value = (Value) objects[4];
            typeConstraintObj = (Value) objects[1];
        }

        @Nullable Value constraint;
        @Nullable SubType constraintSource;

        log.info("Creating pure variable {}", valueName);
        if (typeConstraintObj != null) {
            Type type = Type.of(typeConstraintObj);
            constraint = util().node(DEFINITION, "definition-constraint", true, NEW_SCOPE, parser,
                                     new SourceImpl(util().scope(), token), null, new ArrayList<>(),
                                     i -> $(scope.parameter(VarKey.IT).getValue().is(type)));
            constraintSource = new SimpleSubType(typeConstraintObj.source());
            util().checkLearntType(token, type, value, MIN_PROBABILITY);

        } else {
            constraint = null;
            constraintSource = null;
        }

        Value node = util().node(DEFINITION, true, parser, token, singletonList(
                util().constrain(scope, value, constraint, constraintSource)),
                                 i -> Func.definitionFunc(token, (exportObj != null), value, valueName, constraint,
                                                          constraintSource, parser,
                                                          true, true)
        );

        node.$listen(i -> scope.notify(VarKey.of(valueName)));
        return node;

    }


}
