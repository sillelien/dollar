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
import com.sillelien.dollar.script.api.Scope;
import org.jparsec.Token;
import org.jparsec.functors.Map;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.Callable;

public class ReadOperator implements Map<Token, Map<? super var, ? extends var>> {
    private final Scope scope;

    public ReadOperator(Scope scope) {this.scope = scope;}

    @NotNull @Override
    public Map<? super var, ? extends var> map(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();
        return new Map<var, var>() {
            @Override
            public var map(@NotNull var rhs) {
                Callable<var> callable = () -> rhs.$read(objects[1] != null,
                                                         objects[2] != null);
                return DollarScriptSupport.toLambda(scope, callable, new SourceSegmentValue(scope, token),
                                                    Arrays.asList(rhs),
                                                    "read:" + objects[1] + ":" + objects[2]);
            }
        };
    }
}
