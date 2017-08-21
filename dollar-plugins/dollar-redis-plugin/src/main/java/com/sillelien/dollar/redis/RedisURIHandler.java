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

package com.sillelien.dollar.redis;

import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.uri.URI;
import com.sillelien.dollar.api.uri.URIHandler;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.List;

import static com.sillelien.dollar.api.DollarStatic.$;

public class RedisURIHandler implements URIHandler {
    private static final int BLOCKING_TIMEOUT = 10;
    @NotNull private final JedisPool jedisPool;
    private final int timeout = 60000;
    @NotNull private final String path;
    @NotNull private final String query;

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

    @NotNull @Override
    public var all() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> result = jedis.lrange(path, 0, -1);
            return $(result);
        }
    }

    @NotNull @Override
    public var write(@NotNull var value, boolean blocking, boolean mutating) {
        return send(value);
    }

    @Override public void destroy() {
        //TODO
    }

    @NotNull @Override
    public var drain() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> result = jedis.lrange(path, 0, -1);
            jedis.ltrim(path, 1, 0);
            return $(result);
        }
    }

    @NotNull
    @Override
    public var get(@NotNull var key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return $(jedis.hget(path, key.$S()));
        }
    }

    @Override public void init() {
        //TODO
    }

    @Override public void pause() {
        //TODO
    }

    @NotNull
    @Override
    public var read(boolean blocking, boolean mutating) {
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
    public var remove(@NotNull var key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return $(jedis.hdel(path, key.$S()));
        }
    }

    @NotNull @Override
    public var removeValue(@NotNull var v) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public var set(@NotNull var key, @NotNull var value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return $(jedis.hset(path, key.$S(), value.$S()));
        }
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override public void start() {
        //TODO
    }

    @Override public void stop() {
        //TODO
    }

    @Override
    public void subscribe(@NotNull Pipeable consumer, @NotNull String id) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    try {
                        consumer.pipe($(message));
                    } catch (Exception e) {
                        DollarStatic.logAndRethrow(e);
                    }
                }

                @Override
                public void onPMessage(String pattern, String channel, String message) {
                    try {
                        consumer.pipe($(message));
                    } catch (Exception e) {
                        DollarStatic.logAndRethrow(e);
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

    @Override public void unpause() {
        //TODO
    }

    @Override
    public void unsubscribe(@NotNull String subId) {
        //TODO
    }

    @NotNull
    var receive() {
        try (Jedis jedis = jedisPool.getResource()) {
            return $(jedis.brpop(BLOCKING_TIMEOUT, path).get(1));
        }
    }

    @NotNull
    var poll() {
        try (Jedis jedis = jedisPool.getResource()) {
            return $(jedis.rpop(path));
        }
    }

    @NotNull
    var peek() {
        try (Jedis jedis = jedisPool.getResource()) {
            return $(jedis.lindex(path, -1));
        }
    }

    @NotNull var send(@NotNull var value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.lpush(path, value.$S());
        }
        return value;
    }
}
