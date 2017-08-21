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

package com.sillelien.dollar.api.script;

import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.plugin.ExtensionPoint;
import com.sillelien.dollar.api.plugin.NoOpProxy;
import org.jetbrains.annotations.NotNull;

import java.util.ServiceLoader;

public interface ModuleResolver extends ExtensionPoint<ModuleResolver> {

    @NotNull
    static ModuleResolver resolveModule(String scheme) {
        final ServiceLoader<ModuleResolver> loader = ServiceLoader.load(ModuleResolver.class);
        for (ModuleResolver piper : loader) {
            if (piper.getScheme().equals(scheme)) {
                return piper.copy();
            }
        }
        return NoOpProxy.newInstance(ModuleResolver.class);
    }


    @NotNull
    String getScheme();

    @NotNull
    <T, P> Pipeable resolve(String uriWithoutScheme, T scope, P parser) throws Exception;
}
