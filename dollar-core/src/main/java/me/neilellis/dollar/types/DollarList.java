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
import com.google.common.collect.ImmutableMap;
import me.neilellis.dollar.AbstractDollar;
import me.neilellis.dollar.exceptions.ListException;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarList extends AbstractDollar {

    private final ImmutableList<var> list;

    DollarList(@NotNull ImmutableList<Throwable> errors, @NotNull JsonArray array) {
         super(errors);
        list = ImmutableList.copyOf((List<var>) array.toList()
                                                     .stream()
                                                     .map((i) -> DollarFactory.fromValue(errors, i))
                                                     .collect(Collectors.toList()));
    }

    DollarList(@NotNull ImmutableList<Throwable> errors, @NotNull List<var> list) {
         super(errors);
        this.list = ImmutableList.copyOf(list);
    }

    DollarList(@NotNull ImmutableList<Throwable> errors, @NotNull Object... values) {
         super(errors);
        List<var> l = new ArrayList<>();
         for (Object value : values) {
             l.add(DollarFactory.fromValue(errors, value));
        }
        list = ImmutableList.copyOf(l);
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public var $(@NotNull String age, long l) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @NotNull @Override public var $(@NotNull String key, double value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @NotNull
    @Override
    public var $append(Object value) {
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
    public boolean $has(@NotNull String key) {
        return false;
    }

    @NotNull
    @Override
    public ImmutableList<var> list() {
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
    public var $(@NotNull String key, Object value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
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
    public ImmutableMap<String, Object> toMap() {
        return null;
    }

    @Override
    public <R> R val() {
        return (R) list;
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
    public String toString() {
        return jsonArray().toString();
    }



}