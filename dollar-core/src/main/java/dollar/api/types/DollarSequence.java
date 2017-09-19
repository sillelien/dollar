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

import com.github.oxo42.stateless4j.StateMachine;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dollar.api.DollarException;
import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.Signal;
import dollar.api.SubType;
import dollar.api.Type;
import dollar.api.Value;
import dollar.api.exceptions.DollarFailureException;
import dollar.api.json.ImmutableJsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dollar.api.DollarStatic.$void;
import static dollar.api.types.DollarFactory.wrap;

public class DollarSequence extends AbstractDollar {


    @NotNull
    private final List<Value> list;
    private final boolean parallel;


    public DollarSequence(@NotNull Stream<Value> stream) {
        super();
        parallel = stream.isParallel();
        list = stream.collect(Collectors.toList());
    }

    public DollarSequence(@NotNull List<Value> list, boolean parallel) {
        super();
        this.list = list;
        this.parallel = parallel;
    }


    @NotNull
    @Override
    public Value $abs() {
        return getValue().$abs();
    }

    @NotNull
    @Override
    public Value $append(@NotNull Value value) {
        ArrayList<Value> values = new ArrayList<>(toVarList());
        values.add(value);
        return wrapList(values);
    }

    @NotNull
    @Override
    public Value $as(@NotNull Type type) {
        return getValue().$as(type);
    }

    @NotNull
    @Override
    public Value $containsKey(@NotNull Value value) {
        return getValue().$containsKey(value);
    }

    @NotNull
    @Override
    public Value $containsValue(@NotNull Value value) {
        return getValue().$containsValue(value);
    }

    @NotNull
    @Override
    public Value $divide(@NotNull Value rhs) {
        return getValue().$divide(rhs);
    }

    @NotNull
    @Override
    public Value $get(@NotNull Value rhs) {
        return getValue().$get(rhs);
    }

    @NotNull
    @Override
    public Value $has(@NotNull Value key) {
        return getValue().$has(key);
    }

    @NotNull
    @Override
    public Value $insert(@NotNull Value value, int position) {
        throw new DollarFailureException(ErrorType.INVALID_SEQUENCE_OPERATION);
    }

    @NotNull
    @Override
    public Value $isEmpty() {
        return DollarFactory.fromValue(getStream(false).count() == 0);
    }


    @NotNull
    @Override
    public Value $mimeType() {
        return getValue().$mimeType();
    }

    @NotNull
    @Override
    public Value $minus(@NotNull Value rhs) {
        return getValue().$minus(rhs);
    }

    @NotNull
    @Override
    public Value $modulus(@NotNull Value rhs) {
        return getValue().$modulus(rhs);
    }

    @NotNull
    @Override
    public Value $multiply(@NotNull Value v) {
        return getValue().$multiply(v);
    }

    @NotNull
    @Override
    public Value $negate() {
        return getValue().$negate();
    }


    @NotNull
    @Override
    public Value $plus(@NotNull Value rhs) {
        return $append(rhs);
    }

    @NotNull
    @Override
    public Value $prepend(@NotNull Value value) {
        throw new DollarFailureException(ErrorType.INVALID_SEQUENCE_OPERATION);
    }

    @NotNull
    @Override
    public Value $remove(@NotNull Value value) {
        throw new DollarFailureException(ErrorType.INVALID_SEQUENCE_OPERATION);
    }

    @NotNull
    @Override
    public Value $removeByKey(@NotNull String key) {
        throw new DollarFailureException(ErrorType.INVALID_SEQUENCE_OPERATION);
    }

    @NotNull
    @Override
    public Value $set(@NotNull Value key, @NotNull Object value) {
        throw new DollarFailureException(ErrorType.INVALID_SEQUENCE_OPERATION);
    }

    @NotNull
    @Override
    public Value $size() {
        return DollarFactory.fromValue(getStream(false).count());
    }

    @NotNull
    @Override
    public Value $stream(boolean parallel) {
        return this;
    }

    @NotNull
    @Override
    public Value $subscribe(@NotNull Pipeable subscription) {
        return $each(subscription);
    }

    @NotNull
    @Override
    public Value $subscribe(@NotNull Pipeable subscription, @NotNull String key) {
        return $each(subscription);
    }

    @NotNull
    @Override
    public Type $type() {
        return Type._SEQUENCE;
    }

    @NotNull
    @Override
    public Value $write(@NotNull Value value) {
        throw new DollarFailureException(ErrorType.INVALID_SEQUENCE_OPERATION);
    }

    @Override
    public boolean collection() {
        return true;
    }

