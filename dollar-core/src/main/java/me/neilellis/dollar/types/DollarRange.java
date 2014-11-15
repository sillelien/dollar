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
import com.google.common.collect.Range;
import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.json.JsonObject;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarRange extends AbstractDollar {

    private final Range<var> range;

    public DollarRange(@NotNull List<Throwable> errors, Object start, Object finish) {
        super(errors);
        range = Range.closed(DollarStatic.$(start), DollarStatic.$(finish));
    }

    public DollarRange(@NotNull List<Throwable> errors, Range range) {
        super(errors);
        this.range = range;
    }



    @NotNull
    @Override
    public var $plus(Object value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public Stream<var> $children() {
        return toList().stream();
    }

    @NotNull
    @Override
    public Stream<var> $children(@NotNull String key) {
        return Stream.of(DollarFactory.failure(DollarFail.FailureType.INVALID_RANGE_OPERATION));
    }

    @Override
    public var $dec(var amount) {
        return this;
    }

    @Override
    public var $inc(var amount) {
        return this;
    }

    @Override
    public var $negate() {
        return DollarFactory.fromValue(errors(), Range.closed(range.upperEndpoint(), range.lowerEndpoint()));
    }

    @Override
    public var $multiply(var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_RANGE_OPERATION); //TODO
    }

    @Override
    public var $divide(var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_RANGE_OPERATION); //TODO
    }

    @Override
    public var $modulus(var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_RANGE_OPERATION); //TODO
    }

    @Override
    public var $abs() {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_RANGE_OPERATION); //TODO
    }

    @Override
    public boolean $has(@NotNull String key) {
        return false;
    }

    @NotNull
    @Override
    public ImmutableList<var> toList() {
        List<var> values = new ArrayList<>();
        var start = range.lowerEndpoint();
        var finish = range.upperEndpoint();
        for (var i = start; i.compareTo(finish) <= 0; i = i.$inc()) {
            values.add(i);
        }
        return ImmutableList.copyOf(values);
    }

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public String S(@NotNull String key) {
        return range.toString();
    }

    @NotNull
    @Override
    public var $rm(@NotNull String value) {
        return $copy();

    }

    @NotNull
    @Override
    public var $minus(@NotNull Object value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public var $(@NotNull var key, Object value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_RANGE_OPERATION);
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public Integer I() {
        return null;
    }

    @Override
    public Integer I(@NotNull String key) {
        return null;
    }

    @NotNull
    @Override
    public var decode() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public var $get(@NotNull String key) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public JsonObject json(@NotNull String key) {
        return new JsonObject();
    }

    @NotNull
    @Override
    public Range<var> $() {
        return range;
    }

    @Override
    public Stream<String> keyStream() {
        return null;

    }

    @Override
    public var $(Number n) {
        return toList().get(n.intValue());
    }

    @Override
    public Number number(@NotNull String key) {
        return null;
    }

    @NotNull
    @Override
    public JsonObject json() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.putArray("value", jsonArray());
        return jsonObject;

    }

    @Override
    public ImmutableList<String> strings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> toMap() {
        return null;
    }

    @NotNull
    @Override
    public Number N() {
        return range.upperEndpoint().L() - range.lowerEndpoint().L();
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public int hashCode() {
        return range.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof var) {
            var unwrapped = ((var) obj)._unwrap();
            if (unwrapped instanceof DollarRange) {
                return range.equals(((DollarRange) unwrapped).range);
            }
            if (unwrapped instanceof DollarList) {
                return unwrapped.toList().equals(toList());
            }
        }
        return false;

    }

    @Override
    public int size() {
        return toList().size();
    }

    @Override
    public boolean containsValue(Object value) {
        return range.lowerEndpoint().compareTo(DollarStatic.$(value)) <= 0 && range.upperEndpoint().compareTo(DollarStatic.$(value)) >= 0;
    }

    @NotNull
    @Override
    public var remove(Object value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public String S() {
        return String.format("%s..%s", range.lowerEndpoint(), range.upperEndpoint());
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean isTruthy() {
        return !range.isEmpty();
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isNeitherTrueNorFalse() {
        return true;
    }

    @Override
    public int compareTo(var o) {
        if (containsValue(o)) {
            return 0;
        }
        if (range.lowerEndpoint().compareTo(o) < 0) {
            return -1;
        }
        if (range.upperEndpoint().compareTo(o) > 0) {
            return 1;
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean is(Type... types) {
        for (Type type : types) {
            if (type == Type.RANGE) {
                return true;
            }
        }
        return false;
    }
}
