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

package dollar.api;

import com.google.common.collect.Multimap;
import dollar.api.script.SourceSegment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface Scope {

    interface Listener extends Pipeable {
        @NotNull
        String getId();
    }

    @NotNull
    var addErrorHandler(@NotNull var handler);

    void clear();

    @NotNull var get(@NotNull String key, boolean mustFind);

    @Nullable var get(@NotNull String key);

    Variable variable(@NotNull String k);


    @Nullable var constraint(@NotNull String key);

    @Nullable
    String constraintSource(@NotNull String key);


    @Nullable String file();

    @NotNull Multimap<String, Listener> listeners();

    @NotNull
    var parameter(@NotNull String key);

    @Nullable Scope scopeForKey(@NotNull String key);

    @Nullable String source();

    @NotNull <T> Map<String, T> variables();

    @NotNull var handleError(@NotNull Throwable t);

    @NotNull
    var handleError(@NotNull Throwable t, @NotNull var context);

    @NotNull
    var handleError(@NotNull Throwable t, @NotNull SourceSegment context);

    boolean has(@NotNull String key);

    boolean hasParameter(@NotNull String key);

    void listen(@NotNull String key, @NotNull String id, @NotNull var listener);

    void listen(@NotNull String key, @NotNull String id, @NotNull Pipeable listener);

    @Nullable var notify(@NotNull String variableName);

    void notifyScope(@NotNull String key, @NotNull var value);

    var set(@NotNull String key, @NotNull var value, boolean readonly, @Nullable var constraint,
            @Nullable String constraintSource, boolean isVolatile,
            boolean fixed, boolean pure);

    @NotNull
    var parameter(@NotNull String key, @NotNull var value);

    @Nullable
    Scope parent();

    boolean hasParent(@Nullable Scope scope);

    boolean isRoot();

    @NotNull
    Scope copy();


    void destroy();

    boolean pure();

    @NotNull List<var> parametersAsVars();


    void registerClass(@NotNull String name, @NotNull DollarClass dollarClass);

    DollarClass getDollarClass(String name);

    boolean isClassScope();
}
