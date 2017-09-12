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

import dollar.api.Scope;
import dollar.api.VarKey;
import dollar.api.var;
import dollar.internal.runtime.script.ScriptScope;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;

public interface DollarParser {


    @NotNull ThreadLocal<DollarParser> parser = new ThreadLocal<>();

    void export(VarKey name, @NotNull var export);


    @NotNull
    ParserOptions options();


    @NotNull
    var parse(@NotNull ScriptScope scriptScope, @NotNull String source) throws Exception;

    @NotNull var parse(@NotNull File file, boolean parallel) throws Exception;

    @NotNull
    var parse(@NotNull InputStream in, boolean parallel, @NotNull Scope scope) throws Exception;

    @NotNull
    var parse(@NotNull InputStream in, @NotNull String file, boolean parallel) throws Exception;

    @NotNull var parse(@NotNull String source, boolean parallel) throws Exception;

    @NotNull var parseMarkdown(@NotNull String source);

}
