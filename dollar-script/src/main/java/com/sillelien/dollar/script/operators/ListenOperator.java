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

import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.var;
import com.sillelien.dollar.script.DollarScriptSupport;
import com.sillelien.dollar.script.Operator;
import com.sillelien.dollar.script.api.Scope;
import org.jparsec.functors.Binary;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.Callable;

import static com.sillelien.dollar.api.DollarStatic.$;

public class ListenOperator implements Binary<var>, Operator {
    private final Scope scope;
    private final boolean pure;
    private SourceSegment source;


    public ListenOperator(Scope scope, boolean pure) {
        this.scope = scope;
        this.pure = pure;
    }


    @Override
    public var map(@NotNull var lhs, @NotNull var rhs) {
        Callable<var> callable = () -> {
            return $(lhs.$listen(i -> scope.getDollarParser().inScope(pure, "listen", scope, newScope -> {
                newScope.setParameter("1", i[0]);
                //todo: change to read
                return rhs._fixDeep(false);
            })));
        };
        return DollarScriptSupport.toLambda(scope, callable, source, Arrays.asList(lhs, rhs), "listen");
    }

    @Override public void setSource(SourceSegment source) {
        this.source = source;
    }
}
