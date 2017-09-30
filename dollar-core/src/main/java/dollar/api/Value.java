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
import dollar.api.types.NotificationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.Serializable;
import java.util.UUID;
import java.util.stream.Stream;

import static dollar.api.DollarStatic.$void;

public interface Value extends Serializable, Comparable<Value>, StateAware<Value> {


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
    default Value $(@NotNull String key, @Nullable Object value) {
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
        return toHumanString();

    }

    /**
     * Computes the absolute value for this object. Currently the result is only defined for numeric values.
     *
     * @return the absolute value
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    Value $abs();

    @NotNull
    @Guarded(ChainGuard.class)
    Value $all();

    @NotNull
    @Guarded(NotNullGuard.class)
    Value $append(@NotNull Value value);

    /**
     * Cast this object to the {@link Type} specified. If the object cannot be converted it will fail with {@link
     * ErrorType#INVALID_CAST}*
     *
     * @param type the type to cast to
     * @return this casted
     */
    @NotNull
    Value $as(@NotNull Type type);

    @NotNull
    @Guarded(ChainGuard.class)
    Value $avg(boolean parallel);

    @NotNull
    default Value $cancel(@NotNull Value id) {return $void();}

    /**
     * Select a value from map based upon the current value and return that.
     *
     * @param map the map
     * @return the Value
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    Value $choose(@NotNull Value map);

    default Value $clear() {return $void();}

    @NotNull Value $constrain(@NotNull Value constraint, @Nullable SubType label);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default Value $contains(@NotNull Value value) {
        return $containsValue(value);
    }

    @Guarded(ChainGuard.class)
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    Value $containsKey(@NotNull Value value);

    @Guarded(ChainGuard.class)
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    Value $containsValue(@NotNull Value value);

    /**
     * Returns a deep copy of this object. You should never need to use this operation as all {@link
     * Value} objects are immutable. Therefore they can freely be shared between threads.
     *
     * @return a deep copy of this object
     */
    @NotNull
    @Guarded(ChainGuard.class)
    Value $copy();

    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    @Guarded(NotNullCollectionGuard.class)
    @NotNull
    Value $copy(@NotNull ImmutableList<Throwable> errors);

