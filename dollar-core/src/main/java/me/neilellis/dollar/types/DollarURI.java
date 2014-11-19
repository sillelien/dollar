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
import me.neilellis.dollar.Type;
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
    public <R> R $() {
        return (R) uri;
    }

    @NotNull
    @Override
    public var $(@NotNull Number n) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
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

    public var $containsValue(var value) {
        return DollarStatic.$(false);
    }

    @Override
    public var $has(@NotNull String key) {
        return DollarStatic.$(!handler.get(DollarStatic.$(key)).isVoid());
    }

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {
        return ImmutableMap.of();
    }

    @NotNull
    @Override
    public var $get(@NotNull String key) {
        return handler.get(DollarStatic.$(key));
    }

    @NotNull
    @Override
    public var $minus(@NotNull Object value) {
        return handler.removeValue(DollarStatic.$(value));

    }

    @NotNull
    @Override
    public var $plus(@Nullable Object value) {
        return handler.send(DollarStatic.$(value), true, true);
    }

    @NotNull
    @Override
    public var $rm(@NotNull String key) {
        return handler.remove(DollarStatic.$(key));

    }

    @NotNull
    @Override
    public var $(@NotNull var key, @Nullable Object value) {
        return handler.set(DollarStatic.$(key), DollarStatic.$(value));

    }

    @Override
    public var $size() {
        return DollarStatic.$(handler.size());
    }

    @Override
    public Stream<String> keyStream() {
        return Stream.empty();
    }

    @Override
    public var remove(Object key) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);

    }

    @NotNull
    @Override
    public var $dec(@NotNull var amount) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $inc(@NotNull var amount) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $negate() {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $divide(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $abs() {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public var $notify(var v) {
        return handler.send(v, false, false);
    }

    @Override
    public String $listen(Pipeable pipe, String key) {
        return $listen(pipe);
    }

    @Override
    public var $each(Pipeable pipe) {
        return super.$each(pipe);
    }

    @Override
    public var $drain() {
        return handler.drain();
    }

    @Override
    public var $all() {
        return handler.all();
    }

    @Override
    public boolean isUri() {
        return true;
    }

    @Override
    public var $send(var value, boolean blocking, boolean mutating) {
        return handler.send(value, blocking, mutating);
    }

    @Override
    public var $receive(boolean blocking, boolean mutating) {
        return handler.receive(blocking, mutating);
    }

    @Override
    public String S() {
        return uri;
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
    public var $publish(var lhs) {
        return handler.publish(lhs);
    }

    @NotNull
    @Override
    public ImmutableList<var> toList() {
        return ImmutableList.copyOf(handler.all().toList());
    }

    @Override
    public int compareTo(var o) {
        return Comparator.<String>naturalOrder().compare(uri, o.toString());
    }

    @Override
    public var decode() {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @NotNull
    @Override
    public Integer I() {
        return 0;
    }

    @Override
    public boolean isTruthy() {
        return handler != null;
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @NotNull
    @Override
    public Integer I(@NotNull String key) {
        return 0;
    }

    @Override
    public boolean isNeitherTrueNorFalse() {
        return true;
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
    public boolean is(Type... types) {
        for (Type type : types) {
            if (type == Type.URI) {
                return true;
            }
        }
        return false;
    }

    @Override
    public var $as(Type type) {
        switch (type) {
            case STRING:
                return DollarStatic.$(S());
            case LIST:
                return $all();
            case MAP:
                return DollarStatic.$("value", this);
            case VOID:
                return DollarStatic.$void();
            default:
                throw new UnsupportedOperationException();
        }
    }
}
