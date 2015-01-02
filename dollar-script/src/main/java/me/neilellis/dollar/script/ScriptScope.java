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

import me.neilellis.dollar.api.DollarException;
import me.neilellis.dollar.api.DollarStatic;
import me.neilellis.dollar.api.collections.MultiHashMap;
import me.neilellis.dollar.api.collections.MultiMap;
import me.neilellis.dollar.api.var;
import me.neilellis.dollar.script.api.DollarParser;
import me.neilellis.dollar.script.api.Scope;
import me.neilellis.dollar.script.api.Variable;
import me.neilellis.dollar.script.api.exceptions.DollarScriptException;
import me.neilellis.dollar.script.api.exceptions.VariableNotFoundException;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.error.ParserException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static me.neilellis.dollar.api.DollarStatic.*;

public class ScriptScope implements Scope {

    @NotNull private static final Logger log = LoggerFactory.getLogger("ScriptScope");

    @NotNull private static final AtomicInteger counter = new AtomicInteger();
    @NotNull protected final ConcurrentHashMap<String, Variable> variables = new ConcurrentHashMap<>();

    @NotNull final String id;
    @Nullable private final String source;
    @NotNull private final MultiMap<String, var> listeners = new MultiHashMap();
    @NotNull private final List<var> errorHandlers = new CopyOnWriteArrayList<>();

    private final String file;
    @Nullable Scope parent;
    private Parser<var> parser;
    @Nullable private DollarParser dollarParser;
    private boolean parameterScope;


    public ScriptScope(String name) {
        this.parent = null;
        this.source = "<unknown>";
        this.file = "<unknown file>";
        this.dollarParser = null;
        id = String.valueOf(name + ":" + counter.incrementAndGet());
    }

    public ScriptScope(@NotNull Scope parent, String file, @Nullable String source, String name) {
        this.parent = parent;
        this.file = file;
        if (source == null) {
            this.source = "<unknown>";
        } else {
            this.source = source;

        }
        this.dollarParser = parent.getDollarParser();
        id = String.valueOf(name + ":" + counter.incrementAndGet());
    }

    public ScriptScope(@Nullable DollarParser dollarParser, @Nullable String source, String file) {
        this.source = source;
        this.dollarParser = dollarParser;
        this.file = file;
        id = String.valueOf("(top):" + counter.incrementAndGet());
    }

    @NotNull public Scope addChild(String source, String name) {
        return new ScriptScope(this, file, source, name);
    }

    @NotNull @Override
    public var addErrorHandler(var handler) {
        errorHandlers.add(handler);
        return $void();
    }

    @Override public void clear() {
        if (DollarStatic.getConfig().debugScope()) { log.info("Clearing scope " + this); }
        variables.clear();
        listeners.clear();
    }

