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

import me.neilellis.dollar.guard.*;
import me.neilellis.dollar.types.DollarFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface var extends ErrorAware, TypeAware, PipeAware,
                             OldAndDeprecated, VarInternal, NumericAware, BooleanAware, ControlFlowAware,
                             AssertionAware, URIAware, MetadataAware, Comparable<var>, LogAware, StateAware<var> {


    @NotNull
    @Guarded(ChainGuard.class)
    default var $(@NotNull String key, @Nullable Object value) {
        return $(DollarStatic.$(key), value);
    }

    @NotNull
    @Guarded(ChainGuard.class) var $(@NotNull var key, @Nullable Object value);

    /**
     * Returns a {@link me.neilellis.dollar.json.JsonObject}, JsonArray or primitive type such that it can be added to
     * either a {@link me.neilellis.dollar.json.JsonObject} or JsonArray.
     *
     * @return a Json friendly object.
     */
    @Nullable <R> R $();

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class) var $(@NotNull Pipeable lambda);

    @NotNull
    @Guarded(NotNullParametersGuard.class)
    default var $(@NotNull Number n) {
        return $(DollarStatic.$(n));
    }

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class) var $(@NotNull var rhs);

    @NotNull
    @Guarded(ChainGuard.class)
    Stream<var> $children();

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class) Stream<var> $children(@NotNull String key);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $contains(@NotNull var value) {
        return $containsValue(value);
    }

    @Guarded(ChainGuard.class)
    @NotNull
    @Guarded(NotNullParametersGuard.class) var $containsValue(@NotNull var value);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(ReturnVarOnlyGuard.class)
    @Guarded(NotNullParametersGuard.class) var $default(Object o);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $get(@NotNull String key) {
        return $(key);
    }

    @NotNull var $(@NotNull String key);

    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default var $get(@NotNull Object key) {
        return $(String.valueOf(key));
    }

    /**
     * Returns true if this JSON object has the supplied key.
     *
     * @param key the key
     * @return true if the key exists.
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    var $has(@NotNull String key);

    @NotNull
    @Guarded(ChainGuard.class) var $isEmpty();

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(ReturnVarOnlyGuard.class)
    default var $list() {
        return DollarFactory.fromValue(toList(), errors());
    }


    @NotNull
    @Guarded(ChainGuard.class)
    default var $match(@NotNull String key, @Nullable String value) {
        return DollarStatic.$(value != null && value.equals(S(key)));
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
    @Guarded(ChainGuard.class)
    default var $mimeType() {
        return DollarStatic.$("application/json");
    }

    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $minus(@NotNull var value);

    /**
     * Returns a new {@link me.neilellis.dollar.var} with this value appended to it.
     *
     * @param value the value to append, this value may be null
     *
     * @return a new object with the value supplied appended
     */

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(ReturnVarOnlyGuard.class) var $plus(@Nullable var value);

    @NotNull
    @Deprecated
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
    default var $set(@NotNull String key, @Nullable Object value) {
        return $(DollarStatic.$(key), value);
    }

    @NotNull
    @Guarded(ChainGuard.class) var $size();

    @NotNull Stream<var> $stream(boolean parallel);

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

    void clear();

    @NotNull
    @Guarded(ChainGuard.class) boolean containsKey(Object key);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullCollectionGuard.class) Set<Map.Entry<String, var>> entrySet();

    @NotNull
    @Guarded(ChainGuard.class)
    default var err() {
        System.err.println(toString());
        return this;
    }

    @NotNull
    @Override String toString();

    @NotNull
    @Deprecated
    @Guarded(NotNullGuard.class)
    Stream<String> keyStream();

    /**
     * Returns this {@link me.neilellis.dollar.var} object as a stream of key value pairs, with the values themselves
     * being {@link me.neilellis.dollar.var} objects.
     *
     * @return stream of key/value pairs
     */

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(NotNullCollectionGuard.class)
    @Deprecated
    java.util.stream.Stream<Map.Entry<String, var>> kvStream();

    /**
     * Prints the S() value of this {@link var} to stdout.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    default var out() {
        System.out.println(toString());
        return this;
    }

    @NotNull
    @Guarded(ChainGuard.class) <R> R remove(Object value);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullCollectionGuard.class)
    Collection<var> values();

}
