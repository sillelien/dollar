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

import me.neilellis.dollar.api.var;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

public interface DollarParser {

    @NotNull Scope currentScope();

    void export(@NotNull String name, @NotNull var export);

    @NotNull ParserErrorHandler getErrorHandler();

    <T> T inScope(boolean pure, String scopeName,
                  @NotNull Scope currentScope,
                  @NotNull Function<Scope, T> r);

    ParserOptions options();

    @NotNull var parse(@NotNull File file, boolean parallel) throws IOException;

    @NotNull var parse(@NotNull Scope scope, @NotNull String source);

    @NotNull var parse(@NotNull Scope scope, @NotNull File file, boolean parallel) throws IOException;

    @NotNull var parse(@NotNull Scope scope, InputStream in, boolean parallel) throws IOException;

    @NotNull var parse(InputStream in, String file, boolean parallel) throws IOException;

    @NotNull var parse(@NotNull String source, boolean parallel) throws IOException;

    @NotNull var parseMarkdown(@NotNull String source);

    List<Scope> scopes();
}
