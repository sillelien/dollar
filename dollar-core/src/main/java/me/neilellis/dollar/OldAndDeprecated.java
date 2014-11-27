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
import me.neilellis.dollar.guard.NotNullCollectionGuard;
import me.neilellis.dollar.guard.NotNullGuard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.stream.Stream;

import static me.neilellis.dollar.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface OldAndDeprecated {

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
    @Deprecated
    @Guarded(ChainGuard.class) var $inc(@NotNull var amount);

    @NotNull
    @Deprecated
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class) var $pipe(@NotNull String classModule);

    @Deprecated
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class) var eval(String label, DollarEval eval);

    @Deprecated
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class) var eval(DollarEval eval);

    //  /**
//   * If the class has a method $ call($ in) then that method is called otherwise converts this object to a set of
// string
//   * parameters and passes them to the main method of the clazz. <p> NB: This is the preferred way to pass values
//   * between classes as it preserves the stateless nature. Try where possible to maintain a stateless context to
//   * execution. </p>
//   *
//   * @param clazz the class to pass this to.
//   */
    @Deprecated
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class) var eval(Class clazz);

    @NotNull
    @Deprecated
    @Guarded(NotNullGuard.class) Stream<String> keyStream();

    /**
     * Returns this {@link me.neilellis.dollar.var} object as a stream of key value pairs, with the values themselves
     * being {@link me.neilellis.dollar.var} objects.
     *
     * @return stream of key/value pairs
     */

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(NotNullCollectionGuard.class)
    @Deprecated java.util.stream.Stream<Map.Entry<String, var>> kvStream();

    /**
     * Returns the value for the supplied key as a general {@link Number}.
     *
     * @param key the key to look up
     *
     * @return a Number or null if this operation is not applicable
     */
    @Nullable
    @Deprecated Number number(@NotNull String key);
}
