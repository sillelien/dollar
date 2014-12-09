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

import java.util.List;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface Builtin<T> {

    T execute(boolean pure, List<var> args, Scope scope);


    interface DollarStyle extends Builtin<var> {
        var execute(boolean pure, List<var> args, Scope scope);
    }

    interface JavaStyle<T> extends Builtin<T> {
        T execute(boolean pure, List<var> args, Scope scope);
    }

    class BuiltinImpl implements Builtin<var> {

        private final int minargs;
        private final int maxargs;
        private final Builtin function;
        private boolean pure;
        private String name;

        public BuiltinImpl(String name, Builtin function, int minargs, int maxargs, boolean pure) {
            this.name = name;
            this.function = function;
            this.minargs = minargs;
            this.maxargs = maxargs;
            this.pure = pure;
        }

        public boolean isPure() {
            return pure;
        }

        @Override
        public var execute(boolean pure, List<var> args, Scope scope) {
            if (!this.pure && pure) {
                throw new DollarScriptException("Cannot use an impure function '" + name + "' in a pure expression");
            }

            if (args.size() < minargs) {
                throw new IllegalArgumentException("Minimum number of arguments for '" + name + "' is " + minargs);
            }
            if (args.size() > maxargs) {
                throw new IllegalArgumentException("Maximum number of arguments for '" + name + "' is " + maxargs);
            }
            Object result = function.execute(pure, args, scope);
            if (result instanceof var) {
                return (var) result;
            }
            return DollarFactory.fromValue(result);
        }

    }

}
