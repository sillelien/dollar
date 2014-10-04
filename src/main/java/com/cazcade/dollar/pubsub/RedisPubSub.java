package com.cazcade.dollar.pubsub;

import com.cazcade.dollar.$;
import com.cazcade.dollar.DollarFuture;
import com.cazcade.dollar.DollarNull;
import com.cazcade.dollar.DollarStatic;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.function.Consumer;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class RedisPubSub implements DollarPubSub {


    private static final JedisPool jedisPool;
    private static final JedisPoolConfig poolConfig = new JedisPoolConfig();

    static {
        poolConfig.setMaxTotal(128);
        jedisPool = new JedisPool(poolConfig, System.getProperty("dollar.redis", "localhost"));
    }


    @Override
    public void pub($ value, String... locations) {
        for (String location : locations) {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(location, value.$$());
            }
        }
    }

    @Override
    public Sub sub(Consumer<$> action, String... locations) {
        RedisPubSubAdapter jedisPubSub = new RedisPubSubAdapter(action);
        DollarFuture future = DollarStatic.$fork(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(jedisPubSub, locations);
            }
            return DollarNull.INSTANCE;
        });
        jedisPubSub.setFuture(future);

        return jedisPubSub;
    }
}
