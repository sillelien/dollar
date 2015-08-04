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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public interface MultiMap<K, V> extends Cloneable, Serializable {

    @NotNull Collection<V> allValues();

    /**
     * Clear the map. <p> This clears each collection in the map, and so may be slow.
     */
    void clear();

    boolean containsKey(Object key);

    /**
     * Checks whether the map contains the value specified. <p> This checks all collections against all keys for the
     * value, and thus could be slow.
     *
     * @param value the value to search for
     *
     * @return true if the map contains the value
     */
    boolean containsValue(Object value);

    /**
     * Checks whether the collection at the specified key contains the value.
     *
     * @param value the value to search for
     *
     * @return true if the map contains the value
     *
     * @since Commons Collections 3.1
     */
    boolean containsValue(Object key, Object value);

    Iterable<? extends Map.Entry<K, Collection<V>>> entries();

    /**
     * Gets the collection mapped to the specified key. This method is a convenience method to typecast the result of
     * {@code get(key)}.
     *
     * @param key the key to retrieve
     *
     * @return the collection mapped to the key, null if no mapping
     *
     * @since Commons Collections 3.1
     */
    @NotNull Collection<V> getCollection(K key);

    /**
     * Gets an iterator for the collection mapped to the specified key.
     *
     * @param key the key to get an iterator for
     *
     * @return the iterator of the collection at the key, empty iterator if key not in map
     *
     * @since Commons Collections 3.1
     */
    Iterator<V> iterator(Object key);

    /**
     * Adds a collection of values to the collection associated with the specified key.
     *
     * @param key    the key to store against
     * @param values the values to add to the collection at the key, null ignored
     *
     * @return true if this map changed
     *
     * @since Commons Collections 3.1
     */
    boolean putAll(Object key, Collection<V> values);

    /**
     * Adds the value to the collection associated with the specified key. <p> Unlike a normal {@code Map} the
     * previous value is not replaced. Instead the new value is added to the collection stored against the key.
     *
     * @param key   the key to store against
     * @param value the value to add to the collection at the key
     *
     * @return the value added if the map changed and null if the map did not change
     */
    V putValue(K key, V value);

    /**
     * Removes a specific value from map. <p> The item is removed from the collection mapped to the specified key. Other
     * values attached to that key are unaffected. <p> If the last value for a key is removed, {@code null} will be
     * returned from a subsequant {@code get(key)}.
     *
     * @param key  the key to remove from
     * @param item the value to remove
     *
     * @return the value removed (which was passed in), null if nothing removed
     */
    boolean remove(Object key, Object item);

    /**
     * Gets the size of the collection mapped to the specified key.
     *
     * @param key the key to get size for
     *
     * @return the size of the collection at the key, zero if key not in map
     *
     * @since Commons Collections 3.1
     */
    int size(Object key);

    /**
     * Gets a collection containing all the values in the map. <p> This returns a collection containing the combination
     * of values from all keys.
     *
     * @return a collection view of the values contained in this map
     */
    @NotNull Collection<Collection<V>> values();

}
