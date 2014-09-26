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

import org.json.JSONObject;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface $<T> {

    $ $eval(String js);

    Integer $int();

    boolean isNull();

    $<T> $(String age, long l);

    $ $(String key);

    String $$(String key);


    $<T> $(String key, Object value);

    /**
     * Returns the wrapped object.
     * @return the wrapped object
     */
    T $();


    String $$();

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

    java.util.stream.Stream<$<T>> children();

    java.util.stream.Stream children(String key);

    /**
     * Returns a deep copy of this object, such that mutations to it
     * does not effect this.
     *
     * @return a deep copy of this
     */
    $<T> copy();

    /**
     * Returns true if this JSON object has the supplied key.
     *
     * @param key the key
     * @return true if the key exists.
     */
    boolean has(String key);

    java.util.stream.Stream<Map.Entry<String, $<T>>> keyValues();

    java.util.stream.Stream<String> keys();

    $<T> rm(String value);

    FutureDollar<T> send(EventBus e, String destination);

    Map<String, $<T>> split();

    List<String> splitValues();

    @Override
    String toString();

    T val();

    /**
     * URL decode.
     *
     * @return decoded string value
     */
    $ decode();

}
