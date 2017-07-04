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

package com.sillelien.dollar.uri.mapdb;

import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.execution.DollarExecutor;
import com.sillelien.dollar.api.plugin.Plugins;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.uri.URI;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapdb.BTreeMap;
import org.mapdb.MapModificationListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.sillelien.dollar.api.DollarStatic.$;
import static com.sillelien.dollar.api.DollarStatic.$void;

public class MapDBCircleURI extends AbstractMapDBURI implements MapModificationListener<var, var> {

    private static final ConcurrentHashMap<String, Future> subscribers = new ConcurrentHashMap<>();
    @Nullable private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);
    private final BTreeMap<var, var> bTreeMap;
    private final int size;
    private final ArrayBlockingQueue<var> queue;


    public MapDBCircleURI(String scheme, @NotNull URI uri) {
        super(uri, scheme);
        bTreeMap = tx.treeMap(getHost(), new VarSerializer(), new VarSerializer()).modificationListener(this).createOrOpen();
        size= Integer.parseInt(uri.paramWithDefault("size", "100").get(0));
        queue = new ArrayBlockingQueue<>(size);
    }

    @NotNull @Override public var all() {
        final BlockingQueue<var> queue = getQueue();
        final ArrayList<var> objects = new ArrayList<>();
        queue.drainTo(objects);
        final var result = DollarFactory.fromValue(objects);
        return result;

    }

    @Override public var write(@NotNull var value, boolean blocking, boolean mutating) {
        if (value.isVoid()) {
            return $(false);
        }
            try {
                if (!blocking) {
                    return $(getQueue().offer(value._fixDeep()));
                } else {
                    getQueue().put(value._fixDeep());
                    return $(true);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                return $(false);
            }
    }

    @Override public var drain() {
            final BlockingQueue<var> queue = getQueue();
            final ArrayList<var> objects = new ArrayList<>();
            queue.drainTo(objects);
        return DollarFactory.fromValue(objects);
    }

    @NotNull @Override public var get(var key) {
        throw new UnsupportedOperationException();
    }

    @Override public var read(boolean blocking, boolean mutating) {
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
                e.printStackTrace();
                return $void();
            }
    }

    @NotNull @Override public var remove(var v) {
        throw new UnsupportedOperationException();
    }

    @Override public var removeValue(@NotNull var v) {
        var unwrapped = v._unwrap();
        if(getQueue().remove(unwrapped)) {
             return unwrapped;
         } else {
            return $void();
        }
    }

    @NotNull @Override public var set(var key, var value) {
        throw new UnsupportedOperationException();
    }

    @Override public int size() {
        return getQueue().size();
    }

    @Override public void subscribe(@NotNull Pipeable consumer, @NotNull String id) throws IOException {
        final Future schedule = executor.scheduleEvery(1000, () ->{
            Object o = MapDBCircleURI.this.getQueue().poll();
            if (o != null) {
                try {
                    consumer.pipe($(o));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        subscribers.put(id, schedule);
    }

    @Override public void unsubscribe(@NotNull String subId) {
        subscribers.get(subId).cancel(false);
    }

    private BlockingQueue<var> getQueue() {
        return queue;
    }

    @Override
    public void modify(@NotNull var key, @Nullable var oldValue, @Nullable var newValue, boolean triggered) {
        if(newValue != null) {
            queue.offer(newValue._fixDeep());
        }
    }
}
