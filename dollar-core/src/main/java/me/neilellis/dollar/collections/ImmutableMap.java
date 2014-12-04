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

package me.neilellis.dollar.collections;

import com.google.common.collect.Maps;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
@SuppressWarnings("SuspiciousMethodCalls") public class ImmutableMap<K extends Comparable<K>, V> implements
                                                                                                 Iterable<Map.Entry<K, V>> {

  private final Map<K, V> map = Maps.<K, V>newLinkedHashMap();

  public static <K extends Comparable<K>, V> ImmutableMap<K, V> of(K key, V value) {
    ImmutableMap<K, V> immutableMap = new ImmutableMap<>();
    immutableMap.map.put(key, value);
    return immutableMap;
  }

  public static <K extends Comparable<K>, V> ImmutableMap<K, V> copyOf(Map<K, V> m) {
    ImmutableMap<K, V> immutableMap = new ImmutableMap<>();
    immutableMap.map.putAll(m);
    return immutableMap;
  }

  public static ImmutableMap<String, var> of() {
    return new ImmutableMap<>();
  }

  public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    return map.compute(key, remappingFunction);
  }

  public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
    return map.computeIfAbsent(key, mappingFunction);
  }

  public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
    return map.computeIfPresent(key, remappingFunction);
  }

  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  public Set<Map.Entry<K, V>> entrySet() {
    return map.entrySet();
  }

  public void forEach(BiConsumer<? super K, ? super V> action) {
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
  public void forEach(Consumer<? super Map.Entry<K, V>> action) {
    map.entrySet().forEach(action);
  }

  @Override
  public Spliterator<Map.Entry<K, V>> spliterator() {
    return map.entrySet().spliterator();
  }

  public Set<K> keySet() {
    return map.keySet();
  }

  public Map<K, V> mutable() {
    return new LinkedHashMap<>(map);
  }

  public int size() {
    return map.size();
  }

  public Collection<V> values() {
    return map.values();
  }
}
