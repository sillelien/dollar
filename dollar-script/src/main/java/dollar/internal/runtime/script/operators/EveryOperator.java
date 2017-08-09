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

import com.sillelien.dollar.api.execution.DollarExecutor;
import com.sillelien.dollar.api.plugin.Plugins;
import com.sillelien.dollar.api.time.Scheduler;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.Scope;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.jparsec.functors.Map;

import static com.sillelien.dollar.api.DollarStatic.$;
import static com.sillelien.dollar.api.DollarStatic.$void;
import static dollar.internal.runtime.script.DollarScriptSupport.createReactiveNode;
import static dollar.internal.runtime.script.DollarScriptSupport.currentScope;

public class EveryOperator implements Map<Token, var> {
    private final DollarParser dollarParser;
    @Nullable
    private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);
    private final boolean pure;

    public EveryOperator(DollarParser dollarParser, boolean pure) {
        this.dollarParser = dollarParser;
        this.pure = pure;
    }

    @Override
    public var map(Token token) {
        final int[] count = new int[]{-1};
        Object[] objects = (Object[]) token.value();
        return createReactiveNode(true, "every", dollarParser, token, (var) objects[3], args -> {
            Scope scope= currentScope();
            Scheduler.schedule(i -> {
                count[0]++; // William Gibson
//                System.out.println("COUNT "+count[0]);
                return DollarScriptSupport.inScope(true, scope, newScope -> {
                    try {
//                    System.err.println(newScope);
                        newScope.setParameter("1", $(count[0]));
                        if (objects[1] instanceof var && ((var) objects[1]).isTrue()) {
                            Scheduler.cancel(i[0].$S());
                            return i[0];
                        } else if (objects[2] instanceof var && ((var) objects[2]).isTrue()) {
                            return $void();
                        } else {
                            return ((var) objects[3])._fixDeep();
                        }
                    } catch (Exception e) {
                        return DollarFactory.failure(e);
                    }

                });
            }, ((long) (((var) objects[0]).toDouble() * 24.0 * 60.0 * 60.0 * 1000.0)));
            return $void();
        });

    }
}
