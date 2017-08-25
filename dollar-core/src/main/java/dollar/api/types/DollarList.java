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

import dollar.api.CollectionAware;
import dollar.api.DollarStatic;
import dollar.api.NumericAware;
import dollar.api.Pipeable;
import dollar.api.Type;
import dollar.api.collections.ImmutableList;
import dollar.api.collections.ImmutableMap;
import dollar.api.execution.DollarExecutor;
import dollar.api.guard.Guarded;
import dollar.api.guard.NotNullGuard;
import dollar.api.json.ImmutableJsonObject;
import dollar.api.json.JsonArray;
import dollar.api.json.JsonObject;
import dollar.api.plugin.Plugins;
import dollar.api.var;
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

    @NotNull
    private static final Logger log = LoggerFactory.getLogger("DollarList");
    public static final int MAX_LIST_MULTIPLIER = 1000;
    @Nullable
    private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);
    @NotNull
    private final ImmutableList<var> list;

    DollarList(@NotNull ImmutableList<Throwable> errors, @NotNull JsonArray array) {
        this(errors, ImmutableList.copyOf(array.toList()));
    }

    DollarList(@NotNull ImmutableList<Throwable> errors, @NotNull ImmutableList<?> list) {
        super(errors);
        List<var> l = new ArrayList<>();
        for (Object value : list) {
            if ((value instanceof var)) {
                if (((var) value).dynamic() || !((var) value).isVoid()) {
                    l.add((var) value);
                }
            } else //noinspection StatementWithEmptyBody
                if (value == null) {
                    l.add(DollarStatic.$null(Type._ANY));
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
                if (((var) value).dynamic() || !((var) value).isVoid()) {
                    l.add((var) value);
                }
            } else //noinspection StatementWithEmptyBody
                if (value == null) {
                    l.add(DollarStatic.$null(Type._ANY));
                } else {
                    l.add(DollarFactory.fromValue(value, errors));
                }
        }
        list = ImmutableList.copyOf(l);
    }

    @NotNull
    @Override
    public var $abs() {
        return this;
    }

    @NotNull
    @Override
    public var $minus(@NotNull var rhs) {
        ArrayList<var> newVal = new ArrayList<>(list.mutable());
        newVal.remove(rhs);
        return DollarFactory.fromValue(newVal, errors());
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $plus(@NotNull var rhs) {
        return $append(rhs);

    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $negate() {
        ArrayList<var> result = new ArrayList<>(list.mutable());
        Collections.reverse(result);
        return DollarFactory.fromValue(result, errors());
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $divide(@NotNull var rhs) {

        var rhsFix = rhs.$fixDeep();
        if ((rhsFix.toDouble() == null) || (rhsFix.toDouble() == 0.0)) {
            return DollarFactory.infinity(true, errors(), rhsFix.errors());
        }
        final int size = (int) ((double) list.size() / Math.abs(rhsFix.toDouble()));
        if (Math.abs(size) > list.size()) {
            return $multiply(DollarFactory.fromValue(1.0d / rhsFix.toDouble(), rhsFix.errors()));
        }
        if (rhsFix.positive()) {
            return DollarFactory.fromValue(list.subList(0, size), errors(), rhsFix.errors());
        } else {
            return DollarFactory.fromValue(list.subList(list.size() - size, list.size()), errors(), rhsFix.errors());
        }
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $modulus(@NotNull var rhs) {
        final int size = (int) ((double) list.size() / rhs.toDouble());
        return DollarFactory.fromValue(list.subList(list.size() - size, list.size()), errors(), rhs.errors());
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $multiply(@NotNull var rhs) {
        var v = rhs.$fixDeep();
        ArrayList<var> list = new ArrayList<>();
        final int max = Math.abs(v.toInteger());
        if (max > MAX_LIST_MULTIPLIER) {
            return DollarFactory.failure(ErrorType.MULTIPLIER_TOO_LARGE,
                                         "Cannot multiply a list by a value greater than " + MAX_LIST_MULTIPLIER,
                                         false);
        }
        for (int i = 0; i < max; i++) {
            list.addAll(this.list.mutable());
        }
        if (v.negative()) {
            Collections.reverse(list);
        }
        return DollarFactory.fromValue(list, errors(), v.errors());
    }

    @NotNull
    @Override
    public Integer toInteger() {
        return $stream(false).mapToInt(NumericAware::toInteger).sum();
    }

    @NotNull
    @Override
    public Number toNumber() {
        return 0;
    }

    @NotNull
    @Override
    public var $as(@NotNull Type type) {
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
            return DollarFactory.failure(ErrorType.INVALID_CAST);
        }
    }

    @NotNull
    @Override
    public ImmutableList<var> toVarList() {
        List<var> result = new ArrayList<>();
        for (var in : list) {
            result.add(in.$fix(1, false));
        }
        return ImmutableList.copyOf(result);
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

    @NotNull
    @Override
    public ImmutableMap<var, var> toVarMap() {
        AtomicInteger counter = new AtomicInteger();
        return list.stream().map(var -> DollarStatic.$(String.valueOf(counter.getAndIncrement()), var)).reduce(
                CollectionAware::$append).get().toVarMap();
    }

    @NotNull
    @Override
    public String toYaml() {
        Yaml yaml = new Yaml();
        return yaml.dump(list);
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
    public boolean isVoid() {
        return false;
    }

    @Override
    public ImmutableList<String> toStrings() {
        return ImmutableList.copyOf(list.stream().map(Object::toString).collect(Collectors.toList()));
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

    @NotNull
    @Override
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return ImmutableMap.copyOf(Collections.singletonMap((K) "value", (V) toVarList()));
    }

    @NotNull
    @Override
    public String toHumanString() {
        return jsonArray().toString();
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
    public String toDollarScript() {
        StringBuilder builder = new StringBuilder("[");
        for (var value : list) {
            builder.append(value.toDollarScript()).append(",");
        }
        builder.append("]");
        return builder.toString();
    }

    @NotNull
    @Override
    public <R> R toJavaObject() {
        return (R) Collections.unmodifiableList(toList().mutable());
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
    public var $get(@NotNull var key) {
        if (key.number()) {
            return list.get(key.toInteger());
        }
        for (var var : list) {
            if (var.equals(key)) {
                return var;
            }
        }
        return DollarStatic.$void();
    }

    @Guarded(NotNullGuard.class)
    @NotNull
    @Override
    public var $append(@NotNull var value) {

        final ArrayList<var> newList = new ArrayList<>(toVarList().mutable());
        if (value.list()) {
            newList.addAll(value.toVarList().mutable());
        } else {
            newList.add(value);
        }
        return DollarFactory.fromValue(newList, errors(), value.errors());
    }

    @NotNull
    @Override
    public var $containsValue(@NotNull var value) {
        return DollarStatic.$(list.contains(DollarStatic.fix(value, false)));
    }

    @NotNull
    @Override
    public var $containsKey(@NotNull var value) {
        return DollarStatic.$(list.contains(DollarStatic.fix(value, false)));
    }

    @NotNull
    @Override
    public var $has(@NotNull var key) {
        return $containsValue(key);
    }

    @NotNull
    @Override
    public var $size() {
        return DollarStatic.$(list.size());
    }

    @Guarded(NotNullGuard.class)
    @NotNull
    @Override
    public var $prepend(@NotNull var value) {
        final ArrayList newList = new ArrayList();

        if (value.list()) {
            newList.addAll(value.toVarList().mutable());
        } else {
            newList.add(value);
        }
        newList.addAll(toVarList().mutable());
        return DollarFactory.fromValue(newList, errors(), value.errors());
    }

    @Guarded(NotNullGuard.class)
    @NotNull
    @Override
    public var $insert(@NotNull var value, int position) {
        final ArrayList newList = new ArrayList();
        newList.addAll(toVarList().mutable());
        if (value.list()) {
            newList.addAll(position, value.toVarList().mutable());
        } else {
            newList.add(position, value);
        }
        return DollarFactory.fromValue(newList, errors(), value.errors());
    }


    @NotNull
    @Override
    public var $removeByKey(@NotNull String value) {
        return DollarFactory.failure(ErrorType.INVALID_LIST_OPERATION);
    }

    @NotNull
    @Override
    public var $set(@NotNull var key, @NotNull Object value) {
        ArrayList<var> newVal = new ArrayList<>(list.mutable());
        if (key.integer()) {
            newVal.set(key.toInteger(), DollarFactory.fromValue(value));
        } else {
            return DollarFactory.failure(ErrorType.INVALID_LIST_OPERATION);
        }
        return DollarFactory.fromValue(newVal, errors());
    }

    @NotNull
    @Override
    public var $remove(@NotNull var value) {
        List<var> newList = list.stream().filter(val -> !val.equals(value)).collect(Collectors.toList());
        return DollarFactory.fromValue(newList, errors());
    }

    @NotNull
    @Override
    public int size() {
        return list.size();
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $listen(@NotNull Pipeable pipe) {
        String key = UUID.randomUUID().toString();
        $listen(pipe, key);
        return DollarStatic.$(key);
    }

    @NotNull
    @Override
    public var $listen(@NotNull Pipeable pipe, @NotNull String key) {
        for (var v : list) {
            //Join the children to this, so if the children change
            //listeners to this get the latest value of this.
            v.$listen(i -> this, key);
        }
        return DollarStatic.$(key);
    }

    @NotNull
    @Override
    public var $write(@NotNull var value, boolean blocking, boolean mutating) {
        return $plus(value);
    }

    @NotNull
    @Override
    public var $notify() {
        list.forEach(var::$notify);
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

    @NotNull
    @Override
    public var $fix(int depth, boolean parallel) {
        if (depth < 1) {
            if (DollarStatic.getConfig().debugParallel()) {
                log.info("Fixing done in {}", parallel ? "parallel" : "serial");
            }

            return this;
        } else {
            List<var> result = new ArrayList<>();
            if (parallel) {
                for (var in : list) {
                    if (DollarStatic.getConfig().debugParallel()) {
                        log.info("Fixing list in parallel (depth={})", depth);

                    }
                    result.add(DollarStatic.$fork(() -> in.$fix(depth - 1, true)));
                }

            } else {
                for (var in : list) {
                    if (DollarStatic.getConfig().debugParallel()) {
                        log.info("Fixing list in serial (depth={})", depth);

                    }
                    result.add(in.$fix(depth - 1, false));
                }
            }
            return DollarFactory.fromList(result);
        }

    }

    @Override
    public boolean list() {
        return true;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof ImmutableList) {
            return list.mutable().equals(((ImmutableList) obj).mutable());
        } else if (obj instanceof List) {
            return list.mutable().equals(obj);
        } else if (obj instanceof var) {
            return list.mutable().equals(((var) obj).toVarList().mutable());
        }
        return false;
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
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean neitherTrueNorFalse() {
        return true;
    }

    @Override
    public boolean truthy() {
        return !list.isEmpty();
    }

}
