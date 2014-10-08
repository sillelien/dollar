package technology.neil.dollar.pubsub;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface Sub {
    void await() throws InterruptedException;

    void cancel();
}
