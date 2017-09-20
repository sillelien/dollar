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
import dollar.api.DollarStatic;
import dollar.api.MetaKey;
import dollar.api.Pipeable;
import dollar.api.Signal;
import dollar.api.StateTracer;
import dollar.api.SubType;
import dollar.api.Type;
import dollar.api.TypePrediction;
import dollar.api.Value;
import dollar.api.json.ImmutableJsonObject;
import dollar.api.monitor.DollarMonitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.stream.Stream;

public class DollarWrapper implements Value {

    @Nullable
    private final Value value;
    @NotNull
    private DollarMonitor monitor;
    @NotNull
    private StateTracer tracer;

    public DollarWrapper(@Nullable Value value) {
        this.value = value;
        if (value == null) {
            throw new NullPointerException();
        }
    }

    DollarWrapper(@Nullable Value value, @NotNull DollarMonitor monitor, @NotNull StateTracer tracer) {
//        tracer.trace(DollarNull.INSTANCE,value, StateTracer.Operations.CREATE);
        this.value = value;
        this.monitor = monitor;
        this.tracer = tracer;
        if (value == null) {
            throw new NullPointerException();
        }
    }

    @NotNull
    private static String sanitize(@NotNull String location) {
        return location.replaceAll("[^\\w.]+", "_");

    }

    @NotNull
    @Override
    public Value $abs() {
        return getValue().$abs();
    }

    @NotNull
    @Override
    public Value $all() {
        return getValue().$all();
    }

    @NotNull
    @Override
    public Value $append(@NotNull Value value) {
        return getValue().$append(value);
    }

    @NotNull
    @Override
    public Value $as(@NotNull Type type) {
        return getValue().$as(type);
    }

    @NotNull
    @Override
    public Value $avg(boolean parallel) {
        return getValue().$avg(parallel);
    }

    @NotNull
    @Override
    public Value $choose(@NotNull Value map) {
        return tracer.trace(this, getValue().$choose(map), StateTracer.Operations.CHOOSE);
    }

