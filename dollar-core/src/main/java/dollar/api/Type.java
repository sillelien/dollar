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

package dollar.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class Type {


    /**
     * The constant ANY.
     */
    @NotNull
    public static final Type _ANY = new Type("Any");
    @NotNull
    public static final Type _BLOCK = new Type("Block");
    /**
     * The constant BOOLEAN.
     */
    @NotNull
    public static final Type _BOOLEAN = new Type("Boolean");
    /**
     * The constant DATE.
     */
    @NotNull
    public static final Type _DATE = new Type("Date");
    /**
     * The constant DECIMAL.
     */
    @NotNull
    public static final Type _DECIMAL = new Type("Decimal");
    /**
     * The constant ERROR.
     */
    @NotNull
    public static final Type _ERROR = new Type("Error");
    /**
     * The constant INFINITY.
     */
    @NotNull
    public static final Type _INFINITY = new Type("Infinity");
    /**
     * The constant INTEGER.
     */
    @NotNull
    public static final Type _INTEGER = new Type("Integer");
    /**
     * The constant LIST.
     */
    @NotNull
    public static final Type _LIST = new Type("List");
    /**
     * The constant MAP.
     */
    @NotNull
    public static final Type _MAP = new Type("Map");
    @NotNull
    public static final Type _QUEUE = new Type("Queue");
    /**
     * The constant RANGE.
     */
    @NotNull
    public static final Type _RANGE = new Type("Range");
    /**
     * The constant STRING.
     */
    @NotNull
    public static final Type _SEQUENCE = new Type("Sequence");
    /**
     * The constant STRING.
     */
    @NotNull
    public static final Type _STRING = new Type("String");
    /**
     * The constant URI.
     */
    @NotNull
    public static final Type _URI = new Type("URI");
    /**
     * The constant VOID.
     */
    @NotNull
    public static final Type _VOID = new Type("Void");
    @NotNull
    private final String constraint;
    @NotNull
    private final String name;


    /**
     * Instantiates a new Type.
     *
     * @param name the name
     */
    public Type(@NotNull String name, @NotNull String constraint) {

        this.name = name;
        if (constraint != null) {
            this.constraint = constraint;
        } else {
            this.constraint = "";
        }
    }

    /**
     * Instantiates a new Type.
     */
    public Type(@NotNull Type type, @Nullable String constraint) {

        name = type.name;
        if (constraint != null) {
            this.constraint = constraint;
        } else {
            this.constraint = "";
        }
    }

    /**
     * Instantiates a new Type.
     *
     * @param name the name
     */
    public Type(@NotNull String name) {

        this.name = name
        ;
        constraint = "";
    }

    public Type(@NotNull Type type, @Nullable SubType subType) {
        name = type.name;
        if (subType != null) {
            constraint = subType.asString();
        } else {
            constraint = "";
        }
    }

    /**
     * Create a Type from a string
     *
     * @param name the name of the Type
     * @return the Type
     */
    @NotNull
    public static Type of(@NotNull String name) {
        String[] split = name.split(":");
        if (split.length == 2) {
            return new Type(split[0], split[1]);
        } else {
            return new Type(name);
        }
    }

    /**
     * Create a Type from a Value
     *
     * @param name the name of the Type
     * @return the Type
     */
    @NotNull
    public static Type of(@NotNull Value name) {
        String[] split = name.$S().split(":");
        if (split.length == 2) {
            return new Type(split[0], split[1]);
        } else {
            return new Type(name.$S());
        }
    }

    public boolean canBe(@NotNull Type type) {
        return type.name.equals(name);
    }

    /**
     * Return the constraint portion of the Type
     *
     * @return the constraint
     */
    @Contract(pure = true)
    @NotNull
    public String constraint() {
        return constraint;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;
        Type type = (Type) o;
        return Objects.equals(name, type.name) &&
                       Objects.equals(constraint, type.constraint);
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public String toString() {
        return constraint.isEmpty() ? name : (name + ":" + constraint);
    }

    public boolean is(@Nullable Type t) {
        return name.equals((t != null) ? t.name : null);

    }

    /**
     * Name string.
     *
     * @return the string
     */
    @Contract(pure = true)
    @NotNull
    public String name() {
        return name;
    }
}
