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

import com.google.common.base.Objects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Format: representational-type / logical-type : constraint-type
 * <p>
 * e.g. String/JSON:sizeLessThanOne
 */
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
    private final String constraintType;

    @Nullable
    private final String logicalType;

    @NotNull
    private final String representationalType;


    /**
     * Instantiates a new Type.
     *
     * @param name the name
     */
    public Type(@NotNull String name, @NotNull String constraint) {

        representationalType = name;
        if (constraint != null) {
            constraintType = constraint;
        } else {
            constraintType = "";
        }
        logicalType = null;
    }

    /**
     * Instantiates a new Type.
     */
    public Type(@NotNull Type type, @Nullable String constraint) {

        representationalType = type.representationalType;
        if (constraint != null) {
            constraintType = constraint;
        } else {
            constraintType = "";
        }
        logicalType = null;
    }

    /**
     * Instantiates a new Type.
     *
     * @param name the name
     */
    public Type(@NotNull String name) {

        representationalType = name
        ;
        constraintType = "";
        logicalType = null;
    }

    public Type(@NotNull Type type, @Nullable SubType subType) {
        representationalType = type.representationalType;
        if (subType != null) {
            constraintType = subType.asString();
        } else {
            constraintType = "";
        }
        logicalType = null;
    }

    public Type(@NotNull String rep, @Nullable String logical, @NotNull String constraint) {
        representationalType = rep;
        logicalType = logical;
        constraintType = constraint;
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
            String[] split2 = split[0].split("/");
            if (split2.length == 2) {
                return new Type(split2[0], split2[1], split[1]);
            } else {
                return new Type(split[0], split[1]);
            }
        } else {
            String[] split2 = split[0].split("/");
            if (split2.length == 2) {
                return new Type(split2[0], split2[1], "");
            } else {
                return new Type(name);
            }
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
        return Type.of(name.$S());
    }

    public static Type of(@NotNull Object o, @Nullable Object o1, @Nullable Object o2) {
        return new Type(o.toString(), (o1 != null) ? o1.toString() : null, (o2 != null) ? o2.toString() : "");
    }

    public boolean canBe(@NotNull Type type) {
        return type.representationalType.equals(representationalType);
    }

    /**
     * Return the constraint portion of the Type
     *
     * @return the constraint
     */
    @Contract(pure = true)
    @NotNull
    public String constraint() {
        return constraintType;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(constraintType, logicalType, representationalType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return Objects.equal(constraintType, type.constraintType) &&
                       Objects.equal(logicalType, type.logicalType) &&
                       Objects.equal(representationalType, type.representationalType);
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public String toString() {
        return constraintType.isEmpty() ? representationalType : (representationalType + ":" + constraintType);
    }

    public boolean is(@Nullable Type t) {
        return representationalType.equals((t != null) ? t.representationalType : null);

    }

    /**
     * Name string.
     *
     * @return the string
     */
    @Contract(pure = true)
    @NotNull
    public String name() {
        return representationalType;
    }
}
