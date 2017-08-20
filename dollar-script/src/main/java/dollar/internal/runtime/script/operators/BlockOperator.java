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
import dollar.internal.runtime.script.api.DollarParser;
import org.jetbrains.annotations.NotNull;
import org.jparsec.Token;
import org.jparsec.functors.Map;

import java.util.List;
import java.util.stream.IntStream;

import static com.sillelien.dollar.api.DollarStatic.$void;
import static dollar.internal.runtime.script.SourceNodeOptions.SCOPE_WITH_CLOSURE;

public class BlockOperator implements Map<Token, var> {

    @NotNull
    private final DollarParser dollarParser;
    private final boolean pure;

    public BlockOperator(@NotNull DollarParser dollarParser, boolean pure) {
        this.dollarParser = dollarParser;
        this.pure = pure;
    }

    @NotNull
    private static var TO_BLOCK(@NotNull List<var> l) {
        if (l.isEmpty()) {
            return $void();
        } else {
            IntStream.range(0, l.size() - 1).forEach(i -> l.get(i)._fixDeep(false));
            return l.get(l.size() - 1);
        }
    }

    @NotNull
    @Override
    public var map(@NotNull Token token) {
        //noinspection unchecked
        List<var> l = (List<var>) token.value();
        return DollarScriptSupport.node("block", pure,
                                        SCOPE_WITH_CLOSURE, dollarParser, token, l, parallel -> TO_BLOCK(l));
    }

}
