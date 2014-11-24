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
import me.neilellis.dollar.json.ImmutableJsonObject;
import me.neilellis.dollar.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Stream;


/**
 * The $ class is the class used to hold a JsonObject data structure. It can be used for managing
 * other data types too by converting them to JsonObject and back.
 *
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarMap extends AbstractDollar implements var {

    private static ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
    /**
     * Publicly accessible object containing the current state as a JsonObject, if you're working in Vert.x primarily with the JsonObject type you will likely end all chained expressions with '.$'
     *
     * For example:
     * <code>
     * eb.send("api.validate", $("key", key).$("params", request.params()).$)
     * </code>
     */
    private final
    @NotNull
    LinkedHashMap<String, var> map;

    /**
     * Create a new and empty $ object.
     *
     * @param errors
     */
    DollarMap(@NotNull ImmutableList<Throwable> errors) {
        super(errors);
        map = new LinkedHashMap<String, var>();
    }

    /**
     * Create a $ object from a variety of different objects. At present the following are supported:<br/>
     * <ul>
     * <li>JsonObject</li>
     * <li>MultiMap</li>
     * <li>Message</li>
     * </ul>
     *
     * Any other object types will be converted to a string using .toString() and will then be parsed as JSON.
     *
     * @param o the object of unknown type to be converted to a JsonObject and then wrapped by the $ class.
     */
    DollarMap(@NotNull ImmutableList<Throwable> errors, @NotNull JsonObject o) {
        super(errors);
        map = mapToVarMap(o.toMap());
    }

    private LinkedHashMap<String, var> mapToVarMap(Map<String, Object> stringObjectMap) {
        LinkedHashMap<String, var> result = new LinkedHashMap<String, var>();
        for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
            result.put(entry.getKey(), DollarFactory.fromValue(entry.getValue()));
        }
        return result;
    }

    public DollarMap(ImmutableList<Throwable> errors, Map<String, Object> o) {
        super(errors);
        map = mapToVarMap(o);
    }

    private DollarMap(ImmutableList<Throwable> errors, LinkedHashMap<String, var> o) {
        super(errors);
        this.map = deepClone(o);
    }

    private LinkedHashMap<String, var> deepClone(LinkedHashMap<String, var> o) {
        LinkedHashMap<String, var> result = new LinkedHashMap<String, var>();
        for (Map.Entry<String, var> entry : o.entrySet()) {
            result.put(entry.getKey(), entry.getValue().$copy());
        }
        return result;
    }

    public DollarMap(ImmutableList<Throwable> errors, ImmutableJsonObject immutableJsonObject) {
        super(errors);
        this.map = mapToVarMap(immutableJsonObject.toMap());
    }

    @NotNull
    @Override
    public var $(@NotNull var key, Object value) {
        LinkedHashMap<String, var> copyMap = copyMap();
        copyMap.put(key.$S(), DollarFactory.fromValue(value, errors()));
        return DollarFactory.wrap(new DollarMap(errors(), copyMap));
    }

    @NotNull
    @Override
    public var $get(@NotNull String key) {
        return DollarFactory.fromValue(map.get(key), errors());
    }

    @NotNull
    @Override
    public <R> R $() {
        return (R) json();
    }

    @NotNull
    @Override
    public var $(@NotNull Number n) {
        String key = map.keySet().toArray()[n.intValue()].toString();
        return DollarStatic.$(key, map.get(key));
    }

    @NotNull
    @Override
    public Stream<var> $children() {
        return split().values().stream();
    }

    @NotNull
    public Map<String, var> split() {
        return copyMap();
    }

    private LinkedHashMap<String, var> copyMap() {
        return deepClone(map);
    }

    @NotNull
    @Override
    public Stream<var> $children(@NotNull String key) {
        throw new UnsupportedOperationException("Not implemented correctly yet.");
//        List<var> list = json().getArray(key).toList();
//        if (list == null) {
//            return Stream.empty();
//        }
//        return list.stream();
    }

    public var $containsValue(var value) {
        return DollarStatic.$(false);
    }

    @Override
    public var $has(@NotNull String key) {
        return DollarStatic.$(map.containsKey(key));
    }

    @NotNull
    @Override
    public String S(@NotNull String key) {
        return $get(key).S();
    }

    @NotNull
    @Override
    public var $minus(@NotNull var value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_MAP_OPERATION);
    }

    @NotNull
    @Override
    public var $plus(var value) {
        LinkedHashMap<String, var> copy = copyMap();
        if (value instanceof var) {
            copy.putAll(((var) value).$map().mutable());
            return DollarFactory.wrap(new DollarMap(errors(), copy));
        }
        throw new IllegalArgumentException("Only the addition of DollarJson objects supported at present.");

    }

    @NotNull
    @Override
    public var $rm(@NotNull String value) {
        JsonObject jsonObject = json();
        jsonObject.removeField(value);
        return DollarFactory.fromValue(jsonObject, errors());
    }

    @Override
    public var $size() {
        return DollarStatic.$(toMap().size());
    }

    @Override
    public java.util.stream.Stream<String> keyStream() {
        return map.keySet().stream();
    }

    @NotNull
    @Override
    public var remove(Object value) {
        throw new UnsupportedOperationException();
    }

    private Map<String, Object> varMapToMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, var> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue().$());
        }
        return result;
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
        return DollarFactory.failure(DollarFail.FailureType.INVALID_MAP_OPERATION);
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_MAP_OPERATION);
    }

    @NotNull
    @Override
    public var $divide(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_MAP_OPERATION);
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_MAP_OPERATION);
    }

    @NotNull
    @Override
    public var $abs() {
        return this;
    }

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {

        return ImmutableMap.<String, var>copyOf(map);
    }

    @NotNull
    @Override
    public String S() {
        return json().toString();
    }

    @Override
    public var $as(Type type) {
        switch (type) {
            case MAP:
                return this;
            case LIST:
                return DollarStatic.$(toList());
            case BOOLEAN:
                return DollarStatic.$(!map.isEmpty());
            case STRING:
                return DollarFactory.fromStringValue(S());
            case VOID:
                return DollarStatic.$void();
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public Integer I() {
        throw new UnsupportedOperationException("Cannot convert JSON to an integer");
    }

    @Override
    public Integer I(@NotNull String key) {
        return map.get(key).I();
    }

    @NotNull
    @Override
    public Number N() {
        return 0;
    }

    @Override
    public boolean is(Type... types) {
        for (Type type : types) {
            if (type == Type.MAP) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public JsonObject json(@NotNull String key) {
        JsonObject object = json().getObject(key);
        if (object == null) {
            return null;
        }
        return object;
    }

    @NotNull
    @Override
    public ImmutableList<var> toList() {
        final ArrayList<var> entries = new ArrayList<>();
        for (Map.Entry<String, var> entry : map.entrySet()) {
            entries.add(DollarStatic.$(entry.getKey(), entry.getValue()));
        }
        return ImmutableList.copyOf(entries);
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public Number number(@NotNull String key) {
        return map.get(key).N();
    }

    @NotNull
    @Override
    public JSONObject orgjson() {
        return new JSONObject(varMapToMap());
    }

    @NotNull
    @Override
    public JsonObject json() {
        return new JsonObject(varMapToMap());
    }

    @Override
    public ImmutableList<String> strings() {
        List<String> values = new ArrayList<>();
        Map<String, Object> map = toMap();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            values.add(entry.getKey());
            values.add(entry.getValue().toString());
        }
        return ImmutableList.copyOf(values);
    }

    @Override
    public Map<String, Object> toMap() {
        return varMapToMap();
    }

    @NotNull @Override public var _fix(boolean parallel) {
        HashMap<String, var> result = new HashMap<>();
        for (Map.Entry<String, var> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue()._fix(false));
        }
        return DollarStatic.$(result);
    }

    @NotNull
    @Override
    public var $mimeType() {
        return DollarStatic.$("application/json");
    }

    @NotNull
    @Override
    public Stream<var> $stream(boolean parallel) {
        return split().values().stream();
    }

    @NotNull
    @Override
    public java.util.stream.Stream<Map.Entry<String, var>> kvStream() {
        return split().entrySet().stream();
    }

    @Override
    public String $listen(Pipeable pipe) {
        String key = UUID.randomUUID().toString();
        $listen(pipe, key);
        return key;
    }

    @Override
    public var $notify() {
        for (var v : map.values()) {
            v.$notify();
        }
        return this;
    }

    @Override
    public String $listen(Pipeable pipe, String key) {
        for (var v : map.values()) {
            //Join the children to this, so if the children change
            //listeners to this get the latest value of this.
            v.$listen(i -> this, key);
        }
        return key;
    }

    @NotNull
    @Override
    public var eval(String label, @NotNull DollarEval lambda) {
        return lambda.eval($copy());
    }

    @Override
    public boolean isMap() {
        return true;
    }

    @NotNull
    @Override
    public var $copy() {
        return DollarFactory.wrap(new DollarMap(errors(), map));
    }

    @Override public boolean isPair() {
        return map.size() == 1;
    }

    @Override
    public int compareTo(var o) {
        return Comparator.<var>naturalOrder().<var>compare(this, o);
    }

    @NotNull
    @Override
    public var decode() {
        return DollarFactory.fromValue(URLDecoder.decode(S()), errors());
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
        return !map.isEmpty();
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isNeitherTrueNorFalse() {
        return true;
    }
}


