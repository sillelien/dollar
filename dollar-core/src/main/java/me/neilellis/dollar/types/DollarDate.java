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
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.abs;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarDate extends AbstractDollarSingleValue<Long> {

    public DollarDate(@NotNull List<Throwable> errors, @NotNull Long value) {
        super(errors, value);
    }

    @NotNull
    @Override
    public Date $() {
        return new Date(value);
    }

    @NotNull
    @Override
    public var $abs() {
        return DollarFactory.fromValue(new Date(abs(value)),errors());
    }

    @NotNull
    @Override
    public var $dec(@NotNull var amount) {
        return DollarFactory.fromValue( value.longValue() - asLong(amount.D()),errors(),amount.errors());
    }

    private long asLong(double d) {
        return (long) (d * (24.0 * 60.0 * 60.0 * 1000.0));
    }

    @NotNull
    @Override
    public var $divide(@NotNull var v) {
        return DollarFactory.fromValue(asDecimal() / v.D(), errors(), v.errors());
    }

    @NotNull
    @Override
    public var $inc(@NotNull var amount) {
        return DollarFactory.fromValue(value.longValue() + asLong(amount.D()),errors(),amount.errors());
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var v) {
        return DollarFactory.fromValue(asDecimal() % v.D(),errors(),v.errors());
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return DollarFactory.fromValue(asDecimal() * v.D(),errors(),v.errors());
    }

    @NotNull
    @Override
    public var $negate() {
        return DollarFactory.fromValue(-asDecimal(),errors());
    }

    private Double asDecimal() {
        return value.doubleValue() / (24 * 60 * 60 * 1000);
    }

    @Override
    public var $as(Type type) {
        switch (type) {
            case BOOLEAN:
                return DollarStatic.$(value != 0);
            case STRING:
                return DollarStatic.$(new Date(value).toString());
            case LIST:
                return DollarStatic.$(Arrays.asList(this));
            case MAP:
                return DollarStatic.$("value", this);
            case DECIMAL:
                return DollarStatic.$(asDecimal());
            case INTEGER:
                return DollarStatic.$(asDecimal());
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
    public boolean is(Type... types) {
        for (Type type : types) {
            if (type == Type.DATE) {
                return true;
            }
        }
        return false;
    }

    @Override
    @NotNull
    public Number number(@NotNull String key) {
        return value;
    }

    @NotNull
    @Override
    public Double D() {
        return asDecimal();
    }

    @Nullable
    @Override
    public Long L() {
        return asDecimal().longValue();
    }

    @Override
    public boolean isDecimal() {
        return false;
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
            if (unwrapped instanceof DollarDate) {
                return $equals(unwrapped);
            } else {
                return toString().equals(obj.toString());
            }
        } else {
            return toString().equals(obj.toString());
        }
    }

    public boolean $equals(var other) {
            return Objects.equals(toString(), other.toString());
    }

    @Override
    public int compareTo(var o) {
        return $minus(o).I();
    }

    @NotNull
    @Override
    public var $minus(var newValue) {
        return DollarFactory.fromValue(new Date(value - asLong(newValue.D())), errors(),
                                       newValue.errors());

    }

    @NotNull
    @Override
    public var $plus(var newValue) {
                return DollarFactory.fromValue(new Date(value + asLong(newValue.D())), errors(), newValue.errors());

    }

    @NotNull @Override public String S() {
        return new Date(value).toString();
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
