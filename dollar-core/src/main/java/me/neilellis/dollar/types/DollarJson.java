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
import com.google.common.collect.Multimap;
import me.neilellis.dollar.DollarEval;
import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.json.ImmutableJsonObject;
import me.neilellis.dollar.json.JsonArray;
import me.neilellis.dollar.json.JsonObject;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


/**
 * The $ class is the class used to hold a JsonObject data structure. It can be used for managing
 * other data types too by converting them to JsonObject and back.
 *
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
class DollarJson extends AbstractDollar implements var {

    private static ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
    /**
     * Publicly accessible object containing the current state as a JsonObject, if you're working in Vert.x primarily with the JsonObject type you will likely end all chained expressions with '.$'
     *
     * For example:
     * <code>
     * eb.send("api.validate", $("key", key).$("params", request.params()).$)
     * </code>
     */
    private final @NotNull
    ImmutableJsonObject json;

    /**
     * Create a new and empty $ object.
     * @param errors
     */
    DollarJson(@NotNull ImmutableList<Throwable> errors) {
        super(errors);
        json = new ImmutableJsonObject(new JsonObject());
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
    DollarJson(@NotNull ImmutableList<Throwable> errors, @NotNull JsonObject o) {
        super(errors);
        this.json = new ImmutableJsonObject(o);
    }

    public DollarJson(ImmutableList<Throwable> errors, ImmutableJsonObject immutableJsonObject) {
        super(errors);
        this.json = immutableJsonObject;
    }

    @NotNull
    @Override
    public var $(@NotNull String key, long value) {
        return DollarFactory.fromValue(errors(), json().putNumber(key, value));
    }

    @NotNull
    @Override
    public var $(@NotNull String key, double value) {
        return DollarFactory.fromValue(errors(), json().putNumber(key, value));
    }


    @NotNull
    @Override
    public var $append(Object value) {
        JsonObject copy = json();
        if (value instanceof var && ((var) value).$() instanceof JsonObject) {
            return DollarFactory.fromValue(errors(), copy.mergeIn(((var) value).$()));
        }
        throw new IllegalArgumentException("Only the addition of DollarJson objects supported at present.");
    }

    @NotNull
    @Override
    public Stream<var> $children() {
        return split().values().stream();
    }

    @NotNull
    public Map<String, var> split() {
        HashMap<String, var> map = new HashMap<>();
        ImmutableJsonObject jsonObject = json;
        for (String key : jsonObject.toMap().keySet()) {
            Object field = jsonObject.getField(key);
            if (field instanceof JsonObject) {
                map.put(key, DollarFactory.fromField(errors(), field));
            }
        }
        return map;
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
        return json.containsField(key);
    }

    @NotNull
    @Override
    public ImmutableList<var> toList() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {
        Map<String, var> result = new HashMap<>();
        Map<String, Object> objectMap = json.toMap();
        for (Entry<String, Object> entry : objectMap.entrySet()) {
            result.put(entry.getKey(), DollarFactory.fromValue(errors(), entry.getValue()));
        }
        return ImmutableMap.<String, var>copyOf(result);
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
    public var $(@NotNull String name, Object o) {
        JsonObject copy = this.json();
        if (o instanceof DollarWrapper) {
            //unwrap
            return $(name, ((DollarWrapper) o).getValue());
        } else if (o instanceof Multimap) {
            copy.putObject(name, DollarStatic.mapToJson((Multimap) o));
        } else if (o instanceof JsonArray) {
            copy.putArray(name, (JsonArray) o);
        } else if (o instanceof JsonObject) {
            copy.putObject(name, ((JsonObject) o).copy());
        } else if (o instanceof DollarJson) {
            copy.putObject(name, ((DollarJson) o).json());
        } else if (o instanceof String) {
            copy.putString(name, (String) o);
        } else if (o instanceof Number) {
            copy.putNumber(name, (Number) o);
        } else if (o instanceof DollarNumber) {
            copy.putNumber(name, ((DollarNumber) o).$());
        } else if (o instanceof DollarString) {
            copy.putString(name, ((DollarString) o).$());
        } else {
            copy.putString(name, String.valueOf(o));
        }
        return DollarFactory.fromValue(errors(), copy);
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
        return json.getInteger(key);
    }

    @NotNull
    @Override
    public var decode() {
        return DollarFactory.fromValue(errors(), URLDecoder.decode(S()));
    }

    @NotNull
    @Override
    public var $(@NotNull String key) {
        return DollarFactory.fromField(errors(), json.getField(key));
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
        return json.getFieldNames().stream();
    }

    @Override
    public Number number(@NotNull String key) {
        return json.getNumber(key);
    }

    @NotNull
    @Override
    public JSONObject orgjson() {
        return new JSONObject(json.toMap());
    }

    @NotNull
    @Override
    public JsonObject json() {
        return json.mutable();
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
        return json.toMap();
    }

    @Override
    public <R> R val() {
        return (R) json();
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
        return DollarFactory.fromValue(errors(), json());
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
    public String toString() {
        return json.toString();
    }
}


