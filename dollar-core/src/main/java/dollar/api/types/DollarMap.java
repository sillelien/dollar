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
import dollar.api.Pipeable;
import dollar.api.Type;
import dollar.api.Value;
import dollar.api.exceptions.DollarFailureException;
import dollar.api.json.ImmutableJsonObject;
import dollar.api.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dollar.api.types.DollarFactory.wrap;


public class DollarMap extends AbstractDollar {

    /**
     * Publicly accessible object containing the current state as a JsonObject, if you're working in Vert.x primarily
     * with the JsonObject type you will likely end all chained expressions with '.$'
     * <p>
     * For example: {@code eb.send("api.validate", $("key", key).$("params", request.params()).$) }
     */
    protected final
    @NotNull
    LinkedHashMap<Value, Value> map;


    /**
     * Create a $ object from a variety of different objects. At present the following are supported:<br/> <ul>
     * <li>JsonObject</li> <li>MultiMap</li> <li>Message</li> </ul>
     * <p>
     * Any other object types will be converted to a string using .toString() and will then be parsed as JSON.
     *
     * @param o the object of unknown type to be converted to a JsonObject and then wrapped by the $ class.
     */
    DollarMap(@NotNull JsonObject o) {
        super();
        map = mapToVarMap(o.toMap());
    }

    public DollarMap(@NotNull Map<?, ?> o) {
        super();
        map = mapToVarMap(o);
    }

    public DollarMap(@NotNull LinkedHashMap<Value, Value> o) {
        super();
        map = deepClone(o);
    }

    public DollarMap(@NotNull ImmutableMap<Value, Value> o) {
        super();
        map = mapToVarMap(o);
    }

    public DollarMap(@NotNull ImmutableJsonObject immutableJsonObject) {
        super();
        map = mapToVarMap(immutableJsonObject.toMap());
    }

    public DollarMap() {
        super();
        map = new LinkedHashMap<>();
    }

    @NotNull
    @Override
    public Value $abs() {
        return this;
    }

    @NotNull
    @Override
    public Value $append(@NotNull Value value) {
        final LinkedHashMap<Value, Value> newMap = new LinkedHashMap<>(toVarMap());
        newMap.put(value.$pairKey(), value.$pairValue());
        return DollarFactory.fromValue(newMap);
    }

    @NotNull
    @Override
    public Value $as(@NotNull Type type) {
        if (type.is(Type._MAP)) {
            return this;
        } else if (type.is(Type._LIST)) {
            return DollarStatic.$(toVarList());
        } else if (type.is(Type._BOOLEAN)) {
            return DollarStatic.$(!map.isEmpty());
        } else if (type.is(Type._STRING)) {
            return DollarFactory.fromStringValue(toHumanString());
        } else if (type.is(Type._VOID)) {
            return DollarStatic.$void();
        } else {
            throw new DollarFailureException(ErrorType.INVALID_CAST);
        }
    }

    @NotNull
    @Override
    public Value $containsKey(@NotNull Value value) {
        return DollarStatic.$(map.containsKey(value));
    }

    @Override
    @NotNull
    public Value $containsValue(@NotNull Value value) {
        return DollarStatic.$(map.containsValue(value));
    }

    @NotNull
    @Override
    public Value $divide(@NotNull Value rhs) {
        throw new DollarFailureException(ErrorType.INVALID_MAP_OPERATION);
    }

    @NotNull
    @Override
    public Value $get(@NotNull Value key) {
        if (key.integer()) {
            final Value mapKey = (Value) map.keySet().toArray()[key.toInteger()];
            return DollarStatic.$(mapKey, map.get(mapKey));
        } else {
            return DollarFactory.fromValue(map.get(key));
        }

    }

    @NotNull
    @Override
    public Value $has(@NotNull Value key) {
        return DollarStatic.$(map.containsKey(key));
    }

    @NotNull
    @Override
    public Value $insert(@NotNull Value value, int position) {
        final LinkedHashMap<Value, Value> newMap = new LinkedHashMap<>();
        int count = 0;
        for (Map.Entry<Value, Value> entry : newMap.entrySet()) {
            if (count == position) {
                newMap.put(value.$pairKey(), value.$pairValue());
            }
            newMap.put(entry.getKey(), entry.getValue());

        }
        newMap.putAll(toVarMap());
        return DollarFactory.fromValue(newMap);
    }

    @NotNull
    @Override
    public Value $listen(@NotNull Pipeable pipe) {
        String key = UUID.randomUUID().toString();
        $listen(pipe, key);
        return DollarStatic.$(key);
    }

