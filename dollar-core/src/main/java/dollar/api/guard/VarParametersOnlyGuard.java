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

package dollar.api.guard;

import dollar.api.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class VarParametersOnlyGuard implements Guard {
    @NotNull
    @Override
    public String description() {
        return "All Var Guard";
    }

    @Override
    public void postCondition(@NotNull Object guarded, @NotNull Method method, @NotNull Object[] args, @NotNull Object result) {
    }

    @Override
    public void preCondition(@NotNull Object guarded, @NotNull Method method, @Nullable Object[] args) {
        if (args != null) {
            for (Object arg : args) {
                assertTrue(arg instanceof Value, method);
            }
        }
    }

}
