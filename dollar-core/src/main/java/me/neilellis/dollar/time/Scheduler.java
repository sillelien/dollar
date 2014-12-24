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

package me.neilellis.dollar.time;

import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.types.DollarFactory;

import java.util.UUID;
import java.util.concurrent.*;

import static me.neilellis.dollar.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class Scheduler {

    private static final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(3);
    private static final ConcurrentHashMap<String, ScheduledFuture> scheduledTasks = new ConcurrentHashMap<>();

    public static String schedule(Pipeable task, long duration) {

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

    public static void cancel(String id) {
        ScheduledFuture scheduledFuture = scheduledTasks.get(id);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }
    }
}
