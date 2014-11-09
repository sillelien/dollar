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
import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.json.JsonObject;
import me.neilellis.dollar.plugin.Plugins;
import me.neilellis.dollar.uri.URIHandler;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarURI extends AbstractDollar {

    private String uri;
    private URIHandler handler;

    public DollarURI(@NotNull List<Throwable> errors, String uri) throws Exception {
        super(errors);
        this.uri = uri.substring(uri.indexOf(":") + 1);
        String scheme = uri.substring(0, uri.indexOf(":"));
        handler = Plugins.resolveURIProvider(scheme).forURI(scheme, this.uri);
    }


    @NotNull
    @Override
    public var $plus(@Nullable Object value) {
        return handler.push(DollarStatic.$(value));
    }

    @NotNull
    @Override
    public Stream<var> $children() {
        return handler.all().toList().stream();
    }

    @NotNull
    @Override
    public Stream<var> $children(@NotNull String key) {
        return handler.get(DollarStatic.$(key)).toList().stream();
    }

    @Override
    public boolean $has(@NotNull String key) {
        return !handler.get(DollarStatic.$(key)).isVoid();
    }

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {
        return ImmutableMap.of();
    }


    @NotNull
    @Override
    public var $rm(@NotNull String key) {
        return handler.remove(DollarStatic.$(key));

    }

    @NotNull
    @Override
    public var $minus(@NotNull Object value) {
        return handler.removeValue(DollarStatic.$(value));

    }

    @NotNull
    @Override
    public var $(@NotNull var key, @Nullable Object value) {
        return handler.set(DollarStatic.$(key), DollarStatic.$(value));

    }

    @NotNull
    @Override
    public var $(@NotNull String key) {
        return handler.get(DollarStatic.$(key));
    }

    @NotNull
    @Override
    public <R> R $() {
        return (R) uri;
    }

    @Override
    public Stream<String> keyStream() {
        return Stream.empty();
    }

    @Override
    public var $(Number n) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean isTruthy() {
        return false;
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isNeitherTrueNorFalse() {
        return true;
    }

    @Override
    public int size() {
        return handler.size();
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public var remove(Object key) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);

    }

    @Override
    public var $dec(var amount) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public var $inc(var amount) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public var $negate() {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public var $multiply(var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public var $divide(var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public var $modulus(var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public var $abs() {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public var decode() {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public String S() {
        return uri;
    }

    @NotNull
    @Override
    public ImmutableList<var> toList() {
        return ImmutableList.copyOf(handler.all().toList());
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @NotNull
    @Override
    public Integer I() {
        return 0;
    }

    @NotNull
    @Override
    public Integer I(@NotNull String key) {
        return 0;
    }

    @Nullable
    @Override
    public JsonObject json(@NotNull String key) {
        return new JsonObject();
    }

    @Nullable
    @Override
    public Number number(@NotNull String key) {
        return 0;
    }

    @Nullable
    @Override
    public JsonObject json() {
        return new JsonObject();
    }

    @Nullable
    @Override
    public ImmutableList<String> strings() {
        return ImmutableList.of();
    }

    @Nullable
    @Override
    public Map<String, Object> toMap() {
        return Collections.emptyMap();
    }

    @NotNull
    @Override
    public Number N() {
        return 0;
    }

    @Override
    public var $subscribe(Pipeable pipe) {
        try {
            handler.subscribe(i -> {
                try {
                    return pipe.pipe(i);
                } catch (Exception e) {
                    return DollarStatic.logAndRethrow(e);
                }
            });
        } catch (IOException e) {
            return DollarStatic.logAndRethrow(e);
        }
        return this;
    }

    @Override
    public var $notify(var v) {
        return handler.dispatch(v);
    }

    @Override
    public String $listen(Pipeable pipe, String key) {
        return $listen(pipe);
    }

    @Override
    public var $send(var v) {
        return handler.send(v);
    }

    @Override
    public var $each(Pipeable pipe) throws Exception {
        return super.$each(pipe);
    }

    @Override
    public int compareTo(var o) {
        return Comparator.<String>naturalOrder().compare(uri, o.toString());
    }

    @Override
    public var $receive() {
        return handler.receive();
    }

    @Override
    public var $poll() {
        return handler.poll();
    }

    @Override
    public var $peek() {
        return handler.peek();
    }

    @Override
    public var $pop() {
        return handler.pop();
    }

    @Override
    public var $all() {
        return handler.all();
    }


    @Override
    public var $dispatch(var value) {
        return handler.dispatch(value);
    }

    @Override
    public var $give(var value) {
        return handler.give(value);
    }

    @Override
    public var $push(var lhs) {
        return handler.push(lhs);
    }

    @Override
    public var $publish(var lhs) {
        return handler.publish(lhs);
    }

    @Override
    public var $drain() {
        return handler.drain();
    }
}
