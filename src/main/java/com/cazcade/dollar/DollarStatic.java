package com.cazcade.dollar;

import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Map;

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


    public static $ $(Object o) {
        if(o == null) {
            return DollarNull.INSTANCE;
        }
        if(o instanceof Number) {
            return new DollarNumber((Number) o);
        }
        if(o instanceof String) {
            try {
                return new DollarJson(new JsonObject(o.toString()));
            } catch (DecodeException de) {
                return new DollarString((String) o);
            }
        }
        JsonObject json;
        if (o instanceof JsonObject) {
            json = ((JsonObject) o);
        }
        if (o instanceof JsonObject) {
            json = ((JsonObject) o);
        } else if (o instanceof MultiMap) {
            json = mapToJson((MultiMap) o);
        } else if (o instanceof Map) {
            json = new JsonObject((Map<String, Object>) o);
        } else if (o instanceof Message) {
            json = ((JsonObject) ((Message) o).body());
            if(json == null) {
                return DollarNull.INSTANCE;
            }
        } else {
            json = new JsonObject(o.toString());
        }
        return new DollarJson(json);
    }

    static JsonObject mapToJson(MultiMap map) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, String> entry : map) {
            jsonObject.putString(entry.getKey(), entry.getValue());
        }
        return jsonObject;
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
        return DollarFactory.fromValue().$(key, jsonObject);
    }

    public static JsonArray $array(Object... values) {
        return new JsonArray(values);
    }


}
