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

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.google.common.collect.ImmutableList;
import me.neilellis.dollar.*;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.json.JsonObject;
import me.neilellis.dollar.plugin.Plugins;
import me.neilellis.dollar.uri.URIHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarURI extends AbstractDollar {

    private final StateMachine<ResourceState, Signal> stateMachine;
    private String uri;
    private URIHandler handler;


    public DollarURI(@NotNull List<Throwable> errors, String uri) throws Exception {
        super(errors);
        this.uri = uri.substring(uri.indexOf(":") + 1);
        String scheme = uri.substring(0, uri.indexOf(":"));
        handler = Plugins.resolveURIProvider(scheme).forURI(scheme, this.uri);
        StateMachineConfig<ResourceState, Signal> stateMachineConfig = getDefaultStateMachineConfig();
        stateMachineConfig.configure(ResourceState.RUNNING).onEntry(i -> {handler.start();});
        stateMachineConfig.configure(ResourceState.RUNNING).onExit(i -> {handler.stop();});
        stateMachineConfig.configure(ResourceState.INITIAL).onExit(i -> {handler.init();});
        stateMachineConfig.configure(ResourceState.DESTROYED).onEntry(i -> {handler.destroy();});
        stateMachineConfig.configure(ResourceState.PAUSED).onEntry(i -> {handler.pause();});
        stateMachineConfig.configure(ResourceState.PAUSED).onExit(i -> {handler.unpause();});
        stateMachine = new StateMachine<ResourceState, Signal>(ResourceState.INITIAL, stateMachineConfig);
        stateMachine.fire(Signal.START);

    }

    @NotNull
    @Override
    public var $(@NotNull var key, @Nullable Object value) {
        assertRunning();

        return handler.set(DollarStatic.$(key), DollarStatic.$(value));

    }

    @NotNull
    @Override
    public var $get(@NotNull String key) {
        assertRunning();

        return handler.get(DollarStatic.$(key));
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
        assertRunning();
        return handler.all().toList().stream();
    }

    @NotNull
    @Override
    public Stream<var> $children(@NotNull String key) {
        assertRunning();

        return handler.get(DollarStatic.$(key)).toList().stream();
    }

    public var $containsValue(var value) {
        return DollarStatic.$(false);
    }

    @Override
    public var $has(@NotNull String key) {
        assertRunning();
        return DollarStatic.$(!handler.get(DollarStatic.$(key)).isVoid());
    }

    @NotNull
    @Override
    public var $minus(@NotNull var value) {
        assertRunning();
        return handler.removeValue(DollarStatic.$(value));

    }

    @NotNull
    @Override
    public var $plus(@Nullable var value) {
        assertRunning();
        return handler.send(DollarStatic.$(value), true, true);
    }

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {
        return ImmutableMap.of();
    }

    @NotNull
    @Override
    public var $rm(@NotNull String key) {
        assertRunning();

        return handler.remove(DollarStatic.$(key));

    }

    @Override
    public var $size() {
        assertRunning();
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

    private void assertRunning() {
        if (!stateMachine.isInState(ResourceState.RUNNING)) {
            throw new DollarException("Resource is in state " + stateMachine.getState() + " should be RUNNING");
        }
    }

    @NotNull
    @Override
    public var $abs() {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $dec(@NotNull var amount) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $divide(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $inc(@NotNull var amount) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $negate() {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public Integer I(@NotNull String key) {
        return 0;
    }

    @Override
    public var $all() {
        assertRunning();
        return handler.all();
    }

    @Override
    public var $drain() {
        assertRunning();
        return handler.drain();
    }

    @Override
    public String $listen(Pipeable pipe, String key) {
        return $listen(pipe);
    }

    @Override
    public var $notify() {
        assertRunning();
        return handler.send(this, false, false);
    }

    @Override
    public var $publish(var lhs) {
        assertRunning();
        return handler.publish(lhs);
    }

    @Override
    public var $receive(boolean blocking, boolean mutating) {
        assertRunning();
        return handler.receive(blocking, mutating);
    }

    @Override
    public var $send(var value, boolean blocking, boolean mutating) {
        assertRunning();
        return handler.send(value, blocking, mutating);
    }

    @Override
    public var $subscribe(Pipeable pipe) {
        assertRunning();
        final String subId = UUID.randomUUID().toString();
        try {
            handler.subscribe(i -> {
                try {
                    return pipe.pipe(i);
                } catch (Exception e) {
                    return DollarStatic.logAndRethrow(e);
                }
            }, subId);
        } catch (IOException e) {
            return DollarStatic.logAndRethrow(e);
        }
        return DollarStatic.$("id", subId).$("unsub", DollarFactory.fromLambda(i -> {
            handler.unsubscribe(subId);
            return DollarStatic.$(subId);
        }));
    }

    @Override
    public var $each(Pipeable pipe) {
        return super.$each(pipe);
    }

    @Override public StateMachine<ResourceState, Signal> getStateMachine() {
        return stateMachine;
    }

    @Override
    public boolean isUri() {
        return true;
    }

    @Override
    public int compareTo(var o) {
        return Comparator.<String>naturalOrder().compare(uri, o.toString());
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public String S() {
        return uri;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean isTruthy() {
        return handler != null;
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isNeitherTrueNorFalse() {
        return true;
    }


    @NotNull
    @Override
    public ImmutableList<var> toList() {
        assertRunning();
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
                return DollarFactory.failure(DollarFail.FailureType.INVALID_CAST);
        }
    }
}
