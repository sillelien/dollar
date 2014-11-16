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
import me.neilellis.dollar.guard.NotNullGuard;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface LogAware {

    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var debugf(String message, Object... values);

    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var debug(Object message);

    @Guarded(ChainGuard.class)
    var debug();

    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var infof(String message, Object... values);

    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var info(Object message);

    @Guarded(ChainGuard.class)
    var info();

    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var errorf(String message, Object... values);

    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var error(Throwable exception);

    @Guarded(ChainGuard.class)
    @Guarded(NotNullGuard.class)
    var error(Object message);

    @Guarded(ChainGuard.class)
    var error();


}
