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
import me.neilellis.dollar.types.DollarFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Map;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface TypeAware {

    /**
     * Returns the same as S() but defaults to "" if null.
     *
     * @return a null safe version of S()
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    default String $S() {
        String s = S();
        return s == null ? "" : s;

    }

    String S();

    /**
     * Cast this object to the {@link Type} specified. If the object cannot be converted it will fail with {@link
     * me.neilellis.dollar.types.FailureType#INVALID_CAST}
     *
     * @param type the type to cast to
     *
     * @return this casted
     */
    var $as(Type type);

    /**
     * Convert this object into a list of objects, basically the same as casting to a List.
     *
     * @return a list type var.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    default var $split() {
        return DollarFactory.fromValue($list());
    }

    /**
     * Converts this to a list of vars. Only really useful for collection types.
     *
     * @return a list of vars.
     */
    @Guarded(NotNullCollectionGuard.class)
    @Guarded(AllVarCollectionGuard.class)
    @NotNull ImmutableList<var> $list();

    Type $type();

    @NotNull
    @Guarded(NotNullGuard.class) Double D();

    @NotNull
    @Guarded(NotNullGuard.class) Integer I();

    @Guarded(NotNullGuard.class)
    @NotNull Long L();

    @Guarded(NotNullGuard.class)
    @NotNull Number N();

    @Guarded(NotNullGuard.class)
    default String getPairKey() {
        return toMap().keySet().iterator().next();
    }

    /**
     * Returns this object as a set of nested maps the values are completely unwrapped and don't contain 'var' objects.
     *
     * @return a nested Map or null if the operation doesn't make sense (i.e. on a single valued object or list)
     */
    @NotNull
    @Guarded(NotNullGuard.class) Map<String, Object> toMap();

    @NotNull
    @Guarded(NotNullGuard.class)
    default var getPairValue() {
        return $map().values().iterator().next();
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(AllVarMapGuard.class) ImmutableMap<String, var> $map();

    /**
     * Returns true if this object is of any of the supplied types.
     *
     * @param types a list of types
     *
     * @return true if of one of the types
     */
    @Guarded(NotNullGuard.class) boolean is(@NotNull Type... types);

    boolean isCollection();

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

    /**
     * Convert this object into a Dollar JsonArray.
     *
     * @return a JsonArray
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    default JsonArray jsonArray() {
        JsonArray array = new JsonArray();
        for (me.neilellis.dollar.var item : $list()) {
            if (item == this) {
                throw new IllegalArgumentException();
            }
            if (!item.isVoid()) {
                array.add(item.$());
            }
        }
        return array;
    }

    /**
     * Is this object a void object? Void objects are similar to null, except they can have methods called on them.
     *
     * This is a similar concept to nil in Objective-C.
     *
     * @return true if this is a void object
     *
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
     * Convert this to a Dollar JsonObject
     *
     * @return this as a JsonObject
     */
    @Nullable JsonObject json();

    /**
     * Returns this object as a list of string values or null if this is not applicable.
     *
     * @return a list of strings
     */
    @Nullable ImmutableList<String> strings();

    /**
     * Converts this to a list of value objects such as you would get from $(). Only really useful for collection
     * types.
     *
     * @return a list of vars.
     */
    @Guarded(NotNullCollectionGuard.class)
    @NotNull ImmutableList<Object> toList();

    /**
     * Convert this object into a stream.
     *
     * @return an InputStream
     */
    @Guarded(NotNullGuard.class)
    @NotNull InputStream toStream();

}
