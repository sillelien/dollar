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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Variable {
    private final boolean readonly;
    @Nullable
    private final var constraint;
    private final long thread;
    private final boolean pure;
    private final boolean fixed;
    @Nullable
    private final String constraintSource;
    private final boolean numeric;
    private boolean isVolatile;
    @NotNull
    private var value;

    public Variable(@NotNull var value, @Nullable var constraint, @Nullable String constraintSource, boolean numeric) {
        this.numeric = numeric;
        setValue(value);
        this.constraint = constraint;
        this.constraintSource = constraintSource;
        fixed = false;
        readonly = false;
        pure = false;
        thread = Thread.currentThread().getId();
    }

    public Variable(@NotNull var value,
                    boolean readonly,
                    @Nullable var constraint,
                    @Nullable String constraintSource,
                    boolean isVolatile,
                    boolean fixed, boolean pure, boolean numeric) {
        this.numeric = numeric;
        setValue(value);
        this.readonly = readonly;
        this.constraint = constraint;
        this.constraintSource = constraintSource;
        setVolatile(isVolatile);
        this.fixed = fixed;
        this.pure = pure;
        thread = Thread.currentThread().getId();
    }

    @Nullable
    public var getConstraint() {
        return constraint;
    }

    @Nullable
    public String getConstraintSource() {
        return constraintSource;
    }

    public long getThread() {
        return thread;
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) { return true; }
        if ((o == null) || (getClass() != o.getClass())) { return false; }

        Variable variable = (Variable) o;

        return getValue().equals(variable.getValue());

    }

    @NotNull
    public var getValue() {
        return value;
    }

    public void setValue(@NotNull var value) {
        this.value = value;
    }

    public boolean isPure() {
        return pure;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public boolean isVolatile() {
        return isVolatile;
    }

    public void setVolatile(boolean isVolatile) {
        this.isVolatile = isVolatile;
    }

    public boolean isNumeric() {
        return numeric;
    }


    public Variable copy(@NotNull var var) {
        return new Variable(var, readonly, constraint, constraintSource, isVolatile, fixed, pure, numeric);
    }
}
