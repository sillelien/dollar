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
import dollar.api.Value;
import dollar.api.exceptions.DollarFailureException;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Collections;

import static java.lang.Math.abs;

public class DollarInteger extends AbstractDollarSingleValue<Long> {

    public DollarInteger(@NotNull Long value) {
        super(value);
        if (value < -Long.MAX_VALUE) {
            throw new IllegalArgumentException(
                                                      "Cannot create a Dollar Integer with a value less than -" + Long.MAX_VALUE);
        }
    }

    @NotNull
    @Override
    public Value $abs() {
        return DollarFactory.fromValue(abs(value));
    }

    @NotNull
    @Override
    public Value $as(@NotNull Type type) {

        if (type.is(Type._BOOLEAN)) {
            return DollarStatic.$(value != 0);
        } else if (type.is(Type._STRING)) {
            return DollarStatic.$(toHumanString());
        } else if (type.is(Type._LIST)) {
            return DollarStatic.$(Collections.singletonList(this));
        } else if (type.is(Type._MAP)) {
            return DollarStatic.$("value", this);
        } else if (type.is(Type._INTEGER)) {
            return this;
        } else if (type.is(Type._DECIMAL)) {
            return DollarStatic.$((double) value);
        } else if (type.is(Type._VOID)) {
            return DollarStatic.$void();
        } else {
            throw new DollarFailureException(ErrorType.INVALID_CAST);
        }
    }

    @NotNull
    @Override
    public Value $divide(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();
        if (rhsFix.infinite()) {
            return DollarFactory.fromValue(0);
        } else if (rhsFix.decimal() && (rhsFix.toDouble() != 0.0)) {
            return DollarFactory.fromValue(value.doubleValue() / rhsFix.toDouble());
        } else if (rhsFix.toLong() == 0) {
            return DollarFactory.infinity(positive());
        } else {
            return DollarFactory.fromValue(value / rhsFix.toLong());
        }
    }

    @NotNull
    @Override
    public Value $modulus(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();
        if (rhsFix.infinite()) {
            return DollarFactory.infinity(positive());
        }
        if (rhsFix.zero()) {
            return DollarFactory.infinity(positive());
        }
        if (rhsFix.decimal()) {
            return DollarFactory.fromValue(value.doubleValue() % rhsFix.toDouble());
        } else {
            return DollarFactory.fromValue(value % rhsFix.toLong());
        }
    }

    @NotNull
    @Override
    public Value $multiply(@NotNull Value newValue) {
        if (newValue.infinite()) {
            return newValue.$multiply(this);
        }
        if (newValue.zero()) {
            return DollarFactory.fromValue(0);
        }

        if (newValue.decimal()) {
            return DollarFactory.fromValue(value.doubleValue() * newValue.toDouble());
        } else {
            return DollarFactory.fromValue(value * newValue.toLong());
        }
    }

    @NotNull
    @Override
    public Value $negate() {
        return DollarFactory.fromValue(-value);
    }

    @NotNull
    @Override
    public Type $type() {
        return new Type(Type._INTEGER, constraintLabel());
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type.is(Type._INTEGER)) {
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
        return (int) Math.signum(value);
    }

    @NotNull
    @Override
    public String toDollarScript() {
        return toString();
    }

    @Override
    public int toInteger() {
        return value.intValue();
    }

    @NotNull
    @Override
    public Number toJavaObject() {
        return value;
    }

    @NotNull
    @Override
    public Number toNumber() {
        return value;
    }

    @NotNull
    @Override
    public String toYaml() {
        return "integer: " + value;
    }

    @Override
    public boolean truthy() {
        return value != 0;
    }

    @NotNull
    @Override
    public Value $plus(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();
        if (rhsFix.infinite()) {
            return rhsFix;
        }
        if (rhsFix.decimal()) {
            return DollarFactory.fromValue(value.doubleValue() + rhsFix.toDouble());
        } else if (rhsFix.list()) {
            return DollarFactory.fromValue(rhsFix.$prepend(this));
        } else if (rhsFix.range()) {
            return DollarFactory.fromValue(rhsFix.$plus(this));
        } else if (rhsFix.string()) {
            return DollarFactory.fromValue(toString() + rhsFix);
        } else {
            if ((abs(value) < (Long.MAX_VALUE / 2)) && (Math.abs(rhsFix.toLong()) < (Long.MAX_VALUE / 2))) {
                return DollarFactory.fromValue(value + rhsFix.toLong());
            } else {
                final BigDecimal added = new BigDecimal(value).add(new BigDecimal(rhsFix.toLong()));
                if (added.abs().compareTo(new BigDecimal(Long.MAX_VALUE)) == 1) {
                    return DollarFactory.fromValue(added);
                } else {
                    return DollarFactory.fromValue(value + rhsFix.toLong());
                }
            }
        }
    }

    @Override
    public int compareTo(@NotNull Value o) {
        if (equals(o)) {
            return 0;
        }
        if (o.number()) {
            return (value.compareTo(o.toLong()));
        } else {
            return 1;
        }
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
    public boolean number() {
        return true;
    }

    @Override
    public double toDouble() {
        return value.doubleValue();
    }

    @Override
    public long toLong() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Value) {
            Value unwrapped = ((Value) obj).$unwrap();
            if (unwrapped instanceof DollarInteger) {
                return value.equals(unwrapped.toLong());
            } else {
                return value.toString().equals(obj.toString());
            }
        } else {
            return value.toString().equals(obj.toString());
        }
    }

}
