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

package me.neilellis.dollar;

import com.google.common.collect.ImmutableList;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarFuture implements var {

    private Future<var> value = new CompletableFuture<>();

    public DollarFuture(Future<var> value) {
        this.value = value;
    }

    @NotNull
    @Override
    public var $(@NotNull String age, long l) {
        return getValue().$(age, l);
    }

    @NotNull @Override public var $(@NotNull String key, double value) {
        return getValue().$(key, value);
    }

    @Override
    public String S() {
        return getValue().S();
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
        return getValue().$error(errorMessage);
    }

    @NotNull
    @Override
    public var $error(@NotNull Throwable error) {
        return getValue().$error(error);
    }

    @NotNull
    @Override
    public var $error() {
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

    @NotNull @Override public var $invalid(@NotNull String errorMessage) {
        return getValue().$invalid(errorMessage);
    }

    @NotNull
    @Override
    public var $error(@NotNull String errorMessage, @NotNull ErrorType type) {
        return getValue().$error(errorMessage, type);
    }

    @NotNull
    @Override
    public ImmutableList<Throwable> errors() {
        return getValue().errors();
    }

    @NotNull
    @Override
    public ImmutableList<var> toList() {
        return getValue().toList();
    }

    @NotNull
    @Override
    public var $load(@NotNull String location) {
        return getValue().$load(location);
    }

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {
        return getValue().$map();
    }

    @Override public boolean $match(@NotNull String key, @Nullable String value) {
        return getValue().$match(key, value);
    }

    @NotNull
    @Override
    public String S(@NotNull String key) {
        return getValue().S(key);
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

    @NotNull
    @Override
    public var $pipe(@NotNull String label, @NotNull String js) {
        return getValue().$pipe(label, js);
    }

    @NotNull
    @Override
    public var $pipe(@NotNull String label, @NotNull Pipeable pipe) {
        return getValue().$pipe(label, pipe);
    }

    @NotNull
    @Override
    public var $pipe(@NotNull String js) {
        return $pipe("anon", js);
    }

    @NotNull
    @Override
    public var $pipe(@NotNull Class<? extends Script> clazz) {
        return getValue().$pipe(clazz);
    }

    @NotNull
    @Override
    public var $pop(@NotNull String location, int timeoutInMillis) {
        return getValue().$pop(location, timeoutInMillis);
    }

    @Override
    public var $post(String url) {
        return getValue().$post(url);
    }

    @NotNull
    @Override
    public var $pub(@NotNull String... locations) {
        return getValue().$pub(locations);
    }

    @NotNull
    @Override
    public var $push(@NotNull String location) {
        return getValue().$push(location);
    }

    @NotNull
    @Override
    public var $rm(@NotNull String value) {
        return getValue().$rm(value);
    }

    @NotNull
    @Override
    public var $save(@NotNull String location) {
        return getValue().$save(location);
    }

    @NotNull
    @Override
    public var $save(@NotNull String location, int expiryInMilliseconds) {
        return getValue().$save(location, expiryInMilliseconds);
    }

    @NotNull
    @Override
    public var $set(@NotNull String key, Object value) {
        return getValue().$set(key, value);
    }

    @NotNull
    @Override
    public var $(@NotNull String key, Object value) {
        return getValue().$(key, value);
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
        return getValue();
    }

    @Override
    public var clearErrors() {
        return getValue().clearErrors();
    }

    @NotNull
    @Override
    public var copy(@NotNull ImmutableList<Throwable> errors) {
        return getValue().copy(errors);
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
    public var eval(String label, DollarEval eval) {
        return getValue().eval(label, eval);
    }

    @Override
    public var eval(DollarEval lambda) {
        return eval("anon", lambda);
    }

    @Override
    public var eval(Class clazz) {
        return getValue().eval(clazz);
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
    public ImmutableList<String> strings() {
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

    var getValue() {
        try {
            var value = this.value.get();
            if (value == null) {
                return DollarStatic.handleError(new NullPointerException(), value);
            }
            return value;
        } catch (InterruptedException ie) {
            return DollarStatic.handleInterrupt(ie);
        } catch (ExecutionException e) {
            return DollarStatic.handleError(e.getCause(), this);
        } catch (Exception e) {
            return DollarStatic.handleError(e, this);
        }
    }

    public void setValue(var newValue) {
        if (value instanceof CompletableFuture) {
            ((CompletableFuture) value).complete(newValue);
        }
    }

    public Future<var> future() {
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
        return getValue().remove(value);
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
