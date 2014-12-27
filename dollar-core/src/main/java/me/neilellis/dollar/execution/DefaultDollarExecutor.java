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

package me.neilellis.dollar.execution;

import java.util.concurrent.*;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
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

    @Override public DollarExecutor copy() {
        return this;
    }

    @Override public int priority() {
        return 0;
    }

    @Override public <T> Future<T> executeInBackground(Callable<T> callable) {
        return backgroundExecutor.submit(callable);
    }

    @Override public <T> Future<T> executeNow(Callable<T> callable) {
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

    @Override public Future<?> scheduleEvery(long millis, Runnable runnable) {
        return scheduledExecutor.scheduleAtFixedRate(runnable, millis, millis, TimeUnit.MILLISECONDS);
    }

    @Override public <T> Future<T> submit(Callable<T> callable) {
        return forkJoinPool.submit(callable);
    }
}
