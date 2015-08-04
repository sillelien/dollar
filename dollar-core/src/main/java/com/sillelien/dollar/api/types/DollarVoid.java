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
import com.sillelien.dollar.api.Pipeable;
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
import java.util.Objects;
import java.util.stream.Stream;

public class DollarVoid extends AbstractDollar implements var {


    public static final var INSTANCE = DollarFactory.newVoid();

    public DollarVoid(@NotNull ImmutableList<Throwable> errors) {
        super(errors);
    }

    public DollarVoid() {

        super(ImmutableList.of());
    }

    @NotNull
    @Override
    public var $abs() {
        return this;
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
        return this;
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
        return this;
    }

    @Override public int sign() {
        return 0;
    }

    @NotNull @Override
    public Integer toInteger() {
        return 0;
    }

    @NotNull
    @Override
    public Number toNumber() {
        return 0;
    }

    @Override
    public var $as(@NotNull Type type) {
        if (type.equals(Type.BOOLEAN)) {
            return DollarStatic.$(false);
        } else if (type.equals(Type.STRING)) {
            return DollarStatic.$("");
        } else if (type.equals(Type.LIST)) {
            return DollarStatic.$(Arrays.asList());
        } else if (type.equals(Type.MAP)) {
            return DollarStatic.$("value", this);
        } else if (type.equals(Type.DECIMAL)) {
            return DollarStatic.$(0.0d);
        } else if (type.equals(Type.INTEGER)) {
            return DollarStatic.$(0);
        } else if (type.equals(Type.VOID)) {
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
        return ImmutableList.of();
    }

    @Override public Type $type() {
        return Type.VOID;
    }

    @Override public boolean collection() {
        return false;
    }

    @NotNull
    @Override
    public ImmutableMap<var, var> $map() {
        return ImmutableMap.of();
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (Objects.equals(type, Type.VOID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isVoid() {
        return true;
    }

    @Override
    public ImmutableList<String> strings() {
        return ImmutableList.of();
    }

    @NotNull @Override public ImmutableList<Object> toList() {
        return ImmutableList.of();
    }

    @NotNull @Override
    public Map<String, Object> toMap() {
        return Collections.emptyMap();
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
        return DollarStatic.$(0);
    }

    @NotNull @Override public var $prepend(@NotNull var value) {
        return this;
    }

    @NotNull
    @Override
    public var $removeByKey(@NotNull String value) {
        return _copy();
    }

    @NotNull
    @Override
    public var $set(@NotNull var key, Object value) {
        return _copy();
    }

    @NotNull
    @Override
    public var remove(Object value) {
        return _copy();
    }

    @NotNull @Override public var $remove(var value) {
        return this;
    }

    @NotNull
    @Override
    public Stream<var> $stream(boolean parallel) {
        return Stream.empty();
    }

    @NotNull
    @Override
    public var $eval(@NotNull String js) {
        return _copy();
    }

    @NotNull
    @Override
    public var $pipe(@NotNull String js, @NotNull String label) {
        return _copy();
    }

    @NotNull
    @Override
    public var $pipe(@NotNull Class<? extends Pipeable> clazz) {
        return _copy();
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return (obj instanceof var && ((var) obj).toJavaObject() == null) || obj == null;
    }

    @Override
    public int compareTo(@NotNull var o) {
        if (o.isVoid()) {
            return 0;
        } else {
            return 1;
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
        return false;
    }

    @NotNull
    @Override
    public String toHumanString() {
        return "";
    }

    @NotNull @Override public String toDollarScript() {
        return "void";
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

    /**
     * If you stare into the void, the void will stare back at you.
     *
     * @param you - you.
     */
    @SuppressWarnings("InfiniteRecursion")
    void stare(var you) {
        this.stare(you);
    }
}
