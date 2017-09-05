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

import dollar.api.exceptions.DollarFailureException;
import dollar.api.guard.ChainGuard;
import dollar.api.guard.Guarded;
import dollar.api.guard.NotNullParametersGuard;
import dollar.api.types.ErrorType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface URIAware {

    @NotNull
    @Guarded(ChainGuard.class)
    var $all();

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $dispatch(@NotNull var lhs) {
        return $write(lhs, false, false);
    }


    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    var $write(@NotNull var value, boolean blocking, boolean mutating);

    @NotNull
    @Guarded(ChainGuard.class)
    var $drain();


    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $give(@NotNull var lhs) {
        return $write(lhs, false, true);
    }


    @NotNull
    @Guarded(ChainGuard.class)
    default var $peek() {
        return $read(false, false);
    }

    /**
     * Generic read
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    var $read(boolean blocking, boolean mutating);

    @NotNull
    @Guarded(ChainGuard.class)
    default var $poll() {
        return $read(false, true);
    }

    @NotNull
    @Guarded(ChainGuard.class)
    default var $pop() {
        return $read(true, true);
    }

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    var $publish(@NotNull var lhs);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $push(@NotNull var lhs) {
        return $write(lhs, true, true);
    }

    /**
     * Receive (from this) synchronously.
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default var $read() {
        return $read(true, true);
    }

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $subscribe(@NotNull Pipeable subscription) {
        return $subscribe(subscription, UUID.randomUUID().toString());
    }

    /**
     * For Lambdas and reactive programming, do not use.
     *
     * @param pipeable
     * @param id
     * @return
     */
    @NotNull
    default var $listen(@NotNull Pipeable pipeable, @NotNull String id) {return DollarStatic.$void();}

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $subscribe(@NotNull Pipeable subscription, @NotNull String key) {
        throw new DollarFailureException(ErrorType.INVALID_OPERATION);
    }

    /**
     * Send (to this) synchronously.
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default var $write(@NotNull var value) {
        return $write(value, true, true);
    }


}
