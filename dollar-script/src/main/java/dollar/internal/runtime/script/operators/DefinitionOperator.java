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
import dollar.internal.runtime.script.DollarUtilFactory;
import dollar.internal.runtime.script.SimpleSubType;
import dollar.internal.runtime.script.parser.Func;
import dollar.internal.runtime.script.parser.SourceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

import static dollar.api.DollarStatic.$;
import static dollar.api.types.meta.MetaConstants.CONSTRAINT_SOURCE;
import static dollar.internal.runtime.script.api.DollarUtil.MIN_PROBABILITY;
import static dollar.internal.runtime.script.parser.SourceNodeOptions.NEW_SCOPE;
import static dollar.internal.runtime.script.parser.Symbols.DEFINITION;

public class DefinitionOperator implements Function<Token, Function<? super Value, ? extends Value>> {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("DefinitionOperator");
    private final boolean def;
    @NotNull
    private final DollarParser parser;
    private final boolean pure;

    public DefinitionOperator(boolean pure, @NotNull DollarParser parser, boolean def) {
        this.pure = pure;
        this.parser = parser;
        this.def = def;
    }

    @Override
    @Nullable
    public Function<? super Value, ? extends Value> apply(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();
        Scope scope = DollarUtilFactory.util().scope();

        final Object exportObj = objects[1];

        return (Function<Value, Value>) rhs -> {
            Value value;
            Value variableName;
            final Value typeConstraintObj;
            @Nullable Value constraint;
            @Nullable SubType constraintSource;
            boolean readonly;

            if (def) {
                variableName = (Value) objects[3];
                value = rhs;
                typeConstraintObj = (Value) objects[1];
                readonly = true;

            } else {
                variableName = (Value) objects[3];
                value = rhs;
                typeConstraintObj = (Value) objects[2];
                readonly = objects[1] != null;
            }


            if (pure) {
                log.info("Creating pure variable {}", variableName);
            } else {
                log.info("Creating impure variable {}", variableName);
            }

            if (typeConstraintObj != null) {
                Type type = Type.of(typeConstraintObj);
                constraint = DollarUtilFactory.util().node(DEFINITION, "definition-constraint", pure, NEW_SCOPE, parser,
                                                           new SourceImpl(DollarUtilFactory.util().scope(), token), null,
                                                           new ArrayList<>(),
                                                           i -> $(scope.parameter(VarKey.IT).getValue().is(type)));
                DollarUtilFactory.util().checkLearntType(token, type, rhs, MIN_PROBABILITY);
                SourceImpl meta = typeConstraintObj.meta(CONSTRAINT_SOURCE);
                if (meta != null) {
                    constraintSource = new SimpleSubType(meta);
                } else {
                    constraintSource = null;
                }
            } else {
                constraint = null;
                constraintSource = null;
            }

            boolean finalReadonly = readonly;

            Value node = DollarUtilFactory.util().node(DEFINITION, pure, parser, token,
                                                       Arrays.asList(rhs,
                                                                     DollarUtilFactory.util().constrain(scope, value, constraint,
                                                                                                        constraintSource)),
                                                       i -> Func.definitionFunc(token, (exportObj != null), value, variableName,
                                                                                constraint, constraintSource,
                                                                                parser, pure, finalReadonly)
            );

            node.$listen(i -> scope.notify(VarKey.of(variableName)));
            return node;
        };
    }
}
