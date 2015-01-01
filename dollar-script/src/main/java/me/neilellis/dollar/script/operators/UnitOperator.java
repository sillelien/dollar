/*
 * Copyright (c) 2014-2015 Neil Ellis
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

import me.neilellis.dollar.api.var;
import me.neilellis.dollar.script.*;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.functors.Map;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.Callable;

import static me.neilellis.dollar.api.DollarStatic.$void;
import static me.neilellis.dollar.api.DollarStatic.fix;
import static me.neilellis.dollar.script.DollarScriptSupport.getVariable;

public class UnitOperator implements Map<Token, var> {
    private final Scope scope;
    private final DollarParser dollarParser;
    private final boolean pure;

    public UnitOperator(DollarParser dollarParser, Scope scope, boolean pure) {
        this.dollarParser = dollarParser;
        this.scope = scope;
        this.pure = pure;
    }

    @Override public var map(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();
        final SourceSegmentValue source = new SourceSegmentValue(scope, token);
        Callable<var> callable = () -> dollarParser.inScope(pure, "unit", scope, newScope -> {
            if (Builtins.exists(objects[1].toString())) {
                return Builtins.execute(objects[1].toString(), Arrays.asList((var) objects[0]), newScope, pure);
            } else {
                final var defaultValue = $void();
                final var variable = getVariable(pure, newScope, objects[1].toString(), false, defaultValue, source);
                newScope.setParameter("1", (var) objects[0]);
                return fix(variable, false);
            }
        });
        return DollarScriptSupport.toLambda(scope, callable, source, Arrays.asList((var) objects[0], (var) objects[1]),
                                            "unit");
    }
}
