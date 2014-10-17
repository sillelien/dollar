package me.neilellis.dollar.pubsub;

import me.neilellis.dollar.DollarFuture;
import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.types.DollarVoid;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.function.Consumer;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class RedisPubSub implements DollarPubSub {


    @NotNull
    private static final JedisPool jedisPool;
    private static final JedisPoolConfig poolConfig = new JedisPoolConfig();

    static {
        poolConfig.setMaxTotal(128);
        jedisPool = new JedisPool(poolConfig, System.getProperty("dollar.redis", "localhost"));
    }


    @Override
    public void pub(@NotNull var value, @NotNull String... locations) {
        for (String location : locations) {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(location, value.S());
            }
        }
    }

    @NotNull
    @Override
    public Sub sub(Consumer<var> action, String... locations) {
        try {

            RedisPubSubAdapter jedisPubSub = new RedisPubSubAdapter(action);
            DollarFuture future = DollarStatic.$fork(() -> {
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.subscribe(jedisPubSub, locations);
                }
                return DollarVoid.INSTANCE;
            });
            jedisPubSub.setFuture(future);

            return jedisPubSub;
        } catch (Exception e) {
            return DollarStatic.logAndRethrow(e);
        }
    }
}
