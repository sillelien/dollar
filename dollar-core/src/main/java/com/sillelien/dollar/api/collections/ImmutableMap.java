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

package com.sillelien.dollar.api.collections;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("SuspiciousMethodCalls") public final class ImmutableMap<K extends Comparable<K>, V> implements
                                                                                                       Iterable<Map
                                                                                                               .Entry<K, V>> {

    private final Map<K, V> map = new LinkedHashMap<>();

    @NotNull public static <K extends Comparable<K>, V> ImmutableMap<K, V> of(K key, V value) {
        ImmutableMap<K, V> immutableMap = new ImmutableMap<>();
        immutableMap.map.put(key, value);
        return immutableMap;
    }

    @NotNull public static <K extends Comparable<K>, V> ImmutableMap<K, V> copyOf(@NotNull Map<K, V> m) {
        ImmutableMap<K, V> immutableMap = new ImmutableMap<>();
        immutableMap.map.putAll(m);
        return immutableMap;
    }

    @NotNull public static <K extends Comparable<K>, V> ImmutableMap<K, V> of() {
        return new ImmutableMap<>();
    }

    public V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map.compute(key, remappingFunction);
    }

    public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
        return map.computeIfAbsent(key, mappingFunction);
    }

    public V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return map.computeIfPresent(key, remappingFunction);
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @NotNull public Set<Map.Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    public void forEach(@NotNull BiConsumer<? super K, ? super V> action) {
        map.forEach(action);
    }

    public V get(Object key) {
        return map.get(key);
    }

    public V getOrDefault(Object key, V defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass") @Override
    public boolean equals(Object o) {
        return map.equals(o);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return map.entrySet().iterator();
    }

    @Override
    public void forEach(@NotNull Consumer<? super Map.Entry<K, V>> action) {
        map.entrySet().forEach(action);
    }



    @Override
    public Spliterator<Map.Entry<K, V>> spliterator() {
        return map.entrySet().spliterator();
    }

    @NotNull public Set<K> keySet() {
        return map.keySet();
    }

    @NotNull public Map<K, V> mutable() {
        return new LinkedHashMap<>(map);
    }

    public int size() {
        return map.size();
    }

    @NotNull public Collection<V> values() {
        return map.values();
    }
}
