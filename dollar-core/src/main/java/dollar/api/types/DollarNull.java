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
import dollar.api.DollarStatic;
import dollar.api.Type;
import dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;

public class DollarNull extends AbstractDollar {


    @NotNull
    private final Type type;


    public DollarNull(@NotNull ImmutableList<Throwable> errors, @NotNull Type type) {
        super();
        this.type = type;
    }

    public DollarNull(@NotNull Type type) {
        super();
        this.type = type;
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
        return DollarFactory.newNull(type);

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
        return this;
    }

    @NotNull
    @Override
    public var $set(@NotNull var key, @NotNull Object value) {
        return $copy();
    }

    @NotNull
    @Override
    public var $size() {
        return DollarStatic.$(1);
    }

    @NotNull
    @Override
    public Type $type() {
        return type;
    }

    @Override
    public boolean collection() {
        return false;
    }

    @Override
    public boolean is(@NotNull Type... types) {
        if (Objects.equals(type, Type._ANY)) {
            return true;
        }
        for (Type type : types) {
            if (type.is(this.type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBoolean() {
        return type.is(Type._BOOLEAN);
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean neitherTrueNorFalse() {
        return true;
    }

    @NotNull
    @Override
    public var remove(@NotNull Object value) {
        return this;
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
        return "null";
    }

    @NotNull
    @Override
    public String toHumanString() {
        return "";
    }

    @Override
    public int toInteger() {
        return 0;
    }

    @NotNull
    @Override
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return ImmutableMap.copyOf(Collections.singletonMap((K) "value", (V) null));
    }

    @Nullable
    @Override
    public <R> R toJavaObject() {
        return null;
    }

    @NotNull
    @Override
    public String toJsonString() {
        return "null";
    }

    @NotNull
    @Override
    public ImmutableList<Object> toList() {
        return ImmutableList.of(this);
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
        return ImmutableMap.of(DollarStatic.$("value"), this);
    }

    @NotNull
    @Override
    public String toYaml() {
        return "null: " + type;
    }

    @Override
    public boolean truthy() {
        return false;
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
    public int hashCode() {
        int result = super.hashCode();
        result = (31 * result) + type.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof var) {
            if (((var) other).isNull()) {
                return true;
            }
        }
        return false;

    }
}
