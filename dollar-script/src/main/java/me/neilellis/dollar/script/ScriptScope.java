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

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import me.neilellis.dollar.DollarException;
import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.script.exceptions.DollarScriptException;
import me.neilellis.dollar.script.exceptions.VariableNotFoundException;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.error.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static me.neilellis.dollar.DollarStatic.*;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class ScriptScope implements Scope {
    private static final Logger log = LoggerFactory.getLogger(ScriptScope.class);

    private static final AtomicInteger counter = new AtomicInteger();
    protected final ConcurrentHashMap<String, Variable> variables = new ConcurrentHashMap<>();
    final String id;
    private final String source;
    private final Multimap<String, var> listeners = LinkedListMultimap.create();
    private final List<var> errorHandlers = new CopyOnWriteArrayList<>();
    Scope parent;
    private Parser<var> parser;
    private DollarParser dollarParser;
    private boolean parameterScope;

    public ScriptScope(String name) {
        this.parent = null;
        this.source = "<unknown>";
        this.dollarParser = null;
        id = String.valueOf(name + ":" + counter.incrementAndGet());
    }

    public ScriptScope(Scope parent, String source, String name) {
        this.parent = parent;
        if (source == null) {
            this.source = "<unknown>";
        } else {
            this.source = source;

        }
        this.dollarParser = parent.getDollarParser();
        id = String.valueOf(name + ":" + counter.incrementAndGet());
    }

    public ScriptScope(DollarParser dollarParser, String source) {
        this.source = source;
        this.dollarParser = dollarParser;
        id = String.valueOf("(top):" + counter.incrementAndGet());
    }

    public Scope addChild(String source, String name) {
        return new ScriptScope(this, source, name);
    }

    @Override
    public var addErrorHandler(var handler) {
        errorHandlers.add(handler);
        return $void();
    }

    @Override public void clear() {
        if (DollarStatic.config.debugScope()) { log.info("Clearing scope " + this); }
        variables.clear();
        listeners.clear();
    }

    @Override public var get(String key, boolean mustFind) {
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

    @Override
    public var get(String key) {
        return get(key, false);
    }

    @Override public var getConstraint(String key) {
        Scope scope = getScopeForKey(key);
        if (scope == null) {
            scope = this;
        }
        if (DollarStatic.config.debugScope()) { log.info("Getting constraint for " + key + " in " + scope); }
        if (scope.getVariables().containsKey(key) && scope.getVariables().get(key).constraint != null) {
            return scope.getVariables().get(key).constraint;
        }
        return null;
    }

    @Override public DollarParser getDollarParser() {
        return dollarParser;
    }

    @Override public void setDollarParser(DollarParser dollarParser) {
        this.dollarParser = dollarParser;
    }

    @Override public Multimap<String, var> getListeners() {
        return listeners;
    }

    @Override
    public var getParameter(String key) {
        if (DollarStatic.config.debugScope()) { log.info("Looking up parameter " + key + " in " + this); }
        Scope scope = getScopeForParameters();
        if (scope == null) {
            scope = this;
        } else {
            if (DollarStatic.config.debugScope()) { log.info("Found " + key + " in " + scope); }
        }
        Variable result = scope.getVariables().get(key);

        return result != null ? result.value : $void();
    }

    @Override public Scope getScopeForKey(String key) {
        if (variables.containsKey(key)) {
            return this;
        }
        if (parent != null) {
            return parent.getScopeForKey(key);
        } else {
            if (DollarStatic.config.debugScope()) { log.info("Scope not found for " + key); }
            return null;
        }
    }

    @Override public Scope getScopeForParameters() {
        if (parameterScope) {
            return this;
        }
        if (parent != null) {
            return parent.getScopeForParameters();
        } else {
            if (DollarStatic.config.debugScope()) { log.info("Parameter scope not found."); }
            return null;
        }
    }

    @Override public String getSource() {
        return source;
    }

    @Override public Map<String, Variable> getVariables() {
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
        if (DollarStatic.config.debugScope()) {
            log.info("Checking for " + key + " in " + scope);
        }

        Variable val = scope.getVariables().get(key);
        return val != null;

    }

    @Override
    public boolean hasParameter(String key) {
        if (DollarStatic.config.debugScope()) { log.info("Looking up parameter " + key + " in " + this); }
        Scope scope = getScopeForParameters();
        if (scope == null) {
            scope = this;
        } else {
            if (DollarStatic.config.debugScope()) { log.info("Found " + key + " in " + scope); }
        }
        Variable result = scope.getVariables().get(key);

        return result != null;
    }

    @Override
    public void listen(String key, var listener) {
        if (key.matches("[0-9]+")) {
            if (DollarStatic.config.debugScope()) {
                log.info("Cannot listen to positional parameter $" + key + " in " + this);
            }
            return;
        }
        Scope scopeForKey = getScopeForKey(key);
        if (scopeForKey == null) {
            if (DollarStatic.config.debugScope()) { log.info("Key " + key + " not found in " + this); }
            listeners.put(key, listener);
            return;
        }
        if (DollarStatic.config.debugScope()) { log.info("Listening for " + key + " in " + scopeForKey); }
        scopeForKey.getListeners().put(key, listener);
    }

    @Override public var notify(String variableName) {
        final Scope scopeForKey = getScopeForKey(variableName);
        if (scopeForKey == null) {
            return $void();
        }
        scopeForKey.getListeners().get(variableName).forEach(me.neilellis.dollar.var::$notify);
        return scopeForKey.get(variableName);
    }

    @Override
    public void notifyScope(String key, var value) {
        if (value == null) {
            throw new NullPointerException();
        }
        if (listeners.containsKey(key)) {
            listeners.get(key).forEach(me.neilellis.dollar.var::$notify);
        }
    }

    @Override
    public var set(String key, var value, boolean readonly, var constraint, boolean isVolatile, boolean fixed,
                   boolean pure) {
        if (key.matches("[0-9]+")) {
            throw new AssertionError("Cannot set numerical keys, use setParameter");
        }
        Scope scope = getScopeForKey(key);
        if (scope == null) {
            scope = this;
        }
        if (DollarStatic.config.debugScope()) { log.info("Setting " + key + " in " + scope); }
        if (scope.getVariables().containsKey(key) && scope.getVariables().get(key).readonly) {
            throw new DollarScriptException("Cannot change the value of variable " + key + " it is readonly");
        }
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
            variable.value = value;
        } else {
            scope.getVariables().put(key, new Variable(value, readonly, constraint, isVolatile, fixed, pure));
        }
        scope.notifyScope(key, value);
        return value;
    }

    @Override public var setParameter(String key, var value) {
        if (DollarStatic.config.debugScope()) { log.info("Setting parameter " + key + " in " + this); }
        if (key.matches("[0-9]+") && variables.containsKey(key)) {
            throw new AssertionError("Cannot change the value of positional variables.");
        }
        this.parameterScope = true;
        variables.put(key, new Variable(value, null));
        this.notifyScope(key, value);
        return value;
    }

    @Override public void setParent(Scope scope) {
        this.parent = scope;
    }

    public Parser<var> getParser() {
        return parser;
    }

    public void setParser(Parser<var> parser) {
        this.parser = parser;
    }

    @Override
    public String toString() {
        return id + "->" + parent;
    }

    private boolean checkConstraint(var value, Variable oldValue, var constraint) {
        setParameter("it", value);
        System.out.println("SET it=" + value);
        if (oldValue != null) {
            setParameter("previous", oldValue.value);
        }
        final boolean fail = constraint.isFalse();
        setParameter("it", $void());
        setParameter("previous", $void());
        return fail;
    }

}
