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

import com.google.common.collect.ImmutableList;
import me.neilellis.dollar.guard.AllVarCollectionGuard;
import me.neilellis.dollar.guard.Guarded;
import me.neilellis.dollar.guard.NonNullCollectionGuard;
import me.neilellis.dollar.json.JsonArray;
import me.neilellis.dollar.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Map;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface TypeAware {
    @NotNull
    default String $S() {
        String s = S();
        return s == null ? "" : s;

    }

    String S();


    @Guarded(NonNullCollectionGuard.class)
    @Guarded(AllVarCollectionGuard.class)
    @NotNull
    ImmutableList<var> toList();

    /**
     * Is this object a void object? Void objects are similar to null, except they can have methods called on them.
     * <p/>
     * This is a similar concept to nil in Objective-C.
     *
     * @return true if this is a void object
     * @see me.neilellis.dollar.types.DollarVoid
     * @see me.neilellis.dollar.types.DollarFail
     */
    boolean isVoid();

    @NotNull
    Double D();

    @NotNull
    Integer I();

    @NotNull
    Long L();

    /**
     * Returns the value for the supplied key as an Integer.
     *
     * @param key the key
     * @return an Integer value (or null).
     */
    @NotNull
    Integer I(@NotNull String key);

    default boolean isDecimal() {
        return false;
    }

    default boolean isInteger() {
        return false;
    }

    default boolean isList() {
        return false;
    }

    default boolean isMap() {
        return false;
    }

    default boolean isNumber() {
        return false;
    }

    default boolean isSingleValue() {
        return false;
    }

    default boolean isString() {
        return false;
    }

    @Nullable
    JsonObject json(@NotNull String key);

    @NotNull
    default JsonArray jsonArray() {
        JsonArray array = new JsonArray();
        for (me.neilellis.dollar.var var : toList()) {
            array.add(var.$());
        }
        return array;
    }

    /**
     * Returns the value for the supplied key as a general {@link Number}.
     *
     * @param key the key to look up
     * @return a Number or null if this operation is not applicable
     */
    @Nullable
    Number number(@NotNull String key);

    /**
     * Returns this object as a org.json.JSONObject.
     * <p/>
     * NB: This conversion is quite efficient.
     *
     * @return a JSONObject
     */
    @Nullable
    default JSONObject orgjson() {
        JsonObject json = json();
        if (json != null) {
            return new JSONObject(json.toMap());
        } else {
            return null;
        }
    }

    /**
     * Convert this to a Vert.x JsonObject
     *
     * @return this as a JsonObject
     */
    @Nullable
    JsonObject json();

    /**
     * Returns this object as a list of string values or null if this is not applicable.
     *
     * @return a list of strings
     */
    @Nullable
    ImmutableList<String> strings();

    /**
     * Returns this object as a set of nested maps.
     *
     * @return a nested Map or null if the operation doesn't make sense (i.e. on a single valued object or list)
     */
    @Nullable
    Map<String, Object> toMap();

    @NotNull
    Number N();
}