    @NotNull
    @Override
    public Value err() {
        return getValue().err();

    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type.is(Type._SEQUENCE)) {
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
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean neitherTrueNorFalse() {
        return true;
    }

    @NotNull
    @Override
    public Value out() {
        return getValue().out();
    }

    @NotNull
    @Override
    public Value remove(@NotNull Object value) {
        throw new DollarFailureException(ErrorType.INVALID_SEQUENCE_OPERATION);
    }

    @Override
    public int sign() {
        return getValue().sign();
    }

    @Override
    public int size() {
        return (int) getStream(false).count();
    }

    @Override
    public @NotNull Stream<Value> stream(boolean parallel) {
        return getStream(false);
    }

    @NotNull
    @Override
    public String toDollarScript() {
        return getValue().toString();
    }

    @NotNull
    @Override
    public String toHumanString() {
        return getValue().toHumanString();
    }

    @Override
    public int toInteger() {
        return getValue().toInteger();
    }

    @NotNull
    @Override
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return getValue().toJavaMap();
    }

    @Nullable
    @Override
    public <R> R toJavaObject() {
        return getValue().toJavaObject();
    }

    @NotNull
    @Override
    public ImmutableJsonObject toJsonObject() {
        return getValue().toJsonObject();
    }

    @NotNull
    @Override
    public ImmutableList<?> toList() {
        return getValue().toList();
    }

    @NotNull
    @Override
    public Number toNumber() {
        return getValue().toNumber();
    }

    @Override
    public ImmutableList<String> toStrings() {
        return ImmutableList.copyOf(getStream(false).map(Value::toString).collect(Collectors.toList()));
    }

    @NotNull
    @Override
    public ImmutableList<Value> toVarList() {
        return ImmutableList.copyOf(getStream(false).collect(Collectors.toList()));
    }

    @NotNull
    @Override
    public ImmutableMap<Value, Value> toVarMap() {
        return getValue().toVarMap();
    }

    @NotNull
    @Override
    public String toYaml() {
        return getValue().toYaml();
    }

    @Override
    public boolean truthy() {
        return getValue().truthy();
    }

    @NotNull
    @Override
    public Value $all() {
        return getValue();
    }

    @NotNull
    @Override
    public Value $avg(boolean parallel) {
        return $sum(parallel).$divide($size());
    }

    @NotNull
    @Override
    public Value $choose(@NotNull Value map) {
        return getValue().$choose(map);
    }

    @Override
    public Value $constrain(@NotNull Value constraint, SubType source) {
        return getValue().$constrain(constraint, source);
    }

    @NotNull
    @Override
    public Value $copy() {
        return wrap(new DollarSequence(getStream(false)));
    }

    @NotNull
    @Override
    public Value $copy(@NotNull ImmutableList<Throwable> errors) {
        return getValue().$copy();
    }

    @NotNull
    @Override
    public Value $default(@NotNull Value v) {
        return (getStream(false).count() == 0) ? v : getValue();
    }

    @NotNull
    @Override
    public Value $drain() {
        throw new DollarFailureException(ErrorType.INVALID_SEQUENCE_OPERATION);
    }

    @NotNull
    @Override
    public Value $each(@NotNull Pipeable pipe) {
        return eachInternal(pipe);
    }

    @NotNull
    @Override
    public Value $fix(int depth, boolean parallel) {
        return getValue().$fix(depth, parallel);
    }

    @NotNull
    @Override
    public Value $max(boolean parallel) {
        return getStream(false).max(Comparator.comparing(Value::toDouble)).orElseThrow(
                () -> new DollarException("Null encountered"));
    }

    @NotNull
    @Override
    public Value $min(boolean parallel) {
        return getStream(false).min(Comparator.comparing(Value::toDouble)).orElseThrow(
                () -> new DollarException("Null encountered"));
    }

    @NotNull
    @Override
    public Value $notify() {
        return getValue().$notify();
    }

    @NotNull
    @Override
    public Value $product(boolean parallel) {
        return getStream(false).reduce(Value::$multiply).orElse($void());
    }

    @NotNull
    @Override
    public Value $publish(@NotNull Value lhs) {
        return getValue().$publish(lhs);

    }

    @NotNull
    @Override
    public Value $read(boolean blocking, boolean mutating) {
        return getValue().$read(blocking, mutating);
    }

    @NotNull
    @Override
    public Value $reverse(boolean parallel) {
        throw new DollarFailureException(ErrorType.INVALID_SEQUENCE_OPERATION);
    }

    @NotNull
    @Override
    public Value $sort(boolean parallel) {
        return wrapStream(getStream(true).sorted());
    }

