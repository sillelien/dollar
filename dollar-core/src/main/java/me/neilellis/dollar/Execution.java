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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static me.neilellis.dollar.DollarStatic.$void;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class Execution {

    public static ForkJoinPool forkJoinPool;

    private static ExecutorService backgroundExecutor;

    static {
        init();
    }

    static {
        getRuntime().addShutdownHook(new Thread(Execution::forceShutdown));

    }

    public static void forceShutdown() {
        backgroundExecutor.shutdownNow();
        forkJoinPool.shutdownNow();
    }

    public static void restart() {
        shutdown();
        init();
    }

    private static void shutdown() {
        backgroundExecutor.shutdown();
        forkJoinPool.shutdown();
    }

    public static void init() {
        forkJoinPool = new ForkJoinPool(getRuntime().availableProcessors() * 8);
        backgroundExecutor = newFixedThreadPool(getRuntime().availableProcessors());
    }

    public static Future<var> executeInBackground(Pipeable pipe) {
        return backgroundExecutor.submit(() -> pipe.pipe($void()));
    }
}
