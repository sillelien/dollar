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

import me.neilellis.dollar.guard.ChainGuard;
import me.neilellis.dollar.guard.Guarded;
import me.neilellis.dollar.guard.NotNullParametersGuard;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface URIAware {

    @Guarded(NotNullParametersGuard.class)
    String $listen(Pipeable pipe);

    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    var $notify(var value);

    @Guarded(NotNullParametersGuard.class)
    String $listen(Pipeable pipe, String key);


    /**
     * Generic Send.
     */
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    var $send(var value, boolean blocking, boolean mutating);

    /**
     * Send (to this) synchronously.
     */
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default var $send(var value) {
        return $send(value, true, false);
    }

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $dispatch(var lhs) {
        return $send(lhs, false, false);
    }

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $give(var lhs) {
        return $send(lhs, false, true);
    }

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $push(var lhs) {
        return $send(lhs, true, true);
    }


    /**
     * Generic receive
     */
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    var $receive(boolean blocking, boolean mutating);

    /**
     * Receive (from this) synchronously.
     */
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default var $receive() {
        return $receive(true, false);
    }


    @Guarded(ChainGuard.class)
    default var $poll() {
        return $receive(false, true);
    }

    @Guarded(ChainGuard.class)
    default var $peek() {
        return $receive(false, false);
    }

    @Guarded(ChainGuard.class)
    default var $pop() {
        return $receive(true, true);
    }


    @Guarded(ChainGuard.class)
    var $drain();


    @Guarded(ChainGuard.class)
    var $all();

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    var $subscribe(Pipeable subscription);

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    var $publish(var lhs);

}
