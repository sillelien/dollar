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

package me.neilellis.dollar.script.api;

import me.neilellis.dollar.api.var;
import org.jetbrains.annotations.Nullable;

public class Variable {
    private final boolean readonly;
    private final var constraint;
    private final long thread;
    private final boolean pure;
    private final boolean fixed;
    private final String constraintSource;
    private boolean isVolatile;
    private var value;

    public Variable(var value, var constraint, String constraintSource) {

        this.setValue(value);
        this.constraint = constraint;
        this.constraintSource = constraintSource;
        this.fixed = false;
        readonly = false;
        pure = false;
        thread = Thread.currentThread().getId();
    }

    public Variable(var value, boolean readonly, var constraint, String constraintSource, boolean isVolatile,
                    boolean fixed, boolean pure) {
        this.setValue(value);
        this.readonly = readonly;
        this.constraint = constraint;
        this.constraintSource = constraintSource;
        this.setVolatile(isVolatile);
        this.fixed = fixed;
        this.pure = pure;
        thread = Thread.currentThread().getId();
    }

    public var getConstraint() {
        return constraint;
    }

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

    public var getValue() {
        return value;
    }

    public void setValue(var value) {
        this.value = value;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        Variable variable = (Variable) o;

        return getValue().equals(variable.getValue());

    }

    public boolean isFixed() {
        return fixed;
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
