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

package com.sillelien.dollar.api.plugin;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class NoOpProxy<T extends ExtensionPoint<T>> implements java.lang.reflect.InvocationHandler {


  private final Class<T> c;
  private boolean first = true;

  private NoOpProxy(Class<T> c) {
    this.c = c;
  }

    @NotNull public static <T extends ExtensionPoint<T>> T newInstance(@NotNull Class<T> c) {
    return (T) java.lang.reflect.Proxy.newProxyInstance(
        c.getClassLoader(),
        new Class<?>[]{c},
        new NoOpProxy<>(c));
  }

    @NotNull public Object invoke(Object proxy, @NotNull Method m, Object[] args)
      throws Throwable {
    throw new UnsupportedOperationException("Method " + m + " cannot be invoked as no provider for " + c.getName() + " could be found on the classpath, please add the appropriate plugin to the classpath.");
  }
}