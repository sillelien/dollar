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

package me.neilellis.dollar.script;

import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.functors.Map;

import java.util.List;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
class ListOperator implements Map<Token, var> {
    private final Scope scope;
    private final DollarParser dollarParser;
    private boolean pure;

    public ListOperator(DollarParser dollarParser, Scope scope, boolean pure) {
        this.dollarParser = dollarParser;
        this.scope = scope;
        this.pure = pure;
    }

    @Override public var map(Token t) {
        List<var> o = (List<var>) t.value();
        final var lambda = DollarScriptSupport.wrapLambda(new SourceValue(scope, t), scope,
                                                          parallel -> dollarParser.inScope(pure, "list", scope,
                                                                                           newScope -> {
                                                                                               Scope scope2 = newScope;
                                                                                               return DollarFactory
                                                                                                       .fromValue(
                                                                                                               o);
                                                                                           }));
        for (var v : o) {
            v.$listen(i -> lambda.$notify());
        }
        return lambda;

    }
}
