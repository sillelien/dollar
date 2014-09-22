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
public class $ {

    /**
     * Publicly accessible object containing the current state as a JsonObject, if you're working in Vert.x primarily with the JsonObject type you will likely end all chained expressions with '.$'
     *
     * For example:
     * <code>
     *      eb.send("api.validate", $("key", key).$("params", request.params()).$)
     * </code>
     */
    public JsonObject $;

    /**
     * Create a new and empty $ object.
     */
    $() {
        $ = (new JsonObject());
    }

    /**
     * Create a $ object from a variety of different objects. At present the following are supported:<br/>
     * <ul>
     *     <li>JsonObject</li>
     *     <li>MultiMap</li>
     *     <li>Message</li>
     * </ul>
     *
     * Any other object types will be converted to a string using .toString() and will then be parsed as JSON.
     *
     * @param o the object of unknown type to be converted to a JsonObject and then wrapped by the $ class.
     */
    $(Object o) {
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

    public $  $(String age, long l) {
        $.putNumber(age,l);
        return this;
    }

    public Map<String,$> split() {
        HashMap<String, com.cazcade.dollar.$> map = new HashMap<String, $>();
        for(String key : $.toMap().keySet()) {
            Object field = $.getField(key);
            if(field instanceof JsonObject) {
                map.put(key, new $(field));
            }
        }
        return map;
    }

    public List<String> splitValues() {
        List<String> list= new ArrayList<>();
        for(String key : $.toMap().keySet()) {
            String field = $.getField(key).toString();
            list.add(field);
        }
        return list;
    }

    private JsonObject mapToJson(MultiMap map) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, String> entry : map) {
            jsonObject.putString(entry.getKey(), entry.getValue());
        }
        return jsonObject;
    }

    public String $(String key) {
        return $.getField(key).toString();
    }

    public com.cazcade.dollar.$ $(String name, MultiMap multiMap) {
        $.putObject(name, mapToJson(multiMap));
        return this;
    }

    public com.cazcade.dollar.$ $(String name, JsonArray value) {
        $.putArray(name, value);
        return this;
    }

    public com.cazcade.dollar.$ $(String key, com.cazcade.dollar.$ value) {
        return $(key, value.$json());
    }

    public com.cazcade.dollar.$ $(String key, JsonObject jsonObject) {
        $.putObject(key, jsonObject);
        return this;
    }

    /**
     * Convert this to a Vert.x JsonObject - equivalent to .$
     *
     * @return this JSON as a JsonObject
     */
    public JsonObject $json() {
        return $;
    }

    /**
     * Builder method for creating a new key/value pair in this object.
     *
     * @param key the key
     * @param value the String value
     * @return this
     */
    public com.cazcade.dollar.$ $(String key, String value) {
        $.putString(key, value);
        return this;
    }

    /**
     * Shorthand for .$.toString()
     * @return this JSON as a string.
     */
    public String $() {
        return $.toString();
    }

    /**
     * Equivalent returns a Vert.x JsonObject child object value for the supplied key.
     *
     * @param key the key
     * @return a JsonObject
     */
    public JsonObject $json(String key) {
        return $.getObject(key);
    }

    /**
     * Returns the value for the supplied key as an Integer.
     *
     * @param key the key
     * @return an Integer value (or null).
     */
    public Integer $int(String key) {
        return $json().getInteger(key);
    }

    /**
     * Returns this JSON object as a set of nested maps.
     *
     * @return a nested Map
     */
    public Map<String, Object> $map() {
        return $.toMap();
    }

    /**
     * Returns the value for the supplied key as a general Number.
     *
     * @param key the key
     * @return a Number
     */
    public Number $number(String key) {
        return $json().getNumber(key);
    }

    /**
     * Returns this JSON as a org.json.JSONObject, which can be used
     * with none Vert.x APIs etc. This conversion is quite efficient.
     *
     * @return a JSONObject
     */
    public JSONObject $orgjson() {
        return new JSONObject($.toMap());
    }

    /**
     * Returns a deep copy of this object, such that mutations to it
     * does not effect this.
     *
     * @return a deep copy of this
     */
    public com.cazcade.dollar.$ copy() {
        return new com.cazcade.dollar.$($.copy());
    }

    /**
     * Returns true if this JSON object has the supplied key.
     *
     * @param key the key
     * @return true if the key exists.
     */
    public boolean has(String key) {
        return $json().containsField(key);
    }

    public com.cazcade.dollar.$ rm(String value) {
        $.removeField(value);
        return this;
    }

    public java.util.stream.Stream<Map.Entry<String, com.cazcade.dollar.$>> keyValues() {
        return split().entrySet().stream();
    }

    public java.util.stream.Stream<String> keys() {
        return $.getFieldNames().stream();
    }

    public java.util.stream.Stream<com.cazcade.dollar.$> children() {
        return split().values().stream();
    }

    public java.util.stream.Stream children(String key) {
        return $.getArray(key).toList().stream();
    }

    @Override
    public String toString() {
        return $.toString();
    }

    public com.cazcade.dollar.$ ¢(String key) {
        return child(key);
    }

    public com.cazcade.dollar.$ child(String key) {
        JsonObject child = $.getObject(key);
        if (child == null) {
            return null;
        }
        return new com.cazcade.dollar.$(child);
    }

    public FutureDollar send(EventBus e, String destination) {
        FutureDollar futureDollar = new FutureDollar(this);
        e.send(destination,$,(Message<JsonObject> message)-> {
            futureDollar.handle(message);
        });
        return futureDollar;
    }
}


