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

import com.google.common.collect.ImmutableList;
import me.neilellis.dollar.guard.ChainGuard;
import me.neilellis.dollar.guard.Guarded;
import me.neilellis.dollar.guard.ReturnVarOnlyGuard;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface VarInternal {
    /**
     * Returns a deep copy of this object. You should never need to use this operation as all {@link
     * me.neilellis.dollar.var} objects are immutable. Therefore they can freely be shared between threads.
     *
     * @return a deep copy of this object
     */
    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(ReturnVarOnlyGuard.class)
    var $copy();

    @NotNull
    /**
     * Like _unwrap() except it causes lambda evaluation and propagates through lists and maps. The result is a
     * static value with no nested lambdas.
     */
    var _fix();

    void _src(String src);

    String _src();

    /**
     * Unwraps any wrapper classes around the actual type class.
     *
     * @return an unwrapped class.
     */
    @NotNull var _unwrap();

    @NotNull var copy(@NotNull ImmutableList<Throwable> errors);

}
