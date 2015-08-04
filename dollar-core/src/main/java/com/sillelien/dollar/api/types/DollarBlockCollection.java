/*
 * Copyright (c) 2014-2015 Neil Ellis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sillelien.dollar.api.types;

import com.github.oxo42.stateless4j.StateMachine;
import com.sillelien.dollar.api.*;
import com.sillelien.dollar.api.collections.ImmutableList;
import com.sillelien.dollar.api.collections.ImmutableMap;
import com.sillelien.dollar.api.guard.*;
import com.sillelien.dollar.api.json.ImmutableJsonObject;
import com.sillelien.dollar.api.json.JsonArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DollarBlockCollection implements var {
    private final List<var> value;
    private volatile var cachedValue;


    public DollarBlockCollection(List<var> value) {
        this.value = value;
    }

    @Override @NotNull @Guarded(ChainGuard.class) public var $(@NotNull String key, @Nullable Object value) {
        return this.getValue().$(key, value);
    }

    @NotNull var getValue() {
        for (int i = 0; i < value.size() - 1; i++) {
            value.get(i)._fixDeep(false);
        }
        return value.get(value.size() - 1);
    }

    @Override @NotNull @Guarded(NotNullGuard.class) public String $S() {return getValue().$S();}

    @Override public String toHumanString() {return getValue().toHumanString();}

    @Override @NotNull @Guarded(ChainGuard.class) @Guarded(ReturnVarOnlyGuard.class) @Guarded(NotNullParametersGuard
            .class)
    public var $default(var v) {return getValue().$default(v);}

    @Override @NotNull @Guarded(ChainGuard.class) public var $mimeType() {return getValue().$mimeType();}

    @Override @NotNull public Stream<var> $stream(boolean parallel) {return getValue().$stream(parallel);}

    @NotNull @Override @Guarded(ChainGuard.class) public var err() {return getValue().err();}

    @Override @NotNull @Guarded(NotNullGuard.class) public JsonArray jsonArray() {return getValue().jsonArray();}

    @Override @NotNull @Guarded(ChainGuard.class) public var out() {return getValue().out();}

    @NotNull @Override public String toDollarScript() {
        StringBuilder builder = new StringBuilder("{");
        for (var value : $list()) {
            builder.append(value.toDollarScript()).append("\n");
        }
        builder.append("}");
        return builder.toString();
    }

    @Override @Nullable public <R> R toJavaObject() {return getValue().toJavaObject();}

    @Override @Nullable public JSONObject toOrgJson() {return getValue().toOrgJson();}

    @NotNull @Override public ImmutableJsonObject toJsonObject() {return getValue().toJsonObject();}

    @Override @NotNull @Guarded(NotNullGuard.class) @Guarded(ChainGuard.class)
    public var $abs() {return getValue().$abs();}

    @Override @Guarded(NotNullGuard.class) @Guarded(ChainGuard.class) @NotNull
    public var $dec() {return getValue().$dec();}

    @Override @NotNull @Guarded(NotNullParametersGuard.class) @Guarded(ChainGuard.class) public var $minus(
            @NotNull var rhs) {
        return this.getValue().$minus(rhs);
    }

    @Override @NotNull @Guarded(ChainGuard.class) public var $plus(@NotNull var rhs) {
        return this.getValue().$plus(rhs);
    }

    @Override @NotNull @Guarded(NotNullGuard.class) @Guarded(ChainGuard.class)
    public var $negate() {return getValue().$negate();}

    @Override @NotNull @Guarded(NotNullGuard.class) @Guarded(ChainGuard.class) public var $divide(
            @NotNull var rhs) {
        return getValue().$divide(rhs);
    }

    @Override @NotNull @Guarded(NotNullGuard.class) @Guarded(ChainGuard.class)
    public var $inc() {return getValue().$inc();}

    @Override @NotNull @Guarded(NotNullGuard.class) @Guarded(ChainGuard.class) public var $modulus(
            @NotNull var rhs) {
        return getValue().$modulus(rhs);
    }

    @Override @NotNull @Guarded(NotNullGuard.class) @Guarded(ChainGuard.class) public var $multiply(
            @NotNull var v) {
        return getValue().$multiply(v);
    }

    @Override public int sign() {
        return getValue().sign();
    }

    @Override @NotNull @Guarded(NotNullGuard.class) public Double toDouble() {return getValue().toDouble();}

    @Override @NotNull @Guarded(NotNullGuard.class) public Integer toInteger() {return getValue().toInteger();}

    @Override @Guarded(NotNullGuard.class) @NotNull public Long toLong() {return getValue().toLong();}

    @Override @Guarded(NotNullGuard.class) @NotNull public Number toNumber() {return getValue().toNumber();}

    @Override @Guarded(ChainGuard.class) public var $all() {return getValue().$all();}

    @Override @Guarded(ChainGuard.class) @Guarded(NotNullParametersGuard.class) public var $dispatch(
            var lhs) {
        return getValue().$dispatch(lhs);
    }

    @Override @Guarded(NotNullParametersGuard.class) @Guarded(ChainGuard.class) public var $write(var value,
                                                                                                  boolean blocking,
                                                                                                  boolean mutating) {
        return this.getValue().$write(value, blocking, mutating);
    }

    @Override @Guarded(ChainGuard.class) public var $drain() {return getValue().$drain();}

    @Override @Guarded(ChainGuard.class) @Guarded(NotNullParametersGuard.class) public var $give(
            var lhs) {
        return getValue().$give(lhs);
    }

    @Override public var $listen(Pipeable pipeable) {return getValue().$listen(pipeable);}

    @Override @Guarded(NotNullParametersGuard.class) @Guarded(ChainGuard.class)
    public var $notify() {return getValue().$notify();}

    @Override @Guarded(ChainGuard.class) public var $peek() {return getValue().$peek();}

    @Override @Guarded(NotNullParametersGuard.class) @Guarded(ChainGuard.class) public var $read(boolean blocking,
                                                                                                 boolean mutating) {
        return getValue().$read(blocking, mutating);
    }

    @Override @Guarded(ChainGuard.class) public var $poll() {return getValue().$poll();}

    @Override @Guarded(ChainGuard.class) public var $pop() {return getValue().$pop();}

    @Override @Guarded(ChainGuard.class) @Guarded(NotNullParametersGuard.class) public var $publish(
            var lhs) {
        return getValue().$publish(lhs);
    }

    @Override @Guarded(ChainGuard.class) @Guarded(NotNullParametersGuard.class) public var $push(
            var lhs) {
        return getValue().$push(lhs);
    }

    @Override @Guarded(NotNullParametersGuard.class) @Guarded(ChainGuard.class)
    public var $read() {return getValue().$read();}

    @Override @Guarded(ChainGuard.class) @Guarded(NotNullParametersGuard.class) public var $subscribe(
            Pipeable subscription) {
        return getValue().$subscribe(subscription);
    }

    @Override public var $listen(Pipeable pipeable, String id) {return getValue().$listen(pipeable, id);}

    @Override @Guarded(ChainGuard.class) @Guarded(NotNullParametersGuard.class) public var $subscribe(
            Pipeable subscription, String key) {
        return getValue().$subscribe(subscription, key);
    }

    @Override @Guarded(NotNullParametersGuard.class) @Guarded(ChainGuard.class) public var $write(
            var value) {
        return this.getValue().$write(value);
    }

    @Override public var $as(Type type) {return getValue().$as(type);}

    @Override @NotNull @Guarded(ChainGuard.class) public var $split() {return getValue().$split();}

    @Override @Guarded(NotNullCollectionGuard.class) @Guarded(AllVarCollectionGuard.class) @NotNull
    public ImmutableList<var> $list() {return getValue().$list();}

    @Override public Type $type() {
        return getValue().$type();
    }

    @Override public boolean collection() {return true;}

    @Override public boolean dynamic() {return getValue().dynamic();}

    @Override @Guarded(NotNullGuard.class) public var getPairKey() {return getValue().getPairKey();}

    @Override @NotNull @Guarded(NotNullGuard.class) @Guarded(AllVarMapGuard.class)
    public ImmutableMap<var, var> $map() {return getValue().$map();}

    @NotNull @Override @Guarded(NotNullGuard.class) public var getPairValue() {return getValue().getPairValue();}

    @Override @Guarded(NotNullGuard.class) public boolean is(@NotNull Type... types) {return getValue().is(types);}

    @Override public boolean isVoid() {return getValue().isVoid();}

    @Override public boolean list() {return getValue().list();}

    @Override public boolean map() {return getValue().map();}

    @Override public boolean number() {return getValue().number();}

    @Override public boolean decimal() {return getValue().decimal();}

    @Override public boolean integer() {return getValue().integer();}

    @Override public boolean pair() {return getValue().pair();}

    @Override public boolean singleValue() {return getValue().singleValue();}

    @Override public boolean string() {return getValue().string();}

    @Override @Nullable public ImmutableList<String> strings() {return getValue().strings();}

    @NotNull @Override public ImmutableList<Object> toList() {
        return ImmutableList.of(getValue().toJavaObject());
    }

    @NotNull @Override @Guarded(NotNullGuard.class) public Map<?, ?> toMap() {return getValue().toMap();}

    @Override @Guarded(NotNullGuard.class) @NotNull public InputStream toStream() {return getValue().toStream();}

    @Override public boolean uri() {return getValue().uri();}

    @Override @Guarded(NotNullGuard.class) @Guarded(ChainGuard.class) public var $choose(var map) {
        return getValue().$choose(map);
    }

    @Override @Guarded(NotNullGuard.class) @Guarded(ChainGuard.class) public var $each(
            Pipeable pipe) {
        return getValue().$each(pipe);
    }

    @Override @NotNull @Guarded(NotNullGuard.class) public var $create() {return getValue().$create();}

    @Override @NotNull @Guarded(NotNullGuard.class) public var $destroy() {return getValue().$destroy();}

    @Override @NotNull @Guarded(NotNullGuard.class) public var $pause() {return getValue().$pause();}

    @Override @Guarded(NotNullGuard.class) public void $signal(@NotNull Signal signal) {getValue().$signal(signal);}

    @Override @NotNull @Guarded(NotNullGuard.class) public var $start() {return getValue().$start();}

    @Override @NotNull @Guarded(NotNullGuard.class) public var $state() {return getValue().$state();}

    @Override @NotNull @Guarded(NotNullGuard.class) public var $stop() {return getValue().$stop();}

    @Override @NotNull @Guarded(NotNullGuard.class) public var $unpause() {return getValue().$unpause();}

    @Override @NotNull @Guarded(NotNullGuard.class)
    public StateMachine<ResourceState, Signal> getStateMachine() {return getValue().getStateMachine();}

    @Override @NotNull @Guarded(NotNullParametersGuard.class) @Guarded(ChainGuard.class) public var $error(
            @NotNull String errorMessage) {
        return getValue().$error(errorMessage);
    }

    @Override @NotNull @Guarded(NotNullParametersGuard.class) @Guarded(ChainGuard.class) public var $error(
            @NotNull Throwable error) {
        return getValue().$error(error);
    }

    @Override @NotNull @Guarded(ChainGuard.class) public var $error() {return getValue().$error();}

    @Override @NotNull @Guarded(NotNullParametersGuard.class) @Guarded(ChainGuard.class)
    public var $errors() {return getValue().$errors();}

    @Override @NotNull @Guarded(NotNullParametersGuard.class) @Guarded(ChainGuard.class) public var $fail(
            @NotNull Consumer<ImmutableList<Throwable>> handler) {
        return getValue().$fail(handler);
    }

    @Override @NotNull @Guarded(NotNullParametersGuard.class) @Guarded(ChainGuard.class) public var $invalid(
            @NotNull String errorMessage) {
        return getValue().$invalid(errorMessage);
    }

    @Override @NotNull @Guarded(NotNullParametersGuard.class) @Guarded(ChainGuard.class) public var $error(
            @NotNull String errorMessage, @NotNull ErrorType type) {
        return getValue().$error(errorMessage, type);
    }

    @Override @NotNull @Guarded(ChainGuard.class) public var clearErrors() {return getValue().clearErrors();}

    @Override @NotNull @Guarded(NotNullCollectionGuard.class)
    public List<String> errorTexts() {return getValue().errorTexts();}

    @Override @NotNull @Guarded(NotNullCollectionGuard.class)
    public ImmutableList<Throwable> errors() {return getValue().errors();}

    @Override public boolean hasErrors() {return getValue().hasErrors();}

    @Override @NotNull @Guarded(NotNullGuard.class) @Guarded(ChainGuard.class) public var $eval(
            @NotNull String js) {
        return getValue().$eval(js);
    }

    @Override @NotNull @Guarded(NotNullGuard.class) @Guarded(ChainGuard.class) public var $pipe(
            @NotNull Pipeable pipe) {
        return getValue().$pipe(pipe);
    }

    @Override @NotNull @Guarded(NotNullGuard.class) @Guarded(ChainGuard.class) public var $pipe(@NotNull String label,
                                                                                                @NotNull Pipeable
                                                                                                        pipe) {
        return getValue().$pipe(label, pipe);
    }

    @Override @NotNull @Guarded(NotNullGuard.class) @Guarded(ChainGuard.class) public var $pipe(@NotNull String label,
                                                                                                @NotNull String js) {
        return getValue().$pipe(label, js);
    }

    @Override @NotNull @Guarded(NotNullGuard.class) @Guarded(ChainGuard.class) public var $pipe(
            @NotNull Class<? extends Pipeable> clazz) {
        return getValue().$pipe(clazz);
    }

    @Override @NotNull @Guarded(ChainGuard.class) @Guarded(NotNullParametersGuard.class) public var $get(
            @NotNull var rhs) {
        return getValue().$get(rhs);
    }

    @NotNull @Override public var $append(@NotNull var value) {
        return getValue().$append(value);
    }

    @Override @NotNull @Guarded(ChainGuard.class) @Guarded(NotNullParametersGuard.class) public var $contains(
            @NotNull var value) {
        return this.getValue().$contains(value);
    }

    @Override @Guarded(ChainGuard.class) @NotNull @Guarded(NotNullParametersGuard.class) public var $containsValue(
            @NotNull var value) {
        return this.getValue().$containsValue(value);
    }

    @Override @NotNull @Guarded(NotNullParametersGuard.class) public var $has(@NotNull var key) {
        return getValue().$has(key);
    }

    @Override @NotNull @Guarded(ChainGuard.class) public var $isEmpty() {return getValue().$isEmpty();}

    @Override @NotNull @Guarded(ChainGuard.class) public var $size() {return getValue().$size();}

    @NotNull @Override public var $prepend(@NotNull var value) {
        return getValue().$prepend(value);
    }

    @Override @NotNull public var $removeByKey(@NotNull String key) {return getValue().$removeByKey(key);}

    @Override @NotNull public var $set(@NotNull String key, @Nullable Object value) {
        return this.getValue().$set(key, value);
    }

    @Override @NotNull @Guarded(ChainGuard.class) public var $set(@NotNull var key, @Nullable Object value) {
        return this.getValue().$set(key, value);
    }

    @NotNull @Override public var $remove(var value) {
        return getValue().$remove(value);
    }

    @Override @NotNull @Guarded(ChainGuard.class) public var _copy() {return getValue()._copy();}

    @Override @Guarded(ChainGuard.class) @Guarded(NotNullGuard.class) @Guarded(NotNullCollectionGuard.class) @NotNull
    public var _copy(@NotNull ImmutableList<Throwable> errors) {return getValue()._copy(errors);}

    @Override @NotNull @Guarded(ChainGuard.class) public var _fix(boolean parallel) {return _fix(1, parallel);}

    @Override public var _fix(int depth, boolean parallel) {
        if (depth <= 1) {
            return this;
        } else {
            return getValue()._fix(depth - 1, parallel);
        }
    }

    @Override @Guarded(ChainGuard.class) public var _fixDeep(boolean parallel) {
        return _fix(Integer.MAX_VALUE, parallel);
    }

    @Override public TypePrediction _predictType() {
        return getValue()._predictType();
    }


    @Override @Guarded(ChainGuard.class) @NotNull public var _unwrap() {return getValue()._unwrap();}

    @Override public int compareTo(@NotNull var o) {return getValue().compareTo(o);}

    @Override @Guarded(ChainGuard.class) @Guarded(NotNullGuard.class) public var debug(
            Object message) {
        return getValue().debug(message);
    }

    @Override @Guarded(ChainGuard.class) public var debug() {return getValue().debug();}

    @Override @Guarded(ChainGuard.class) @Guarded(NotNullGuard.class) public var debugf(String message,
                                                                                        Object... values) {
        return getValue().debugf(message, values);
    }

    @Override @Guarded(ChainGuard.class) @Guarded(NotNullGuard.class) public var error(
            Throwable exception) {
        return getValue().error(exception);
    }

    @Override @Guarded(ChainGuard.class) @Guarded(NotNullGuard.class) public var error(
            Object message) {
        return getValue().error(message);
    }

    @Override @Guarded(ChainGuard.class) public var error() {return getValue().error();}

    @Override @Guarded(ChainGuard.class) @Guarded(NotNullGuard.class) public var errorf(String message,
                                                                                        Object... values) {
        return getValue().errorf(message, values);
    }

    @Override @Guarded(ChainGuard.class) @Guarded(NotNullGuard.class) public var info(
            Object message) {
        return getValue().info(message);
    }

    @Override @Guarded(ChainGuard.class) public var info() {return getValue().info();}

    @Override @Guarded(ChainGuard.class) @Guarded(NotNullGuard.class) public var infof(String message,
                                                                                       Object... values) {
        return getValue().infof(message, values);
    }

    @Override public String getMetaAttribute(String key) {return getValue().getMetaAttribute(key);}

    @Override public void setMetaAttribute(String key, String value) {this.getValue().setMetaAttribute(key, value);}

    @Override public boolean isBoolean() {return getValue().isBoolean();}

    @Override public boolean isFalse() {return getValue().isFalse();}

    @Override public boolean isTrue() {return getValue().isTrue();}

    @Override public boolean neitherTrueNorFalse() {return getValue().neitherTrueNorFalse();}

    @Override public boolean truthy() {return getValue().truthy();}

    @NotNull @Guarded(NotNullGuard.class) @Override public String toString() {return getValue().toString();}
}
