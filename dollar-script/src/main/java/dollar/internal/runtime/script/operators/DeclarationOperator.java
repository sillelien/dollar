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

import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import com.sillelien.dollar.api.Scope;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.jparsec.functors.Map;

import static com.sillelien.dollar.api.DollarStatic.$;
import static com.sillelien.dollar.api.DollarStatic.$void;
import static dollar.internal.runtime.script.DollarScriptSupport.currentScope;

public class DeclarationOperator implements Map<Token, Map<? super var, ? extends var>> {
    private final boolean pure;
    private DollarParser parser;

    public DeclarationOperator(boolean pure, DollarParser parser) {
        this.pure = pure;
        this.parser = parser;
    }

    @Nullable public Map<? super var, ? extends var> map(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();
        final String constraintSource;
        if (objects[1] instanceof var) {
            constraintSource = ((var) objects[1])._source().getSourceSegment();
        } else {
            constraintSource = null;
        }
        Scope scope = currentScope();

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
                            DollarScriptSupport.wrapLambda(token, i -> {
                                final Type type = Type.valueOf(objects[1].toString().toUpperCase());
                                var it = scope.getParameter("it");
                                return $(it.is(type));
                            }, null, null, parser);
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
                    parser.export(variableName, value);
                }
                return $void();
            }
        };
    }
}
