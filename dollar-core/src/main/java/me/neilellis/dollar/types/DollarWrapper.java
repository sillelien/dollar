/*
 * Copyright (c) 2014 Neil Ellis
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

package me.neilellis.dollar.types;

import com.google.common.collect.ImmutableList;
import me.neilellis.dollar.*;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.json.JsonObject;
import me.neilellis.dollar.monitor.DollarMonitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * The DollarWrapper class exists to provide basic AOP style constructs without having actual AOP magic.
 *
 * Currently the DollarWrapper class provides monitoring and state tracing functions.
 *
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarWrapper implements var {

    private DollarMonitor monitor;
    private StateTracer tracer;
    private ErrorLogger errorLogger;
    private var value;

    DollarWrapper(var value, DollarMonitor monitor, StateTracer tracer, ErrorLogger errorLogger) {
//        tracer.trace(DollarNull.INSTANCE,value, StateTracer.Operations.CREATE);
        this.value = value;
        this.monitor = monitor;
        this.tracer = tracer;
        this.errorLogger = errorLogger;
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
    public var $plus(Object value) {
        return getValue().$plus(value);
    }

    @NotNull
    @Override
    public Stream<var> $children() {
        return getValue().$children();
    }

    @NotNull
    @Override
    public var $default(Object o) {
        return getValue().$default(o);
    }

    @NotNull
    @Override
    public Stream $children(@NotNull String key) {
        return getValue().$children(key);
    }

    @Override
    public var $choose(var map) {
        return tracer.trace(this, getValue().$choose(map), StateTracer.Operations.CHOOSE);
    }

    @Override
    public var $each(Pipeable pipe) {
        return getValue().$each(pipe);
    }

    @NotNull
    @Override
    public var $copy() {
        return getValue().$copy();
    }

    @NotNull
    @Override
    public var $has(@NotNull String key) {
        return DollarStatic.$(getValue().$has(key));
    }

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {
        return getValue().$map();
    }

    @NotNull
    @Override
    public var $match(@NotNull String key, String value) {
        return DollarStatic.$(getValue().$match(key, value));
    }

    @NotNull
    @Override
    public String S(@NotNull String key) {
        return getValue().S(key);
    }

    @NotNull
    @Override
    public var $mimeType() {
        return DollarStatic.$(getValue().$mimeType());
    }

    @NotNull
    @Override
    public var $post(String url) {
        return tracer.trace(this,
                monitor.run("$post",
                        "dollar.post",
                        "Posting to " + url,
                        () -> getValue().$post(url)),
                StateTracer.Operations.EVAL,
                url);
    }

    @NotNull
    @Override
    public var $rm(@NotNull String key) {
        return tracer.trace(this, getValue().$rm(key), StateTracer.Operations.REMOVE_BY_KEY, key);
    }

    @NotNull
    @Override
    public var $minus(@NotNull Object value) {
        return getValue().$minus(value);
    }

    @NotNull
    @Override
    public var $set(@NotNull String key, Object value) {
        return tracer.trace(this, getValue().$set(key, value), StateTracer.Operations.SET, key, value);
    }

    @NotNull
    @Override
    public var $(@NotNull var key, Object value) {
        return tracer.trace(this, getValue().$(key, value), StateTracer.Operations.SET, key, value);
    }

    @NotNull
    @Override
    public Stream<var> $stream() {
        return getValue().$stream();
    }

    @NotNull
    @Override
    public var $void(@NotNull Callable<var> handler) {
        return getValue().$void(handler);
    }

    @NotNull
    @Override
    public var $get(@NotNull Object key) {
        return getValue().$get(key);
    }

    @NotNull
    @Override
    public var $get(@NotNull String key) {
        return getValue().$get(key);
    }


    @Nullable
    @Override
    public <R> R $() {
        return getValue().$();
    }

    @NotNull
    @Override
    public Stream<String> keyStream() {
        return getValue().keyStream();
    }

    @NotNull
    @Override
    public Stream<Map.Entry<String, var>> kvStream() {
        return getValue().kvStream();
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
    public var $(@NotNull Pipeable lambda) {
        return getValue().$(lambda);
    }

    @NotNull
    @Override
    public var $(@NotNull Number n) {
        return getValue().$(n);
    }

    @NotNull
    @Override
    public var $(@NotNull var rhs) {
        return getValue().$(rhs);
    }

    var getValue() {
        if (value == null) {
            throw new IllegalStateException("Value has become null!!");
        }
        return value;
    }

    @NotNull
    @Override
    public var $dec(@NotNull var key, @NotNull var amount) {
        return tracer.trace(this, getValue().$dec(key, amount), StateTracer.Operations.DEC, key, amount);
    }

    @NotNull
    @Override
    public var $dec(@NotNull var amount) {
        return tracer.trace(this, getValue().$dec(amount), StateTracer.Operations.DEC, amount);
    }

    @NotNull
    @Override
    public var $inc(@NotNull var key, @NotNull var amount) {
        return tracer.trace(this, getValue().$inc(key, amount), StateTracer.Operations.INC, key, amount);
    }

    @NotNull
    @Override
    public var $inc(@NotNull var amount) {
        return tracer.trace(this, getValue().$inc(amount), StateTracer.Operations.INC, amount);
    }

    @NotNull
    @Override
    public var $negate() {
        return getValue().$negate();
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return getValue().$multiply(v);
    }

    @NotNull
    @Override
    public var $divide(@NotNull var v) {
        return getValue().$divide(v);
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var v) {
        return getValue().$modulus(v);
    }

    @NotNull
    @Override
    public var $abs() {
        return getValue().$abs();
    }

    @NotNull
    @Override
    public var $error(@NotNull String errorMessage) {
        errorLogger.log(errorMessage);
        return getValue().$error(errorMessage);
    }

    @NotNull
    @Override
    public var $error(@NotNull Throwable error) {
        errorLogger.log(error);
        return getValue().$error(error);
    }

    @NotNull
    @Override
    public var $error() {
        errorLogger.log();
        return getValue().$error();
    }

    @NotNull
    @Override
    public var $errors() {
        return getValue().$errors();
    }

    @NotNull
    @Override
    public var $fail(@NotNull Consumer<List<Throwable>> handler) {
        return getValue().$fail(handler);
    }

    @NotNull
    @Override
    public var $invalid(@NotNull String errorMessage) {
        errorLogger.log();
        return getValue().$invalid(errorMessage);
    }

    @NotNull
    @Override
    public var $error(@NotNull String errorMessage, @NotNull ErrorType type) {
        errorLogger.log(errorMessage, type);
        return getValue().$error(errorMessage, type);
    }

    @NotNull
    @Override
    public ImmutableList<Throwable> errors() {
        return getValue().errors();
    }

    @NotNull
    @Override
    public var $pipe(@NotNull String label, @NotNull String js) {
        return tracer.trace(this,
                monitor.run("$pipe",
                        "dollar.pipe.js." + sanitize(label),
                        "Evaluating: " + js,
                        () -> getValue().$pipe(label, js)),
                StateTracer.Operations.EVAL,
                label,
                js);
    }

    @Override
    public boolean hasErrors() {
        return getValue().hasErrors();
    }

    @Override
    public var clearErrors() {
        return tracer.trace(this, getValue().clearErrors(), StateTracer.Operations.CLEAR_ERRORS);
    }

    @NotNull
    @Override
    public List<String> errorTexts() {
        return getValue().errorTexts();
    }


    @NotNull
    @Override
    public var $pipe(@NotNull String label, @NotNull Pipeable pipe) {
        return tracer.trace(this,
                monitor.run("$pipe",
                        "dollar.pipe.pipeable." + sanitize(label), "",
                        () -> getValue().$pipe(label, pipe)),
                StateTracer.Operations.EVAL, label,
                pipe.getClass().getName());
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

    @Override
    public var $all() {
        return getValue().$all();
    }

    @Override
    public var $subscribe(Pipeable subscription) {
        return getValue().$subscribe(subscription);
    }


    @NotNull
    @Override
    public var $eval(@NotNull String js) {
        return $pipe("anon", js);
    }

    @NotNull
    @Override
    public var $pipe(@NotNull String classModule) {
        return getValue().$pipe(classModule);
    }

    @NotNull
    @Override
    public var $pipe(@NotNull Class<? extends Pipeable> clazz) {
        return tracer.trace(this,
                monitor.run("pipe",
                        "dollar.run.pipe." + sanitize(clazz),
                        "Piping to " + clazz.getName(),
                        () -> getValue().$pipe(clazz)),
                StateTracer.Operations.PIPE,
                clazz.getName());
    }


    @Override
    public String S() {
        return getValue().S();
    }

    @NotNull
    @Override
    public ImmutableList<var> toList() {
        return getValue().toList();
    }

    @Override
    public boolean isVoid() {
        return getValue().isVoid();
    }

    @Override
    public boolean is(Type... types) {
        return getValue().is(types);
    }

    @Nullable
    @Override
    public Double D() {
        return getValue().D();
    }

    @Override
    public Integer I() {
        return getValue().I();
    }

    @Nullable
    @Override
    public Long L() {
        return getValue().L();
    }

    @Override
    public Integer I(@NotNull String key) {
        return getValue().I(key);
    }

    @Override
    public boolean isDecimal() {
        return getValue().isDecimal();
    }

    @Override
    public boolean isInteger() {
        return getValue().isInteger();
    }

    @Override
    public boolean isList() {
        return getValue().isList();
    }

    @Override
    public boolean isMap() {
        return getValue().isMap();
    }

    @Override
    public boolean isNumber() {
        return getValue().isNumber();
    }

    @Override
    public boolean isSingleValue() {
        return getValue().isSingleValue();
    }

    @Override
    public boolean isString() {
        return getValue().isString();
    }

    @Override
    public boolean isLambda() {
        return getValue().isLambda();
    }

    @Nullable
    @Override
    public JsonObject json(@NotNull String key) {
        return getValue().json(key);
    }

    @Override
    public Number number(@NotNull String key) {
        return getValue().number(key);
    }

    @Nullable
    @Override
    public JSONObject orgjson() {
        return getValue().orgjson();
    }

    @Nullable
    @Override
    public JsonObject json() {
        return getValue().json();
    }

    @Override
    public ImmutableList<String> strings() {
        return getValue().strings();
    }

    @Override
    public Map<String, Object> toMap() {
        return getValue().toMap();
    }

    @NotNull
    @Override
    public Number N() {
        return getValue().N();
    }

    @Override
    public InputStream toStream() {
        return getValue().toStream();
    }

    @Override
    public boolean isUri() {
        return getValue().isUri();
    }

    @Override
    public var $as(Type type) {
        return getValue().$as(type);
    }

    @NotNull
    @Override
    public var _unwrap() {
        return value._unwrap();
    }

    @Override
    public String _src() {
        return getValue()._src();
    }

    @Override
    public void _src(String src) {
        getValue()._src(src);
    }


    @NotNull
    @Override
    public var copy(@NotNull ImmutableList<Throwable> errors) {
        return getValue().$copy();
    }

    @Override
    public var decode() {
        return getValue().decode();
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
    @Override
    public var $size() {
        return DollarStatic.$(getValue().$size());
    }

    @NotNull
    @Override
    public var $isEmpty() {
        return DollarStatic.$(getValue().$isEmpty());
    }

    @NotNull
    @Override
    public boolean containsKey(Object key) {
        return getValue().containsKey(key);
    }

    @NotNull
    @Override
    public var $containsValue(@NotNull Object value) {
        return DollarStatic.$(getValue().$containsValue(value));
    }

    @NotNull
    @Override
    public var remove(Object value) {
        return tracer.trace(this, getValue().remove(value), StateTracer.Operations.REMOVE_BY_VALUE, value);
    }

    @Override
    public var eval(@NotNull String label, DollarEval lambda) {
        return tracer.trace(this,
                monitor.run("$fun",
                        "dollar.pipe.java." + sanitize(label),
                        "Evaluating Lambda",
                        () -> getValue().eval(label, lambda)),
                StateTracer.Operations.EVAL,
                label,
                lambda);
    }

    @Override
    public void clear() {
        getValue().clear();
    }

    @NotNull
    @Override
    public Collection<var> values() {
        return getValue().values();
    }

    @Override
    public var eval(DollarEval lambda) {
        return eval("anon", lambda);
    }

    @NotNull
    @Override
    public Set<Map.Entry<String, var>> entrySet() {
        return getValue().entrySet();
    }


    @Override
    public var eval(@NotNull Class clazz) {
        return monitor.run("save",
                "dollar.run." + clazz.getName(),
                "Running class " + clazz,
                () -> getValue().eval(clazz));
    }



    @NotNull
    private String sanitize(@NotNull Class clazz) {
        return clazz.getName().toLowerCase();
    }

    @Override
    public boolean isBoolean() {
        return getValue().isBoolean();
    }

    @Override
    public boolean isTrue() {
        return getValue().isTrue();
    }

    @Override
    public boolean isTruthy() {
        return getValue().isTruthy();
    }

    @Override
    public boolean isFalse() {
        return getValue().isFalse();
    }

    @Override
    public boolean isNeitherTrueNorFalse() {
        return getValue().isNeitherTrueNorFalse();
    }

    @Override
    public var assertNotVoid(String message) throws AssertionError {
        return getValue().assertNotVoid(message);
    }

    @Override
    public var assertTrue(Function<var, Boolean> assertion, String message) throws AssertionError {
        return getValue().assertTrue(assertion, message);
    }

    @Override
    public var assertFalse(Function<var, Boolean> assertion, String message) throws AssertionError {
        return getValue().assertFalse(assertion, message);
    }

    @Override
    public String $listen(Pipeable pipe) {
        return getValue().$listen(pipe);
    }

    @Override
    public var $notify(var value) {
        return getValue().$notify(value);
    }

    @Override
    public String $listen(Pipeable pipe, String key) {
        return getValue().$listen(pipe, key);
    }

    @Override
    public var $send(var value, boolean blocking, boolean mutating) {
        return getValue().$send(value, blocking, mutating);
    }

    @Override
    public var $send(var value) {
        return getValue().$send(value);
    }

    @Override
    public var $receive() {
        return getValue().$receive();
    }

    @Override
    public var $drain() {
        return getValue().$drain();
    }


    @Override
    public var $poll() {
        return getValue().$poll();
    }

    @Override
    public var $peek() {
        return getValue().$peek();

    }


    @Override
    public var $dispatch(var lhs) {
        return getValue().$dispatch(lhs);

    }

    @Override
    public var $give(var lhs) {
        return getValue().$give(lhs);

    }

    @Override
    public var $push(var lhs) {
        return getValue().$push(lhs);

    }

    @Override
    public var $receive(boolean blocking, boolean mutating) {
        return getValue().$receive(blocking, mutating);
    }

    @Override
    public var $publish(var lhs) {
        return getValue().$publish(lhs);

    }

    @Override
    public void setMetaAttribute(String key, String value) {
        getValue().setMetaAttribute(key, value);
    }

    @Override
    public String getMetaAttribute(String key) {
        return getValue().getMetaAttribute(key);
    }

    @Override
    public int compareTo(var o) {
        return getValue().compareTo(o);
    }

    @Override
    public var debugf(String message, Object... values) {
        return getValue().debugf(message, values);
    }

    @Override
    public var debug(Object message) {
        return getValue().debug(message);
    }

    @Override
    public var debug() {
        return getValue().debug();
    }

    @Override
    public var infof(String message, Object... values) {
        return getValue().infof(message, values);
    }

    @Override
    public var info(Object message) {
        return getValue().info(message);
    }

    @Override
    public var info() {
        return getValue().info();
    }

    @Override
    public var errorf(String message, Object... values) {
        return getValue().errorf(message, values);
    }

    @Override
    public var error(Throwable exception) {
        return getValue().error(exception);
    }

    @Override
    public var error(Object message) {
        return getValue().error(message);
    }

    @Override
    public var error() {
        return getValue().error();
    }
}
