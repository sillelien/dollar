/*
 *    Copyright (c) 2014-2017 Neil Ellis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dollar.internal.runtime.script;

import dollar.api.Scope;
import dollar.api.types.DollarFactory;
import dollar.api.var;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Builtin<T> {

    @NotNull
    T execute(boolean pure, @NotNull List<var> args, @NotNull Scope scope);


    interface DollarStyle extends Builtin<var> {
        @NotNull
        var execute(boolean pure, @NotNull List<var> args, @NotNull Scope scope);
    }

    interface JavaStyle<T> extends Builtin<T> {
        @NotNull
        T execute(boolean pure, @NotNull List<var> args, @NotNull Scope scope);
    }

    class BuiltinImpl<R> implements Builtin<var> {

        private final int minargs;
        private final int maxargs;
        @NotNull
        private final Builtin function;
        private final boolean pure;
        @NotNull
        private final String name;

        public BuiltinImpl(@NotNull String name, @NotNull Builtin<R> function, int minargs, int maxargs, boolean pure) {
            this.name = name;
            this.function = function;
            this.minargs = minargs;
            this.maxargs = maxargs;
            this.pure = pure;
        }

        public boolean isPure() {
            return pure;
        }

        @NotNull @Override
        public var execute(boolean pure, @NotNull List<var> args, @NotNull Scope scope) {
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
