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

package com.sillelien.dollar.api.time;

import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.types.DollarFactory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.*;

import static com.sillelien.dollar.api.DollarStatic.$;

public class Scheduler {

    private static final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(3);
    private static final ConcurrentHashMap<String, ScheduledFuture> scheduledTasks = new ConcurrentHashMap<>();

    @NotNull public static String schedule(@NotNull Pipeable task, long duration) {

        String id = UUID.randomUUID().toString();
        scheduledTasks.put(id, scheduler.scheduleAtFixedRate(() -> {
            try {
                task.pipe($(id));
            } catch (Exception e) {
                e.printStackTrace();
                DollarFactory.failure(e);
            }
        }, duration, duration, TimeUnit.MILLISECONDS));
        return id;
    }

    public static void cancel(@NotNull String id) {
        ScheduledFuture scheduledFuture = scheduledTasks.get(id);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }
}
