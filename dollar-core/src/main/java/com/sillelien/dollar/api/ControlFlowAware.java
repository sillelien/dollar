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

package com.sillelien.dollar.api;

import com.sillelien.dollar.api.guard.ChainGuard;
import com.sillelien.dollar.api.guard.Guarded;
import com.sillelien.dollar.api.guard.NotNullGuard;
import org.jetbrains.annotations.NotNull;

public interface ControlFlowAware {

    /**
     * Select a value from map based upon the current value and return that.
     *
     * @param map the map
     * @return the var
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    var $choose(@NotNull var map);

    /**
     * $ each.
     *
     * @param pipe the pipe
     * @return the var
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    @Guarded(ChainGuard.class)
    var $each(@NotNull Pipeable pipe);
}
