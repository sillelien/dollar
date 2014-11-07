/*
 * Copyright (c) 2014 Neil Ellis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.neilellis.dollar;

import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.guard.*;
import me.neilellis.dollar.types.DollarFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface var extends Map<String, var>, IntegrationProviderAware, ErrorAware, TypeAware, PipeAware,
        OldAndDeprecated, VarInternal, BasicIOAware, NumericAware, BooleanAware, ControlFlowAware, AssertionAware, ReactiveAware, MetadataAware, Comparable<var> {


    /**
     * Return a new object with the key and value added to it.
     *
     * @param key   the key
     * @param value the value
     * @return a new {@link me.neilellis.dollar.var} object with the key/value pair included.
     */
    @NotNull
    @Guarded(SetKeyValueGuard.class)
    @Guarded(ReturnVarOnlyGuard.class)
    var $(@NotNull String key, long value);

    /**
     * Return a new object with the key and value added to it.
     *
     * @param key   the key
     * @param value the value
     * @return a new {@link me.neilellis.dollar.var} object with the key/value pair included.
     */
    @NotNull
    @Guarded(SetKeyValueGuard.class)
    @Guarded(ReturnVarOnlyGuard.class)
    var $(@NotNull String key, double value);

    @Guarded(SetKeyValueGuard.class)
    @Guarded(ReturnVarOnlyGuard.class)
    var $(@NotNull String key, Pipeable value);

    @NotNull
    @Override
    String toString();

    /**
     * Returns a new {@link me.neilellis.dollar.var} with this value appended to it.
     *
     * @param value the value to append, this value may be null
     * @return a new object with the value supplied appended
     */

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(ReturnVarOnlyGuard.class)
    var $plus(@Nullable Object value);

    @NotNull
    @Guarded(ChainGuard.class)
    Stream<var> $children();

    @Guarded(ChainGuard.class)
    @Guarded(ReturnVarOnlyGuard.class)
    var $default(Object o);

    @NotNull
    @Guarded(ChainGuard.class)
    Stream<var> $children(@NotNull String key);

    /**
     * Returns a deep copy of this object. You should never need to use this operation as all {@link
     * me.neilellis.dollar.var} objects are immutable. Therefore they can freely be shared between threads.
     *
     * @return a deep copy of this object
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(ReturnVarOnlyGuard.class)
    var $copy();

    /**
     * Returns true if this JSON object has the supplied key.
     *
     * @param key the key
     * @return true if the key exists.
     */
    @Guarded(NotNullParametersGuard.class)
    boolean $has(@NotNull String key);


    @Guarded(ChainGuard.class)
    @Guarded(ReturnVarOnlyGuard.class)
    default var $list() {
        return DollarFactory.fromValue(errors(), toList());
    }

    @NotNull
    @Guarded(AllVarMapGuard.class)
    ImmutableMap<String, var> $map();

    default boolean $match(@NotNull String key, @Nullable String value) {
        return value != null && value.equals(S(key));
    }

    @Nullable
    @Guarded(NotNullParametersGuard.class)
    default String S(@NotNull String key) {
        return $(key).S();
    }

    /**
     * Returns the mime type of this {@link var} object. By default this will be 'application/json'
     *
     * @return the mime type associated with this object.
     */
    @NotNull
    default String $mimeType() {
        return "application/json";
    }

    @Guarded(ReturnVarOnlyGuard.class)
    var $post(String url);

    /**
     * Remove by key. (Map like data only).
     *
     * @param key the key of the key/value pair to remove
     * @return the modified var
     */
    @NotNull
    var $rm(@NotNull String key);


    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    var $minus(@NotNull Object value);

    @NotNull
    default var $set(@NotNull String key, @Nullable Object value) {
        return $(key, value);
    }

    @NotNull
    @Guarded(ChainGuard.class)
    var $(@NotNull String key, @Nullable Object value);

    @NotNull
    Stream<var> $stream();

    /**
     * Execute the handler if {@link #$void} is true.
     *
     * @param handler the handler to execute
     * @return the result of executing the handler if this is void, otherwise this
     * @see me.neilellis.dollar.types.DollarVoid
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default var $void(@NotNull Callable<var> handler) {
        if (isVoid()) {
            try {
                return handler.call();
            } catch (Exception e) {
                return DollarStatic.handleError(e, this);
            }
        } else {
            return this;
        }
    }


    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)

    default var get(@NotNull Object key) {
        return $(String.valueOf(key));
    }

    @NotNull
    var $(@NotNull String key);

    /**
     * Returns a {@link me.neilellis.dollar.json.JsonObject}, JsonArray or primitive type such that it can be added to
     * either a {@link me.neilellis.dollar.json.JsonObject} or JsonArray.
     *
     * @return a Json friendly object.
     */
    @NotNull
    <R> R $();

    @Guarded(NotNullGuard.class)
    Stream<String> keyStream();

    /**
     * Returns this {@link me.neilellis.dollar.var} object as a stream of key value pairs, with the values themselves
     * being {@link me.neilellis.dollar.var} objects.
     *
     * @return stream of key/value pairs
     */

    @Nullable
    @Guarded(NotNullGuard.class)
    java.util.stream.Stream<Map.Entry<String, var>> kvStream();

    /**
     * Returns the underlying data structure. This method is useful for the rare cases you need direct access to the
     * underlying Java type. However it is virtually always better to use $() which returns it in a JSON friendly manner.
     *
     * @param <R> the return type expected
     * @return the underlying Java data structure.
     */
    @Nullable
    default <R> R val() {
        return $();
    }

    @Guarded(ChainGuard.class)
    default var err() {
        System.err.println(toString());
        return this;
    }

    /**
     * Prints the S() value of this {@link var} to stdout.
     */
    @Guarded(ChainGuard.class)
    default var out() {
        System.out.println(toString());
        return this;
    }


    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    var $(Pipeable lambda);


    var $(Number n);

    var $(var rhs);

}
