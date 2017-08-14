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
import dollar.internal.runtime.script.SourceNodeOptions;
import dollar.internal.runtime.script.api.DollarParser;
import org.jetbrains.annotations.NotNull;
import org.jparsec.Token;
import org.jparsec.functors.Map;

import java.util.Arrays;

import static com.sillelien.dollar.api.DollarStatic.$;
import static dollar.internal.runtime.script.DollarScriptSupport.createNode;

public class WhileOperator implements Map<Token, Map<? super var, ? extends var>> {
    private final DollarParser parser;
    private final boolean pure;

    public WhileOperator(DollarParser dollarParser, boolean pure) {
        this.parser = dollarParser;
        this.pure = pure;
    }

    @NotNull
    public Map<? super var, ? extends var> map(@NotNull Token token) {
        var lhs = (var) token.value();
        return rhs -> {
            return createNode("while", SourceNodeOptions.NEW_SCOPE, parser, token, Arrays.asList(lhs, rhs),
                              i -> {
                                  while (lhs.isTrue()) {
                                      rhs._fixDeep();
                                  }
                                  return $(false);
                              });
        };
    }
}
