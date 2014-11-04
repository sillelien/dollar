/*
 * Copyright (c) 2014 Neil Ellis
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

package me.neilellis.dollar.script;

import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class TakeOperator extends ScopedVarUnaryOperator {


    public TakeOperator(ScriptScope scope) {
        super(scope, null);
    }


    @Override
    public var map(var from) {
        try {
            return DollarFactory.fromLambda(v -> from.$take());
        } catch (AssertionError e) {
            throw new AssertionError(e + " at '" + source.get() + "'", e);
        } catch (Throwable e) {
            throw new Error(e + " at '" + source.get() + "'");
        }

    }


}
