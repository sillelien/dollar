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

import java.util.function.Consumer;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface IntegrationProviderAware {
    /**
     * Send synchronously using an integration provider.
     *
     * @param uri
     */
    @Guarded(NotNullParametersGuard.class)
    @Guarded(ChainGuard.class)
    var $send(String uri);

    /**
     * Listen using an integration provider.
     *
     * @param uri
     */
    @Guarded(NotNullParametersGuard.class)
    void $listen(String uri, Consumer<var> handler);

    /**
     * Publish using an integration provider.
     *
     * @param uri
     */
    @Guarded(NotNullParametersGuard.class)
    void $publish(String uri);

    /**
     * Send async using an integration provider
     *
     * @param uri
     */
    @Guarded(NotNullParametersGuard.class)
    void $dispatch(String uri);
}
