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

package dollar.internal.runtime.script.parser.scope;

import dollar.api.DollarStatic;
import dollar.api.Scope;
import dollar.api.VarKey;
import dollar.api.Variable;
import dollar.api.var;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import dollar.internal.runtime.script.api.exceptions.VariableNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dollar.api.DollarStatic.$void;

public class PureScope extends ScriptScope {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(PureScope.class);

    public PureScope(@NotNull Scope parent, @NotNull String source, @NotNull String name, @Nullable String file) {
        super(parent, source, name, false, false);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Cannot clear a pure scope");
    }

    @NotNull
    @Override
    public var get(@NotNull VarKey key, boolean mustFind) {

        if (DollarStatic.getConfig().debugScope()) {
            log.info("Looking up {} in {}", key, this);
        }
        Scope scope = scopeForKey(key);
        if (scope == null) {
            scope = this;
        } else {
            if (DollarStatic.getConfig().debugScope()) {
                log.info("Found {} in {}", key, scope);
            }
        }
        Variable result = scope.variables().get(key);
        if ((result != null)) {
            if (!result.isPure()) {
                throw new DollarScriptException("Cannot access impure values in a pure expression, put  'pure'  before the " +
                                                        "definition of '" + key + "' (" + this + ")");
            }
            if (result.isReadonly()) {

            } else {
                throw new DollarScriptException("Cannot access non constant values in a pure expression, put 'const' before  the " +
                                                        "definition of '" + key + "' (" + this + ")");
            }
        }
        if (mustFind) {
            if (result == null) {
                throw new VariableNotFoundException(key, this);
            } else {
                return result.getValue();
            }
        } else {
            return (result != null) ? result.getValue() : $void();
        }
    }

    @Override
    public boolean pure() {
        return true;
    }


    @NotNull
    @Override
    public String toString() {
        return id + "(P)" + "->" + parent;
    }


}
