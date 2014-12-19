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

import me.neilellis.dollar.collections.ImmutableList;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.guard.*;
import me.neilellis.dollar.types.DollarFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * me.neilellis.dollar.types.ErrorType#INVALID_CAST}
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


    @Guarded(NotNullGuard.class)
    default var getPairKey() {
        return $map().keySet().iterator().next();
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(AllVarMapGuard.class) ImmutableMap<var, var> $map();

    @NotNull
    @Guarded(NotNullGuard.class)
    default var getPairValue() {
        return $map().values().iterator().next();
    }

    /**
     * Returns true if this object is of any of the supplied types.
     *
     * @param types a list of types
     *
     * @return true if of one of the types
     */
    @Guarded(NotNullGuard.class) boolean is(@NotNull Type... types);

    default boolean isCollection() {
        return false;
    }

    default boolean isDecimal() {
        return false;
    }

    default boolean isError() {
        return false;
    }

    default boolean isInfinite() {
        return false;
    }

    default boolean isInteger() {
        return false;
    }

    default boolean isLambda() {
        return false;
    }

    default boolean isList() {
        return false;
    }

    default boolean isMap() {
        return false;
    }

    default boolean isNull() { return false;}

    default boolean isNumber() {
        return false;
    }

    default boolean isPair() {
        return false;
    }

    default boolean isRange() {
        return false;
    }

    default boolean isSingleValue() {
        return false;
    }

    default boolean isString() {
        return false;
    }

    default boolean isUri() {
        return false;
    }

    /**
     * Is this object a void object? Void objects are similar to null, except they can have methods called on them.
     *
     * This is a similar concept to nil in Objective-C.
     *
     * @return true if this is a void object
     */
    default boolean isVoid() {
        return false;
    }

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
    @NotNull ImmutableList<?> toList();

    /**
     * Returns this object as a set of nested maps the values are completely unwrapped and don't contain 'var' objects.
     *
     * @return a nested Map or null if the operation doesn't make sense (i.e. on a single valued object or list)
     */
    @NotNull
    @Guarded(NotNullGuard.class) <K, V> Map<K, V> toMap();

    /**
     * Convert this object into a stream.
     *
     * @return an InputStream
     */
    @Guarded(NotNullGuard.class)
    @NotNull InputStream toStream();

}
