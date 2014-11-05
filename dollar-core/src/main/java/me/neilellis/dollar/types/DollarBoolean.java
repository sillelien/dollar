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

import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarBoolean extends AbstractDollarSingleValue<Boolean> {

    public DollarBoolean(@NotNull List<Throwable> errors, @NotNull Boolean value) {
        super(errors, value);
    }

    @Override
    public var $dec(long amount) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_BOOLEAN_VALUE_OPERATION);
    }

    @Override
    public var $inc(long amount) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_BOOLEAN_VALUE_OPERATION);
    }

    @Override
    public var $negate() {
        return DollarFactory.fromValue(errors(), !value);
    }

    @Override
    public var $multiply(var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_BOOLEAN_VALUE_OPERATION);
    }

    @Override
    public var $divide(var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_BOOLEAN_VALUE_OPERATION);
    }

    @Override
    public var $modulus(var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_BOOLEAN_VALUE_OPERATION);
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
    @NotNull
    public Integer I() {
        return value ? 1 : 0;
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
    @NotNull
    public Number number(@NotNull String key) {
        return 0;
    }

    @NotNull
    @Override
    public Number N() {
        return value ? 1 : 0;
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
