/*
 *    Copyright (c) 2014-2017 Neil Ellis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.sillelien.dollar.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MetadataAware {

    /**
     * Gets meta attribute.
     *
     * @param key the key
     * @return the meta attribute
     */
    @Nullable
    String metaAttribute(@NotNull String key);

    /**
     * Gets meta attribute.
     *
     * @param key the key
     * @return the meta object
     */
    @Nullable
    Object meta(@NotNull String key);

    /**
     * Sets meta attribute.
     *
     * @param key   the key
     * @param value the value
     */
    void metaAttribute(@NotNull String key, @NotNull String value);

    /**
     * Sets meta attribute.
     *
     * @param key   the key
     * @param value the value
     */
    void meta(@NotNull String key, @NotNull Object value);
}
