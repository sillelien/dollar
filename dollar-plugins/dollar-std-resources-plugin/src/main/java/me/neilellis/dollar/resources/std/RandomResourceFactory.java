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

package me.neilellis.dollar.resources.std;

import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.execution.DollarExecutor;
import me.neilellis.dollar.plugin.Plugins;
import me.neilellis.dollar.uri.URI;
import me.neilellis.dollar.uri.URIHandler;
import me.neilellis.dollar.uri.URIHandlerFactory;
import me.neilellis.dollar.var;

import java.io.IOException;
import java.util.HashMap;

import static me.neilellis.dollar.DollarStatic.$;
import static me.neilellis.dollar.DollarStatic.$void;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class RandomResourceFactory implements URIHandlerFactory {
    private static final me.neilellis.dollar.execution.DollarExecutor
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

    @Override public URIHandlerFactory copy() {
        return this;
    }

    @Override public URIHandler forURI(String scheme, URI uri) throws Exception {

        return new URIHandler() {

            @Override public var all() {
                return $(Math.random());
            }

            @Override public var write(var value, boolean blocking, boolean mutating) {
                return $(Math.random() % value.toDouble());
            }

            @Override public void destroy() { }

            @Override public var drain() { return $(Math.random()); }

            @Override public var get(var key) {
                return $(Math.random() % key.toDouble());
            }

            @Override public void init() { }

            @Override public void pause() { }

            @Override public var read(boolean blocking, boolean mutating) {
                return $(Math.random());
            }

            @Override public var remove(var v) {
                return $void();
            }

            @Override public var removeValue(var v) {
                return $void();
            }

            @Override public var set(var key, var value) {
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

    @Override public boolean handlesScheme(String scheme) {
        return scheme.equals("random");
    }
}
