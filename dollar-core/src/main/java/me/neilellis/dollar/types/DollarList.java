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
import me.neilellis.dollar.*;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.json.JsonArray;
import me.neilellis.dollar.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static me.neilellis.dollar.DollarStatic.fix;

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
                l.add(DollarFactory.fromValue(value, errors));
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
                l.add(DollarFactory.fromValue(value, errors));
            }
        }
        this.list = ImmutableList.copyOf(l);
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
        return DollarFactory.fromValue(newVal, errors());
    }

    @NotNull
    @Override
    public <R> R $() {
        return (R) jsonArray();
    }

    @NotNull
    @Override
    public var $(@NotNull Number n) {
        return list.get(n.intValue());
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
    public var $containsValue(var value) {
        return DollarStatic.$(list.contains(fix(value, false)));
    }

    @NotNull
    @Override
    public var $(@NotNull String key) {
        for (var var : list) {
            if (var.$S().equals(key)) {
                return var;
            }
        }
        return DollarStatic.$void();
    }

    @Override
    public var $has(@NotNull String key) {
        return DollarStatic.$(false);
    }

    @Nullable
    @Override
    public String S(@NotNull String key) {
        return null;
    }

    @NotNull
    @Override
    public var $minus(@NotNull var value) {
        ArrayList<var> newVal = new ArrayList<>(list);
        newVal.remove(value);
        return DollarFactory.fromValue(newVal, errors());
    }

    @NotNull
    @Override
    public var $plus(var value) {
        return DollarFactory.fromValue(ImmutableList.builder()
                                                    .addAll(list)
                                                    .add(DollarFactory.fromValue(value))
                                                    .build(), errors()
        );
    }

    @NotNull
    @Override
    public var $rm(@NotNull String value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {
        return null;
    }

    @Override
    public var $size() {
        return DollarStatic.$(list.size());
    }

    @Override
    public Stream<String> keyStream() {
        List<String> strings = strings();
        if (strings == null) {
            return Stream.empty();
        }
        return strings.stream();
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
        return DollarFactory.fromValue(newList, errors());
    }

    @NotNull
    @Override
    public var $abs() {
        return this;
    }

    @NotNull
    @Override
    public var $dec(@NotNull var amount) {
        return this;
    }

    @NotNull
    @Override
    public var $divide(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @NotNull
    @Override
    public var $inc(@NotNull var amount) {
        return this;
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @NotNull
    @Override
    public ImmutableList<var> toList() {
        return list;
    }

    @NotNull
    @Override
    public var $negate() {
        ArrayList<var> result = new ArrayList<>(list);
        Collections.reverse(result);
        return DollarFactory.fromValue(result, errors());
    }

    @NotNull @Override public var _fix(boolean parallel) {
        try {
            return Execution.forkJoinPool.submit(() ->
                                                         DollarFactory.fromValue(
                                                                 $stream(parallel).map(v -> v._fix(parallel))
                                                                                  .collect(Collectors.toList()),
                                                                 errors())).get();
        } catch (InterruptedException e) {
            Thread.interrupted();
            return DollarFactory.failure(DollarFail.FailureType.INTERRUPTED, e);

        } catch (ExecutionException e) {
            return DollarFactory.failure(DollarFail.FailureType.EXECUTION_FAILURE, e);

        }

    }

    @NotNull
    @Override
    public Stream<var> $stream(boolean parallel) {
        Stream<var> stream;
        if (parallel) {
            stream = list.stream().parallel();
        } else {
            stream = list.stream();
        }
        return stream;
    }

    @NotNull
    @Override
    public Stream<Map.Entry<String, var>> kvStream() {
        return null;
    }

    @Override
    public String $listen(Pipeable pipe) {
        String key = UUID.randomUUID().toString();
        $listen(pipe, key);
        return key;
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
    public var $notify() {
        for (var v : list) {
            v.$notify();
        }
        return this;
    }

    @Override
    public Integer I(@NotNull String key) {
        DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
        return null;
    }

    @Override
    public boolean isList() {
        return true;
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

    @NotNull
    @Override
    public var $copy() {
        return DollarFactory.fromValue(list.stream().map(var::$copy).collect(Collectors.toList()), errors());
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
    public boolean isVoid() {
        return false;
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
    public Integer I() {
        return $stream(false).collect(Collectors.summingInt((i) -> i.I()));
    }






    @org.jetbrains.annotations.NotNull
    @Override
    public JsonObject json(@NotNull String key) {
        DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
        return null;
    }


    @Override
    public Number number(@NotNull String key) {
        DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
        return null;
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


    @Override
    public var $as(Type type) {
        switch (type) {
            case LIST:
                return this;
            case MAP:
                return DollarStatic.$(toMap());
            case STRING:
                return DollarFactory.fromStringValue(S());
            case VOID:
                return DollarStatic.$void();
            case BOOLEAN:
                return DollarStatic.$(!list.isEmpty());
            default:
                return DollarFactory.failure(DollarFail.FailureType.INVALID_CAST);

        }
    }


    @NotNull
    @Override
    public String S() {
        return jsonArray().toString();
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

}
