/*
 * Copyright (c) 2014-2015 Neil Ellis
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

package com.sillelien.dollar.api.types;

import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.collections.ImmutableList;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

import static java.lang.Math.abs;

public class DollarInteger extends AbstractDollarSingleValue<Long> {

    public DollarInteger(@NotNull ImmutableList<Throwable> errors, @NotNull Long value) {
        super(errors, value);
        if (value < -Long.MAX_VALUE) {
            throw new IllegalArgumentException(
                    "Cannot create a Dollar Integer with a value less than -" + Long.MAX_VALUE);
        }
    }

    @NotNull
    @Override
    public var $abs() {
        return DollarFactory.fromValue(abs(value.longValue()), errors());
    }

    @NotNull
    @Override
    public var $negate() {
        return DollarFactory.fromValue(-value, errors());
    }

    @NotNull
    @Override
    public var $divide(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.infinite()) {
            return DollarFactory.fromValue(0, errors(), rhsFix.errors());
        } else if (rhsFix.decimal() && rhsFix.toDouble() != 0.0) {
            return DollarFactory.fromValue(value.doubleValue() / rhsFix.toDouble(), errors(),
                                           rhsFix.errors());
        } else if (rhsFix.toLong() == 0) {
            return DollarFactory.infinity(positive(), errors(), rhsFix.errors());
        } else {
            return DollarFactory.fromValue(value / rhsFix.toLong(), errors(), rhsFix.errors());
        }
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.infinite()) {
            return DollarFactory.infinity(positive(), errors(), rhsFix.errors());
        }
        if (rhsFix.zero()) {
            return DollarFactory.infinity(positive(), errors(), rhsFix.errors());
        }
        if (rhsFix.decimal()) {
            return DollarFactory.fromValue(value.doubleValue() % rhsFix.toDouble(), errors(),
                                           rhsFix.errors());
        } else {
            return DollarFactory.fromValue(value % rhsFix.toLong(), errors(), rhsFix.errors());
        }
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var newValue) {
        if (newValue.infinite()) {
            return newValue.$multiply(this);
        }
        if (newValue.zero()) {
            return DollarFactory.fromValue(0, errors(), newValue.errors());
        }

        if (newValue.decimal()) {
            return DollarFactory.fromValue(value.doubleValue() * newValue.toDouble(), errors(),
                                           newValue.errors());
        } else {
            return DollarFactory.fromValue(value * newValue.toLong(), errors(), newValue.errors());
        }
    }

    @Override public int sign() {
        return (int) Math.signum(value);
    }

    @Override
    @NotNull
    public Integer toInteger() {
        return value.intValue();
    }

    @NotNull
    @Override
    public Number toNumber() {
        return value;
    }

    @Override
    public var $as(@NotNull Type type) {

        if (type.equals(Type.BOOLEAN)) {
            return DollarStatic.$(value != 0);
        } else if (type.equals(Type.STRING)) {
            return DollarStatic.$(toHumanString());
        } else if (type.equals(Type.LIST)) {
            return DollarStatic.$(Arrays.asList(this));
        } else if (type.equals(Type.MAP)) {
            return DollarStatic.$("value", this);
        } else if (type.equals(Type.INTEGER)) {
            return this;
        } else if (type.equals(Type.DECIMAL)) {
            return DollarStatic.$((double) value);
        } else if (type.equals(Type.VOID)) {
            return DollarStatic.$void();
        } else {
            return DollarFactory.failure(ErrorType.INVALID_CAST);
        }
    }

    @Override public Type $type() {
        return Type.INTEGER;
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (Objects.equals(type, Type.INTEGER)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public var $plus(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.infinite()) {
            return rhsFix;
        }
        if (rhsFix.decimal()) {
            return DollarFactory.fromValue(value.doubleValue() + rhsFix.toDouble(), errors(),
                                           rhsFix.errors());
        } else if (rhsFix.list()) {
            return DollarFactory.fromValue(rhsFix.$prepend(this), errors(), rhsFix.errors());
        } else if (rhsFix.range()) {
            return DollarFactory.fromValue(rhsFix.$plus(this), errors(), rhsFix.errors());
        } else if (rhsFix.string()) {
            return DollarFactory.fromValue(toString() + rhsFix.toString(), errors(), rhsFix.errors());
        } else {
            if (abs(value.longValue()) < Long.MAX_VALUE / 2 && Math.abs(rhsFix.toLong()) < Long.MAX_VALUE / 2) {
                return DollarFactory.fromValue(value + rhsFix.toLong(), errors(), rhsFix.errors());
            } else {
                final BigDecimal added = new BigDecimal(value).add(new BigDecimal(rhsFix.toLong()));
                if (added.abs().compareTo(new BigDecimal(Long.MAX_VALUE)) == 1) {
                    return DollarFactory.fromValue(added, errors(), rhs.errors());
                } else {
                    return DollarFactory.fromValue(value + rhsFix.toLong(), errors(), rhs.errors());
                }
            }
        }
    }

    @Override
    public int compareTo(@NotNull var o) {
        if (o.number()) {
            return $minus(o).toInteger();
        } else {
            return toDollarScript().compareTo(o.toDollarScript());
        }
    }

    @NotNull @Override public String toDollarScript() {
        return toString();
    }

    @NotNull
    @Override
    public Number toJavaObject() {
        return value;
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
        return value != 0;
    }

    @Override
    public boolean number() {
        return true;
    }

    @Override
    public boolean decimal() {
        return false;
    }

    @Override
    public boolean integer() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof var) {
            var unwrapped = ((var) obj)._unwrap();
            if (unwrapped instanceof DollarInteger) {
                return $equals(unwrapped);
            } else {
                return value.toString().equals(obj.toString());
            }
        } else {
            return value.toString().equals(obj.toString());
        }
    }

    @NotNull
    @Override
    public Double toDouble() {
        return value.doubleValue();
    }

    @NotNull @Override
    public Long toLong() {
        return value;
    }

    boolean $equals(@NotNull var other) {
        return value.equals(other.toLong());
    }

}
