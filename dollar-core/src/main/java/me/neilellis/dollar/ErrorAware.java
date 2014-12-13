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

import me.neilellis.dollar.collections.ImmutableList;
import me.neilellis.dollar.guard.ChainGuard;
import me.neilellis.dollar.guard.Guarded;
import me.neilellis.dollar.guard.NotNullCollectionGuard;
import me.neilellis.dollar.guard.NotNullParametersGuard;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface ErrorAware {
    public enum ErrorType {VALIDATION, SYSTEM}

    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $error(@NotNull String errorMessage);

    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $error(@NotNull Throwable error);

    @NotNull
    @Guarded(ChainGuard.class) var $error();

    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $errors();

    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $fail(@NotNull Consumer<ImmutableList<Throwable>> handler);

    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default var $invalid(@NotNull String errorMessage) {
        return $error(errorMessage, ErrorType.VALIDATION);
    }

    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $error(@NotNull String errorMessage, @NotNull ErrorType type);

    @NotNull
    @Guarded(ChainGuard.class)
    var clearErrors();

    @NotNull
    @Guarded(NotNullCollectionGuard.class) List<String> errorTexts();

    @NotNull
    @Guarded(NotNullCollectionGuard.class) ImmutableList<Throwable> errors();

    boolean hasErrors();
}
