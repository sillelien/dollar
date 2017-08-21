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

package com.sillelien.dollar.api.time;

import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.execution.DollarExecutor;
import com.sillelien.dollar.api.plugin.Plugins;
import com.sillelien.dollar.api.types.DollarFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static com.sillelien.dollar.api.DollarStatic.$;

public class Scheduler {

    @NotNull
    private static final Logger log = LoggerFactory.getLogger("Scheduler");

    @NotNull
    private static final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(3);
    @Nullable
    private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);
    @NotNull
    private static final ConcurrentHashMap<String, Future> scheduledTasks = new ConcurrentHashMap<>();

    @NotNull
    public static String schedule(@NotNull Pipeable task, long duration) {

        String id = UUID.randomUUID().toString();
        scheduledTasks.put(id, executor.scheduleEvery(duration, () -> {
            try {
                assert executor != null;
                task.pipe($(id));
            } catch (Exception e) {
                log.debug(e.getMessage(), e);
                DollarFactory.failure(e);
            }
        }));
        return id;
    }

    public static void cancel(@NotNull String id) {
        Future future = scheduledTasks.get(id);
        if (future != null) {
            future.cancel(false);
        }
    }
}
