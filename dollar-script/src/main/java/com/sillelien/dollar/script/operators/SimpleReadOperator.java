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

import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.var;
import com.sillelien.dollar.script.DollarScriptSupport;
import com.sillelien.dollar.script.UnaryOp;
import com.sillelien.dollar.script.api.Scope;
import org.jetbrains.annotations.NotNull;

public class SimpleReadOperator extends UnaryOp {


    public SimpleReadOperator(Scope scope) {
        super("simple-read", scope, null);
    }


    @Override
    public var map(@NotNull var from) {
        return DollarScriptSupport.wrapReactive(scope, () -> DollarFactory.fromURI(from).$read(), source, operation,
                                                from);
    }

}
