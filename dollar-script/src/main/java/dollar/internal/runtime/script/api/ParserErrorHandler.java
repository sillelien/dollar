/*
 *    Copyright (c) 2014-2017 Neil Ellis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dollar.internal.runtime.script.api;

import dollar.api.DollarException;
import dollar.api.Scope;
import dollar.api.script.SourceSegment;
import dollar.api.var;
import dollar.internal.runtime.script.api.exceptions.DollarAssertionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface ParserErrorHandler {

    @NotNull
    var handle(@NotNull Scope scope, @Nullable SourceSegment source, @NotNull DollarAssertionException e);

    @NotNull var handle(@NotNull Scope scope, @Nullable SourceSegment source, @NotNull DollarException e);

    @NotNull
    var handle(@NotNull Scope scope, @Nullable SourceSegment source, @NotNull Throwable e);

    <T extends Throwable> void handleTopLevel(@NotNull T t, @NotNull String name, @NotNull File file) throws T;

}
