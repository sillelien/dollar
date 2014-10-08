package technology.neil.dollar.store;

import technology.neil.dollar.DollarFactory;
import technology.neil.dollar.var;
import redis.clients.jedis.Jedis;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class RedisStore implements DollarStore {

    private Jedis jedis = new Jedis(System.getProperty("dollar.redis", "localhost"));

    @Override
    public var get(String location) {
        return DollarFactory.fromValue(jedis.get(location));

    }

    @Override
    public var pop(String location, int timeoutInMillis) {
        return DollarFactory.fromValue(jedis.brpop(timeoutInMillis / 1000, location).get(1));
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
