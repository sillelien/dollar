package com.cazcade.dollar;

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
        return DollarFactory.fromValue();
    }

    public static $ $(String name, MultiMap multiMap) {
        return DollarFactory.fromValue().$(name, multiMap);
    }

    public static $ $(String json) {
        return DollarFactory.fromValue(json);
    }

    public static $ $(Object o) {
        return DollarFactory.fromValue(o);
    }

    public static $ $(String name, JsonArray value) {
        return DollarFactory.fromValue().$(name, value);
    }

    public static $ $(String key, String value) {
        return DollarFactory.fromValue().$(key, value);
    }

    public static $ $(JsonObject json) {
        return DollarFactory.fromValue(json);
    }

    public static $ $(String key, $ value) {
        return DollarFactory.fromValue().$(key, value);
    }

    public static $ $(String key, JsonObject jsonObject) {
        return DollarFactory.fromValue().$child(key, jsonObject);
    }

    public static JsonArray $array(Object... values) {
        return new JsonArray(values);
    }


}
