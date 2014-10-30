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

import me.neilellis.dollar.var;
import org.codehaus.jparsec.Parser;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class ScriptScope {

    private final String id = UUID.randomUUID().toString();
    private final String source;
    private ScriptScope parent;
    private ConcurrentHashMap<String, var> variables = new ConcurrentHashMap<>();
    private boolean lambdaUnderConstruction;
    private Parser<var> parser;
    private DollarParser dollarParser;

    public ScriptScope(DollarParser dollarParser, ScriptScope parent, String source) {
        this.parent = parent;
        this.source = source;
    }

    public ScriptScope(ScriptScope parent, String source) {
        this.parent = parent;
        this.source = source;
        this.dollarParser = parent.getDollarParser();
    }


    public ScriptScope(DollarParser dollarParser, String source) {
        this.source = source;
        this.dollarParser = dollarParser;
    }


    public void setParent(ScriptScope scriptScope) {
        this.parent = scriptScope;
    }

    public var get(String variable) {
//        System.out.println("Looking up "+variable +" in "+id);
        var val = variables.get(variable);
        if (val == null) {
            if (parent == null) {
                return dollarParser.notFound(variable, this);
            } else {
                return parent.get(variable);
            }
        }
        return val;
    }


    public boolean has(String variable) {
//        System.out.println("Looking up "+variable +" in "+id);
        var val = variables.get(variable);
        return val != null || parent != null && parent.has(variable);

    }

    public var set(String key, var value) {
//        System.out.println("Setting up "+key +" in "+id);
        variables.put(key, value);
        return value;
    }

    public boolean isLambdaUnderConstruction() {
        return lambdaUnderConstruction;
    }

    public void setLambdaUnderConstruction(boolean lambdaUnderConstruction) {
        this.lambdaUnderConstruction = lambdaUnderConstruction;
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


}
