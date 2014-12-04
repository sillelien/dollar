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

package me.neilellis.dollar;

import java.util.concurrent.*;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static me.neilellis.dollar.DollarStatic.$void;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class Execution {

    public static ForkJoinPool forkJoinPool;

    private static ExecutorService backgroundExecutor;
    private static ScheduledExecutorService scheduledExecutor;

    static {
        init();
    }

    static {
        getRuntime().addShutdownHook(new Thread(Execution::forceShutdown));

    }

    static void forceShutdown() {
        backgroundExecutor.shutdownNow();
        forkJoinPool.shutdownNow();
        scheduledExecutor.shutdownNow();
    }

    public static void restart() {
        shutdown();
        init();
    }

    private static void shutdown() {
        backgroundExecutor.shutdown();
        forkJoinPool.shutdown();
        scheduledExecutor.shutdown();
    }

    private static void init() {
        forkJoinPool = new ForkJoinPool(getRuntime().availableProcessors() * 8);
        backgroundExecutor = newFixedThreadPool(getRuntime().availableProcessors());
        scheduledExecutor = Executors.newScheduledThreadPool(getRuntime().availableProcessors());
    }

    public static Future<var> executeInBackground(Pipeable pipe) {
        return backgroundExecutor.submit(() -> pipe.pipe($void()));
    }

    public static void schedule(long millis, Runnable runnable) {
        scheduledExecutor.scheduleAtFixedRate(runnable, millis, millis, TimeUnit.MILLISECONDS);
    }
}
