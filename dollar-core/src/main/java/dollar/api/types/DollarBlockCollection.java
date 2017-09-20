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
import dollar.api.MetaKey;
import dollar.api.Pipeable;
import dollar.api.Signal;
import dollar.api.SubType;
import dollar.api.Type;
import dollar.api.TypePrediction;
import dollar.api.Value;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.List;


//Created by BlockOperator directly now
public class DollarBlockCollection implements Value {
    @NotNull
    private final Value value;


    public DollarBlockCollection(@NotNull List<Value> value) {
        for (int i = 0; i < (value.size() - 1); i++) {
            value.get(i).$fixDeep(false);
        }
        this.value = value.get(value.size() - 1);
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public Value $(@NotNull String key, @Nullable Object value) {
        return getValue().$(key, value);
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public String $S() {return getValue().$S();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public Value $abs() {return getValue().$abs();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public Value $all() {return getValue().$all();}

    @NotNull
    @Override
    public Value $append(@NotNull Value value) {
        return getValue().$append(value);
    }

    @NotNull
    @Override
    public Value $as(@NotNull Type type) {return getValue().$as(type);}

    @NotNull
    @Override
    public Value $avg(boolean parallel) {
        return getValue().$avg(parallel);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public Value $choose(@NotNull Value map) {
        return getValue().$choose(map);
    }

    @Override
    public Value $constrain(@NotNull Value constraint, SubType source) {
        return getValue().$constrain(constraint, source);
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public Value $contains(
                                  @NotNull Value value) {
        return getValue().$contains(value);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public Value $containsKey(@NotNull Value value) {
        return getValue().$containsKey(value);
    }

    @Override
    @Guarded(ChainGuard.class)
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    public Value $containsValue(
                                       @NotNull Value value) {
        return getValue().$containsValue(value);
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public Value $copy() {return getValue().$copy();}

    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    @Guarded(NotNullCollectionGuard.class)
    @NotNull
    public Value $copy(@NotNull ImmutableList<Throwable> errors) {return getValue().$copy(errors);}

    @Override
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @NotNull
    public Value $dec() {return getValue().$dec();}

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(ReturnVarOnlyGuard.class)
    @Guarded(NotNullParametersGuard
                     .class)
    public Value $default(@NotNull Value v) {return getValue().$default(v);}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public Value $dispatch(
                                  @NotNull Value lhs) {
        return getValue().$dispatch(lhs);
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public Value $divide(
                                @NotNull Value rhs) {
        return getValue().$divide(rhs);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public Value $drain() {return getValue().$drain();}

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public Value $each(
                              @NotNull Pipeable pipe) {
        return getValue().$each(pipe);
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public Value $fix(boolean parallel) {return $fix(1, parallel);}

    @NotNull
    @Override
    public Value $fix(int depth, boolean parallel) {
        if (depth <= 1) {
            return this;
        } else {
            return getValue().$fix(depth - 1, parallel);
        }
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public Value $fixDeep(boolean parallel) {
        return $fix(Integer.MAX_VALUE, parallel);
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public Value $get(
                             @NotNull Value rhs) {
        return getValue().$get(rhs);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public Value $give(
                              @NotNull Value lhs) {
        return getValue().$give(lhs);
    }

    @Override
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    public Value $has(@NotNull Value key) {
        return getValue().$has(key);
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public Value $inc() {return getValue().$inc();}

    @NotNull
    @Override
    public Value $insert(@NotNull Value value, int position) {
        return getValue().$insert(value, position);
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public Value $isEmpty() {return getValue().$isEmpty();}

    @NotNull
    @Override
    public Value $listen(@NotNull Pipeable pipeable) {return getValue().$listen(pipeable);}

    @NotNull
    @Override
    public Value $listen(@NotNull Pipeable pipeable, @NotNull String id) {return getValue().$listen(pipeable, id);}

    @NotNull
    @Override
    public Value $max(boolean parallel) {
        return getValue().$max(parallel);
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public Value $mimeType() {return getValue().$mimeType();}

    @NotNull
    @Override
    public Value $min(boolean parallel) {
        return getValue().$min(parallel);
    }

    @Override
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public Value $minus(
                               @NotNull Value rhs) {
        return getValue().$minus(rhs);
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public Value $modulus(
                                 @NotNull Value rhs) {
        return getValue().$modulus(rhs);
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public Value $multiply(
                                  @NotNull Value v) {
        return getValue().$multiply(v);
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public Value $negate() {return getValue().$negate();}

    @Override
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public Value $notify(NotificationType type, Value value) {return getValue().$notify(type, value);}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public Value $peek() {return getValue().$peek();}

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public Value $plus(@NotNull Value rhs) {
        return getValue().$plus(rhs);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public Value $poll() {return getValue().$poll();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public Value $pop() {return getValue().$pop();}

    @NotNull
    @Override
    public Value $prepend(@NotNull Value value) {
        return getValue().$prepend(value);
    }

    @NotNull
    @Override
    public Value $product(boolean parallel) {
        return null;
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public Value $publish(
                                 @NotNull Value lhs) {
        return getValue().$publish(lhs);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public Value $push(
                              @NotNull Value lhs) {
        return getValue().$push(lhs);
    }

    @NotNull
    @Override
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public Value $read(boolean blocking,
                       boolean mutating) {
        return getValue().$read(blocking, mutating);
    }

    @NotNull
    @Override
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public Value $read() {return getValue().$read();}

    @NotNull
    @Override
    public Value $remove(@NotNull Value value) {
        return getValue().$remove(value);
    }

    @Override
    @NotNull
    public Value $removeByKey(@NotNull String key) {return getValue().$removeByKey(key);}

    @NotNull
    @Override
    public Value $reverse(boolean parallel) {
        return getValue().$reverse(parallel);

    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public Value $set(@NotNull Value key, @NotNull Object value) {
        return getValue().$set(key, value);
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public Value $size() {return getValue().$size();}

    @NotNull
    @Override
    public Value $sort(boolean parallel) {
        return getValue().$sort(parallel);
    }

    @Override
    @Guarded(ChainGuard.class)
    public Value $split(boolean parallel) {return getValue().$split(parallel);}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public Value $subscribe(
                                   @NotNull Pipeable subscription) {
        return getValue().$subscribe(subscription);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public Value $subscribe(
                                   @NotNull Pipeable subscription, @NotNull String key) {
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

    @Override
    @Guarded(ChainGuard.class)
    @NotNull
    public Value $unwrap() {return getValue().$unwrap();}

    @NotNull
    @Override
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public Value $write(@NotNull Value value,
                        boolean blocking,
                        boolean mutating) {
        return getValue().$write(value, blocking, mutating);
    }

    @NotNull
    @Override
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public Value $write(
                               @NotNull Value value) {
        return getValue().$write(value);
    }

    @Override
    public boolean collection() {return true;}

    @NotNull
    @Override
    public SubType constraintLabel() {
        return getValue().constraintLabel();
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    public Value debug(
                              @NotNull Object message) {
        return getValue().debug(message);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public Value debug() {return getValue().debug();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    public Value debugf(@NotNull String message,
                        Object... values) {
        return getValue().debugf(message, values);
    }

    @Override
    public boolean decimal() {return getValue().decimal();}

    @Override
    public boolean dynamic() {return getValue().dynamic();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public Value err() {return getValue().err();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    public Value error(
                              @NotNull Throwable exception) {
        return getValue().error(exception);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    public Value error(
                              @NotNull Object message) {
        return getValue().error(message);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public Value error() {return getValue().error();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    public Value errorf(@NotNull String message,
                        Object... values) {
        return getValue().errorf(message, values);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    public Value info(
                             @NotNull Object message) {
        return getValue().info(message);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public Value info() {return getValue().info();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    public Value infof(@NotNull String message,
                       Object... values) {
        return getValue().infof(message, values);
    }

    @Override
    public boolean integer() {return getValue().integer();}

    @Override
    @Guarded(NotNullGuard.class)
    public boolean is(@NotNull Type... types) {return getValue().is(types);}

    @Override
    public boolean isBoolean() {return getValue().isBoolean();}

    @Override
    public boolean isFalse() {return getValue().isFalse();}

    @Override
    public boolean isTrue() {return getValue().isTrue();}

    @Override
    public boolean isVoid() {return getValue().isVoid();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public JsonArray jsonArray() {return getValue().jsonArray();}

    @Override
    public boolean list() {return getValue().list();}

    @Override
    public boolean map() {return getValue().map();}

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
    public void metaAttribute(@NotNull MetaKey key, @NotNull String value) {getValue().metaAttribute(key, value);}

    @NotNull
    @Override
    public String metaAttribute(@NotNull MetaKey key) {return getValue().metaAttribute(key);}

    @Override
    public boolean neitherTrueNorFalse() {return getValue().neitherTrueNorFalse();}

    @Override
    public boolean number() {return getValue().number();}

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public Value out() {return getValue().out();}

    @Override
    public boolean pair() {return getValue().pair();}

    @NotNull
    @Override
    public TypePrediction predictType() {
        return getValue().predictType();
    }

    @Override
    public boolean queue() {
        return getValue().queue();
    }

    @Override
    public int sign() {
        return getValue().sign();
    }

    @Override
    public boolean singleValue() {return getValue().singleValue();}

    @NotNull
    @Override
    public int size() {
        return getValue().size();
    }

    @Override
    public boolean string() {return getValue().string();}

    @NotNull
    @Override
    public String toDollarScript() {
        StringBuilder builder = new StringBuilder("{");
        for (Value v : toVarList()) {
            builder.append(v.toDollarScript()).append("\n");
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public double toDouble() {return getValue().toDouble();}

    @NotNull
    @Override
    public String toHumanString() {return getValue().toHumanString();}

    @Override
    public int toInteger() {return getValue().toInteger();}

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return getValue().toJavaMap();
    }

    @Override
    @Nullable
    public <R> R toJavaObject() {return getValue().toJavaObject();}

    @NotNull
    @Override
    public ImmutableJsonObject toJsonObject() {return getValue().toJsonObject();}

    @NotNull
    @Override
    public ImmutableList<Object> toList() {
        return ImmutableList.of(getValue().toJavaObject());
    }

    @Override
    @Guarded(NotNullGuard.class)
    @NotNull
    public long toLong() {return getValue().toLong();}

    @Override
    @Guarded(NotNullGuard.class)
    @NotNull
    public Number toNumber() {return getValue().toNumber();}

    @Override
    @Guarded(NotNullGuard.class)
    @NotNull
    public InputStream toStream() {return getValue().toStream();}

    @Override
    @Nullable
    public ImmutableList<String> toStrings() {return getValue().toStrings();}

    @Override
    @Guarded(NotNullCollectionGuard.class)
    @Guarded(AllVarCollectionGuard.class)
    @NotNull
    public ImmutableList<Value> toVarList() {
        return getValue().toVarList();
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(AllVarMapGuard.class)
    public ImmutableMap<Value, Value> toVarMap() {
        return getValue().toVarMap();
    }

    @NotNull
    @Override
    public String toYaml() {
        return getValue().toYaml();
    }

    @Override
    public boolean truthy() {return getValue().truthy();}

    @Override
    public boolean uri() {return getValue().uri();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public Value $create() {return getValue().$create();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public Value $destroy() {return getValue().$destroy();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public Value $pause() {return getValue().$pause();}

    @Override
    @Guarded(NotNullGuard.class)
    public void $signal(@NotNull Signal signal) {getValue().$signal(signal);}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public Value $start() {return getValue().$start();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public Value $state() {return getValue().$state();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public Value $stop() {return getValue().$stop();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public Value $unpause() {return getValue().$unpause();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public StateMachine<ResourceState, Signal> getStateMachine() {return getValue().getStateMachine();}

    @Override
    public int compareTo(@NotNull Value o) {return getValue().compareTo(o);}

    @NotNull
    @Guarded(NotNullGuard.class)
    public Value getPairKey() {
        return getValue().$pairKey();
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    public Value getPairValue() {
        return getValue().$pairValue();
    }

    @NotNull
    Value getValue() {
        return value;
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    @Override
    public String toString() {return getValue().toString();}

}
