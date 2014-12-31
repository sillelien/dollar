/*
 * Copyright (c) 2014-2015 Neil Ellis
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

package me.neilellis.dollar.learner.hazelcast;

import com.hazelcast.core.MapLoader;
import com.hazelcast.core.MapStore;
import com.thoughtworks.xstream.XStream;
import me.neilellis.dollar.execution.DollarExecutor;
import me.neilellis.dollar.plugin.Plugins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileMapStore<K, V> implements MapStore<K, V>, MapLoader<K, V> {

    @Nullable private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);

    @NotNull private ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();

    {
        File file = new File(System.getProperty("user.home") + "/.dollar/typelearning.xml");
        file.getParentFile().mkdirs();
        executor.scheduleEvery(1000, () -> {
            try (FileOutputStream out = new FileOutputStream(file)) {
                new XStream().toXML(map, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        if (file.exists()) {
            map = (ConcurrentHashMap<K, V>) new XStream().fromXML(file);
        }
    }


    @NotNull @Override public V load(@NotNull K key) {
        return map.get(key.toString());
    }

    @NotNull @Override public Map<K, V> loadAll(@NotNull Collection<K> keys) {
        final HashMap<K, V> hashMap = new HashMap<>();

        for (K key : keys) {
            hashMap.put(key, map.get(key.toString()));
        }

        return hashMap;
    }

    @NotNull @Override public Set<K> loadAllKeys() {
        return new HashSet<>(Arrays.asList((K[]) map.keySet().toArray()));
    }

    @Override public void store(@NotNull K key, @NotNull V value) {
        map.put(key, value);
    }

    @Override public void storeAll(@NotNull Map<K, V> map) {

        for (Map.Entry<K, V> entry : map.entrySet()) {
            this.map.put(entry.getKey(), entry.getValue());
        }

    }

    @Override public void delete(@NotNull K key) {
        map.remove(key);
    }

    @Override public void deleteAll(@NotNull Collection<K> keys) {

        for (K key : keys) {
            map.remove(key);
        }
    }
}
