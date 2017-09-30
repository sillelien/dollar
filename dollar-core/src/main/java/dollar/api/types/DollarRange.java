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

package dollar.api.types;


import com.google.common.collect.BoundType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import dollar.api.DollarException;
import dollar.api.DollarStatic;
import dollar.api.Type;
import dollar.api.Value;
import dollar.api.exceptions.DollarFailureException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static dollar.api.DollarStatic.$void;
import static dollar.api.types.DollarFactory.INFINITY;
import static dollar.api.types.DollarFactory.wrap;

public class DollarRange extends DollarCollection {

    @NotNull
    private final Range<Value> range;
    private boolean reversed;

    public DollarRange(@NotNull Value start, @NotNull Value finish) {
        super();
        Value startUnwrap;
        Value finishUnwrap;

        if (start.compareTo(finish) < 0) {
            startUnwrap = start.$unwrap();
            finishUnwrap = finish.$unwrap();
        } else {
            startUnwrap = finish.$unwrap();
            finishUnwrap = start.$unwrap();
            reversed = true;

        }

        assert startUnwrap != null;
        assert finishUnwrap != null;
        range = Range.closed(startUnwrap, finishUnwrap);
    }


    public DollarRange(boolean lowerBounds,
                       boolean upperBounds,
                       boolean closedLeft,
                       boolean closedRight, @Nullable Value lower, @Nullable Value upper) {
        super();
        Value lowerBound;
        Value upperBound;
        if ((lower != null) && (upper != null) && (lower.compareTo(upper) > 0)) {
            lowerBound = upper;
            upperBound = lower;
            reversed = true;
        } else {
            lowerBound = lower;
            upperBound = upper;
        }
        if (!lowerBounds && !upperBounds) {
            range = Range.all();
        } else if (!lowerBounds) {
            if (closedRight) {
                range = Range.atMost(upperBound);
            } else {
                range = Range.lessThan(upperBound);
            }
        } else if (!upperBounds) {
            if (closedLeft) {
                range = Range.atLeast(lowerBound);
            } else {
                range = Range.greaterThan(lowerBound);
            }
        } else if (closedLeft) {
            if (closedRight) {
                range = Range.closed(lowerBound, upperBound);
            } else {
                //openRight
                range = Range.closedOpen(lowerBound, upperBound);
            }
        } else if (!closedLeft) {
            //openLeft
            if (closedRight) {
                range = Range.openClosed(lowerBound, upperBound);
            } else {
                //openRight
                if (lowerBound.equals(upperBound)) {
                    throw new IllegalArgumentException("Cannot have an open range with lower bounds being the same as upper " +
                                                               "bounds");
                } else {
                    range = Range.open(lowerBound, upperBound);
                }
            }
        } else {
            throw new IllegalStateException();
        }
    }

    public DollarRange(@NotNull Range<Value> range, boolean reversed) {
        super();
        this.range = range;
        this.reversed = reversed;
    }

    @NotNull
    @Override
    public Value $abs() {
        return wrap(new DollarRange(
                                           Range.range(DollarFactory.fromValue(range.lowerEndpoint().$abs()),
                                                       range.lowerBoundType(),
                                                       DollarFactory.fromValue(range.upperEndpoint().$abs()),
                                                       range.upperBoundType())
                                           , reversed));
    }

    @NotNull
    @Override
    public Value $append(@NotNull Value value) {
        return $plus(value);
    }

    @NotNull
    @Override
    public Value $as(@NotNull Type type) {
        if (type.is(Type._LIST)) {
            return DollarStatic.$(toVarList());
        } else if (type.is(Type._MAP)) {
            return DollarStatic.$(toJavaMap());
        } else if (type.is(Type._STRING)) {
            return DollarFactory.fromStringValue(toHumanString());
        } else if (type.is(Type._VOID)) {
            return $void();
        } else {
            throw new DollarFailureException(ErrorType.INVALID_CAST);
        }
    }

