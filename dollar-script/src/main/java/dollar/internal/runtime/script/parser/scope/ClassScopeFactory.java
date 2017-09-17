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

package dollar.internal.runtime.script.parser.scope;

import dollar.api.DollarClass;
import dollar.api.Scope;
import dollar.api.Value;
import dollar.api.types.DollarFactory;
import dollar.internal.runtime.script.obj.DollarObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static dollar.internal.runtime.script.DollarUtilFactory.util;

public class ClassScopeFactory implements DollarClass {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(ClassScopeFactory.class);
    @NotNull
    private final List<Value> expression;
    @NotNull
    private final String name;

    public ClassScopeFactory(@NotNull Scope parent,
                             @NotNull String name,
                             @NotNull List<Value> expression,
                             boolean root,
                             boolean parallel) {

        this.name = name;
        this.expression = expression;
    }

    @NotNull
    @Override
    public Value instance(List<Value> params) {
        ScriptScope subScope = new ScriptScope(util().scope(), "class-" + name, false, true);
        return util().inScope(true, subScope, scope -> {
            util().addParameterstoCurrentScope(scope, params);
            Value constructor = DollarFactory.fromList(expression.stream().map(Value::$fixDeep).collect(Collectors.toList()));
//            System.err.println(scope.variables());
            return (Value) new DollarObject(name, constructor, scope.variables());
        }).orElseThrow(() -> new AssertionError("Optional should not be null here"));
    }

}
