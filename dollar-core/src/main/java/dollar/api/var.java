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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dollar.api.exceptions.DollarFailureException;
import dollar.api.guard.AllVarCollectionGuard;
import dollar.api.guard.AllVarMapGuard;
import dollar.api.guard.ChainGuard;
import dollar.api.guard.Guarded;
import dollar.api.guard.NotNullCollectionGuard;
import dollar.api.guard.NotNullGuard;
import dollar.api.guard.NotNullParametersGuard;
import dollar.api.guard.ReturnVarOnlyGuard;
import dollar.api.json.ImmutableJsonObject;
import dollar.api.json.JsonArray;
import dollar.api.json.JsonObject;
import dollar.api.script.Source;
import dollar.api.types.DollarFactory;
import dollar.api.types.ErrorType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.Serializable;
import java.util.UUID;
import java.util.stream.Stream;

public interface var extends Serializable, Comparable<var>, StateAware<var> {


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
     * Computes the absolute value for this object. Currently the result is only defined for numeric values.
     *
     * @return the absolute value
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    var $abs();

    @NotNull
    @Guarded(ChainGuard.class)
    var $all();

    @NotNull
    @Guarded(NotNullGuard.class)
    var $append(@NotNull var value);

    /**
     * Cast this object to the {@link Type} specified. If the object cannot be converted it will fail with {@link
     * ErrorType#INVALID_CAST}*
     *
     * @param type the type to cast to
     * @return this casted
     */
    @NotNull
    var $as(@NotNull Type type);

    @NotNull
    @Guarded(ChainGuard.class)
    var $avg(boolean parallel);

    @NotNull
    default var $cancel(@NotNull var id) {return DollarStatic.$void();}

    /**
     * Select a value from map based upon the current value and return that.
     *
     * @param map the map
     * @return the var
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    var $choose(@NotNull var map);

    var $constrain(@NotNull var constraint, @Nullable SubType label);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $contains(@NotNull var value) {
        return $containsValue(value);
    }

    @Guarded(ChainGuard.class)
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    var $containsKey(@NotNull var value);

    @Guarded(ChainGuard.class)
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    var $containsValue(@NotNull var value);

    /**
     * Returns a deep copy of this object. You should never need to use this operation as all {@link
     * var} objects are immutable. Therefore they can freely be shared between threads.
     *
     * @return a deep copy of this object
     */
    @NotNull
    @Guarded(ChainGuard.class)
    var $copy();

    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    @Guarded(NotNullCollectionGuard.class)
    @NotNull
    var $copy(@NotNull ImmutableList<Throwable> errors);

    /**
     * Decrements this value, decrementing is the same as  {@code $minus($(1))} for numeric values but may be
     * different behaviour for non-numeric values.
     *
     * @return the new decremented value
     */
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @NotNull
    default var $dec() {
        return $minus(DollarStatic.$(1));
    }

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

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $dispatch(@NotNull var lhs) {
        return $write(lhs, false, false);
    }

    /**
     * Divide the value. For toStrings this means splitting. For numbers this is the usual numerical division.
     *
     * @param rhs the value to divide by
     * @return the divided value
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    var $divide(@NotNull var rhs);

    @NotNull
    @Guarded(ChainGuard.class)
    var $drain();

    /**
     * $ each.
     *
     * @param pipe the pipe
     * @return the var
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    var $each(@NotNull Pipeable pipe);

    @NotNull
    @Guarded(ChainGuard.class)
    default var $equals(@Nullable var other) {
        return DollarFactory.fromValue(equals(other));
    }

    /**
     * Like $unwrap() except it causes lambda evaluation but does not propagate through lists and maps.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    var $fix(boolean parallel);

    @NotNull
    @Guarded(ChainGuard.class)
    var $fix(int depth, boolean parallel);

    @NotNull
    @Guarded(ChainGuard.class)
    default var $fixDeep() { return $fixDeep(false);}

    @NotNull
    @Guarded(ChainGuard.class)
    var $fixDeep(boolean parallel);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    var $get(@NotNull var rhs);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $give(@NotNull var lhs) {
        return $write(lhs, false, true);
    }

    /**
     * Convenience method for the Java API. Returns true if this object has the supplied key.
     *
     * @param key the key
     * @return true if the key exists.
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    default var $has(@NotNull String key) {
        return $has(DollarStatic.$(key));
    }

    /**
     * Returns true if this object has the supplied key.
     *
     * @param key the key
     * @return true if the key exists.
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    var $has(@NotNull var key);

    /**
     * Incrementing is the same as {@code $plus($(1))} for numerical values, it has type dependent behaviour for
     * the other types.
     *
     * @return the incremented value
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    default var $inc() {
        return $plus(DollarStatic.$(1));
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    var $insert(@NotNull var value, int position);

    /**
     * Returns a boolean var which is true if this is empty.
     *
     * @return a true var if it is empty.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    default var $isEmpty() {
        return DollarStatic.$($size().toInteger() == 0);
    }

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
     * This is for reactive programming using lamdas, you probably want $subscribe(...).
     *
     * @param pipeable action
     */
    @NotNull
    default var $listen(@NotNull Pipeable pipeable) {return DollarStatic.$void();}

