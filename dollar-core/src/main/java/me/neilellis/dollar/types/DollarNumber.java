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

import me.neilellis.dollar.AbstractDollarSingleValue;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarNumber extends AbstractDollarSingleValue<Number> {

    public DollarNumber(@NotNull List<Throwable> errors, @NotNull Number value) {
        super(errors,value);
    }

    @NotNull
    @Override
    public var $copy() {
        return new DollarNumber(errors(), value);
    }

    @Override
    @NotNull
    public Integer I() {
        return value.intValue();
    }

    @Override
    @NotNull
    public Number number(@NotNull String key) {
        return value;
    }

    @NotNull
    @Override
    public Number $() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DollarNumber) {
            return $().equals(((DollarNumber) obj).$());
        } else {
            return value.toString().equals(obj.toString());
        }
    }

    @Nullable
    @Override
    public Double D() {
        return value.doubleValue();
    }

    @Nullable
    @Override
    public Long L() {
        return value.longValue();
    }


}
