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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarString extends AbstractDollarSingleValue<String> {


    public DollarString(@NotNull List<Throwable> errors, @NotNull String value) {
        super(errors, value);
    }

    @NotNull
    @Override
    public <R> R $() {
        return (R) value;
    }

    @NotNull
    @Override
    public var $abs() {
        return this;
    }

    @NotNull @Override public var $dec() {
        return DollarStatic.$(String.valueOf((char) (value.charAt(value.length() - 1) - 1)));
    }

    @NotNull
    @Override
    public var $divide(@NotNull var v) {
        final String[] split = value.split(v.S());
        return DollarFactory.fromValue(Arrays.asList(split), errors());

    }

    @NotNull @Override public var $inc() {
        return DollarStatic.$(String.valueOf((char) (value.charAt(value.length() - 1) + 1)));
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_STRING_OPERATION);
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        String newValue = "";
        Long max = v.L();
        for (int i = 0; i < max; i++) {
            newValue = newValue + value;
        }
        return DollarFactory.fromValue(newValue, errors());
    }

    @NotNull
    @Override
    public var $negate() {
        return DollarFactory.fromValue(new StringBuilder(value).reverse().toString(), errors());
    }

    @Override
    public var $as(Type type) {
        switch (type) {
            case BOOLEAN:
                return DollarStatic.$(value.equals("true") || value.equals("yes"));
            case STRING:
                return this;
            case LIST:
                return DollarStatic.$(Arrays.asList(this));
            case MAP:
                return DollarStatic.$("value", this);
            case DECIMAL:
                return DollarStatic.$(Double.parseDouble(value));
            case INTEGER:
                return DollarStatic.$(Long.parseLong(value));
            case VOID:
                return DollarStatic.$void();
            case URI:
                return DollarFactory.fromURI(value);
            default:
                return DollarFactory.failure(DollarFail.FailureType.INVALID_CAST);
        }
    }

    @NotNull @Override
    public Integer I() {
        return Integer.parseInt(value);
    }

    @NotNull
    @Override
    public Number N() {
        return D();
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type == Type.STRING) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    @Override
    public var $dec(@NotNull var amount) {
        return this;
    }

    @NotNull
    @Override
    public var $inc(@NotNull var amount) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_STRING_OPERATION);
    }

    @Override
    public Number number(@NotNull String key) {
        return new BigDecimal(key);
    }

    @NotNull
    @Override
    public var $plus(var newValue) {
        return DollarFactory.fromValue(value + newValue.toString(), errors());
    }

    @Override
    public var $size() {
        return DollarStatic.$(value.length());
    }

    @Override
    public int compareTo(var o) {
        return Comparator.<String>naturalOrder().compare(value, o.$S());
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
        return !value.isEmpty();
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isNeitherTrueNorFalse() {
        return false;
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj != null && value.equals(obj.toString());
    }
}
