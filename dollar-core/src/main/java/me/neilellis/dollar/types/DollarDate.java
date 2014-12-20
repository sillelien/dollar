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

import java.time.*;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarDate extends AbstractDollarSingleValue<LocalDateTime> {

    private static final double ONE_DAY_MILLIS = 24.0 * 60.0 * 60.0 * 1000.0;
    private static final double ONE_DAY_SECONDS = 24.0 * 60.0 * 60.0;

    public DollarDate(@NotNull ImmutableList<Throwable> errors, @NotNull Long value) {
        super(errors, LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault()));
    }

    public DollarDate(ImmutableList<Throwable> errors, LocalDateTime value) {
        super(errors, value);
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
    public var $divide(@NotNull var v) {
        var rhsFix = v._fixDeep();
        if (rhsFix.isZero() || rhsFix.D() == null || rhsFix.D() == 0.0) {
            return DollarFactory.infinity(v.isPositive(), errors(), v.errors());
        }
        return DollarFactory.fromValue(asDecimal() / v.D(), errors(), v.errors());
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var v) {
        return DollarFactory.fromValue(asDecimal() % v.D(), errors(), v.errors());
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return DollarFactory.fromValue(asDecimal() * v.D(), errors(), v.errors());
    }

    @Override
    @NotNull
    public Integer I() {
        return asDecimal().intValue();
    }

    @NotNull
    @Override
    public Number N() {
        return asDecimal();
    }

    @Override public int sign() {
        return (int) Math.signum(asDecimal());
    }

    private Double asDecimal() {
        try {
            final long millis = value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            return ((double) millis) / (24 * 60 * 60 * 1000);
        } catch (ArithmeticException e) {
            final long seconds = value.atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
            return ((double) seconds) / (24 * 60 * 60);

        }
    }

    @Override
    public var $as(Type type) {
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
            return DollarFactory.failure(me.neilellis.dollar.types.ErrorType.INVALID_CAST);
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
        return DollarFactory.fromValue(value.get(ChronoField.valueOf(key.S().toUpperCase())), errors());
    }

//    @NotNull
//    @Override
//    public var $dec(@NotNull var amount) {
//        return DollarFactory.fromValue(value.plusSeconds((long) (ONE_DAY_SECONDS * amount.D())), errors(),
//                                       amount.errors());
//    }
//
//    @NotNull
//    @Override
//    public var $inc(@NotNull var amount) {
//        return DollarFactory.fromValue(value.minusSeconds((long) (ONE_DAY_SECONDS * amount.D())), errors(),
//                                       amount.errors());
//    }

    @NotNull @Override public var $set(@NotNull var key, Object v) {
        return DollarFactory.fromValue(
                value.with(ChronoField.valueOf(key.S().toUpperCase()), DollarFactory.fromValue(v).L()), errors(),
                key.errors());
    }

    @NotNull
    @Override
    public var $plus(var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.isString()) {
            return DollarFactory.fromValue(toString() + rhsFix.toString(), errors(), rhsFix.errors());
        } else if (rhsFix.isInfinite()) {
            return rhs;
        } else if (rhsFix.isList()) {
            return DollarFactory.fromValue(rhsFix.$prepend(this), errors(), rhsFix.errors());
        } else if (rhsFix.isRange()) {
            return DollarFactory.fromValue(rhsFix.$plus(this), errors(), rhsFix.errors());
        } else {
            try {
                return DollarFactory.fromValue(value.plusSeconds((long) (ONE_DAY_SECONDS * rhsFix.D())), errors(),
                                               rhsFix.errors());
            } catch (DateTimeException dte) {
                if (value.toEpochSecond(ZoneOffset.UTC) + (ONE_DAY_SECONDS * rhsFix.D()) > 0) {
                    return DollarFactory.fromValue(LocalDateTime.MAX, errors(),
                                                   rhsFix.errors());
                } else {
                    return DollarFactory.fromValue(LocalDateTime.MIN, errors(),
                                                   rhsFix.errors());
                }
            }
        }

    }

    @NotNull @Override public String S() {
        return value.toString();
    }

    @NotNull @Override
    public Long L() {
        return asDecimal().longValue();
    }

    @NotNull
    @Override
    public Double D() {
        return asDecimal();
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

    boolean $equals(var other) {
        return Objects.equals(toString(), other.toString());
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
        return true;
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
