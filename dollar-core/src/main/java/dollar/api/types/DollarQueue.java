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

import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.Type;
import dollar.api.collections.ImmutableList;
import dollar.api.collections.ImmutableMap;
import dollar.api.guard.ChainGuard;
import dollar.api.guard.Guarded;
import dollar.api.guard.NotNullGuard;
import dollar.api.guard.NotNullParametersGuard;
import dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Still under active development, I wouldn't use just yet!
 */
public class DollarQueue extends AbstractDollar {

    @NotNull
    private final ConcurrentHashMap<String, Pipeable> listeners = new ConcurrentHashMap<>();
    @NotNull
    private final Queue<var> queue;

    public DollarQueue(@NotNull ImmutableList<Throwable> errors, @NotNull Queue<var> queue) {
        super(errors);
        this.queue = queue;
    }

    /**
     * Generic read
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public var $read(boolean blocking, boolean mutating) {
        if (mutating) {
            return queue.poll();
        } else {
            return queue.peek();
        }
    }

    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public var $write(@NotNull var value, boolean blocking, boolean mutating) {
        if (mutating) {
            queue.add(value);
            listeners.forEach((s, pipeable) -> {
                try {
                    pipeable.pipe(value);
                } catch (Exception e) {
                    DollarFactory.failure(e);
                }
            });
            return this;
        } else {
            return DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
        }
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public var $all() {
        return $list();
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public var $drain() {
        ArrayList<var> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            result.add(queue.poll());
        }
        return DollarFactory.fromList(result);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public var $publish(@NotNull var value) {
        listeners.forEach((s, pipeable) -> {
            try {
                pipeable.pipe(value);
            } catch (Exception e) {
                DollarFactory.failure(e);
            }
        });
        return this;
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public var $each(@NotNull Pipeable pipe) {
        ArrayList<var> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            try {
                result.add(pipe.pipe(queue.poll()));
            } catch (Exception e) {
                DollarFactory.failure(e);
            }
        }
        return DollarFactory.fromList(result);
    }

    @Override
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @NotNull
    public var $subscribe(@NotNull Pipeable subscription, @NotNull String key) {
        listeners.put(key, subscription);
        return this;
    }

    @Override
    @Guarded(NotNullGuard.class)
    @NotNull
    public String toHumanString() {
        return toJsonString();
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public String toDollarScript() {
        DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
        return ErrorType.INVALID_QUEUE_OPERATION.toString();
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public <R> R toJavaObject() {
        return (R) queue;
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
        return false;
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $get(@NotNull var rhs) {
        return (var) queue.toArray()[rhs.toInteger()];
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $append(@NotNull var value) {
        return DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $containsValue(@NotNull var value) {
        return DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $containsKey(@NotNull var value) {
        return DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $has(@NotNull var key) {
        return DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $size() {
        return DollarStatic.$(queue.size());
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $prepend(@NotNull var value) {
        $push(value);
        return this;
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $insert(@NotNull var value, int position) {
        return DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $removeByKey(@NotNull String key) {
        return DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $set(@NotNull var key, @Nullable Object value) {
        return DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $remove(@NotNull var valueToRemove) {
        return DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public int compareTo(var o) {
        return 0;
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $abs() {
        return DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $plus(@NotNull var rhs) {
        return $push(rhs);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $negate() {
        return DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $divide(@NotNull var rhs) {
        return DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $modulus(@NotNull var rhs) {
        return DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public var $multiply(@NotNull var v) {
        return DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    public Integer toInteger() {
        DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION);
        return null;
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public var $as(@NotNull Type type) {
        if (type.is(Type._LIST)) {
            return DollarStatic.$(queue.toArray());
        } else if (type.is(Type._MAP)) {
            return DollarStatic.$(queue.toArray()).$map();
        }
        return DollarFactory.failure(ErrorType.INVALID_CAST);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public ImmutableList<var> toVarList() {
        return ImmutableList.copyOf(Arrays.asList((var[]) queue.toArray()));
    }

    @Override
    @NotNull
    public Type $type() {
        return new Type(Type._QUEUE, constraintLabel());
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public ImmutableMap<var, var> toVarMap() {
        return DollarStatic.$(queue.toArray()).toVarMap();
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public String toYaml() {
        Yaml yaml = new Yaml();
        return yaml.dump(queue.toArray());
    }

    @Override
    @Guarded(NotNullGuard.class)
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type.is(Type._QUEUE)) {
                return true;
            }
        }
        return false;
    }


    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public ImmutableList<String> strings() {
        return ImmutableList.copyOf(queue.stream().map(var::toString).collect(Collectors.toList()));
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public ImmutableList<?> toList() {
        return ImmutableList.copyOf(Arrays.asList(queue.toArray()));
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return $map().toJavaMap();
    }

    @Override
    public boolean queue() {
        return true;
    }
}
