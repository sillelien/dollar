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

package dollar.execution.simple;

import dollar.api.DollarStatic;
import dollar.api.Scope;
import dollar.api.Value;
import dollar.api.VarFlags;
import dollar.api.VarKey;
import dollar.api.execution.DollarExecutor;
import dollar.api.script.DollarParser;
import dollar.api.script.Source;
import dollar.api.types.DollarFactory;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static dollar.api.DollarStatic.getConfig;
import static dollar.internal.runtime.script.DollarUtilFactory.util;
import static dollar.internal.runtime.script.parser.Symbols.FORK;
import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.Executors.newFixedThreadPool;

public class ScopeAwareDollarExecutor implements DollarExecutor {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(ScopeAwareDollarExecutor.class);
    @Nullable
    private ExecutorService backgroundExecutor;
    @Nullable
    private ForkJoinPool forkJoinPool;
    @Nullable
    private ScheduledExecutorService scheduledExecutor;


    public ScopeAwareDollarExecutor() {
        getRuntime().addShutdownHook(new Thread(this::forceStop));
        start();
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
        if (getConfig().debugExecution()) {
            log.info("Background Execution");
        }
        return backgroundExecutor.submit(wrap(callable));
    }

    @NotNull
    @Override
    public <T> Future<T> executeNow(@NotNull Callable<T> callable) {
        if (getConfig().debugExecution()) {
            log.info("Immediate Execution");
        }
        CompletableFuture<T> future = new CompletableFuture<>();
        try {
            future.complete(callable.call());
        } catch (Exception e) {
            throw new DollarScriptException(e);
        }
        return future;
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

    @NotNull
    @Override
    public Value fork(@NotNull Source source, @NotNull Value in, @NotNull Function<Value, Value> call) {
        Future<Value> varFuture = submit(() -> call.apply(in));
        return util().node(FORK, false, DollarStatic.context().parser(), source,
                           Arrays.asList(in),
                           j -> {
                               log.debug("Waiting for future ...");
                               return varFuture.get();
                           }
        );
    }

    @NotNull
    @Override
    public Value forkAndReturnId(@NotNull Source source, @NotNull Value in, @NotNull Function<Value, Value> call) {
        Future<Value> varFuture = submit(() -> call.apply(in));
        VarKey id = VarKey.random();
        log.debug("Future obtained, returning future node");
        util().scope().set(id,
                           util().node(FORK, false, DollarStatic.context().parser(), source,
                                       Arrays.asList(in),
                                       j -> {
                                           log.debug("Waiting for future ...");
                                           return varFuture.get();
                                       }
                           ), null, null, new VarFlags(true, true, false, false, false, true));
        return DollarFactory.fromStringValue(id.asString());
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
        if (getConfig().debugExecution()) {
            log.info("Forking");
        }
        return forkJoinPool.submit(wrap(callable));
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

    private Runnable wrap(@NotNull Runnable runnable) {
        Scope scope = util().scope();
        DollarParser parser = DollarStatic.context().parser();
        return () -> util().inScope(true, scope, newScope -> {
            DollarStatic.context().parser(parser);
            runnable.run();
            return null;
        });
    }

    private <T> Callable<T> wrap(@NotNull Callable<T> callable) {
        Scope scope = util().scope();
        DollarParser parser = DollarStatic.context().parser();
        return () -> util().inScope(true, scope, newScope -> {
            DollarStatic.context().parser(parser);
            return callable.call();
        }).orElseThrow(() -> new AssertionError("callable.call() should not be null"));
    }
}
