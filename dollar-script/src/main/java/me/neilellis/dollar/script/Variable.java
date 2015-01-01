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

package me.neilellis.dollar.script;

import me.neilellis.dollar.api.var;
import org.jetbrains.annotations.Nullable;

class Variable {
    final boolean readonly;
    final var constraint;
    final long thread;
    final boolean pure;
    final boolean fixed;
    final String constraintSource;
    boolean isVolatile;
    var value;

    public Variable(var value, var constraint, String constraintSource) {

        this.value = value;
        this.constraint = constraint;
        this.constraintSource = constraintSource;
        this.fixed = false;
        readonly = false;
        pure = false;
        thread = Thread.currentThread().getId();
    }

    public Variable(var value, boolean readonly, var constraint, String constraintSource, boolean isVolatile,
                    boolean fixed, boolean pure) {
        this.value = value;
        this.readonly = readonly;
        this.constraint = constraint;
        this.constraintSource = constraintSource;
        this.isVolatile = isVolatile;
        this.fixed = fixed;
        this.pure = pure;
        thread = Thread.currentThread().getId();
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        Variable variable = (Variable) o;

        return value.equals(variable.value);

    }
}
