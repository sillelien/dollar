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
    public var $(@NotNull var key, Object value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_SINGLE_VALUE_OPERATION);
    }

    @NotNull
    @Override
    public var $(@NotNull Number n) {
        if (n.longValue() != 0) {
            return DollarFactory.failure(DollarFail.FailureType.INVALID_SINGLE_VALUE_OPERATION);
        } else {
            return this;
        }
    }

    @NotNull
    public Stream<var> $children() {
        return Stream.empty();

    }

    @NotNull
    public Stream<var> $children(@NotNull String key) {
        return Stream.empty();
    }

    @Override
    public var $containsValue(var value) {
        return DollarStatic.$(this.value.equals(value));
    }

    @NotNull
    public var $(@NotNull String key) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_SINGLE_VALUE_OPERATION);
    }

    public var $has(@NotNull String key) {
        return DollarStatic.$(S().equals(key));
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

    @Override
    public var $size() {
        return DollarStatic.$(1);
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

    @Override public boolean isCollection() {
        return false;
    }

    public Map<String, Object> toMap() {
        return Collections.singletonMap("value", value);
    }

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {
        return ImmutableMap.of("value", this);
    }

    @Nullable
    public JsonObject json(@NotNull String key) {
        return null;
    }

    @NotNull
    @Override
    public ImmutableList<var> toList() {
        return ImmutableList.of(this);
    }

    @Override
    public boolean isVoid() {
        return false;
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

    @Override
    public var $isEmpty() {
        return DollarStatic.$(false);
    }

    @NotNull
    @Override
    public Stream<var> $stream(boolean parallel) {
        return Stream.of(this);
    }

    @NotNull
    @Override
    public var $copy() {
        return DollarFactory.fromValue(value, errors());
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
    public var $minus(@NotNull var value) {
        if (value.equals(this)) {
            return DollarStatic.$void();
        } else {
            return this;
        }
    }

    @NotNull
    @Override
    public var $plus(var newValue) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_SINGLE_VALUE_OPERATION);
    }

    public Stream<String> keyStream() {
        return Stream.empty();

    }


}