    /**
     * For Lambdas and reactive programming, do not use.
     *
     * @param pipeable
     * @param id
     * @return
     */
    @NotNull
    default var $listen(@NotNull Pipeable pipeable, @NotNull String id) {return DollarStatic.$void();}

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

    @NotNull
    @Guarded(ChainGuard.class)
    var $max(boolean parallel);

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
    @Guarded(ChainGuard.class)
    var $min(boolean parallel);

    /**
     * Deducts from var, for toStrings this means remove all occurrence of and for collections it means remove the value
     * from the collection. For numbers it is standard numeric arithmetic.
     *
     * @param rhs the value to deduct from this
     * @return the new value
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    default @Guarded(ChainGuard.class)
    var $minus(@NotNull var rhs) {
        return $plus(rhs.$negate());
    }

    /**
     * Returns the remainder after a division.
     *
     * @param rhs the value to divide by
     * @return the remainder
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    var $modulus(@NotNull var rhs);

    /**
     * $ multiply.
     *
     * @param v the v
     * @return the var
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    var $multiply(@NotNull var v);

    /**
     * Negate the value, for lists, toStrings and maps this means reversing the elements. For numbers it is the usual
     * numeric negation.
     *
     * @return the negated value
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    var $negate();

    @NotNull
    @Guarded(ChainGuard.class)
    default var $notEquals(@Nullable var other) {
        return DollarFactory.fromValue(!equals(other));
    }

    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    var $notify();

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
     * Gets pair value.
     *
     * @return the pair value
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    default var $pairValue() {
        return toVarMap().values().iterator().next();
    }

    @NotNull
    @Guarded(ChainGuard.class)
    default var $peek() {
        return $read(false, false);
    }

    /**
     * Returns a new {@link var} with this value added to it. Like {@link #$minus(var)} the actual behaviour varies with
     * types. So for toStrings this is concatenation for collections it is adding a new element.
     *
     * @param rhs the value to add
     * @return a new object with the value supplied added
     */

    @NotNull
    @Guarded(ChainGuard.class)
    var $plus(@NotNull var rhs);

    @NotNull
    @Guarded(ChainGuard.class)
    default var $poll() {
        return $read(false, true);
    }

