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

package me.neilellis.dollar.plugins.pipe;

import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.deps.DependencyRetriever;
import me.neilellis.dollar.script.ModuleResolver;
import me.neilellis.dollar.script.Scope;
import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.resolution.DependencyResolutionException;

public class MavenModuleResolver implements ModuleResolver {
    @NotNull @Override
    public ModuleResolver copy() {
        return this;
    }

    @NotNull @Override
    public String getScheme() {
        return "mvn";
    }

    @NotNull @Override
    public Pipeable resolve(@NotNull String uriWithoutScheme, Scope scope) {
        String[] strings = uriWithoutScheme.split(":", 2);
        try {
            return (Pipeable) DependencyRetriever.retrieve(strings[1]).loadClass(strings[0]).newInstance();
        } catch (@NotNull InstantiationException | IllegalAccessException | ClassNotFoundException |
                DependencyResolutionException e) {
            return DollarStatic.logAndRethrow(e);
        }
    }
}
