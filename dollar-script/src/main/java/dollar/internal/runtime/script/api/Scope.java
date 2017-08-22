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

import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.collections.MultiMap;
import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    Variable getVariable(@NotNull String key);

    @Nullable var getConstraint(@NotNull String key);

    @Nullable
    String getConstraintSource(@NotNull String key);



    @Nullable String getFile();

    @NotNull MultiMap<String, Listener> getListeners();

    @NotNull
    var parameter(@NotNull String key);

    @Nullable Scope getScopeForKey(@NotNull String key);

    @Nullable Scope getScopeForParameters();

    @Nullable String getSource();

    @NotNull <T>  Map<String, T> getVariables();

    @NotNull var handleError(@NotNull Throwable t);

    @NotNull
    var handleError(@NotNull Throwable t, var context);

    @NotNull
    var handleError(@NotNull Throwable t, SourceSegment context);

    boolean has(@NotNull String key);

    boolean hasParameter(@NotNull String key);

    void listen(@NotNull String key, @NotNull String id, @NotNull var listener);

    void listen(@NotNull String key, @NotNull String id, @NotNull Pipeable listener);

    @Nullable var notify(@NotNull String variableName);

    void notifyScope(@NotNull String key, @NotNull var value);

    @NotNull var set(@NotNull String key, @NotNull var value, boolean readonly, @Nullable var constraint,
                     @Nullable String constraintSource, boolean isVolatile,
                     boolean fixed, boolean pure);

    @NotNull
    var parameter(@NotNull String key, @NotNull var value);

    void setParent(@Nullable Scope scope);

    @Nullable
    Scope getParent();

    boolean hasParent(@Nullable Scope scope);

    boolean isRoot();

    @NotNull
    Scope copy();


    void destroy();

    boolean pure();
}
