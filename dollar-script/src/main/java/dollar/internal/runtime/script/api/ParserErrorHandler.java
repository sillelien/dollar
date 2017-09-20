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
import dollar.api.Value;
import dollar.api.script.Source;
import dollar.internal.runtime.script.api.exceptions.DollarAssertionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public interface ParserErrorHandler {

    @NotNull
    Value handle(@NotNull Scope scope, @Nullable Source source, @NotNull DollarAssertionException e);

    @NotNull Value handle(@NotNull Scope scope, @Nullable Source source, @NotNull DollarException e);

    @NotNull
    Value handle(@NotNull Scope scope, @Nullable Source source, @NotNull Throwable e);

    <T extends Throwable> void handleTopLevel(@NotNull T t, @NotNull String name, @Nullable File file) throws T;

}
