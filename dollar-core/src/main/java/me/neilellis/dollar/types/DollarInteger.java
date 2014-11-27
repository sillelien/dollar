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
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.abs;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarInteger extends AbstractDollarSingleValue<Long> {

    public DollarInteger(@NotNull List<Throwable> errors, @NotNull Long value) {
        super(errors, value);
    }

    @NotNull
    @Override
    public Number $() {
        return value;
    }

    @NotNull
    @Override
    public var $abs() {
        return DollarFactory.fromValue(abs(value.longValue()), errors());
    }

    @NotNull
    @Override
    public var $divide(@NotNull var newValue) {
        if (newValue.isDecimal()) {
            return DollarFactory.fromValue(value.doubleValue() / newValue.D(), errors(),
                                           newValue.errors());
        } else {
            return DollarFactory.fromValue(value / newValue.L(), errors(), newValue.errors());
        }
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var newValue) {
        if (newValue.isDecimal()) {
            return DollarFactory.fromValue(value.doubleValue() % newValue.D(), errors(),
                                           newValue.errors());
        } else {
            return DollarFactory.fromValue(value % newValue.L(), errors(), newValue.errors());
        }
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var newValue) {
        if (newValue.isDecimal()) {
            return DollarFactory.fromValue(value.doubleValue() * newValue.D(), errors(),
                                           newValue.errors());
        } else {
            return DollarFactory.fromValue(value * newValue.L(), errors(), newValue.errors());
        }
    }

    @NotNull
    @Override
    public var $negate() {
        return DollarFactory.fromValue(-value, errors());
    }

    @Override
    public var $as(Type type) {
        switch (type) {
            case BOOLEAN:
                return DollarStatic.$(value != 0);
            case STRING:
                return DollarStatic.$(S());
            case LIST:
                return DollarStatic.$(Arrays.asList(this));
            case MAP:
                return DollarStatic.$("value", this);
            case INTEGER:
                return this;
            case DECIMAL:
                return DollarStatic.$((double) value);
            case VOID:
                return DollarStatic.$void();
            default:
                return DollarFactory.failure(DollarFail.FailureType.INVALID_CAST);

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
    public Double D() {
        return value.doubleValue();
    }

    @NotNull @Override
    public Long L() {
        return value;
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

    public boolean $equals(var other) {
        return value.equals(other.L());
    }

    @Override
    public int compareTo(var o) {
        return $minus(o).I();
    }

    @NotNull
    @Override
    public var $minus(var newValue) {
        if (newValue.isDecimal()) {
            return DollarFactory.fromValue(value.doubleValue() - newValue.D(), errors(),
                                           newValue.errors());
        } else {
            return DollarFactory.fromValue(value - newValue.L(), errors(), newValue.errors());
        }
    }

    @NotNull
    @Override
    public var $plus(var newValue) {
        if (newValue.isDecimal()) {
            return DollarFactory.fromValue(value.doubleValue() + newValue.D(), errors(),
                                           newValue.errors());
        } else {
            return DollarFactory.fromValue(value + newValue.L(), errors(), newValue.errors());
        }
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
        return value != 0;
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
    @NotNull
    public Number number(@NotNull String key) {
        return value;
    }

    @NotNull
    @Override
    public var $dec(@NotNull var amount) {
        return DollarFactory.fromValue(value - amount.L(), errors(), amount.errors());
    }

    @NotNull
    @Override
    public var $inc(@NotNull var amount) {
        return DollarFactory.fromValue(value + amount.L(), errors(), amount.errors());
    }
}
