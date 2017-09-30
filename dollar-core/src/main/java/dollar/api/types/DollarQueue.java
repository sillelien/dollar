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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.Type;
import dollar.api.Value;
import dollar.api.exceptions.DollarFailureException;
import dollar.api.guard.ChainGuard;
import dollar.api.guard.Guarded;
import dollar.api.guard.NotNullGuard;
import dollar.api.guard.NotNullParametersGuard;
import org.jetbrains.annotations.NotNull;
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
    private final Queue<Value> queue;

    public DollarQueue(@NotNull ImmutableList<Throwable> errors, @NotNull Queue<Value> queue) {
        super();
        this.queue = queue;
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $abs() {
        throw new DollarFailureException(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $append(@NotNull Value value) {
        throw new DollarFailureException(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @Override
    @NotNull
    @Guarded(NotNullGuard.class)
    public Value $as(@NotNull Type type) {
        if (type.is(Type._LIST)) {
            return DollarStatic.$(queue.toArray());
        } else if (type.is(Type._MAP)) {
            return DollarStatic.$(queue.toArray()).$map();
        }
        throw new DollarFailureException(ErrorType.INVALID_CAST);
    }

    @Override
    public @NotNull Value $avg(boolean parallel) {
        return $list().$avg(parallel);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $containsKey(@NotNull Value value) {
        throw new DollarFailureException(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $containsValue(@NotNull Value value) {
        throw new DollarFailureException(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $divide(@NotNull Value rhs) {
        throw new DollarFailureException(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $get(@NotNull Value rhs) {
        return (Value) queue.toArray()[rhs.toInteger()];
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $has(@NotNull Value key) {
        throw new DollarFailureException(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $insert(@NotNull Value value, int position) {
        throw new DollarFailureException(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @Override
    public @NotNull Value $max(boolean parallel) {
        return $list().$max(parallel);

    }

    @Override
    public @NotNull Value $min(boolean parallel) {
        return $list().$min(parallel);

    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $modulus(@NotNull Value rhs) {
        throw new DollarFailureException(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $multiply(@NotNull Value v) {
        throw new DollarFailureException(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $negate() {
        throw new DollarFailureException(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $plus(@NotNull Value rhs) {
        return $push(rhs);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $prepend(@NotNull Value value) {
        $push(value);
        return this;
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $remove(@NotNull Value valueToRemove) {
        throw new DollarFailureException(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $removeByKey(@NotNull String key) {
        throw new DollarFailureException(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $set(@NotNull Value key, @NotNull Object value) {
        throw new DollarFailureException(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public Value $size() {
        return DollarStatic.$(queue.size());
    }

    @Override
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @NotNull
    public Value $subscribe(@NotNull Pipeable subscription, @NotNull String key) {
        listeners.put(key, subscription);
        return this;
    }

    @Override
    @NotNull
    public Type $type() {
        return new Type(Type._QUEUE, constraintLabel());
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
    public int size() {
        return queue.size();
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public String toDollarScript() {
        @NotNull Value result;
        throw new DollarFailureException(ErrorType.INVALID_QUEUE_OPERATION);
    }

    @Override
    @Guarded(NotNullGuard.class)
    @NotNull
    public String toHumanString() {
        return toJsonString();
    }

    @Override
    public int toInteger() {
        DollarFactory.failure(ErrorType.INVALID_QUEUE_OPERATION, "Cannot convert a queue to an integer");
        return 0;
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public <K extends Comparable<K>, V> ImmutableMap<K, V> toJavaMap() {
        return $map().toJavaMap();
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public <R> R toJavaObject() {
        return (R) queue;
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
    public ImmutableList<String> toStrings() {
        return ImmutableList.copyOf(queue.stream().map(Value::toString).collect(Collectors.toList()));
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public ImmutableList<Value> toVarList() {
        return ImmutableList.copyOf(Arrays.asList((Value[]) queue.toArray()));
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    public ImmutableMap<Value, Value> toVarMap() {
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
    public boolean truthy() {
        return false;
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public Value $all() {
        return $list();
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    public Value $drain() {
        ArrayList<Value> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            result.add(queue.poll());
        }
        return DollarFactory.fromList(result);
    }

    @NotNull
    @Override
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    public Value $each(@NotNull Pipeable pipe) {
        ArrayList<Value> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            try {
                result.add(pipe.pipe(queue.poll()));
            } catch (Exception e) {
                DollarFactory.failure(e);
            }
        }
        return DollarFactory.fromList(result);
    }

    @NotNull
    @Override
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    public Value $publish(@NotNull Value value) {
        listeners.forEach((s, pipeable) -> {
            try {
                pipeable.pipe(value);
            } catch (Exception e) {
                DollarFactory.failure(e);
            }
        });
        return this;
    }

    /**
     * Generic read
     */
    @Override
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public Value $read(boolean blocking, boolean mutating) {
        if (mutating) {
            return queue.poll();
        } else {
            return queue.peek();
        }
    }

    @Override
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    public Value $write(@NotNull Value value, boolean blocking, boolean mutating) {
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
            throw new DollarFailureException(ErrorType.INVALID_QUEUE_OPERATION);
        }
    }

    @Override
    public boolean queue() {
        return true;
    }

    @Override
    public int compareTo(Value o) {
        return 0;
    }
}
