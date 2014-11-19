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
import java.util.List;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarBoolean extends AbstractDollarSingleValue<Boolean> {

    public DollarBoolean(@NotNull List<Throwable> errors, @NotNull Boolean value) {
        super(errors, value);
    }

    @Override
    public var $as(Type type) {
        switch (type) {
            case BOOLEAN:
                return this;
            case STRING:
                return DollarStatic.$(S());
            case LIST:
                return DollarStatic.$(Arrays.asList(this));
            case MAP:
                return DollarStatic.$("value", this);
            case NUMBER:
                return DollarStatic.$(value ? 1 : 0);
            case VOID:
                return DollarStatic.$void();
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    @NotNull
    public Integer I() {
        return value ? 1 : 0;
    }

    @NotNull
    @Override
    public Number N() {
        return value ? 1 : 0;
    }

    @Override
    public boolean is(Type... types) {
        for (Type type : types) {
            if (type == Type.BOOLEAN) {
                return true;
            }
        }
        return false;
    }

    @Override
    @NotNull
    public Number number(@NotNull String key) {
        return 0;
    }

    @NotNull
    @Override
    public var $dec(@NotNull var amount) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_BOOLEAN_VALUE_OPERATION);
    }

    @NotNull
    @Override
    public var $inc(@NotNull var amount) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_BOOLEAN_VALUE_OPERATION);
    }

    @NotNull
    @Override
    public var $negate() {
        return DollarFactory.fromValue(errors(), !value);
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_BOOLEAN_VALUE_OPERATION);
    }

    @NotNull
    @Override
    public var $divide(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_BOOLEAN_VALUE_OPERATION);
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_BOOLEAN_VALUE_OPERATION);
    }

    @NotNull
    @Override
    public var $abs() {
        return this;
    }

    @NotNull
    @Override
    public Double D() {
        return value ? 1.0 : 0.0;
    }

    @Nullable
    @Override
    public Long L() {
        return value ? 1L : 0L;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DollarBoolean) {
            return $().equals(((DollarBoolean) obj).$());
        } else {
            return value.toString().equals(obj.toString());
        }
    }

    @NotNull
    @Override
    public Boolean $() {
        return value;
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
        return false;
    }

    @Override
    public int compareTo(var o) {
        return I() - o.I();
    }

    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public boolean isTrue() {
        return value;
    }

    @Override
    public boolean isTruthy() {
        return value;
    }

    @Override
    public boolean isFalse() {
        return !value;
    }

    @Override
    public boolean isNeitherTrueNorFalse() {
        return false;
    }
}
