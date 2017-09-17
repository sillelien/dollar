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

package dollar.uri.mapdb;

import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.Value;
import dollar.api.execution.DollarExecutor;
import dollar.api.plugin.Plugins;
import dollar.api.types.DollarFactory;
import dollar.api.uri.URI;
import dollar.internal.mapdb.BTreeMap;
import dollar.internal.mapdb.MapModificationListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class MapDBCircleURI extends AbstractMapDBURI implements MapModificationListener<Value, Value> {
    @Nullable
    private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(MapDBCircleURI.class);
    @NotNull
    private static final ConcurrentHashMap<String, Future> subscribers = new ConcurrentHashMap<>();
    @NotNull
    private final ArrayBlockingQueue<Value> queue;


    public MapDBCircleURI(@NotNull String scheme, @NotNull URI uri) {
        super(uri, scheme);
        try (BTreeMap<Value, Value> bTreeMap = tx.treeMap(getHost(), new VarSerializer(), new VarSerializer()).modificationListener(
                this).createOrOpen()) {

        }
        int size = Integer.parseInt(uri.paramWithDefault("size", "100").get(0));
        queue = new ArrayBlockingQueue<>(size);
    }

    @NotNull
    @Override
    public Value all() {
        final BlockingQueue<Value> queue = getQueue();
        final ArrayList<Value> objects = new ArrayList<>();
        queue.drainTo(objects);
        final Value result = DollarFactory.fromValue(objects);
        return result;

    }

    @NotNull
    @Override
    public Value drain() {
        final BlockingQueue<Value> queue = getQueue();
        final ArrayList<Value> objects = new ArrayList<>();
        queue.drainTo(objects);
        return DollarFactory.fromValue(objects);
    }

    @NotNull
    @Override
    public Value get(@NotNull Value key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value read(boolean blocking, boolean mutating) {
        try {
            if (blocking && mutating) {
                return getQueue().take();
            } else if (blocking) {
                return getQueue().poll(60, TimeUnit.SECONDS);
            } else if (mutating) {
                return getQueue().poll();
            } else {
                return getQueue().peek();
            }
        } catch (InterruptedException e) {
            log.debug(e.getMessage(), e);
            return DollarStatic.$void();
        }
    }

    @NotNull
    @Override
    public Value remove(@NotNull Value v) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Value removeValue(@NotNull Value v) {
        Value unwrapped = v.$unwrap();
        if (getQueue().remove(unwrapped)) {
            return unwrapped;
        } else {
            return DollarStatic.$void();
        }
    }

    @NotNull
    @Override
    public Value set(@NotNull Value key, @NotNull Value value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return getQueue().size();
    }

    @Override
    public void subscribe(@NotNull Pipeable consumer, @NotNull String id) throws IOException {
        final Future schedule = executor.scheduleEvery(1000, () -> {
            Object o = getQueue().poll();
            if (o != null) {
                try {
                    consumer.pipe(DollarStatic.$(o));
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
        subscribers.put(id, schedule);
    }

    @Override
    public void unsubscribe(@NotNull String subId) {
        subscribers.get(subId).cancel(false);
    }

    @NotNull
    @Override
    public Value write(@NotNull Value value, boolean blocking, boolean mutating) {
        if (value.isVoid()) {
            return DollarStatic.$(false);
        }
        try {
            if (!blocking) {
                return DollarStatic.$(getQueue().offer(value.$fixDeep()));
            } else {
                getQueue().put(value.$fixDeep());
                return DollarStatic.$(true);
            }

        } catch (InterruptedException e) {
            log.debug(e.getMessage(), e);
            return DollarStatic.$(false);
        }
    }

    @NotNull
    private BlockingQueue<Value> getQueue() {
        return queue;
    }

    @Override
    public void modify(@NotNull Value key, @Nullable Value oldValue, @Nullable Value newValue, boolean triggered) {
        if (newValue != null) {
            queue.offer(newValue.$fixDeep());
        }
    }
}
