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
import dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static dollar.api.DollarStatic.$void;
import static dollar.api.types.DollarFactory.INFINITY;
import static dollar.api.types.DollarFactory.wrap;

public class DollarRange extends AbstractDollar {

    @NotNull
    private final Range<var> range;
    private boolean reversed = false;

    public DollarRange(@NotNull ImmutableList<Throwable> errors, @NotNull var start, @NotNull var finish) {
        super(errors);
        var startUnwrap;
        var finishUnwrap;

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


    public DollarRange(@NotNull ImmutableList<Throwable> errors,
                       boolean lowerBounds,
                       boolean upperBounds,
                       boolean closedLeft,
                       boolean closedRight, @Nullable var lower, @Nullable var upper) {
        super(errors);
        var lowerBound;
        var upperBound;
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

    public DollarRange(@NotNull ImmutableList<Throwable> errors, @NotNull Range<var> range, boolean reversed) {
        super(errors);
        this.range = range;
        this.reversed = reversed;
    }

    @NotNull
    @Override
    public var $abs() {
        return wrap(new DollarRange(errors(),
                                    Range.range(DollarFactory.fromValue(range.lowerEndpoint().$abs()),
                                                range.lowerBoundType(),
                                                DollarFactory.fromValue(range.upperEndpoint().$abs()),
                                                range.upperBoundType())
                                           , reversed));
    }

    @NotNull
    @Override
    public var $minus(@NotNull var rhs) {
        var rhsFix = rhs.$fixDeep();
        return wrap(new DollarRange(errors(),
                                    Range.range(DollarFactory.fromValue(range.lowerEndpoint().$minus(rhsFix)),
                                                range.lowerBoundType(),
                                                DollarFactory.fromValue(range.upperEndpoint().$minus(rhsFix)),
                                                range.upperBoundType())
                                           , reversed));
    }

    @NotNull
    @Override
    public var $plus(@NotNull var rhs) {
        var rhsFix = rhs.$fixDeep();

        return wrap(new DollarRange(errors(),
                                    Range.range(DollarFactory.fromValue(range.lowerEndpoint().$plus(rhsFix)),
                                                range.lowerBoundType(),
                                                DollarFactory.fromValue(range.upperEndpoint().$plus(rhsFix)),
                                                range.upperBoundType())
                                           , reversed));

    }

    @NotNull
    @Override
    public var $negate() {
        return wrap(new DollarRange(errors(),
                                    Range.range(DollarFactory.fromValue(range.lowerEndpoint().$negate()),
                                                range.lowerBoundType(),
                                                DollarFactory.fromValue(range.upperEndpoint().$negate()),
                                                range.upperBoundType())
                                           , reversed));
    }

    @NotNull
    @Override
    public var $divide(@NotNull var rhs) {
        var rhsFix = rhs.$fixDeep();
        return wrap(new DollarRange(errors(),
                                    Range.range(DollarFactory.fromValue(range.lowerEndpoint().$divide(rhsFix)),
                                                range.lowerBoundType(),
                                                DollarFactory.fromValue(range.upperEndpoint().$divide(rhsFix)),
                                                range.upperBoundType())
                                           , reversed));

    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        var rhsFix = rhs.$fixDeep();
        return wrap(new DollarRange(errors(),
                                    Range.range(DollarFactory.fromValue(range.lowerEndpoint().$modulus(rhsFix)),
                                                range.lowerBoundType(),
                                                DollarFactory.fromValue(range.upperEndpoint().$modulus(rhsFix)),
                                                range.upperBoundType())
                                           , reversed));

    }

    @NotNull
    @Override
    public var $multiply(@NotNull var rhs) {
        var rhsFix = rhs.$fixDeep();
        return wrap(new DollarRange(errors(),
                                    Range.range(DollarFactory.fromValue(range.lowerEndpoint().$multiply(rhsFix)),
                                                range.lowerBoundType(),
                                                DollarFactory.fromValue(range.upperEndpoint().$multiply(rhsFix)),
                                                range.upperBoundType())
                                           , reversed));

    }

    @NotNull
    @Override
    public Integer toInteger() {
        return diff().toInteger();
    }

    private var lower() {
        if (reversed) {
            return range.upperEndpoint();
        } else {
            return range.lowerEndpoint();
        }
    }

    @NotNull
    private var diff() {
        var upper = range.upperEndpoint();
        assert upper != null;
        var lower = range.lowerEndpoint();
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
            if (upper.toLong().equals(lower.toLong())) {
                return DollarFactory.fromValue(1, errors());
            }
            final long diff = reversed ? (lower.toLong() - upper.toLong()) : (upper.toLong() - lower.toLong());
            return DollarFactory.fromValue(diff + (long) Math.signum(diff), errors());
        }
        if (upper.decimal()) {
            if (upper.toDouble().equals(lower.toDouble())) {
                return DollarFactory.fromValue(1.0, errors());
            }
            final double diff = reversed ? (lower.toDouble() - upper.toDouble()) : (upper.toDouble() - lower.toDouble());
            return DollarFactory.fromValue(diff + Math.signum(diff), errors());
        }
        int count = 0;
        var start = lower;
        var finish = upper;
        if (start.compareTo(finish) < 1) {
            for (var i = start; i.compareTo(finish) <= 0; i = i.$inc()) {
                count++;
            }
        } else {
            for (var i = start; i.compareTo(finish) <= 0; i = i.$dec()) {
                count--;
            }
        }
        return DollarFactory.fromValue(count, errors());

    }

    @NotNull
    @Override
    public var $as(@NotNull Type type) {
        if (type.is(Type._LIST)) {
            return DollarStatic.$(toVarList());
        } else if (type.is(Type._MAP)) {
            return DollarStatic.$(toJavaMap());
        } else if (type.is(Type._STRING)) {
            return DollarFactory.fromStringValue(toHumanString());
        } else if (type.is(Type._VOID)) {
            return $void();
        } else {
            return DollarFactory.failure(ErrorType.INVALID_CAST);
        }
    }

    @NotNull
    @Override
    public ImmutableList<var> toVarList() {
        List<var> values = new ArrayList<>();
        var start = lower();
        var finish = upper();
        if (start.compareTo(finish) < 1) {
            for (var i = start; i.compareTo(finish) <= 0; i = i.$inc()) {
                values.add(i);
            }
        } else {
            for (var i = start; i.compareTo(finish) <= 0; i = i.$dec()) {
                values.add(i);
            }
        }
        return ImmutableList.copyOf(values);
    }

    @NotNull
    @Override
    public Type $type() {
        return new Type(Type._RANGE, constraintLabel());
    }

    @Override
    public boolean collection() {
        return true;
    }

    @NotNull
    @Override
    public ImmutableMap<var, var> toVarMap() {
        DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
        return ImmutableMap.of();
    }

    @NotNull
    @Override
    public String toYaml() {
        DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
        return "";
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
    public boolean range() {
        return true;
    }

    @Override
    public ImmutableList<String> toStrings() {
        DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
        return null;
    }

    @NotNull
    @Override
    public ImmutableList<Object> toList() {
        List<Object> values = new ArrayList<>();
        var start = lower();
        var finish = upper();
        for (var i = start; i.compareTo(finish) <= 0; i = i.$inc()) {
            values.add(i.toJavaObject());
        }
        return ImmutableList.copyOf(values);
    }

    @NotNull
    @Override
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return null;
    }

    @NotNull
    @Override
    public String toHumanString() {
        return String.format("%s..%s", lower(), upper());
    }

    @NotNull
    @Override
    public String toDollarScript() {
        return "((" + lower().toDollarScript() + ")..(" + upper().toDollarScript() + "))";
    }

    @NotNull
    @Override
    public Range toJavaObject() {
        return range;
    }

    private var upper() {
        if (reversed) {
            return range.lowerEndpoint();
        } else {
            return range.upperEndpoint();
        }
    }

    @NotNull
    @Override
    public var $get(@NotNull var key) {
        if (key.integer()) {
            long keyL;
            if (key.toLong() < 0) {
                keyL = size() + key.toLong();
            } else {
                keyL = key.toLong();
            }
            var upper = range.upperEndpoint();
            assert upper != null;
            var lower = range.lowerEndpoint();
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
                if (upper.toLong().equals(lower.toLong()) && (keyL == 0)) {
                    return DollarFactory.fromValue(lower, errors());
                }
                final long result = reversed ? (upper.toLong() - keyL) : (lower.toLong() + keyL);
                return DollarFactory.fromValue(result, errors());
            }
            if (upper.decimal()) {
                if (upper.toDouble().equals(lower.toDouble()) && (keyL == 0)) {
                    return DollarFactory.fromValue(lower, errors());
                }
                final double diff = reversed ? (upper.toDouble() - keyL) : (lower.toDouble() + keyL);
                return DollarFactory.fromValue(diff + Math.signum(diff), errors());
            }
            if (upper.equals(lower) && (keyL == 0)) {
                return DollarFactory.fromValue(lower, errors());
            }
            if (!reversed) {
                var start = lower;
                for (long i = 0; i < keyL; i++) {
                    start = start.$inc();
                }
                return start;
            } else {
                var finish = upper;
                for (long i = 0; i < keyL; i++) {
                    finish = finish.$dec();
                }
                return finish;
            }

        }
        return DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public var $append(@NotNull var value) {
        return $plus(value);
    }

    @NotNull
    @Override
    public var $containsValue(@NotNull var value) {
        return DollarStatic.$(range.contains(value));
    }

    @NotNull
    @Override
    public var $containsKey(@NotNull var value) {
        return DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public var $has(@NotNull var key) {
        return DollarStatic.$(false);
    }

    @NotNull
    @Override
    public var $size() {
        return diff().$abs();
    }

    @NotNull
    @Override
    public var $prepend(@NotNull var value) {
        return $(toVarList()).$prepend(value);
    }

    @NotNull
    @Override
    public var $insert(@NotNull var value, int position) {
        return $(toVarList()).$insert(value, position);
    }

    @NotNull
    @Override
    public var $removeByKey(@NotNull String value) {
        return $copy();

    }

    @NotNull
    @Override
    public var $set(@NotNull var key, Object value) {
        return DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public var remove(@NotNull Object value) {
        return DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public var $remove(@NotNull var value) {
        return DollarFactory.failure(ErrorType.INVALID_RANGE_OPERATION);
    }

    @NotNull
    @Override
    public int size() {
        return Math.abs(diff().toInteger());
    }

    @Override
    public int compareTo(@NotNull var o) {
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

    @Override
    public int hashCode() {
        return range.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof var) {
            var unwrapped = ((var) obj).$fixDeep().$unwrap();
            if (unwrapped instanceof DollarRange) {
                return range.equals(((DollarRange) unwrapped).range);
            }
        }
        return false;

    }

    @Override
    public Double toDouble() {
        return diff().toDouble();
    }

    @NotNull
    @Override
    public Long toLong() {
        return diff().toLong();
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
    public boolean truthy() {
        return !range.isEmpty();
    }


    @NotNull
    @Override
    public var $min(boolean parallel) {
        return lower();
    }

    @NotNull
    @Override
    public var $max(boolean parallel) {
        return upper();
    }


    @NotNull
    @Override
    public var $avg(boolean parallel) {
        //TODO: can we calculate without iteration?
        return $sum(parallel).$divide($size());
    }

    @Override
    public var $reverse(boolean parallel) {
        return wrap(new DollarRange(errors(), range, !reversed));
    }

    @NotNull
    @Override
    public var $sort(boolean parallel) {
        return this;
    }

    @NotNull
    @Override
    public var $unique(boolean parallel) {
        return this;
    }


}
