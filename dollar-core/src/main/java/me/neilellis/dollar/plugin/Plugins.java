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

package me.neilellis.dollar.plugin;

import java.util.*;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class Plugins {

  public static <T extends ExtensionPoint<T>> List<T> allProviders(Class<T> serviceClass) {
    final ServiceLoader<T> loader = ServiceLoader.<T>load(serviceClass);
    final Iterator<T> iterator = loader.iterator();
    if (!iterator.hasNext()) {
      return Collections.singletonList(NoOpProxy.newInstance(serviceClass));
    }
    List<T> result = new ArrayList<>();
    while (iterator.hasNext()) { result.add(iterator.next()); }
    return result;
  }

  //TODO: aggregate all available providers
  public static <T extends ExtensionPoint<T>> T newInstance(Class<T> serviceClass) {
    final ServiceLoader<T> loader = ServiceLoader.load(serviceClass);
    if (!loader.iterator().hasNext()) {
      return NoOpProxy.newInstance(serviceClass);
    }
    return loader.iterator().next().copy();
  }

  public static <T extends ExtensionPoint<T>> T sharedInstance(Class<T> serviceClass) {
    final ServiceLoader<T> loader = ServiceLoader.load(serviceClass);
    if (!loader.iterator().hasNext()) {
      return NoOpProxy.newInstance(serviceClass);
    }
    return loader.iterator().next();
  }

}
