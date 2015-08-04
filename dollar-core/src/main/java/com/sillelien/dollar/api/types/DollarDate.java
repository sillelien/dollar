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

import java.time.*;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Objects;

public class DollarDate extends AbstractDollarSingleValue<Instant> {

    private static final double ONE_DAY_MILLIS = 24.0 * 60.0 * 60.0 * 1000.0;
    private static final double ONE_DAY_SECONDS = 24.0 * 60.0 * 60.0;

    public DollarDate(@NotNull ImmutableList<Throwable> errors, @NotNull Long value) {
        super(errors, Instant.ofEpochMilli(value));
    }

    public DollarDate(@NotNull ImmutableList<Throwable> errors, @NotNull Instant value) {
        super(errors, value);
    }

    public DollarDate(@NotNull ImmutableList<Throwable> errors, @NotNull LocalDateTime value) {
        super(errors, value.toInstant(ZoneOffset.UTC));
    }

    @NotNull
    @Override
    public var $abs() {
        return this;
    }

    @NotNull
    @Override
    public var $negate() {
        return DollarFactory.fromValue(-asDecimal(), errors());
    }

    @NotNull
    @Override
    public var $divide(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.zero() || rhsFix.toDouble() == null || rhsFix.toDouble() == 0.0) {
            return DollarFactory.infinity(rhs.positive(), errors(), rhs.errors());
        }
        return DollarFactory.fromValue(asDecimal() / rhs.toDouble(), errors(), rhs.errors());
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        return DollarFactory.fromValue(asDecimal() % rhs.toDouble(), errors(), rhs.errors());
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return DollarFactory.fromValue(asDecimal() * v.toDouble(), errors(), v.errors());
    }

    @Override public int sign() {
        return (int) Math.signum(asDecimal());
    }

    @Override
    @NotNull
    public Integer toInteger() {
        return asDecimal().intValue();
    }

    @NotNull
    @Override
    public Number toNumber() {
        return asDecimal();
    }

    @NotNull private Double asDecimal() {
        try {
            final long millis = value.toEpochMilli();
            return ((double) millis) / (24 * 60 * 60 * 1000);
        } catch (ArithmeticException e) {
            final long seconds = value.getEpochSecond();
            return ((double) seconds) / (24 * 60 * 60);

        }
    }

    @Override
    public var $as(@NotNull Type type) {
        if (type.equals(Type.BOOLEAN)) {
            return DollarStatic.$(true);
        } else if (type.equals(Type.STRING)) {
            return DollarStatic.$(value.toString());
        } else if (type.equals(Type.LIST)) {
            return DollarStatic.$(Arrays.asList(this));
        } else if (type.equals(Type.MAP)) {
            return DollarStatic.$("value", this);
        } else if (type.equals(Type.DECIMAL)) {
            return DollarStatic.$(asDecimal());
        } else if (type.equals(Type.INTEGER)) {
            return DollarStatic.$(asDecimal().longValue());
        } else if (type.equals(Type.VOID)) {
            return DollarStatic.$void();
        } else {
            return DollarFactory.failure(ErrorType.INVALID_CAST);
        }
    }

    @Override public Type $type() {
        return Type.DATE;
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type == Type.DATE) {
                return true;
            }
        }
        return false;
    }

    @NotNull @Override public var $get(@NotNull var key) {
        final LocalDateTime localDateTime = LocalDateTime.ofInstant(value, ZoneId.systemDefault());
        return DollarFactory.fromValue(localDateTime.get(ChronoField.valueOf(key.toHumanString().toUpperCase())),
                                       errors());
    }

//    @NotNull
//    @Override
//    public var $dec(@NotNull var amount) {
//        return DollarFactory.fromValue(value.plusSeconds((long) (ONE_DAY_SECONDS * amount.toDouble())), errors(),
//                                       amount.errors());
//    }
//
//    @NotNull
//    @Override
//    public var $inc(@NotNull var amount) {
//        return DollarFactory.fromValue(value.minusSeconds((long) (ONE_DAY_SECONDS * amount.toDouble())), errors(),
//                                       amount.errors());
//    }

    @NotNull @Override public var $set(@NotNull var key, Object v) {
        return DollarFactory.fromValue(
                LocalDateTime.ofInstant(value, ZoneId.systemDefault())
                             .with(ChronoField.valueOf(key.toHumanString().toUpperCase()),
                                   DollarFactory.fromValue(v).toLong()),
                errors(),
                key.errors());
    }

    @NotNull @Override public String toHumanString() {
        return value.toString();
    }

    @NotNull
    @Override
    public var $plus(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.string()) {
            return DollarFactory.fromValue(toString() + rhsFix.toString(), errors(), rhsFix.errors());
        } else if (rhsFix.infinite()) {
            return rhs;
        } else if (rhsFix.list()) {
            return DollarFactory.fromValue(rhsFix.$prepend(this), errors(), rhsFix.errors());
        } else if (rhsFix.range()) {
            return DollarFactory.fromValue(rhsFix.$plus(this), errors(), rhsFix.errors());
        } else {
            try {
                return DollarFactory.fromValue(value.plusSeconds((long) (ONE_DAY_SECONDS * rhsFix.toDouble())),
                                               errors(),
                                               rhsFix.errors());
            } catch (DateTimeException dte) {
                if (value.getEpochSecond() + (ONE_DAY_SECONDS * rhsFix.toDouble()) > 0) {
                    return DollarFactory.fromValue(LocalDateTime.MAX, errors(),
                                                   rhsFix.errors());
                } else {
                    return DollarFactory.fromValue(LocalDateTime.MIN, errors(),
                                                   rhsFix.errors());
                }
            }
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
        return true;
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
        return false;
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

    @NotNull
    @Override
    public Double toDouble() {
        return asDecimal();
    }

    @NotNull @Override
    public Long toLong() {
        return asDecimal().longValue();
    }

    boolean $equals(@NotNull var other) {
        return Objects.equals(toString(), other.toString());
    }

    @NotNull @Override public String toDollarScript() {
        return String.format("(\"%s\" as Date)", toString());
    }

    @NotNull
    @Override
    public String toJavaObject() {
        return value.toString();
    }

    private long asLong(double d) {
        return (long) (d * ONE_DAY_MILLIS);
    }

}
