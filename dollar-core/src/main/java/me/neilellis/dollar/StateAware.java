/*
 * Copyright (c) 2014 Neil Ellis
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

package me.neilellis.dollar;

import com.github.oxo42.stateless4j.StateMachine;
import me.neilellis.dollar.guard.Guarded;
import me.neilellis.dollar.guard.NotNullGuard;
import me.neilellis.dollar.types.ResourceState;
import me.neilellis.dollar.types.Signal;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface StateAware<R extends StateAware<R>> {

    @NotNull
    @Guarded(NotNullGuard.class)
    R $create();

    @NotNull
    @Guarded(NotNullGuard.class)
    R $destroy();

    @NotNull
    @Guarded(NotNullGuard.class)
    R $pause();

    @Guarded(NotNullGuard.class) void $signal(@NotNull Signal signal);

    @NotNull
    @Guarded(NotNullGuard.class)
    R $start();

    @NotNull
    @Guarded(NotNullGuard.class)
    R $state();

    @NotNull
    @Guarded(NotNullGuard.class)
    R $stop();

    @NotNull
    @Guarded(NotNullGuard.class)
    R $unpause();

    @NotNull
    @Guarded(NotNullGuard.class)
    StateMachine<ResourceState, Signal> getStateMachine();

}
