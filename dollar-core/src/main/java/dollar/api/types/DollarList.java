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

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.Type;
import dollar.api.Value;
import dollar.api.exceptions.DollarFailureException;
import dollar.api.guard.Guarded;
import dollar.api.guard.NotNullGuard;
import dollar.api.json.ImmutableJsonObject;
import dollar.api.json.JsonArray;
import dollar.api.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DollarList extends AbstractDollar {

    public static final int MAX_LIST_MULTIPLIER = 1000;

    @NotNull
    private static final Logger log = LoggerFactory.getLogger(DollarList.class);

    @NotNull
    private final ImmutableList<Value> list;

    DollarList(@NotNull JsonArray array) {
        this(ImmutableList.copyOf(array.toList()));
    }

    DollarList(@NotNull ImmutableList<?> list) {
        super();
        List<Value> l = new ArrayList<>();
        for (Object value : list) {
            if ((value instanceof Value)) {
                if (((Value) value).dynamic() || !((Value) value).isVoid()) {
                    l.add((Value) value);
                }
            } else if (value == null) {
                l.add(DollarStatic.$null(Type._ANY));
            } else {
                l.add(DollarFactory.fromValue(value));
            }
        }
        this.list = ImmutableList.copyOf(l);
    }

    DollarList(@NotNull Object[] values) {
        super();
        List<Value> l = new ArrayList<>();
        for (Object value : values) {
            if ((value instanceof Value)) {
                if (((Value) value).dynamic() || !((Value) value).isVoid()) {
                    l.add((Value) value);
                }
            } else if (value == null) {
                l.add(DollarStatic.$null(Type._ANY));
            } else {
                l.add(DollarFactory.fromValue(value));
            }
        }
        list = ImmutableList.copyOf(l);
    }

    @NotNull
    @Override
    public Value $abs() {
        return this;
    }

    @Guarded(NotNullGuard.class)
    @NotNull
    @Override
    public Value $append(@NotNull Value value) {

        final ArrayList<Value> newList = new ArrayList<>(toVarList());
        if (value.list()) {
            newList.addAll(value.toVarList());
        } else {
            newList.add(value);
        }
        return DollarFactory.fromValue(newList);
    }

    @NotNull
    @Override
    public Value $as(@NotNull Type type) {
        if (type.is(Type._LIST)) {
            return this;
        } else if (type.is(Type._MAP)) {
            return DollarStatic.$(toJavaMap());
        } else if (type.is(Type._STRING)) {
            return DollarFactory.fromStringValue(toHumanString());
        } else if (type.is(Type._VOID)) {
            return DollarStatic.$void();
        } else if (type.is(Type._BOOLEAN)) {
            return DollarStatic.$(!list.isEmpty());
        } else {
            throw new DollarFailureException(ErrorType.INVALID_CAST);
        }
    }

    @NotNull
    @Override
    public Value $containsKey(@NotNull Value value) {
        return DollarStatic.$(list.contains(DollarStatic.fix(value, false)));
    }

    @NotNull
    @Override
    public Value $containsValue(@NotNull Value value) {
        return DollarStatic.$(list.contains(DollarStatic.fix(value, false)));
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $divide(@NotNull Value rhs) {

        Value rhsFix = rhs.$fixDeep();
        if ((rhsFix.toDouble() == 0.0)) {
            return DollarFactory.infinity(true);
        }
        final int size = (int) ((double) list.size() / Math.abs(rhsFix.toDouble()));
        if (Math.abs(size) > list.size()) {
            return $multiply(DollarFactory.fromValue(1.0d / rhsFix.toDouble()));
        }
        if (rhsFix.positive()) {
            return DollarFactory.fromValue(list.subList(0, size));
        } else {
            return DollarFactory.fromValue(list.subList(list.size() - size, list.size()));
        }
    }

    @NotNull
    @Override
    public Value $get(@NotNull Value key) {
        if (key.number()) {
            if (key.toLong() < 0) {
                return list.get(size() + key.toInteger());
            } else {
                return list.get(key.toInteger());
            }

        }
        for (Value Value : list) {
            if (Value.equals(key)) {
                return Value;
            }
        }
        return DollarStatic.$void();
    }

    @NotNull
    @Override
    public Value $has(@NotNull Value key) {
        return $containsValue(key);
    }

    @Guarded(NotNullGuard.class)
    @NotNull
    @Override
    public Value $insert(@NotNull Value value, int position) {
        final ArrayList newList = new ArrayList();
        newList.addAll(toVarList());
        if (value.list()) {
            newList.addAll(position, value.toVarList());
        } else {
            newList.add(position, value);
        }
        return DollarFactory.fromValue(newList);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $listen(@NotNull Pipeable pipe) {
        String key = UUID.randomUUID().toString();
        $listen(pipe, key);
        return DollarStatic.$(key);
    }

    @NotNull
    @Override
    public Value $listen(@NotNull Pipeable pipe, @NotNull String key) {
        for (Value v : list) {
            //Join the children to this, so if the children change
            //listeners to this get the latest value of this.
            v.$listen(i -> this, key);
        }
        return DollarStatic.$(key);
    }

    @NotNull
    @Override
    public Value $minus(@NotNull Value rhs) {
        ArrayList<Value> newVal = new ArrayList<>(list);
        newVal.remove(rhs);
        return DollarFactory.fromValue(newVal);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $modulus(@NotNull Value rhs) {
        final int size = (int) ((double) list.size() / rhs.toDouble());
        return DollarFactory.fromValue(list.subList(list.size() - size, list.size()));
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $multiply(@NotNull Value rhs) {
        Value v = rhs.$fixDeep();
        ArrayList<Value> list = new ArrayList<>();
        final int max = Math.abs(v.toInteger());
        if (max > MAX_LIST_MULTIPLIER) {
            return DollarFactory.failure(ErrorType.MULTIPLIER_TOO_LARGE,
                                         "Cannot multiply a list by a value greater than " + MAX_LIST_MULTIPLIER,
                                         false);
        }
        for (int i = 0; i < max; i++) {
            list.addAll(this.list);
        }
        if (v.negative()) {
            Collections.reverse(list);
        }
        return DollarFactory.fromValue(list);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $negate() {
        ArrayList<Value> result = new ArrayList<>(list);
        Collections.reverse(result);
        return DollarFactory.fromValue(result);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $plus(@NotNull Value rhs) {
        return $append(rhs);

    }

    @Guarded(NotNullGuard.class)
    @NotNull
    @Override
    public Value $prepend(@NotNull Value value) {
        final ArrayList newList = new ArrayList();

        if (value.list()) {
            newList.addAll(value.toVarList());
        } else {
            newList.add(value);
        }
        newList.addAll(toVarList());
        return DollarFactory.fromValue(newList);
    }

    @NotNull
    @Override
    public Value $remove(@NotNull Value value) {
        List<Value> newList = list.stream().filter(val -> !val.equals(value)).collect(Collectors.toList());
        return DollarFactory.fromValue(newList);
    }

    @NotNull
    @Override
    public Value $removeByKey(@NotNull String value) {
        throw new DollarFailureException(ErrorType.INVALID_LIST_OPERATION);
    }

    @NotNull
    @Override
    public Value $set(@NotNull Value key, @NotNull Object value) {
        ArrayList<Value> newVal = new ArrayList<>(list);
        if (key.integer()) {
            newVal.set(key.toInteger(), DollarFactory.fromValue(value));
        } else {
            throw new DollarFailureException(ErrorType.INVALID_LIST_OPERATION);
        }
        return DollarFactory.fromValue(newVal);
    }

    @NotNull
    @Override
    public Value $size() {
        return DollarStatic.$(list.size());
    }

    @NotNull
    @Override
    public Value $stream(boolean parallel) {
        Stream<Value> stream;
        if (parallel) {
            stream = list.stream().parallel();
        } else {
            stream = list.stream();
        }
        return DollarFactory.fromStream(stream);
    }

    @NotNull
    @Override
    public Type $type() {
        return new Type(Type._LIST, constraintLabel());
    }

    @Override
    public boolean collection() {
        return true;
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type.is(Type._LIST)) {
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
        return false;
    }

    /**
     * Convert this object into a Dollar JsonArray.
     *
     * @return a JsonArray
     */
    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public JsonArray jsonArray() {
        return (JsonArray) DollarFactory.toJson($fixDeep());
    }

    @Override
    public boolean neitherTrueNorFalse() {
        return true;
    }

    @NotNull
    @Override
    public int size() {
        return list.size();
    }

    @NotNull
    @Override
    public String toDollarScript() {
        StringBuilder builder = new StringBuilder("[");
        for (Value value : list) {
            builder.append(value.toDollarScript()).append(",");
        }
        builder.append("]");
        return builder.toString();
    }

    @NotNull
    @Override
    public String toHumanString() {
        return jsonArray().toString();
    }

    @Override
    public int toInteger() {
        return stream(false).mapToInt(Value::toInteger).sum();
    }

    @NotNull
    @Override
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return ImmutableMap.copyOf(Collections.singletonMap((K) "value", (V) toVarList()));
    }

    @NotNull
    @Override
    public <R> R toJavaObject() {
        return (R) Collections.unmodifiableList(toList());
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
        for (Value val : list) {
            newList.add(val.toJavaObject());
        }
        return ImmutableList.copyOf(newList);

    }

    @NotNull
    @Override
    public Number toNumber() {
        return 0;
    }

    @Override
    public ImmutableList<String> toStrings() {
        return ImmutableList.copyOf(list.stream().map(Object::toString).collect(Collectors.toList()));
    }

    @NotNull
    @Override
    public ImmutableList<Value> toVarList() {
        List<Value> result = new ArrayList<>();
        for (Value in : list) {
            result.add(in.$fix(1, false));
        }
        return ImmutableList.copyOf(result);
    }

    @NotNull
    @Override
    public ImmutableMap<Value, Value> toVarMap() {
        AtomicInteger counter = new AtomicInteger();
        return list.stream().map(var -> DollarStatic.$(String.valueOf(counter.getAndIncrement()), var)).reduce(
                Value::$append).get().toVarMap();
    }

    @NotNull
    @Override
    public String toYaml() {
        Yaml yaml = new Yaml();
        return yaml.dump(list);
    }

    @Override
    public boolean truthy() {
        return !list.isEmpty();
    }

    @NotNull
    @Override
    public Value $copy() {
        return DollarFactory.fromValue(list.stream().map(Value::$copy).collect(Collectors.toList()));
    }

    @NotNull
    @Override
    public Value $fix(int depth, boolean parallel) {
        if (depth < 1) {
            if (DollarStatic.getConfig().debugParallel()) {
                log.info("Fixing done in {}", parallel ? "parallel" : "serial");
            }

            return this;
        } else {
            List<Value> result = new ArrayList<>();
            if (parallel) {
                for (Value in : list) {
                    if (DollarStatic.getConfig().debugParallel()) {
                        log.info("Fixing list in parallel (depth={})", depth);

                    }
                    result.add(DollarStatic.$fork(source(), in, i -> i.$fix(depth, true)));
                }

            } else {
                for (Value in : list) {
                    if (DollarStatic.getConfig().debugParallel()) {
                        log.info("Fixing list in serial (depth={})", depth);

                    }
                    result.add(in.$fix(depth, false));
                }
            }
            return DollarFactory.fromList(result);
        }

    }

    @Override
    public Value $notify(NotificationType type, Value value) {
        list.forEach(member -> member.$notify(NotificationType.UNARY_VALUE_CHANGE, value));
        return this;
    }

    @NotNull
    @Override
    public Value $reverse(boolean parallel) {
        ArrayList<Value> newList = new ArrayList<>(list);
        Collections.reverse(newList);
        return DollarFactory.wrap(new DollarList(ImmutableList.copyOf(newList)));
    }

    @NotNull
    @Override
    public Value $write(@NotNull Value value, boolean blocking, boolean mutating) {
        return $plus(value);
    }

    @Override
    public boolean list() {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), list);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof ImmutableList) {
            return list.equals(obj);
        } else if (obj instanceof List) {
            return list.equals(obj);
        } else if (obj instanceof Value) {
            return list.equals(((Value) obj).toVarList());
        }
        return false;
    }

    @Override
    public int compareTo(@NotNull Value o) {
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
