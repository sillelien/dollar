/*
 *    Copyright (c) 2014-2017 Neil Ellis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dollar.redis;

import dollar.api.DollarException;
import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.Value;
import dollar.api.uri.URI;
import dollar.api.uri.URIHandler;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.List;

public class RedisURIHandler implements URIHandler {
    private static final int BLOCKING_TIMEOUT = 10;
    @NotNull
    private final JedisPool jedisPool;
    @NotNull
    private final String path;
    @NotNull
    private final String query;
    private final int timeout = 60000;

    public RedisURIHandler(@NotNull URI uri, @NotNull JedisPoolConfig jedisPoolConfig) {
        URI uri1 = uri;
        String host = uri1.host();
        int port = uri1.port();
        String userInfo = uri1.userInfo();
        path = uri1.path().substring(1);
        query = uri1.queryString();
        if (userInfo != null) {
            String[] usernamePassword = userInfo.split(":");
            jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, usernamePassword[1]);
        } else {
            jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout);
        }
    }

    @NotNull
    @Override
    public Value all() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> result = jedis.lrange(path, 0, -1);
            return DollarStatic.$(result);
        }
    }

    @Override
    public void destroy() {
        //TODO
    }

    @NotNull
    @Override
    public Value drain() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> result = jedis.lrange(path, 0, -1);
            jedis.ltrim(path, 1, 0);
            return DollarStatic.$(result);
        }
    }

    @NotNull
    @Override
    public Value get(@NotNull Value key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return DollarStatic.$(jedis.hget(path, key.$S()));
        }
    }

    @Override
    public void init() {
        //TODO
    }

    @Override
    public void pause() {
        //TODO
    }

    @NotNull
    @Override
    public Value read(boolean blocking, boolean mutating) {
        if (blocking && !mutating) {
            return receive();
        } else if (!blocking && mutating) {
            return poll();
        } else if (!blocking && !mutating) {
            return peek();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @NotNull
    @Override
    public Value remove(@NotNull Value key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return DollarStatic.$(jedis.hdel(path, key.$S()));
        }
    }

    @NotNull
    @Override
    public Value removeValue(@NotNull Value v) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Value set(@NotNull Value key, @NotNull Value value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return DollarStatic.$(jedis.hset(path, key.$S(), value.$S()));
        }
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void start() {
        //TODO
    }

    @Override
    public void stop() {
        //TODO
    }

    @Override
    public void subscribe(@NotNull Pipeable consumer, @NotNull String id) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    try {
                        consumer.pipe(DollarStatic.$(message));
                    } catch (Exception e) {
                        throw new DollarException(e);
                    }
                }

                @Override
                public void onPMessage(String pattern, String channel, String message) {
                    try {
                        consumer.pipe(DollarStatic.$(message));
                    } catch (Exception e) {
                        throw new DollarException(e);
                    }
                }

                @Override
                public void onSubscribe(String channel, int subscribedChannels) {
                    //TODO
                }

                @Override
                public void onUnsubscribe(String channel, int subscribedChannels) {
                    //TODO
                }

                @Override
                public void onPUnsubscribe(String pattern, int subscribedChannels) {
                    //TODO
                }

                @Override
                public void onPSubscribe(String pattern, int subscribedChannels) {
                    //TODO
                }
            }, path);
        }
    }

    @Override
    public void unpause() {
        //TODO
    }

    @Override
    public void unsubscribe(@NotNull String subId) {
        //TODO
    }

    @NotNull
    @Override
    public Value write(@NotNull Value value, boolean blocking, boolean mutating) {
        return send(value);
    }

    @NotNull
    Value peek() {
        try (Jedis jedis = jedisPool.getResource()) {
            return DollarStatic.$(jedis.lindex(path, -1));
        }
    }

    @NotNull
    Value poll() {
        try (Jedis jedis = jedisPool.getResource()) {
            return DollarStatic.$(jedis.rpop(path));
        }
    }

    @NotNull
    Value receive() {
        try (Jedis jedis = jedisPool.getResource()) {
            return DollarStatic.$(jedis.brpop(BLOCKING_TIMEOUT, path).get(1));
        }
    }

    @NotNull Value send(@NotNull Value value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.lpush(path, value.$S());
        }
        return value;
    }
}
