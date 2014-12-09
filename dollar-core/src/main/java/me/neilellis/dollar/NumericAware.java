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

import me.neilellis.dollar.guard.ChainGuard;
import me.neilellis.dollar.guard.Guarded;
import me.neilellis.dollar.guard.NotNullGuard;
import me.neilellis.dollar.guard.NotNullParametersGuard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.neilellis.dollar.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface NumericAware {


    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class) var $abs();

    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @NotNull
    default var $dec() {
        return $minus($(1));
    }

    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $minus(@NotNull var v);

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class) var $divide(@NotNull var v);

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    default var $inc() {
        return $plus($(1));
    }

    /**
     * Returns a new {@link me.neilellis.dollar.var} with this value appended to it.
     *
     * @param v the value to append, this value may be null
     *
     * @return a new object with the value supplied appended
     */

    @NotNull
    @Guarded(ChainGuard.class) var $plus(@Nullable var v);

    default var $minus(int i) {
        return $minus(DollarStatic.$(i));
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class) var $modulus(@NotNull var v);

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class) var $multiply(@NotNull var v);

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class) var $negate();

    default var $plus(int i) {
        return $plus(DollarStatic.$(i));
    }
}
