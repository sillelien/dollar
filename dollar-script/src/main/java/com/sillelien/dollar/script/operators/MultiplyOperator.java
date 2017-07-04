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
import com.sillelien.dollar.script.api.Scope;
import org.jparsec.functors.Map2;

public class MultiplyOperator {
    private final Map2<var, var, var> function;
    private final Scope scope;

    public MultiplyOperator(Map2<var, var, var> function, Scope scope) {
        this.function = function;
        this.scope = scope;
    }

    public Map2<var, var, var> getFunction() {
        return function;
    }

    public Scope getScope() {
        return scope;
    }
}
