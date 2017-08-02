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
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.api.DollarParser;
import com.sillelien.dollar.api.Scope;
import org.jparsec.functors.Map;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.sillelien.dollar.api.DollarStatic.*;

public class CollectOperator implements Map<Object[], var> {
    private final DollarParser dollarParser;
    private final boolean pure;

    public CollectOperator(DollarParser dollarParser, boolean pure) {
        this.dollarParser = dollarParser;
        this.pure = pure;
    }

    @NotNull @Override public var map(Object[] objects) {
        ((var) objects[0]).$listen(new Pipeable() {
            private final int[] count = new int[]{-1};
            private final ArrayList<var> collected = new ArrayList<>();
            final Scope scope= DollarScriptSupport.currentScope();

            @Override public var pipe(var... in) throws Exception {
                var value = fix((var) objects[0], false);
                count[0]++;
                return DollarScriptSupport.inScope(pure, "collect", newScope -> {
                    newScope.setParameter("count", $(count[0]));
                    newScope.setParameter("it", value);
                    //noinspection StatementWithEmptyBody
                    if (objects[2] instanceof var && ((var) objects[2]).isTrue()) {
                        //skip
                    } else {
                        collected.add(value);
                    }
                    var returnValue = $void();
                    newScope.setParameter("collected", $(collected));
                    final boolean endValue = objects[1] instanceof var && ((var) objects[1]).isTrue();
                    if (endValue) {
                        collected.clear();
                        count[0] = -1;
                        returnValue = ((var) objects[3])._fixDeep();
                    }
                    return returnValue;
                });
            }
        });
        return $void();
    }
}
