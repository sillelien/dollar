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

package dollar.plugins.pipe;

import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.script.ModuleResolver;
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
    public <T, P> Pipeable resolve(@NotNull String uriWithoutScheme, @NotNull T scope, @NotNull P parser) throws Exception {
        return (Pipeable) DollarStatic.context().getClassLoader().loadClass(uriWithoutScheme).newInstance();
    }
}
