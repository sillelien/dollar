/*
 * Copyright (c) 2014-2015 Neil Ellis
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

package com.sillelien.dollar.api.types;

import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.collections.ImmutableList;
import com.sillelien.dollar.api.guard.Guarded;
import com.sillelien.dollar.api.guard.NotNullGuard;
import com.sillelien.dollar.api.json.JsonArray;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.sillelien.dollar.api.types.DollarFactory.failure;
import static com.sillelien.dollar.api.types.ErrorType.*;

public class DollarString extends AbstractDollarSingleValue<String> {


    private static final int MAX_STRING_LENGTH = Integer.MAX_VALUE;

    public DollarString(@NotNull ImmutableList<Throwable> errors, @NotNull String value) {
        super(errors, value);
    }

    @NotNull
    @Override
    public var $abs() {
        return this;
    }

    @NotNull @Override public var $dec() {
        return DollarStatic.$(String.valueOf((char) (value.charAt(value.length() - 1) - 1)));
    }

    @NotNull @Override public var $minus(@NotNull var rhs) {
        return DollarFactory.fromStringValue(value.replace(rhs.toString(), ""), errors(), rhs.errors());
    }

    @NotNull
    @Override
    public var $negate() {
        return DollarFactory.fromValue(new StringBuilder(value).reverse().toString(), errors());
    }

    @NotNull
    @Override
    public var $divide(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.string() && !rhsFix.toString().isEmpty()) {
            try {
//                final Pattern pattern = Pattern.compile(rhsFix.toString(),Pattern.LITERAL);
                final String quote = Pattern.quote(rhsFix.toString());
                final String[] split = value.split(quote);
                if (split.length == 1) {
                    return this;
                }

                return DollarFactory.fromValue(Arrays.asList(split), errors(), rhsFix.errors());
            } catch (PatternSyntaxException pse) {
                return failure(BAD_REGEX, pse, false);
            }
        } else if (rhsFix.number()) {
            if (rhsFix.toDouble() == 0.0) {
                return DollarFactory.infinity(true, errors(), rhsFix.errors());
            }
            if (rhsFix.toDouble() > 0.0 && rhsFix.toDouble() < 1.0) {
                return $multiply(DollarFactory.fromValue(1.0 / rhsFix.toDouble(), rhs.errors()));
            }
            if (rhsFix.toDouble() < 0) {
                return this;
            }
            return DollarFactory.fromStringValue(
                    value.substring(0, (int) ((double) value.length() / rhsFix.toDouble())), errors(),
                    rhsFix.errors());
        } else {
            return this;
        }

    }

    @NotNull @Override public var $inc() {
        return DollarFactory.fromStringValue(String.valueOf((char) (value.charAt(value.length() - 1) + 1)));
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        return failure(INVALID_STRING_OPERATION);
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var rhs) {
        String newValue = "";
        var rhsFix = rhs._fixDeep();
        if (rhsFix.number()) {
            if (rhsFix.toDouble() == 0.0) {
                return DollarFactory.fromStringValue("", errors(), rhsFix.errors());
            }
            if (rhsFix.toDouble() > 0.0 && rhsFix.toDouble() < 1.0) {
                return $divide(DollarFactory.fromValue(1.0 / rhsFix.toDouble(), rhs.errors()));
            }
            if (rhsFix.toInteger() < 0) {
                return this;
            }
        }
        Long max = rhs.toLong();
        if (max * value.length() > MAX_STRING_LENGTH) {
            return failure(STRING_TOO_LARGE,
                           "String multiplication would result in a string with size of " + (max * value.length()),
                           false);
        }
        for (int i = 0; i < max; i++) {
            newValue = newValue + value;
        }
        return DollarFactory.fromValue(newValue, errors());
    }

    @NotNull @Override
    public Integer toInteger() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    @Override
    public var $as(@NotNull Type type) {
        if (type.equals(Type.BOOLEAN)) {
            return DollarStatic.$(value.equals("true") || value.equals("yes"));
        } else if (type.equals(Type.STRING)) {
            return this;
        } else if (type.equals(Type.LIST)) {
            return DollarStatic.$(Arrays.asList(this));
        } else if (type.equals(Type.MAP)) {
            return DollarStatic.$("value", this);
        } else if (type.equals(Type.DECIMAL)) {
            return DollarStatic.$(Double.parseDouble(value));
        } else if (type.equals(Type.INTEGER)) {
            return DollarStatic.$(Long.parseLong(value));
        } else if (type.equals(Type.VOID)) {
            return DollarStatic.$void();
        } else if (type.equals(Type.DATE)) {
            return DollarFactory.fromValue(LocalDateTime.parse(value));
        } else if (type.equals(Type.URI)) {
            return DollarFactory.fromURI(value);
        } else {
            return failure(INVALID_CAST);
        }
    }

    @Override public Type $type() {
        return Type.STRING;
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (Objects.equals(type, Type.STRING)) {
                return true;
            }
        }
        return false;
    }

    @NotNull @Override
    public var $size() {
        return DollarStatic.$(value.length());
    }

    @NotNull
    @Override
    public var $plus(@NotNull var rhs) {
        final ImmutableList<Throwable> thisErrors = errors();
        final ArrayList<Throwable> errors = new ArrayList<>();
        errors.addAll(thisErrors.mutable());
        errors.addAll(rhs.errors().mutable());
        return DollarFactory.wrap(new DollarString(ImmutableList.copyOf(errors), value + rhs.toHumanString()));
    }

    @Override
    public int compareTo(@NotNull var o) {
        return Comparator.<String>naturalOrder().compare(value, o.$S());
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
        return false;
    }

    @Override
    public boolean truthy() {
        return !value.isEmpty();
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    public JsonArray jsonArray() {
        return new JsonArray(value);
    }

    @NotNull @Override public String toDollarScript() {
        return String.format("\"%s\"", org.apache.commons.lang.StringEscapeUtils.escapeJava(value));
    }

    @NotNull
    @Override
    public <R> R toJavaObject() {
        return (R) value;
    }

    @Override
    public boolean string() {
        return true;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass") @Override
    public boolean equals(@Nullable Object obj) {
        return obj != null && value.equals(obj.toString());
    }

    @Nullable @Override
    public Double toDouble() {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    @NotNull
    @Override
    public String toJsonString() {
        return "\""+value+"\"";
    }
}
