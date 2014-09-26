/*
 * Copyright (c) 2014-2014 Cazcade Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cazcade.dollar;

import org.json.JSONObject;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public abstract class AbstractDollarSingleValue<T> implements $<T> {
    private static ScriptEngine nashorn   = new ScriptEngineManager().getEngineByName("nashorn");

    protected final T value;

    @Override
    public $ $eval(String js) {
        try {
            SimpleScriptContext context = new SimpleScriptContext();
            context.setAttribute("$",value,context.getScopes().get(0));
            return DollarFactory.fromValue(nashorn.eval(js, context));
        } catch (ScriptException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public AbstractDollarSingleValue(T value) {
        if(value == null) {
            throw new NullPointerException();
        }
        this.value= value;
    }



    @Override
    public T val() {
        return value;
    }

    @Override
    public DollarString decode() {
        return new DollarString(URLDecoder.decode($$()));
    }


    @Override
    public boolean isNull() {
        return false;
    }

    public $<T> $(String age, long l) {
        throw new UnsupportedOperationException();
    }

    public $ $(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String $$(String key) {
        return $(key).$$();
    }


    public $<T> $(String key, Object value) {
        throw new UnsupportedOperationException();
    }


    public Integer $int(String key) {
        throw new UnsupportedOperationException();
    }

    public JsonObject $json() {
        throw new UnsupportedOperationException();
    }

    public JsonObject $json(String key) {
        throw new UnsupportedOperationException();
    }

    public Map<String, Object> $map() {
        throw new UnsupportedOperationException();
    }

    public JSONObject $orgjson() {
        throw new UnsupportedOperationException();

    }


    public Stream<$<T>> children() {
        throw new UnsupportedOperationException();

    }

    public Stream children(String key) {
        throw new UnsupportedOperationException();

    }
    @Override
    public String $$() {
        return toString();
    }


    public boolean has(String key) {
        throw new UnsupportedOperationException();
    }

    public Stream<Map.Entry<String, $<T>>> keyValues() {
        throw new UnsupportedOperationException();

    }

    public Stream<String> keys() {
        throw new UnsupportedOperationException();

    }

    public $<T> rm(String value) {
        throw new UnsupportedOperationException();

    }

    public Map<String, $<T>> split() {
        throw new UnsupportedOperationException();

    }

    @Override
    public FutureDollar<T> send(EventBus e, String destination) {
        throw new UnsupportedOperationException();

    }


    public $<T> ¢(String key) {
        throw new UnsupportedOperationException();
    }

    public List<String> splitValues() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
