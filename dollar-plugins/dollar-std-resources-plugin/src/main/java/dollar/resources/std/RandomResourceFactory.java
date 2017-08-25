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

package dollar.resources.std;

import dollar.api.Pipeable;
import dollar.api.execution.DollarExecutor;
import dollar.api.plugin.Plugins;
import dollar.api.uri.URI;
import dollar.api.uri.URIHandler;
import dollar.api.uri.URIHandlerFactory;
import dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

import static dollar.api.DollarStatic.$;
import static dollar.api.DollarStatic.$void;

public class RandomResourceFactory implements URIHandlerFactory {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(RandomResourceFactory.class);
    @Nullable private static final DollarExecutor
            executor =
            Plugins.sharedInstance(DollarExecutor.class);
    @NotNull
    private final HashMap<String, Pipeable> consumers = new HashMap<>();


    public RandomResourceFactory() {
        executor.scheduleEvery(500, () -> {
            for (Pipeable consumer : consumers.values()) {
                try {
                    consumer.pipe($(Math.random()));
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                }
            }
        });
    }

    @NotNull @Override public URIHandlerFactory copy() {
        return this;
    }

    @NotNull
    @Override
    public URIHandler forURI(@NotNull String scheme, @NotNull URI uri) throws Exception {

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

            @NotNull
            @Override
            public var remove(@NotNull var v) {
                return $void();
            }

            @NotNull
            @Override
            public var removeValue(@NotNull var v) {
                return $void();
            }

            @NotNull
            @Override
            public var set(@NotNull var key, @NotNull var value) {
                return $void();
            }

            @Override public int size() {
                return 1;
            }

            @Override public void start() {
            }

            @Override public void stop() {
            }

            @Override
            public void subscribe(@NotNull Pipeable consumer, @NotNull String id) throws IOException {
                consumers.put(id, consumer);
            }

            @Override public void unpause() {
            }

            @Override
            public void unsubscribe(@NotNull String subId) {
                consumers.remove(subId);
            }
        };
    }

    @Override public boolean handlesScheme(@NotNull String scheme) {
        return "random".equals(scheme);
    }
}
