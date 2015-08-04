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

import java.util.Arrays;
import java.util.Objects;

import static java.lang.Math.abs;

public class DollarDecimal extends AbstractDollarSingleValue<Double> {

    public DollarDecimal(@NotNull ImmutableList<Throwable> errors, @NotNull Double value) {
        super(errors, value);
    }

    @NotNull
    @Override
    public var $abs() {
        return DollarFactory.fromValue(abs(value), errors());
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
        if (rhsFix.toDouble() == null || rhsFix.zero()) {
            return DollarFactory.infinity(positive(), errors(), rhsFix.errors());
        }
        if (rhsFix.infinite() || new Double(value / rhsFix.toDouble()).isInfinite()) {
            return DollarFactory.fromValue(0, errors(), rhsFix.errors());
        }
        return DollarFactory.fromValue(value / rhsFix.toDouble(), errors(), rhsFix.errors());
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.infinite()) {
            return DollarFactory.fromValue(0.0, errors(), rhsFix.errors());
        }
        if (rhsFix.zero()) {
            return DollarFactory.infinity(positive(), errors(), rhsFix.errors());
        }

        return DollarFactory.fromValue(value % rhsFix.toDouble(), errors(), rhsFix.errors());
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.infinite()) {
            return rhsFix.$multiply(this);
        }
        if (rhsFix.zero()) {
            return DollarFactory.fromValue(0.0, errors(), rhsFix.errors());
        }

        if (new Double(value * rhsFix.toDouble()).isInfinite()) {
            return DollarFactory.infinity(Math.signum(value) * Math.signum(rhsFix.toDouble()) > 0, errors(),
                                          rhs.errors());
        }
        return DollarFactory.fromValue(value * rhsFix.toDouble(), errors(), rhsFix.errors());
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
            return DollarStatic.$(value.intValue() != 0);
        } else if (type.equals(Type.STRING)) {
            return DollarStatic.$(toHumanString());
        } else if (type.equals(Type.LIST)) {
            return DollarStatic.$(Arrays.asList(this));
        } else if (type.equals(Type.MAP)) {
            return DollarStatic.$("value", this);
        } else if (type.equals(Type.DECIMAL)) {
            return this;
        } else if (type.equals(Type.INTEGER)) {
            return DollarStatic.$(value.longValue());
        } else if (type.equals(Type.VOID)) {
            return DollarStatic.$void();
        } else {
            return DollarFactory.failure(ErrorType.INVALID_CAST);
        }
    }

    @Override public Type $type() {
        return Type.DECIMAL;
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (Objects.equals(type, Type.DECIMAL)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public var $plus(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.string()) {
            return DollarFactory.fromValue(toString() + rhsFix.toString(), errors(), rhsFix.errors());
        } else if (rhsFix.range()) {
            return DollarFactory.fromValue(rhsFix.$plus(this), errors(), rhsFix.errors());
        } else if (rhsFix.list()) {
            return DollarFactory.fromValue(rhsFix.$prepend(this), errors(), rhsFix.errors());
        } else {
            if (new Double(value + rhsFix.toDouble()).isInfinite()) {
                return DollarFactory.infinity(Math.signum(value + rhsFix.toDouble()) > 0, errors(), rhs.errors());
            }
            return DollarFactory.fromValue(value + rhsFix.toDouble(), errors(), rhsFix.errors());
        }
    }

    @Override
    public int compareTo(@NotNull var o) {
        return $minus(o).toInteger();
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
        return value.intValue() != 0;
    }

    @Override
    public boolean number() {
        return true;
    }

    @Override
    public boolean decimal() {
        return true;
    }

    @Override
    public boolean integer() {
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof var) {
            var unwrapped = ((var) obj)._unwrap();
            if (unwrapped instanceof DollarDecimal) {
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
        return value;
    }

    @NotNull @Override
    public Long toLong() {
        return value.longValue();
    }

    boolean $equals(@NotNull var other) {
        if (integer()) {
            return value.longValue() == other.toLong();
        } else {
            return value.doubleValue() == other.toDouble();
        }
    }

    @NotNull @Override public String toDollarScript() {
        return toString();
    }


    @NotNull
    @Override
    public Double toJavaObject() {
        return value;
    }

}
