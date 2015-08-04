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
import com.sillelien.dollar.api.guard.NotNullGuard;

public interface LogAware {

    /**
     * Debug var.
     *
     * @param message the message
     *
     * @return the var
     */
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var debug(Object message);

    /**
     * Debug var.
     *
     * @return the var
     */
    @Guarded(ChainGuard.class)
    var debug();

    /**
     * Debugf var.
     *
     * @param message the message
     * @param values the values
     * @return the var
     */
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class) var debugf(String message, Object... values);

    /**
     * Error var.
     *
     * @param exception the exception
     * @return the var
     */
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class) var error(Throwable exception);

    /**
     * Error var.
     *
     * @param message the message
     * @return the var
     */
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class) var error(Object message);

    /**
     * Error var.
     *
     * @return the var
     */
    @Guarded(ChainGuard.class) var error();

    /**
     * Errorf var.
     *
     * @param message the message
     * @param values  the values
     *
     * @return the var
     */
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var errorf(String message, Object... values);

    /**
     * Info var.
     *
     * @param message the message
     * @return the var
     */
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class) var info(Object message);

    /**
     * Info var.
     *
     * @return the var
     */
    @Guarded(ChainGuard.class) var info();

    /**
     * Infof var.
     *
     * @param message the message
     * @param values the values
     * @return the var
     */
    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class) var infof(String message, Object... values);


}
