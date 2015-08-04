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

package com.sillelien.dollar.api;

import com.sillelien.dollar.api.guard.ChainGuard;
import com.sillelien.dollar.api.guard.Guarded;
import com.sillelien.dollar.api.guard.NotNullParametersGuard;

public interface URIAware {

    @Guarded(ChainGuard.class) var $all();

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $dispatch(var lhs) {
        return $write(lhs, false, false);
    }


    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $write(var value, boolean blocking, boolean mutating);

    @Guarded(ChainGuard.class) var $drain();


    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $give(var lhs) {
        return $write(lhs, false, true);
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
        return $read(false, false);
    }

    /**
     * Generic read
     */
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class) var $read(boolean blocking, boolean mutating);

    @Guarded(ChainGuard.class)
    default var $poll() {
        return $read(false, true);
    }

    @Guarded(ChainGuard.class)
    default var $pop() {
        return $read(true, true);
    }

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class) var $publish(var lhs);

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $push(var lhs) {
        return $write(lhs, true, true);
    }

    /**
     * Receive (from this) synchronously.
     */
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default var $read() {
        return $read(true, true);
    }

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class) default var $subscribe(Pipeable subscription) {
        return $listen(subscription, null);
    }

    default var $listen(Pipeable pipeable, String id) {return DollarStatic.$void();}

    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class) default var $subscribe(Pipeable subscription, String key) {
        return $listen(subscription, key);
    }

    /**
     * Send (to this) synchronously.
     */
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    default var $write(var value) {
        return $write(value, true, true);
    }


}
