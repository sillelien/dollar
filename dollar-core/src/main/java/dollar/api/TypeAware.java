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

import dollar.api.collections.ImmutableList;
import dollar.api.collections.ImmutableMap;
import dollar.api.guard.AllVarCollectionGuard;
import dollar.api.guard.AllVarMapGuard;
import dollar.api.guard.ChainGuard;
import dollar.api.guard.Guarded;
import dollar.api.guard.NotNullCollectionGuard;
import dollar.api.guard.NotNullGuard;
import dollar.api.types.DollarFactory;
import dollar.api.types.ErrorType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public interface TypeAware {


    /**
     * Cast this object to the {@link Type} specified. If the object cannot be converted it will fail with {@link
     * ErrorType#INVALID_CAST}*
     *
     * @param type the type to cast to
     * @return this casted
     */
    @NotNull
    var $as(@NotNull Type type);

    /**
     * Convert this object into a list of objects, basically the same as casting to a List.
     *
     * @return a list type var.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    default var $split() {
        return DollarFactory.fromValue(toVarList());
    }

    /**
     * Converts this to a list of vars. Only really useful for collection types.
     *
     * @return a list of vars.
     */
    @Guarded(NotNullCollectionGuard.class)
    @Guarded(AllVarCollectionGuard.class)
    @NotNull
    ImmutableList<var> toVarList();

    /**
     * Converts this to a list of vars. Only really useful for collection types.
     *
     * @return a list of vars.
     */
    @Guarded(NotNullCollectionGuard.class)
    @Guarded(AllVarCollectionGuard.class)
    @NotNull
    default var $list() {
        return DollarFactory.fromList(toVarList());
    }


    /**
     * Returns the definitive type of this object, this will trigger execution in dynamic values.
     *
     * @return the type
     */
    @NotNull
    Type $type();


    /**
     * Returns the definitive type of this object, this will trigger execution in dynamic values.
     *
     * @return the type
     */
    @NotNull
    default Type type() {
        return $type();
    }

    /**
     * Is collection.
     *
     * @return the boolean
     */
    default boolean collection() {
        return false;
    }

    /**
     * Returns true if this object is dynamically evaluated.
     *
     * @return true if a dynamic value
     */
    default boolean dynamic() {
        return false;
    }

    /**
     * Gets pair key.
     *
     * @return the pair key
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    default var $pairKey() {
        return toVarMap().keySet().iterator().next();
    }


    /**
     * $ map.
     *
     * @return an immutable map
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(AllVarMapGuard.class)
    ImmutableMap<var, var> toVarMap();

    /**
     * $ map.
     *
     * @return the immutable map
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    default var $map() {
        return DollarFactory.fromMap(toVarMap());
    }


    /**
     * $ map.
     *
     * @return the immutable map
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    String toYaml();


    /**
     * Gets pair value.
     *
     * @return the pair value
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    default var $pairValue() {
        return toVarMap().values().iterator().next();
    }

    /**
     * Is infinite.
     *
     * @return true if this is an infinite value.
     */
    default boolean infinite() {
        return false;
    }

    /**
     * Returns true if this object is of any of the supplied types.
     *
     * @param types a list of types
     * @return true if of one of the types
     */
    @Guarded(NotNullGuard.class)
    boolean is(@NotNull Type... types);



    /**
     * Returns true if this is a null value, the null value is <b>not</b> the same as a void value. A void value does
     * not take up space in a collection, i.e. it has no size. A null however does and can optionally have a type also.
     *
     * @return true if this is a null value
     */
    default boolean isNull() { return false;}


    /**
     * Is this object a void object? Void objects are a similar concept to nil in Objective-C, any operation on them
     * results in a void value. However they take up no space in a collection. So are closer to the Java concept of
     * void. If you wish to represent a non-existent value use void (think of this like a zero length array ). If you
     * wish to represent a lack of value use null (think of this as a single length array with no assigned value.
     *
     * @return true if this is a void object
     */
    default boolean isVoid() {
        return false;
    }

    /**
     * Is list.
     *
     * @return true if this is a list
     */
    default boolean list() {
        return false;
    }

    /**
     * Is map.
     *
     * @return true if this is a map
     */
    default boolean map() {
        return false;
    }

    /**
     * Is number.
     *
     * @return true if this is any sort of number, i.e. decimal or integer
     */
    default boolean number() {
        return decimal() || integer();
    }

    /**
     * Is decimal.
     *
     * @return true if a decimal number
     */
    default boolean decimal() {
        return false;
    }

    /**
     * Is integer.
     *
     * @return true if an integer and not a decimal number
     */
    default boolean integer() {
        return false;
    }

    /**
     * A pair is a special case of a Map where this only one key/value pair.
     *
     * @return true if this is a pair
     */
    default boolean pair() {
        return false;
    }

    /**
     * Is range.
     *
     * @return true if this is a range
     */
    default boolean range() {
        return false;
    }

    /**
     * Returns true if this is an object which can only have a single value, i.e. it is not a collection of any form
     * (including a pair).
     *
     * @return true if this is a single value
     */
    default boolean singleValue() {
        return false;
    }

    /**
     * Is string.
     *
     * @return true if this is a string
     */
    default boolean string() {
        return false;
    }

    /**
     * Returns this object as a list of string values or null if this is not applicable.
     *
     * @return a list of toStrings
     */
    @Nullable
    ImmutableList<String> toStrings();


    /**
     * Converts this to a list of value objects such as you would get from $(). Only really useful for collection
     * types.
     *
     * @return a list of vars.
     */
    @Guarded(NotNullCollectionGuard.class)
    @NotNull
    ImmutableList<?> toList();

    /**
     * Returns this object as a set of nested maps the values are completely unwrapped and don't contain 'var' objects.
     *
     * @param <K> the type parameter
     * @param <V> the type parameter
     * @return a nested Map or null if the operation doesn't make sense (i.e. on a single valued object or list)
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap();

    /**
     * Convert this object into a stream.
     *
     * @return an InputStream
     */
    @Guarded(NotNullGuard.class)
    @NotNull
    InputStream toStream();

    /**
     * Is uri.
     *
     * @return true if this is a URI
     */
    boolean uri();


    boolean queue();


}
