package io.snapito.api;

import org.json.JSONObject;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * To use the $ class you need to statically import all of the methods from this class.
 * This is effectively a factory class for the $ class with additional convenience methods.
 *
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarStatic {

    public static $ $() {
        return new $();
    }

    public static $ $(String name, MultiMap multiMap) {
        return new $().$(name, multiMap);
    }

    public static $ $(String json) {
        return new $(json);
    }

    public static $ $(Object o) {
        return new $(o);
    }

    public static $ $(String name, JsonArray value) {
        return new $().$(name, value);
    }

    public static $ $(String key, String value) {
        return new $().$(key, value);
    }

    public static $ $(JsonObject json) {
        return new $(json);
    }

    public static $ $(String key, $ value) {
        return new $().$(key, value);
    }

    public static $ $(String key, JsonObject jsonObject) {
        return new $().$(key, jsonObject);
    }

    public static JsonArray $array(Object... values) {
        return $.$array(values);
    }

    public static Integer $int(String key) {
        return new $().$int(key);
    }

    public static JsonObject $json(String key) {
        return new $().$json(key);
    }

    public static JsonObject $json() {
        return new $().$json();
    }

    public static JSONObject $orgjson() {
        return new $().$orgjson();
    }

    public static $ child(String key) {
        return new $().child(key);
    }

    public static $ copy() {
        return new $().copy();
    }

    public static boolean has(String key) {
        return new $().has(key);
    }

    public static $ rm(String value) {
        return new $().rm(value);
    }
}
