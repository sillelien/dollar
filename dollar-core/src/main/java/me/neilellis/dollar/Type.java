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

package me.neilellis.dollar;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class Type {
    public static final ConcurrentHashMap<String, Type> lookup = new ConcurrentHashMap<>();
    public static final AtomicInteger nextOrdinal = new AtomicInteger(0);

    public static final Type STRING = new Type("STRING");
    public static final Type DECIMAL = new Type("DECIMAL");
    public static final Type INTEGER = new Type("INTEGER");
    public static final Type LIST = new Type("LIST");
    public static final Type MAP = new Type("MAP");
    public static final Type URI = new Type("URI");
    public static final Type VOID = new Type("VOID");
    public static final Type RANGE = new Type("RANGE");
    public static final Type BOOLEAN = new Type("BOOLEAN");
    public static final Type ERROR = new Type("ERROR");
    public static final Type DATE = new Type("DATE");
    public static final Type INFINITY = new Type("INFINITY");
    public static final Type ANY = new Type("ANY");


    private final String name;
    private final int ordinal;


    Type(String name) {

        this.name = name.toUpperCase();
        lookup.put(name, this);
        this.ordinal = nextOrdinal.getAndIncrement();
    }

    public static Type valueOf(String name) {
        return lookup.get(name.toUpperCase());
    }

    public static Type[] values() {
        return lookup.values().toArray(new Type[lookup.values().size()]);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        Type type = (Type) o;

        if (!name.equals(type.name)) { return false; }

        return true;
    }

    @Override public String toString() {
        return name;
    }

    public String name() {
        return name;
    }

    public int ordinal() {
        return ordinal;
    }
}
