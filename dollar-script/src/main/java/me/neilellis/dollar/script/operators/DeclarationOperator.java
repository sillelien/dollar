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

import me.neilellis.dollar.api.Pipeable;
import me.neilellis.dollar.api.Type;
import me.neilellis.dollar.api.var;
import me.neilellis.dollar.script.DollarScriptSupport;
import me.neilellis.dollar.script.Scope;
import me.neilellis.dollar.script.SourceSegmentValue;
import me.neilellis.dollar.script.exceptions.DollarScriptException;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.functors.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.neilellis.dollar.api.DollarStatic.$;
import static me.neilellis.dollar.api.DollarStatic.$void;

public class DeclarationOperator implements Map<Token, Map<? super var, ? extends var>> {
    private final Scope scope;
    private final boolean pure;

    public DeclarationOperator(Scope scope, boolean pure) {
        this.scope = scope;
        this.pure = pure;
    }

    @Nullable public Map<? super var, ? extends var> map(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();
        final String constraintSource;
        if (objects[1] instanceof var) {
            constraintSource = ((var) objects[1])._source().getSourceSegment();
        } else {
            constraintSource = null;
        }
        return new Map<var, var>() {
            @NotNull public var map(var v) {
                var value;
                if (objects.length == 5) {
                    //Pure prefix in action here so objects[4] is the pure expression instead of the parameter v
                    // passed in
                    value = (var) objects[4];
                } else {
                    value = v;
                }
                var constraint;
                if (objects[1] != null) {
                    constraint =
                            DollarScriptSupport.wrapLambda(new SourceSegmentValue(scope, token), scope, i -> {
                                final Type type = Type.valueOf(objects[1].toString().toUpperCase());
                                var it = scope.getParameter("it");
                                return $(it.is(type));
                            }, null, null);
                } else {
                    constraint = null;
                }
                final String variableName = objects[2].toString();
                Pipeable
                        action =
                        i -> scope.set(variableName, value, pure, constraint, constraintSource, false, false, pure);
                try {
                    action.pipe($void());
                } catch (Exception e) {
                    throw new DollarScriptException(e);
                }
                value.$listen(i -> scope.notify(variableName));
                if (objects[0] != null) {
                    scope.getDollarParser().export(variableName, value);
                }
                return $void();
            }
        };
    }
}
