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

package dollar.learner.simple;

import dollar.api.Type;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TypeScoreMap {
    private static final long TTL = 24 * 60 * 60 * 1000;

    @NotNull ConcurrentHashMap<String, AtomicLong> map = new ConcurrentHashMap<>();
    private long lastAccess;

    public TypeScoreMap(@NotNull String key) {

    }

    @NotNull
    public AtomicLong getOrDefault(@NotNull Type key) {
        lastAccess = System.currentTimeMillis();
        return map.getOrDefault(key.toString(), new AtomicLong());
    }

    public void increment(@NotNull Type type) {
        lastAccess = System.currentTimeMillis();
        AtomicLong count = getOrDefault(type);
        count.incrementAndGet();
        map.put(type.toString(), count);
    }

    public boolean expired() {
        return lastAccess < (System.currentTimeMillis() - TTL);
    }


    @NotNull
    public Map<String, AtomicLong> map() {
        return map;
    }
}
