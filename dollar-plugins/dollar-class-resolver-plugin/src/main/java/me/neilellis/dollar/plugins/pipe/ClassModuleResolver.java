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

import me.neilellis.dollar.api.DollarStatic;
import me.neilellis.dollar.api.Pipeable;
import me.neilellis.dollar.api.script.ModuleResolver;
import org.jetbrains.annotations.NotNull;

public class ClassModuleResolver implements ModuleResolver {
    @NotNull @Override
    public ModuleResolver copy() {
        return this;
    }

    @NotNull @Override
    public String getScheme() {
        return "class";
    }

    @NotNull @Override
    public <T> Pipeable resolve(@NotNull String uriWithoutScheme, T scope) throws Exception {
        return (Pipeable) DollarStatic.context().getClassLoader().loadClass(uriWithoutScheme).newInstance();
    }
}
