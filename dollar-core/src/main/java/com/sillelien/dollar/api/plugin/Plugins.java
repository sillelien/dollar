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

import com.sillelien.dollar.api.DollarException;
import com.sillelien.dollar.api.uri.URIHandlerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Plugins {

    @NotNull public static <T extends ExtensionPoint<T>> List<T> allProviders(@NotNull Class<T> serviceClass) {
        final ServiceLoader<T> loader = ServiceLoader.load(serviceClass);
        final Iterator<T> iterator = loader.iterator();
        if (!iterator.hasNext()) {
            return Collections.singletonList(NoOpProxy.newInstance(serviceClass));
        }
        List<T> result = new ArrayList<>();
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        return result;
    }

    //TODO: aggregate all available providers
    public static <T extends ExtensionPoint<T>> T newInstance(@NotNull Class<T> serviceClass) {
        final ServiceLoader<T> loader = ServiceLoader.load(serviceClass);
        if (!loader.iterator().hasNext()) {
            return NoOpProxy.newInstance(serviceClass);
        }
        return loader.iterator().next().copy();
    }

    @NotNull public static URIHandlerFactory resolveURIProvider(String scheme) {
        final ServiceLoader<URIHandlerFactory> loader = ServiceLoader.load(URIHandlerFactory.class);
        for (URIHandlerFactory handler : loader) {
            if (handler.handlesScheme(scheme)) {
                return handler;
            }
        }
        throw new DollarException("Could not find any provider for URI scheme " + scheme);
    }

    @Nullable public static <T extends ExtensionPoint<T>> T sharedInstance(@NotNull Class<T> serviceClass) {
        final ServiceLoader<T> loader = ServiceLoader.load(serviceClass);
        if (!loader.iterator().hasNext()) {
            return NoOpProxy.newInstance(serviceClass);
        }
        int priority = -1;
        T plugin = null;
        for (T t : loader) {
            if (t.priority() > priority) {
                plugin = t;
                priority = t.priority();
            }
        }
        return plugin;
    }

}
