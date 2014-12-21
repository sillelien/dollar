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

import me.neilellis.dollar.Type;
import me.neilellis.dollar.script.DollarScriptSupport;
import me.neilellis.dollar.script.Scope;
import me.neilellis.dollar.script.SourceValue;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.functors.Map;

import java.util.List;

import static me.neilellis.dollar.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class IsOperator implements Map<Token, Map<? super var, ? extends var>> {
    private final Scope scope;

    public IsOperator(Scope scope) {this.scope = scope;}

    @Override public Map<? super var, ? extends var> map(Token token) {
        List<var> rhs = (List<var>) token.value();
        return lhs -> DollarScriptSupport.wrapReactive(scope, () -> {
            for (var value : rhs) {
                if (lhs.is(Type.valueOf(value.toString()))) {
                    return $(true);
                }
            }
            return $(false);
        }, new SourceValue(scope, token), "is " + rhs, lhs);
    }
}
