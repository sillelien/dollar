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

import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.types.DollarFail;
import me.neilellis.dollar.types.DollarString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    @NotNull
    protected final T value;

    public AbstractDollarSingleValue(@NotNull List<Throwable> errors, @NotNull T value) {
        super(errors);
        this.value = value;
    }

    @NotNull
    public var $(@NotNull String age, long l) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public var $append(Object newValue) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_SINGLE_VALUE_OPERATION);
    }

    @NotNull
    public Stream<var> $children() {
        return Stream.empty();

    }

    @NotNull
    public Stream<var> $children(@NotNull String key) {
        return Stream.empty();
    }

    public boolean $has(@NotNull String key) {
        return S().equals(key);
    }

    @NotNull
    @Override
    public List<var> $list() {
        return Collections.singletonList(this);
    }

    @NotNull
    @Override
    public Map<String, var> $map() {
        return Collections.singletonMap("value", this);
    }

    @NotNull
    @Override
    public String string(@NotNull String key) {
        return $(key).S();
    }

    @NotNull
    public var $rm(@NotNull String value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_SINGLE_VALUE_OPERATION);

    }

    @NotNull
    public var $(@NotNull String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean $void() {
        return false;
    }

    public Integer I(@NotNull String key) {
        return null;
    }

    @NotNull
    @Override
    public DollarString decode() {
        return new DollarString(errors(), URLDecoder.decode(S()));
    }

    @NotNull
    public var $(@NotNull String key) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_SINGLE_VALUE_OPERATION);
    }

    @Nullable
    public JsonObject json() {
        return null;
    }

    @Nullable
    public JsonObject json(@NotNull String key) {
        return null;
    }

    public Stream<String> keyStream() {
        return Stream.empty();

    }

    @Nullable
    public JSONObject orgjson() {
        return null;

    }

    @Override
    public List<String> strings() {
        return Collections.singletonList(S());
    }

    public Map<String, Object> toMap() {
        return null;
    }

    @Override
    public <R> R val() {
        return (R) value;
    }

    @Override
    public int hashCode() {
        return value.toString().hashCode();
    }

    @NotNull
    @Override
    public Stream<var> $stream() {
        return Stream.of(this);
    }

    @NotNull
    @Override
    public var $copy() {
        return DollarFactory.fromValue(errors(), value);
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean containsValue(Object value) {
        return this.value.equals(value);
    }

    @NotNull
    @Override
    public var remove(Object newValue) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_SINGLE_VALUE_OPERATION);
    }

    @NotNull
    @Override
    public String toString() {
        return value.toString();
    }

}
