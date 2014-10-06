/*
 * Copyright (c) 2014-2014 Cazcade Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cazcade.dollar;

import kotlin.Pair;
import org.json.JSONObject;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface var {

    static Map<String, String> config = new HashMap<String, String>();

    static void config(String key, String value) {
        config.put(key, value);
    }

    static String config(String s) {
        return config.get(s);
    }


    var $(String age, long l);

    String $$(String key);

    Integer $int();

    /**
     * Returns the value for the supplied key as an Integer.
     *
     * @param key the key
     * @return an Integer value (or null).
     */
    Integer $int(String key);

    /**
     * Convert this to a Vert.x JsonObject - equivalent to .$
     *
     * @return this JSON as a JsonObject
     */
    JsonObject $json();

    /**
     * Equivalent returns a Vert.x JsonObject child object value for the supplied key.
     *
     * @param key the key
     * @return a JsonObject
     */
    JsonObject $json(String key);

    List<String> $list();

    /**
     * Returns this JSON object as a set of nested maps.
     *
     * @return a nested Map
     */
    Map<String, Object> $map();

    /**
     * Returns the value for the supplied key as a general Number.
     *
     * @param key the key
     * @return a Number
     */
    Number $number(String key);

    /**
     * Returns this JSON as a org.json.JSONObject, which can be used
     * with none Vert.x APIs etc. This conversion is quite efficient.
     *
     * @return a JSONObject
     */
    JSONObject $orgjson();

    var add(Object value);

    java.util.stream.Stream<var> children();

    java.util.stream.Stream children(String key);

    /**
     * URL decode.
     *
     * @return decoded string value
     */
    var decode();

    default void err() {
        System.err.println($$());
    }

    default String $$() {
        return toString();
    }

    var eval(String label, String js);

    var eval(String js);

    var eval(String label, DollarEval eval);

    var eval(DollarEval eval);

    /**
     * If the class has a method $ call($ in) then that method is called otherwise
     * converts this object to a set of string parameters and passes them to the main method of the clazz.
     * <p>
     * NB: This is the preferred way to pass values between classes as it preserves the stateless nature.
     * Try where possible to maintain a stateless context to execution.
     * </p>
     * @param clazz the class to pass this to.
     */
    var eval(Class clazz);

    default var get(String key) {
        return $(key);
    }

    var $(String key);

    /**
     * Returns true if this JSON object has the supplied key.
     *
     * @param key the key
     * @return true if the key exists.
     */
    boolean has(String key);

    default var invoke(Pair... pairs) {
        var copy = copy();
        for (Pair pair : pairs) {
            copy = copy.$(pair.getFirst().toString(), pair.getSecond());
        }
        return copy;
    }

    /**
     * Returns a deep copy of this object, such that mutations to it
     * does not effect this.
     *
     * @return a deep copy of this
     */
    var copy();

    var $(String key, Object value);

    boolean isNull();

    java.util.stream.Stream<Map.Entry<String, var>> keyValues();

    java.util.stream.Stream<String> keys();

    var load(String location);

    String mimeType();

    default void out() {
        System.out.println($$());
    }

    var pass(Class<? extends Script> clazz);

    var pop(String location, int timeoutInMillis);

    void pub(String... locations);

    void push(String location);

    var remove(Object value);

    var rm(String value);

    var save(String location);

    var save(String location, int expiryInMilliseconds);

    FutureDollar send(EventBus e, String destination);

    default var set(String key, Object value) {
        return $(key, value);
    }

    Map<String, var> split();

    List<String> splitValues();

    Stream<var> stream();

    @Override
    String toString();

    default <R> R val() {
        return $();
    }

    /**
     * Returns the wrapped object.
     *
     * @return the wrapped object
     */
    <R> R $();

}
