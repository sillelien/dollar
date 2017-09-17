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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import dollar.api.DollarStatic;
import dollar.api.Type;
import dollar.api.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public final class DollarInfinity extends AbstractDollar {


    @NotNull
    public static final Value INSTANCE = DollarFactory.INFINITY;
    private final boolean positive;


    public DollarInfinity(boolean positive) {

        super();
        this.positive = positive;
    }

    @NotNull
    @Override
    public Value $abs() {
        return DollarFactory.wrap(new DollarInfinity(true));
    }

    @NotNull
    @Override
    public Value $append(@NotNull Value value) {
        return this;
    }

    @NotNull
    @Override
    public Value $as(@NotNull Type type) {
        if (type.is(Type._BOOLEAN)) {
            return DollarStatic.$(true);
        } else if (type.is(Type._STRING)) {
            return DollarStatic.$(positive ? "infinity" : "-infinity");
        } else if (type.is(Type._LIST)) {
            return DollarStatic.$(Collections.singletonList(this));
        } else if (type.is(Type._MAP)) {
            return DollarStatic.$("value", this);
        } else if (type.is(Type._DECIMAL)) {
            return DollarStatic.$(positive ? Double.MAX_VALUE : Double.MIN_VALUE);
        } else if (type.is(Type._INTEGER)) {
            return DollarStatic.$(positive ? Long.MAX_VALUE : Long.MIN_VALUE);
        } else if (type.is(Type._VOID)) {
            return DollarStatic.$void();
        } else if (type.is(Type._DATE)) {
            return this;
        } else if (type.is(Type._RANGE)) {
            return DollarFactory.fromValue(Range.closed($get(DollarStatic.$(0)), $get(DollarStatic.$(0))));
        } else {
            return DollarFactory.failure(ErrorType.INVALID_CAST, type.toString(), false);
        }
    }

    @NotNull
    @Override
    public Value $containsKey(@NotNull Value value) {
        return DollarStatic.$(false);
    }

    @NotNull
    @Override
    public Value $containsValue(@NotNull Value value) {
        return DollarStatic.$(false);
    }

    @NotNull
    @Override
    public Value $divide(@NotNull Value rhs) {
        return this;
    }

    @NotNull
    @Override
    public Value $get(@NotNull Value key) {
        return this;
    }

    @NotNull
    @Override
    public Value $has(@NotNull Value key) {
        return DollarStatic.$(false);
    }

    @NotNull
    @Override
    public Value $insert(@NotNull Value value, int position) {
        return this;
    }

    @NotNull
    @Override
    public Value $minus(@NotNull Value rhs) {
        return this;
    }

    @NotNull
    @Override
    public Value $modulus(@NotNull Value rhs) {
        return this;
    }

    @NotNull
    @Override
    public Value $multiply(@NotNull Value v) {
        if (v.isVoid()) {
            return DollarStatic.$void();
        }
        if (v.$isEmpty().isTrue()) {
            return DollarFactory.fromValue(0);
        }
        boolean
                positiveResult =
                (positive && v.positive()) || (!(positive && negative()) && !(!positive && v.positive()));
        return DollarFactory.wrap(new DollarInfinity(positiveResult));
    }

    @NotNull
    @Override
    public Value $negate() {
        return DollarFactory.wrap(new DollarInfinity(!positive));
    }

    @NotNull
    @Override
    public Value $plus(@NotNull Value rhs) {
        return this;
    }

    @NotNull
    @Override
    public Value $prepend(@NotNull Value value) {
        return this;
    }

    @NotNull
    @Override
    public Value $remove(@NotNull Value value) {
        return this;
    }

    @NotNull
    @Override
    public Value $removeByKey(@NotNull String value) {
        return this;
    }

    @NotNull
    @Override
    public Value $set(@NotNull Value key, @NotNull Object value) {
        return this;
    }

    @NotNull
    @Override
    public Value $size() {
        return this;
    }

    @NotNull
    @Override
    public Type $type() {
        return Type._INFINITY;
    }

    @Override
    public boolean collection() {
        return false;
    }

    @Override
    public boolean infinite() {
        return false;
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type.is(Type._INFINITY) || type.is(Type._INTEGER) || type.is(Type._DECIMAL)) {
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

    @NotNull
    @Override
    public Value remove(@NotNull Object value) {
        return this;
    }

    @Override
    public int sign() {
        return positive ? 1 : -1;
    }

    @Override
    public int size() {
        return toNumber().intValue();
    }

    @NotNull
    @Override
    public String toDollarScript() {
        return positive ? "infinity" : "-infinity";
    }

    @NotNull
    @Override
    public String toHumanString() {
        return positive ? "infinity" : "-infinity";
    }

    @Override
    public int toInteger() {
        return positive ? Integer.MAX_VALUE : Integer.MIN_VALUE;
    }

    @NotNull
    @Override
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return ImmutableMap.copyOf(Collections.singletonMap((K) "value", (V) toNumber()));
    }

    @Nullable
    @Override
    public <R> R toJavaObject() {
        return null;
    }

    @NotNull
    @Override
    public ImmutableList<Object> toList() {
        return ImmutableList.of(toNumber());
    }

    @NotNull
    @Override
    public Number toNumber() {
        return positive ? Double.MAX_VALUE : Double.MIN_VALUE;
    }

    @Override
    public ImmutableList<String> toStrings() {
        return ImmutableList.of();
    }

    @NotNull
    @Override
    public ImmutableList<Value> toVarList() {
        return ImmutableList.of(this);
    }

    @NotNull
    @Override
    public ImmutableMap<Value, Value> toVarMap() {
        return ImmutableMap.of(DollarStatic.$("value"), this);
    }

    @NotNull
    @Override
    public String toYaml() {
        return "infinity: " + (positive ? "positive" : "negative");
    }

    @Override
    public boolean truthy() {
        return true;
    }

    @Override
    public int compareTo(@NotNull Value o) {
        if (o.infinite()) {
            return Integer.compare(sign(), o.sign());
        } else {
            if (positive) {
                return 1;
            } else {
                return -1;
            }
        }

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Value) {
            Value o = ((Value) other).$fixDeep().$unwrap();
            if (this == o) { return true; }
            if ((o == null) || (getClass() != o.getClass())) { return false; }
            DollarInfinity that = (DollarInfinity) o;

            return positive == that.positive;
        } else {
            return false;
        }

    }


}
