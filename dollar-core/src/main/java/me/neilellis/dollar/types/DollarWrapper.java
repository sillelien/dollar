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
import me.neilellis.dollar.monitor.Monitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.vertx.java.core.json.JsonObject;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
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

    private Monitor monitor;
    private StateTracer tracer;
    private ErrorLogger errorLogger;
    private var value;

    DollarWrapper(var value, Monitor monitor, StateTracer tracer, ErrorLogger errorLogger) {
//        tracer.trace(DollarNull.INSTANCE,value, StateTracer.Operations.CREATE);
        this.value = value;
        this.monitor = monitor;
        this.tracer = tracer;
        this.errorLogger = errorLogger;
    }


    @NotNull
    @Override
    public var $(@NotNull String key, long l) {
        return tracer.trace(this, getValue().$(key, l), StateTracer.Operations.SET, key, l);
    }

    @NotNull @Override public var $(@NotNull String key, double value) {
        return tracer.trace(this, getValue().$(key, value), StateTracer.Operations.SET, key, value);
    }

    @NotNull
    @Override
    public var $append(Object value) {
        return getValue().$append(value);
    }

    @NotNull
    @Override
    public Stream<var> $children() {
        return getValue().$children();
    }

    @NotNull
    @Override
    public Stream $children(@NotNull String key) {
        return getValue().$children(key);
    }

    @NotNull
    @Override
    public var $copy() {
        return getValue().$copy();
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

    @Override
    public boolean $has(@NotNull String key) {
        return getValue().$has(key);
    }

    @NotNull
    @Override
    public var $invalid(@NotNull String errorMessage) {
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
    public List<Throwable> errors() {
        return getValue().errors();
    }

    @NotNull
    @Override
    public var $load(@NotNull String location) {
        return tracer.trace(DollarVoid.INSTANCE,
                            monitor.run("save",
                                        "dollar.persist.temp.load." + sanitize(location),
                                        "Loading value at " + location,
                                        () -> getValue().$load(location)),
                            StateTracer.Operations.LOAD,
                            location);
    }

    @NotNull
    @Override
    public Map<String, var> $map() {
        return getValue().$map();
    }

    @Override
    public boolean $match(@NotNull String key, String value) {
        return getValue().$match(key, value);
    }

    @NotNull
    @Override
    public String string(@NotNull String key) {
        return getValue().string(key);
    }

    @NotNull
    @Override
    public String $mimeType() {
        return getValue().$mimeType();
    }

    @Override
    public void $out() {
        getValue().$out();
    }

    @Override
    public String S() {
        return getValue().S();
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

    @NotNull
    @Override
    public var $pipe(@NotNull String js) {
        return $pipe("anon", js);
    }

    @NotNull
    @Override
    public var $pipe(@NotNull Class<? extends Script> clazz) {
        return tracer.trace(this,
                            monitor.run("pipe",
                                        "dollar.run.pipe." + sanitize(clazz),
                                        "Piping to " + clazz.getName(),
                                        () -> getValue().$pipe(clazz)),
                            StateTracer.Operations.PIPE,
                            clazz.getName());
    }

    @NotNull
    @Override
    public var $pipe(@NotNull Function<var, var> function) {
        return tracer.trace(this,
                            monitor.run("pipe",
                                        "dollar.run.pipe",
                                        "Piping to " + function.getClass().getName(),
                                        () -> getValue().$pipe(function)),
                            StateTracer.Operations.PIPE);
    }

    @NotNull
    @Override
    public var $pop(@NotNull String location, int timeoutInMillis) {
        return tracer.trace(DollarVoid.INSTANCE,
                            monitor.run("pop",
                                        "dollar.persist.temp.pop." + sanitize(location),
                                        "Popping value from " + location,
                                        () -> getValue().$pop(location, timeoutInMillis)),
                            StateTracer.Operations.POP,
                            location);
    }

    @NotNull
    @Override
    public var $pub(@NotNull String... locations) {
        return tracer.trace(this,
                            monitor.run("pub",
                                        "dollar.message.pub." + sanitize(Arrays.toString(locations)),
                                        "Publishing value to " + Arrays.toString(locations),
                                        () -> getValue().$pub(
                                            locations)),
                            StateTracer.Operations.PUBLISH,
                            locations);
    }

    @NotNull
    @Override
    public var $push(@NotNull String location) {
        return tracer.trace(this,
                            monitor.run("push",
                                        "dollar.persist.temp.push." + sanitize(location),
                                        "Pushing value to " + location,
                                        () -> getValue().$push(location)),
                            StateTracer.Operations.PUSH,
                            location);
    }

    @NotNull
    @Override
    public var $rm(@NotNull String key) {
        return tracer.trace(this, getValue().$rm(key), StateTracer.Operations.REMOVE_BY_KEY, key);
    }

    @NotNull
    @Override
    public var $save(@NotNull String location) {
        tracer.trace(this, this, StateTracer.Operations.SAVE);
        return monitor.run("save",
                           "dollar.persist.temp.save." + sanitize(location),
                           "Saving value at " + location,
                           () -> getValue().$save(location));
    }

    @NotNull
    @Override
    public var $save(@NotNull String location, int expiryInMilliseconds) {
        tracer.trace(this, this, StateTracer.Operations.SAVE);
        return monitor.run("save",
                           "dollar.persist.temp.save." + sanitize(location),
                           "Saving value at " + location + " with expiry " + expiryInMilliseconds,
                           () -> getValue().$save(
                               location,
                               expiryInMilliseconds));
    }

    @NotNull
    @Override
    public var $set(@NotNull String key, Object value) {
        return tracer.trace(this, getValue().$set(key, value), StateTracer.Operations.SET, key, value);
    }

    @NotNull
    @Override
    public var $(@NotNull String key, Object value) {
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

    @Override
    public boolean isVoid() {
        return getValue().isVoid();
    }

    @Nullable @Override public Double D() {
        return getValue().D();
    }

    @Override
    public Integer I() {
        return getValue().I();
    }

    @Override
    public Integer I(@NotNull String key) {
        return getValue().I(key);
    }

    @Nullable @Override public Long L() {
        return getValue().L();
    }

    @NotNull
    @Override
    public var _unwrap() {
        return value;
    }

    @Override
    public var clearErrors() {
        return tracer.trace(this, getValue().clearErrors(), StateTracer.Operations.CLEAR_ERRORS);
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
    public void err() {
        getValue().err();
    }

    @NotNull
    @Override
    public List<String> errorTexts() {
        return getValue().errorTexts();
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
    public var eval(DollarEval lambda) {
        return eval("anon", lambda);
    }

    @Override
    public var eval(@NotNull Class clazz) {
        return monitor.run("save",
                           "dollar.run." + clazz.getName(),
                           "Running class " + clazz,
                           () -> getValue().eval(clazz));
    }

    @NotNull
    @Override
    public var get(@NotNull Object key) {
        return getValue().get(key);
    }

    @NotNull
    @Override
    public var $(@NotNull String key) {
        return getValue().$(key);
    }

    @Override
    public boolean hasErrors() {
        return getValue().hasErrors();
    }

    @NotNull
    @Override
    public JsonObject json(@NotNull String key) {
        return getValue().json(key);
    }

    @NotNull
    @Override
    public List<var> list() {
        return getValue().list();
    }

    @NotNull
    @Override
    public <R> R $() {
        return getValue().$();
    }

    @Override
    public Stream<String> keyStream() {
        return getValue().keyStream();
    }

    @Override
    public Stream<Map.Entry<String, var>> kvStream() {
        return getValue().kvStream();
    }

    @Override
    public Number number(@NotNull String key) {
        return getValue().number(key);
    }

    @NotNull
    @Override
    public JSONObject orgjson() {
        return getValue().orgjson();
    }

    @NotNull
    @Override
    public JsonObject json() {
        return getValue().json();
    }

    @Override
    public List<String> strings() {
        return getValue().strings();
    }

    @Override
    public Map<String, Object> toMap() {
        return getValue().toMap();
    }

    @Override
    public <R> R val() {
        return getValue().val();
    }

    @NotNull
    private String sanitize(@NotNull Class<? extends Script> clazz) {
        return clazz.getName().toLowerCase();
    }

    @NotNull
    private static String sanitize(@NotNull String location) {
        return location.replaceAll("[^\\w.]+", "_");

    }

    var getValue() {
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

    @Override
    public int size() {
        return getValue().size();
    }

    @Override
    public boolean isEmpty() {
        return getValue().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return getValue().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return getValue().containsValue(value);
    }

    @Override
    public var put(String key, var value) {
        return getValue().put(key, value);
    }

    @Override
    public var remove(Object value) {
        return tracer.trace(this, getValue().remove(value), StateTracer.Operations.REMOVE_BY_VALUE, value);
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends var> m) {
        getValue().putAll(m);
    }

    @Override
    public void clear() {
        getValue().clear();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return getValue().keySet();
    }

    @NotNull
    @Override
    public Collection<var> values() {
        return getValue().values();
    }

    @NotNull
    @Override
    public Set<Entry<String, var>> entrySet() {
        return getValue().entrySet();
    }

    @Override
    public var getOrDefault(Object key, var defaultValue) {
        return getValue().getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(@NotNull BiConsumer<? super String, ? super var> action) {
        getValue().forEach(action);
    }

    @Override
    public void replaceAll(@NotNull BiFunction<? super String, ? super var, ? extends var> function) {
        getValue().replaceAll(function);
    }

    @Override
    public var putIfAbsent(String key, var value) {
        return getValue().putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return getValue().remove(key, value);
    }

    @Override
    public boolean replace(String key, var oldValue, var newValue) {
        return getValue().replace(key, oldValue, newValue);
    }

    @Override
    public var replace(String key, var value) {
        return getValue().replace(key, value);
    }

    @Override
    public var computeIfAbsent(String key, @NotNull Function<? super String, ? extends var> mappingFunction) {
        return getValue().computeIfAbsent(key, mappingFunction);
    }

    @Override
    public var computeIfPresent(String key,
                                @NotNull BiFunction<? super String, ? super var, ? extends var> remappingFunction) {
        return getValue().computeIfPresent(key, remappingFunction);
    }

    @Override
    public var compute(String key, @NotNull BiFunction<? super String, ? super var, ? extends var> remappingFunction) {
        return getValue().compute(key, remappingFunction);
    }

    @Override
    public var merge(String key, @NotNull var value,
                     @NotNull BiFunction<? super var, ? super var, ? extends var> remappingFunction) {
        return getValue().merge(key, value, remappingFunction);
    }
}
