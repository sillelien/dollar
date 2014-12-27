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

import me.neilellis.dollar.script.DollarParser;
import me.neilellis.dollar.script.DollarScriptSupport;
import me.neilellis.dollar.script.Scope;
import me.neilellis.dollar.script.SourceValue;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.functors.Map;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.neilellis.dollar.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class MapOperator implements Map<Token, var> {
    private final Scope scope;
    private final DollarParser dollarParser;
    private boolean pure;

    public MapOperator(DollarParser dollarParser, Scope scope, boolean pure) {
        this.dollarParser = dollarParser;
        this.scope = scope;
        this.pure = pure;
    }

    @Override public var map(Token t) {
        List<var> o = (List<var>) t.value();
        final var
                lambda =
                DollarScriptSupport.wrapLambda(new SourceValue(scope, t), scope,
                                               parallel -> dollarParser.inScope(pure, "map", scope, newScope -> {
            if (o.size() == 1) {
                return DollarFactory.blockCollection(o);
            }
            Stream<me.neilellis.dollar.var> stream;
            if (parallel.isTrue()) {
                stream = o.stream().parallel();
            } else {
                stream = o.stream();
            }
            //Not really a map if only one entry unless it's a pair, in fact it's really a block.
            return $(stream.map(v -> v._fix(parallel.isTrue()))
                           .collect(Collectors.toConcurrentMap(v -> v.pair() ? v.getPairKey() : v.$S(),
                                                               v -> v.pair() ? v.getPairValue() : v)));
                                               }), o, "map");
        for (var value : o) {
            value.$listen(i->lambda.$notify());
        }
        return lambda;
    }
}
