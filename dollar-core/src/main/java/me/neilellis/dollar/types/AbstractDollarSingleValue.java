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

package me.neilellis.dollar.types;

import com.google.common.collect.ImmutableList;
import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.json.JsonObject;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

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
        return DollarFactory.failure(DollarFail.FailureType.INVALID_SINGLE_VALUE_OPERATION);
    }

    @NotNull
    @Override
    public var $(@NotNull String key, double value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_SINGLE_VALUE_OPERATION);
    }

    @NotNull
    @Override
    public var $plus(Object newValue) {
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
    public ImmutableList<var> toList() {
        return ImmutableList.of(this);
    }

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {
        return ImmutableMap.of("value", this);
    }

    @NotNull
    @Override
    public String S(@NotNull String key) {
        return $(key).S();
    }

    @NotNull
    public var $rm(@NotNull String value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_SINGLE_VALUE_OPERATION);

    }

    @NotNull
    @Override
    public var $minus(@NotNull Object value) {
        if (value.equals(this)) {
            return DollarStatic.$void();
        } else {
            return this;
        }
    }

    @NotNull
    public var $(@NotNull String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    public Integer I(@NotNull String key) {
        return null;
    }

    @NotNull
    @Override
    public var decode() {
        return DollarFactory.fromValue(errors(), URLDecoder.decode(S()));
    }

    @NotNull
    public var $(@NotNull String key) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_SINGLE_VALUE_OPERATION);
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

    @Nullable
    public JsonObject json() {
        return null;
    }

    @Override
    public ImmutableList<String> strings() {
        return ImmutableList.of(S());
    }

    public Map<String, Object> toMap() {
        return Collections.singletonMap("value", value);
    }

    @Override
    public <R> R val() {
        return (R) value;
    }

    @Override
    public boolean isSingleValue() {
        return true;
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
    public String S() {
        return value.toString();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public var $(Number n) {
        if (n.longValue() != 0) {
            return DollarFactory.failure(DollarFail.FailureType.INVALID_SINGLE_VALUE_OPERATION);
        } else {
            return this;
        }
    }
}
