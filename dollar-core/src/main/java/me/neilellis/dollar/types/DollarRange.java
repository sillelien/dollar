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


import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.Type;
import me.neilellis.dollar.collections.ImmutableList;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.collections.Range;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarRange extends AbstractDollar {

    private final Range range;

    public DollarRange(@NotNull ImmutableList<Throwable> errors, Object start, Object finish) {
        super(errors);
        range = new Range(DollarStatic.$(start), DollarStatic.$(finish));
    }

    public DollarRange(@NotNull ImmutableList<Throwable> errors, Range range) {
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
    public var $minus(var rhs) {
        var rhsFix = rhs._fixDeep();
        return DollarFactory.fromValue(new Range(DollarFactory.fromValue(range.lowerEndpoint().$minus(rhsFix)),
                                                 DollarFactory.fromValue(range.upperEndpoint().$minus(rhsFix))),
                                       errors(), rhsFix.errors());
    }

    @NotNull
    @Override
    public var $divide(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        return DollarFactory.fromValue(new Range(DollarFactory.fromValue(range.lowerEndpoint().$divide(rhsFix)),
                                                 DollarFactory.fromValue(range.upperEndpoint().$divide(rhsFix))),
                                       errors(), rhsFix.errors());
    }

    @NotNull
    @Override
    public var $plus(var rhs) {
        var rhsFix = rhs._fixDeep();

        return DollarFactory.fromValue(new Range(DollarFactory.fromValue(range.lowerEndpoint().$plus(rhsFix)),
                                                 DollarFactory.fromValue(range.upperEndpoint().$plus(rhsFix))),
                                       errors(), rhsFix.errors());

    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        return DollarFactory.fromValue(new Range(DollarFactory.fromValue(range.lowerEndpoint().$modulus(rhsFix)),
                                                 DollarFactory.fromValue(range.upperEndpoint().$modulus(rhsFix))),
                                       errors(), rhsFix.errors());
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        return DollarFactory.fromValue(new Range(DollarFactory.fromValue(range.lowerEndpoint().$multiply(rhsFix)),
                                                 DollarFactory.fromValue(range.upperEndpoint().$multiply(rhsFix))),
                                       errors(), rhsFix.errors());
    }

    @NotNull
    @Override
    public var $negate() {
        return DollarFactory.fromValue(new Range(range.upperEndpoint(), range.lowerEndpoint()), errors());
    }

    @Override
    public Integer I() {
        return diff().I();
    }

    public var diff() {return range.upperEndpoint().$minus(range.lowerEndpoint());}

    @NotNull
    @Override
    public String S() {
        return String.format("%s..%s", range.lowerEndpoint(), range.upperEndpoint());
    }

    @NotNull
    @Override
    public var $get(@NotNull var key) {
        if (key.isInteger()) {
            return DollarFactory.fromValue($list().get(key.I()));
        }
        return DollarFactory.failure(me.neilellis.dollar.types.ErrorType.INVALID_RANGE_OPERATION);
    }

    @Override
    public var $as(Type type) {
        switch (type) {
            case LIST:
                return DollarStatic.$($list());
            case MAP:
                return DollarStatic.$(toMap());
            case STRING:
                return DollarFactory.fromStringValue(S());
            case VOID:
                return DollarStatic.$void();
            default:
                return DollarFactory.failure(me.neilellis.dollar.types.ErrorType.INVALID_CAST);
        }
    }

    @NotNull @Override public var $append(@NotNull var value) {
        return $plus(value);
    }

    @NotNull
    @Override
    public ImmutableList<var> $list() {
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
    public var $containsValue(@NotNull var value) {
        return DollarStatic.$(range.lowerEndpoint().compareTo(DollarStatic.$(value)) <= 0 &&
                              range.upperEndpoint().compareTo(DollarStatic.$(value)) >= 0);
    }

    @Override public Type $type() {
        return Type.RANGE;
    }

    @NotNull @Override
    public var $has(@NotNull var key) {
        return DollarStatic.$(false);
    }

    @NotNull
    @Override
    public var $size() {
        return diff();
    }

    @NotNull @Override public var $prepend(@NotNull var value) {
        return value.$append(value);
    }

    @NotNull
    @Override
    public var $removeByKey(@NotNull String value) {
        return $copy();

    }

    @NotNull
    @Override
    public var $set(@NotNull var key, Object value) {
        return DollarFactory.failure(me.neilellis.dollar.types.ErrorType.INVALID_RANGE_OPERATION);
    }

    @Override public boolean isRange() {
        return true;
    }

    @NotNull
    @Override
    public var remove(Object value) {
        return DollarFactory.failure(me.neilellis.dollar.types.ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull @Override public var $remove(var value) {
        return DollarFactory.failure(me.neilellis.dollar.types.ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull @Override
    public Map<String, Object> toMap() {
        return null;
    }

    @Override
    public Double D() {
        return diff().D();
    }

    @NotNull
    @Override
    public ImmutableMap<var, var> $map() {
        DollarFactory.failure(me.neilellis.dollar.types.ErrorType.INVALID_RANGE_OPERATION);
        return ImmutableMap.of();
    }

    @Override
    public Long L() {
        return diff().L();
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type == Type.RANGE) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return range.hashCode();
    }

    @Override public boolean isCollection() {
        return true;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof var) {
            var unwrapped = ((var) obj)._fixDeep()._unwrap();
            if (unwrapped instanceof DollarRange) {
                return range.equals(((DollarRange) unwrapped).range);
            }
        }
        return false;

    }

    @Override
    public ImmutableList<String> strings() {
        DollarFactory.failure(me.neilellis.dollar.types.ErrorType.INVALID_RANGE_OPERATION);
        return null;
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

    @NotNull @Override public ImmutableList<Object> toList() {
        List<Object> values = new ArrayList<>();
        var start = range.lowerEndpoint();
        var finish = range.upperEndpoint();
        for (var i = start; i.compareTo(finish) <= 0; i = i.$inc()) {
            values.add(i.toJavaObject());
        }
        return ImmutableList.copyOf(values);
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
    public boolean isNeitherTrueNorFalse() {
        return true;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean isTruthy() {
        return !range.isEmpty();
    }

    @NotNull @Override public String toDollarScript() {
        return "((" + range.lowerEndpoint().toDollarScript() + ")..(" + range.upperEndpoint().toDollarScript() + "))";
    }

    @NotNull
    @Override
    public Range toJavaObject() {
        return range;
    }






















}
