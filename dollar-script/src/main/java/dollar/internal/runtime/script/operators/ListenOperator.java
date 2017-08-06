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

import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.Operator;
import dollar.internal.runtime.script.api.DollarParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.functors.Binary;

import java.util.Arrays;

import static com.sillelien.dollar.api.DollarStatic.$;

public class ListenOperator implements Binary<var>, Operator {
    private final boolean pure;
    @Nullable
    private SourceSegment source;
    @NotNull
    private DollarParser parser;


    public ListenOperator(boolean pure, @NotNull DollarParser parser) {
        this.pure = pure;
        this.parser = parser;
    }


    @NotNull
    @Override
    public var map(@NotNull var lhs, @NotNull var rhs) {
        Pipeable callable = args -> {
            return $(lhs.$listen(i -> DollarScriptSupport.inScope(false, pure, "listen", newScope -> {
                newScope.setParameter("1", i[0]);
                //todo: change to read
                return rhs._fixDeep(false);
            })));
        };
        return DollarScriptSupport.createNode("listen", parser, DollarScriptSupport.currentScope(),
                                              source, Arrays.asList(lhs, rhs), callable);
    }

    @Override public void setSource(@NotNull SourceSegment source) {
        this.source = source;
    }
}
