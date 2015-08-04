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

package com.sillelien.dollar.script.operators;

import com.sillelien.dollar.api.var;
import com.sillelien.dollar.script.DollarScriptSupport;
import com.sillelien.dollar.script.SourceSegmentValue;
import com.sillelien.dollar.script.api.DollarParser;
import com.sillelien.dollar.script.api.Scope;
import org.codehaus.jparsec.Token;
import org.codehaus.jparsec.functors.Map;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.Callable;

import static com.sillelien.dollar.api.DollarStatic.$;

public class WhileOperator implements Map<Token, Map<? super var, ? extends var>> {
    private final Scope scope;
    private final DollarParser parser;
    private final boolean pure;

    public WhileOperator(DollarParser dollarParser, Scope scope, boolean pure) {
        this.parser = dollarParser;
        this.scope = scope;
        this.pure = pure;
    }

    public Map<? super var, ? extends var> map(@NotNull Token token) {
        var lhs = (var) token.value();
        return rhs -> {
            Callable<var> callable = () -> parser.inScope(pure, "while", scope, newScope -> {
                        while (lhs.isTrue()) {
                            rhs._fixDeep();
                        }
                        return $(false);
                    });
            return DollarScriptSupport.toLambda(scope, callable, new SourceSegmentValue(scope, token),
                                                Arrays.asList(lhs, rhs), "while");
        };
    }
}
