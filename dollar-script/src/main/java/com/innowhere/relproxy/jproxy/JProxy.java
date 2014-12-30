
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

import com.innowhere.relproxy.impl.jproxy.JProxyConfigImpl;
import com.innowhere.relproxy.impl.jproxy.JProxyDefaultImpl;
import org.jetbrains.annotations.NotNull;

public class JProxy {
    /**
     * Creates a {@link JProxyConfig} object to be used to configure {@code JProxy} and {@link
     * JProxyScriptEngineFactory}.
     *
     * @return a new configuration object.
     * @see #init(JProxyConfig)
     * @see JProxyScriptEngineFactory#create(JProxyConfig)
     */
    @NotNull public static JProxyConfig createJProxyConfig() {
        return JProxyDefaultImpl.createJProxyConfig();
    }

    /**
     * Initializes {@code JProxy} with the provided configuration object.
     *
     * @param config
     */
    public static void init(JProxyConfig config) {
        JProxyDefaultImpl.initStatic((JProxyConfigImpl) config);
    }

    /**
     * Creates a proxy object using {@code java.lang.reflect.Proxy} based on the provided Java object and the class
     * of the implemented Java interface.
     *
     * <p>If {@code JProxy} has been configured and is enabled this method returns a {@code java.lang.reflect.Proxy}
     * object implementing instead of
     * the original object provided. Methods called in proxy object are received by {@code JProxy} and forwarded to
     * the original object, if source code
     * managed by {@code JProxy} has been changed, the class of the original object is reloaded based on the new
     * source and the original object
     * is recreated with the new class and fields are re-set in the new object, then the method is called on the new
     * original object.</p>
     *
     * <p>If {@code JProxy} is disabled returns the original object provided with no performance penalty.</p>
     *
     * @param <T>   the interface implemented by the original object and proxy object returned.
     * @param obj   the original object to proxy.
     * @param clasz the class of the interface implemented by the original object and proxy object returned.
     * @return the {@code java.lang.reflect.Proxy} object associated or the original object when {@code JProxy} is disabled.
     */
    public static <T> T create(T obj, Class<T> clasz) {
        return JProxyDefaultImpl.createStatic(obj, clasz);
    }

    /**
     * Stops source code periodic change detection.
     *
     * <p>Periodicity of change detection is defined by {@link JProxyConfig#setScanPeriod(long)}</p>
     *
     * @return true if source change detection has been stopped, false if it is already stopped or {@code JProxy} is not enabled or initialized.
     * @see #stop()
     */
    public static boolean stop() {
        return JProxyDefaultImpl.stopStatic();
    }

    /**
     * Starts source code periodic change detection.
     *
     * <p>Periodicity of change detection is defined by {@link JProxyConfig#setScanPeriod(long)}.</p>
     *
     * <p>By default when {@code JProxy} is initialized and enabled.</p>
     *
     * @return true if source change detection has been started again, false if it is already started or cannot start
     * because {@code JProxy} is not enabled or initialized or scan period is not positive.
     * @see #start()
     */
    public static boolean start() {
        return JProxyDefaultImpl.startStatic();
    }
}
