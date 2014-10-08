package technology.neil.dollar.pubsub;

import technology.neil.dollar.DollarFactory;
import technology.neil.dollar.DollarFuture;
import technology.neil.dollar.var;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class RedisPubSubAdapter extends JedisPubSub implements Sub {
    private final Consumer<var> action;
    private DollarFuture future;
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
        lock.release();
    }

    @Override
    public void onUnsubscribe(String s, int i) {
        lock.release();

    }
}
