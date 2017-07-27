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

import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.SourceSegmentValue;
import dollar.internal.runtime.script.api.Scope;
import org.jparsec.Token;
import org.jparsec.functors.Map;

import java.util.Arrays;
import java.util.concurrent.Callable;

import static com.sillelien.dollar.api.DollarStatic.$void;

public class VariableUsageOperator implements Map<Token, Map<? super var, ? extends var>> {
    private final Scope scope;
    private final boolean pure;

    public VariableUsageOperator(Scope scope, boolean pure) {
        this.scope = scope;
        this.pure = pure;
    }

    @Override public Map<? super var, ? extends var> map(Token token) {

        final SourceSegmentValue source = new SourceSegmentValue(scope, token);
        return rhs -> {
            Callable<var> callable = () -> {
                return DollarScriptSupport.getVariable(pure, scope, rhs.toString(), false, $void(), source);
            };
            return DollarScriptSupport.toLambda(scope, callable, source, Arrays.asList(rhs),
                                                "variable-usage--" + rhs._source().getSourceSegment());
        };
    }
}