    @NotNull
    @Guarded(ChainGuard.class)
    default var $pop() {
        return $read(true, true);
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    var $prepend(@NotNull var value);

    @NotNull
    @Guarded(ChainGuard.class)
    var $product(boolean parallel);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    var $publish(@NotNull var lhs);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $push(@NotNull var lhs) {
        return $write(lhs, true, true);
    }

    /**
     * Generic read
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    var $read(boolean blocking, boolean mutating);

    /**
     * Receive (from this) synchronously.
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default var $read() {
        return $read(true, true);
    }

    /**
     * Return a new version of this object with the supplied value removed. THe removal is type specific.
     *
     * @param valueToRemove the value to remove.
     * @return a new object with the value removed.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    var $remove(@NotNull var valueToRemove);

    /**
     * Remove by key. (Map like data only).
     *
     * @param key the key of the key/value pair to remove
     * @return the modified var
     */
    @NotNull
    var $removeByKey(@NotNull String key);

    @NotNull
    @Guarded(ChainGuard.class)
    var $reverse(boolean parallel);

    @NotNull
    default String $serialized() {
        return DollarFactory.toJson(this).toString();
    }

    /**
     * If this type supports the setting of Key/Value pairs this will set the supplied key value pair on a copy of this
     * object. If it doesn't an exception will be thrown.
     *
     * @param key   a String key for the value to be stored in this value.
     * @param value the {@link var} to add.
     * @return the updated copy.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    var $set(@NotNull var key, @NotNull Object value);

    @NotNull
    @Guarded(ChainGuard.class)
    var $size();

    @NotNull
    @Guarded(ChainGuard.class)
    var $sort(boolean parallel);

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
     * Return the content of this object as a stream of values.
     *
     * @param parallel allow actions to be taken on the stream in parallel.
     * @return a stream of values.
     */
    @NotNull
    Stream<var> $stream(boolean parallel);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $subscribe(@NotNull Pipeable subscription) {
        return $subscribe(subscription, UUID.randomUUID().toString());
    }

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $subscribe(@NotNull Pipeable subscription, @NotNull String key) {
        throw new DollarFailureException(ErrorType.INVALID_OPERATION);
    }

    @NotNull
    @Guarded(ChainGuard.class)
    var $sum(boolean parallel);

    /**
     * Remove whitespace before and after the first and last non-whitespace characters.
     *
     * @return
     */
    @NotNull
    default var $trim() {
        return DollarFactory.fromStringValue(toString().trim());
    }

    /**
     * Returns the definitive type of this object, this will trigger execution in dynamic values.
     *
     * @return the type
     */
    @NotNull
    Type $type();

    @NotNull
    @Guarded(ChainGuard.class)
    var $unique(boolean parallel);

    /**
     * Unwraps any wrapper classes around the actual type class.
     *
     * @return an unwrapped class.
     */
    @Guarded(ChainGuard.class)
    @Nullable
    var $unwrap();

    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    var $write(@NotNull var value, boolean blocking, boolean mutating);

    /**
     * Send (to this) synchronously.
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default var $write(@NotNull var value) {
        return $write(value, true, true);
    }

    /**
     * Is collection.
     *
     * @return the boolean
     */
    default boolean collection() {
        return false;
    }

    @Nullable
    SubType constraintLabel();

    /**
     * Debug var.
     *
     * @param message the message
     * @return the var
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var debug(@NotNull Object message);

    /**
     * Debug var.
     *
     * @return the var
     */
    @NotNull
    @Guarded(ChainGuard.class)
    var debug();

    /**
     * Debugf var.
     *
     * @param message the message
     * @param values  the values
     * @return the var
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var debugf(@NotNull String message, Object... values);

    /**
     * Is decimal.
     *
     * @return true if a decimal number
     */
    default boolean decimal() {
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

    default boolean equalsString(@NotNull String s) {
        return toString().equals(s);
    }

    /**
     * Prints the toHumanString() value of this {@link var} to standard error.
     *
     * @return this
     */
    @NotNull
    @Guarded(ChainGuard.class)
    default var err() {
        System.err.println(toString());
        return this;
    }

    /**
     * Error var.
     *
     * @param exception the exception
     * @return the var
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var error(@NotNull Throwable exception);

    /**
     * Error var.
     *
     * @param message the message
     * @return the var
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var error(@NotNull Object message);

    /**
     * Error var.
     *
     * @return the var
     */
    @NotNull
    @Guarded(ChainGuard.class)
    var error();

    /**
     * Errorf var.
     *
     * @param message the message
     * @param values  the values
     * @return the var
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var errorf(@NotNull String message, Object... values);

    /**
     * Is infinite.
     *
     * @return true if this is an infinite value.
     */
    default boolean infinite() {
        return false;
    }

    /**
     * Info var.
     *
     * @param message the message
     * @return the var
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var info(@NotNull Object message);

    /**
     * Info var.
     *
     * @return the var
     */
    @NotNull
    @Guarded(ChainGuard.class)
    var info();

    /**
     * Infof var.
     *
     * @param message the message
     * @param values  the values
     * @return the var
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var infof(@NotNull String message, Object... values);

    /**
     * Is integer.
     *
     * @return true if an integer and not a decimal number
     */
    default boolean integer() {
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
     * Is boolean.
     *
     * @return true if this object is a boolean
     */
    default boolean isBoolean() {
        return false;
    }

    /**
     * Returns a boolean  which is true if this is empty.
     *
     * @return true if it is empty.
     */
    @Guarded(ChainGuard.class)
    default boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Is false.
     *
     * @return true if this object is a boolean and is false
     */
    default boolean isFalse() {
        return false;
    }

    /**
     * Returns true if this is a null value, the null value is <b>not</b> the same as a void value. A void value does
     * not take up space in a collection, i.e. it has no size. A null however does and can optionally have a type also.
     *
     * @return true if this is a null value
     */
    default boolean isNull() { return false;}

    /**
     * Is true.
     *
     * @return true if this object is a boolean and is true
     */
    default boolean isTrue() {
        return false;
    }

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
     * Gets meta attribute.
     *
     * @param key the key
     * @return the meta object
     */
    @Nullable <T> T meta(@NotNull MetaKey key);

    /**
     * Sets meta attribute.
     *
     * @param key   the key
     * @param value the value
     */
    <T> void meta(@NotNull MetaKey key, @NotNull T value);

    /**
     * Sets meta attribute.
     *
     * @param key   the key
     * @param value the value
     */
    void metaAttribute(@NotNull MetaKey key, @NotNull String value);

    /**
     * Gets meta attribute.
     *
     * @param key the key
     * @return the meta attribute
     */
    @Nullable
    String metaAttribute(@NotNull MetaKey key);

    /**
     * Is negative.
     *
     * @return the boolean
     */
    default boolean negative() {
        return sign() <= 0;
    }

    /**
     * Is neither true nor false.
     *
     * @return true if this object is neither true nor false
     */
    boolean neitherTrueNorFalse();

    /**
     * Is number.
     *
     * @return true if this is any sort of number, i.e. decimal or integer
     */
    default boolean number() {
        return decimal() || integer();
    }

    /**
     * Prints the toHumanString() value of this {@link var} to standard out.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    default var out() {
        System.out.println(toString());
        return this;
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
     * Is positive.
     *
     * @return the boolean
     */
    default boolean positive() {
        return sign() >= 0;
    }

    /**
     * Attempt to predict what type this object is. For static types this will predict the the same value as returned by
     * {@link var#$type()}
     *
     * @return a prediction of what type this object may be.
     */
    @Nullable
    TypePrediction predictType();

    default boolean queue() {
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
     * Convenience version of {@link #$remove(var)} for the Java API.
     *
     * @param valueToRemove the value to be removed.
     * @return a new object with the value removed.
     */
    @Nullable
    @Guarded(ChainGuard.class)
    default <R> R remove(@NotNull Object valueToRemove) {
        return $remove(DollarStatic.$(valueToRemove)).toJavaObject();
    }

    /**
     * Sign int.
     *
     * @return the int
     */
    default int sign() {
        if (toDouble() == null) {
            return 0;
        }
        return (int) Math.signum(toDouble());
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

    int size();

    @Nullable
    default Source source() {
        return DollarStatic.context().source();
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
     * Convert this value to a DollarScript compatible string value. Note that dynamic values <b>will</b> be executed to provide this. The string only represents the computed value.
     *
     * @return
     */
    @NotNull
    String toDollarScript();

    /**
     * toDouble double.
     *
     * @return the double
     */
    @Nullable
    Double toDouble();

    /**
     * Converts this value to a human readable string.
     *
     * @return the string
     */
    @NotNull
    String toHumanString();

    /**
     * toInteger integer.
     *
     * @return the integer
     */
    @NotNull
    Integer toInteger();

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
     * Returns the underlying storage value for this type.
     *
     * @return the underlying Java object
     */
    @Nullable <R> R toJavaObject();

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
     * Converts this to a list of value objects such as you would get from $(). Only really useful for collection
     * types.
     *
     * @return a list of vars.
     */
    @Guarded(NotNullCollectionGuard.class)
    @NotNull <T> ImmutableList<T> toList();

    /**
     * toLong long.
     *
     * @return the long
     */
    @NotNull
    Long toLong();

    /**
     * toNumber number.
     *
     * @return the number
     */
    @Nullable
    default Number toNumber() {
        return toDouble();
    }

    /**
     * Convert this object into a stream.
     *
     * @return an InputStream
     */
    @Guarded(NotNullGuard.class)
    @NotNull
    InputStream toStream();

    @NotNull
    @Guarded(NotNullGuard.class)
    @Override
    String toString();

    /**
     * Returns this object as a list of string values or null if this is not applicable.
     *
     * @return a list of toStrings
     */
    @Nullable
    ImmutableList<String> toStrings();


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
    String toYaml();

    /**
     * Is truthy, i.e. if numeric then non zero, if a string,list or map then not empty etc.
     *
     * @return if this object is more true than false
     */
    boolean truthy();

    /**
     * Returns the definitive type of this object, this will trigger execution in dynamic values.
     *
     * @return the type
     */
    @NotNull
    @Deprecated
    default Type type() {
        return $type();
    }

    /**
     * Is uri.
     *
     * @return true if this is a URI
     */
    default boolean uri() {
        return false;
    }

    /**
     * Is zero.
     *
     * @return the boolean
     */
    default boolean zero() {
        return sign() == 0;
    }
}

