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

import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.Scope;
import dollar.internal.runtime.script.api.Variable;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import dollar.internal.runtime.script.api.exceptions.VariableNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sillelien.dollar.api.DollarStatic.$void;

public class PureScope extends ScriptScope {
    private static final Logger log = LoggerFactory.getLogger(ScriptScope.class);

    public PureScope(@NotNull Scope parent, String source, String name, @Nullable String file) {
        super(parent, file != null ? file : parent.getFile(), source, name, false);
    }

    @Override public void clear() {
        throw new UnsupportedOperationException("Cannot clear a pure scope");
    }

    @NotNull @Override public var get(@NotNull String key, boolean mustFind) {
        if (key.matches("[0-9]+")) {
            throw new AssertionError("Cannot get numerical keys, use getParameter");
        }
        if (DollarStatic.getConfig().debugScope()) { log.info("Looking up " + key + " in " + this); }
        Scope scope = getScopeForKey(key);
        if (scope == null) {
            scope = this;
        } else {
            if (DollarStatic.getConfig().debugScope()) { log.info("Found " + key + " in " + scope); }
        }
        Variable result = (Variable) scope.getVariables().get(key);
        if (result != null && !(result.isReadonly() && result.isFixed()) && !result.isPure()) {
            throw new UnsupportedOperationException(
                    "Cannot access non constant values in a pure expression, putValue either 'pure' or 'const' as " +
                    "appropriate before '" +
                    key +
                    "'");
        }
        if (mustFind) {
            if (result == null) {
                throw new VariableNotFoundException(key, this);
            } else {
                return result.getValue();
            }
        } else {
            return result != null ? result.getValue() : $void();
        }
    }

    @Nullable @Override public Scope getScopeForParameters() {
        return this;
    }

    @NotNull @Override
    public var set(@NotNull String key, @NotNull var value, boolean readonly, @Nullable var constraint,
                   String constraintSource,
                             boolean isVolatile, boolean fixed,
                             boolean pure) {
        if (isVolatile) {
            throw new UnsupportedOperationException("Cannot have volatile variables in a pure expression");
        }
        if (key.matches("[0-9]+")) {
            throw new AssertionError("Cannot set numerical keys, use setParameter");
        }
        Scope scope = getScopeForKey(key);
        if (scope != null && scope != this) {
            throw new UnsupportedOperationException("Cannot modify variables outside of a pure scope");
        }
        if (scope == null) {
            scope = this;
        }
        if (DollarStatic.getConfig().debugScope()) { log.info("Setting " + key + " in " + scope); }
        if (scope != null && scope.getVariables().containsKey(key) && ((Variable)scope.getVariables().get(key)).isReadonly()) {
            throw new DollarScriptException("Cannot change the value of variable " + key + " it is readonly");
        }
        final var fixedValue = fixed ? value._fixDeep() : value;
        if (scope.getVariables().containsKey(key)) {
            final Variable variable = ((Variable)scope.getVariables().get(key));
            if (!variable.isVolatile() && variable.getThread() != Thread.currentThread().getId()) {
                handleError(new DollarScriptException("Concurrency Error: Cannot change the variable " +
                                                      key +
                                                      " in a different thread from that which is created in."));
            }
            if (variable.getConstraint() != null) {
                if (constraint != null) {
                    handleError(new DollarScriptException(
                            "Cannot change the constraint on a variable, attempted to redeclare for " + key));
                }
            }
            variable.setValue(fixedValue);
        } else {
            scope.getVariables()
                 .put(key, new Variable(fixedValue, readonly, constraint, constraintSource, false, fixed, pure));
        }
        scope.notifyScope(key, fixedValue);
        return value;
    }

    @NotNull @Override
    public String toString() {
        return id + "(P)->" + parent;
    }
}
