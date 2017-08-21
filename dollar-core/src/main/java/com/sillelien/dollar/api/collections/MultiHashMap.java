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

package com.sillelien.dollar.api.collections;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class MultiHashMap<K, V> extends ConcurrentHashMap<K, Collection<V>> implements MultiMap<K,
                                                                                                       V> {

    private static final long serialVersionUID = 1943563828307035349L;
    // backed values collection
    @NotNull
    private final transient Collection<V> values = new Values();

    /**
     * Constructor.
     */
    public MultiHashMap() {
        super();
    }

    /**
     * Constructor.
     *
     * @param initialCapacity the initial map capacity
     */
    public MultiHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Constructor.
     *
     * @param initialCapacity the initial map capacity
     * @param loadFactor      the amount 0.0-1.0 at which to resize the map
     */
    public MultiHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Constructor that copies the input map creating an independent copy. <p> This method performs different behaviour
     * depending on whether the map specified is a MultiMap or not. If a MultiMap is specified, each internal collection
     * is also cloned. If the specified map only implements Map, then the values are not cloned. <p> NOTE: From Commons
     * Collections 3.1 this method correctly copies a MultiMap to form a truly independent new map.
     *
     * @param mapToCopy a Map to copy
     */
    public MultiHashMap(@NotNull Map<K, Collection<V>> mapToCopy) {
        // be careful of JDK 1.3 vs 1.4 differences
        super((int) (mapToCopy.size() * 1.4f));
        if (mapToCopy instanceof MultiMap) {
            for (Entry<K, Collection<V>> entry : mapToCopy.entrySet()) {
                Collection<V> coll = entry.getValue();
                Collection<V> newColl = createCollection(coll);
                super.put(entry.getKey(), newColl);
            }
        } else {
            putAll(mapToCopy);
        }
    }

    /**
     * Creates a new instance of the map value Collection container. <p> This method can be overridden to use your own
     * collection type.
     *
     * @param coll the collection to copy, may be null
     * @return the new collection
     */
    @NotNull
    protected Collection<V> createCollection(@Nullable Collection<V> coll) {
        if (coll == null) {
            return new ArrayList<>();
        } else {
            return new ArrayList<>(coll);
        }
    }

    //-----------------------------------------------------------------------

    @Override
    @NotNull
    public Collection<V> allValues() {
        return values;
    }

    @Override
    public boolean containsValue(@NotNull Object key, @NotNull Object value) {
        Collection coll = getCollection(key);
        return coll.contains(value);
    }

    @NotNull
    @Override
    public Iterable<? extends Entry<K, Collection<V>>> entries() {
        return entrySet();
    }

    @NotNull
    @Override
    public Collection<V> getCollection(@NotNull Object key) {
        return getOrDefault(key, new ArrayList<>());
    }

    @Nullable
    @Override
    public Iterator<V> iterator(@NotNull Object key) {
        Collection coll = getCollection(key);
        return coll.iterator();
    }

    @Override
    public boolean putAll(@NotNull Object key, @Nullable Collection<V> values) {
        if ((values == null) || values.isEmpty()) {
            return false;
        }
        Collection coll = getCollection(key);
        if (coll.isEmpty()) {
            coll = createCollection(values);
            if (coll.isEmpty()) {
                return false;
            }
            super.put((K) key, coll);
            return true;
        } else {
            return coll.addAll(values);
        }
    }

    @Nullable
    public V putValue(@NotNull K key, @NotNull V value) {
        // NOTE:: putValue is called during deserialization in JDK < 1.4 !!!!!!
        //        so we must have a readObject()
        Collection<V> coll = getCollection(key);
        if (coll.isEmpty()) {
            super.put(key, coll);
        }
        boolean results = coll.add(value);
        return (results ? value : null);
    }

    @Override
    public int size(@NotNull Object key) {
        Collection coll = getCollection(key);
        return coll.size();
    }

    @Override
    public void clear() {
        // For gc, clear each list in the map
        Set pairs = super.entrySet();
        for (Object pair : pairs) {
            Entry keyValuePair = (Entry) pair;
            Collection coll = (Collection) keyValuePair.getValue();
            coll.clear();
        }
        super.clear();
    }

    @Override
    public boolean containsValue(@NotNull Object value) {
        Set pairs = super.entrySet();

        for (Object pair : pairs) {
            Entry keyValuePair = (Entry) pair;
            Collection coll = (Collection) keyValuePair.getValue();
            if (coll.contains(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean remove(Object key, Object item) {
        Collection valuesForKey = getCollection(key);
        valuesForKey.remove(item);

        // remove the list if it is now empty
        // (saves space, and allows is to work)
        if (valuesForKey.isEmpty()) {
            remove(key);
        }
        return true;
    }

    /**
     * Clones the map creating an independent copy. <p> The clone will shallow clone the collections as well as the
     * map.
     *
     * @return the cloned map
     */
    @NotNull
    public Object clone() throws CloneNotSupportedException {
        MultiHashMap cloned = (MultiHashMap) super.clone();

        // clone each Collection container
        for (Object o : cloned.entrySet()) {
            Entry entry = (Entry) o;
            Collection coll = (Collection) entry.getValue();
            Collection newColl = createCollection(coll);
            entry.setValue(newColl);
        }
        return cloned;
    }

    //-----------------------------------------------------------------------

    /**
     * Gets the total size of the map by counting all the values.
     *
     * @return the total size of the map counting all values
     * @since Commons Collections 3.1
     */
    public int totalSize() {
        int total = 0;
        Collection values = super.values();
        for (Object value : values) {
            Collection coll = (Collection) value;
            total += coll.size();
        }
        return total;
    }

    /**
     * Inner class to view the elements.
     */
    private class Values extends AbstractCollection<V> {

        @NotNull
        public Iterator<V> iterator() {
            return new ValueIterator();
        }

        public int size() {
            int compt = 0;
            for (Object o : this) {
                compt++;
            }
            return compt;
        }

        public void clear() {
            MultiHashMap.this.clear();
        }

    }

    //-----------------------------------------------------------------------

    /**
     * Inner iterator to view the elements.
     */
    private final class ValueIterator implements Iterator<V> {
        @NotNull
        private final Iterator<Collection<V>> backedIterator;
        @NotNull
        private Iterator<V> tempIterator;

        private ValueIterator() {
            backedIterator = MultiHashMap.super.values().iterator();
        }

        public boolean hasNext() {
            return searchNextIterator();
        }

        private boolean searchNextIterator() {
            while ((tempIterator == null) || !tempIterator.hasNext()) {
                if (!backedIterator.hasNext()) {
                    return false;
                }
                tempIterator = backedIterator.next().iterator();
            }
            return true;
        }

        @NotNull
        public V next() {
            if (!searchNextIterator()) {
                throw new NoSuchElementException();
            }
            return tempIterator.next();
        }

        public void remove() {
            if (tempIterator == null) {
                throw new IllegalStateException();
            }
            tempIterator.remove();
        }

    }

}
