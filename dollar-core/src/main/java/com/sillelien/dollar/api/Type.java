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

package com.sillelien.dollar.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class Type {

    /**
     * The constant lookup.
     */
    public static final ConcurrentHashMap<String, Type> lookup = new ConcurrentHashMap<>();

    /**
     * The constant STRING.
     */
    public static final Type STRING = new Type("STRING");
    /**
     * The constant DECIMAL.
     */
    public static final Type DECIMAL = new Type("DECIMAL");
    /**
     * The constant INTEGER.
     */
    public static final Type INTEGER = new Type("INTEGER");
    /**
     * The constant LIST.
     */
    public static final Type LIST = new Type("LIST");
    /**
     * The constant MAP.
     */
    public static final Type MAP = new Type("MAP");
    /**
     * The constant URI.
     */
    public static final Type URI = new Type("URI");
    /**
     * The constant VOID.
     */
    public static final Type VOID = new Type("VOID");
    /**
     * The constant RANGE.
     */
    public static final Type RANGE = new Type("RANGE");
    /**
     * The constant BOOLEAN.
     */
    public static final Type BOOLEAN = new Type("BOOLEAN");
    /**
     * The constant ERROR.
     */
    public static final Type ERROR = new Type("ERROR");
    /**
     * The constant DATE.
     */
    public static final Type DATE = new Type("DATE");
    /**
     * The constant INFINITY.
     */
    public static final Type INFINITY = new Type("INFINITY");
    /**
     * The constant ANY.
     */
    public static final Type ANY = new Type("ANY");


    @NotNull private final String name;


    /**
     * Instantiates a new Type.
     *
     * @param name the name
     */
    Type(@NotNull String name) {

        this.name = name.toUpperCase();
        lookup.put(name.toUpperCase(), this);
    }

    /**
     * Create a Type from a string or null if it doesn't exist.
     *
     * @param name the name of the Type
     *
     * @return the Type
     */
    public static Type valueOf(@NotNull String name) {
        return lookup.get(name.toUpperCase());
    }

    /**
     * All the Types.
     *
     * @return an array of {@link Type}s
     */
    @NotNull public static Type[] values() {
        return lookup.values().toArray(new Type[lookup.values().size()]);
    }

    /**
     * All the names of the {@link Type}s
     *
     * @return a Set of all the names
     */
    public static Set<String> stringValues() {
        return lookup.values().stream().map(Type::toString).collect(Collectors.toSet());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        Type type = (Type) o;

        return name.equalsIgnoreCase(type.name);

    }

    @NotNull @Override public String toString() {
        return name;
    }

    /**
     * Name string.
     *
     * @return the string
     */
    @NotNull public String name() {
        return name;
    }

}
