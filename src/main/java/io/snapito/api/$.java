package io.snapito.api;

import org.json.JSONObject;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 *
 */
public class $ {

    public JsonObject $;

    public $(JsonObject json) {
        if (json == null) {
            throw new NullPointerException("Null Json");
        }
        $ = (json);
    }

    public $(String jsonStr) {
        $ = (new JsonObject(jsonStr));
    }

    public $() {
        $ = (new JsonObject());
    }

    public $(Object o) {
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

    public static io.snapito.api.$ $new(JsonObject json) {
        return new io.snapito.api.$(json);
    }

    public static io.snapito.api.$ $new(String json) {
        return new io.snapito.api.$(json);
    }

    public static io.snapito.api.$ $new(Object o) {

        return new io.snapito.api.$(o);
    }

    public String $(String key) {
        return $.getString(key);
    }

    public io.snapito.api.$ $(String name, MultiMap multiMap) {
        $.putObject(name, mapToJson(multiMap));
        return this;
    }

    public io.snapito.api.$ $(String name, JsonArray value) {
        $.putArray(name, value);
        return this;
    }

    public io.snapito.api.$ $(String key, io.snapito.api.$ value) {
        return $(key, value.$json());
    }

    public io.snapito.api.$ $(String key, JsonObject jsonObject) {
        $.putObject(key, jsonObject);
        return this;
    }

    public JsonObject $json() {
        return $;
    }

    public io.snapito.api.$ $(String key, String value) {
        $.putString(key, value);
        return this;
    }

    public String $() {
        return $.toString();
    }

    public io.snapito.api.$ $$(String json) {
        return new io.snapito.api.$($json(json));
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

    public io.snapito.api.$ copy() {
        return new io.snapito.api.$($.copy());
    }

    public boolean has(String key) {
        return $json().containsField(key);
    }

    public io.snapito.api.$ rm(String value) {
        $.removeField(value);
        return this;
    }

    @Override
    public String toString() {
        return $.toString();
    }

    public io.snapito.api.$ ¢(String key) {
        return child(key);
    }

    public io.snapito.api.$ child(String key) {
        JsonObject child = $.getObject(key);
        if (child == null) {
            return null;
        }
        return new io.snapito.api.$(child);
    }
}


