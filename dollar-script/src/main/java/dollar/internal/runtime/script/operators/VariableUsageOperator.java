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
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.SourceNodeOptions;
import dollar.internal.runtime.script.api.DollarParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.jparsec.functors.Map;

import java.util.Arrays;

import static com.sillelien.dollar.api.DollarStatic.$void;

public class VariableUsageOperator implements Map<Token, Map<? super var, ? extends var>> {
    private final boolean pure;
    private DollarParser parser;
    private boolean numeric;

    public VariableUsageOperator(boolean pure, DollarParser parser, boolean numeric) {
        this.pure = pure;
        this.parser = parser;
        this.numeric = numeric;
    }

    @Nullable
    @Override
    public Map<? super var, ? extends var> map(@NotNull Token token) {

        return rhs -> {

            Pipeable callable = i -> DollarScriptSupport.getVariable(pure, rhs.toString(),
                                                                     numeric, $void(), token, parser);

            return DollarScriptSupport.createNode("variable-usage--" + rhs._source().getSourceSegment(),
                                                  SourceNodeOptions.NO_SCOPE, parser, token, Arrays.asList(rhs), callable);
        };
    }
}
