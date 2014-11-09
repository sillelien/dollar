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


    DollarList(@NotNull ImmutableList<Throwable> errors, @NotNull List<var> list) {
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

    DollarList(@NotNull ImmutableList<Throwable> errors, @NotNull Object... values) {
        super(errors);
        List<var> l = new ArrayList<>();
        for (Object value : values) {
            if ((value instanceof var)) {
                l.add((var) value);
            } else {
                l.add(DollarFactory.fromValue(errors, value));
            }
        }
        list = ImmutableList.copyOf(l);
    }

    @NotNull
    @Override
    public var $plus(Object value) {
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

    @Override
    public var $dec(var amount) {
        return this;
    }

    @Override
    public var $inc(var amount) {
        return this;
    }

    @Override
    public var $negate() {
        ArrayList<var> result = new ArrayList<>(list);
        Collections.reverse(result);
        return DollarFactory.fromValue(errors(), result);
    }

    @Override
    public var $multiply(var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @Override
    public var $divide(var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @Override
    public var $modulus(var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @Override
    public var $abs() {
        return this;
    }

    @Override
    public boolean $has(@NotNull String key) {
        return false;
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
    public var $(@NotNull String key) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
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
        return ImmutableList.copyOf(list.stream().map(Object::toString).collect(Collectors.toList()));
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

    @Override
    public <R> R val() {
        return (R) list;
    }

    @Override
    public var $(Number n) {
        return list.get(n.intValue());
    }

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
    public int size() {
        return list.size();
    }

    @Override
    public boolean containsValue(Object value) {
        return list.contains(value);
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
}
