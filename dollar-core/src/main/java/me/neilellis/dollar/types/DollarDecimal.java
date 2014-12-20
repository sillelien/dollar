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

import java.util.Arrays;

import static java.lang.Math.abs;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
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
        if (rhsFix.D() == null || rhsFix.isZero()) {
            return DollarFactory.infinity(isPositive(), errors(), rhsFix.errors());
        }
        if (rhsFix.isInfinite() || new Double(value / rhsFix.D()).isInfinite()) {
            return DollarFactory.fromValue(0, errors(), rhsFix.errors());
        }
        return DollarFactory.fromValue(value / rhsFix.D(), errors(), rhsFix.errors());
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.isInfinite()) {
            return DollarFactory.fromValue(0.0, errors(), rhsFix.errors());
        }
        if (rhsFix.isZero()) {
            return DollarFactory.infinity(isPositive(), errors(), rhsFix.errors());
        }

        return DollarFactory.fromValue(value % rhsFix.D(), errors(), rhsFix.errors());
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.isInfinite()) {
            return rhsFix.$multiply(this);
        }
        if (rhsFix.isZero()) {
            return DollarFactory.fromValue(0.0, errors(), rhsFix.errors());
        }

        if (new Double(value * rhsFix.D()).isInfinite()) {
            return DollarFactory.infinity(Math.signum(value) * Math.signum(rhsFix.D()) > 0, errors(), rhs.errors());
        }
        return DollarFactory.fromValue(value * rhsFix.D(), errors(), rhsFix.errors());
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

    @Override
    public var $as(Type type) {
        if (type.equals(Type.BOOLEAN)) {
            return DollarStatic.$(value.intValue() != 0);
        } else if (type.equals(Type.STRING)) {
            return DollarStatic.$(S());
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
            return DollarFactory.failure(me.neilellis.dollar.types.ErrorType.INVALID_CAST);
        }
    }

    @Override public Type $type() {
        return Type.DECIMAL;
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type == Type.DECIMAL) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public var $plus(var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.isString()) {
            return DollarFactory.fromValue(toString() + rhsFix.toString(), errors(), rhsFix.errors());
        } else if (rhsFix.isRange()) {
            return DollarFactory.fromValue(rhsFix.$plus(this), errors(), rhsFix.errors());
        } else if (rhsFix.isList()) {
            return DollarFactory.fromValue(rhsFix.$prepend(this), errors(), rhsFix.errors());
        } else {
            if (new Double(value + rhsFix.D()).isInfinite()) {
                return DollarFactory.infinity(Math.signum(value + rhsFix.D()) > 0, errors(), rhs.errors());
            }
            return DollarFactory.fromValue(value + rhsFix.D(), errors(), rhsFix.errors());
        }
    }

    @NotNull @Override
    public Long L() {
        return value.longValue();
    }

    @NotNull
    @Override
    public Double D() {
        return value;
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

    @Override
    public boolean isDecimal() {
        return true;
    }

    @Override
    public boolean isInteger() {
        return false;
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    boolean $equals(var other) {
        if (isInteger()) {
            return value.longValue() == other.L();
        } else {
            return value.doubleValue() == other.D();
        }
    }

    @Override
    public int compareTo(@NotNull var o) {
        return $minus(o).I();
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
        return value.intValue() != 0;
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
