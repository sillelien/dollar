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
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Unary;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class UnaryOp implements Unary<var>, Operator {
    private final boolean immediate;
    protected Scope scope;
    protected Source source;
    private Map<var, var> function;


    public UnaryOp(Scope scope, Map<var, var> function) {
        if (scope == null) {
            throw new NullPointerException();
        }
        this.scope = scope;
        this.function = function;
        this.immediate = false;
    }

    public UnaryOp(Scope scope, boolean immediate, Map<var, var> function) {
        if (scope == null) {
            throw new NullPointerException();
        }
        this.immediate = immediate;
        this.function = function;
        this.scope = scope;
    }

    @Override
    public var map(var from) {

        if (immediate) {
            return function.map(from);
        }

        //Lazy evaluation
        final var lambda = DollarScriptSupport.wrapReactiveUnary(scope, from, () -> function.map(from), source);
        return lambda;

    }


    @Override
    public void setSource(Source source) {
        this.source = source;
    }
}
