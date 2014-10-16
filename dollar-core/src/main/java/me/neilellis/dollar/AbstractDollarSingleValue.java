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

package me.neilellis.dollar;

import org.json.JSONObject;
import org.vertx.java.core.json.JsonObject;

import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public abstract class AbstractDollarSingleValue<T> extends AbstractDollar implements var {

    protected final T value;

    public AbstractDollarSingleValue(List<Throwable> errors, T value) {
        super(errors);
        if (value == null) {
            throw new NullPointerException();
        }
        this.value = value;
    }

    public var $(String age, long l) {
        throw new UnsupportedOperationException();
    }

    public var $(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String $$(String key) {
        return $(key).$$();
    }

    public var $(String key) {
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

    @Override
    public List<var> list() {
        return Collections.singletonList(this);
    }

    public Map<String, Object> $map() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, var> map() {
        throw new UnsupportedOperationException();
    }

    public JSONObject $orgjson() {
        throw new UnsupportedOperationException();

    }

    @Override
    public var add(Object newValue) {
        throw new UnsupportedOperationException();
    }

    public Stream<var> children() {
        throw new UnsupportedOperationException();

    }

    public Stream children(String key) {
        throw new UnsupportedOperationException();

    }

    @Override
    public DollarString decode() {
        return new DollarString($errors(),URLDecoder.decode($$()));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof AbstractDollarSingleValue) {
            return value.equals(((AbstractDollarSingleValue) obj).$val());
        } else {
            return value.toString().equals(obj.toString());
        }

    }

    @Override
    public <R> R $val() {
        return (R) value;
    }

    public boolean has(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        return value.toString().hashCode();
    }

    @Override
    public boolean isNull() {
        return false;
    }

    public Stream<Map.Entry<String, var>> keyValues() {
        throw new UnsupportedOperationException();

    }

    public Stream<String> $keys() {
        throw new UnsupportedOperationException();

    }

    @Override
    public var remove(Object newValue) {

        throw new UnsupportedOperationException();
    }

    public var rm(String value) {
        throw new UnsupportedOperationException();

    }

    public Map<String, var> split() {
        throw new UnsupportedOperationException();

    }

    public List<String> splitValues() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<var> stream() {
        return Stream.of(this);
    }

    @Override
    public List<String> $strings() {
        return Collections.singletonList($$());
    }

    @Override
    public String $$() {
        return toString();
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public var ¢(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean containsValue(Object value) {
        return this.value.equals(value);
    }

    @Override
    public var copy(List<Throwable> errors) {
        List<Throwable> errorList = $errors();
        errorList.addAll(errors);
        return DollarFactory.fromValue(errorList, value);
    }

    @Override
    public var copy() {
        return  DollarFactory.fromValue($errors(),value);
    }

}
