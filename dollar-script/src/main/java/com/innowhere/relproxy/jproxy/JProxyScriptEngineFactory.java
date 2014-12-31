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

package com.innowhere.relproxy.jproxy;

import com.innowhere.relproxy.impl.jproxy.screngine.JProxyScriptEngineFactoryImpl;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngineFactory;

public abstract class JProxyScriptEngineFactory implements ScriptEngineFactory {
    /**
     * Factory method to create a {@code JProxyScriptEngineFactory} based on the provided configuration.
     *
     * <p>{@code javax.script.ScriptEngine} returned by the same factory object calling {@code ScriptEngineFactory
     * .getScriptEngine()} will be using the provided configuration.</p>
     *
     * @param config the configuration object.
     * @return the new factory initialized with the provided configuration.
     */
    @NotNull public static JProxyScriptEngineFactory create(JProxyConfig config) {
        return JProxyScriptEngineFactoryImpl.create(config);
    }
}
