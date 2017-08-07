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

import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.Scope;
import dollar.internal.runtime.script.ScriptScope;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.InputStream;

public interface DollarParser {


    void export(@NotNull String name, @NotNull var export);

    @NotNull ParserErrorHandler getErrorHandler();

    ParserOptions options();


    @NotNull var parse(ScriptScope scriptScope, @NotNull String source) throws Exception;

    @NotNull var parse( @NotNull File file, boolean parallel) throws Exception;

    @NotNull var parse(InputStream in, boolean parallel, Scope scope) throws Exception;

    @NotNull var parse(InputStream in, String file, boolean parallel) throws Exception;

    @NotNull var parse(@NotNull String source, boolean parallel) throws Exception;

    @NotNull var parseMarkdown(@NotNull String source);

}
