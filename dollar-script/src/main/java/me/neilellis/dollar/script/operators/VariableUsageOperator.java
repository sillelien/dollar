/*
 * Copyright (c) 2014 Neil Ellis
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

import me.neilellis.dollar.script.DollarScriptSupport;
import me.neilellis.dollar.script.Scope;
import me.neilellis.dollar.script.SourceValue;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.functors.Map;

import java.util.Arrays;
import java.util.concurrent.Callable;

import static me.neilellis.dollar.DollarStatic.$void;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class VariableUsageOperator implements Map<Token, Map<? super var, ? extends var>> {
    private final Scope scope;
    private boolean pure;

    public VariableUsageOperator(Scope scope, boolean pure) {
        this.scope = scope;
        this.pure = pure;
    }

    @Override public Map<? super var, ? extends var> map(Token token) {

        final SourceValue source = new SourceValue(scope, token);
        return rhs -> {
            Callable<var> callable = () -> {
                return DollarScriptSupport.getVariable(pure, scope, rhs.toString(), false, $void(), source);
            };
            return DollarScriptSupport.toLambda(scope, callable, source, Arrays.asList(rhs),
                                                "variable-usage--" + rhs._source().getTokenSource());
        };
    }
}
