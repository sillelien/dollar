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

import java.util.Collections;

import static dollar.api.types.DollarFactory.fromValue;
import static java.lang.Math.abs;

public class DollarDecimal extends AbstractDollarSingleValue<Double> {

    public DollarDecimal(@NotNull Double value) {
        super(value);
    }

    @NotNull
    @Override
    public Value $abs() {
        return fromValue(abs(value));
    }

    @NotNull
    @Override
    public Value $as(@NotNull Type type) {
        if (type.is(Type._BOOLEAN)) {
            return DollarStatic.$(value.intValue() != 0);
        } else if (type.is(Type._STRING)) {
            return DollarStatic.$(toHumanString());
        } else if (type.is(Type._LIST)) {
            return DollarStatic.$(Collections.singletonList(this));
        } else if (type.is(Type._MAP)) {
            return DollarStatic.$("value", this);
        } else if (type.is(Type._DECIMAL)) {
            return this;
        } else if (type.is(Type._INTEGER)) {
            return DollarStatic.$(value.longValue());
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
        if (rhsFix.zero()) {
            return DollarFactory.infinity(positive());
        }
        if (rhsFix.infinite() || Double.valueOf(value / rhsFix.toDouble()).isInfinite()) {
            return fromValue(0);
        }
        return fromValue(value / rhsFix.toDouble());
    }

    @NotNull
    @Override
    public Value $modulus(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();
        if (rhsFix.infinite()) {
            return fromValue(0.0);
        }
        if (rhsFix.zero()) {
            return DollarFactory.infinity(positive());
        }

        return fromValue(value % rhsFix.toDouble());
    }

    @NotNull
    @Override
    public Value $multiply(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();
        if (rhsFix.infinite()) {
            return rhsFix.$multiply(this);
        }
        if (rhsFix.zero()) {
            return fromValue(0.0);
        }

        if (Double.valueOf(value * rhsFix.toDouble()).isInfinite()) {
            return DollarFactory.infinity((Math.signum(value) * Math.signum(rhsFix.toDouble())) > 0);
        }
        return fromValue(value * rhsFix.toDouble());
    }

    @NotNull
    @Override
    public Value $negate() {
        return fromValue(-value);
    }

    @NotNull
    @Override
    public Type $type() {
        return new Type(Type._DECIMAL, constraintLabel());
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type.is(Type._DECIMAL)) {
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

    @NotNull
    @Override
    public String toDollarScript() {
        return toString();
    }

    @Override
    @NotNull
    public int toInteger() {
        return value.intValue();
    }

    @NotNull
    @Override
    public Double toJavaObject() {
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
        return "decimal: " + value;
    }

    @Override
    public boolean truthy() {
        return value.intValue() != 0;
    }

    @NotNull
    @Override
    public Value $plus(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();
        if (rhsFix.string()) {
            return fromValue(toString() + rhsFix);
        } else if (rhsFix.range()) {
            return fromValue(rhsFix.$plus(this));
        } else if (rhsFix.list()) {
            return fromValue(rhsFix.$prepend(this));
        } else {
            if (Double.valueOf(value + rhsFix.toDouble()).isInfinite()) {
                return DollarFactory.infinity(Math.signum(value + rhsFix.toDouble()) > 0);
            }
            return fromValue(value + rhsFix.toDouble());
        }
    }

    @Override
    public int compareTo(@NotNull Value o) {
        return $minus(o).toInteger();
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
    public boolean number() {
        return true;
    }

    @Override
    public double toDouble() {
        return value;
    }

    @Override
    public long toLong() {
        return value.longValue();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Value) {
            Value unwrapped = ((Value) obj).$unwrap();
            if (unwrapped instanceof DollarDecimal) {
                if (integer()) {
                    return value.longValue() == unwrapped.toLong();
                } else {
                    return value == unwrapped.toDouble();
                }
            } else {
                return value.toString().equals(obj.toString());
            }
        } else if (obj == null) {
            return false;
        } else {
            return value.toString().equals(obj.toString());
        }
    }

}
