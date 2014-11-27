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
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.guard.*;
import me.neilellis.dollar.json.JsonArray;
import me.neilellis.dollar.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Map;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface TypeAware {

    @NotNull
    @Guarded(NotNullGuard.class)
    default String $S() {
        String s = S();
        return s == null ? "" : s;

    }

    String S();

    var $as(Type type);

    @NotNull
    @Guarded(NotNullGuard.class)
    Double D();

    @NotNull
    @Guarded(NotNullGuard.class)
    Integer I();

    /**
     * Returns the value for the supplied key as an Integer.
     *
     * @param key the key
     * @return an Integer value (or null).
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    Integer I(@NotNull String key);

    @Guarded(NotNullGuard.class)
    @NotNull Long L();

    @Guarded(NotNullGuard.class)
    @NotNull Number N();

    @Guarded(NotNullGuard.class)
    default String getPairKey() {
        return toMap().keySet().iterator().next();
    }

    /**
     * Returns this object as a set of nested maps.
     *
     * @return a nested Map or null if the operation doesn't make sense (i.e. on a single valued object or list)
     */
    @Guarded(NotNullGuard.class)
    @Nullable Map<String, Object> toMap();

    @Guarded(NotNullGuard.class)
    default var getPairValue() {
        return $map().values().iterator().next();
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(AllVarMapGuard.class) ImmutableMap<String, var> $map();

    //Any of these
    @Guarded(NotNullGuard.class) boolean is(@NotNull Type... types);

    boolean isDecimal();

    boolean isInteger();

    boolean isLambda();

    boolean isList();

    boolean isMap();

    boolean isNumber();

    boolean isPair();

    boolean isSingleValue();

    boolean isString();

    boolean isUri();


    @Nullable
    JsonObject json(@NotNull String key);

    @NotNull
    @Guarded(NotNullGuard.class)
    default JsonArray jsonArray() {
        JsonArray array = new JsonArray();
        for (me.neilellis.dollar.var var : toList()) {
            if (!var.isVoid()) {
                array.add(var.$());
            }
        }
        return array;
    }

    @Guarded(NotNullCollectionGuard.class)
    @Guarded(AllVarCollectionGuard.class)
    @NotNull ImmutableList<var> toList();

    /**
     * Is this object a void object? Void objects are similar to null, except they can have methods called on them.
     *
     * This is a similar concept to nil in Objective-C.
     *
     * @return true if this is a void object
     * @see me.neilellis.dollar.types.DollarVoid
     * @see me.neilellis.dollar.types.DollarFail
     */
    boolean isVoid();

    /**
     * Returns this object as a org.json.JSONObject.
     *
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

    @Guarded(NotNullGuard.class)
    @NotNull
    InputStream toStream();

}
