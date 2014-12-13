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
    public Double $() {
        return value;
    }

    @NotNull
    @Override
    public var $abs() {
        return DollarFactory.fromValue(abs(value), errors());
    }

    @NotNull
    @Override
    public var $divide(@NotNull var v) {
        return DollarFactory.fromValue(value / v.D(), errors(), v.errors());
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var v) {
        return DollarFactory.fromValue(value % v.D(), errors(), v.errors());
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return DollarFactory.fromValue(value * v.D(), errors(), v.errors());
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
                return DollarStatic.$(value.intValue() != 0);
            case STRING:
                return DollarStatic.$(S());
            case LIST:
                return DollarStatic.$(Arrays.asList(this));
            case MAP:
                return DollarStatic.$("value", this);
            case DECIMAL:
                return this;
            case INTEGER:
                return DollarStatic.$(value.longValue());
            case VOID:
                return DollarStatic.$void();
            default:
                return DollarFactory.failure(FailureType.INVALID_CAST);

        }
    }

    @Override public Type $type() {
        return Type.DECIMAL;
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
            if (type == Type.DECIMAL) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public Double D() {
        return value;
    }

    @NotNull @Override
    public Long L() {
        return value.longValue();
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

    @NotNull
    @Override
    public var $minus(@NotNull var v) {
        return DollarFactory.fromValue(value - v.D(), errors(), v.errors());

    }

    @NotNull
    @Override
    public var $plus(var v) {
        return DollarFactory.fromValue(value + v.D(), errors(), v.errors());
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
        return value.intValue() != 0;
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isNeitherTrueNorFalse() {
        return true;
    }


}
