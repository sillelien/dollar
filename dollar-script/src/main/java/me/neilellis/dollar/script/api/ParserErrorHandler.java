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

import me.neilellis.dollar.api.DollarException;
import me.neilellis.dollar.api.script.SourceSegment;
import me.neilellis.dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ParserErrorHandler {
    var handle(@NotNull Scope scope, @NotNull SourceSegment source, @NotNull AssertionError e);

    var handle(@NotNull Scope scope, @NotNull SourceSegment source, @NotNull DollarException e);

    @NotNull var handle(Scope scope, @Nullable SourceSegment source, Exception e);

    void handleTopLevel(Throwable t) throws Throwable;
}
