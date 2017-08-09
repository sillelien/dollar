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

import java.util.Arrays;

import static com.sillelien.dollar.api.DollarStatic.$;
import static com.sillelien.dollar.api.DollarStatic.$void;

public class WhenOperator implements Map<Token, var> {

    private DollarParser parser;

    public WhenOperator(DollarParser parser) {
        this.parser = parser;
    }

    @Override
    public var map(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();
        var lhs = (var) objects[0];
        var rhs = (var) objects[1];
        var lambda = DollarScriptSupport.createNode(true, "when", token, Arrays.asList(lhs, rhs), parser, i -> lhs.isTrue() ? $((Object) rhs.toJavaObject()) : $void()
        );
        lhs.$listen(i -> {
//            lambda.$notify();
            if (lhs.isTrue()) {
                return $((Object) rhs.toJavaObject());
            }
            return $void();
        });
        return lambda;
    }
}
