/*
 *    Copyright (c) 2014-2017 Neil Ellis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dollar.api.types;

import dollar.api.DollarStatic;
import dollar.api.Type;
import dollar.api.exceptions.DollarFailureException;
import dollar.api.var;
import org.jetbrains.annotations.NotNull;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.Objects;

public class DollarDate extends AbstractDollarSingleValue<Instant> {

    private static final double ONE_DAY_SECONDS = 24.0 * 60.0 * 60.0;

    public DollarDate(@NotNull Long value) {
        super(Instant.ofEpochMilli(value));
    }

    public DollarDate(@NotNull Instant value) {
        super(value);
    }

    public DollarDate(@NotNull LocalDateTime value) {
        super(value.toInstant(ZoneOffset.UTC));
    }

    @NotNull
    @Override
    public var $abs() {
        return this;
    }

    @NotNull
    @Override
    public var $as(@NotNull Type type) {
        if (type.is(Type._BOOLEAN)) {
            return DollarStatic.$(true);
        } else if (type.is(Type._STRING)) {
            return DollarStatic.$(value.toString());
        } else if (type.is(Type._LIST)) {
            return DollarStatic.$(Collections.singletonList(this));
        } else if (type.is(Type._MAP)) {
            return DollarStatic.$("value", this);
        } else if (type.is(Type._DECIMAL)) {
            return DollarStatic.$(asDecimal());
        } else if (type.is(Type._INTEGER)) {
            return DollarStatic.$(asDecimal().longValue());
        } else if (type.is(Type._VOID)) {
            return DollarStatic.$void();
        } else {
            throw new DollarFailureException(ErrorType.INVALID_CAST);
        }
    }

    @NotNull
    @Override
    public var $divide(@NotNull var rhs) {
        var rhsFix = rhs.$fixDeep();
        if (rhsFix.zero() || (rhsFix.toDouble() == 0.0)) {
            return DollarFactory.infinity(rhs.positive());
        }
        return DollarFactory.fromValue(asDecimal() / rhs.toDouble());
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        return DollarFactory.wrap(new DollarDecimal(asDecimal() % rhs.toDouble()));
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var rhs) {
        return DollarFactory.wrap(new DollarDecimal(asDecimal() * rhs.toDouble()));
    }

    @NotNull
    @Override
    public var $negate() {
        return DollarFactory.fromValue(-asDecimal());
    }

    @NotNull
    @Override
    public Type $type() {
        return new Type(Type._DATE, constraintLabel());
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type.is(Type._DATE)) {
                return true;
            }
        }
        return false;
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
    public int sign() {
        return (int) Math.signum(asDecimal());
    }

    @NotNull
    @Override
    public String toDollarScript() {
        return String.format("(\"%s\" as Date)", toString());
    }

    @Override
    @NotNull
    public int toInteger() {
        return asDecimal().intValue();
    }

    @NotNull
    @Override
    public String toJavaObject() {
        return value.toString();
    }

    @NotNull
    @Override
    public Number toNumber() {
        return asDecimal();
    }

    @NotNull
    @Override
    public String toYaml() {
        return "date: " + value;
    }

    @Override
    public boolean truthy() {
        return true;
    }

    @NotNull
    @Override
    public var $get(@NotNull var key) {
        final LocalDateTime localDateTime = LocalDateTime.ofInstant(value, ZoneId.systemDefault());
        return DollarFactory.fromValue(localDateTime.get(ChronoField.valueOf(key.toHumanString().toUpperCase())));
    }

    @NotNull
    @Override
    public var $plus(@NotNull var rhs) {
        var rhsFix = rhs.$fixDeep();
        if (rhsFix.string()) {
            return DollarFactory.fromValue(toString() + rhsFix);
        } else if (rhsFix.infinite()) {
            return rhs;
        } else if (rhsFix.list()) {
            return DollarFactory.fromValue(rhsFix.$prepend(this));
        } else if (rhsFix.range()) {
            return DollarFactory.fromValue(rhsFix.$plus(this));
        } else {
            try {
                return DollarFactory.fromValue(value.plusSeconds((long) (ONE_DAY_SECONDS * rhsFix.toDouble())));
            } catch (DateTimeException dte) {
                if ((value.getEpochSecond() + (ONE_DAY_SECONDS * rhsFix.toDouble())) > 0) {
                    return DollarFactory.fromValue(LocalDateTime.MAX);
                } else {
                    return DollarFactory.fromValue(LocalDateTime.MIN);
                }
            }
        }

    }

    @NotNull
    @Override
    public var $set(@NotNull var key, @NotNull Object v) {
        return DollarFactory.fromValue(
                LocalDateTime.ofInstant(value, ZoneId.systemDefault())
                        .with(ChronoField.valueOf(key.toHumanString().toUpperCase()),
                              DollarFactory.fromValue(v).toLong())
        );
    }

    @NotNull
    @Override
    public String toHumanString() {
        return value.toString();
    }

    @NotNull
    private Double asDecimal() {
        try {
            final long millis = value.toEpochMilli();
            return ((double) millis) / (24 * 60 * 60 * 1000);
        } catch (ArithmeticException e) {
            final long seconds = value.getEpochSecond();
            return ((double) seconds) / (24 * 60 * 60);

        }
    }

    @Override
    public int compareTo(@NotNull var o) {
        return $minus(o).toInteger();
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
    public boolean number() {
        return true;
    }

    @Override
    public double toDouble() {
        return asDecimal();
    }

    @NotNull
    @Override
    public long toLong() {
        return asDecimal().longValue();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof var) {
            var unwrapped = ((var) obj).$unwrap();
            if (unwrapped instanceof DollarDate) {
                return Objects.equals(toString(), unwrapped.toString());
            } else {
                return toString().equals(obj.toString());
            }
        } else if (obj == null) {
            return false;
        } else {
            return toString().equals(obj.toString());
        }
    }

}
