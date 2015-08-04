/*
 * Copyright (c) 2014-2015 Neil Ellis
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

package com.sillelien.dollar.api.types;

import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.collections.ImmutableList;
import com.sillelien.dollar.api.collections.ImmutableMap;
import com.sillelien.dollar.api.json.ImmutableJsonObject;
import com.sillelien.dollar.api.json.JsonObject;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class DollarMap extends AbstractDollar implements var {

    /**
     * Publicly accessible object containing the current state as a JsonObject, if you're working in Vert.x primarily
     * with the JsonObject type you will likely end all chained expressions with '.$'
     *
     * For example: {@code eb.send("api.validate", $("key", key).$("params", request.params()).$) }
     */
    private final
    @NotNull
    LinkedHashMap<var, var> map;

    /**
     * Create a new and empty $ object.
     *
     * @param errors the errors
     */
    DollarMap(@NotNull ImmutableList<Throwable> errors) {
        super(errors);
        map = new LinkedHashMap<>();
    }

    /**
     * Create a $ object from a variety of different objects. At present the following are supported:<br/> <ul>
     * <li>JsonObject</li> <li>MultiMap</li> <li>Message</li> </ul>
     *
     * Any other object types will be converted to a string using .toString() and will then be parsed as JSON.
     *
     * @param o the object of unknown type to be converted to a JsonObject and then wrapped by the $ class.
     */
    DollarMap(@NotNull ImmutableList<Throwable> errors, @NotNull JsonObject o) {
        super(errors);
        map = mapToVarMap(o.toMap());
    }

    @NotNull private LinkedHashMap<var, var> mapToVarMap(@NotNull Map<?, ?> stringObjectMap) {
        LinkedHashMap<var, var> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : stringObjectMap.entrySet()) {
            result.put(DollarFactory.fromValue(entry.getKey()), DollarFactory.fromValue(entry.getValue()));
        }
        return result;
    }

    public DollarMap(@NotNull ImmutableList<Throwable> errors, @NotNull Map<?, ?> o) {
        super(errors);
        map = mapToVarMap(o);
    }

    private DollarMap(@NotNull ImmutableList<Throwable> errors, @NotNull LinkedHashMap<var, var> o) {
        super(errors);
        this.map = deepClone(o);
    }

    @NotNull private LinkedHashMap<var, var> deepClone(@NotNull LinkedHashMap<var, var> o) {
        LinkedHashMap<var, var> result = new LinkedHashMap<>();
        for (Map.Entry<var, var> entry : o.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public DollarMap(@NotNull ImmutableList<Throwable> errors, @NotNull ImmutableJsonObject immutableJsonObject) {
        super(errors);
        this.map = mapToVarMap(immutableJsonObject.toMap());
    }

    @NotNull
    @Override
    public var $abs() {
        return this;
    }

    @NotNull
    @Override
    public var $minus(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.map()) {
            LinkedHashMap<var, var> copy = copyMap();
            for (Map.Entry<var, var> entry : rhsFix.$map().entrySet()) {
                copy.remove(entry.getKey());
            }
            return DollarFactory.wrap(new DollarMap(errors(), copy));
        } else {
            LinkedHashMap<var, var> copy = copyMap();
            copy.remove(rhsFix.$S());
            return DollarFactory.wrap(new DollarMap(errors(), copy));
        }
    }

    @NotNull
    @Override
    public var $plus(@NotNull var rhs) {
        var rhsFix = rhs._fixDeep();
        if (rhsFix.map()) {
            LinkedHashMap<var, var> copy = copyMap();
            copy.putAll(rhsFix.$map().mutable());
            return DollarFactory.wrap(new DollarMap(errors(), copy));
        } else if (rhsFix.string()) {
            return DollarFactory.fromValue(toHumanString() + rhsFix.toHumanString(), errors(), rhsFix.errors());
        } else {
            LinkedHashMap<var, var> copy = copyMap();
            copy.put(DollarFactory.fromValue(hash(rhsFix.$S().getBytes())), rhsFix);
            return DollarFactory.wrap(new DollarMap(errors(), copy));
        }
    }

    @NotNull
    @Override
    public var $negate() {
        LinkedHashMap<var, var> result = new LinkedHashMap<>();
        final ArrayList<Map.Entry<var, var>> entries = new ArrayList<>(map.entrySet());
        Collections.reverse(entries);
        for (Map.Entry<var, var> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return DollarFactory.fromValue(result, errors());
    }

    @NotNull
    @Override
    public var $divide(@NotNull var rhs) {
        return DollarFactory.failure(ErrorType.INVALID_MAP_OPERATION);
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        return DollarFactory.failure(ErrorType.INVALID_MAP_OPERATION);
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return DollarFactory.failure(ErrorType.INVALID_MAP_OPERATION);
    }

    @NotNull @Override
    public Integer toInteger() {
        DollarFactory.failure(ErrorType.INVALID_MAP_OPERATION);
        return null;
    }

    @NotNull
    @Override
    public Number toNumber() {
        return 0;
    }

    @NotNull
    @Override
    public String toHumanString() {
        return toJsonObject().toString();
    }

    @NotNull
    @Override
    public var $mimeType() {
        return DollarStatic.$("application/json");
    }

    @NotNull @Override public String toDollarScript() {
        StringBuilder builder = new StringBuilder("{");
        for (Map.Entry<var, var> entry : map.entrySet()) {
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
    public <R> R toJavaObject() {
        return (R) varMapToMap();
    }

    @NotNull
    @Override
    public JSONObject toOrgJson() {
        return new JSONObject(varMapToMap());
    }

    @NotNull private Map<Object, Object> varMapToMap() {
        Map<Object, Object> result = new LinkedHashMap<>();
        for (Map.Entry<var, var> entry : $map().entrySet()) {
            result.put(entry.getKey().toJavaObject(), entry.getValue().toJavaObject());
        }
        return result;
    }

    @NotNull private LinkedHashMap<var, var> copyMap() {
        return deepClone(map);
    }

    @Override
    public var $as(@NotNull Type type) {
        if (type.equals(Type.MAP)) {
            return this;
        } else if (type.equals(Type.LIST)) {
            return DollarStatic.$($list());
        } else if (type.equals(Type.BOOLEAN)) {
            return DollarStatic.$(!map.isEmpty());
        } else if (type.equals(Type.STRING)) {
            return DollarFactory.fromStringValue(toHumanString());
        } else if (type.equals(Type.VOID)) {
            return DollarStatic.$void();
        } else {
            return DollarFactory.failure(ErrorType.INVALID_CAST);
        }
    }

    @NotNull
    @Override
    public ImmutableList<var> $list() {
        final List<var> entries =
                map.entrySet()
                   .stream()
                   .map(entry -> DollarStatic.$(entry.getKey(), entry.getValue()))
                   .collect(Collectors.toList());
        return ImmutableList.copyOf(entries);
    }

    @Override public Type $type() {
        return Type.MAP;
    }

    @Override public boolean collection() {
        return true;
    }

    @NotNull
    @Override
    public ImmutableMap<var, var> $map() {

        LinkedHashMap<var, var> result = new LinkedHashMap<>();
        for (Map.Entry<var, var> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue()._fix(false));
        }
        return ImmutableMap.copyOf(result);
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (Objects.equals(type, Type.MAP)) {
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
    public ImmutableList<String> strings() {
        List<String> values = new ArrayList<>();
        Map<Object, Object> map = toMap();
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            values.add(entry.getKey().toString());
            values.add(entry.getValue().toString());
        }
        return ImmutableList.copyOf(values);
    }

    @NotNull @Override public ImmutableList<Object> toList() {
        final ArrayList<Object> entries = new ArrayList<>();
        for (Map.Entry<var, var> entry : map.entrySet()) {
            entries.add(entry.getValue().toJavaObject());
        }
        return ImmutableList.copyOf(entries);
    }

    @NotNull @Override
    public Map<Object, Object> toMap() {
        return varMapToMap();
    }

    @NotNull
    @Override
    public var $get(@NotNull var key) {
        if (key.integer()) {
            final Object mapKey = map.keySet().toArray()[key.toInteger()];
            return DollarStatic.$(mapKey, map.get(mapKey));
        } else {
            return DollarFactory.fromValue(map.get(key), errors());
        }

    }

    @NotNull @Override public var $append(@NotNull var value) {
        final LinkedHashMap<var, var> newMap = new LinkedHashMap<>($map().mutable());
        newMap.put(value.getPairKey(), value.getPairValue());
        return DollarFactory.fromValue(newMap, errors(), value.errors());
    }

    @NotNull public var $containsValue(@NotNull var value) {
        return DollarStatic.$(false);
    }

    @NotNull @Override
    public var $has(@NotNull var key) {
        return DollarStatic.$(map.containsKey(key));
    }

    @NotNull @Override
    public var $size() {
        return DollarStatic.$(toMap().size());
    }

    @NotNull @Override public var $prepend(@NotNull var value) {
        final LinkedHashMap<var, var> newMap = new LinkedHashMap<>();
        newMap.put(value.getPairKey(), value.getPairValue());
        newMap.putAll($map().mutable());
        return DollarFactory.fromValue(newMap, errors(), value.errors());
    }

    @NotNull
    @Override
    public var $removeByKey(@NotNull String value) {
        JsonObject jsonObject = toJsonObject().mutable();
        jsonObject.removeField(value);
        return DollarFactory.fromValue(jsonObject, errors());
    }

    @NotNull
    @Override
    public var $set(@NotNull var key, Object value) {
        LinkedHashMap<var, var> copyMap = copyMap();
        copyMap.put(key, DollarFactory.fromValue(value, errors()));
        return DollarFactory.wrap(new DollarMap(errors(), copyMap));
    }

    @NotNull
    @Override
    public var $remove(var value) {
        return DollarFactory.failure(ErrorType.INVALID_MAP_OPERATION);
    }

    @Override
    public var $listen(Pipeable pipe) {
        String key = UUID.randomUUID().toString();
        $listen(pipe, key);
        return DollarStatic.$(key);
    }

    @Override
    public var $listen(Pipeable pipe, String key) {
        for (var v : map.values()) {
            //Join the children to this, so if the children change
            //listeners to this get the latest value of this.
            v.$listen(i -> this, key);
        }
        return DollarStatic.$(key);
    }

    @NotNull @Override
    public var $notify() {
        map.values().forEach(var::$notify);
        return this;
    }

    @NotNull
    @Override
    public Stream<var> $stream(boolean parallel) {
        return split().values().stream();
    }

    @NotNull
    @Override
    public var _copy() {
        return DollarFactory.wrap(new DollarMap(errors(), map));
    }

    @NotNull @Override public var _fix(int depth, boolean parallel) {
        if (depth <= 1) {
            return this;
        } else {
            LinkedHashMap<var, var> result = new LinkedHashMap<>();
            for (Map.Entry<var, var> entry : map.entrySet()) {
                result.put(entry.getKey(), entry.getValue()._fix(depth - 1, parallel));
            }
            return new DollarMap(errors(), result);
        }
    }

    @Override
    public boolean map() {
        return true;
    }

    @Override public boolean pair() {
        return map.size() == 1;
    }

    @NotNull Map<var, var> split() {
        return copyMap();
    }

    @Override
    public int compareTo(@NotNull var o) {
        return Comparator.<var>naturalOrder().<var>compare(this, o);
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
        return !map.isEmpty();
    }

}


