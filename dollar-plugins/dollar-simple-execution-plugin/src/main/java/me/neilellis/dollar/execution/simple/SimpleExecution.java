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

package me.neilellis.dollar.execution.simple;

import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.execution.Execution;
import me.neilellis.dollar.var;

import java.util.concurrent.*;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static me.neilellis.dollar.DollarStatic.$void;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class SimpleExecution implements Execution {

    public ForkJoinPool forkJoinPool;

    private ExecutorService backgroundExecutor;
    private ScheduledExecutorService scheduledExecutor;


    public SimpleExecution() {
        getRuntime().addShutdownHook(new Thread(this::forceShutdown));
        start();
    }

    @Override public void start() {
        forkJoinPool = new ForkJoinPool(getRuntime().availableProcessors() * 8);
        backgroundExecutor = newFixedThreadPool(getRuntime().availableProcessors());
        scheduledExecutor = Executors.newScheduledThreadPool(getRuntime().availableProcessors());
    }

    @Override public void stop() {
        backgroundExecutor.shutdown();
        forkJoinPool.shutdown();
        scheduledExecutor.shutdown();
    }

    @Override public Execution copy() {
        return this;
    }

    @Override public Future<var> executeInBackground(Pipeable pipe) {
        return backgroundExecutor.submit(() -> pipe.pipe($void()));
    }

    @Override public void forceShutdown() {
        backgroundExecutor.shutdownNow();
        forkJoinPool.shutdownNow();
        scheduledExecutor.shutdownNow();
    }

    @Override public void restart() {
        stop();
        start();
    }

    @Override public ScheduledFuture<?> schedule(long millis, Runnable runnable) {
        return scheduledExecutor.scheduleAtFixedRate(runnable, millis, millis, TimeUnit.MILLISECONDS);
    }

    @Override public ForkJoinTask submit(Callable callable) {
        return forkJoinPool.submit(callable);
    }
}
