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

    @Guarded(ChainGuard.class) var $all();

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $dispatch(var lhs) {
        return $send(lhs, false, false);
    }

    /**
     * Generic Send.
     */
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $send(var value, boolean blocking, boolean mutating);

    @Guarded(ChainGuard.class) var $drain();

//    @Guarded(NotNullParametersGuard.class) String $listen(Pipeable pipe);
//
//    @Guarded(NotNullParametersGuard.class) String $listen(Pipeable pipe, String key);

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $give(var lhs) {
        return $send(lhs, false, true);
    }

    /**
     * THis is for reactive programming using lamdas, you probably want $subscribe(...).
     *
     * @param pipeable action
     */
    default var $listen(Pipeable pipeable) {return DollarStatic.$void();}

    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $notify();

    @Guarded(ChainGuard.class)
    default var $peek() {
        return $receive(false, false);
    }

    /**
     * Generic receive
     */
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $receive(boolean blocking, boolean mutating);

    @Guarded(ChainGuard.class)
    default var $poll() {
        return $receive(false, true);
    }

    @Guarded(ChainGuard.class)
    default var $pop() {
        return $receive(true, true);
    }

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class) var $publish(var lhs);

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $push(var lhs) {
        return $send(lhs, true, true);
    }

    /**
     * Receive (from this) synchronously.
     */
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default var $receive() {
        return $receive(true, false);
    }

    /**
     * Send (to this) synchronously.
     */
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default var $send(var value) {
        return $send(value, true, false);
    }

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class) default var $subscribe(Pipeable subscription) {
        return $listen(subscription,
                       null);
    }

    default var $listen(Pipeable pipeable, String id) {return DollarStatic.$void();}

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class) default var $subscribe(Pipeable subscription, String key) {
        return $listen(subscription, key);
    }


}