    @NotNull
    @Override
    public Value $listen(@NotNull Pipeable pipe, @NotNull String key) {
        for (Value v : map.values()) {
            //Join the children to this, so if the children change
            //listeners to this get the latest value of this.
            v.$listen(i -> this, key);
        }
        return DollarStatic.$(key);
    }

    @NotNull
    @Override
    public Value $map() {
        return this;
    }

    @NotNull
    @Override
    public Value $mimeType() {
        return DollarStatic.$("application/json");
    }

    @NotNull
    @Override
    public Value $minus(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();
        if (rhsFix.map()) {
            LinkedHashMap<Value, Value> copy = copyMap();
            for (Map.Entry<Value, Value> entry : rhsFix.toVarMap().entrySet()) {
                copy.remove(entry.getKey());
            }
            return wrap(new DollarMap(copy));
        } else {
            LinkedHashMap<Value, Value> copy = copyMap();
            copy.remove(rhsFix);
            return wrap(new DollarMap(copy));
        }
    }

    @NotNull
    @Override
    public Value $modulus(@NotNull Value rhs) {
        throw new DollarFailureException(ErrorType.INVALID_MAP_OPERATION);
    }

    @NotNull
    @Override
    public Value $multiply(@NotNull Value v) {
        throw new DollarFailureException(ErrorType.INVALID_MAP_OPERATION);
    }

    @NotNull
    @Override
    public Value $negate() {
        LinkedHashMap<Value, Value> result = new LinkedHashMap<>();
        final ArrayList<Map.Entry<Value, Value>> entries = new ArrayList<>(map.entrySet());
        Collections.reverse(entries);
        for (Map.Entry<Value, Value> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return DollarFactory.fromValue(result);
    }

    @NotNull
    @Override
    public Value $plus(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();
        if (rhsFix.map()) {
            LinkedHashMap<Value, Value> copy = copyMap();
            copy.putAll(rhsFix.toVarMap());
            return wrap(new DollarMap(copy));
        } else if (rhsFix.string()) {
            return DollarFactory.fromValue(toHumanString() + rhsFix.toHumanString());
        } else {
            LinkedHashMap<Value, Value> copy = copyMap();
            copy.put(DollarFactory.fromValue("_" + copy.size()), rhsFix);
            return wrap(new DollarMap(copy));
        }
    }

    @NotNull
    @Override
    public Value $prepend(@NotNull Value value) {
        final LinkedHashMap<Value, Value> newMap = new LinkedHashMap<>();
        newMap.put(value.$pairKey(), value.$pairValue());
        newMap.putAll(toVarMap());
        return DollarFactory.fromValue(newMap);
    }

    @NotNull
    @Override
    public Value $remove(@NotNull Value key) {
        final LinkedHashMap<Value, Value> newMap = new LinkedHashMap<>(map);
        newMap.remove(key);
        return DollarFactory.fromValue(newMap);
    }

    @NotNull
    @Override
    public Value $removeByKey(@NotNull String key) {
        final LinkedHashMap<Value, Value> newMap = new LinkedHashMap<>(map);
        newMap.remove(DollarStatic.$(key));
        return DollarFactory.fromValue(newMap);
    }

    @NotNull
    @Override
    public Value $set(@NotNull Value key, @NotNull Object value) {
        LinkedHashMap<Value, Value> copyMap = copyMap();
        copyMap.put(key, DollarFactory.fromValue(value));
        return wrap(new DollarMap(copyMap));
    }

    @NotNull
    @Override
    public Value $size() {
        return DollarStatic.$(toJavaMap().size());
    }


    @NotNull
    @Override
    public Type $type() {
        return new Type(Type._MAP, constraintLabel());
    }

    @Override
    public boolean collection() {
        return true;
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type.is(Type._MAP)) {
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

    @Override
    public boolean neitherTrueNorFalse() {
        return true;
    }

    @NotNull
    @Override
    public int size() {
        return map.size();
    }

    @NotNull
    @Override
    public Stream<Value> stream(boolean parallel) {
        return split().values().stream();
    }

    @NotNull
    @Override
    public String toDollarScript() {
        StringBuilder builder = new StringBuilder("{");
        for (Map.Entry<Value, Value> entry : map.entrySet()) {
            builder.append(entry.getKey().toDollarScript())
                    .append(" : ")
                    .append(entry.getValue().toDollarScript())
                    .append(",\n");
        }
        builder.append("}");
        return builder.toString();
    }

    @NotNull
    @Override
    public String toHumanString() {
        return toJsonObject().toString();
    }

    @NotNull
    @Override
    public int toInteger() {
        @NotNull Value result;
        throw new DollarFailureException(ErrorType.INVALID_MAP_OPERATION);
    }

    @NotNull
    @Override
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        Map<K, V> varMap = varMapToMap();
        return ImmutableMap.copyOf(varMap);
    }

    @NotNull
    @Override
    public <R> R toJavaObject() {
        return (R) varMapToMap();
    }

    @NotNull
    @Override
    public ImmutableList<Object> toList() {
        final ArrayList<Object> entries = new ArrayList<>();
        for (Map.Entry<Value, Value> entry : map.entrySet()) {
            entries.add(entry.getValue().toJavaObject());
        }
        return ImmutableList.copyOf(entries);
    }

    @NotNull
    @Override
    public Number toNumber() {
        return 0;
    }

    @Override
    public ImmutableList<String> toStrings() {
        List<String> values = new ArrayList<>();
        ImmutableMap<String, Object> map = toJavaMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            assert entry.getKey() instanceof String;
            values.add(entry.getKey());
            values.add(entry.getValue().toString());
        }
        return ImmutableList.copyOf(values);
    }

