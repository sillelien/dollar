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
import org.jetbrains.annotations.NotNull;

import static me.neilellis.dollar.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface NumericAware {


    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class) var $abs();

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @Deprecated
    default var $dec(@NotNull String key, @NotNull var amount) {
        return $dec($(key), amount);
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @Deprecated var $dec(@NotNull var key, @NotNull var amount);

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @Deprecated
    default var $dec(@NotNull String key, @NotNull Number amount) {
        return $dec($(key), $(amount));
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @Deprecated
    default var $dec(@NotNull Number amount) {
        return $dec($(amount));
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @Deprecated var $dec(@NotNull var amount);

    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @NotNull
    default var $dec() {
        return $dec($(1));
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class) var $divide(@NotNull var v);

    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @NotNull
    @Deprecated
    default var $inc(@NotNull String key, @NotNull var amount) {
        return $inc($(key), amount);
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @Deprecated var $inc(@NotNull var key, @NotNull var amount);

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @Deprecated
    default var $inc(@NotNull String key, @NotNull Number amount) {
        return $inc($(key), $(amount));
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    @Deprecated
    default var $inc(@NotNull Number amount) {
        return $inc($(amount));
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class) var $inc(@NotNull var amount);

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    default var $inc() {
        return $inc($(1));
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
}
