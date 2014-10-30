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

import java.math.BigDecimal;
import java.util.List;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarNumber extends AbstractDollarSingleValue<Number> {

    public DollarNumber(@NotNull List<Throwable> errors, @NotNull Number value) {
        super(errors, value);
    }

    @Override
    public var $dec(long amount) {
        return new DollarNumber(errors(), value.longValue() - amount);
    }

    @Override
    public var $inc(long amount) {
        return new DollarNumber(errors(), value.longValue() + amount);
    }

    @NotNull
    @Override
    public Double D() {
        return value.doubleValue();
    }

    @Nullable
    @Override
    public Long L() {
        return value.longValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DollarNumber) {
            return $().equals(((DollarNumber) obj).$());
        } else {
            return value.toString().equals(obj.toString());
        }
    }

    @NotNull
    @Override
    public Number $() {
        return value;
    }

    @Override
    @NotNull
    public Integer I() {
        return value.intValue();
    }

    @Override
    public boolean isDecimal() {
        return value instanceof BigDecimal || value instanceof Float || value instanceof Double;
    }

    @Override
    public boolean isInteger() {
        return value instanceof Long || value instanceof Integer;
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    @NotNull
    public Number number(@NotNull String key) {
        return value;
    }

    @NotNull
    @Override
    public Number N() {
        return value;
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

    @NotNull
    @Override
    public var $plus(Object newValue) {
        if (newValue instanceof var) {
            if (((var) newValue).isInteger()) {
                return DollarFactory.fromValue(errors(), value.longValue() + ((var) newValue).L());
            }
            if (((var) newValue).isDecimal()) {
                return DollarFactory.fromValue(errors(), value.doubleValue() + ((var) newValue).D());
            }
        }
        return super.$plus(newValue);

    }

    @NotNull
    @Override
    public var $minus(Object newValue) {
        if (newValue instanceof var) {
            if (((var) newValue).isInteger()) {
                return DollarFactory.fromValue(errors(), value.longValue() - ((var) newValue).L());
            }
            if (((var) newValue).isDecimal()) {
                return DollarFactory.fromValue(errors(), value.doubleValue() - ((var) newValue).D());
            }
        }
        return super.$plus(newValue);

    }


}
