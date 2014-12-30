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

package me.neilellis.dollar.resources.std;

import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.execution.DollarExecutor;
import me.neilellis.dollar.plugin.Plugins;
import me.neilellis.dollar.uri.URI;
import me.neilellis.dollar.uri.URIHandler;
import me.neilellis.dollar.uri.URIHandlerFactory;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;

import static me.neilellis.dollar.DollarStatic.$;
import static me.neilellis.dollar.DollarStatic.$void;

public class RandomResourceFactory implements URIHandlerFactory {
    @Nullable private static final me.neilellis.dollar.execution.DollarExecutor
            executor =
            Plugins.sharedInstance(DollarExecutor.class);
    private final HashMap<String, Pipeable> consumers = new HashMap<>();


    public RandomResourceFactory() {
        executor.scheduleEvery(500, () -> {
            for (Pipeable consumer : consumers.values()) {
                try {
                    consumer.pipe($(Math.random()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @NotNull @Override public URIHandlerFactory copy() {
        return this;
    }

    @NotNull @Override public URIHandler forURI(String scheme, URI uri) throws Exception {

        return new URIHandler() {

            @NotNull @Override public var all() {
                return $(Math.random());
            }

            @NotNull @Override public var write(@NotNull var value, boolean blocking, boolean mutating) {
                return $(Math.random() % value.toDouble());
            }

            @Override public void destroy() { }

            @NotNull @Override public var drain() { return $(Math.random()); }

            @NotNull @Override public var get(@NotNull var key) {
                return $(Math.random() % key.toDouble());
            }

            @Override public void init() { }

            @Override public void pause() { }

            @NotNull @Override public var read(boolean blocking, boolean mutating) {
                return $(Math.random());
            }

            @NotNull @Override public var remove(var v) {
                return $void();
            }

            @NotNull @Override public var removeValue(var v) {
                return $void();
            }

            @NotNull @Override public var set(var key, var value) {
                return $void();
            }

            @Override public int size() {
                return 1;
            }

            @Override public void start() {
            }

            @Override public void stop() {
            }

            @Override public void subscribe(Pipeable consumer, String id) throws IOException {
                consumers.put(id, consumer);
            }

            @Override public void unpause() {
            }

            @Override public void unsubscribe(String subId) {
                consumers.remove(subId);
            }
        };
    }

    @Override public boolean handlesScheme(@NotNull String scheme) {
        return scheme.equals("random");
    }
}
