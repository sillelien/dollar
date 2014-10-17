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

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
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
public interface var extends Map<String, var> {

    static Map<String, String> config = new HashMap<String, String>();

    static void config(String key, String value) {
        config.put(key, value);
    }

    static String config(String s) {
        return config.get(s);
    }

    @NotNull
    var $(String age, long l);

    @NotNull
    var $add(Object value);

    @NotNull
    java.util.stream.Stream<var> $children();

    @NotNull
    java.util.stream.Stream<var> $children(@NotNull String key);

    /**
     * Returns a deep copy of this object, such that mutations to it
     * does not effect this.
     *
     * @return a deep copy of this
     */
    @NotNull
    var $copy();

    @NotNull
    var $error(@NotNull String errorMessage);

    @NotNull
    var $error(@NotNull Throwable error);

    @NotNull
    var $error();

    @NotNull
    var $errors();

    @NotNull
    var $fail(@NotNull Consumer<List<Throwable>> handler);

    /**
     * Returns true if this JSON object has the supplied key.
     *
     * @param key the key
     * @return true if the key exists.
     */
    boolean $has(@NotNull String key);

    @NotNull
    default var $invalid(@NotNull String errorMessage) {
        return $error(errorMessage, ErrorType.VALIDATION);
    }

    @NotNull
    var $error(@NotNull String errorMessage, @NotNull ErrorType type);

    @NotNull
    List<var> $list();

    @NotNull
    var $load(@NotNull String location);

    @NotNull
    Map<String, var> $map();

    default boolean $match(@NotNull String key, @Nullable String value) {
        return value != null && value.equals(string(key));
    }

    @Nullable
    String string(@NotNull String key);

    /**
     * Returns the mime type of this {@link var} object. By default this will be 'application/json'
     *
     * @return the mime type associated with this object.
     */
    @NotNull
    default String $mimeType() {
        return "application/json";
    }

    boolean $null();

    @NotNull
    var $null(@NotNull Callable<var> handler);

    /**
     * Prints the $$() value of this {@link me.neilellis.dollar.var} to stdout.
     */
    default void $out() {
        System.out.println($$());
    }

    default String $$() {
        return toString();
    }

    @NotNull
    @Override
    String toString();

    @NotNull
    var $pipe(@NotNull String label, @NotNull String js);

    @NotNull
    var $pipe(@NotNull String js);

    @NotNull
    var $pipe(@NotNull Class<? extends Script> clazz);

    @NotNull
    var $pipe(@NotNull Function<var, var> function);

    @NotNull
    var $pop(@NotNull String location, int timeoutInMillis);

    @NotNull
    var $pub(@NotNull String... locations);

    @NotNull
    var $push(@NotNull String location);

    /**
     * Remove by key. (Map like data only).
     *
     * @param key the key of the key/value pair to remove
     * @return the modified var
     */
    @NotNull
    var $rm(@NotNull String key);

    @NotNull
    var $save(@NotNull String location);

    @NotNull
    var $save(@NotNull String location, int expiryInMilliseconds);

    @NotNull
    default var $set(@NotNull String key, @Nullable Object value) {
        return $(key, value);
    }

    @NotNull
    var $(@NotNull String key, @Nullable Object value);

    @NotNull
    Stream<var> $stream();

    @NotNull
    default var _unwrap() {
        return this;
    }

    void clearErrors();

    @NotNull
    var copy(@NotNull ImmutableList<Throwable> errors);

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

    @NotNull
    List<String> errorTexts();

    @NotNull
    List<Throwable> errors();

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
     *
     * @param clazz the class to pass this to.
     */
    @Deprecated
    var eval(Class clazz);

    @NotNull
    default var get(@NotNull Object key) {
        return $(String.valueOf(key));
    }

    @NotNull
    var $(@NotNull String key);

    boolean hasErrors();

    @Nullable
    Integer integer();

    /**
     * Returns the value for the supplied key as an Integer.
     *
     * @param key the key
     * @return an Integer value (or null).
     */
    @Nullable
    Integer integer(@NotNull String key);

    /**
     * Convert this to a Vert.x JsonObject
     *
     * @return this as a JsonObject
     */
    @Nullable
    JsonObject json();

    /**
     * Equivalent returns a Vert.x JsonObject child object value for the supplied key.
     *
     * @param key the key
     * @return a JsonObject
     */
    @Nullable
    JsonObject json(@NotNull String key);

    @Nullable
    Stream<String> keyStream();

    /**
     * Returns this {@link me.neilellis.dollar.var} object as a stream of key value pairs, with the values themselves being {@link me.neilellis.dollar.var} objects.
     *
     * @return stream of key/value pairs
     */

    @Nullable
    java.util.stream.Stream<Map.Entry<String, var>> kvStream();

    /**
     * Returns the value for the supplied key as a general Number.
     *
     * @param key the key
     * @return a Number
     */
    @Nullable
    Number number(@NotNull String key);

    /**
     * Returns this JSON as a org.json.JSONObject, which can be used
     * with none Vert.x APIs etc. This conversion is quite efficient.
     *
     * @return a JSONObject
     */
    @Nullable
    JSONObject orgjson();

    @Nullable
    List<String> strings();

    /**
     * Returns this JSON object as a set of nested maps.
     *
     * @return a nested Map
     */
    @Nullable
    Map<String, Object> toMap();

    /**
     * Returns the underlying data structure. This method is useful for the rare cases you need direct access to the underlying Java type. However it is virtually always better to use $() which returns it in a JSON friendly manner.
     *
     * @param <R> the return type expected
     * @return the underlying Java data structure.
     */
    @Nullable
    default <R> R val() {
        return $();
    }

    /**
     * Returns a {@link org.vertx.java.core.json.JsonObject}, JsonArray or primitive type such that it can be added to either a {@link org.vertx.java.core.json.JsonObject} or JsonArray.
     *
     * @return a Json friendly object.
     */
    @NotNull
    <R> R $();

    enum ErrorType {VALIDATION, SYSTEM}

}
