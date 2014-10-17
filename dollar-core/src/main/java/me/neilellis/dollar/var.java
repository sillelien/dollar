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

package me.neilellis.dollar;

import org.json.JSONObject;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface var extends Map<String,var> {

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
     * Convert this to a Vert.x JsonObject
     *
     * @return this as a JsonObject
     */
    JsonObject $json();

    /**
     * Equivalent returns a Vert.x JsonObject child object value for the supplied key.
     *
     * @param key the key
     * @return a JsonObject
     */
    JsonObject $json(String key);

    List<var> list();

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
     * Returns a deep copy of this object, such that mutations to it
     * does not effect this.
     *
     * @return a deep copy of this
     */
    var copy();

    /**
     * URL decode.
     *
     * @return decoded string value
     */
    @Deprecated
    var decode();

    default void err() {
        System.err.println($$());
    }

    default String $$() {
        return toString();
    }

    @Deprecated
    var eval(String label, String js);

    @Deprecated
    var eval(String js);

    @Deprecated
    var eval(String label, DollarEval eval);

    @Deprecated
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
    @Deprecated
    var eval(Class clazz);

    default var get(Object key) {
        return $(String.valueOf(key));
    }

    var $(String key);

    /**
     * Returns true if this JSON object has the supplied key.
     *
     * @param key the key
     * @return true if the key exists.
     */
    boolean has(String key);

    boolean isNull();

    java.util.stream.Stream<Map.Entry<String, var>> keyValues();

    java.util.stream.Stream<String> $keys();

    String $mimeType();

    default void out() {
        System.out.println($$());
    }

    var remove(Object value);

    var rm(String value);

    @Deprecated
    FutureDollar send(EventBus e, String destination);

    default var set(String key, Object value) {
        return $(key, value);
    }

    var $(String key, Object value);

    Map<String, var> split();

    Stream<var> stream();

    List<String> $strings();

    @Override
    String toString();

    default var _unwrap() {
        return this;
    }

    Map<String, var> map();

    default <R> R $val() {
        return $();
    }

    /**
     * Returns the wrapped object.
     *
     * @return the wrapped object
     */
    <R> R $();

    var copy(List<Throwable> errors);

    //Matching

    default boolean $match(String key, String value) {
        return value != null && value.equals($$(key));
    }

    
    //Error Handling

    enum ErrorType {VALIDATION,SYSTEM}

    default var invalid(String errorMessage) {
      return error(errorMessage,ErrorType.VALIDATION);
    }

    var error(String errorMessage, ErrorType type);

    var error(String errorMessage);

    var error(Throwable error);

    var error();

    boolean hasErrors();

    List<String> $errorTexts();

    List<Throwable> $errors();

    void clearErrors();

    var errors();

    var fail(Consumer<List<Throwable>> handler);

    var ifNull(Callable<var> handler);



    //services

    var pipe(Class<? extends Script> clazz);

    var pipe(Function<var,var> function);

    var pop(String location, int timeoutInMillis);

    var pub(String... locations);

    var push(String location);

    var save(String location);

    var save(String location, int expiryInMilliseconds);

    var load(String location);

}