    @Override
    public Value $constrain(@NotNull Value constraint, SubType source) {
        return getValue().$constrain(constraint, source);
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
    public Value $copy() {
        return getValue().$copy();
    }

    @NotNull
    @Override
    public Value $copy(@NotNull ImmutableList<Throwable> errors) {
        return getValue().$copy();
    }

    @NotNull
    @Override
    public Value $default(@NotNull Value v) {
        return getValue().$default(v);
    }

    @NotNull
    @Override
    public Value $dispatch(@NotNull Value lhs) {
        return getValue().$dispatch(lhs);

    }

    @NotNull
    @Override
    public Value $divide(@NotNull Value rhs) {
        return getValue().$divide(rhs);
    }

    @NotNull
    @Override
    public Value $drain() {
        return getValue().$drain();
    }

    @NotNull
    @Override
    public Value $each(@NotNull Pipeable pipe) {
        return getValue().$each(pipe);
    }

    @NotNull
    @Override
    public Value $fix(boolean parallel) {
        return getValue().$fix(parallel);
    }

    @NotNull
    @Override
    public Value $fix(int depth, boolean parallel) {
        return getValue().$fix(depth, parallel);
    }

    @NotNull
    @Override
    public Value $fixDeep(boolean parallel) {
        return getValue().$fixDeep(parallel);
    }

    @NotNull
    @Override
    public Value $get(@NotNull Value rhs) {
        return getValue().$get(rhs);
    }

    @NotNull
    @Override
    public Value $give(@NotNull Value lhs) {
        return getValue().$give(lhs);

    }

    @NotNull
    @Override
    public Value $has(@NotNull Value key) {
        return DollarStatic.$(getValue().$has(key));
    }

    @NotNull
    @Override
    public Value $insert(@NotNull Value value, int position) {
        return getValue().$insert(value, position);
    }

    @NotNull
    @Override
    public Value $isEmpty() {
        return DollarStatic.$(getValue().$isEmpty());
    }

    @NotNull
    @Override
    public Value $listen(@NotNull Pipeable pipe) {
        return getValue().$listen(pipe);
    }

    @NotNull
    @Override
    public Value $listen(@NotNull Pipeable pipe, @NotNull String key) {
        return getValue().$listen(pipe, key);
    }

    @NotNull
    @Override
    public Value $max(boolean parallel) {
        return getValue().$max(parallel);
    }

    @NotNull
    @Override
    public Value $mimeType() {
        return getValue().$mimeType();
    }

    @NotNull
    @Override
    public Value $min(boolean parallel) {
        return getValue().$min(parallel);
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

    @Override
    public Value $notify(NotificationType type, Value value) {
        return getValue().$notify(type, value);
    }

    @NotNull
    @Override
    public Value $peek() {
        return getValue().$peek();

    }

    @NotNull
    @Override
    public Value $plus(@NotNull Value rhs) {
        return getValue().$plus(rhs);
    }

    @NotNull
    @Override
    public Value $poll() {
        return getValue().$poll();
    }

    @NotNull
    @Override
    public Value $pop() {
        return tracer.trace(DollarVoid.INSTANCE,
                            monitor.run("pop",
                                        "dollar.persist.temp.pop",
                                        "Popping value from ",
                                        () -> getValue().$pop()),
                            StateTracer.Operations.POP);
    }

    @NotNull
    @Override
    public Value $prepend(@NotNull Value value) {
        return getValue().$prepend(value);
    }

    @NotNull
    @Override
    public Value $product(boolean parallel) {
        return getValue().$product(parallel);
    }

    @NotNull
    @Override
    public Value $publish(@NotNull Value lhs) {
        return getValue().$publish(lhs);

    }

    @NotNull
    @Override
    public Value $push(@NotNull Value lhs) {
        return getValue().$push(lhs);

    }

    @NotNull
    @Override
    public Value $read(boolean blocking, boolean mutating) {
        return getValue().$read(blocking, mutating);
    }

    @NotNull
    @Override
    public Value $read() {
        return getValue().$read();
    }

    @NotNull
    @Override
    public Value $remove(@NotNull Value value) {
        return getValue().$remove(value);
    }

    @NotNull
    @Override
    public Value $removeByKey(@NotNull String key) {
        return tracer.trace(this, getValue().$removeByKey(key), StateTracer.Operations.REMOVE_BY_KEY, key);
    }

    @NotNull
    @Override
    public Value $reverse(boolean parallel) {
        return getValue().$reverse(parallel);
    }

    @NotNull
    @Override
    public Value $set(@NotNull Value key, @NotNull Object value) {
        return tracer.trace(this, getValue().$set(key, value), StateTracer.Operations.SET, key, value);
    }

    @NotNull
    @Override
    public Value $size() {
        return DollarStatic.$(getValue().$size());
    }

    @NotNull
    @Override
    public Value $sort(boolean parallel) {
        return getValue().$sort(parallel);
    }

    @NotNull
    @Override
    public Value $stream(boolean parallel) {
        return getValue().$stream(false);
    }

    @NotNull
    @Override
    public Value $subscribe(@NotNull Pipeable subscription) {
        return getValue().$subscribe(subscription);
    }

    @NotNull
    @Override
    public Value $subscribe(@NotNull Pipeable subscription, @NotNull String key) {
        return getValue().$subscribe(subscription, key);
    }

    @NotNull
    @Override
    public Value $sum(boolean parallel) {
        return getValue().$sum(parallel);
    }

    @NotNull
    @Override
    public Type $type() {
        return getValue().$type();
    }

    @NotNull
    @Override
    public Value $unique(boolean parallel) {
        return getValue().$unique(parallel);
    }

    @NotNull
    @Override
    public Value $unwrap() {
        return getValue().$unwrap();
    }

    @NotNull
    @Override
    public Value $write(@NotNull Value value, boolean blocking, boolean mutating) {
        return getValue().$write(value, blocking, mutating);
    }

    @NotNull
    @Override
    public Value $write(@NotNull Value value) {
        return getValue().$write(value);
    }

    @Override
    public boolean collection() {
        return getValue().collection();
    }

    @NotNull
    @Override
    public SubType constraintLabel() {
        return getValue().constraintLabel();
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
        return getValue().decimal();
    }

    @Override
    public boolean dynamic() {
        return getValue().dynamic();
    }

    @NotNull
    @Override
    public Value err() {
        return getValue().err();

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
        return getValue().integer();
    }

    @Override
    public boolean is(@NotNull Type... types) {
        return getValue().is(types);
    }

    @Override
    public boolean isBoolean() {
        return getValue().isBoolean();
    }

    @Override
    public boolean isFalse() {
        return getValue().isFalse();
    }

    @Override
    public boolean isTrue() {
        return getValue().isTrue();
    }

    @Override
    public boolean isVoid() {
        return getValue().isVoid();
    }

    @Override
    public boolean list() {
        return getValue().list();
    }

    @Override
    public boolean map() {
        return getValue().map();
    }

    @NotNull
    @Override
    public Object meta(@NotNull MetaKey key) {
        return getValue().meta(key);
    }

    @Override
    public void meta(@NotNull MetaKey key, @NotNull Object value) {
        getValue().meta(key, value);

    }

    @Override
    public void metaAttribute(@NotNull MetaKey key, @NotNull String value) {
        getValue().metaAttribute(key, value);
    }

    @NotNull
    @Override
    public String metaAttribute(@NotNull MetaKey key) {
        return getValue().metaAttribute(key);
    }

    @Override
    public boolean neitherTrueNorFalse() {
        return getValue().neitherTrueNorFalse();
    }

    @Override
    public boolean number() {
        return getValue().number();
    }

    @NotNull
    @Override
    public Value out() {
        return getValue().out();
    }

    @Override
    public boolean pair() {
        return getValue().pair();
    }

    @NotNull
    @Override
    public TypePrediction predictType() {
        return getValue().predictType();
    }

    @Override
    public boolean queue() {
        return getValue().queue();
    }

    @NotNull
    @Override
    public Value remove(@NotNull Object value) {
        return tracer.trace(this, getValue().remove(value), StateTracer.Operations.REMOVE_BY_VALUE, value);
    }

    @Override
    public int sign() {
        return getValue().sign();
    }

    @Override
    public boolean singleValue() {
        return getValue().singleValue();
    }

    @Override
    public int size() {
        return getValue().size();
    }

    @Override
    public @NotNull Stream<Value> stream(boolean parallel) {
        return getValue().stream(parallel);
    }

    @Override
    public boolean string() {
        return getValue().string();
    }

    @NotNull
    @Override
    public String toDollarScript() {
        return getValue().toString();
    }

    @Override
    public double toDouble() {
        return getValue().toDouble();
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

    @Override
    public long toLong() {
        return getValue().toLong();
    }

    @NotNull
    @Override
    public Number toNumber() {
        return getValue().toNumber();
    }

    @NotNull
    @Override
    public InputStream toStream() {
        return getValue().toStream();
    }

    @Override
    public ImmutableList<String> toStrings() {
        return getValue().toStrings();
    }

    @NotNull
    @Override
    public ImmutableList<Value> toVarList() {
        return getValue().toVarList();
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

    @Override
    public boolean uri() {
        return getValue().uri();
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

    @NotNull
    public Value $set(@NotNull String key, Object value) {
        return tracer.trace(null, getValue().$set(DollarStatic.$(key), value), StateTracer.Operations.SET, key, value);
    }

    @Override
    public int compareTo(@NotNull Value o) {
        return getValue().compareTo(o.$unwrap());
    }

    @NotNull
    Value getValue() {
        if (value == null) {
            throw new IllegalStateException("Value has become null!!");
        }
        return value;
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


}
