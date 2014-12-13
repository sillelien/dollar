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


import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;


/**
 * <code>MultiHashMap</code> is the default implementation of the {@link me.neilellis.dollar.collections.MultiMap
 * MultiMap} interface. <p> A <code>MultiMap</code> is a Map with slightly different semantics. Putting a value into the
 * map will add the value to a Collection at that key. Getting a value will return a Collection, holding all the values
 * put to that key. <p> This implementation uses an <code>ArrayList</code> as the collection. The internal storage list
 * is made available without cloning via the <code>get(Object)</code> and <code>entrySet()</code> methods. The
 * implementation returns <code>null</code> when there are no values mapped to a key. <p> For example:
 * <pre>
 * MultiMap mhm = new MultiHashMap();
 * mhm.put(key, "A");
 * mhm.put(key, "B");
 * mhm.put(key, "C");
 * List list = (List) mhm.get(key);</pre>
 * <p> <code>list</code> will be a list containing "A", "B", "C".
 *
 * @author Christopher Berry
 * @author James Strachan
 * @author Steve Downey
 * @author Stephen Colebourne
 * @author Julien Buret
 * @author Serhiy Yevtushenko
 * @version $Revision: 1.20 $ $Date: 2004/06/09 22:11:54 $
 * @since Commons Collections 2.0
 */
public class MultiHashMap extends HashMap implements MultiMap {

    // compatibility with commons-collection releases 2.0/2.1
    private static final long serialVersionUID = 1943563828307035349L;
    // backed values collection
    private transient Collection values = null;

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
    public MultiHashMap(Map mapToCopy) {
        // be careful of JDK 1.3 vs 1.4 differences
        super((int) (mapToCopy.size() * 1.4f));
        if (mapToCopy instanceof org.eclipse.jetty.util.MultiMap) {
            for (Iterator it = mapToCopy.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
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
    protected Collection createCollection(Collection coll) {
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

    @Override public Iterable<? extends Entry> entries() {
        return entrySet();
    }

    @Override public Collection getCollection(Object key) {
        return (Collection) get(key);
    }

    @Override public Iterator iterator(Object key) {
        Collection coll = getCollection(key);
        if (coll == null) {
            return new Iterator() {
                @Override public boolean hasNext() {
                    return false;
                }

                @Override public Object next() {
                    return null;
                }
            };
        }
        return coll.iterator();
    }

    @Override public boolean putAll(Object key, Collection values) {
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

    @Override public Object put(Object key, Object value) {
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
        Iterator pairsIterator = pairs.iterator();
        while (pairsIterator.hasNext()) {
            Map.Entry keyValuePair = (Map.Entry) pairsIterator.next();
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
        Iterator pairsIterator = pairs.iterator();
        while (pairsIterator.hasNext()) {
            Map.Entry keyValuePair = (Map.Entry) pairsIterator.next();
            Collection coll = (Collection) keyValuePair.getValue();
            if (coll.contains(value)) {
                return true;
            }
        }
        return false;
    }

    @Override public Collection values() {
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
    public Object clone() {
        MultiHashMap cloned = (MultiHashMap) super.clone();

        // clone each Collection container
        for (Iterator it = cloned.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
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
        for (Iterator it = values.iterator(); it.hasNext(); ) {
            Collection coll = (Collection) it.next();
            total += coll.size();
        }
        return total;
    }

    /**
     * Inner class to view the elements.
     */
    private class Values extends AbstractCollection {

        public Iterator iterator() {
            return new ValueIterator();
        }

        public int size() {
            int compt = 0;
            Iterator it = iterator();
            while (it.hasNext()) {
                it.next();
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
        private Iterator backedIterator;
        private Iterator tempIterator;

        private ValueIterator() {
            backedIterator = MultiHashMap.super.values().iterator();
        }

        private boolean searchNextIterator() {
            while (tempIterator == null || tempIterator.hasNext() == false) {
                if (backedIterator.hasNext() == false) {
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
    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
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
            for (Iterator iterator = entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry entry = (Map.Entry) iterator.next();
                // put has created a extra collection level, remove it
                super.put(entry.getKey(), ((Collection) entry.getValue()).iterator().next());
            }
        }
    }

}