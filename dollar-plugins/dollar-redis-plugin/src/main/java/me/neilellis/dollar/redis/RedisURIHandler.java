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

import me.neilellis.dollar.uri.URIHandler;
import me.neilellis.dollar.var;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Consumer;

import static me.neilellis.dollar.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class RedisURIHandler implements URIHandler {
    public static final int BLOCKING_TIMEOUT = 10;
    private final JedisPool jedisPool;
    private final int timeout = 60000;
    private final String path;
    private final String query;

    public RedisURIHandler(String uri, JedisPoolConfig jedisPoolConfig) throws URISyntaxException {
        URI uri1 = new URI(uri);
        String host = uri1.getHost();
        int port = uri1.getPort();
        String userInfo = uri1.getUserInfo();
        path = uri1.getPath();
        query = uri1.getQuery();
        if (userInfo != null) {
            String[] usernamePassword = userInfo.split(":");
            jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, usernamePassword[1]);
        } else {
            jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout);
        }
    }

    @Override
    public var dispatch(var value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.lpush(path, value.$S());
        }
        return value;
    }

    @Override
    public void subscribe(Consumer<var> consumer) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    consumer.accept($(message));
                }

                @Override
                public void onPMessage(String pattern, String channel, String message) {
                    consumer.accept($(message));
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
    public var poll() {
        try (Jedis jedis = jedisPool.getResource()) {
            return $(jedis.rpop(path));
        }
    }

    @Override
    public var receive() {
        try (Jedis jedis = jedisPool.getResource()) {
            return $(jedis.brpop(BLOCKING_TIMEOUT, path).get(1));
        }
    }

    @Override
    public var send(var value) {
        return dispatch(value);
    }

    @Override
    public var push(var value) {
        return dispatch(value);
    }

    @Override
    public var peek() {
        try (Jedis jedis = jedisPool.getResource()) {
            return $(jedis.lindex(path, -1));
        }
    }

    @Override
    public var set(var key, var value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return $(jedis.hset(path, key.$S(), value.$S()));
        }
    }

    @Override
    public var get(var key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return $(jedis.hget(path, key.$S()));
        }
    }

    @Override
    public var all() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> result = jedis.lrange(path, 0, -1);
            return $(result);
        }
    }

    @Override
    public var remove(var key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return $(jedis.hdel(path, key.$S()));
        }
    }

    @Override
    public var removeValue(var v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public var subscribe() {
        throw new UnsupportedOperationException();
    }

    @Override
    public var give(var value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var drain() {
        try (Jedis jedis = jedisPool.getResource()) {
            List<String> result = jedis.lrange(path, 0, -1);
            jedis.ltrim(path, 1, 0);
            return $(result);
        }
    }
}
