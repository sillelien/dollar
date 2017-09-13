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
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dollar.api.DollarException;
import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.Signal;
import dollar.api.Type;
import dollar.api.exceptions.DollarFailureException;
import dollar.api.plugin.Plugins;
import dollar.api.uri.URI;
import dollar.api.uri.URIHandler;
import dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

public class DollarURI extends AbstractDollar {

    private final transient @NotNull URIHandler handler;
    private final transient @NotNull StateMachine<ResourceState, Signal> stateMachine;
    private final @NotNull URI uri;


    public DollarURI(@NotNull URI uri) {
        super();
        this.uri = uri;
        String scheme = uri.scheme();
        try {
            handler = Plugins.resolveURIProvider(scheme).forURI(scheme, uri);

            StateMachineConfig<ResourceState, Signal> stateMachineConfig = getDefaultStateMachineConfig();
            stateMachineConfig.configure(ResourceState.RUNNING).onEntry(i -> handler.start());
            stateMachineConfig.configure(ResourceState.RUNNING).onExit(i -> handler.stop());
            stateMachineConfig.configure(ResourceState.INITIAL).onExit(i -> handler.init());
            stateMachineConfig.configure(ResourceState.DESTROYED).onEntry(i -> handler.destroy());
            stateMachineConfig.configure(ResourceState.PAUSED).onEntry(i -> handler.pause());
            stateMachineConfig.configure(ResourceState.PAUSED).onExit(i -> handler.unpause());
            stateMachine = new StateMachine<>(ResourceState.INITIAL, stateMachineConfig);
        } catch (Exception e) {
            throw new DollarException(e);
        }

    }

