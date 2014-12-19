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

import me.neilellis.dollar.*;
import me.neilellis.dollar.collections.ImmutableList;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.guard.Guarded;
import me.neilellis.dollar.guard.NotNullGuard;
import me.neilellis.dollar.json.ImmutableJsonObject;
import me.neilellis.dollar.json.JsonArray;
import me.neilellis.dollar.json.JsonObject;
import org.jetbrains.annotations.NotNull;
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

    public static final int MAX_LIST_MULTIPLIER = 1000;
    private final ImmutableList<var> list;

    DollarList(@NotNull ImmutableList<Throwable> errors, @NotNull JsonArray array) {
        this(errors, ImmutableList.copyOf(array.toList()));
    }

    DollarList(@NotNull ImmutableList<Throwable> errors, @NotNull ImmutableList<?> list) {
        super(errors);
        List<var> l = new ArrayList<>();
        for (Object value : list) {
            if ((value instanceof var)) {
                if (((var) value).isLambda() || !((var) value).isVoid()) {
                    l.add((var) value);
                }
            } else //noinspection StatementWithEmptyBody
                if (value == null) {
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
            } else //noinspection StatementWithEmptyBody
                if (value == null) {
                    //Skip
                } else {
                    l.add(DollarFactory.fromValue(value, errors));
                }
        }
        this.list = ImmutableList.copyOf(l);
    }

    @NotNull
    @Override
    public var $abs() {
        return this;
    }

    @NotNull
    @Override
    public var $minus(@NotNull var v) {
        ArrayList<var> newVal = new ArrayList<>(list.mutable());
        newVal.remove(v);
        return DollarFactory.fromValue(newVal, errors());
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $plus(@NotNull var v) {
        return $append(v);

    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $negate() {
        ArrayList<var> result = new ArrayList<var>(list.mutable());
        Collections.reverse(result);
        return DollarFactory.fromValue(result, errors());
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $divide(@NotNull var rhs) {

        var rhsFix = rhs._fixDeep();
        if (rhsFix.D() == null || rhsFix.D() == 0.0) {
            return DollarFactory.infinity(true, errors(), rhsFix.errors());
        }
        final int size = (int) ((double) list.size() / Math.abs(rhsFix.D()));
        if (Math.abs(size) > list.size()) {
            return $multiply(DollarFactory.fromValue(1.0d / rhsFix.D(), rhsFix.errors()));
        }
        if (rhsFix.isPositive()) {
            return DollarFactory.fromValue(list.subList(0, size), errors(), rhsFix.errors());
        } else {
            return DollarFactory.fromValue(list.subList(list.size() - size, list.size()), errors(), rhsFix.errors());
        }
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $modulus(@NotNull var v) {
        final int size = (int) ((double) list.size() / v.D());
        return DollarFactory.fromValue(list.subList(list.size() - size, list.size()), errors(), v.errors());
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $multiply(@NotNull var rhs) {
        var v = rhs._fixDeep();
        ArrayList<var> list = new ArrayList<>();
        final int max = Math.abs(v.I());
        if (max > MAX_LIST_MULTIPLIER) {
            return DollarFactory.failure(me.neilellis.dollar.types.ErrorType.MULTIPLIER_TOO_LARGE,
                                         "Cannot multiply a list by a value greater than " + MAX_LIST_MULTIPLIER,
                                         false);
        }
        for (int i = 0; i < max; i++) {
            list.addAll(this.list.mutable());
        }
        if (v.isNegative()) {
            Collections.reverse(list);
        }
        return DollarFactory.fromValue(list, errors(), v.errors());
    }

    @NotNull @Override
    public Integer I() {
        return $stream(false).collect(Collectors.summingInt(
                (java.util.function.ToIntFunction<NumericAware>) (numericAware) -> numericAware.I()));
    }

    @NotNull
    @Override
    public Number N() {
        return 0;
    }

    @NotNull
    @Override
    public var $get(@NotNull var key) {
        if (key.isNumber()) {
            return list.get(key.I());
        }
        for (var var : list) {
            if (var.equals(key)) {
                return var;
            }
        }
        return DollarStatic.$void();
    }

    @Guarded(NotNullGuard.class)
    @NotNull @Override public var $append(@NotNull var value) {

        final ArrayList<var> newList = new ArrayList<>($list().mutable());
        if (value.isList()) {
            newList.addAll(value.$list().mutable());
        } else {
            newList.add(value);
        }
        return DollarFactory.fromValue(newList, errors(), value.errors());
    }

    @NotNull @Override
    public var $containsValue(@NotNull var value) {
        return DollarStatic.$(list.contains(fix(value, false)));
    }

    @NotNull @Override
    public var $has(@NotNull var key) {
        return $containsValue(key);
    }

    @NotNull @Override
    public var $size() {
        return DollarStatic.$(list.size());
    }

    @Guarded(NotNullGuard.class)
    @NotNull @Override public var $prepend(@NotNull var value) {
        final ArrayList newList = new ArrayList();
        newList.addAll($list().mutable());
        if (value.isList()) {
            newList.addAll(value.$list().mutable());
        } else {
            newList.add(value);
        }
        return DollarFactory.fromValue(newList, errors(), value.errors());
    }

    @NotNull
    @Override
    public var $removeByKey(@NotNull String value) {
        return DollarFactory.failure(me.neilellis.dollar.types.ErrorType.INVALID_LIST_OPERATION);
    }

    @NotNull
    @Override
    public var $set(@NotNull var key, Object value) {
        ArrayList<var> newVal = new ArrayList<>(list.mutable());
        if (key.isInteger()) {
            newVal.set(key.I(), DollarFactory.fromValue(value));
        } else {
            return DollarFactory.failure(me.neilellis.dollar.types.ErrorType.INVALID_LIST_OPERATION);
        }
        return DollarFactory.fromValue(newVal, errors());
    }

    @NotNull
    @Override
    public var $remove(var value) {
        List<var> newList = list.stream().filter(val -> !val.equals(value)).collect(Collectors.toList());
        return DollarFactory.fromValue(newList, errors());
    }

    @Override
    @Guarded(NotNullGuard.class)
    public var $listen(Pipeable pipe) {
        String key = UUID.randomUUID().toString();
        $listen(pipe, key);
        return DollarStatic.$(key);
    }

    @Override
    public var $listen(Pipeable pipe, String key) {
        for (var v : list) {
            //Join the children to this, so if the children change
            //listeners to this get the latest value of this.
            v.$listen(i -> this, key);
        }
        return DollarStatic.$(key);
    }

    @Override public var $write(var value, boolean blocking, boolean mutating) {
        return $plus(value);
    }

    @NotNull
    @Override
    public ImmutableMap<var, var> $map() {
        return null;
    }

    @Override
    public var $notify() {
        list.forEach(me.neilellis.dollar.var::$notify);
        return this;
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
    public var $copy() {
        return DollarFactory.fromValue(list.stream().map(var::$copy).collect(Collectors.toList()), errors());
    }

    @Override public var _fix(int depth, boolean parallel) {
        if (depth <= 1) {
            return this;
        } else {
            ImmutableList<var> result;
            if (parallel) {
                try {
                    result =
                            ImmutableList.copyOf(Execution.forkJoinPool.submit(
                                    () -> $stream(parallel).map(v -> v._fix(depth - 1, parallel)).collect(
                                            Collectors.toList())).get());

                } catch (InterruptedException e) {
                    Thread.interrupted();
                    result =
                            ImmutableList.<var>of(
                                    DollarFactory.failure(me.neilellis.dollar.types.ErrorType.INTERRUPTED, e,
                                                          false));

                } catch (ExecutionException e) {
                    result =
                            ImmutableList.of(
                                    DollarFactory.failure(me.neilellis.dollar.types.ErrorType.EXECUTION_FAILURE,
                                                          e.getCause(),
                                                          false));

                }
                return new DollarList(errors(), result);
            } else {
                return new DollarList(errors(),
                                      ImmutableList.copyOf($stream(parallel).map(v -> v._fix(depth - 1, parallel))
                                                                            .collect(Collectors.toList())));
            }
        }

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof ImmutableList) {
            return list.mutable().equals(((ImmutableList) obj).mutable());
        } else if (obj instanceof List) {
            return list.mutable().equals(obj);
        } else if (obj instanceof var) {
            return list.mutable().equals(((var) obj).$list().mutable());
        }
        return false;
    }

    @Override
    public boolean isList() {
        return true;
    }

    @Override
    public int compareTo(@NotNull var o) {
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
    public boolean isBoolean() {
        return false;
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
        return !list.isEmpty();
    }


    @NotNull
    @Override
    public <R> R toJavaObject() {
        return (R) jsonArray();
    }

    @NotNull @Override public String toDollarScript() {
        StringBuilder builder = new StringBuilder("[");
        for (var value : list) {
            builder.append(value.toDollarScript()).append(",");
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * Convert this object into a Dollar JsonArray.
     *
     * @return a JsonArray
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    public JsonArray jsonArray() {
        return (JsonArray) DollarFactory.toJson(this);
    }

    @NotNull
    @Override
    public JSONObject toOrgJson() {
        return new JSONObject(toJsonObject().toMap());
    }

    @NotNull
    @Override
    public ImmutableList<var> $list() {
        try {
            return ImmutableList.copyOf(
                    Execution.forkJoinPool.submit(() -> $stream(false).map(v -> v._fix(false)).collect(
                            Collectors.toList())).get());
        } catch (InterruptedException e) {
            Thread.interrupted();
            return ImmutableList.of(DollarFactory.failure(me.neilellis.dollar.types.ErrorType.INTERRUPTED, e, false));

        } catch (ExecutionException e) {
            return ImmutableList.of(DollarFactory.failure(me.neilellis.dollar.types.ErrorType.EXECUTION_FAILURE, e,
                                                          false));

        }
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public ImmutableJsonObject toJsonObject() {
        JsonArray array = jsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.putArray("value", array);
        return new ImmutableJsonObject(jsonObject);
    }

    @NotNull
    @Override
    public ImmutableList<Object> toList() {
        List<Object> newList = new ArrayList<>();
        for (var val : list) {
            newList.add(val.toJavaObject());
        }
        return ImmutableList.copyOf(newList);

    }


    @Override
    public boolean isVoid() {
        return false;
    }


    @Override
    public ImmutableList<String> strings() {
        return ImmutableList.copyOf(list.stream().map(Object::toString).collect(Collectors.toList()));
    }


    @NotNull @Override
    public Map<String, Object> toMap() {
        return Collections.singletonMap("value", $list());
    }


    @Override public boolean isCollection() {
        return true;
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
                return DollarFactory.failure(me.neilellis.dollar.types.ErrorType.INVALID_CAST);

        }
    }


    @Override public Type $type() {
        return Type.LIST;
    }

    @NotNull
    @Override
    public String S() {
        return jsonArray().toString();
    }


    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type == Type.LIST) {
                return true;
            }
        }
        return false;
    }

}
