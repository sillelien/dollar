package technology.neil.dollar.pubsub;

import technology.neil.dollar.var;

import java.util.function.Consumer;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface DollarPubSub {

    void pub(var value, String... locations);

    Sub sub(Consumer<var> lambda, String... locations);
}
