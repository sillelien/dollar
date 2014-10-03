package com.cazcade.dollar.store;

import com.cazcade.dollar.$;
import com.cazcade.dollar.DollarFactory;
import redis.clients.jedis.Jedis;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class RedisStore implements DollarStore {

    private Jedis jedis = new Jedis(System.getProperty("dollar.redis", "localhost"));

    @Override
    public $ get(String location) {
        return DollarFactory.fromValue(jedis.get(location));

    }

    @Override
    public $ pop(String location, int timeoutInMillis) {
        return DollarFactory.fromValue(jedis.brpop(timeoutInMillis / 1000, location).get(1));
    }

    @Override
    public void push(String location, $ value) {
        jedis.rpush(location, value.$$());
    }

    @Override
    public void set(String location, $ value) {
        jedis.set(location, value.$$());
    }

    @Override
    public void set(String location, $ value, int expiryInMilliseconds) {
        jedis.set(location, value.$$());
    }


}
