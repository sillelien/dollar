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

import com.google.common.collect.ImmutableList;
import me.neilellis.dollar.store.DollarStore;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
@Deprecated
public class RedisStore implements DollarStore {

    @NotNull
    private Jedis jedis = new Jedis(System.getProperty("dollar.redis", "localhost"));

    @Override
    public DollarStore copy() {
        return this;
    }

    @NotNull
    @Override
    public var get(String location) {
        String value;
        try {
            value = jedis.get(location);
        } catch (Exception e) {
            return DollarFactory.fromValue(ImmutableList.of(e), null);
        }
        return DollarFactory.fromValue(ImmutableList.of(), value);

    }

    @NotNull
    @Override
    public var pop(String location, int timeoutInMillis) {
        List<String> value;
        try {
            value = jedis.brpop(timeoutInMillis / 1000, location);
        } catch (Exception e) {
            return DollarFactory.fromValue(ImmutableList.of(e), null);
        }
        return DollarFactory.fromValue(ImmutableList.of(), value.get(1));
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
