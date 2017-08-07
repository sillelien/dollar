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

package com.sillelien.dollar.execution.simple;

import dollar.internal.runtime.script.Scope;
import com.sillelien.dollar.api.execution.DollarExecutor;
import dollar.internal.runtime.script.DollarScriptSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static dollar.internal.runtime.script.DollarScriptSupport.currentScope;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class ScopeAwareDollarExecutor implements DollarExecutor {

    @Nullable
    private ForkJoinPool forkJoinPool;
    @Nullable
    private ExecutorService backgroundExecutor;
    @Nullable
    private ScheduledExecutorService scheduledExecutor;


    public ScopeAwareDollarExecutor() {
        getRuntime().addShutdownHook(new Thread(this::forceStop));
        start();
    }

    @Override
    public void start() {
        forkJoinPool = new ForkJoinPool(getRuntime().availableProcessors() * 8);
        backgroundExecutor = newFixedThreadPool(getRuntime().availableProcessors());
        scheduledExecutor = Executors.newScheduledThreadPool(getRuntime().availableProcessors());
    }

    @Override
    public void stop() {
        if (backgroundExecutor != null) {
            backgroundExecutor.shutdown();
            backgroundExecutor = null;
        }
        if (forkJoinPool != null) {
            forkJoinPool.shutdown();
            forkJoinPool = null;
        }
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
            scheduledExecutor = null;
        }
    }

    @NotNull
    @Override
    public DollarExecutor copy() {
        return this;
    }

    @NotNull
    @Override
    public <T> Future<T> executeInBackground(@NotNull Callable<T> callable) {
        assert backgroundExecutor != null;
        return backgroundExecutor.submit(wrap(callable));
    }

    @NotNull
    @Override
    public <T> Future<T> executeNow(@NotNull Callable<T> callable) {
        final FutureTask<T> tFutureTask = new FutureTask<>(wrap(callable));
        tFutureTask.run();
        return tFutureTask;
    }

    @Override
    public void forceStop() {
        if (backgroundExecutor != null) {
            backgroundExecutor.shutdownNow();
            backgroundExecutor = null;
        }
        if (forkJoinPool != null) {
            forkJoinPool.shutdownNow();
            forkJoinPool = null;
        }
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdownNow();
            scheduledExecutor = null;
        }
    }

    @Override
    public void restart() {
        stop();
        start();
    }

    @NotNull
    @Override
    public Future<?> scheduleEvery(long millis, @NotNull Runnable runnable) {
        assert scheduledExecutor != null;
        return scheduledExecutor.scheduleAtFixedRate(wrap(runnable), millis, millis, TimeUnit.MILLISECONDS);
    }

    @NotNull
    @Override
    public <T> Future<T> submit(@NotNull Callable<T> callable) {
        assert forkJoinPool != null;
        return forkJoinPool.submit(wrap(callable));
    }

    private Runnable wrap(@NotNull Runnable runnable) {
        Scope scope = currentScope();
        return () -> DollarScriptSupport.inScope(true, scope, newScope -> {
            runnable.run();
            return null;
        });
    }

    private <T> Callable<T> wrap(@NotNull Callable<T> callable) {
        Scope scope = currentScope();
        return () -> DollarScriptSupport.inScope(true, scope, newScope -> callable.call());
    }
}
