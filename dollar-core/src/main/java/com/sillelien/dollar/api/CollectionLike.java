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

package com.sillelien.dollar.api;

import com.sillelien.dollar.api.guard.ChainGuard;
import com.sillelien.dollar.api.guard.Guarded;
import com.sillelien.dollar.api.guard.NotNullGuard;
import com.sillelien.dollar.api.guard.NotNullParametersGuard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CollectionLike {

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class) default var $(@NotNull Object key) {
        return $get(DollarStatic.$(key));
    }

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class) var $get(@NotNull var rhs);

    @NotNull
    @Guarded(NotNullGuard.class) var $append(@NotNull var value);

    @NotNull
    @Guarded(ChainGuard.class)
    @Guarded(NotNullParametersGuard.class)
    default var $contains(@NotNull var value) {
        return $containsValue(value);
    }

    @Guarded(ChainGuard.class)
    @NotNull
    @Guarded(NotNullParametersGuard.class) var $containsValue(@NotNull var value);

    /**
     * Convenience method for the Java API. Returns true if this object has the supplied key.
     *
     * @param key the key
     *
     * @return true if the key exists.
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class) default var $has(@NotNull String key) {
        return $has(DollarStatic.$(key));
    }

    /**
     * Returns true if this object has the supplied key.
     *
     * @param key the key
     *
     * @return true if the key exists.
     */
    @NotNull
    @Guarded(NotNullParametersGuard.class) var $has(@NotNull var key);

    /**
     * Returns a boolean var which is true if this is empty.
     *
     * @return a true var if it is empty.
     */
    @NotNull
    @Guarded(ChainGuard.class) default var $isEmpty() {
        return DollarStatic.$($size().toInteger() == 0);
    }

    @NotNull
    @Guarded(ChainGuard.class) var $size();

    @NotNull
    @Guarded(NotNullGuard.class) var $prepend(@NotNull var value);

    /**
     * Remove by key. (Map like data only).
     *
     * @param key the key of the key/value pair to remove
     *
     * @return the modified var
     */
    @NotNull var $removeByKey(@NotNull String key);

    @NotNull
    default var $set(@NotNull String key, @Nullable Object value) {
        return $set(DollarStatic.$(key), value);
    }

    /**
     * If this type supports the setting of Key/Value pairs this will set the supplied key value pair on a copy of this
     * object. If it doesn't an exception will be thrown.
     *
     * @param key   a String key for the value to be stored in this value.
     * @param value the {@link var} to add.
     *
     * @return the updated copy.
     */
    @NotNull
    @Guarded(ChainGuard.class) var $set(@NotNull var key, @Nullable Object value);

    /**
     * Convenience version of {@link #$remove(var)} for the Java API.
     *
     * @param valueToRemove the value to be removed.
     *
     * @return a new object with the value removed.
     */
    @NotNull
    @Guarded(ChainGuard.class) default var remove(Object valueToRemove) {
        return $remove(DollarStatic.$(valueToRemove));
    }

    /**
     * Return a new version of this object with the supplied value removed. THe removal is type specific.
     *
     * @param valueToRemove the value to remove.
     *
     * @return a new object with the value removed.
     */
    @NotNull
    @Guarded(ChainGuard.class) var $remove(var valueToRemove);
}
