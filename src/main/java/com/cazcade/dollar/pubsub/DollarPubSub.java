package com.cazcade.dollar.pubsub;

import com.cazcade.dollar.$;

import java.util.function.Consumer;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface DollarPubSub {

    void pub($ value, String... locations);

    Sub sub(Consumer<$> lambda, String... locations);
}