    @NotNull
    @Override
    public Value $containsKey(@NotNull Value value) {
        throw new DollarFailureException(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public Value $containsValue(@NotNull Value value) {
        return DollarStatic.$(range.contains(value));
    }

    @NotNull
    @Override
    public Value $divide(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();
        return wrap(new DollarRange(
                                           Range.range(DollarFactory.fromValue(range.lowerEndpoint().$divide(rhsFix)),
                                                       range.lowerBoundType(),
                                                       DollarFactory.fromValue(range.upperEndpoint().$divide(rhsFix)),
                                                       range.upperBoundType())
                                           , reversed));

    }

    @NotNull
    @Override
    public Value $get(@NotNull Value key) {
        if (key.integer()) {
            long keyL;
            if (key.toLong() < 0) {
                keyL = size() + key.toLong();
            } else {
                keyL = key.toLong();
            }
            Value upper = range.upperEndpoint();
            assert upper != null;
            Value lower = range.lowerEndpoint();
            assert lower != null;

            // (1..3) 2,2 [1..3] 1,3
            if (reversed && !range.hasUpperBound()) {
                throw new DollarException("Attempted to get an element from an unbounded range offset from the upper bound " +
                                                  "(reversed)");
            }
            if (!reversed && !range.hasLowerBound()) {
                throw new DollarException("Attempted to get an element from an unbounded range offset from the lower bound " +
                                                  "(not reversed)");
            }
            if (range.upperBoundType().equals(BoundType.OPEN)) {
                upper = upper.$dec();
            }

            if (range.lowerBoundType().equals(BoundType.OPEN)) {
                lower = lower.$inc();
            }
            if (upper.compareTo(lower) < 0) {
                throw new DollarException("Elements not available in an empty range");
            }
            if (upper.integer()) {
                if ((upper.toLong() == lower.toLong()) && (keyL == 0)) {
                    return DollarFactory.fromValue(lower);
                }
                final long result = reversed ? (upper.toLong() - keyL) : (lower.toLong() + keyL);
                return DollarFactory.fromValue(result);
            }
            if (upper.decimal()) {
                if ((upper.toDouble() == lower.toDouble()) && (keyL == 0)) {
                    return DollarFactory.fromValue(lower);
                }
                final double diff = reversed ? (upper.toDouble() - keyL) : (lower.toDouble() + keyL);
                return DollarFactory.fromValue(diff + Math.signum(diff));
            }
            if (upper.equals(lower) && (keyL == 0)) {
                return DollarFactory.fromValue(lower);
            }
            if (!reversed) {
                Value start = lower;
                for (long i = 0; i < keyL; i++) {
                    start = start.$inc();
                }
                return start;
            } else {
                Value finish = upper;
                for (long i = 0; i < keyL; i++) {
                    finish = finish.$dec();
                }
                return finish;
            }

        }
        throw new DollarFailureException(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public Value $has(@NotNull Value key) {
        return DollarStatic.$(false);
    }

    @NotNull
    @Override
    public Value $insert(@NotNull Value value, int position) {
        return $get(DollarStatic.$(toVarList())).$insert(value, position);
    }

    @NotNull
    @Override
    public Value $minus(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();
        return wrap(new DollarRange(
                                           Range.range(DollarFactory.fromValue(range.lowerEndpoint().$minus(rhsFix)),
                                                       range.lowerBoundType(),
                                                       DollarFactory.fromValue(range.upperEndpoint().$minus(rhsFix)),
                                                       range.upperBoundType())
                                           , reversed));
    }

    @NotNull
    @Override
    public Value $modulus(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();
        return wrap(new DollarRange(
                                           Range.range(DollarFactory.fromValue(range.lowerEndpoint().$modulus(rhsFix)),
                                                       range.lowerBoundType(),
                                                       DollarFactory.fromValue(range.upperEndpoint().$modulus(rhsFix)),
                                                       range.upperBoundType())
                                           , reversed));

    }

    @NotNull
    @Override
    public Value $multiply(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();
        return wrap(new DollarRange(
                                           Range.range(DollarFactory.fromValue(range.lowerEndpoint().$multiply(rhsFix)),
                                                       range.lowerBoundType(),
                                                       DollarFactory.fromValue(range.upperEndpoint().$multiply(rhsFix)),
                                                       range.upperBoundType())
                                           , reversed));

    }

    @NotNull
    @Override
    public Value $negate() {
        return wrap(new DollarRange(
                                           Range.range(DollarFactory.fromValue(range.lowerEndpoint().$negate()),
                                                       range.lowerBoundType(),
                                                       DollarFactory.fromValue(range.upperEndpoint().$negate()),
                                                       range.upperBoundType())
                                           , reversed));
    }

    @NotNull
    @Override
    public Value $plus(@NotNull Value rhs) {
        Value rhsFix = rhs.$fixDeep();

        return wrap(new DollarRange(
                                           Range.range(DollarFactory.fromValue(range.lowerEndpoint().$plus(rhsFix)),
                                                       range.lowerBoundType(),
                                                       DollarFactory.fromValue(range.upperEndpoint().$plus(rhsFix)),
                                                       range.upperBoundType())
                                           , reversed));

    }

    @NotNull
    @Override
    public Value $prepend(@NotNull Value value) {
        return $get(DollarStatic.$(toVarList())).$prepend(value);
    }

    @NotNull
    @Override
    public Value $remove(@NotNull Value value) {
        throw new DollarFailureException(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public Value $removeByKey(@NotNull String value) {
        return $copy();

    }

    @NotNull
    @Override
    public Value $set(@NotNull Value key, @NotNull Object value) {
        throw new DollarFailureException(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public Type $type() {
        return new Type(Type._RANGE, constraintLabel());
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type.is(Type._RANGE)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean neitherTrueNorFalse() {
        return true;
    }

    @Override
    public boolean range() {
        return true;
    }

    @NotNull
    @Override
    public Value remove(@NotNull Object value) {
        throw new DollarFailureException(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public int size() {
        return Math.abs(diff().toInteger());
    }

    @NotNull
    @Override
    public String toDollarScript() {
        return "((" + lower().toDollarScript() + ")..(" + upper().toDollarScript() + "))";
    }

    @NotNull
    @Override
    public String toHumanString() {
        return String.format("%s..%s", lower(), upper());
    }

    @Override
    public int toInteger() {
        return diff().toInteger();
    }

    @NotNull
    @Override
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return null;
    }

    @NotNull
    @Override
    public Range toJavaObject() {
        return range;
    }

    @NotNull
    @Override
    public ImmutableList<Object> toList() {
        List<Object> values = new ArrayList<>();
        Value start = lower();
        Value finish = upper();
        for (Value i = start; i.compareTo(finish) <= 0; i = i.$inc()) {
            values.add(i.toJavaObject());
        }
        return ImmutableList.copyOf(values);
    }

    @Override
    public ImmutableList<String> toStrings() {
        throw new DollarFailureException(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public ImmutableList<Value> toVarList() {
        List<Value> values = new ArrayList<>();
        Value start = lower();
        Value finish = upper();
        if (start.compareTo(finish) < 1) {
            for (Value i = start; i.compareTo(finish) <= 0; i = i.$inc()) {
                values.add(i);
            }
        } else {
            for (Value i = start; i.compareTo(finish) <= 0; i = i.$dec()) {
                values.add(i);
            }
        }
        return ImmutableList.copyOf(values);
    }

    @NotNull
    @Override
    public ImmutableMap<Value, Value> toVarMap() {
        @NotNull Value result;
        throw new DollarFailureException(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public String toYaml() {
        @NotNull Value result;
        throw new DollarFailureException(ErrorType.INVALID_RANGE_OPERATION);
    }

    @Override
    public boolean truthy() {
        return !range.isEmpty();
    }

    @NotNull
    @Override
    public Value $avg(boolean parallel) {
        //TODO: can we calculate without iteration?
        return $sum(parallel).$divide($size());
    }

    @NotNull
    @Override
    public Value $max(boolean parallel) {
        return upper();
    }

    @NotNull
    @Override
    public Value $min(boolean parallel) {
        return lower();
    }

    @NotNull
    @Override
    public Value $size() {
        return diff().$abs();
    }

    @Override
    public boolean collection() {
        return true;
    }

    @NotNull
    @Override
    public Value $sort(boolean parallel) {
        return this;
    }

    @NotNull
    @Override
    public Value $unique(boolean parallel) {
        return this;
    }

    @Override
    protected @NotNull Stream<Value> getStream(boolean chain) {
        return toVarList().stream();
    }

    @NotNull
    @Override
    public Value $reverse(boolean parallel) {
        return wrap(new DollarRange(range, !reversed));
    }

    @Override
    public double toDouble() {
        return diff().toDouble();
    }

    @NotNull
    @Override
    public long toLong() {
        return diff().toLong();
    }

    @Override
    public int hashCode() {
        return range.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Value) {
            Value unwrapped = ((Value) obj).$fixDeep().$unwrap();
            if (unwrapped instanceof DollarRange) {
                return range.equals(((DollarRange) unwrapped).range);
            }
        }
        return false;

    }

    @Override
    public int compareTo(@NotNull Value o) {
        if ($containsValue(o).isTrue()) {
            return 0;
        }
        if (lower().compareTo(o) < 0) {
            return -1;
        }
        if (upper().compareTo(o) > 0) {
            return 1;
        }
        throw new IllegalStateException();
    }

    @NotNull
    private Value diff() {
        Value upper = range.upperEndpoint();
        assert upper != null;
        Value lower = range.lowerEndpoint();
        assert lower != null;

        // (1..3) 2,2 [1..3] 1,3
        if (!range.hasUpperBound()) {
            return INFINITY;
        }
        if (!range.hasLowerBound()) {
            return INFINITY;
        }
        if (range.upperBoundType().equals(BoundType.OPEN)) {
            upper = upper.$dec();
        }

        if (range.lowerBoundType().equals(BoundType.OPEN)) {
            lower = lower.$inc();
        }
        if (upper.compareTo(lower) < 0) {
            return $void();
        }
        if (upper.integer()) {
            if (upper.toLong() == lower.toLong()) {
                return DollarFactory.fromValue(1);
            }
            final long diff = reversed ? (lower.toLong() - upper.toLong()) : (upper.toLong() - lower.toLong());
            return DollarFactory.fromValue(diff + (long) Math.signum(diff));
        }
        if (upper.decimal()) {
            if (upper.toDouble() == lower.toDouble()) {
                return DollarFactory.fromValue(1.0);
            }
            final double diff = reversed ? (lower.toDouble() - upper.toDouble()) : (upper.toDouble() - lower.toDouble());
            return DollarFactory.fromValue(diff + Math.signum(diff));
        }
        int count = 0;
        Value start = lower;
        Value finish = upper;
        if (start.compareTo(finish) < 1) {
            for (Value i = start; i.compareTo(finish) <= 0; i = i.$inc()) {
                count++;
            }
        } else {
            for (Value i = start; i.compareTo(finish) <= 0; i = i.$dec()) {
                count--;
            }
        }
        return DollarFactory.fromValue(count);

    }

    @NotNull
    private Value lower() {
        if (reversed) {
            return range.upperEndpoint();
        } else {
            return range.lowerEndpoint();
        }
    }

    @NotNull
    private Value upper() {
        if (reversed) {
            return range.lowerEndpoint();
        } else {
            return range.upperEndpoint();
        }
    }


}
