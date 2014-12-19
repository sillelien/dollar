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

import me.neilellis.dollar.script.Operator;
import me.neilellis.dollar.script.Scope;
import me.neilellis.dollar.script.Source;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.functors.Binary;

import static me.neilellis.dollar.DollarStatic.fix;
import static me.neilellis.dollar.script.DollarScriptSupport.wrapReactiveBinary;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class SubscribeOperator implements Binary<var>, Operator {
    private final Scope scope;
    private Source source;
    private boolean pure;


    public SubscribeOperator(Scope scope, boolean pure) {
        this.scope = scope;
        this.pure = pure;
    }


    @Override
    public var map(var lhs, var rhs) {

        return wrapReactiveBinary(scope, lhs, rhs,
                                  () -> lhs.$subscribe(
                                          i -> scope.getDollarParser().inScope(pure, "subscribe", scope, newScope -> {
                                      final var it = fix(i, false);
                                      scope.getDollarParser().currentScope().setParameter("1", it);
                                      scope.getDollarParser().currentScope().setParameter("it", it);
                                      return fix(rhs, false);
                                          })), source);

    }

    @Override
    public void setSource(Source source) {
        this.source = source;
    }
}
