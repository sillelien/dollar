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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarNull extends AbstractDollar implements var {


    public static final DollarNull INSTANCE = new DollarNull();

    public DollarNull() {

    }

    @Override
    public var $(String age, long l) {
        return this;
    }

    @Override
    public var $(String key) {
        return this;
    }

    @Override
    public var $(String key, Object value) {
        return DollarFactory.fromValue().$(key, value);
    }

    @Override
    public <R> R $() {
        return null;
    }

    @Override
    public String $$(String key) {
        return "";
    }

    @Override
    public String $$() {
        return "";
    }

    @Override
    public Integer $int() {
        return 0;
    }

    @Override
    public Integer $int(String key) {
        return 0;
    }

    @Override
    public JsonObject $json() {
        return new JsonObject();
    }

    @Override
    public JsonObject $json(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<var> $list() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> $map() {
        return Collections.emptyMap();
    }

    @Override
    public Number $number(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject $orgjson() {
        return new JSONObject();
    }

    @Override
    public var add(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<var> children() {
        return Collections.<var>emptyList().stream();
    }

    @Override
    public Stream children(String key) {
        return Collections.emptyList().stream();
    }

    @Override
    public DollarNull copy() {
        return this;
    }

    @Override
    public var decode() {
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == null || (obj instanceof var && ((var) obj).$() == null);
    }

    @Override
    public var eval(String js, String label) {
        return INSTANCE;
    }

    @Override
    public var eval(String label, DollarEval lambda) {
        return lambda.eval(INSTANCE);
    }

    @Override
    public boolean has(String key) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public Stream<Map.Entry<String, var>> keyValues() {
        return Collections.<String, var>emptyMap().entrySet().stream();
    }

    @Override
    public Stream<String> keys() {
        return Collections.<String>emptyList().stream();
    }

    @Override
    public var remove(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var rm(String value) {
        return this;
    }

    @Override
    public FutureDollar send(EventBus e, String destination) {
        throw new NullPointerException();
    }

    @Override
    public Map<String, var> split() {
        return Collections.emptyMap();
    }

    public List<String> splitValues() {
        return Collections.emptyList();
    }

    @Override
    public Stream<var> stream() {
        return Stream.empty();
    }

    @Override
    public List<String> strings() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "";
    }

    @Override
    public <R> R val() {
        return null;
    }
}
