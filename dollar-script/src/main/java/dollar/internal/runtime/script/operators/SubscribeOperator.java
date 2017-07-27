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

import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.Operator;
import dollar.internal.runtime.script.api.Scope;
import org.jparsec.functors.Binary;
import org.jetbrains.annotations.NotNull;

import static com.sillelien.dollar.api.DollarStatic.fix;
import static dollar.internal.runtime.script.DollarScriptSupport.wrapReactive;

public class SubscribeOperator implements Binary<var>, Operator {
    private final Scope scope;
    private final boolean pure;
    private SourceSegment source;


    public SubscribeOperator(Scope scope, boolean pure) {
        this.scope = scope;
        this.pure = pure;
    }


    @Override
    public var map(@NotNull var lhs, var rhs) {

        return wrapReactive(scope, () -> lhs.$subscribe(
                                    i -> scope.getDollarParser().inScope(pure, "subscribe", scope, newScope -> {
                                        final var it = fix(i[0], false);
                                        scope.getDollarParser().currentScope().setParameter("1", it);
                                        scope.getDollarParser().currentScope().setParameter("it", it);
                                        return fix(rhs, false);
                                    })), source, "subscribe", lhs, rhs
        );

    }

    @Override
    public void setSource(SourceSegment source) {
        this.source = source;
    }
}
