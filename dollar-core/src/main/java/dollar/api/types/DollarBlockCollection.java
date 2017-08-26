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
import dollar.api.Pipeable;
import dollar.api.Signal;
import dollar.api.Type;
import dollar.api.TypePrediction;
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
import dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;


//Created by BlockOperator directly now
public class DollarBlockCollection implements var {
    @NotNull
    private final List<var> value;


    public DollarBlockCollection(@NotNull List<var> value) {
        this.value = value;
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public var $(@NotNull String key, @Nullable Object value) {
        return getValue().$(key, value);
    }

    @NotNull
    var getValue() {
        for (int i = 0; i < (value.size() - 1); i++) {
            value.get(i).$fixDeep(false);
        }
        return value.get(value.size() - 1);
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public String $S() {return getValue().$S();}

    @NotNull
    @Override
    public String toHumanString() {return getValue().toHumanString();}

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(ReturnVarOnlyGuard.class)
    @Guarded(NotNullParametersGuard
                     .class)
    public var $default(@NotNull var v) {return getValue().$default(v);}

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public var $mimeType() {return getValue().$mimeType();}

    @Override
    @NotNull
    public Stream<var> $stream(boolean parallel) {return getValue().$stream(parallel);}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public var err() {return getValue().err();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public JsonArray jsonArray() {return getValue().jsonArray();}

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public var out() {return getValue().out();}

    @NotNull
    @Override
    public String toDollarScript() {
        StringBuilder builder = new StringBuilder("{");
        for (var v : toVarList()) {
            builder.append(v.toDollarScript()).append("\n");
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    @Nullable
    public <R> R toJavaObject() {return getValue().toJavaObject();}


    @NotNull
    @Override
    public ImmutableJsonObject toJsonObject() {return getValue().toJsonObject();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public var $abs() {return getValue().$abs();}

    @Override
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @NotNull
    public var $dec() {return getValue().$dec();}

    @Override
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public var $minus(
                             @NotNull var rhs) {
        return getValue().$minus(rhs);
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public var $plus(@NotNull var rhs) {
        return getValue().$plus(rhs);
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public var $negate() {return getValue().$negate();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public var $divide(
                              @NotNull var rhs) {
        return getValue().$divide(rhs);
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public var $inc() {return getValue().$inc();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public var $modulus(
                               @NotNull var rhs) {
        return getValue().$modulus(rhs);
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public var $multiply(
                                @NotNull var v) {
        return getValue().$multiply(v);
    }

    @Override
    public int sign() {
        return getValue().sign();
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public Double toDouble() {return getValue().toDouble();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public Integer toInteger() {return getValue().toInteger();}

    @Override
    @Guarded(NotNullGuard.class)
    @NotNull
    public Long toLong() {return getValue().toLong();}

    @Override
    @Guarded(NotNullGuard.class)
    @NotNull
    public Number toNumber() {return getValue().toNumber();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public var $all() {return getValue().$all();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public var $dispatch(
                                @NotNull var lhs) {
        return getValue().$dispatch(lhs);
    }

    @NotNull
    @Override
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public var $write(@NotNull var value,
                      boolean blocking,
                      boolean mutating) {
        return getValue().$write(value, blocking, mutating);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public var $drain() {return getValue().$drain();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public var $give(
                            @NotNull var lhs) {
        return getValue().$give(lhs);
    }

    @NotNull
    @Override
    public var $listen(@NotNull Pipeable pipeable) {return getValue().$listen(pipeable);}

    @NotNull
    @Override
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public var $notify() {return getValue().$notify();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public var $peek() {return getValue().$peek();}

    @NotNull
    @Override
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public var $read(boolean blocking,
                     boolean mutating) {
        return getValue().$read(blocking, mutating);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public var $poll() {return getValue().$poll();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public var $pop() {return getValue().$pop();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public var $publish(
                               @NotNull var lhs) {
        return getValue().$publish(lhs);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public var $push(
                            @NotNull var lhs) {
        return getValue().$push(lhs);
    }

    @NotNull
    @Override
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public var $read() {return getValue().$read();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public var $subscribe(
                                 @NotNull Pipeable subscription) {
        return getValue().$subscribe(subscription);
    }

    @NotNull
    @Override
    public var $listen(@NotNull Pipeable pipeable, @NotNull String id) {return getValue().$listen(pipeable, id);}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public var $subscribe(
                                 @NotNull Pipeable subscription, @NotNull String key) {
        return getValue().$subscribe(subscription, key);
    }

    @NotNull
    @Override
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public var $write(
                             @NotNull var value) {
        return getValue().$write(value);
    }

    @NotNull
    @Override
    public var $as(@NotNull Type type) {return getValue().$as(type);}

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public var $split() {return getValue().$split();}

    @Override
    @Guarded(NotNullCollectionGuard.class)
    @Guarded(AllVarCollectionGuard.class)
    @NotNull
    public ImmutableList<var> toVarList() {
        return getValue().toVarList();
    }

    @NotNull
    @Override
    public Type $type() {
        return getValue().$type();
    }

    @NotNull
    @Override
    public String constraintLabel() {
        return getValue().constraintLabel();
    }

    @Override
    public boolean collection() {return true;}

    @Override
    public boolean dynamic() {return getValue().dynamic();}

    @NotNull
    @Guarded(NotNullGuard.class)
    public var getPairKey() {
        return getValue().$pairKey();
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(AllVarMapGuard.class)
    public ImmutableMap<var, var> toVarMap() {
        return getValue().toVarMap();
    }

    @NotNull
    @Override
    public String toYaml() {
        return getValue().toYaml();
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    public var getPairValue() {
        return getValue().$pairValue();
    }

    @Override
    @Guarded(NotNullGuard.class)
    public boolean is(@NotNull Type... types) {return getValue().is(types);}

    @Override
    public boolean isVoid() {return getValue().isVoid();}

    @Override
    public boolean list() {return getValue().list();}

    @Override
    public boolean map() {return getValue().map();}

    @Override
    public boolean number() {return getValue().number();}

    @Override
    public boolean decimal() {return getValue().decimal();}

    @Override
    public boolean integer() {return getValue().integer();}

    @Override
    public boolean pair() {return getValue().pair();}

    @Override
    public boolean singleValue() {return getValue().singleValue();}

    @Override
    public boolean string() {return getValue().string();}

    @Override
    @Nullable
    public ImmutableList<String> toStrings() {return getValue().toStrings();}

    @NotNull
    @Override
    public ImmutableList<Object> toList() {
        return ImmutableList.of(getValue().toJavaObject());
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return getValue().toJavaMap();
    }

    @Override
    @Guarded(NotNullGuard.class)
    @NotNull
    public InputStream toStream() {return getValue().toStream();}

    @Override
    public boolean uri() {return getValue().uri();}

    @Override
    public boolean queue() {
        return getValue().queue();
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public var $choose(@NotNull var map) {
        return getValue().$choose(map);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public var $each(
                            @NotNull Pipeable pipe) {
        return getValue().$each(pipe);
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public var $create() {return getValue().$create();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public var $destroy() {return getValue().$destroy();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public var $pause() {return getValue().$pause();}

    @Override
    @Guarded(NotNullGuard.class)
    public void $signal(@NotNull Signal signal) {getValue().$signal(signal);}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public var $start() {return getValue().$start();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public var $state() {return getValue().$state();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public var $stop() {return getValue().$stop();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public var $unpause() {return getValue().$unpause();}

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public StateMachine<ResourceState, Signal> getStateMachine() {return getValue().getStateMachine();}

    @Override
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public var $error(
                             @NotNull String errorMessage) {
        return getValue().$error(errorMessage);
    }

    @Override
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public var $error(
                             @NotNull Throwable error) {
        return getValue().$error(error);
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public var $error() {return getValue().$error();}

    @Override
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public var $errors() {return getValue().$errors();}

    @Override
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public var $fail(
                            @NotNull Consumer<ImmutableList<Throwable>> handler) {
        return getValue().$fail(handler);
    }

    @Override
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public var $invalid(
                               @NotNull String errorMessage) {
        return getValue().$invalid(errorMessage);
    }

    @Override
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public var $error(
                             @NotNull String errorMessage, @NotNull ErrorType type) {
        return getValue().$error(errorMessage, type);
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public var clearErrors() {return getValue().clearErrors();}

    @Override
    @NotNull
    @Guarded(NotNullCollectionGuard.class)
    public List<String> errorTexts() {return getValue().errorTexts();}

    @Override
    @NotNull
    @Guarded(NotNullCollectionGuard.class)
    public ImmutableList<Throwable> errors() {return getValue().errors();}

    @Override
    public boolean hasErrors() {return getValue().hasErrors();}



    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public var $get(
                           @NotNull var rhs) {
        return getValue().$get(rhs);
    }

    @NotNull
    @Override
    public var $append(@NotNull var value) {
        return getValue().$append(value);
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public var $contains(
                                @NotNull var value) {
        return getValue().$contains(value);
    }

    @Override
    @Guarded(ChainGuard.class)
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    public var $containsValue(
                                     @NotNull var value) {
        return getValue().$containsValue(value);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public var $containsKey(@NotNull var value) {
        return getValue().$containsKey(value);
    }

    @Override
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    public var $has(@NotNull var key) {
        return getValue().$has(key);
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public var $isEmpty() {return getValue().$isEmpty();}

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public var $size() {return getValue().$size();}

    @NotNull
    @Override
    public var $prepend(@NotNull var value) {
        return getValue().$prepend(value);
    }

    @NotNull
    @Override
    public var $insert(@NotNull var value, int position) {
        return getValue().$insert(value, position);
    }

    @Override
    @NotNull
    public var $removeByKey(@NotNull String key) {return getValue().$removeByKey(key);}

    @Override
    @NotNull
    public var $set(@NotNull String key, @Nullable Object value) {
        return getValue().$set(key, value);
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public var $set(@NotNull var key, @Nullable Object value) {
        return getValue().$set(key, value);
    }

    @NotNull
    @Override
    public var $remove(@NotNull var value) {
        return getValue().$remove(value);
    }

    @NotNull
    @Override
    public int size() {
        return getValue().size();
    }

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public var $copy() {return getValue().$copy();}

    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    @Guarded(NotNullCollectionGuard.class)
    @NotNull
    public var $copy(@NotNull ImmutableList<Throwable> errors) {return getValue().$copy(errors);}

    @Override
    @NotNull
    @Guarded(ChainGuard.class)
    public var $fix(boolean parallel) {return $fix(1, parallel);}

    @NotNull
    @Override
    public var $fix(int depth, boolean parallel) {
        if (depth <= 1) {
            return this;
        } else {
            return getValue().$fix(depth - 1, parallel);
        }
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public var $fixDeep(boolean parallel) {
        return $fix(Integer.MAX_VALUE, parallel);
    }

    @NotNull
    @Override
    public TypePrediction predictType() {
        return getValue().predictType();
    }


    @Override
    @Guarded(ChainGuard.class)
    @NotNull
    public var $unwrap() {return getValue().$unwrap();}

    @Override
    public int compareTo(@NotNull var o) {return getValue().compareTo(o);}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    public var debug(
                            @NotNull Object message) {
        return getValue().debug(message);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public var debug() {return getValue().debug();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    public var debugf(@NotNull String message,
                      Object... values) {
        return getValue().debugf(message, values);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    public var error(
                            @NotNull Throwable exception) {
        return getValue().error(exception);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    public var error(
                            @NotNull Object message) {
        return getValue().error(message);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public var error() {return getValue().error();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    public var errorf(@NotNull String message,
                      Object... values) {
        return getValue().errorf(message, values);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    public var info(
                           @NotNull Object message) {
        return getValue().info(message);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public var info() {return getValue().info();}

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    public var infof(@NotNull String message,
                     Object... values) {
        return getValue().infof(message, values);
    }

    @NotNull
    @Override
    public String metaAttribute(@NotNull String key) {return getValue().metaAttribute(key);}

    @Override
    public void metaAttribute(@NotNull String key, @NotNull String value) {getValue().metaAttribute(key, value);}

    @Override
    public boolean isBoolean() {return getValue().isBoolean();}

    @Override
    public boolean isFalse() {return getValue().isFalse();}

    @Override
    public boolean isTrue() {return getValue().isTrue();}

    @Override
    public boolean neitherTrueNorFalse() {return getValue().neitherTrueNorFalse();}

    @Override
    public boolean truthy() {return getValue().truthy();}

    @NotNull
    @Guarded(NotNullGuard.class)
    @Override
    public String toString() {return getValue().toString();}

    @NotNull
    @Override
    public var $constrain(@NotNull var constraint, @NotNull String source) {
        return getValue().$constrain(constraint, source);
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

}
