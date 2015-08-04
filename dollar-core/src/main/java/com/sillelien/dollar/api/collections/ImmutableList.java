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
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public final class ImmutableList<V> implements Iterable<V> {

    private List<V> list = new ArrayList<>();


    public ImmutableList(List<V> list) {
        this.list = list;
    }

    @NotNull public static <V> ImmutableList<V> of(V... objects) {
        return new ImmutableList<>(Arrays.asList(objects));
    }

    @NotNull public static <V> ImmutableList<V> copyOf(@NotNull List<V>... lists) {
        List<V> list = new ArrayList<>();
        for (List<V> vList : lists) {
            list.addAll(vList);
        }
        return new ImmutableList<>(list);
    }

    @NotNull public static <V> ImmutableList<V> copyOf(@NotNull ImmutableList<V>... lists) {
        List<V> list = new ArrayList<>();
        for (ImmutableList<V> vList : lists) {
            list.addAll(vList.list);
        }
        return new ImmutableList<>(list);
    }

    public boolean contains(Object o) {return list.contains(o);}

    public boolean containsAll(@NotNull Collection<?> c) {return list.containsAll(c);}

    @NotNull public V get(int index) {return list.get(index);}

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ImmutableList that = (ImmutableList) o;

        return list.equals(that.list);

    }

    @Override public String toString() {
        return list.toString();
    }

    public int indexOf(Object o) {return list.indexOf(o);}

    public boolean isEmpty() {return list.isEmpty();}

    @NotNull public Iterator<V> iterator() {return list.iterator();}

    public void forEach(@NotNull Consumer<? super V> action) {list.forEach(action);}

    public Spliterator<V> spliterator() {return list.spliterator();}

    public int lastIndexOf(Object o) {return list.lastIndexOf(o);}

    @NotNull public ListIterator<V> listIterator() {return list.listIterator();}

    @NotNull public ListIterator<V> listIterator(int index) {return list.listIterator(index);}

    @NotNull public List<V> mutable() {
        return new ArrayList<>(list);
    }

    public Stream<V> parallelStream() {return list.parallelStream();}

    public int size() {return list.size();}

    public Stream<V> stream() {return list.stream();}

    @NotNull public List<V> subList(int fromIndex, int toIndex) {return list.subList(fromIndex, toIndex);}

    @NotNull public Object[] toArray() {return list.toArray();}

    @NotNull public <T> T[] toArray(@NotNull T[] a) {return list.toArray(a);}
}
