package com.cazcade.dollar;

import org.json.JSONObject;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

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
      * Create a new $ class from the supplied JsonObject. This will just directly wrap the supplied JsonObject.
      * <b>Important, it does not copy the object, so operations will directly modify the supplied value, use the .copy() method on the JsonObject first if you do not wish this to occur.</b>
     */
    $(JsonObject json) {
        if (json == null) {
            throw new NullPointerException("Null Json");
        }
        $ = (json);
    }

    /**
     * Create a new $ object from the supplied JSON string, this uses the JsonObject(String) constructor to parse the Json.
     * @param jsonStr a string in JSON format, e.g. {foo:'bar'}
     */
    $(String jsonStr) {
        $ = (new JsonObject(jsonStr));
    }

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

    public static JsonArray $array(Object... values) {
        return new JsonArray(values);
    }

    public static com.cazcade.dollar.$ $new(JsonObject json) {
        return new com.cazcade.dollar.$(json);
    }

    public static com.cazcade.dollar.$ $new(String json) {
        return new com.cazcade.dollar.$(json);
    }

    public static com.cazcade.dollar.$ $new(Object o) {

        return new com.cazcade.dollar.$(o);
    }

    public String $(String key) {
        return $.getString(key);
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

    public JsonObject $json() {
        return $;
    }

    public com.cazcade.dollar.$ $(String key, String value) {
        $.putString(key, value);
        return this;
    }

    public String $() {
        return $.toString();
    }

    public com.cazcade.dollar.$ $$(String json) {
        return new com.cazcade.dollar.$($json(json));
    }

    public JsonObject $json(String key) {
        return $.getObject(key);
    }

    public Integer $int(String key) {
        return $json().getInteger(key);
    }

    public Map<String, Object> $map() {
        return $.toMap();
    }

    public Number $number(String key) {
        return $json().getNumber(key);
    }

    public JSONObject $orgjson() {
        return new JSONObject($.toMap());
    }

    public com.cazcade.dollar.$ copy() {
        return new com.cazcade.dollar.$($.copy());
    }

    public boolean has(String key) {
        return $json().containsField(key);
    }

    public com.cazcade.dollar.$ rm(String value) {
        $.removeField(value);
        return this;
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
}