    @Override public var get(@NotNull String key, boolean mustFind) {
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
        Variable result = scope.getVariables().get(key);

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

    @Override
    public var get(@NotNull String key) {
        return get(key, false);
    }

    @Nullable @Override public var getConstraint(String key) {
        Scope scope = getScopeForKey(key);
        if (scope == null) {
            scope = this;
        }
        if (DollarStatic.getConfig().debugScope()) { log.info("Getting constraint for " + key + " in " + scope); }
        if (scope.getVariables().containsKey(key) && scope.getVariables().get(key).getConstraint() != null) {
            return scope.getVariables().get(key).getConstraint();
        }
        return null;
    }

    @Nullable @Override public DollarParser getDollarParser() {
        return dollarParser;
    }

    @Override public void setDollarParser(@Nullable DollarParser dollarParser) {
        this.dollarParser = dollarParser;
    }

    @Override public String getFile() {
        return file;
    }

    @Override public MultiMap<String, var> getListeners() {
        return listeners;
    }

    @NotNull @Override
    public var getParameter(String key) {
        if (DollarStatic.getConfig().debugScope()) { log.info("Looking up parameter " + key + " in " + this); }
        Scope scope = getScopeForParameters();
        if (scope == null) {
            scope = this;
        } else {
            if (DollarStatic.getConfig().debugScope()) { log.info("Found " + key + " in " + scope); }
        }
        Variable result = scope.getVariables().get(key);

        return result != null ? result.getValue() : $void();
    }

    @Nullable @Override public Scope getScopeForKey(String key) {
        if (variables.containsKey(key)) {
            return this;
        }
        if (parent != null) {
            return parent.getScopeForKey(key);
        } else {
            if (DollarStatic.getConfig().debugScope()) { log.info("Scope not found for " + key); }
            return null;
        }
    }

    @Nullable @Override public Scope getScopeForParameters() {
        if (parameterScope) {
            return this;
        }
        if (parent != null) {
            return parent.getScopeForParameters();
        } else {
            if (DollarStatic.getConfig().debugScope()) { log.info("Parameter scope not found."); }
            return null;
        }
    }

    @Nullable @Override public String getSource() {
        return source;
    }

    @NotNull @Override public Map<String, Variable> getVariables() {
        return variables;
    }

    @Override public var handleError(Throwable t) {
        if (errorHandlers.isEmpty()) {
            if (parent == null) {
                if (t instanceof ParserException) {
                    throw (ParserException) t;
                }
                if (t instanceof DollarException) {
                    throw (DollarException) t;
                }
                throw new DollarScriptException(t);
            } else {
                return parent.handleError(t);
            }
        } else {
            setParameter("type", $(t.getClass().getName()));
            setParameter("msg", $(t.getMessage()));
            try {
                for (var handler : errorHandlers) {
                    fix(handler, false);
                }
            } finally {
                setParameter("type", $void());
                setParameter("msg", $void());
            }
            return $void();
        }

    }

    @Override
    public boolean has(String key) {
        Scope scope = getScopeForKey(key);
        if (scope == null) {
            scope = this;
        }
        if (DollarStatic.getConfig().debugScope()) {
            log.info("Checking for " + key + " in " + scope);
        }

        Variable val = scope.getVariables().get(key);
        return val != null;

    }

    @Override
    public boolean hasParameter(String key) {
        if (DollarStatic.getConfig().debugScope()) { log.info("Looking up parameter " + key + " in " + this); }
        Scope scope = getScopeForParameters();
        if (scope == null) {
            scope = this;
        } else {
            if (DollarStatic.getConfig().debugScope()) { log.info("Found " + key + " in " + scope); }
        }
        Variable result = scope.getVariables().get(key);

        return result != null;
    }

    @Override
    public void listen(@NotNull String key, var listener) {
        if (key.matches("[0-9]+")) {
            if (DollarStatic.getConfig().debugScope()) {
                log.info("Cannot listen to positional parameter $" + key + " in " + this);
            }
            return;
        }
        Scope scopeForKey = getScopeForKey(key);
        if (scopeForKey == null) {
            if (DollarStatic.getConfig().debugScope()) { log.info("Key " + key + " not found in " + this); }
            listeners.putValue(key, listener);
            return;
        }
        if (DollarStatic.getConfig().debugScope()) { log.info("Listening for " + key + " in " + scopeForKey); }
        scopeForKey.getListeners().putValue(key, listener);
    }

    @Override public var notify(String variableName) {
        final Scope scopeForKey = getScopeForKey(variableName);
        if (scopeForKey == null) {
            return $void();
        }
        scopeForKey.getListeners().getCollection(variableName).forEach(var::$notify);
        return scopeForKey.get(variableName);
    }

    @Override
    public void notifyScope(String key, @Nullable var value) {
        if (value == null) {
            throw new NullPointerException();
        }
        if (listeners.containsKey(key)) {
            listeners.getCollection(key).forEach(var::$notify);
        }
    }

    @Override
    public var set(@NotNull String key, var value, boolean readonly, @Nullable var constraint, String constraintSource,
                   boolean isVolatile,
                   boolean fixed,
                   boolean pure) {
        if (key.matches("[0-9]+")) {
            throw new AssertionError("Cannot set numerical keys, use setParameter");
        }
        Scope scope = getScopeForKey(key);
        if (scope == null) {
            scope = this;
        }
        if (DollarStatic.getConfig().debugScope()) { log.info("Setting " + key + " in " + scope); }
        if (scope.getVariables().containsKey(key) && scope.getVariables().get(key).isReadonly()) {
            throw new DollarScriptException("Cannot change the value of variable " + key + " it is readonly");
        }
        if (scope.getVariables().containsKey(key)) {
            final Variable variable = scope.getVariables().get(key);
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
            variable.setValue(value);
        } else {
            scope.getVariables()
                 .put(key, new Variable(value, readonly, constraint, constraintSource, isVolatile, fixed, pure));
        }
        scope.notifyScope(key, value);
        return value;
    }

    @Override public var setParameter(@NotNull String key, var value) {
        if (DollarStatic.getConfig().debugScope()) { log.info("Setting parameter " + key + " in " + this); }
        if (key.matches("[0-9]+") && variables.containsKey(key)) {
            throw new AssertionError("Cannot change the value of positional variables.");
        }
        this.parameterScope = true;
        variables.put(key, new Variable(value, null, null));
        this.notifyScope(key, value);
        return value;
    }

    @Override public void setParent(@Nullable Scope scope) {
        this.parent = scope;
    }

    public Parser<var> getParser() {
        return parser;
    }

    public void setParser(Parser<var> parser) {
        this.parser = parser;
    }

    @Nullable @Override
    public String toString() {
        return id + "->" + parent;
    }

    private boolean checkConstraint(var value, @Nullable Variable oldValue, @NotNull var constraint) {
        setParameter("it", value);
        System.out.println("SET it=" + value);
        if (oldValue != null) {
            setParameter("previous", oldValue.getValue());
        }
        final boolean fail = constraint.isFalse();
        setParameter("it", $void());
        setParameter("previous", $void());
        return fail;
    }

}
