package me.neilellis.dollar.store;

import me.neilellis.dollar.DollarFactory;
import me.neilellis.dollar.var;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class RedisStore implements DollarStore {

    private Jedis jedis = new Jedis(System.getProperty("dollar.redis", "localhost"));

    @Override
    public var get(String location) {
        String value;
        try {
            value = jedis.get(location);
        } catch (Exception e) {
            return DollarFactory.fromValue(Collections.singletonList(e), null);
        }
        return DollarFactory.fromValue(Collections.emptyList(),value);

    }

    @Override
    public var pop(String location, int timeoutInMillis) {
        List<String> value;
        try {
            value = jedis.brpop(timeoutInMillis / 1000, location);
        } catch (Exception e) {
            return DollarFactory.fromValue(Collections.singletonList(e), null);
        }
        return DollarFactory.fromValue(Collections.emptyList(), value.get(1));
    }

    @Override
    public void push(String location, var value) {
        jedis.rpush(location, value.$$());
    }

    @Override
    public void set(String location, var value) {
        jedis.set(location, value.$$());
    }

    @Override
    public void set(String location, var value, int expiryInMilliseconds) {
        jedis.set(location, value.$$());
    }


}
