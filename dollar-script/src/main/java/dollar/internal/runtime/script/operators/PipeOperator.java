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
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.Builtins;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.exceptions.VariableNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jparsec.Token;
import org.jparsec.functors.Map;

import java.util.function.Function;

import static com.sillelien.dollar.api.DollarStatic.$;
import static dollar.internal.runtime.script.DollarScriptSupport.inScope;
import static java.util.Collections.singletonList;

public class PipeOperator implements Function<Token, Map<var, var>> {

    @NotNull
    private final DollarParser parser;
    private final boolean pure;

    public PipeOperator(@NotNull DollarParser parser, boolean pure) {

        this.parser = parser;
        this.pure = pure;
    }


    @Override
    public Map<var, var> apply(@NotNull Token token) {
        var lhs = (var) token.value();
        return new Map<var, var>() {
            @NotNull
            @Override
            public var map(@NotNull var rhs) {
                Pipeable pipe = i -> inScope(true, pure, "pipe",
                                             newScope -> {
                                                 var lhsFix = lhs._fix(false);
                                                 newScope.setParameter("1", lhsFix);
                                                 Object rhsVal = rhs.toJavaObject();
                                                 if ((rhsVal instanceof String)) {
                                                     String rhsStr = rhsVal.toString();
                                                     if (rhs.getMetaAttribute(
                                                             "__builtin") != null) {
                                                         return Builtins.execute(rhsStr,
                                                                                 singletonList(
                                                                                         lhsFix),
                                                                                 pure);
                                                     } else if (newScope.has(rhsStr)) {
                                                         return newScope.get(
                                                                 rhsVal.toString())._fix(2, false);
                                                     } else {
                                                         throw new VariableNotFoundException(rhsStr,
                                                                                             newScope);
                                                     }
                                                 } else {
                                                     return $(rhsVal);
                                                 }
                                             });
                return DollarScriptSupport.createReactiveNode("pipe",
                                                              parser, token, rhs,
                                                              pipe);
            }
        };
    }
}
