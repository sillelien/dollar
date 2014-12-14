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
import me.neilellis.dollar.*;
import me.neilellis.dollar.collections.ImmutableList;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.plugin.Plugins;
import me.neilellis.dollar.uri.URI;
import me.neilellis.dollar.uri.URIHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarURI extends AbstractDollar {

    private final StateMachine<ResourceState, Signal> stateMachine;
    private final URI uri;
    private URIHandler handler;


    public DollarURI(@NotNull ImmutableList<Throwable> errors, URI uri) {
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
        return DollarFactory.failure(FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $minus(@NotNull var v) {
        ensureRunning();
        return handler.removeValue(DollarStatic.$(v));

    }

    @NotNull
    @Override
    public var $divide(@NotNull var v) {
        return DollarFactory.failure(FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $plus(@Nullable var v) {
        ensureRunning();
        return handler.write(DollarStatic.$(v), true, true);
    }

    @NotNull
    @Override
    public var $modulus(@NotNull var v) {
        return DollarFactory.failure(FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        return DollarFactory.failure(FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $negate() {
        return DollarFactory.failure(FailureType.INVALID_URI_OPERATION);
    }

    private void ensureRunning() {
        if (stateMachine.isInState(ResourceState.INITIAL)) {
            stateMachine.fire(Signal.START);
        }
        if (!stateMachine.isInState(ResourceState.RUNNING)) {
            throw new DollarException("Resource is in state " + stateMachine.getState() + " should be RUNNING");
        }
    }

    @NotNull
    @Override
    public var $set(@NotNull var key, @Nullable Object value) {
        ensureRunning();

        return handler.set(DollarStatic.$(key), DollarStatic.$(value));

    }

    @NotNull
    @Override
    public <R> R $() {
        return (R) uri;
    }

    @NotNull
    @Override
    public var $get(@NotNull var key) {
        ensureRunning();
        return handler.get(key);
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

    @NotNull
    @Override
    public var $removeByKey(@NotNull String key) {
        ensureRunning();
        return handler.remove(DollarStatic.$(key));

    }

    @NotNull @Override
    public var $remove(var key) {
        return DollarFactory.failure(FailureType.INVALID_URI_OPERATION);

    }

    @Override
    public var $subscribe(Pipeable pipe) {
        return $subscribe(pipe, null);
    }

    @Override
    public var $subscribe(Pipeable pipe, String id) {
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

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {
        return ImmutableMap.of();
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
    public boolean isNeitherTrueNorFalse() {
        return true;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean isTruthy() {
        return handler != null;
    }


    @NotNull






    @Override
    public var $all() {
        ensureRunning();
        return handler.all();
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
    public var $publish(var lhs) {
        ensureRunning();
        return handler.publish(lhs);
    }

    @Override
    public var $read(boolean blocking, boolean mutating) {
        ensureRunning();
        return handler.read(blocking, mutating);
    }

    @Override
    public var $write(var value, boolean blocking, boolean mutating) {
        ensureRunning();
        return handler.write(value, blocking, mutating);
    }


    @Override
    public var $each(Pipeable pipe) {
        return super.$each(pipe);
    }

    @NotNull @Override public StateMachine<ResourceState, Signal> getStateMachine() {
        return stateMachine;
    }

    @Override
    public boolean isUri() {
        return true;
    }


    @Override public Type $type() {
        return Type.URI;
    }

    @Override
    public String S() {
        return uri.toString();
    }


    @NotNull
    @Override
    public ImmutableList<var> $list() {
        ensureRunning();
        return ImmutableList.copyOf(handler.all().$list());
    }

    @NotNull @Override public ImmutableList<Object> toList() {
        return ImmutableList.of(uri);
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
    public ImmutableList<String> strings() {
        return ImmutableList.of();
    }


    @NotNull @Override
    public Map<String, Object> toMap() {
        return Collections.emptyMap();
    }


    @NotNull
    @Override
    public Number N() {
        return 0;
    }

    @Override public boolean isCollection() {
        return false;
    }


    @Override
    public boolean is(@NotNull Type... types) {
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
            case URI:
                return this;
            default:
                return DollarFactory.failure(FailureType.INVALID_CAST);
        }
    }
}