    @NotNull
    @Override
    public var $abs() {
        throw new DollarFailureException(ErrorType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $append(@NotNull var value) {
        return handler.append(DollarStatic.$(value));
    }

    @NotNull
    @Override
    public var $as(@NotNull Type type) {
        if (type.is(Type._STRING)) {
            return DollarStatic.$(toHumanString());
        } else if (type.is(Type._LIST)) {
            return $all();
        } else if (type.is(Type._MAP)) {
            return DollarStatic.$("value", this);
        } else if (type.is(Type._VOID)) {
            return DollarStatic.$void();
        } else if (type.is(Type._URI)) {
            return this;
        } else {
            throw new DollarFailureException(ErrorType.INVALID_CAST);
        }
    }

    @NotNull
    @Override
    public var $containsKey(@NotNull var value) {
        return DollarStatic.$(false);
    }

    @Override
    @NotNull
    public var $containsValue(@NotNull var value) {
        return DollarStatic.$(false);
    }

    @NotNull
    @Override
    public var $divide(@NotNull var rhs) {
        throw new DollarFailureException(ErrorType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $get(@NotNull var key) {
        ensureRunning();
        return handler.get(key);
    }

    @NotNull
    @Override
    public var $has(@NotNull var key) {
        ensureRunning();
        return DollarStatic.$(!handler.get(key).isVoid());
    }

    @NotNull
    @Override
    public var $insert(@NotNull var value, int position) {
        return handler.insert(DollarStatic.$(value));
    }

    @NotNull
    @Override
    public var $minus(@NotNull var rhs) {
        ensureRunning();
        return handler.removeValue(DollarStatic.$(rhs));

    }

    @NotNull
    @Override
    public var $modulus(@NotNull var rhs) {
        throw new DollarFailureException(ErrorType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $multiply(@NotNull var v) {
        throw new DollarFailureException(ErrorType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $negate() {
        throw new DollarFailureException(ErrorType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $plus(@NotNull var rhs) {
        ensureRunning();
        return handler.append(rhs);
    }

    @NotNull
    @Override
    public var $prepend(@NotNull var value) {
        return handler.prepend(DollarStatic.$(value));
    }

    @NotNull
    @Override
    public var $remove(@NotNull var key) {
        throw new DollarFailureException(ErrorType.INVALID_URI_OPERATION);

    }

    @NotNull
    @Override
    public var $removeByKey(@NotNull String key) {
        ensureRunning();
        return handler.remove(DollarStatic.$(key));

    }

    @NotNull
    @Override
    public var $set(@NotNull var key, @NotNull Object value) {
        ensureRunning();

        return handler.set(DollarStatic.$(key), DollarStatic.$(value));

    }

    @NotNull
    @Override
    public var $size() {
        ensureRunning();
        return DollarStatic.$(handler.size());
    }

    @NotNull
    @Override
    public var $subscribe(@NotNull Pipeable pipe) {
        return $subscribe(pipe, null);
    }

    @NotNull
    @Override
    public var $subscribe(@NotNull Pipeable pipe, @Nullable String id) {
        ensureRunning();
        final String subId = (id == null) ? UUID.randomUUID().toString() : id;
        try {
            handler.subscribe(i -> {
                try {
                    return pipe.pipe(i);
                } catch (Exception e) {
                    throw new DollarException(e);
                }
            }, subId);
        } catch (IOException e) {
            throw new DollarException(e);
        }
        return DollarFactory.blockCollection(
                Collections.singletonList(DollarStatic.$("id", subId).$("unsub", DollarFactory.fromLambda(i -> {
                    handler.unsubscribe(subId);
                    return DollarStatic.$(subId);
                }))));
    }

    @NotNull
    @Override
    public Type $type() {
        return new Type(Type._URI, constraintLabel());
    }

    @Override
    public boolean collection() {
        return false;
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type.is(Type._URI)) {
                return true;
            }
        }
        return false;
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
    public boolean isVoid() {
        return false;
    }

    @Override
    public boolean neitherTrueNorFalse() {
        return true;
    }

    @Override
    public int sign() {
        return 1;
    }

    @NotNull
    @Override
    public int size() {
        ensureRunning();
        return handler.size();
    }

    @NotNull
    @Override
    public String toDollarScript() {
        return String.format("(\"%s\" as Uri)", org.apache.commons.lang.StringEscapeUtils.escapeJava(uri.toString()));
    }

    @NotNull
    @Override
    public String toHumanString() {
        return uri.toString();
    }

    @Override
    public int toInteger() {
        DollarFactory.failure(ErrorType.INVALID_URI_OPERATION, "Cannot convert a URI to an integer");
        return 0;
    }

    @NotNull
    @Override
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return ImmutableMap.copyOf(Collections.<K, V>emptyMap());
    }

    @NotNull
    @Override
    public <R> R toJavaObject() {
        return (R) uri;
    }

    @NotNull
    @Override
    public ImmutableList<Object> toList() {
        return ImmutableList.of(uri);
    }

    @NotNull
    @Override
    public Number toNumber() {
        return 0;
    }

    @Nullable
    @Override
    public ImmutableList<String> toStrings() {
        return ImmutableList.of();
    }

    @NotNull
    @Override
    public ImmutableList<var> toVarList() {
        ensureRunning();
        return ImmutableList.copyOf(handler.all().toVarList());
    }

    @NotNull
    @Override
    public ImmutableMap<var, var> toVarMap() {
        return ImmutableMap.of();
    }

    @NotNull
    @Override
    public String toYaml() {
        return "uri: \"" + uri + "\"";
    }

    @Override
    public boolean truthy() {
        return handler != null;
    }

    @NotNull

    @Override
    public var $all() {
        ensureRunning();
        return handler.all();
    }

    @NotNull
    @Override
    public var $drain() {
        ensureRunning();
        return handler.drain();
    }

    @NotNull
    @Override
    public var $each(@NotNull Pipeable pipe) {
        return super.$each(pipe);
    }

    @NotNull
    @Override
    public var $notify() {
        ensureRunning();
        return handler.write(this, false, false);
    }

    @NotNull
    @Override
    public var $publish(@NotNull var lhs) {
        ensureRunning();
        return handler.publish(lhs);
    }

    @NotNull
    @Override
    public var $read(boolean blocking, boolean mutating) {
        ensureRunning();
        return handler.read(blocking, mutating);
    }

    @NotNull
    @Override
    public var $write(@NotNull var value, boolean blocking, boolean mutating) {
        ensureRunning();
        return handler.write(value, blocking, mutating);
    }

    @Override
    public boolean uri() {
        return true;
    }

    @NotNull
    @Override
    public StateMachine<ResourceState, Signal> getStateMachine() {
        return stateMachine;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uri);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;
        if (!super.equals(o)) return false;
        DollarURI dollarURI = (DollarURI) o;
        return Objects.equals(uri, dollarURI.uri);
    }

    @Override
    public int compareTo(@NotNull var o) {
        return Comparator.<String>naturalOrder().compare(uri.toString(), o.toString());
    }

    private void ensureRunning() {
        if (stateMachine.isInState(ResourceState.INITIAL)) {
            stateMachine.fire(Signal.START);
        }
        if (!stateMachine.isInState(ResourceState.RUNNING)) {
            throw new DollarException("Resource is in state " + stateMachine.getState() + " should be RUNNING");
        }
    }
}
