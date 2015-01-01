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

package me.neilellis.dollar.script.operators;

import me.neilellis.dollar.api.var;
import org.codehaus.jparsec.functors.Map;

public class FunctionCallOperator implements Map<Object[], var> {
    @Override
    public var map(Object[] objects) {
        return ((Map<? super var, ? extends var>) objects[1]).map(((var) objects[0])._fix(2, false));
    }
}
