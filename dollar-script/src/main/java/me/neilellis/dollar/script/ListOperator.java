/*
 * Copyright (c) 2014-2015 Neil Ellis
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

import me.neilellis.dollar.api.types.DollarFactory;
import me.neilellis.dollar.api.var;
import me.neilellis.dollar.script.api.Scope;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.functors.Map;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class ListOperator implements Map<Token, var> {
    private final Scope scope;
    private final DollarParser dollarParser;
    private final boolean pure;

    public ListOperator(DollarParser dollarParser, Scope scope, boolean pure) {
        this.dollarParser = dollarParser;
        this.scope = scope;
        this.pure = pure;
    }

    @Override public var map(@NotNull Token t) {
        List<var> o = (List<var>) t.value();
        final var lambda = DollarScriptSupport.wrapLambda(new SourceSegmentValue(scope, t), scope,
                                                          parallel -> dollarParser.inScope(pure, "list", scope,
                                                                                           newScope -> {
                                                                                               Scope scope2 = newScope;
                                                                                               return DollarFactory
                                                                                                       .fromValue(
                                                                                                               o);
                                                                                           }), o, "list");
        for (var v : o) {
            v.$listen(i -> lambda.$notify());
        }
        return lambda;

    }
}
