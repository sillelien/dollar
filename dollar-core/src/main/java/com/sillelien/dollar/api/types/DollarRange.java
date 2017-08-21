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

package com.sillelien.dollar.api.types;


import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.collections.ImmutableList;
import com.sillelien.dollar.api.collections.ImmutableMap;
import com.sillelien.dollar.api.collections.Range;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DollarRange extends AbstractDollar {

    @NotNull
    private final Range range;

    public DollarRange(@NotNull ImmutableList<Throwable> errors, @NotNull Object start, @NotNull Object finish) {
        super(errors);
        range = new Range(DollarStatic.$(start), DollarStatic.$(finish));
    }

    public DollarRange(@NotNull ImmutableList<Throwable> errors, @NotNull Range range) {
        super(errors);
        this.range = range;
    }

    @NotNull
    @Override
    public var $abs() {
        return DollarFactory.fromValue(new Range(DollarFactory.fromValue(range.lowerEndpoint().$abs()),
                                                 DollarFactory.fromValue(range.upperEndpoint().$abs())),
                                       errors());
    }

    @NotNull
    @Override
    public var $minus(@NotNull var rhs) {
        var rhsFix = rhs.$fixDeep();
        return DollarFactory.fromValue(new Range(DollarFactory.fromValue(range.lowerEndpoint().$minus(rhsFix)),
                                                 DollarFactory.fromValue(range.upperEndpoint().$minus(rhsFix))),
                                       errors(), rhsFix.errors());
    }

    @NotNull
    @Override
    public var $plus(@NotNull var rhs) {
        var rhsFix = rhs.$fixDeep();

        return DollarFactory.fromValue(new Range(DollarFactory.fromValue(range.lowerEndpoint().$plus(rhsFix)),
                                                 DollarFactory.fromValue(range.upperEndpoint().$plus(rhsFix))),
                                       errors(), rhsFix.errors());

    }

    @NotNull
    @Override
    public var $negate() {
        return DollarFactory.fromValue(new Range(range.upperEndpoint(), range.lowerEndpoint()), errors());
    }

    @NotNull
    @Override
    public var $divide(@NotNull var rhs) {
        var rhsFix = rhs.$fixDeep();
        return DollarFactory.fromValue(new Range(DollarFactory.fromValue(range.lowerEndpoint().$divide(rhsFix)),
                                                 DollarFactory.fromValue(range.upperEndpoint().$divide(rhsFix))),
                                       errors(), rhsFix.errors());
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        var rhsFix = rhs.$fixDeep();
        return DollarFactory.fromValue(new Range(DollarFactory.fromValue(range.lowerEndpoint().$modulus(rhsFix)),
                                                 DollarFactory.fromValue(range.upperEndpoint().$modulus(rhsFix))),
                                       errors(), rhsFix.errors());
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var rhs) {
        var rhsFix = rhs.$fixDeep();
        return DollarFactory.fromValue(new Range(DollarFactory.fromValue(range.lowerEndpoint().$multiply(rhsFix)),
                                                 DollarFactory.fromValue(range.upperEndpoint().$multiply(rhsFix))),
                                       errors(), rhsFix.errors());
    }

    @NotNull
    @Override
    public Integer toInteger() {
        return diff().toInteger();
    }

    @NotNull
    public var diff() {
        if (range.upperEndpoint().equals(range.lowerEndpoint())) {
            return DollarFactory.fromValue(1, errors());
        }
        if (range.upperEndpoint().integer()) {
            final long diff = range.upperEndpoint().toLong() - range.lowerEndpoint().toLong();
            return DollarFactory.fromValue(diff + ((long) Math.signum(diff)), errors());
        }
        if (range.upperEndpoint().decimal()) {
            final double diff = range.upperEndpoint().toDouble() - range.lowerEndpoint().toDouble();
            return DollarFactory.fromValue(diff + Math.signum(diff), errors());
        }
        int count = 0;
        var start = range.lowerEndpoint();
        var finish = range.upperEndpoint();
        if (start.compareTo(finish) < 1) {
            for (var i = start; i.compareTo(finish) <= 0; i = i.$inc()) {
                count++;
            }
        } else {
            for (var i = start; i.compareTo(finish) <= 0; i = i.$dec()) {
                count--;
            }
        }
        return DollarFactory.fromValue(count, errors());

    }

    @NotNull
    @Override
    public var $as(@NotNull Type type) {
        if (type.is(Type._LIST)) {
            return DollarStatic.$(toVarList());
        } else if (type.is(Type._MAP)) {
            return DollarStatic.$(toJavaMap());
        } else if (type.is(Type._STRING)) {
            return DollarFactory.fromStringValue(toHumanString());
        } else if (type.is(Type._VOID)) {
            return DollarStatic.$void();
        } else {
            return DollarFactory.failure(ErrorType.INVALID_CAST);
        }
    }

    @NotNull
    @Override
    public String toHumanString() {
        return String.format("%s..%s", range.lowerEndpoint(), range.upperEndpoint());
    }

    @NotNull
    @Override
    public String toDollarScript() {
        return "((" + range.lowerEndpoint().toDollarScript() + ")..(" + range.upperEndpoint().toDollarScript() + "))";
    }

    @NotNull
    @Override
    public Range toJavaObject() {
        return range;
    }

    @NotNull
    @Override
    public ImmutableList<var> toVarList() {
        List<var> values = new ArrayList<>();
        var start = range.lowerEndpoint();
        var finish = range.upperEndpoint();
        if (start.compareTo(finish) < 1) {
            for (var i = start; i.compareTo(finish) <= 0; i = i.$inc()) {
                values.add(i);
            }
        } else {
            for (var i = start; i.compareTo(finish) <= 0; i = i.$dec()) {
                values.add(i);
            }
        }
        return ImmutableList.copyOf(values);
    }

    @NotNull
    @Override
    public Type $type() {
        return new Type(Type._RANGE, constraintLabel());
    }

    @Override
    public boolean collection() {
        return true;
    }

    @NotNull
    @Override
    public ImmutableMap<var, var> toVarMap() {
        DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
        return ImmutableMap.of();
    }

    @NotNull
    @Override
    public String toYaml() {
        DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
        return "";
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type.is(Type._RANGE)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean range() {
        return true;
    }

    @Override
    public ImmutableList<String> strings() {
        DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
        return null;
    }

    @NotNull
    @Override
    public ImmutableList<Object> toList() {
        List<Object> values = new ArrayList<>();
        var start = range.lowerEndpoint();
        var finish = range.upperEndpoint();
        for (var i = start; i.compareTo(finish) <= 0; i = i.$inc()) {
            values.add(i.toJavaObject());
        }
        return ImmutableList.copyOf(values);
    }

    @NotNull
    @Override
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return null;
    }

    @NotNull
    @Override
    public var $get(@NotNull var key) {
        if (key.integer()) {
            return DollarFactory.fromValue(toVarList().get(key.toInteger()));
        }
        return DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public var $append(@NotNull var value) {
        return $plus(value);
    }

    @NotNull
    @Override
    public var $containsValue(@NotNull var value) {
        return DollarStatic.$((range.lowerEndpoint().compareTo(DollarStatic.$(value)) <= 0) &&
                                      (range.upperEndpoint().compareTo(DollarStatic.$(value)) >= 0));
    }

    @NotNull
    @Override
    public var $containsKey(@NotNull var value) {
        return DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public var $has(@NotNull var key) {
        return DollarStatic.$(false);
    }

    @NotNull
    @Override
    public var $size() {
        return diff();
    }

    @NotNull
    @Override
    public var $prepend(@NotNull var value) {
        return $(toVarList()).$prepend(value);
    }

    @NotNull
    @Override
    public var $insert(@NotNull var value, int position) {
        return $(toVarList()).$insert(value, position);
    }

    @NotNull
    @Override
    public var $removeByKey(@NotNull String value) {
        return $copy();

    }

    @NotNull
    @Override
    public var $set(@NotNull var key, Object value) {
        return DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public var remove(@NotNull Object value) {
        return DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public var $remove(@NotNull var value) {
        return DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public int size() {
        return diff().toInteger().intValue();
    }

    @Override
    public int compareTo(@NotNull var o) {
        if ($containsValue(o).isTrue()) {
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
    public int hashCode() {
        return range.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof var) {
            var unwrapped = ((var) obj).$fixDeep().$unwrap();
            if (unwrapped instanceof DollarRange) {
                return range.equals(((DollarRange) unwrapped).range);
            }
        }
        return false;

    }

    @Override
    public Double toDouble() {
        return diff().toDouble();
    }

    @NotNull
    @Override
    public Long toLong() {
        return diff().toLong();
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean neitherTrueNorFalse() {
        return true;
    }

    @Override
    public boolean truthy() {
        return !range.isEmpty();
    }

}