    @NotNull
    @Override
    public Value $sum(boolean parallel) {
        return DollarFactory.fromValue(
                getStream(false).reduce(Value::$plus).orElseThrow(() -> new DollarException("Reduce returned null")));
    }

    @NotNull
    @Override
    public Value $unique(boolean parallel) {
        return wrapStream(getStream(true).distinct());
    }

    @NotNull
    @Override
    public Value $unwrap() {
        return this;
    }

    @NotNull
    @Override
    public Value $write(@NotNull Value value, boolean blocking, boolean mutating) {
        throw new DollarFailureException(ErrorType.INVALID_SEQUENCE_OPERATION);
    }

    @NotNull
    @Override
    public Value debug(@NotNull Object message) {
        return getValue().debug(message);
    }

    @NotNull
    @Override
    public Value debug() {
        return getValue().debug();
    }

    @NotNull
    @Override
    public Value debugf(@NotNull String message, Object... values) {
        return getValue().debugf(message, values);
    }

    @Override
    public boolean decimal() {
        return false;
    }

    @Override
    public boolean dynamic() {
        return false;
    }

    @NotNull
    @Override
    public Value error(@NotNull Throwable exception) {
        return getValue().error(exception);
    }

    @NotNull
    @Override
    public Value error(@NotNull Object message) {
        return getValue().error(message);
    }

    @NotNull
    @Override
    public Value error() {
        return getValue().error();
    }

    @NotNull
    @Override
    public Value errorf(@NotNull String message, Object... values) {
        return getValue().errorf(message, values);
    }

    @NotNull
    @Override
    public Value info(@NotNull Object message) {
        return getValue().info(message);
    }

    @NotNull
    @Override
    public Value info() {
        return getValue().info();
    }

    @NotNull
    @Override
    public Value infof(@NotNull String message, Object... values) {
        return getValue().infof(message, values);
    }

    @Override
    public boolean integer() {
        return false;
    }

    @Override
    public boolean list() {
        return true;
    }

    @Override
    public boolean map() {
        return false;
    }

    @Override
    public boolean number() {
        return false;
    }

    @Override
    public boolean pair() {
        return false;
    }

    @Override
    public boolean queue() {
        return false;
    }

    @Override
    public boolean singleValue() {
        return size() == 1;
    }

    @Override
    public boolean string() {
        return getValue().string();
    }

    @Override
    public double toDouble() {
        return getValue().toDouble();
    }

    @Override
    public long toLong() {
        return getValue().toLong();
    }

    @NotNull
    @Override
    public InputStream toStream() {
        return getValue().toStream();
    }

    @Override
    public boolean uri() {
        return false;
    }

    @NotNull
    @Override
    public Value $create() {
        return getValue().$create();
    }

    @NotNull
    @Override
    public Value $destroy() {
        return getValue().$destroy();
    }

    @NotNull
    @Override
    public Value $pause() {
        return getValue().$pause();
    }

    @Override
    public void $signal(@NotNull Signal signal) {
        getValue().$signal(signal);
    }

    @NotNull
    @Override
    public Value $start() {
        return getValue().$start();
    }

    @NotNull
    @Override
    public Value $state() {
        return getValue().$state();
    }

    @NotNull
    @Override
    public Value $stop() {
        return getValue().$stop();
    }

    @NotNull
    @Override
    public Value $unpause() {
        return getValue().$unpause();
    }

    @NotNull
    @Override
    public StateMachine<ResourceState, Signal> getStateMachine() {
        return getValue().getStateMachine();
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return getValue().equals(obj);
    }

    @NotNull
    @Override
    public String toString() {
        return getValue().toString();
    }

    @NotNull
    public Value $set(@NotNull String key, Object value) {
        return getValue().$set(DollarStatic.$(key), value);
    }

    @Override
    public int compareTo(@NotNull Value o) {
        return getValue().compareTo(o.$unwrap());
    }

    @NotNull
    private Value eachInternal(@NotNull Pipeable pipe) {
        return wrapStream(getStream(true).map(i -> {
            try {
                return pipe.pipe(i);
            } catch (Exception e) {
                throw new DollarException(e);
            }
        }));
    }

    @NotNull
    private Stream<Value> getStream(boolean chain) {
        if (parallel) {
            return list.parallelStream();
        } else {
            return list.stream();
        }
    }

    @NotNull
    Value getValue() {
        return DollarFactory.fromList(getStream(false).collect(Collectors.toList()));
    }

    @NotNull
    private Value wrapList(ArrayList<Value> values) {
        return wrap(new DollarSequence(values.stream()));
    }

    @NotNull
    private Value wrapStream(Stream<Value> stream) {
        return wrap(new DollarSequence(stream));
    }


}
