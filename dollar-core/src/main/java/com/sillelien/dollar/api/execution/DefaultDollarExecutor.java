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

package com.sillelien.dollar.api.execution;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class DefaultDollarExecutor implements DollarExecutor {

    public ForkJoinPool forkJoinPool;

    private ExecutorService backgroundExecutor;
    private ScheduledExecutorService scheduledExecutor;


    public DefaultDollarExecutor() {
        getRuntime().addShutdownHook(new Thread(this::forceStop));
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

    @NotNull @Override public DollarExecutor copy() {
        return this;
    }

    @Override public int priority() {
        return 0;
    }

    @NotNull @Override public <T> Future<T> executeInBackground(@NotNull Callable<T> callable) {
        return backgroundExecutor.submit(callable);
    }

    @NotNull @Override public <T> Future<T> executeNow(@NotNull Callable<T> callable) {
        final FutureTask<T> tFutureTask = new FutureTask<>(callable);
        tFutureTask.run();
        return tFutureTask;
    }

    @Override public void forceStop() {
        backgroundExecutor.shutdownNow();
        forkJoinPool.shutdownNow();
        scheduledExecutor.shutdownNow();
    }

    @Override public void restart() {
        stop();
        start();
    }

    @NotNull @Override public Future<?> scheduleEvery(long millis, @NotNull Runnable runnable) {
        return scheduledExecutor.scheduleAtFixedRate(runnable, millis, millis, TimeUnit.MILLISECONDS);
    }

    @NotNull @Override public <T> Future<T> submit(@NotNull Callable<T> callable) {
        return forkJoinPool.submit(callable);
    }
}
