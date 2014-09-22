package com.cazcade.dollar;

import org.json.JSONObject;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

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
    public JsonObject $;

    /**
     * Create a new and empty $ object.
     */
    DollarJson() {
        $ = (new JsonObject());
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
    DollarJson(Object o) {
        if(o == null) {
            throw new NullPointerException();
        }
        if (o instanceof JsonObject) {
            $ = ((JsonObject) o);
        }
        if (o instanceof JsonObject) {
            $ = ((JsonObject) o);
        } else if (o instanceof MultiMap) {
            $ = mapToJson((MultiMap) o);
        } else if (o instanceof Map) {
            $ = new JsonObject((Map<String, Object>) o);
        } else if (o instanceof Message) {
            $ = ((JsonObject) ((Message) o).body());
        } else {
            $ = new JsonObject(o.toString());
        }
    }

    private JsonObject mapToJson(MultiMap map) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, String> entry : map) {
            jsonObject.putString(entry.getKey(), entry.getValue());
        }
        return jsonObject;
    }

    @Override
    public boolean isNull() {
        return false; //TODO
    }

    @Override
    public com.cazcade.dollar.$<JsonObject> $(String key, long value) {
        $.putNumber(key, value);
        return this;
    }

    @Override
    public $ $(String key) {
        return DollarFactory.fromField($.getField(key));
    }

    @Override
    public String $$(String key) {
        return $(key).$$();
    }

    @Override
    public com.cazcade.dollar.$<JsonObject> $(String name, MultiMap multiMap) {
        $.putObject(name, mapToJson(multiMap));
        return this;
    }

    @Override
    public com.cazcade.dollar.$<JsonObject> $(String name, JsonArray value) {
        $.putArray(name, value);
        return this;
    }

    @Override
    public com.cazcade.dollar.$<JsonObject> $(String key, com.cazcade.dollar.$ value) {
        return $child(key, value.$json());
    }

    @Override
    public com.cazcade.dollar.$<JsonObject> $child(String key, JsonObject jsonObject) {
        $.putObject(key, jsonObject);
        return this;
    }

    @Override
    public JsonObject $json() {
        return $;
    }

    @Override
    public com.cazcade.dollar.$<JsonObject> $(String key, String value) {
        $.putString(key, value);
        return this;
    }

    @Override
    public JsonObject $() {
        return $;
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
        return $.getObject(key);
    }

    @Override
    public Map<String, Object> $map() {
        return $.toMap();
    }

    @Override
    public Number $number(String key) {
        return $json().getNumber(key);
    }

    @Override
    public JSONObject $orgjson() {
        return new JSONObject($.toMap());
    }

    @Override
    public java.util.stream.Stream<com.cazcade.dollar.$<JsonObject>> children() {
        return split().values().stream();
    }

    @Override
    public java.util.stream.Stream children(String key) {
        List list = $.getArray(key).toList();
        if(list == null) {
            return null;
        }
        return list.stream();
    }

    @Override
    public DollarJson copy() {
        return new DollarJson($.copy());
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
        for (String key : $.toMap().keySet()) {
            Object field = $.getField(key);
            if (field instanceof JsonObject) {
                map.put(key, DollarFactory.fromField(field));
            }
        }
        return map;
    }

    @Override
    public java.util.stream.Stream<String> keys() {
        return $.getFieldNames().stream();
    }

    @Override
    public com.cazcade.dollar.$<JsonObject> rm(String value) {
        $.removeField(value);
        return this;
    }

    @Override
    public FutureDollar<JsonObject> send(EventBus e, String destination) {
        FutureDollar<JsonObject> futureDollar = new FutureDollar<>(this);
        e.send(destination, $, (Message<JsonObject> message) -> {
            futureDollar.handle(message);
        });
        return futureDollar;
    }

    @Override
    public List<String> splitValues() {
        List<String> list = new ArrayList<>();
        for (String key : $.toMap().keySet()) {
            String field = $.getField(key).toString();
            list.add(field);
        }
        return list;
    }

    @Override
    public String toString() {
        return $.toString();
    }

    @Override
    public JsonObject val() {
        return $;
    }


    public com.cazcade.dollar.$<JsonObject>¢(String key) {
        return child(key);
    }


    public com.cazcade.dollar.$<JsonObject> child(String key) {
        JsonObject child = $.getObject(key);
        if (child == null) {
            return null;
        }
        return new DollarJson(child);
    }
}


