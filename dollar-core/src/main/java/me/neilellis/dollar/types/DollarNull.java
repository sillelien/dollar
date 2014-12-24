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
import me.neilellis.dollar.collections.ImmutableList;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * To better understand the rationale behind this class, take a look at http://homepages.ecs.vuw.ac
 * .nz/~tk/publications/papers/void.pdf
 *
 * Dollar does not have the concept of null. Instead null {@link me.neilellis.dollar.var} objects are instances of this
 * class.
 *
 * Void is equivalent to 0,"",null except that unlike these values it has behavior that corresponds to a void object.
 *
 * Therefore actions taken against a void object are ignored. Any method that returns a {@link me.neilellis.dollar.var}
 * will return a {@link me.neilellis.dollar.types.DollarNull}.
 *
 * <pre>
 *
 *  var nulled= $null();
 *  nulled.$pipe((i)-&gt;{System.out.println("You'll never see this."});
 *
 * </pre>
 *
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarNull extends AbstractDollar implements var {


    private Type type;


    public DollarNull(@NotNull ImmutableList<Throwable> errors, Type type) {
        super(errors);
        this.type = type;
    }

    public DollarNull(Type type) {
        super(ImmutableList.of());
        this.type = type;
    }

    @NotNull
    @Override
    public var $abs() {
        return this;
    }

    @NotNull
    @Override
    public var $minus(@NotNull var v) {
        return this;
    }

    @NotNull
    @Override
    public var $plus(var v) {
        return this;
    }

    @NotNull
    @Override
    public var $negate() {
        return this;
    }

    @NotNull
    @Override
    public var $divide(@NotNull var v) {
        return this;
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var v) {
        return this;
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return this;
    }

    @NotNull @Override
    public Integer I() {
        return 0;
    }

    @NotNull
    @Override
    public Number N() {
        return 0;
    }

    @Override public int sign() {
        return 0;
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
        return this;
    }

    @NotNull
    @Override
    public var $set(@NotNull var key, Object value) {
        return _copy();
    }

    @NotNull
    @Override
    public var remove(Object value) {
        return this;
    }

    @NotNull @Override public var $remove(var value) {
        return this;
    }

    @NotNull
    @Override
    public String S() {
        return "";
    }

    @Override
    public var $as(Type type) {
        return DollarFactory.newNull(type);

    }

    @NotNull
    @Override
    public ImmutableList<var> $list() {
        return ImmutableList.of();
    }

    @Override public Type $type() {
        return type;
    }

    @NotNull
    @Override
    public ImmutableMap<var, var> $map() {
        return ImmutableMap.of(DollarStatic.$("value"), this);
    }

    @Override
    public boolean is(@NotNull Type... types) {
        if (Objects.equals(type, Type.ANY)) {
            return true;
        }
        for (Type type : types) {
            if (Objects.equals(type, this.type)) {
                return true;
            }
        }
        return false;
    }

    @Override public boolean isCollection() {
        return false;
    }

    @Override public boolean isNull() {
        return true;
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public ImmutableList<String> strings() {
        return ImmutableList.of(null);
    }

    @NotNull @Override public ImmutableList<Object> toList() {
        return ImmutableList.of(this);
    }

    @NotNull @Override
    public Map<String, Object> toMap() {
        return Collections.singletonMap("value", null);
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
        result = 31 * result + type.hashCode();
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

    @Override
    public boolean isBoolean() {
        return Objects.equals(type, Type.BOOLEAN);
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isNeitherTrueNorFalse() {
        return true;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean isTruthy() {
        return false;
    }

    @NotNull @Override public String toDollarScript() {
        return "null";
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
