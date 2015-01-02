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

package me.neilellis.dollar.relproxy.jproxy;

import javax.script.ScriptEngine;

public interface JProxyScriptEngine extends ScriptEngine {
    /**
     * This method is the same as {@link me.neilellis.dollar.relproxy.jproxy.JProxy#create(Object, Class)} but applied
     * to this {@code JProxyScriptEngine}
     *
     * @param <T>   the interface implemented by the original object and proxy object returned.
     * @param obj   the original object to proxy.
     * @param clasz the class of the interface implemented by the original object and proxy object returned.
     *
     * @return the {@code java.lang.reflect.Proxy} object associated or the original object when {@code JProxy} is
     * disabled.
     */
    public <T> T create(T obj, Class<T> clasz);

    /**
     * This method is the same as {@link me.neilellis.dollar.relproxy.jproxy.JProxy#start()} but applied to this {@code
     * JProxyScriptEngine}
     *
     * @return true if source change detection has been started again, false if it is already started or cannot start
     * because this {@code JProxyScriptEngine} is not enabled or initialized or scan period is not positive.
     *
     * @see #start()
     */
    public boolean start();

    /**
     * This method is the same as {@link me.neilellis.dollar.relproxy.jproxy.JProxy#stop()} but applied to this {@code
     * JProxyScriptEngine}
     *
     * @return true if source change detection has been stopped, false if it is already stopped or this {@code
     * JProxyScriptEngine} is not enabled or initialized.
     *
     * @see #stop()
     */
    public boolean stop();
}
