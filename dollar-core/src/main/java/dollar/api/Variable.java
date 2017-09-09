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
    @Nullable
    private final var constraint;
    @Nullable
    private final String constraintSource;
    private final boolean fixed;
    private final boolean numeric;
    private final boolean parameter;
    private final boolean pure;
    private final boolean readonly;
    private final long thread;
    private boolean isVolatile;
    @NotNull
    private var value;

    private Variable(@Nullable var constraint,
                     @Nullable String constraintSource,
                     boolean fixed,
                     boolean numeric,
                     boolean parameter,
                     boolean pure,
                     boolean readonly,
                     long thread,
                     boolean isVolatile,
                     @NotNull var value) {
        this.constraint = constraint;
        this.constraintSource = constraintSource;
        this.fixed = fixed;
        this.numeric = numeric;
        this.parameter = parameter;
        this.pure = pure;
        this.readonly = readonly;
        this.thread = thread;
        this.isVolatile = isVolatile;
        this.value = value;
    }

    public Variable(@NotNull var value, boolean pure, boolean numeric, boolean parameter) {
        this.parameter = parameter;
        if (!parameter) {
            throw new AssertionError("Wrong constructor for non parameters");
        }
        this.numeric = numeric;
        this.value = value;
        constraint = null;
        constraintSource = null;
        fixed = false;
        readonly = true;
        this.pure = pure;
        thread = Thread.currentThread().getId();
    }

    public Variable(@NotNull var value,
                    @Nullable var constraint,
                    @Nullable String constraintSource,
                    boolean numeric,
                    boolean parameter) {
        this.numeric = numeric;
        this.parameter = parameter;
        this.value = value;
        this.constraint = constraint;
        this.constraintSource = constraintSource;
        fixed = false;
        readonly = false;
        pure = false;
        thread = Thread.currentThread().getId();
    }

    public Variable(@NotNull var value,
                    @NotNull VarFlags varFlags,
                    @Nullable var constraint,
                    @Nullable String constraintSource,
                    boolean parameter) {
        numeric = varFlags.isNumeric();
        this.parameter = parameter;
        this.value = value;
        readonly = varFlags.isReadonly();
        this.constraint = constraint;
        this.constraintSource = constraintSource;
        setVolatile(varFlags.isVolatile());
        fixed = varFlags.isFixed();
        pure = varFlags.isPure();
        thread = Thread.currentThread().getId();
    }

    public Variable copy(@NotNull var var) {
        return new Variable(constraint, constraintSource, fixed, numeric, parameter, pure, readonly, thread, isVolatile, var);
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

    @NotNull
    public var getValue() {
        return value;
    }

    public void setValue(@NotNull var value) {
        this.value = value;
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

    public boolean isNumeric() {
        return numeric;
    }

    public boolean isParameter() {
        return parameter;
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
}
