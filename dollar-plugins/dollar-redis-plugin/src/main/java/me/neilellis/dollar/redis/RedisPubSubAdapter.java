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

package me.neilellis.dollar.redis;

import com.google.common.collect.ImmutableList;
import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.StateTracer;
import me.neilellis.dollar.pubsub.DollarPubSub;
import me.neilellis.dollar.pubsub.Sub;
import me.neilellis.dollar.types.DollarFactory;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
@Deprecated
public class RedisPubSubAdapter extends JedisPubSub implements Sub {
    private final DollarPubSub.SubAction action;
    @NotNull
    private final Semaphore lock = new Semaphore(0);
    private final Semaphore firstMessageLock = new Semaphore(0);

    public RedisPubSubAdapter(DollarPubSub.SubAction lambda) {
        this.action = lambda;

    }

    public void await(int seconds) throws InterruptedException {
        if (lock.tryAcquire(seconds, TimeUnit.SECONDS)) {
            lock.release();
        }
    }

    @Override
    public void await() throws InterruptedException {
        lock.acquire();
        lock.release();
    }

    @Override
    public void awaitFirst(int seconds) throws InterruptedException {
        if (lock.tryAcquire(seconds, TimeUnit.SECONDS)) {
            lock.release();
        }
        if (firstMessageLock.tryAcquire(seconds, TimeUnit.SECONDS)) {
            firstMessageLock.release();
        }
    }

    @Override
    public void cancel() {
        super.unsubscribe();
    }


    @Override
    public void onMessage(String channel, String message) {
        try {
            action.handle(DollarStatic.tracer().trace(null, DollarFactory.fromValue(message, ImmutableList.of()),
                    StateTracer.Operations.RECEIVE, channel), this);
        } catch (Exception e) {
            DollarStatic.handleError(e, null);
        } finally {
            firstMessageLock.release();
        }
    }

    @Override
    public void onPMessage(String s, String s1, String message) {
        try {
            action.handle(DollarStatic.tracer().trace(null, DollarFactory.fromValue(message, ImmutableList.of()),
                    StateTracer.Operations.RECEIVE, s, s1), this);
        } catch (Exception e) {
            DollarStatic.handleError(e, null);
        } finally {
            firstMessageLock.release();
        }
    }

    @Override
    public void onSubscribe(String s, int i) {
        lock.release();
    }

    @Override
    public void onUnsubscribe(String s, int i) {
        try {
            lock.release();
        } finally {
            firstMessageLock.release();
        }

    }

    @Override
    public void onPUnsubscribe(String s, int i) {
        try {
            lock.release();
        } finally {
            firstMessageLock.release();
        }
    }

    @Override
    public void onPSubscribe(String s, int i) {
        lock.release();
    }
}
