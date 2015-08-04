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
import com.sillelien.dollar.api.collections.ImmutableMap;
import com.sillelien.dollar.api.collections.Range;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.sillelien.dollar.api.DollarStatic.$void;

public final class DollarInfinity extends AbstractDollar implements var {


    public static final var INSTANCE = DollarFactory.INFINITY;
    private final boolean positive;

    public DollarInfinity(@NotNull ImmutableList<Throwable> errors, boolean positive) {
        super(errors);
        this.positive = positive;
    }

    public DollarInfinity(boolean positive) {

        super(ImmutableList.of());
        this.positive = positive;
    }

    @NotNull
    @Override
    public var $abs() {
        return DollarFactory.wrap(new DollarInfinity(errors(), true));
    }

    @NotNull
    @Override
    public var $minus(@NotNull var rhs) {
        return this;
    }

    @NotNull
    @Override
    public var $plus(@NotNull var rhs) {
        return this;
    }

    @NotNull
    @Override
    public var $negate() {
        return DollarFactory.wrap(new DollarInfinity(errors(), !positive));
    }

    @NotNull
    @Override
    public var $divide(@NotNull var rhs) {
        return this;
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        return this;
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        if (v.isVoid()) {
            return $void();
        }
        if (v.$isEmpty().isTrue()) {
            return DollarFactory.fromValue(0, errors());
        }
        boolean
                positiveResult =
                positive && v.positive() || !(positive && negative()) && !(!positive && v.positive());
        return DollarFactory.wrap(new DollarInfinity(errors(), positiveResult));
    }

    @Override public int sign() {
        return positive ? 1 : -1;
    }

    @NotNull @Override
    public Integer toInteger() {
        return positive ? Integer.MAX_VALUE : Integer.MIN_VALUE;
    }

    @NotNull
    @Override
    public Number toNumber() {
        return positive ? Double.MAX_VALUE : Double.MIN_VALUE;
    }

    @Override
    public var $as(@NotNull Type type) {
        if (type.equals(Type.BOOLEAN)) {
            return DollarStatic.$(true);
        } else if (type.equals(Type.STRING)) {
            return DollarStatic.$(positive ? "infinity" : "-infinity");
        } else if (type.equals(Type.LIST)) {
            return DollarStatic.$(Arrays.asList(this));
        } else if (type.equals(Type.MAP)) {
            return DollarStatic.$("value", this);
        } else if (type.equals(Type.DECIMAL)) {
            return DollarStatic.$(positive ? Double.MAX_VALUE : Double.MIN_VALUE);
        } else if (type.equals(Type.INTEGER)) {
            return DollarStatic.$(positive ? Long.MAX_VALUE : Long.MIN_VALUE);
        } else if (type.equals(Type.VOID)) {
            return $void();
        } else if (type.equals(Type.DATE)) {
            return this;
        } else if (type.equals(Type.RANGE)) {
            return DollarFactory.fromValue(new Range($(0), $(0)));
        } else {
            return DollarFactory.failure(ErrorType.INVALID_CAST, type.toString(), false);
        }
    }

    @NotNull
    @Override
    public ImmutableList<var> $list() {
        return ImmutableList.of(this);
    }

    @Override public Type $type() {
        return Type.INFINITY;
    }

    @Override public boolean collection() {
        return false;
    }

    @NotNull
    @Override
    public ImmutableMap<var, var> $map() {
        return ImmutableMap.of(DollarStatic.$("value"), this);
    }

    @Override
    public boolean infinite() {
        return false;
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type == Type.INFINITY || type == Type.INTEGER || type == Type.DECIMAL) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ImmutableList<String> strings() {
        return ImmutableList.of();
    }

    @NotNull @Override public ImmutableList<Object> toList() {
        return ImmutableList.of(toNumber());
    }

    @NotNull @Override
    public Map<String, Object> toMap() {
        return Collections.singletonMap("value", toNumber());
    }

    @NotNull
    @Override
    public var $get(@NotNull var key) {
        return this;
    }

    @NotNull @Override public var $append(@NotNull var value) {
        return this;
    }

    @NotNull @Override
    public var $containsValue(@NotNull var value) {
        return DollarStatic.$(false);
    }

    @NotNull @Override
    public var $has(@NotNull var key) {
        return DollarStatic.$(false);
    }

    @NotNull @Override
    public var $size() {
        return this;
    }

    @NotNull @Override public var $prepend(@NotNull var value) {
        return this;
    }

    @NotNull
    @Override
    public var $removeByKey(@NotNull String value) {
        return this;
    }

    @NotNull
    @Override
    public var $set(@NotNull var key, Object value) {
        return this;
    }

    @NotNull
    @Override
    public var remove(Object value) {
        return this;
    }

    @NotNull @Override public var $remove(var value) {
        return this;
    }

    @Override
    public int compareTo(@NotNull var o) {
        if (o.infinite()) {
            return new Integer(sign()).compareTo(o.sign());
        } else {
            if (positive) {
                return 1;
            } else {
                return -1;
            }
        }

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof var) {
            var o = ((var) other)._fixDeep()._unwrap();
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            DollarInfinity that = (DollarInfinity) o;

            return positive == that.positive;
        } else {
            return false;
        }

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
    public boolean truthy() {
        return true;
    }

    @NotNull
    @Override
    public String toHumanString() {
        return positive ? "infinity" : "-infinity";
    }

    @NotNull @Override public String toDollarScript() {
        return positive ? "infinity" : "-infinity";
    }

    @Nullable
    @Override
    public <R> R toJavaObject() {
        return null;
    }

    @NotNull
    @Override
    public JSONObject toOrgJson() {
        return new JSONObject();
    }

}
