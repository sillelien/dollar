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
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;

import static java.lang.Math.abs;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
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
        if (rhsFix.isInfinite()) {
            return DollarFactory.fromValue(0, errors(), rhsFix.errors());
        } else if (rhsFix.isDecimal() && rhsFix.D() != 0.0) {
            return DollarFactory.fromValue(value.doubleValue() / rhsFix.D(), errors(),
                                           rhsFix.errors());
        } else if (rhsFix.L() == 0) {
            return DollarFactory.infinity(isPositive(), errors(), rhsFix.errors());
        } else {
            return DollarFactory.fromValue(value / rhsFix.L(), errors(), rhsFix.errors());
        }
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.isInfinite()) {
            return DollarFactory.infinity(isPositive(), errors(), rhsFix.errors());
        }
        if (rhsFix.isZero()) {
            return DollarFactory.infinity(isPositive(), errors(), rhsFix.errors());
        }
        if (rhsFix.isDecimal()) {
            return DollarFactory.fromValue(value.doubleValue() % rhsFix.D(), errors(),
                                           rhsFix.errors());
        } else {
            return DollarFactory.fromValue(value % rhsFix.L(), errors(), rhsFix.errors());
        }
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var newValue) {
        if (newValue.isInfinite()) {
            return newValue.$multiply(this);
        }
        if (newValue.isZero()) {
            return DollarFactory.fromValue(0, errors(), newValue.errors());
        }

        if (newValue.isDecimal()) {
            return DollarFactory.fromValue(value.doubleValue() * newValue.D(), errors(),
                                           newValue.errors());
        } else {
            return DollarFactory.fromValue(value * newValue.L(), errors(), newValue.errors());
        }
    }

    @Override
    @NotNull
    public Integer I() {
        return value.intValue();
    }

    @NotNull
    @Override
    public Number N() {
        return value;
    }

    @Override public int sign() {
        return (int) Math.signum(value);
    }

    @Override
    public var $as(Type type) {

        if (type.equals(Type.BOOLEAN)) {
            return DollarStatic.$(value != 0);
        } else if (type.equals(Type.STRING)) {
            return DollarStatic.$(S());
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
            return DollarFactory.failure(me.neilellis.dollar.types.ErrorType.INVALID_CAST);
        }
    }

    @Override public Type $type() {
        return Type.INTEGER;
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type == Type.INTEGER) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public var $plus(var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.isInfinite()) {
            return rhsFix;
        }
        if (rhsFix.isDecimal()) {
            return DollarFactory.fromValue(value.doubleValue() + rhsFix.D(), errors(),
                                           rhsFix.errors());
        } else if (rhsFix.isList()) {
            return DollarFactory.fromValue(rhsFix.$prepend(this), errors(), rhsFix.errors());
        } else if (rhsFix.isRange()) {
            return DollarFactory.fromValue(rhsFix.$plus(this), errors(), rhsFix.errors());
        } else if (rhsFix.isString()) {
            return DollarFactory.fromValue(toString() + rhsFix.toString(), errors(), rhsFix.errors());
        } else {
            if (Math.abs(value.longValue()) < Long.MAX_VALUE / 2 && Math.abs(rhsFix.L()) < Long.MAX_VALUE / 2) {
                return DollarFactory.fromValue(value + rhsFix.L(), errors(), rhsFix.errors());
            } else {
                final BigDecimal added = new BigDecimal(value).add(new BigDecimal(rhsFix.L()));
                if (added.abs().compareTo(new BigDecimal(Long.MAX_VALUE)) == 1) {
                    return DollarFactory.fromValue(added, errors(), rhs.errors());
                } else {
                    return DollarFactory.fromValue(value + rhsFix.L(), errors(), rhs.errors());
                }
            }
        }
    }

    @NotNull @Override
    public Long L() {
        return value;
    }

    @NotNull
    @Override
    public Double D() {
        return value.doubleValue();
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

    @Override
    public boolean isDecimal() {
        return false;
    }

    @Override
    public boolean isInteger() {
        return true;
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    boolean $equals(var other) {
        return value.equals(other.L());
    }

    @Override
    public int compareTo(@NotNull var o) {
        if (o.isNumber()) {
            return $minus(o).I();
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
    public boolean isNeitherTrueNorFalse() {
        return true;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean isTruthy() {
        return value != 0;
    }

}