    @NotNull
    @Override
    public ImmutableList<Value> toVarList() {
        final List<Value> entries =
                map.entrySet()
                        .stream()
                        .map(entry -> DollarStatic.$(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList());
        return ImmutableList.copyOf(entries);
    }

    @NotNull
    @Override
    public ImmutableMap<Value, Value> toVarMap() {

        LinkedHashMap<Value, Value> result = new LinkedHashMap<>();
        for (Map.Entry<Value, Value> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue().$fix(false));
        }
        return ImmutableMap.copyOf(result);
    }

    @NotNull
    @Override
    public String toYaml() {
        Yaml yaml = new Yaml();
        return yaml.dump(map);
    }

    @Override
    public boolean truthy() {
        return !map.isEmpty();
    }

    @NotNull
    @Override
    public Value $copy() {
        return wrap(new DollarMap(map));
    }

    @NotNull
    @Override
    public Value $fix(int depth, boolean parallel) {
        if (depth <= 1) {
            return this;
        } else {
            LinkedHashMap<Value, Value> result = new LinkedHashMap<>();
            if (parallel) {
                for (Map.Entry<Value, Value> entry : map.entrySet()) {
                    result.put(entry.getKey(), DollarStatic.$fork(source(), entry.getValue(), in -> in.$fix(depth - 1, true)));
                }
            } else {
                for (Map.Entry<Value, Value> entry : map.entrySet()) {
                    result.put(entry.getKey(), entry.getValue().$fix(depth - 1, false));
                }
            }
            return new DollarMap(result);
        }
    }

    @Override
    public Value $notify(NotificationType type, Value newValue) {
        map.values().forEach(member -> member.$notify(NotificationType.UNARY_VALUE_CHANGE, newValue));
        return this;
    }

    @Override
    public boolean map() {
        return true;
    }

    @Override
    public boolean pair() {
        return map.size() == 1;
    }

    @Override
    public int compareTo(@NotNull Value o) {
        return Comparator.<Value>naturalOrder().<Value>compare(this, o);
    }

    @NotNull
    private LinkedHashMap<Value, Value> copyMap() {
        return deepClone(map);
    }

    @NotNull
    private LinkedHashMap<Value, Value> deepClone(@NotNull LinkedHashMap<Value, Value> o) {
        LinkedHashMap<Value, Value> result = new LinkedHashMap<>();
        for (Map.Entry<Value, Value> entry : o.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @NotNull
    private LinkedHashMap<Value, Value> mapToVarMap(@NotNull Map<?, ?> stringObjectMap) {
        LinkedHashMap<Value, Value> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : stringObjectMap.entrySet()) {
            result.put(DollarFactory.fromValue(entry.getKey()), DollarFactory.fromValue(entry.getValue()));
        }
        return result;
    }

    @NotNull
    Map<Value, Value> split() {
        return copyMap();
    }

    @NotNull
    private <K extends Comparable<K>, V> Map<K, V> varMapToMap() {
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<Value, Value> entry : toVarMap().entrySet()) {
            result.put(entry.getKey().toJavaObject(), entry.getValue().toJavaObject());
        }
        return result;
    }

}


