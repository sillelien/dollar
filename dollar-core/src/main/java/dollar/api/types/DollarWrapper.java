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
import dollar.api.Pipeable;
import dollar.api.Signal;
import dollar.api.StateTracer;
import dollar.api.Type;
import dollar.api.TypePrediction;
import dollar.api.json.ImmutableJsonObject;
import dollar.api.monitor.DollarMonitor;
import dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.stream.Stream;

public class DollarWrapper implements var {

    @Nullable
    private final var value;
    @NotNull
    private DollarMonitor monitor;
    @NotNull
    private StateTracer tracer;

    public DollarWrapper(@Nullable var value) {
        this.value = value;
        if (value == null) {
            throw new NullPointerException();
        }
    }

    DollarWrapper(@Nullable var value, @NotNull DollarMonitor monitor, @NotNull StateTracer tracer) {
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
    public var $abs() {
        return getValue().$abs();
    }

    @NotNull
    @Override
    public var $minus(@NotNull var rhs) {
        return getValue().$minus(rhs);
    }

    @NotNull
    @Override
    public var $plus(@NotNull var rhs) {
        return getValue().$plus(rhs);
    }

    @NotNull
    @Override
    public var $negate() {
        return getValue().$negate();
    }

    @NotNull
    @Override
    public var $divide(@NotNull var rhs) {
        return getValue().$divide(rhs);
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        return getValue().$modulus(rhs);
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return getValue().$multiply(v);
    }

    @Override
    public int sign() {
        return getValue().sign();
    }

    @NotNull
    @Override
    public Double toDouble() {
        return getValue().toDouble();
    }

    @NotNull
    @Override
    public Integer toInteger() {
        return getValue().toInteger();
    }

    @NotNull
    @Override
    public Long toLong() {
        return getValue().toLong();
    }

    @NotNull
    @Override
    public Number toNumber() {
        return getValue().toNumber();
    }

    @NotNull
    @Override
    public var $all() {
        return getValue().$all();
    }

    @NotNull
    @Override
    public var $dispatch(@NotNull var lhs) {
        return getValue().$dispatch(lhs);

    }

    @NotNull
    @Override
    public var $write(@NotNull var value, boolean blocking, boolean mutating) {
        return getValue().$write(value, blocking, mutating);
    }

    @NotNull
    @Override
    public var $drain() {
        return getValue().$drain();
    }

    @NotNull
    @Override
    public var $give(@NotNull var lhs) {
        return getValue().$give(lhs);

    }

    @NotNull
    @Override
    public var $peek() {
        return getValue().$peek();

    }

    @NotNull
    @Override
    public var $read(boolean blocking, boolean mutating) {
        return getValue().$read(blocking, mutating);
    }

    @NotNull
    @Override
    public var $poll() {
        return getValue().$poll();
    }

    @NotNull
    @Override
    public var $pop() {
        return tracer.trace(DollarVoid.INSTANCE,
                            monitor.run("pop",
                                        "dollar.persist.temp.pop",
                                        "Popping value from ",
                                        () -> getValue().$pop()),
                            StateTracer.Operations.POP);
    }

    @NotNull
    @Override
    public var $publish(@NotNull var lhs) {
        return getValue().$publish(lhs);

    }

    @NotNull
    @Override
    public var $push(@NotNull var lhs) {
        return getValue().$push(lhs);

    }

    @NotNull
    @Override
    public var $read() {
        return getValue().$read();
    }

    @NotNull
    @Override
    public var $subscribe(@NotNull Pipeable subscription) {
        return getValue().$subscribe(subscription);
    }

    @NotNull
    @Override
    public var $listen(@NotNull Pipeable pipe, @NotNull String key) {
        return getValue().$listen(pipe, key);
    }

    @NotNull
    @Override
    public var $subscribe(@NotNull Pipeable subscription, @NotNull String key) {
        return getValue().$subscribe(subscription, key);
    }

    @NotNull
    @Override
    public var $write(@NotNull var value) {
        return getValue().$write(value);
    }

    @NotNull
    @Override
    public var $append(@NotNull var value) {
        return getValue().$append(value);
    }

    @Override
    public var $avg(boolean parallel) {
        return getValue().$avg(parallel);
    }

    @NotNull
    @Override
    public var $containsKey(@NotNull var value) {
        return getValue().$containsKey(value);
    }

    @NotNull
    @Override
    public var $containsValue(@NotNull var value) {
        return getValue().$containsValue(value);
    }

    @NotNull
    @Override
    public var $get(@NotNull var rhs) {
        return getValue().$get(rhs);
    }

    @NotNull
    @Override
    public var $has(@NotNull var key) {
        return DollarStatic.$(getValue().$has(key));
    }

    @NotNull
    @Override
    public var $insert(@NotNull var value, int position) {
        return getValue().$insert(value, position);
    }

    @NotNull
    @Override
    public var $isEmpty() {
        return DollarStatic.$(getValue().$isEmpty());
    }

    @Override
    public var $max(boolean parallel) {
        return getValue().$max(parallel);
    }

    @Override
    public var $min(boolean parallel) {
        return getValue().$min(parallel);
    }

    @NotNull
    @Override
    public var $prepend(@NotNull var value) {
        return getValue().$prepend(value);
    }

    @Override
    public var $product(boolean parallel) {
        return getValue().$product(parallel);
    }

    @NotNull
    @Override
    public var $remove(@NotNull var value) {
        return getValue().$remove(value);
    }

    @NotNull
    @Override
    public var $removeByKey(@NotNull String key) {
        return tracer.trace(this, getValue().$removeByKey(key), StateTracer.Operations.REMOVE_BY_KEY, key);
    }

    @Override
    public var $reverse(boolean parallel) {
        return getValue().$reverse(parallel);
    }

    @NotNull
    @Override
    public var $set(@NotNull String key, Object value) {
        return tracer.trace(null, getValue().$set(key, value), StateTracer.Operations.SET, key, value);
    }

    @NotNull
    @Override
    public var $set(@NotNull var key, Object value) {
        return tracer.trace(this, getValue().$set(key, value), StateTracer.Operations.SET, key, value);
    }

    @NotNull
    @Override
    public var $size() {
        return DollarStatic.$(getValue().$size());
    }

    @Override
    public var $sort(boolean parallel) {
        return getValue().$sort(parallel);
    }

    @Override
    public var $sum(boolean parallel) {
        return getValue().$sum(parallel);
    }

    @Override
    public var $unique(boolean parallel) {
        return getValue().$unique(parallel);
    }

    @NotNull
    @Override
    public var remove(@NotNull Object value) {
        return tracer.trace(this, getValue().remove(value), StateTracer.Operations.REMOVE_BY_VALUE, value);
    }

    @NotNull
    @Override
    public int size() {
        return getValue().size();
    }

    @NotNull
    @Override
    public var $as(@NotNull Type type) {
        return getValue().$as(type);
    }

    @NotNull
    @Override
    public ImmutableList<var> toVarList() {
        return getValue().toVarList();
    }

    @NotNull
    @Override
    public Type $type() {
        return getValue().$type();
    }

    @Override
    public boolean collection() {
        return getValue().collection();
    }

    @Override
    public boolean dynamic() {
        return getValue().dynamic();
    }

    @NotNull
    @Override
    public ImmutableMap<var, var> toVarMap() {
        return getValue().toVarMap();
    }

    @NotNull
    @Override
    public String toYaml() {
        return getValue().toYaml();
    }

    @Override
    public boolean is(@NotNull Type... types) {
        return getValue().is(types);
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

    @Override
    public boolean number() {
        return getValue().number();
    }

    @Override
    public boolean decimal() {
        return getValue().decimal();
    }

    @Override
    public boolean integer() {
        return getValue().integer();
    }

    @Override
    public boolean pair() {
        return getValue().pair();
    }

    @Override
    public boolean singleValue() {
        return getValue().singleValue();
    }

    @Override
    public boolean string() {
        return getValue().string();
    }

    @Override
    public ImmutableList<String> toStrings() {
        return getValue().toStrings();
    }

    @NotNull
    @Override
    public ImmutableList<?> toList() {
        return getValue().toList();
    }

    @NotNull
    @Override
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return getValue().toJavaMap();
    }

    @NotNull
    @Override
    public InputStream toStream() {
        return getValue().toStream();
    }

    @Override
    public boolean uri() {
        return getValue().uri();
    }

    @Override
    public boolean queue() {
        return getValue().queue();
    }

    @NotNull
    @Override
    public var $choose(@NotNull var map) {
        return tracer.trace(this, getValue().$choose(map), StateTracer.Operations.CHOOSE);
    }

    @NotNull
    @Override
    public var $each(@NotNull Pipeable pipe) {
        return getValue().$each(pipe);
    }

    @NotNull
    @Override
    public var $copy() {
        return getValue().$copy();
    }

    @NotNull
    @Override
    public var $copy(@NotNull ImmutableList<Throwable> errors) {
        return getValue().$copy();
    }

    @NotNull
    @Override
    public var $fix(boolean parallel) {
        return getValue().$fix(parallel);
    }

    @NotNull
    @Override
    public var $fix(int depth, boolean parallel) {
        return getValue().$fix(depth, parallel);
    }

    @NotNull
    @Override
    public var $fixDeep(boolean parallel) {
        return getValue().$fixDeep(parallel);
    }

    @NotNull
    @Override
    public TypePrediction predictType() {
        return getValue().predictType();
    }

    @NotNull
    @Override
    public var $unwrap() {
        return getValue().$unwrap();
    }

    @NotNull
    @Override
    public var $constrain(@NotNull var constraint, @NotNull String source) {
        return getValue().$constrain(constraint, source);
    }

    @NotNull
    @Override
    public String constraintLabel() {
        return getValue().constraintLabel();
    }

    @NotNull
    @Override
    public var $create() {
        return getValue().$create();
    }

    @NotNull
    @Override
    public var $destroy() {
        return getValue().$destroy();
    }

    @NotNull
    @Override
    public var $pause() {
        return getValue().$pause();
    }

    @Override
    public void $signal(@NotNull Signal signal) {
        getValue().$signal(signal);
    }

    @NotNull
    @Override
    public var $start() {
        return getValue().$start();
    }

    @NotNull
    @Override
    public var $state() {
        return getValue().$state();
    }

    @NotNull
    @Override
    public var $stop() {
        return getValue().$stop();
    }

    @NotNull
    @Override
    public var $unpause() {
        return getValue().$unpause();
    }

    @NotNull
    @Override
    public StateMachine<ResourceState, Signal> getStateMachine() {
        return getValue().getStateMachine();
    }

    @NotNull
    @Override
    public var $default(@NotNull var v) {
        return getValue().$default(v);
    }

    @NotNull
    @Override
    public var $listen(@NotNull Pipeable pipe) {
        return getValue().$listen(pipe);
    }

    @NotNull
    @Override
    public var $mimeType() {
        return getValue().$mimeType();
    }

    @NotNull
    @Override
    public var $notify() {
        return getValue().$notify();
    }

    @NotNull
    @Override
    public Stream<var> $stream(boolean parallel) {
        return getValue().$stream(false);
    }

    @NotNull
    @Override
    public var err() {
        return getValue().err();

    }

    @NotNull
    @Override
    public var out() {
        return getValue().out();
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

    @Override
    public int compareTo(@NotNull var o) {
        return getValue().compareTo(o.$unwrap());
    }

    @NotNull
    @Override
    public var debug(@NotNull Object message) {
        return getValue().debug(message);
    }

    @NotNull
    @Override
    public var debug() {
        return getValue().debug();
    }

    @NotNull
    @Override
    public var debugf(@NotNull String message, Object... values) {
        return getValue().debugf(message, values);
    }

    @NotNull
    @Override
    public var error(@NotNull Throwable exception) {
        return getValue().error(exception);
    }

    @NotNull
    @Override
    public var error(@NotNull Object message) {
        return getValue().error(message);
    }

    @NotNull
    @Override
    public var error() {
        return getValue().error();
    }

    @NotNull
    @Override
    public var errorf(@NotNull String message, Object... values) {
        return getValue().errorf(message, values);
    }

    @NotNull
    @Override
    public var info(@NotNull Object message) {
        return getValue().info(message);
    }

    @NotNull
    @Override
    public var info() {
        return getValue().info();
    }

    @NotNull
    @Override
    public var infof(@NotNull String message, Object... values) {
        return getValue().infof(message, values);
    }

    @Nullable
    var getValue() {
        if (value == null) {
            throw new IllegalStateException("Value has become null!!");
        }
        return value;
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return getValue().equals(obj);
    }

    @NotNull
    @Override
    public String toString() {
        return getValue().toString();
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
    public boolean neitherTrueNorFalse() {
        return getValue().neitherTrueNorFalse();
    }

    @Override
    public boolean truthy() {
        return getValue().truthy();
    }

    @NotNull
    @Override
    public Object meta(@NotNull String key) {
        return getValue().meta(key);
    }

    @Override
    public void meta(@NotNull String key, @NotNull Object value) {
        getValue().meta(key, value);

    }

    @Override
    public void metaAttribute(@NotNull String key, @NotNull String value) {
        getValue().metaAttribute(key, value);
    }

    @NotNull
    @Override
    public String metaAttribute(@NotNull String key) {
        return getValue().metaAttribute(key);
    }


}
