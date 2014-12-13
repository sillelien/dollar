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

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public final class ImmutableList<V> implements Iterable<V> {

    private List<V> list = new ArrayList<>();


    public ImmutableList(List<V> list) {
        this.list = list;
    }

    public static <V> ImmutableList<V> of(V... objects) {
        return new ImmutableList<>(Arrays.asList(objects));
    }

    public static <V> ImmutableList<V> copyOf(List<V>... lists) {
        List<V> list = new ArrayList<>();
        for (List<V> vList : lists) {
            list.addAll(vList);
        }
        return new ImmutableList<>(list);
    }

    public static <V> ImmutableList<V> copyOf(ImmutableList<V>... lists) {
        List<V> list = new ArrayList<>();
        for (ImmutableList<V> vList : lists) {
            list.addAll(vList.list);
        }
        return new ImmutableList<>(list);
    }

    public boolean contains(Object o) {return list.contains(o);}

    public boolean containsAll(Collection<?> c) {return list.containsAll(c);}

    public V get(int index) {return list.get(index);}

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ImmutableList that = (ImmutableList) o;

        if (!list.equals(that.list)) { return false; }

        return true;
    }

    @Override public String toString() {
        return list.toString();
    }

    public int indexOf(Object o) {return list.indexOf(o);}

    public boolean isEmpty() {return list.isEmpty();}

    public Iterator<V> iterator() {return list.iterator();}

    public void forEach(Consumer<? super V> action) {list.forEach(action);}

    public Spliterator<V> spliterator() {return list.spliterator();}

    public int lastIndexOf(Object o) {return list.lastIndexOf(o);}

    public ListIterator<V> listIterator() {return list.listIterator();}

    public ListIterator<V> listIterator(int index) {return list.listIterator(index);}

    public List<V> mutable() {
        return new ArrayList<>(list);
    }

    public Stream<V> parallelStream() {return list.parallelStream();}

    public int size() {return list.size();}

    public Stream<V> stream() {return list.stream();}

    public List<V> subList(int fromIndex, int toIndex) {return list.subList(fromIndex, toIndex);}

    public Object[] toArray() {return list.toArray();}

    public <T> T[] toArray(T[] a) {return list.toArray(a);}
}
