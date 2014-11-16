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

import com.google.common.collect.ImmutableList;
import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.exceptions.ListException;
import me.neilellis.dollar.json.JsonArray;
import me.neilellis.dollar.json.JsonObject;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarList extends AbstractDollar {

    private final ImmutableList<var> list;

    DollarList(@NotNull ImmutableList<Throwable> errors, @NotNull JsonArray array) {
        this(errors, array.toList());
    }


    DollarList(@NotNull ImmutableList<Throwable> errors, @NotNull List<Object> list) {
        super(errors);
        List<var> l = new ArrayList<>();
        for (Object value : list) {
            if ((value instanceof var)) {
                if (((var) value).isLambda() || !((var) value).isVoid()) {
                    l.add((var) value);
                }
            } else if (value == null) {
                //Skip
            } else {
                l.add(DollarFactory.fromValue(errors, value));
            }
        }
        this.list = ImmutableList.copyOf(l);
    }

    DollarList(@NotNull ImmutableList<Throwable> errors, @NotNull Object[] values) {
        super(errors);
        List<var> l = new ArrayList<>();
        for (Object value : values) {
            if ((value instanceof var)) {
                if (((var) value).isLambda() || !((var) value).isVoid()) {
                    l.add((var) value);
                }
            } else if (value == null) {
                //Skip
            } else {
                l.add(DollarFactory.fromValue(errors, value));
            }
        }
        this.list = ImmutableList.copyOf(l);
    }

    @NotNull
    @Override
    public var $plus(Object value) {
        if (value == this) {
            throw new IllegalArgumentException("Tried to add to self");
        }
        return DollarFactory.fromValue(errors(),
                ImmutableList.builder()
                        .addAll(list)
                        .add(DollarFactory.fromValue(value))
                        .build());
    }

    @NotNull
    @Override
    public Stream<var> $children() {
        return list.stream();
    }

    @NotNull
    @Override
    public Stream<var> $children(@NotNull String key) {
        return Stream.empty();
    }

    @NotNull
    @Override
    public var $dec(@NotNull var amount) {
        return this;
    }

    @NotNull
    @Override
    public var $inc(@NotNull var amount) {
        return this;
    }

    @NotNull
    @Override
    public var $negate() {
        ArrayList<var> result = new ArrayList<>(list);
        Collections.reverse(result);
        return DollarFactory.fromValue(errors(), result);
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @NotNull
    @Override
    public var $divide(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @NotNull
    @Override
    public var $abs() {
        return this;
    }

    @Override
    public var $has(@NotNull String key) {
        return DollarStatic.$(false);
    }

    @NotNull
    @Override
    public ImmutableList<var> toList() {
        return list;
    }

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {
        return null;
    }

    @Nullable
    @Override
    public String S(@NotNull String key) {
        return null;
    }

    @NotNull
    @Override
    public var $rm(@NotNull String value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @NotNull
    @Override
    public var $minus(@NotNull Object value) {
        ArrayList<var> newVal = new ArrayList<>(list);
        newVal.remove(value);
        return DollarFactory.fromValue(errors(), newVal);
    }

    @NotNull
    @Override
    public var $(@NotNull var key, Object value) {
        ArrayList<var> newVal = new ArrayList<>(list);
        if (key.isInteger()) {
            newVal.set(key.I(), DollarFactory.fromValue(value));
        } else {
            return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
        }
        return DollarFactory.fromValue(errors(), newVal);
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public Integer I() {
        return $stream().collect(Collectors.summingInt((i) -> i.I()));
    }

    @Override
    public Integer I(@NotNull String key) {
        throw new ListException();
    }

    @NotNull
    @Override
    public var decode() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public var $get(@NotNull String key) {
        for (var var : list) {
            if (var.$S().equals(key)) {
                return var;
            }
        }
        return DollarStatic.$void();
    }

    @Override
    public boolean isList() {
        return true;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public JsonObject json(@NotNull String key) {
        throw new ListException();
    }

    @NotNull
    @Override
    public <R> R $() {
        return (R) jsonArray();
    }

    @Override
    public Stream<String> keyStream() {
        List<String> strings = strings();
        if (strings == null) {
            return Stream.empty();
        }
        return strings.stream();
    }

    @Override
    public Number number(@NotNull String key) {
        throw new ListException();
    }

    @NotNull
    @Override
    public JSONObject orgjson() {
        return new JSONObject(json().toMap());
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public JsonObject json() {
        JsonArray array = jsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.putArray("value", array);
        return jsonObject;
    }

    @Override
    public ImmutableList<String> strings() {
        return ImmutableList.<String>copyOf(list.stream().map(Object::toString).collect(Collectors.toList()));
    }

    @Override
    public Map<String, Object> toMap() {
        return Collections.singletonMap("value", new ArrayList<>(toList()));
    }

    @NotNull
    @Override
    public Number N() {
        return 0;
    }

    @NotNull
    @Override
    public var $(@NotNull Number n) {
        return list.get(n.intValue());
    }

    @NotNull
    @Override
    public Stream<Map.Entry<String, var>> kvStream() {
        return null;
    }

    @NotNull
    @Override
    public Stream<var> $stream() {
        return list.stream();
    }

    @NotNull
    @Override
    public var $copy() {
        return DollarFactory.fromValue(errors(), list.stream().map(var::$copy).collect(Collectors.toList()));
    }

    @Override
    public var $size() {
        return DollarStatic.$(list.size());
    }

    @Override
    public var $containsValue(Object value) {
        return DollarStatic.$(list.contains(value));
    }

    @NotNull
    @Override
    public var remove(Object value) {
        List<var> newList = new ArrayList<>();
        for (var val : list) {
            if (!val.equals(value)) {
                newList.add(val);
            }
        }
        return DollarFactory.fromValue(errors(), newList);
    }

    @NotNull
    @Override
    public String S() {
        return jsonArray().toString();
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
        return !list.isEmpty();
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
    public var $notify(var value) {
        for (var v : list) {
            v.$notify(value);
        }
        return this;
    }

    @Override
    public String $listen(Pipeable pipe, String key) {
        for (var v : list) {
            //Join the children to this, so if the children change
            //listeners to this get the latest value of this.
            v.$listen(i -> this, key);
        }
        return key;
    }

    @Override
    public String $listen(Pipeable pipe) {
        String key = UUID.randomUUID().toString();
        $listen(pipe, key);
        return key;
    }

    @Override
    public int compareTo(var o) {
        //TODO: improve comparisons
        if (list.stream().allMatch(v -> v.compareTo(o) == -1)) {
            return -1;
        }
        if (list.stream().allMatch(v -> v.compareTo(o) == 1)) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean is(Type... types) {
        for (Type type : types) {
            if (type == Type.LIST) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof List) {
            return list.equals(obj);
        }
        if (obj instanceof var) {
            return list.equals(((var) obj).toList());
        }
        return false;
    }
}
