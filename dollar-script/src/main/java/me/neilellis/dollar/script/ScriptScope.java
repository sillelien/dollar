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
import me.neilellis.dollar.var;
import org.codehaus.jparsec.Parser;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static me.neilellis.dollar.DollarStatic.$void;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class ScriptScope {

    private static AtomicInteger counter = new AtomicInteger();
    private final String id;
    private final String source;
    private ScriptScope parent;
    private ConcurrentHashMap<String, var> variables = new ConcurrentHashMap<>();
    private boolean lambdaUnderConstruction;
    private Parser<var> parser;
    private DollarParser dollarParser;
    private Multimap<String, var> listeners = LinkedListMultimap.create();

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

    public var get(String key) {
        if (DollarStatic.config.isDebugScope()) System.out.println("Looking up " + key + " in " + this);
        ScriptScope scope = getScopeForKey(key);
        if (scope == null) {
            scope = this;
        } else {
            if (DollarStatic.config.isDebugScope()) System.out.println("Found " + key + " in " + scope);
        }
        var result = scope.variables.get(key);

        return result != null ? result : $void();
    }


    public boolean has(String key) {
        ScriptScope scope = getScopeForKey(key);
        if (scope == null) {
            scope = this;
        }
        if (DollarStatic.config.isDebugScope()) {
            System.out.println("Checking for " + key + " in " + scope);
        }

        var val = scope.variables.get(key);
        return val != null;

    }

    public var set(String key, var value) {
        ScriptScope scope = getScopeForKey(key);
        if (scope == null) {
            scope = this;
        }
        if (DollarStatic.config.isDebugScope()) System.out.println("Setting " + key + " in " + scope);
        scope.variables.put(key, value);
        scope.notifyScope(key, value);
        return value;
    }

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


    public void listen(String key, var listener) {
        if (key.matches("[0-9]+")) {
            if (DollarStatic.config.isDebugScope())
                System.out.println("Cannot listen to positional parameter $" + key + " in " + this);
            return;
        }
        ScriptScope scopeForKey = getScopeForKey(key);
        if (scopeForKey == null) {
            if (DollarStatic.config.isDebugScope()) System.out.println("Key " + key + " not found in " + this);
            listeners.put(key, listener);
            return;
        }
        if (DollarStatic.config.isDebugScope()) System.out.println("Listening for " + key + " in " + scopeForKey);
        scopeForKey.listeners.put(key, listener);
    }

    private ScriptScope getScopeForKey(String key) {
        if (variables.containsKey(key)) {
            return this;
        }
        if (parent != null) {
            return parent.getScopeForKey(key);
        } else {
            if (DollarStatic.config.isDebugScope()) System.out.println("Scope not found for " + key);
            return null;
        }
    }

    @Override
    public String toString() {
        return id + "->" + parent;
    }

    public void clear() {
        if (DollarStatic.config.isDebugScope()) System.out.println("Clearing scope " + this);
        variables.clear();
        listeners.clear();
    }
}
