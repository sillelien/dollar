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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import dollar.api.DollarStatic;
import dollar.api.Type;
import dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.stream.Stream;

public class DollarVoid extends AbstractDollar {


    @NotNull
    public static final var INSTANCE = DollarFactory.newVoid();

    public DollarVoid() {
        super();
    }


    @NotNull
    @Override
    public var $abs() {
        return this;
    }

    @NotNull
    @Override
    public var $append(@NotNull var value) {
        return this;
    }

    @NotNull
    @Override
    public var $as(@NotNull Type type) {
        if (type.is(Type._BOOLEAN)) {
            return DollarStatic.$(false);
        } else if (type.is(Type._STRING)) {
            return DollarStatic.$("");
        } else if (type.is(Type._LIST)) {
            return DollarStatic.$(Collections.emptyList());
        } else if (type.is(Type._MAP)) {
            return DollarStatic.$("value", this);
        } else if (type.is(Type._DECIMAL)) {
            return DollarStatic.$(0.0d);
        } else if (type.is(Type._INTEGER)) {
            return DollarStatic.$(0);
        } else if (type.is(Type._VOID)) {
            return this;
        } else if (type.is(Type._RANGE)) {
            return DollarFactory.fromValue(Range.closed($get(DollarStatic.$(0)), $get(DollarStatic.$(0))));
        } else {
            return DollarFactory.failure(ErrorType.INVALID_CAST, type.toString(), false);
        }
    }

    @NotNull
    @Override
    public var $containsKey(@NotNull var value) {
        return DollarStatic.$(false);
    }

    @NotNull
    @Override
    public var $containsValue(@NotNull var value) {
        return DollarStatic.$(false);
    }

    @NotNull
    @Override
    public var $divide(@NotNull var rhs) {
        return this;
    }

    @NotNull
    @Override
    public var $get(@NotNull var key) {
        return this;
    }

    @NotNull
    @Override
    public var $has(@NotNull var key) {
        return DollarStatic.$(false);
    }

    @NotNull
    @Override
    public var $insert(@NotNull var value, int position) {
        return this;
    }

    @NotNull
    @Override
    public var $minus(@NotNull var rhs) {
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

    @NotNull
    @Override
    public var $negate() {
        return this;
    }

    @NotNull
    @Override
    public var $plus(@NotNull var rhs) {
        return this;
    }

    @NotNull
    @Override
    public var $prepend(@NotNull var value) {
        return this;
    }

    @NotNull
    @Override
    public var $remove(@NotNull var value) {
        return this;
    }

    @NotNull
    @Override
    public var $removeByKey(@NotNull String value) {
        return $copy();
    }

    @NotNull
    @Override
    public var $set(@NotNull var key, @NotNull Object value) {
        return $copy();
    }

    @NotNull
    @Override
    public var $size() {
        return DollarStatic.$(0);
    }

    @NotNull
    @Override
    public Type $type() {
        return Type._VOID;
    }

    @Override
    public boolean collection() {
        return false;
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type.is(Type._VOID)) {
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
    public boolean isVoid() {
        return true;
    }

    @Override
    public boolean neitherTrueNorFalse() {
        return true;
    }

    @NotNull
    @Override
    public var remove(@NotNull Object value) {
        return $copy();
    }

    @Override
    public int sign() {
        return 0;
    }

    @NotNull
    @Override
    public int size() {
        return 0;
    }

    @NotNull
    @Override
    public String toDollarScript() {
        return "void";
    }

    @NotNull
    @Override
    public String toHumanString() {
        return "";
    }

    @NotNull
    @Override
    public Integer toInteger() {
        return 0;
    }

    @NotNull
    @Override
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return ImmutableMap.of();
    }

    @Nullable
    @Override
    public <R> R toJavaObject() {
        return null;
    }

    @NotNull
    @Override
    public <T> ImmutableList<T> toList() {
        return ImmutableList.of();
    }

    @NotNull
    @Override
    public Number toNumber() {
        return 0;
    }

    @Override
    public ImmutableList<String> toStrings() {
        return ImmutableList.of();
    }

    @NotNull
    @Override
    public ImmutableList<var> toVarList() {
        return ImmutableList.of();
    }

    @NotNull
    @Override
    public ImmutableMap<var, var> toVarMap() {
        return ImmutableMap.of();
    }

    @NotNull
    @Override
    public String toYaml() {
        return "void: void";
    }

    @Override
    public boolean truthy() {
        return false;
    }

    @NotNull
    @Override
    public Stream<var> $stream(boolean parallel) {
        return Stream.empty();
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof var && ((var) obj).isVoid()) {
            return true;
        }
        return ((obj instanceof var) && (((var) obj).toJavaObject() == null)) || (obj == null);
    }

    @Override
    public int compareTo(@NotNull var o) {
        if (o.isVoid()) {
            return 0;
        } else {
            return 1;
        }
    }
}
