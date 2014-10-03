package com.cazcade.dollar;

import org.json.JSONObject;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
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
class DollarJson extends AbstractDollar implements com.cazcade.dollar.$ {

    private static ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
    /**
     * Publicly accessible object containing the current state as a JsonObject, if you're working in Vert.x primarily with the JsonObject type you will likely end all chained expressions with '.$'
     * <p/>
     * For example:
     * <code>
     * eb.send("api.validate", $("key", key).$("params", request.params()).$)
     * </code>
     */
    private JsonObject json;

    /**
     * Create a new and empty $ object.
     */
    DollarJson() {
        json = (new JsonObject());
    }

    /**
     * Create a $ object from a variety of different objects. At present the following are supported:<br/>
     * <ul>
     * <li>JsonObject</li>
     * <li>MultiMap</li>
     * <li>Message</li>
     * </ul>
     * <p/>
     * Any other object types will be converted to a string using .toString() and will then be parsed as JSON.
     *
     * @param o the object of unknown type to be converted to a JsonObject and then wrapped by the $ class.
     */
    DollarJson(JsonObject o) {
        this.json= o;
    }

    @Override
    public com.cazcade.dollar.$ $(String key, long value) {
        return DollarFactory.fromValue(json.copy().putNumber(key, value));
    }

    @Override
    public com.cazcade.dollar.$ $(String name, Object o) {
        JsonObject copy = this.json.copy();
        if (o instanceof DollarMonitored) {
            //unwrap
            return $(name, ((DollarMonitored) o).getValue());
        } else if (o instanceof MultiMap) {
            copy.putObject(name, DollarStatic.mapToJson((MultiMap) o));
        } else if (o instanceof JsonArray) {
            copy.putArray(name, (JsonArray) o);
        } else if (o instanceof JsonObject) {
            copy.putObject(name, (JsonObject) o);
        } else if (o instanceof DollarJson) {
            copy.putObject(name, ((DollarJson) o).json);
        } else if (o instanceof DollarNumber) {
            copy.putNumber(name, ((DollarNumber) o).$());
        } else if (o instanceof DollarString) {
            copy.putString(name, ((DollarString) o).$());
        } else if (o instanceof FutureDollar) {
            return $(name, ((FutureDollar) o).then());
        } else {
            copy.putString(name, String.valueOf(o));
        }
        return DollarFactory.fromValue(copy);
    }

    @Override
    public JsonObject $() {
        return json;
    }

    @Override
    public String $$(String key) {
        return $(key).$$();
    }

    @Override
    public $ $(String key) {
        if (key.matches("\\w+")) {
            return DollarFactory.fromField(json.getField(key));
        } else {
            return eval(key);
        }
    }

    @Override
    public Integer $int() {
        throw new UnsupportedOperationException("Cannot convert JSON to an integer");
    }

    @Override
    public Integer $int(String key) {
        return $json().getInteger(key);
    }

    @Override
    public JsonObject $json() {
        return json;
    }

    @Override
    public JsonObject $json(String key) {
        return json.getObject(key);
    }

    @Override
    public List<String> $list() {
        List<String> values = new ArrayList<>();
        Map<String, Object> map = $map();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            values.add(entry.getKey());
            values.add(entry.getValue().toString());
        }
        return values;
    }

    @Override
    public Map<String, Object> $map() {
        return json.toMap();
    }

    @Override
    public Number $number(String key) {
        return $json().getNumber(key);
    }

    @Override
    public JSONObject $orgjson() {
        return new JSONObject(json.toMap());
    }

    @Override
    public $ add(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public java.util.stream.Stream<com.cazcade.dollar.$> children() {
        return split().values().stream();
    }

    @Override
    public Map<String, com.cazcade.dollar.$> split() {
        HashMap<String, com.cazcade.dollar.$> map = new HashMap<>();
        for (String key : json.toMap().keySet()) {
            Object field = json.getField(key);
            if (field instanceof JsonObject) {
                map.put(key, DollarFactory.fromField(field));
            }
        }
        return map;
    }

    @Override
    public java.util.stream.Stream children(String key) {
        List list = json.getArray(key).toList();
        if (list == null) {
            return null;
        }
        return list.stream();
    }

    @Override
    public $ decode() {
        return new DollarString(URLDecoder.decode($$()));
    }

    @Override
    public String $$() {
        return toString();
    }

    @Override
    public String toString() {
        return json.toString();
    }

    @Override
    public $ eval(String label, String js) {
        try {
            SimpleScriptContext context = new SimpleScriptContext();
            context.setAttribute("$", json.toMap(), context.getScopes().get(0));
            return DollarFactory.fromValue(nashorn.eval(js, context));
        } catch (ScriptException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public $ eval(String label, DollarEval lambda) {
        return lambda.eval(copy());
    }

    @Override
    public $ copy() {
        return DollarFactory.fromValue(json.copy());
    }

    @Override
    public boolean has(String key) {
        return $json().containsField(key);
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public java.util.stream.Stream<Map.Entry<String, com.cazcade.dollar.$>> keyValues() {
        return split().entrySet().stream();
    }

    @Override
    public java.util.stream.Stream<String> keys() {
        return json.getFieldNames().stream();
    }

    @Override
    public $ remove(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public com.cazcade.dollar.$ rm(String value) {
        json.removeField(value);
        return this;
    }

    @Override
    public FutureDollar<JsonObject> send(EventBus e, String destination) {
        FutureDollar<JsonObject> futureDollar = new FutureDollar<>(this);
        e.send(destination, json, (Message<JsonObject> message) -> {
            futureDollar.handle(message);
        });
        return futureDollar;
    }

    @Override
    public List<String> splitValues() {
        List<String> list = new ArrayList<>();
        for (String key : json.toMap().keySet()) {
            String field = json.getField(key).toString();
            list.add(field);
        }
        return list;
    }

    @Override
    public Stream<$> stream() {
        return split().values().stream();
    }

    @Override
    public JsonObject val() {
        return json;
    }

    public com.cazcade.dollar.$ ¢(String key) {
        return child(key);
    }

    public com.cazcade.dollar.$ child(String key) {
        JsonObject child = json.getObject(key);
        if (child == null) {
            return null;
        }
        return DollarFactory.fromValue(child);
    }

}


