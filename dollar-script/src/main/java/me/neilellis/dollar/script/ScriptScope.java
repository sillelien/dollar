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
import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.script.exceptions.DollarScriptException;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static me.neilellis.dollar.DollarStatic.$void;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class ScriptScope implements Scope {
    private static final Logger log = LoggerFactory.getLogger(ScriptScope.class);

    private static AtomicInteger counter = new AtomicInteger();
    private final String id;
    private final String source;
    private ScriptScope parent;
    private ConcurrentHashMap<String, Variable> variables = new ConcurrentHashMap<>();
    private boolean lambdaUnderConstruction;
    private Parser<var> parser;
    private DollarParser dollarParser;
    private Multimap<String, var> listeners = LinkedListMultimap.create();
    private boolean parameterScope;

    public ScriptScope(String name) {
        this.parent = null;
        this.source = "<unknown>";
        this.dollarParser = null;
        id = String.valueOf(name + ":" + counter.incrementAndGet());
    }

    public ScriptScope(ScriptScope parent, String source) {
        this.parent = parent;
        this.source = source;
        this.dollarParser = parent.getDollarParser();
        id = String.valueOf("S:" + counter.incrementAndGet());
    }


    public ScriptScope(DollarParser dollarParser, String source) {
        this.source = source;
        this.dollarParser = dollarParser;
        id = String.valueOf("S:" + counter.incrementAndGet());
    }


    public void setParent(ScriptScope scriptScope) {
        this.parent = scriptScope;
    }

    @Override
    public var get(String key) {
        if (key.matches("[0-9]+")) {
            throw new AssertionError("Cannot get numerical keys, use getParameter");
        }
        if (DollarStatic.config.isDebugScope()) log.info("Looking up " + key + " in " + this);
        ScriptScope scope = getScopeForKey(key);
        if (scope == null) {
            scope = this;
        } else {
            if (DollarStatic.config.isDebugScope()) log.info("Found " + key + " in " + scope);
        }
        Variable result = scope.variables.get(key);

        return result != null ? result.value : $void();
    }

    @Override
    public var getParameter(String key) {
        if (DollarStatic.config.isDebugScope()) log.info("Looking up parameter " + key + " in " + this);
        ScriptScope scope = getScopeForParameters();
        if (scope == null) {
            scope = this;
        } else {
            if (DollarStatic.config.isDebugScope()) log.info("Found " + key + " in " + scope);
        }
        Variable result = scope.variables.get(key);

        return result != null ? result.value : $void();
    }

    @Override
    public boolean hasParameter(String key) {
        if (DollarStatic.config.isDebugScope()) log.info("Looking up parameter " + key + " in " + this);
        ScriptScope scope = getScopeForParameters();
        if (scope == null) {
            scope = this;
        } else {
            if (DollarStatic.config.isDebugScope()) log.info("Found " + key + " in " + scope);
        }
        Variable result = scope.variables.get(key);

        return result != null;
    }

    @Override
    public boolean has(String key) {
        ScriptScope scope = getScopeForKey(key);
        if (scope == null) {
            scope = this;
        }
        if (DollarStatic.config.isDebugScope()) {
            log.info("Checking for " + key + " in " + scope);
        }

        Variable val = scope.variables.get(key);
        return val != null;

    }

    @Override
    public var set(String key, var value, boolean readonly) {
        if (key.matches("[0-9]+")) {
            throw new AssertionError("Cannot set numerical keys, use setParameter");
        }
        ScriptScope scope = getScopeForKey(key);
        if (scope == null) {
            scope = this;
        }
        if (DollarStatic.config.isDebugScope()) log.info("Setting " + key + " in " + scope);
        if (scope.variables.containsKey(key) && scope.variables.get(key).readonly) {
            throw new DollarScriptException("Cannot change the value of variable " + key + " it is readonly");
        }
        scope.variables.put(key, new Variable(value, readonly));
        if (readonly) {
            scope.notifyScope(key, value);
        }
        return value;
    }

    public var setParameter(String key, var value) {
        if (DollarStatic.config.isDebugScope()) log.info("Setting parameter " + key + " in " + this);
        if (key.matches("[0-9]+") && variables.containsKey(key)) {
            throw new AssertionError("Cannot change the value of positional variables.");
        }
        this.parameterScope = true;
        variables.put(key, new Variable(value));
        this.notifyScope(key, value);
        return value;
    }

    @Override
    public void notifyScope(String key, var value) {
        if (listeners.containsKey(key)) {
            for (var listener : listeners.get(key)) {
                listener.$notify(value);
            }
        }
    }

    public Parser<var> getParser() {
        return parser;
    }

    public void setParser(Parser<var> parser) {
        this.parser = parser;
    }

    public DollarParser getDollarParser() {
        return dollarParser;
    }

    public void setDollarParser(DollarParser dollarParser) {
        this.dollarParser = dollarParser;
    }

    public ScriptScope addChild(String source) {
        return new ScriptScope(this, source);
    }

    public String getSource() {
        return source;
    }


    @Override
    public void listen(String key, var listener) {
        if (key.matches("[0-9]+")) {
            if (DollarStatic.config.isDebugScope())
                log.info("Cannot listen to positional parameter $" + key + " in " + this);
            return;
        }
        ScriptScope scopeForKey = getScopeForKey(key);
        if (scopeForKey == null) {
            if (DollarStatic.config.isDebugScope()) log.info("Key " + key + " not found in " + this);
            listeners.put(key, listener);
            return;
        }
        if (DollarStatic.config.isDebugScope()) log.info("Listening for " + key + " in " + scopeForKey);
        scopeForKey.listeners.put(key, listener);
    }

    private ScriptScope getScopeForKey(String key) {
        if (variables.containsKey(key)) {
            return this;
        }
        if (parent != null) {
            return parent.getScopeForKey(key);
        } else {
            if (DollarStatic.config.isDebugScope()) log.info("Scope not found for " + key);
            return null;
        }
    }

    private ScriptScope getScopeForParameters() {
        if (parameterScope) {
            return this;
        }
        if (parent != null) {
            return parent.getScopeForParameters();
        } else {
            if (DollarStatic.config.isDebugScope()) log.info("Parameter scope not found.");
            return null;
        }
    }

    @Override
    public String toString() {
        return id + "->" + parent;
    }

    public void clear() {
        if (DollarStatic.config.isDebugScope()) log.info("Clearing scope " + this);
        variables.clear();
        listeners.clear();
    }

    private class Variable {
        private final var value;
        private final boolean readonly;

        public Variable(var value) {

            this.value = value;
            readonly = false;
        }

        public Variable(var value, boolean readonly) {
            this.value = value;
            this.readonly = readonly;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Variable variable = (Variable) o;

            if (!value.equals(variable.value)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }
}