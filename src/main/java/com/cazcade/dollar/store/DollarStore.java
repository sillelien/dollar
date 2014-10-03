package com.cazcade.dollar.store;

import com.cazcade.dollar.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface DollarStore {

    $ get(String location);

    $ pop(String location, int timeoutInMillis);

    void push(String location, $ value);

    void set(String location, $ value);

    void set(String location, $ value, int expiryInMilliseconds);

}
