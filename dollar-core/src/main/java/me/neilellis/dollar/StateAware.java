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
import org.jetbrains.annotations.NotNull;

/**
 * The methods that manage the state of service type objects, such as {@link me.neilellis.dollar.types.DollarURI}
 * @param <R>  the actual type of the subclass
 * @author  <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface StateAware<R extends StateAware<R>> {

    /**
     * Create the underlying resource.
     *
     * @return this
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    R $create();

    /**
     * Destroy the underlying resource.
     *
     * @return this
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    R $destroy();

    /**
     * Pause the underlying resource.
     *
     * @return this
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    R $pause();

    /**
     * Send an arbitrary {@link me.neilellis.dollar.Signal} to the underlying resource.
     *
     * @param signal the signal to send
     */
    @Guarded(NotNullGuard.class) void $signal(@NotNull Signal signal);

    /**
     * Start the underlying resource.
     *
     * @return this
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    R $start();

    /**
     * Returns the state of the underlying resource.
     *
     * @return the state
     */
    @NotNull
    @Guarded(NotNullGuard.class) var $state();

    /**
     * Stop the underlying resource.
     *
     * @return the r
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    R $stop();

    /**
     * Unpause the underlying resource
     *
     * @return this
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    R $unpause();

    /**
     * Returns the state machine used to manage state for the resource.
     *
     * @return the state machine
     */
    @NotNull
    @Guarded(NotNullGuard.class)
    StateMachine<ResourceState, Signal> getStateMachine();

}