    /**
     * Decrements this value, decrementing is the same as  {@code $minus($(1))} for numeric values but may be
     * different behaviour for non-numeric values.
     *
     * @return the new decremented value
     */
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @NotNull
    default Value $dec() {
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
    Value $default(@NotNull Value v);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default Value $dispatch(@NotNull Value lhs) {
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
    Value $divide(@NotNull Value rhs);

    @NotNull
    @Guarded(ChainGuard.class)
    Value $drain();

    /**
     * $ each.
     *
     * @param pipe the pipe
     * @return the Value
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    Value $each(@NotNull Pipeable pipe);

    @NotNull
    @Guarded(ChainGuard.class)
    default Value $equals(@Nullable Value other) {
        return DollarFactory.fromValue(equals(other));
    }

    /**
     * Like $unwrap() except it causes lambda evaluation but does not propagate through lists and maps.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    Value $fix(boolean parallel);

    @NotNull
    @Guarded(ChainGuard.class)
    Value $fix(int depth, boolean parallel);

    @NotNull
    @Guarded(ChainGuard.class)
    default Value $fixDeep() { return $fixDeep(false);}

    @NotNull
    @Guarded(ChainGuard.class)
    Value $fixDeep(boolean parallel);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    Value $get(@NotNull Value rhs);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default Value $give(@NotNull Value lhs) {
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
    default Value $has(@NotNull String key) {
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
    Value $has(@NotNull Value key);

    /**
     * Incrementing is the same as {@code $plus($(1))} for numerical values, it has type dependent behaviour for
     * the other types.
     *
     * @return the incremented value
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    default Value $inc() {
        return $plus(DollarStatic.$(1));
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    Value $insert(@NotNull Value value, int position);

    /**
     * Returns a boolean Value which is true if this is empty.
     *
     * @return a true Value if it is empty.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    default Value $isEmpty() {
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
    default Value $list() {
        return DollarFactory.fromList(toVarList());
    }

    /**
     * This is for reactive programming using lamdas, you probably want $subscribe(...).
     *
     * @param pipeable action
     */
    @NotNull
    default Value $listen(@NotNull Pipeable pipeable) {return $void();}

    /**
     * For Lambdas and reactive programming, do not use.
     *
     * @param pipeable
     * @param id
     * @return
     */
    @NotNull
    default Value $listen(@NotNull Pipeable pipeable, @NotNull String id) {return $void();}

    /**
     * $ map.
     *
     * @return the immutable map
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    default Value $map() {
        return DollarFactory.fromMap(toVarMap());
    }

    @NotNull
    @Guarded(ChainGuard.class)
    Value $max(boolean parallel);

    /**
     * Returns the mime type of this {@link Value} object. By default this will be 'application/json'
     *
     * @return the mime type associated with this object.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    default Value $mimeType() {
        return DollarStatic.$("application/json");
    }

    @NotNull
    @Guarded(ChainGuard.class)
    Value $min(boolean parallel);

    /**
     * Deducts from Value, for toStrings this means remove all occurrence of and for collections it means remove the value
     * from the collection. For numbers it is standard numeric arithmetic.
     *
     * @param rhs the value to deduct from this
     * @return the new value
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    default @Guarded(ChainGuard.class)
    Value $minus(@NotNull Value rhs) {
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
    Value $modulus(@NotNull Value rhs);

    /**
     * $ multiply.
     *
     * @param v the v
     * @return the Value
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    Value $multiply(@NotNull Value v);

    /**
     * Negate the value, for lists, toStrings and maps this means reversing the elements. For numbers it is the usual
     * numeric negation.
     *
     * @return the negated value
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    Value $negate();

    @NotNull
    @Guarded(ChainGuard.class)
    default Value $notEquals(@Nullable Value other) {
        return DollarFactory.fromValue(!equals(other));
    }


    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    Value $notify(@NotNull NotificationType type, @Nullable Value value);

    /**
     * Gets pair key.
     *
     * @return the pair key
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    default Value $pairKey() {
        return toVarMap().keySet().iterator().next();
    }

    /**
     * Gets pair value.
     *
     * @return the pair value
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    default Value $pairValue() {
        return toVarMap().values().iterator().next();
    }

    @NotNull
    @Guarded(ChainGuard.class)
    default Value $peek() {
        return $read(false, false);
    }

    /**
     * Returns a new {@link Value} with this value added to it. Like {@link #$minus(Value)} the actual behaviour varies with
     * types. So for toStrings this is concatenation for collections it is adding a new element.
     *
     * @param rhs the value to add
     * @return a new object with the value supplied added
     */

    @NotNull
    @Guarded(ChainGuard.class)
    Value $plus(@NotNull Value rhs);

    @NotNull
    @Guarded(ChainGuard.class)
    default Value $poll() {
        return $read(false, true);
    }

    @NotNull
    @Guarded(ChainGuard.class)
    default Value $pop() {
        return $read(true, true);
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    Value $prepend(@NotNull Value value);

    @NotNull
    @Guarded(ChainGuard.class)
    Value $product(boolean parallel);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    Value $publish(@NotNull Value lhs);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default Value $push(@NotNull Value lhs) {
        return $write(lhs, true, true);
    }

    /**
     * Generic read
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    Value $read(boolean blocking, boolean mutating);

    /**
     * Receive (from this) synchronously.
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default Value $read() {
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
    Value $remove(@NotNull Value valueToRemove);

    /**
     * Remove by key. (Map like data only).
     *
     * @param key the key of the key/value pair to remove
     * @return the modified Value
     */
    @NotNull
    Value $removeByKey(@NotNull String key);

    @NotNull
    @Guarded(ChainGuard.class)
    Value $reverse(boolean parallel);

    @NotNull
    default String $serialized() {
        return DollarFactory.serialize(this);
    }

    /**
     * If this type supports the setting of Key/Value pairs this will set the supplied key value pair on a copy of this
     * object. If it doesn't an exception will be thrown.
     *
     * @param key   a String key for the value to be stored in this value.
     * @param value the {@link Value} to add.
     * @return the updated copy.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    Value $set(@NotNull Value key, @NotNull Object value);

    @NotNull
    @Guarded(ChainGuard.class)
    Value $size();

    @NotNull
    @Guarded(ChainGuard.class)
    Value $sort(boolean parallel);

    /**
     * Convert this object into a list of objects, basically the same as casting to a List.
     *
     * @param parallel
     * @return a list type Value.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    default Value $split(boolean parallel) {
        return $stream(parallel);
    }

    /**
     * Return the content of this object as a stream of values.
     *
     * @param parallel allow actions to be taken on the stream in parallel.
     * @return a stream of values.
     */
    @NotNull
    @Deprecated
    default Value $stream(boolean parallel) {
        return DollarFactory.fromStream(toVarList(), parallel);
    }

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default Value $subscribe(@NotNull Pipeable subscription) {
        return $subscribe(subscription, UUID.randomUUID().toString());
    }

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default Value $subscribe(@NotNull Pipeable subscription, @NotNull String key) {
        throw new DollarFailureException(ErrorType.INVALID_OPERATION);
    }

    @NotNull
    @Guarded(ChainGuard.class)
    Value $sum(boolean parallel);

    /**
     * Returns the definitive type of this object, this will trigger execution in dynamic values.
     *
     * @return the type
     */
    @NotNull
    Type $type();

    @NotNull
    @Guarded(ChainGuard.class)
    Value $unique(boolean parallel);

    /**
     * Unwraps any wrapper classes around the actual type class.
     *
     * @return an unwrapped class.
     */
    @Guarded(ChainGuard.class)
    @Nullable
    Value $unwrap();

    default Value $wall(Value value) {
        return value;
    }

    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    Value $write(@NotNull Value value, boolean blocking, boolean mutating);

    /**
     * Send (to this) synchronously.
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default Value $write(@NotNull Value value) {
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
     * Debug Value.
     *
     * @param message the message
     * @return the Value
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    Value debug(@NotNull Object message);

    /**
     * Debug Value.
     *
     * @return the Value
     */
    @NotNull
    @Guarded(ChainGuard.class)
    Value debug();

    /**
     * Debugf Value.
     *
     * @param message the message
     * @param values  the values
     * @return the Value
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    Value debugf(@NotNull String message, Object... values);

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
     * Prints the toHumanString() value of this {@link Value} to standard error.
     *
     * @return this
     */
    @NotNull
    @Guarded(ChainGuard.class)
    default Value err() {
        System.err.println(toString());
        return this;
    }

    /**
     * Error Value.
     *
     * @param exception the exception
     * @return the Value
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    Value error(@NotNull Throwable exception);

    /**
     * Error Value.
     *
     * @param message the message
     * @return the Value
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    Value error(@NotNull Object message);

    /**
     * Error Value.
     *
     * @return the Value
     */
    @NotNull
    @Guarded(ChainGuard.class)
    Value error();

    /**
     * Errorf Value.
     *
     * @param message the message
     * @param values  the values
     * @return the Value
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    Value errorf(@NotNull String message, Object... values);

    /**
     * Is infinite.
     *
     * @return true if this is an infinite value.
     */
    default boolean infinite() {
        return false;
    }

    /**
     * Info Value.
     *
     * @param message the message
     * @return the Value
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    Value info(@NotNull Object message);

    /**
     * Info Value.
     *
     * @return the Value
     */
    @NotNull
    @Guarded(ChainGuard.class)
    Value info();

    /**
     * Infof Value.
     *
     * @param message the message
     * @param values  the values
     * @return the Value
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    Value infof(@NotNull String message, Object... values);

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
     * Prints the toHumanString() value of this {@link Value} to standard out.
     */
    @NotNull
    @Guarded(ChainGuard.class)
    default Value out() {
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
     * {@link Value#$type()}
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
     * Convenience version of {@link #$remove(Value)} for the Java API.
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

    @NotNull
    default Stream<Value> stream(boolean parallel) {
        return toVarList().stream();
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
    double toDouble();

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
    int toInteger();

    /**
     * Returns this object as a set of nested maps the values are completely unwrapped and don't contain 'Value' objects.
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
    long toLong();

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
    ImmutableList<Value> toVarList();

    /**
     * $ map.
     *
     * @return an immutable map
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(AllVarMapGuard.class)
    ImmutableMap<Value, Value> toVarMap();

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

