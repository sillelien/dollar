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
import me.neilellis.dollar.var;
import org.codehaus.jparsec.Parser;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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

    public var get(String variable) {
//        System.out.println("Looking up "+variable +" in "+this);
        var val = variables.get(variable);
        if (val == null) {
            if (parent == null) {
                return dollarParser.notFound(variable);
            } else {
                return parent.get(variable);
            }
        }
        return val;
    }


    public boolean has(String variable) {
//        System.out.println("Checking for "+variable +" in "+this);
        var val = variables.get(variable);
        return val != null || parent != null && parent.has(variable);

    }

    public var set(String key, var value) {
//        System.out.println("Setting up "+key +" in "+this);
        variables.put(key, value);
        if (listeners.containsKey(key)) {
            for (var listener : listeners.get(key)) {
                listener.$notify(value);
            }
        }
        return value;
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
        ScriptScope scope = getScopeForKey(key);
        if (scope == null) {
//            System.out.println("Key "+key+" was not in any scope.");
            return;
        }
        scope.listeners.put(key, listener);
    }

    private ScriptScope getScopeForKey(String key) {
        if (variables.containsKey(key)) {
            return this;
        }
        if (parent != null) {
            return parent.getScopeForKey(key);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return id + "->" + parent;
    }

    public void clear() {
        System.out.println("Clearing scope " + this);
        variables.clear();
        listeners.clear();
    }
}
