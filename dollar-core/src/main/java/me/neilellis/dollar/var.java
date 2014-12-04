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

import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface var extends ErrorAware, TypeAware, PipeAware,
                             OldAndDeprecated, VarInternal, NumericAware, BooleanAware, ControlFlowAware,
                             AssertionAware, URIAware, MetadataAware, Comparable<var>, LogAware, StateAware<var> {


    /**
     * If this type supports the setting of Key/Value pairs this will set the supplied key value pair on a copy of this
     * object. If it doesn't an exception will be thrown. This method is a convenience method for the Java API.
     *
     * @param key   a String key for the value to be stored in this value.
     * @param value the value to add.
     *
     * @return the updated copy.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    default var $(@NotNull String key, @Nullable Object value) {
        return $set(DollarStatic.$(key), value);
    }

    /**
     * If this type supports the setting of Key/Value pairs this will set the supplied key value pair on a copy of
     * this object. If it doesn't an exception will be thrown.
     *
     * @param key a String key for the value to be stored in this value.
     * @param value the {@link var} to add.
     * @return the updated copy.
     */
    @NotNull
    @Guarded(ChainGuard.class) var $set(@NotNull var key, @Nullable Object value);

    /**
     * Returns a {@link me.neilellis.dollar.json.JsonObject}, JsonArray or primitive type such that it can be added to
     * either a {@link me.neilellis.dollar.json.JsonObject} or JsonArray.
     *
     * @return a Json friendly object.
     */
    @Nullable <R> R $();

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

    /**
     * Returns true if this JSON object has the supplied key.
     *
     * @param key the key
     *
     * @return true if the key exists.
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class) var $has(@NotNull String key);

    @NotNull
    @Guarded(ChainGuard.class) var $isEmpty();

    @NotNull
    @Guarded(ChainGuard.class)
    default var $list() {
        return DollarFactory.fromValue(toList(), errors());
    }

    @NotNull
    @Guarded(ChainGuard.class)
    default var $match(@NotNull String key, @Nullable String value) {
        return DollarStatic.$(value != null && value.equals($(key).S()));
    }

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class) default var $(@NotNull Object key) {
        return $get(DollarStatic.$(key));
    }

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class) var $get(@NotNull var rhs);

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

    /**
     * Remove by key. (Map like data only).
     *
     * @param key the key of the key/value pair to remove
     *
     * @return the modified var
     */
    @NotNull var $removeByKey(@NotNull String key);

    @NotNull
    default var $set(@NotNull String key, @Nullable Object value) {
        return $set(DollarStatic.$(key), value);
    }

    @NotNull
    @Guarded(ChainGuard.class) var $size();

    @NotNull Stream<var> $stream(boolean parallel);

    /**
     * Execute the handler if {@link #$void} is true.
     *
     * @param handler the handler to execute
     *
     * @return the result of executing the handler if this is void, otherwise this
     *
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
    @Guarded(ChainGuard.class)
    default var err() {
        System.err.println(toString());
        return this;
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    @Override String toString();

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
    @Guarded(ChainGuard.class) default var remove(Object value) {
        return $remove(DollarStatic.$(value));
    }

    @NotNull
    @Guarded(ChainGuard.class) var $remove(var value);



}
