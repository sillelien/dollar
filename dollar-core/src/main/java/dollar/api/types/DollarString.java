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
import dollar.api.guard.Guarded;
import dollar.api.guard.NotNullGuard;
import dollar.api.json.JsonArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static dollar.api.types.DollarFactory.failure;
import static dollar.api.types.ErrorType.*;

public class DollarString extends AbstractDollarSingleValue<String> {


    private static final int MAX_STRING_LENGTH = Integer.MAX_VALUE;

    public DollarString(@NotNull String value) {
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
            return DollarStatic.$("true".equals(value) || "yes".equals(value));
        } else if (type.is(Type._STRING)) {
            return this;
        } else if (type.is(Type._LIST)) {
            return DollarStatic.$(Collections.singletonList(this));
        } else if (type.is(Type._MAP)) {
            return DollarStatic.$("value", this);
        } else if (type.is(Type._DECIMAL)) {
            return DollarStatic.$(Double.parseDouble(value));
        } else if (type.is(Type._INTEGER)) {
            return DollarStatic.$(Long.parseLong(value));
        } else if (type.is(Type._VOID)) {
            return DollarStatic.$void();
        } else if (type.is(Type._DATE)) {
            return DollarFactory.fromValue(LocalDateTime.parse(value));
        } else if (type.is(Type._URI)) {
            return DollarFactory.fromURI(value);
        } else {
            throw new DollarFailureException(INVALID_CAST);
        }
    }

    @NotNull
    @Override
    public Value $dec() {
        return DollarStatic.$(String.valueOf((char) (value.charAt(value.length() - 1) - 1)));
    }

    @NotNull
    @Override
    public Value $divide(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();
        if (rhsFix.string() && !rhsFix.toString().isEmpty()) {
            try {
//                final Pattern pattern = Pattern.compile(rhsFix.toString(),Pattern.LITERAL);
                final String quote = Pattern.quote(rhsFix.toString());
                final String[] split = value.split(quote);
                if (split.length == 1) {
                    return this;
                }

                return DollarFactory.fromValue(Arrays.asList(split));
            } catch (PatternSyntaxException pse) {
                return failure(BAD_REGEX, pse, false);
            }
        } else if (rhsFix.number()) {
            if (rhsFix.toDouble() == 0.0) {
                return DollarFactory.infinity(true);
            }
            if ((rhsFix.toDouble() > 0.0) && (rhsFix.toDouble() < 1.0)) {
                return $multiply(DollarFactory.fromValue(1.0 / rhsFix.toDouble()));
            }
            if (rhsFix.toDouble() < 0) {
                return this;
            }
            return DollarFactory.fromStringValue(
                    value.substring(0, (int) ((double) value.length() / rhsFix.toDouble())));
        } else {
            return this;
        }

    }

    @NotNull
    @Override
    public Value $inc() {
        return DollarFactory.fromStringValue(String.valueOf((char) (value.charAt(value.length() - 1) + 1)));
    }

    @NotNull
    @Override
    public Value $minus(@NotNull Value rhs) {
        return DollarFactory.fromStringValue(value.replace(rhs.toString(), ""));
    }

    @NotNull
    @Override
    public Value $modulus(@NotNull Value rhs) {
        throw new DollarFailureException(INVALID_STRING_OPERATION);
    }

    @NotNull
    @Override
    public Value $multiply(@NotNull Value rhs) {
        StringBuilder newValue = new StringBuilder();
        Value rhsFix = rhs.$fixDeep();
        if (rhsFix.number()) {
            if (rhsFix.toDouble() == 0.0) {
                return DollarFactory.fromStringValue("");
            }
            if ((rhsFix.toDouble() > 0.0) && (rhsFix.toDouble() < 1.0)) {
                return $divide(DollarFactory.fromValue(1.0 / rhsFix.toDouble()));
            }
            if (rhsFix.toInteger() < 0) {
                return this;
            }
        }
        Long max = rhs.toLong();
        if ((max * value.length()) > MAX_STRING_LENGTH) {
            return failure(STRING_TOO_LARGE,
                           "String multiplication would result in a string with size of " + (max * value.length()),
                           false);
        }
        for (int i = 0; i < max; i++) {
            newValue.append(value);
        }
        return DollarFactory.fromValue(newValue.toString());
    }

    @NotNull
    @Override
    public Value $negate() {
        return DollarFactory.fromValue(new StringBuilder(value).reverse().toString());
    }

    @NotNull
    @Override
    public Type $type() {
        return new Type(Type._STRING, constraintLabel());
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type.is(Type._STRING)) {
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
    @NotNull
    @Guarded(NotNullGuard.class)
    public JsonArray jsonArray() {
        return new JsonArray(value);
    }

    @Override
    public boolean neitherTrueNorFalse() {
        return false;
    }

    @NotNull
    @Override
    public String toDollarScript() {
        return String.format("\"%s\"", org.apache.commons.lang.StringEscapeUtils.escapeJava(value));
    }

    @NotNull
    @Override
    public int toInteger() {
        return Integer.parseInt(value);
    }

    @NotNull
    @Override
    public <R> R toJavaObject() {
        return (R) value;
    }

    @NotNull
    @Override
    public String toYaml() {
        Yaml yaml = new Yaml();
        return "string: " + yaml.dump(value);
    }

    @Override
    public boolean truthy() {
        return !value.isEmpty();
    }

    @NotNull
    @Override
    public Value $plus(@NotNull Value rhs) {
        return DollarFactory.wrap(new DollarString(value + rhs.$S()));
    }

    @NotNull
    @Override
    public Value $size() {
        return DollarStatic.$(value.length());
    }

    @NotNull
    @Override
    public String toJsonString() {
        return "\"" + value + "\"";
    }

    @Override
    public int compareTo(@NotNull Value o) {
        return Comparator.<String>naturalOrder().compare(value, o.$S());
    }

    @Override
    public boolean string() {
        return true;
    }

    @Nullable
    @Override
    public double toDouble() {
        return Double.parseDouble(value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return (obj != null) && value.equals(obj.toString());
    }
}
