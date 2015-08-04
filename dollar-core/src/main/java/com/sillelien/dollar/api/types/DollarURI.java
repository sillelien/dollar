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
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.sillelien.dollar.api.*;
import com.sillelien.dollar.api.collections.ImmutableList;
import com.sillelien.dollar.api.collections.ImmutableMap;
import com.sillelien.dollar.api.plugin.Plugins;
import com.sillelien.dollar.api.uri.URI;
import com.sillelien.dollar.api.uri.URIHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class DollarURI extends AbstractDollar {

    @NotNull private final StateMachine<ResourceState, Signal> stateMachine;
    @NotNull private final URI uri;
    private URIHandler handler;


    public DollarURI(@NotNull ImmutableList<Throwable> errors, @NotNull URI uri) {
        super(errors);
        this.uri = uri;
        String scheme = uri.scheme();
        try {
            handler = Plugins.resolveURIProvider(scheme).forURI(scheme, uri);
        } catch (Exception e) {
            throw new DollarException(e);
        }
        StateMachineConfig<ResourceState, Signal> stateMachineConfig = getDefaultStateMachineConfig();
        stateMachineConfig.configure(ResourceState.RUNNING).onEntry(i -> {handler.start();});
        stateMachineConfig.configure(ResourceState.RUNNING).onExit(i -> {handler.stop();});
        stateMachineConfig.configure(ResourceState.INITIAL).onExit(i -> {handler.init();});
        stateMachineConfig.configure(ResourceState.DESTROYED).onEntry(i -> {handler.destroy();});
        stateMachineConfig.configure(ResourceState.PAUSED).onEntry(i -> {handler.pause();});
        stateMachineConfig.configure(ResourceState.PAUSED).onExit(i -> {handler.unpause();});
        stateMachine = new StateMachine<>(ResourceState.INITIAL, stateMachineConfig);

    }

    @NotNull
    @Override
    public var $abs() {
        return DollarFactory.failure(ErrorType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $minus(@NotNull var rhs) {
        ensureRunning();
        return handler.removeValue(DollarStatic.$(rhs));

    }

    @NotNull
    @Override
    public var $plus(@NotNull var rhs) {
        ensureRunning();
        return handler.append(rhs);
    }

    @NotNull
    @Override
    public var $negate() {
        return DollarFactory.failure(ErrorType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $divide(@NotNull var rhs) {
        return DollarFactory.failure(ErrorType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        return DollarFactory.failure(ErrorType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return DollarFactory.failure(ErrorType.INVALID_URI_OPERATION);
    }

    @Override public int sign() {
        return 1;
    }

    @NotNull
    @Override
    public Integer toInteger() {
        return 0;
    }

    @NotNull
    @Override
    public Number toNumber() {
        return 0;
    }

    private void ensureRunning() {
        if (stateMachine.isInState(ResourceState.INITIAL)) {
            stateMachine.fire(Signal.START);
        }
        if (!stateMachine.isInState(ResourceState.RUNNING)) {
            throw new DollarException("Resource is in state " + stateMachine.getState() + " should be RUNNING");
        }
    }

    @Override
    public var $as(@NotNull Type type) {
        if (type.equals(Type.STRING)) {
            return DollarStatic.$(toHumanString());
        } else if (type.equals(Type.LIST)) {
            return $all();
        } else if (type.equals(Type.MAP)) {
            return DollarStatic.$("value", this);
        } else if (type.equals(Type.VOID)) {
            return DollarStatic.$void();
        } else if (type.equals(Type.URI)) {
            return this;
        } else {
            return DollarFactory.failure(ErrorType.INVALID_CAST);
        }
    }

    @NotNull @Override
    public String toHumanString() {
        return uri.toString();
    }

    @NotNull @Override public String toDollarScript() {
        return String.format("(\"%s\" as Uri)", org.apache.commons.lang.StringEscapeUtils.escapeJava(uri.toString()));
    }

    @NotNull
    @Override
    public <R> R toJavaObject() {
        return (R) uri;
    }

    @NotNull

    @Override
    public var $all() {
        ensureRunning();
        return handler.all();
    }

    @Override
    public var $write(var value, boolean blocking, boolean mutating) {
        ensureRunning();
        return handler.write(value, blocking, mutating);
    }

    @Override
    public var $drain() {
        ensureRunning();
        return handler.drain();
    }

    @Override
    public var $notify() {
        ensureRunning();
        return handler.write(this, false, false);
    }

    @Override
    public var $read(boolean blocking, boolean mutating) {
        ensureRunning();
        return handler.read(blocking, mutating);
    }

    @Override
    public var $publish(var lhs) {
        ensureRunning();
        return handler.publish(lhs);
    }

    @Override
    public var $each(@NotNull Pipeable pipe) {
        return super.$each(pipe);
    }

    @NotNull @Override public StateMachine<ResourceState, Signal> getStateMachine() {
        return stateMachine;
    }

    @Override
    public boolean uri() {
        return true;
    }

    @NotNull
    @Override
    public ImmutableList<var> $list() {
        ensureRunning();
        return ImmutableList.copyOf(handler.all().$list());
    }

    @Override public Type $type() {
        return Type.URI;
    }

    @Override public boolean collection() {
        return false;
    }

    @NotNull
    @Override
    public ImmutableMap<var, var> $map() {
        return ImmutableMap.of();
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (Objects.equals(type, Type.URI)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Nullable
    @Override
    public ImmutableList<String> strings() {
        return ImmutableList.of();
    }

    @NotNull @Override public ImmutableList<Object> toList() {
        return ImmutableList.of(uri);
    }

    @NotNull @Override
    public Map<String, Object> toMap() {
        return Collections.emptyMap();
    }

    @NotNull
    @Override
    public var $get(@NotNull var key) {
        ensureRunning();
        return handler.get(key);
    }

    @NotNull @Override public var $append(@NotNull var value) {
        return handler.append(DollarStatic.$(value));
    }

    @NotNull public var $containsValue(@NotNull var value) {
        return DollarStatic.$(false);
    }

    @NotNull @Override
    public var $has(@NotNull var key) {
        ensureRunning();
        return DollarStatic.$(!handler.get(key).isVoid());
    }

    @NotNull @Override
    public var $size() {
        ensureRunning();
        return DollarStatic.$(handler.size());
    }

    @NotNull @Override public var $prepend(@NotNull var value) {
        return handler.prepend(DollarStatic.$(value));
    }

    @NotNull
    @Override
    public var $removeByKey(@NotNull String key) {
        ensureRunning();
        return handler.remove(DollarStatic.$(key));

    }

    @NotNull
    @Override
    public var $set(@NotNull var key, @Nullable Object value) {
        ensureRunning();

        return handler.set(DollarStatic.$(key), DollarStatic.$(value));

    }

    @NotNull @Override
    public var $remove(var key) {
        return DollarFactory.failure(ErrorType.INVALID_URI_OPERATION);

    }

    @Override
    public var $subscribe(@NotNull Pipeable pipe) {
        return $subscribe(pipe, null);
    }

    @Override
    public var $subscribe(@NotNull Pipeable pipe, @Nullable String id) {
        ensureRunning();
        final String subId = id == null ? UUID.randomUUID().toString() : id;
        try {
            handler.subscribe(i -> {
                try {
                    return pipe.pipe(i);
                } catch (Exception e) {
                    e.printStackTrace();
                    return DollarStatic.logAndRethrow(e);
                }
            }, subId);
        } catch (IOException e) {
            e.printStackTrace();
            return DollarStatic.logAndRethrow(e);
        }
        return DollarFactory.blockCollection(
                Arrays.asList(DollarStatic.$("id", subId).$("unsub", DollarFactory.fromLambda(i -> {
                    handler.unsubscribe(subId);
                    return DollarStatic.$(subId);
                }))));
    }

    @Override
    public int compareTo(@NotNull var o) {
        return Comparator.<String>naturalOrder().compare(uri.toString(), o.toString());
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean neitherTrueNorFalse() {
        return true;
    }

    @Override
    public boolean truthy() {
        return handler != null;
    }
}
