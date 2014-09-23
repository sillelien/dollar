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

/**
 * The $ class is the class used to hold a JsonObject data structure. It can be used for managing
 * other data types too by converting them to JsonObject and back.
 *
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
class DollarJson implements com.cazcade.dollar.$<JsonObject> {

    /**
     * Publicly accessible object containing the current state as a JsonObject, if you're working in Vert.x primarily with the JsonObject type you will likely end all chained expressions with '.$'
     * <p/>
     * For example:
     * <code>
     * eb.send("api.validate", $("key", key).$("params", request.params()).$)
     * </code>
     */
    private JsonObject json;
    private static ScriptEngine nashorn   = new ScriptEngineManager().getEngineByName("nashorn");;

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
    public Integer $int() {
        throw new UnsupportedOperationException("Cannot convert JSON to an integer");
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public com.cazcade.dollar.$<JsonObject> $(String key, long value) {
        json.putNumber(key, value);
        return this;
    }

    @Override
    public $ $(String key) {
        if(key.matches("\\w+")) {
            return DollarFactory.fromField(json.getField(key));
        } else {

            try {
                SimpleScriptContext context = new SimpleScriptContext();
                context.setAttribute("$",json.toMap(),context.getScopes().get(0));
                return DollarFactory.fromValue(nashorn.eval(key, context));
            } catch (ScriptException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String $$(String key) {
        return $(key).$$();
    }

    @Override
    public com.cazcade.dollar.$<JsonObject> $(String name, Object o) {
        if (o instanceof MultiMap) {
            json.putObject(name, DollarStatic.mapToJson((MultiMap) o));
        } else if (o instanceof JsonArray) {
            json.putArray(name, (JsonArray) o);
        } else if (o instanceof JsonObject) {
            json.putObject(name, (JsonObject) o);
            return this;
        } else if (o instanceof DollarJson) {
            json.putObject(name, ((DollarJson) o).$json());
        } else if (o instanceof DollarNumber) {
            json.putNumber(name, ((DollarNumber) o).$());
        } else if (o instanceof DollarString) {
            json.putString(name, ((DollarString) o).$());
        } else if (o instanceof FutureDollar) {
            $(name, ((FutureDollar) o).then());
        } else {
            json.putString(name, String.valueOf(o));
        }
        return this;
    }

    @Override
    public JsonObject $json() {
        return json;
    }


    @Override
    public JsonObject $() {
        return json;
    }

    @Override
    public String $$() {
        return toString();
    }

    @Override
    public Integer $int(String key) {
        return $json().getInteger(key);
    }

    @Override
    public JsonObject $json(String key) {
        return json.getObject(key);
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
    public java.util.stream.Stream<com.cazcade.dollar.$<JsonObject>> children() {
        return split().values().stream();
    }

    @Override
    public java.util.stream.Stream children(String key) {
        List list = json.getArray(key).toList();
        if(list == null) {
            return null;
        }
        return list.stream();
    }

    @Override
    public DollarJson copy() {
        return new DollarJson(json.copy());
    }

    @Override
    public boolean has(String key) {
        return $json().containsField(key);
    }

    @Override
    public java.util.stream.Stream<Map.Entry<String, com.cazcade.dollar.$<JsonObject>>> keyValues() {
        return split().entrySet().stream();
    }

    @Override
    public Map<String, com.cazcade.dollar.$<JsonObject>> split() {
        HashMap<String, com.cazcade.dollar.$<JsonObject>> map = new HashMap<>();
        for (String key : json.toMap().keySet()) {
            Object field = json.getField(key);
            if (field instanceof JsonObject) {
                map.put(key, DollarFactory.fromField(field));
            }
        }
        return map;
    }

    @Override
    public java.util.stream.Stream<String> keys() {
        return json.getFieldNames().stream();
    }

    @Override
    public com.cazcade.dollar.$<JsonObject> rm(String value) {
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
    public String toString() {
        return json.toString();
    }

    @Override
    public JsonObject val() {
        return json;
    }


    public com.cazcade.dollar.$<JsonObject>¢(String key) {
        return child(key);
    }


    public com.cazcade.dollar.$<JsonObject> child(String key) {
        JsonObject child = json.getObject(key);
        if (child == null) {
            return null;
        }
        return new DollarJson(child);
    }

    @Override
    public $ decode() {
        return new DollarString(URLDecoder.decode($$()));
    }
}


