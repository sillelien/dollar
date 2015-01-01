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

package me.neilellis.dollar.script;

import me.neilellis.dollar.api.DollarStatic;
import me.neilellis.dollar.api.var;
import me.neilellis.dollar.script.exceptions.DollarScriptException;
import me.neilellis.dollar.script.exceptions.VariableNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static me.neilellis.dollar.api.DollarStatic.$void;

public class PureScope extends ScriptScope {
    private static final Logger log = LoggerFactory.getLogger(ScriptScope.class);

    public PureScope(@NotNull Scope parent, String source, String name, @Nullable String file) {
        super(parent, file != null ? file : parent.getFile(), source, name);
    }

    @Override public void clear() {
        throw new UnsupportedOperationException("Cannot clear a pure scope");
    }

    @Override public var get(@NotNull String key, boolean mustFind) {
        if (key.matches("[0-9]+")) {
            throw new AssertionError("Cannot get numerical keys, use getParameter");
        }
        if (DollarStatic.config.debugScope()) { log.info("Looking up " + key + " in " + this); }
        Scope scope = getScopeForKey(key);
        if (scope == null) {
            scope = this;
        } else {
            if (DollarStatic.config.debugScope()) { log.info("Found " + key + " in " + scope); }
        }
        Variable result = scope.getVariables().get(key);
        if (result != null && !(result.readonly && result.fixed) && !result.pure) {
            throw new UnsupportedOperationException(
                    "Cannot access non constant values in a pure expression, put either 'pure' or 'const' as " +
                    "appropriate before '" +
                    key +
                    "'");
        }
        if (mustFind) {
            if (result == null) {
                throw new VariableNotFoundException(key, this);
            } else {
                return result.value;
            }
        } else {
            return result != null ? result.value : $void();
        }
    }

    @NotNull @Override public Scope getScopeForParameters() {
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
        if (DollarStatic.config.debugScope()) { log.info("Setting " + key + " in " + scope); }
        if (scope != null && scope.getVariables().containsKey(key) && scope.getVariables().get(key).readonly) {
            throw new DollarScriptException("Cannot change the value of variable " + key + " it is readonly");
        }
        final var fixedValue = fixed ? value._fixDeep() : value;
        if (scope.getVariables().containsKey(key)) {
            final Variable variable = scope.getVariables().get(key);
            if (!variable.isVolatile && variable.thread != Thread.currentThread().getId()) {
                handleError(new DollarScriptException("Concurrency Error: Cannot change the variable " +
                                                      key +
                                                      " in a different thread from that which is created in."));
            }
            if (variable.constraint != null) {
                if (constraint != null) {
                    handleError(new DollarScriptException(
                            "Cannot change the constraint on a variable, attempted to redeclare for " + key));
                }
            }
            variable.value = fixedValue;
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
