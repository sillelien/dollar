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
import me.neilellis.dollar.DollarEval;
import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.json.ImmutableJsonObject;
import me.neilellis.dollar.json.JsonObject;
import me.neilellis.dollar.var;
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
class DollarMap extends AbstractDollar implements var {

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

    public DollarMap(ImmutableList<Throwable> errors, Map<String, Object> o) {
        super(errors);
        map = mapToVarMap(o);
    }

    private DollarMap(ImmutableList<Throwable> errors, LinkedHashMap<String, var> o) {
        super(errors);
        this.map = deepClone(o);
    }

    public DollarMap(ImmutableList<Throwable> errors, ImmutableJsonObject immutableJsonObject) {
        super(errors);
        this.map = mapToVarMap(immutableJsonObject.toMap());
    }

    private LinkedHashMap<String, var> deepClone(LinkedHashMap<String, var> o) {
        LinkedHashMap<String, var> result = new LinkedHashMap<String, var>();
        for (Entry<String, var> entry : o.entrySet()) {
            result.put(entry.getKey(), entry.getValue().$copy());
        }
        return result;
    }

    private LinkedHashMap<String, var> mapToVarMap(Map<String, Object> stringObjectMap) {
        LinkedHashMap<String, var> result = new LinkedHashMap<String, var>();
        for (Entry<String, Object> entry : stringObjectMap.entrySet()) {
            result.put(entry.getKey(), DollarFactory.fromValue(entry.getValue()));
        }
        return result;
    }

    @NotNull
    @Override
    public var $(@NotNull String key, long value) {
        return $(key, (Object) value);
    }

    private LinkedHashMap<String, var> copyMap() {
        return deepClone(map);
    }

    @NotNull
    @Override
    public var $(@NotNull String key, double value) {
        return $(key, (Object) value);
    }


    @NotNull
    @Override
    public var $plus(Object value) {
        LinkedHashMap<String, var> copy = copyMap();
        if (value instanceof var) {
            copy.putAll(((var) value).$map().mutable());
            return DollarFactory.wrap(new DollarMap(errors(), copy));
        }
        throw new IllegalArgumentException("Only the addition of DollarJson objects supported at present.");

    }

    @NotNull
    @Override
    public Stream<var> $children() {
        return split().values().stream();
    }

    @Override
    public var $dec(long amount) {
        return this;
    }

    @Override
    public var $inc(long amount) {
        return this;
    }

    @NotNull
    public Map<String, var> split() {
        return copyMap();
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

    @Override
    public boolean $has(@NotNull String key) {
        return map.containsKey(key);
    }

    @NotNull
    @Override
    public ImmutableList<var> toList() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {

        return ImmutableMap.<String, var>copyOf(map);
    }

    @NotNull
    @Override
    public String S(@NotNull String key) {
        return $(key).S();
    }

    @NotNull
    @Override
    public var $rm(@NotNull String value) {
        JsonObject jsonObject = json();
        jsonObject.removeField(value);
        return DollarFactory.fromValue(errors(), jsonObject);
    }

    @NotNull
    @Override
    public var $minus(@NotNull Object value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_MAP_OPERATION);
    }

    @NotNull
    @Override
    public var $(@NotNull String key, Object value) {
        LinkedHashMap<String, var> copyMap = copyMap();
        copyMap.put(key, DollarFactory.fromValue(errors(), value));
        return DollarFactory.wrap(new DollarMap(errors(), copyMap));
    }

    @Override
    public boolean isVoid() {
        return false;
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
    public var decode() {
        return DollarFactory.fromValue(errors(), URLDecoder.decode(S()));
    }

    @NotNull
    @Override
    public var $(@NotNull String key) {
        return DollarFactory.fromValue(errors(), map.get(key));
    }

    @Override
    public boolean isMap() {
        return true;
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
    public <R> R $() {
        return (R) json();
    }

    @Override
    public java.util.stream.Stream<String> keyStream() {
        return map.keySet().stream();
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

    @NotNull
    @Override
    public Number N() {
        return 0;
    }

    private Map<String, Object> varMapToMap() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Entry<String, var> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue().$());
        }
        return result;
    }

    @Override
    public <R> R val() {
        return (R) map;
    }

    @NotNull
    @Override
    public var eval(String label, @NotNull DollarEval lambda) {
        return lambda.eval($copy());
    }

    @NotNull
    @Override
    public String $mimeType() {
        return "application/json";
    }

    @Override
    public java.util.stream.Stream<Map.Entry<String, var>> kvStream() {
        return split().entrySet().stream();
    }

    @NotNull
    @Override
    public Stream<var> $stream() {
        return split().values().stream();
    }

    @NotNull
    @Override
    public var $copy() {
        return DollarFactory.wrap(new DollarMap(errors(), map));
    }

    @Override
    public int size() {
        return toMap().size();
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @NotNull
    @Override
    public var remove(Object value) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public String S() {
        return json().toString();
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

    @Override
    public void $notify(var value) {
        for (var v : map.values()) {
            v.$notify(value);
        }
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


    @Override
    public String $listen(Pipeable pipe) {
        String key = UUID.randomUUID().toString();
        $listen(pipe, key);
        return key;
    }
}


