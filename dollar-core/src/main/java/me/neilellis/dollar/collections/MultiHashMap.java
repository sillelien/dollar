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

package me.neilellis.dollar.collections;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;


public class MultiHashMap extends HashMap implements MultiMap {

    // compatibility with commons-collection releases 2.0/2.1
    private static final long serialVersionUID = 1943563828307035349L;
    // backed values collection
    @Nullable private transient Collection values = null;

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
    public MultiHashMap(@NotNull Map mapToCopy) {
        // be careful of JDK 1.3 vs 1.4 differences
        super((int) (mapToCopy.size() * 1.4f));
        if (mapToCopy instanceof MultiMap) {
            for (Object o : mapToCopy.entrySet()) {
                Entry entry = (Entry) o;
                Collection coll = (Collection) entry.getValue();
                Collection newColl = createCollection(coll);
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
     *
     * @return the new collection
     */
    @Nullable protected Collection createCollection(@Nullable Collection coll) {
        if (coll == null) {
            return new ArrayList();
        } else {
            return new ArrayList(coll);
        }
    }

    //-----------------------------------------------------------------------

    @Override public boolean containsValue(Object key, Object value) {
        Collection coll = getCollection(key);
        if (coll == null) {
            return false;
        }
        return coll.contains(value);
    }

    @NotNull @Override public Iterable<? extends Entry> entries() {
        return entrySet();
    }

    @NotNull @Override public Collection getCollection(Object key) {
        return (Collection) get(key);
    }

    @Nullable @Override public Iterator iterator(Object key) {
        Collection coll = getCollection(key);
        if (coll == null) {
            return new Iterator() {
                @Override public boolean hasNext() {
                    return false;
                }

                @Nullable @Override public Object next() {
                    return null;
                }
            };
        }
        return coll.iterator();
    }

    @Override public boolean putAll(Object key, @Nullable Collection values) {
        if (values == null || values.size() == 0) {
            return false;
        }
        Collection coll = getCollection(key);
        if (coll == null) {
            coll = createCollection(values);
            if (coll.size() == 0) {
                return false;
            }
            super.put(key, coll);
            return true;
        } else {
            return coll.addAll(values);
        }
    }

    @Override public int size(Object key) {
        Collection coll = getCollection(key);
        if (coll == null) {
            return 0;
        }
        return coll.size();
    }

    @Nullable @Override public Object put(Object key, Object value) {
        // NOTE:: put is called during deserialization in JDK < 1.4 !!!!!!
        //        so we must have a readObject()
        Collection coll = getCollection(key);
        if (coll == null) {
            coll = createCollection(null);
            super.put(key, coll);
        }
        boolean results = coll.add(value);
        return (results ? value : null);
    }

    @Override public void clear() {
        // For gc, clear each list in the map
        Set pairs = super.entrySet();
        for (Object pair : pairs) {
            Entry keyValuePair = (Entry) pair;
            Collection coll = (Collection) keyValuePair.getValue();
            coll.clear();
        }
        super.clear();
    }

    @Override public boolean containsValue(Object value) {
        Set pairs = super.entrySet();

        if (pairs == null) {
            return false;
        }
        for (Object pair : pairs) {
            Entry keyValuePair = (Entry) pair;
            Collection coll = (Collection) keyValuePair.getValue();
            if (coll.contains(value)) {
                return true;
            }
        }
        return false;
    }

    @NotNull @Override public Collection values() {
        Collection vs = values;
        return (vs != null ? vs : (values = new Values()));
    }

    @Override public boolean remove(Object key, Object item) {
        Collection valuesForKey = getCollection(key);
        if (valuesForKey == null) {
            return false;
        }
        valuesForKey.remove(item);

        // remove the list if it is now empty
        // (saves space, and allows equals to work)
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
    @NotNull public Object clone() {
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
     *
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
    private class Values extends AbstractCollection {

        @NotNull public Iterator iterator() {
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
    private class ValueIterator implements Iterator {
        private final Iterator backedIterator;
        private Iterator tempIterator;

        private ValueIterator() {
            backedIterator = MultiHashMap.super.values().iterator();
        }

        private boolean searchNextIterator() {
            while (tempIterator == null || !tempIterator.hasNext()) {
                if (!backedIterator.hasNext()) {
                    return false;
                }
                tempIterator = ((Collection) backedIterator.next()).iterator();
            }
            return true;
        }

        public boolean hasNext() {
            return searchNextIterator();
        }

        public Object next() {
            if (searchNextIterator() == false) {
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

    /**
     * Read the object during deserialization.
     */
    private void readObject(@NotNull ObjectInputStream s) throws IOException, ClassNotFoundException {
        // This method is needed because the 1.2/1.3 Java deserialisation called
        // put and thus messed up that method

        // default read object
        s.defaultReadObject();

        // problem only with jvm <1.4
        String version = "1.2";
        try {
            version = System.getProperty("java.version");
        } catch (SecurityException ex) {
            // ignore and treat as 1.2/1.3
        }

        if (version.startsWith("1.2") || version.startsWith("1.3")) {
            for (Object o : entrySet()) {
                Entry entry = (Entry) o;
                // put has created a extra collection level, remove it
                super.put(entry.getKey(), ((Collection) entry.getValue()).iterator().next());
            }
        }
    }

}