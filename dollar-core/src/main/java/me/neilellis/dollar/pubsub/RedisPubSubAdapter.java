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

package me.neilellis.dollar.pubsub;

import me.neilellis.dollar.DollarFuture;
import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPubSub;

import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class RedisPubSubAdapter extends JedisPubSub implements Sub {
    private final Consumer<var> action;
    private DollarFuture future;
    @NotNull
    private Semaphore lock = new Semaphore(1);

    public RedisPubSubAdapter(Consumer<var> lambda) {
        this.action = lambda;
        try {
            lock.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void await() throws InterruptedException {
        lock.acquire();
        lock.release();
    }

    @Override
    public void cancel() {
        System.out.println("Cancelled");
        future.future().cancel(true);
        super.unsubscribe();
    }

    public DollarFuture getFuture() {
        return future;
    }

    public void setFuture(DollarFuture future) {
        this.future = future;
    }

    @Override
    public void onMessage(String channel, String message) {
        try {
            action.accept(DollarFactory.fromValue(Collections.<Throwable>emptyList(),message));
        } catch (Exception e) {
            DollarStatic.handleError(e);
        }
    }

    @Override
    public void onPMessage(String s, String s1, String message) {
        try {
            action.accept(DollarFactory.fromValue(Collections.emptyList(),message));
        } catch (Exception e) {
            DollarStatic.handleError(e);
        }
    }

    @Override
    public void onSubscribe(String s, int i) {
        lock.release();
    }

    @Override
    public void onUnsubscribe(String s, int i) {
        lock.release();

    }

    @Override
    public void onPUnsubscribe(String s, int i) {
        //TODO
    }

    @Override
    public void onPSubscribe(String s, int i) {
        //TODO
    }
}
