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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface ErrorAware {
    @NotNull
    var $error(@NotNull String errorMessage);

    @NotNull
    var $error(@NotNull Throwable error);

    @NotNull
    var $error();

    @NotNull
    var $errors();

    @NotNull
    var $fail(@NotNull Consumer<List<Throwable>> handler);

    @NotNull
    default var $invalid(@NotNull String errorMessage) {
        return $error(errorMessage, ErrorType.VALIDATION);
    }

    @NotNull
    var $error(@NotNull String errorMessage, @NotNull ErrorType type);

    @NotNull
    ImmutableList<Throwable> errors();

    boolean hasErrors();

    var clearErrors();

    @NotNull
    List<String> errorTexts();


    public enum ErrorType {VALIDATION, SYSTEM}
}
