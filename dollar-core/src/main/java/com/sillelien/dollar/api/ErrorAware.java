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

package com.sillelien.dollar.api;

import com.sillelien.dollar.api.collections.ImmutableList;
import com.sillelien.dollar.api.guard.ChainGuard;
import com.sillelien.dollar.api.guard.Guarded;
import com.sillelien.dollar.api.guard.NotNullCollectionGuard;
import com.sillelien.dollar.api.guard.NotNullParametersGuard;
import com.sillelien.dollar.api.types.ErrorType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public interface ErrorAware {


    /**
     * $ error.
     *
     * @param errorMessage the error message
     *
     * @return the var
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $error(@NotNull String errorMessage);

    /**
     * $ error.
     *
     * @param error the error
     * @return the var
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $error(@NotNull Throwable error);

    /**
     * $ error.
     *
     * @return the var
     */
    @NotNull
    @Guarded(ChainGuard.class) var $error();

    /**
     * $ errors.
     *
     * @return the var
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $errors();

    /**
     * $ fail.
     *
     * @param handler the handler
     * @return the var
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $fail(@NotNull Consumer<ImmutableList<Throwable>> handler);

    /**
     * $ invalid.
     *
     * @param errorMessage the error message
     * @return the var
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default var $invalid(@NotNull String errorMessage) {
        return $error(errorMessage, ErrorType.VALIDATION);
    }

    /**
     * $ error.
     *
     * @param errorMessage the error message
     * @param type the type
     * @return the var
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $error(@NotNull String errorMessage, @NotNull ErrorType type);

    /**
     * Return a copy of this object with no errors associated with it.
     *
     * @return a cleaned object
     */
    @NotNull
    @Guarded(ChainGuard.class)
    var clearErrors();

    /**
     * Return
     *
     * @return the list
     */
    @NotNull
    @Guarded(NotNullCollectionGuard.class) List<String> errorTexts();

    /**
     * Errors immutable list.
     *
     * @return the immutable list
     */
    @NotNull
    @Guarded(NotNullCollectionGuard.class) ImmutableList<Throwable> errors();

    /**
     * Has errors.
     *
     * @return the boolean
     */
    boolean hasErrors();
}
