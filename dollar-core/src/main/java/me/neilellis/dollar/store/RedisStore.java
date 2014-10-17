package me.neilellis.dollar.store;

import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class RedisStore implements DollarStore {

    @NotNull
    private Jedis jedis = new Jedis(System.getProperty("dollar.redis", "localhost"));

    @NotNull
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

    @NotNull
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
    public void push(String location, @NotNull var value) {
        jedis.rpush(location, value.S());
    }

    @Override
    public void set(String location, @NotNull var value) {
        jedis.set(location, value.S());
    }

    @Override
    public void set(String location, @NotNull var value, int expiryInMilliseconds) {
        jedis.set(location, value.S());
    }


}
