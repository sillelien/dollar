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

import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.SourceSegmentValue;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.Scope;
import org.jparsec.Token;
import org.jparsec.functors.Map;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sillelien.dollar.api.DollarStatic.$;

public class MapOperator implements Map<Token, var> {
    private final Scope scope;
    private final DollarParser dollarParser;
    private final boolean pure;

    public MapOperator(DollarParser dollarParser, Scope scope, boolean pure) {
        this.dollarParser = dollarParser;
        this.scope = scope;
        this.pure = pure;
    }

    @Override public var map(@NotNull Token t) {
        List<var> o = (List<var>) t.value();
        final var
                lambda =
                DollarScriptSupport.wrapLambda(new SourceSegmentValue(scope, t), scope,
                                               i -> dollarParser.inScope(pure, "map", scope, newScope -> {
                                                   if (o.size() == 1) {
                                                       return DollarFactory.blockCollection(o);
                                                   }
                                                   var parallel = i[0];
                                                   Stream<var> stream;
                                                   if (parallel.isTrue()) {
                                                       stream = o.stream().parallel();
                                                   } else {
                                                       stream = o.stream();
                                                   }
                                                   //Not really a map if only one entry unless it's a pair, in fact
                                                   // it's really a block.
                                                   return $(stream.map(v -> v._fix(parallel.isTrue()))
                                                                  .collect(Collectors.toConcurrentMap(
                                                                          v -> v.pair() ? v.getPairKey() : v.$S(),
                                                                          v -> v.pair() ? v.getPairValue() : v)));
                                               }), o, "map");
        for (var value : o) {
            value.$listen(i -> lambda.$notify());
        }
        return lambda;
    }
}
