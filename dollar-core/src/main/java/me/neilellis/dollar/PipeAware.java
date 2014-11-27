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

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface PipeAware {
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class) var $eval(@NotNull String js);

    default
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    var $pipe(@NotNull Pipeable pipe) {
        return $pipe("anon", pipe);
    }

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    var $pipe(@NotNull String label, @NotNull Pipeable pipe);

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class) var $pipe(@NotNull String label, @NotNull String js);

    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    var $pipe(@NotNull Class<? extends Pipeable> clazz);


}
