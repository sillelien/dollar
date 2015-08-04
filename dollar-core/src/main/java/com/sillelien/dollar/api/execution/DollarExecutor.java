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

import com.sillelien.dollar.api.plugin.ExtensionPoint;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface DollarExecutor extends ExtensionPoint<DollarExecutor> {


    /**
     * Execute job at some point, returns immediately.
     *
     * @param callable the job to perform
     * @param <T>      the return type
     *
     * @return a future for the result
     */
    <T> Future<T> executeInBackground(Callable<T> callable);

    /**
     * Submit a job for execution <b>immediately</b> in an unspecified manner.
     *
     * @param <T>      the return type
     * @param callable the job to perform
     *
     * @return the fork/join task
     */
    <T> Future<T> executeNow(Callable<T> callable);

    /**
     * Force stop, that is do not wait for tasks to finish.
     */
    void forceStop();

    /**
     * Stop the execution processing and restart it.
     */
    void restart();

    /**
     * Schedule a recurring job.
     *
     * @param millis   the time between executions
     * @param runnable the job to perform
     *
     * @return a Future for the task, to enable cancellation
     */
    Future<?> scheduleEvery(long millis, Runnable runnable);

    /**
     * Submit a job for execution in an unspecified manner.
     *
     * @param <T>      the return type
     * @param callable the job to perform
     *
     * @return the fork/join task
     */
    <T> Future<T> submit(Callable<T> callable);

}
