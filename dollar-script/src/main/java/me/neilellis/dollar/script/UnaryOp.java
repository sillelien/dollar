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

import me.neilellis.dollar.script.exceptions.DollarScriptException;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Unary;

import java.util.function.Supplier;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class UnaryOp implements Unary<var>, Operator {
    private final boolean immediate;
    protected Map<var, var> function;
    protected Supplier<String> source;
    protected ScriptScope scope;


    public UnaryOp(ScriptScope scope, Map<var, var> function) {
        this.scope = scope;
        this.function = function;
        this.immediate = false;
    }

    public UnaryOp(boolean immediate, Map<var, var> function) {
        this.immediate = immediate;
        this.function = function;
    }

    @Override
    public var map(var from) {

        try {
            if (immediate) {
                return function.map(from);
            }

            //Lazy evaluation
            final var lambda = DollarFactory.fromLambda(v -> {
                try {
                    return function.map(from);
                } catch (AssertionError e) {
                    throw new AssertionError(e + " at '" + source.get() + "'", e);
                }
            });
            from.$listen(i -> {
                lambda.$notify();
                return i;
            });
            return lambda;
        } catch (AssertionError e) {
            return scope.getDollarParser().getErrorHandler().handle(scope, source.get(), e);
        } catch (DollarScriptException e) {
            return scope.getDollarParser().getErrorHandler().handle(scope, source.get(), e);
        } catch (Exception e) {
            return scope.getDollarParser().getErrorHandler().handle(scope, source.get(), e);
        }


    }


    @Override
    public void setSource(Supplier<String> source) {
        this.source = source;
    }
}
