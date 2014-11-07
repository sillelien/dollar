/*
 * Copyright (c) 2014 Neil Ellis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.neilellis.dollar.redis;

import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.pubsub.DollarPubSub;
import me.neilellis.dollar.pubsub.Sub;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
@Deprecated
public class RedisPubSub implements DollarPubSub {


    @NotNull
    private static final JedisPool jedisPool;
    private static final JedisPoolConfig poolConfig = new JedisPoolConfig();

    static {
        poolConfig.setMaxTotal(128);
        jedisPool = new JedisPool(poolConfig, System.getProperty("dollar.redis", "localhost"));
    }

    @Override
    public DollarPubSub copy() {
        return this;
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
    public Sub sub(SubAction action, String... locations) {
        try {
            RedisPubSubAdapter jedisPubSub = new RedisPubSubAdapter(action);
            DollarStatic.$fork(() -> {
                try (Jedis jedis = jedisPool.getResource()) {
                    jedis.subscribe(jedisPubSub, locations);
                    return null;
                }
            });
            return jedisPubSub;
        } catch (Exception e) {
            return DollarStatic.logAndRethrow(e);
        }
    }
}
