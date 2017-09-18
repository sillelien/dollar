/*
 *    Copyright (c) 2014-2017 Neil Ellis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dollar.api.types;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dollar.api.DollarStatic;
import dollar.api.Value;
import dollar.api.exceptions.DollarFailureException;
import dollar.api.json.ImmutableJsonObject;
import dollar.api.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

public abstract class AbstractDollarSingleValue<T> extends AbstractDollar {
    @NotNull
    final T value;

    AbstractDollarSingleValue(@NotNull T value) {
        super();
        this.value = value;
    }

    @NotNull
    @Override
    public Value $append(@NotNull Value value) {
        return DollarFactory.fromValue(Arrays.asList(this, value));
    }

    @NotNull
    @Override
    public Value $containsKey(@NotNull Value value) {
        return DollarStatic.$(false);
    }

    @NotNull
    @Override
    public Value $containsValue(@NotNull Value value) {
        return DollarStatic.$(this.value.equals(value));
    }

    @NotNull
    @Override
    public Value $get(@NotNull Value rhs) {
        if (equals(rhs)) {
            return DollarFactory.wrap(this);
        } else if (rhs.integer() && (rhs.toInteger() == 0)) {
            return DollarFactory.wrap(this);
        } else {
            return DollarFactory.failure(ErrorType.INVALID_SINGLE_VALUE_OPERATION,
                                         getClass().toString(), false);
        }
    }

    @Override
    @NotNull
    public Value $has(@NotNull Value key) {
        return DollarStatic.$(equals(key));
    }

    @NotNull
    @Override
    public Value $insert(@NotNull Value value, int position) {
        return DollarFactory.failure(ErrorType.INVALID_SINGLE_VALUE_OPERATION,
                                     getClass().toString(), false);
    }

    @NotNull
    @Override
    public Value $isEmpty() {
        return DollarStatic.$(false);
    }

    @NotNull
    @Override
    public Value $plus(@NotNull Value rhs) {
        throw new DollarFailureException(ErrorType.INVALID_SINGLE_VALUE_OPERATION);
    }

    @NotNull
    @Override
    public Value $prepend(@NotNull Value value) {
        return DollarFactory.fromValue(Arrays.asList(value, this));
    }

    @NotNull
    @Override
    public Value $remove(@NotNull Value value) {
        return DollarFactory.failure(ErrorType.INVALID_SINGLE_VALUE_OPERATION,
                                     getClass().toString(), false);
    }

    @Override
    @NotNull
    public Value $removeByKey(@NotNull String value) {
        return DollarFactory.failure(ErrorType.INVALID_SINGLE_VALUE_OPERATION,
                                     getClass().toString(), false);

    }

    @Override
    @NotNull
    public Value $set(@NotNull Value key, @NotNull Object value) {
        throw new DollarFailureException(ErrorType.INVALID_SINGLE_VALUE_OPERATION);
    }

    @NotNull
    @Override
    public Value $size() {
        return DollarStatic.$(1);
    }

    @Override
    public boolean collection() {
        return false;
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @NotNull
    @Override
    public int size() {
        return 1;
    }

    @Override
    public @NotNull Stream<Value> stream(boolean parallel) {
        return Stream.of(this);
    }

    @NotNull
    @Override
    public String toHumanString() {
        return value.toString();
    }

    @Override
    @NotNull
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return ImmutableMap.copyOf(Collections.singletonMap((K) "value", (V) value));
    }

    @Override
    @NotNull
    public ImmutableJsonObject toJsonObject() {
        return new ImmutableJsonObject(new JsonObject());
    }

    @NotNull
    @Override
    public String toJsonString() {
        return String.valueOf((Object) toJavaObject());
    }

    @NotNull
    @Override
    public ImmutableList<? extends T> toList() {
        return ImmutableList.of(value);
    }

    @Override
    public ImmutableList<String> toStrings() {
        return ImmutableList.of(toHumanString());
    }

    @NotNull
    @Override
    public ImmutableList<Value> toVarList() {
        return ImmutableList.of(this);
    }

    @NotNull
    @Override
    public ImmutableMap<Value, Value> toVarMap() {
        return ImmutableMap.of($get(DollarStatic.$("value")), this);
    }

    @NotNull
    @Override
    public Value $avg(boolean parallel) {
        return this;
    }

    @NotNull
    @Override
    public Value $copy() {
        return DollarFactory.fromValue(value);
    }

    @NotNull
    @Override
    public Value $max(boolean parallel) {
        return this;
    }

    @NotNull
    @Override
    public Value $min(boolean parallel) {
        return this;
    }

    @NotNull
    @Override
    public Value $product(boolean parallel) {
        return this;
    }

    @NotNull
    @Override
    public Value $reverse(boolean parallel) {
        return this;
    }

    @NotNull
    @Override
    public Value $sort(boolean parallel) {
        return this;
    }

    @NotNull
    @Override
    public Value $stream(boolean parallel) {
        return DollarFactory.fromValue(Stream.of(this));
    }

    @NotNull
    @Override
    public Value $sum(boolean parallel) {
        return this;
    }

    @NotNull
    @Override
    public Value $unique(boolean parallel) {
        return this;
    }

    @Override
    public boolean singleValue() {
        return true;
    }

    @NotNull
    public Stream<String> keyStream() {
        return Stream.empty();

    }

}
