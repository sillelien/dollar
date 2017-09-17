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

public class DollarBoolean extends AbstractDollarSingleValue<Boolean> {

    public DollarBoolean(@NotNull Boolean value) {
        super(value);
    }

    @NotNull
    @Override
    public Value $abs() {
        return this;
    }

    @NotNull
    @Override
    public Value $as(@NotNull Type type) {
        if (type.is(Type._BOOLEAN)) {
            return this;
        } else if (type.is(Type._STRING)) {
            return DollarStatic.$(toHumanString());
        } else if (type.is(Type._LIST)) {
            return DollarStatic.$(Collections.singletonList(this));
        } else if (type.is(Type._MAP)) {
            return DollarStatic.$("value", this);
        } else if (type.is(Type._DECIMAL)) {
            return DollarStatic.$(value ? 1.0 : 0.0);
        } else if (type.is(Type._INTEGER)) {
            return DollarStatic.$(value ? 1 : 0);
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
        return DollarFactory.fromValue(value == rhsFix.truthy());
    }

    @NotNull
    @Override
    public Value $minus(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();
        return DollarFactory.fromValue(value ^ rhsFix.truthy());
    }

    @NotNull
    @Override
    public Value $modulus(@NotNull Value rhs) {
        throw new DollarFailureException(ErrorType.INVALID_BOOLEAN_VALUE_OPERATION);
    }

    @NotNull
    @Override
    public Value $multiply(@NotNull Value v) {
        throw new DollarFailureException(ErrorType.INVALID_BOOLEAN_VALUE_OPERATION);
    }

    @NotNull
    @Override
    public Value $negate() {
        return DollarFactory.fromValue(!value);
    }

    @NotNull
    @Override
    public Type $type() {
        return new Type(Type._BOOLEAN, constraintLabel());
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type.is(Type._BOOLEAN)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public boolean isFalse() {
        return !value;
    }

    @Override
    public boolean isTrue() {
        return value;
    }

    @Override
    public boolean neitherTrueNorFalse() {
        return false;
    }

    @Override
    public int sign() {
        return toInteger();
    }

    @NotNull
    @Override
    public String toDollarScript() {
        return toString();
    }

    @Override
    @NotNull
    public int toInteger() {
        return value ? 1 : 0;
    }

    @NotNull
    @Override
    public Boolean toJavaObject() {
        return value;
    }

    @NotNull
    @Override
    public Number toNumber() {
        return value ? 1 : 0;
    }

    @NotNull
    @Override
    public String toYaml() {
        return "boolean: " + value;
    }

    @Override
    public boolean truthy() {
        return value;
    }

    @NotNull
    @Override
    public Value $plus(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();
        if (rhsFix.list()) {
            rhsFix.$prepend(this);
        } else if (rhsFix.list()) {
            return DollarFactory.fromValue(rhsFix.$prepend(this));
        } else if (rhsFix.range()) {
            return DollarFactory.fromValue(rhsFix.$plus(this));
        } else if (rhsFix.string()) {
            return DollarFactory.fromValue(value + rhsFix.toHumanString());
        }
        return DollarFactory.fromValue(value || rhsFix.truthy());
    }

    @Override
    public int compareTo(@NotNull Value o) {
        return toInteger() - o.toInteger();
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
        return false;
    }

    @Override
    public double toDouble() {
        return value ? 1.0 : 0.0;
    }

    @Override
    public long toLong() {
        return value ? 1L : 0L;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DollarBoolean) {
            return toJavaObject().equals(((DollarBoolean) obj).toJavaObject());
        } else {
            return value.toString().equals(obj.toString());
        }
    }
}
