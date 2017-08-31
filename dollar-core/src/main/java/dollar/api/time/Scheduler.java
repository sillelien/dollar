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

package dollar.api.time;

import dollar.api.DollarException;
import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.execution.DollarExecutor;
import dollar.api.plugin.Plugins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public final class Scheduler {

    @NotNull
    private static final Logger log = LoggerFactory.getLogger("Scheduler");


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
                task.pipe(DollarStatic.$(id));
            } catch (Exception e) {
                throw new DollarException(e);
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
