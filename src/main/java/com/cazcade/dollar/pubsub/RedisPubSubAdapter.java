package com.cazcade.dollar.pubsub;

import com.cazcade.dollar.$;
import com.cazcade.dollar.DollarFactory;
import com.cazcade.dollar.DollarFuture;
import redis.clients.jedis.JedisPubSub;

import java.util.function.Consumer;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class RedisPubSubAdapter extends JedisPubSub implements Sub {
    private final Consumer<$> action;
    private DollarFuture future;

    public RedisPubSubAdapter(Consumer<$> lambda) {
        this.action = lambda;
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
        action.accept(DollarFactory.fromValue(message));
    }

    @Override
    public void onPMessage(String s, String s1, String message) {
        action.accept(DollarFactory.fromValue(message));
    }

    @Override
    public void onPSubscribe(String s, int i) {
        //TODO
    }

    @Override
    public void onPUnsubscribe(String s, int i) {
        //TODO
    }

    @Override
    public void onSubscribe(String s, int i) {
        //TODO
    }

    @Override
    public void onUnsubscribe(String s, int i) {
        //TODO
    }
}
