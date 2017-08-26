/*
 *    Copyright (c) 2014-2017 Neil Ellis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dollar.api;

import dollar.api.guard.ChainGuard;
import dollar.api.guard.Guarded;
import dollar.api.guard.NotNullGuard;
import dollar.api.guard.NotNullParametersGuard;
import dollar.api.guard.ReturnVarOnlyGuard;
import dollar.api.json.ImmutableJsonObject;
import dollar.api.json.JsonArray;
import dollar.api.json.JsonObject;
import dollar.api.types.DollarFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface var extends ErrorAware, TypeAware, Serializable, StringAware,
                                     VarInternal, NumericAware, BooleanAware, ControlFlowAware,
                                     URIAware, MetadataAware, Comparable<var>, LogAware, StateAware<var>,
                                     CollectionAware {


    /**
     * If this type supports the setting of Key/Value pairs this will set the supplied key value pair on a copy of this
     * object. If it doesn't an exception will be thrown. This method is a convenience method for the Java API.
     *
     * @param key   a String key for the value to be stored in this value.
     * @param value the value to add.
     * @return the updated copy.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    default var $(@NotNull String key, @Nullable Object value) {
        return $set(DollarStatic.$(key), DollarStatic.$(value));
    }

    /**
     * Returns the same as toHumanString() but defaults to "" if null and is shorter :-)
     *
     * @return a null safe version of toHumanString()
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    default String $S() {
        String s = toHumanString();
        return (s == null) ? "" : s;

    }

    /**
     * Converts this value to a human readable string.
     *
     * @return the string
     */
    @NotNull
    String toHumanString();

    /**
     * If this is a void object return v otherwise return this.
     *
     * @param v the object to return if this is void.
     * @return this or v
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(ReturnVarOnlyGuard.class)
    @Guarded(NotNullParametersGuard.class)
    var $default(@NotNull var v);

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
    default String $serialized() {
        return DollarFactory.toJson(this).toString();
    }

    /**
     * Return the content of this object as a stream of values.
     *
     * @param parallel allow actions to be taken on the stream in parallel.
     * @return a stream of values.
     */
    @NotNull
    Stream<var> $stream(boolean parallel);

    /**
     * Prints the toHumanString() value of this {@link var} to standard error.
     *
     * @return this
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    @NotNull
    @Guarded(ChainGuard.class)
    default var err() {
        System.err.println(toString());
        return this;
    }

    @NotNull
    @Guarded(ChainGuard.class)
    default var $equals(@Nullable var other) {
        return DollarFactory.fromValue(equals(other));
    }

    @NotNull
    @Guarded(ChainGuard.class)
    default var $notEquals(@Nullable var other) {
        return DollarFactory.fromValue(!equals(other));
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    @Override
    String toString();

    /**
     * Convert this object into a Dollar JsonArray.
     *
     * @return a JsonArray
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    default JsonArray jsonArray() {
        return (JsonArray) DollarFactory.toJson(DollarStatic.$list(this));
    }

    /**
     * Prints the toHumanString() value of this {@link var} to standard out.
     */
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    @NotNull
    @Guarded(ChainGuard.class)
    default var out() {
        System.out.println(toString());
        return this;
    }

    /**
     * Convert this value to a DollarScript compatible string value. Note that dynamic values <b>will</b> be executed to provide this. The string only represents the computed value.
     *
     * @return
     */
    @NotNull
    String toDollarScript();

    /**
     * Returns the underlying storage value for this type.
     *
     * @return the underlying Java object
     */
    @Nullable
    <R> R toJavaObject();


    /**
     * Convert this to a Dollar {@link JsonObject}
     *
     * @return this as a {@link JsonObject}
     */
    default @Nullable
    ImmutableJsonObject toJsonObject() {
        JsonObject json = (JsonObject) toJsonType();
        if (json == null) {
            return null;
        }
        return new ImmutableJsonObject(json);
    }


    /**
     * Returns a {@link JsonObject}, JsonArray or primitive type such that it can be
     * added to
     * either a {@link JsonObject} or JsonArray.
     *
     * @return the JSON compatible object
     */
    @Nullable
    default Object toJsonType() {
        return DollarFactory.toJson(this);
    }

    /**
     * Convert this to a Dollar {@link JsonObject}
     *
     * @return this as a {@link JsonObject}
     */
    default @NotNull
    String toJsonString() {
        ImmutableJsonObject immutableJsonObject = toJsonObject();
        if (immutableJsonObject != null) {
            return immutableJsonObject.toString();
        } else {
            throw new DollarException("Cannot convert to JSON string");
        }
    }


    default boolean equalsString(@NotNull String s) {
        return toString().equals(s);
    }


    default boolean eq(@NotNull String s) {
        return toString().equals(s);
    }

    default boolean eq(@NotNull Integer i) {
        return toInteger().equals(i);
    }

    default boolean eq(@NotNull Double d) {
        return toInteger().equals(d);
    }

    default boolean eq(@NotNull List l) {
        return toList().equals(l);
    }

    default boolean eq(@NotNull Map m) {
        return toJavaMap().equals(m);
    }


}

