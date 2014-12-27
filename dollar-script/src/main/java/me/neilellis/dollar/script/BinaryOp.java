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

import me.neilellis.dollar.var;
import org.codehaus.jparsec.functors.Binary;
import org.codehaus.jparsec.functors.Map2;

import static me.neilellis.dollar.script.DollarScriptSupport.wrapReactive;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class BinaryOp implements Binary<var>, Operator {
    private final boolean immediate;
    private final Map2<var, var, var> function;
    private final Scope scope;
    private SourceSegment source;
    private String operation;


    public BinaryOp(String operation, Map2<var, var, var> function, Scope scope) {
        this.operation = operation;
        this.function = function;
        this.scope = scope;
        this.immediate = false;
    }

    public BinaryOp(boolean immediate, Map2<var, var, var> function, Scope scope, String operation) {
        this.immediate = immediate;
        this.function = function;
        this.scope = scope;
        this.operation = operation;
    }

    @Override
    public var map(var lhs, var rhs) {
        if (immediate) {
            return function.map(lhs, rhs);
        }
        //Lazy evaluation
        return wrapReactive(scope, () -> function.map(lhs, rhs), source, operation, lhs, rhs);
    }

    @Override
    public void setSource(SourceSegment source) {
        this.source = source;
    }
}
