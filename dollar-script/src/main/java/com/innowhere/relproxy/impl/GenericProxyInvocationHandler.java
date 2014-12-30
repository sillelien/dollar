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

package com.innowhere.relproxy.impl;

import com.innowhere.relproxy.RelProxyOnReloadListener;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @param <T>
 * @author jmarranz
 */
public abstract class GenericProxyInvocationHandler<T> implements InvocationHandler {
    protected final GenericProxyImpl root;
    protected GenericProxyVersionedObject<T> verObj;

    public GenericProxyInvocationHandler(GenericProxyImpl root) {
        this.root = root;
    }

    @Override
    public synchronized Object invoke(Object proxy, @NotNull Method method, Object[] args) throws Throwable {
        T oldObj = verObj.getCurrent();
        T obj = verObj.getNewVersion();

        RelProxyOnReloadListener reloadListener = root.getRelProxyOnReloadListener();
        if (oldObj != obj && reloadListener != null)
            reloadListener.onReload(oldObj, obj, proxy, method, args);

        return method.invoke(obj, args);
    }
}
