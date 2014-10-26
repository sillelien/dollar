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

package com.innowhere.relproxy.jproxy;

import com.innowhere.relproxy.impl.jproxy.screngine.JProxyScriptEngineFactoryImpl;

import javax.script.ScriptEngineFactory;

/**
 * Is the root class of JSR-223 Java Scripting API support.
 *
 * @author Jose Maria Arranz Santamaria
 */
public abstract class JProxyScriptEngineFactory implements ScriptEngineFactory {
    /**
     * Factory method to create a <code>JProxyScriptEngineFactory</code> based on the provided configuration.
     *
     * <p><code>javax.script.ScriptEngine</code> returned by the same factory object calling <code>ScriptEngineFactory.getScriptEngine()</code> will be using the provided configuration.</p>
     *
     * @param config the configuration object.
     * @return the new factory initialized with the provided configuration.
     */
    public static JProxyScriptEngineFactory create(JProxyConfig config) {
        return JProxyScriptEngineFactoryImpl.create(config);
    }
}
