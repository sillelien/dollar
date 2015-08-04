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

public interface BooleanAware {

    /**
     * Is boolean.
     *
     * @return true if this object is a boolean
     */
    boolean isBoolean();

    /**
     * Is false.
     *
     * @return true if this object is a boolean and is false
     */
    boolean isFalse();

    /**
     * Is true.
     *
     * @return true if this object is a boolean and is true
     */
    boolean isTrue();

    /**
     * Is neither true nor false.
     *
     * @return true if this object is neither true nor false
     */
    boolean neitherTrueNorFalse();

    /**
     * Is truthy, i.e. if numeric then non zero, if a string,list or map then not empty etc.
     *
     * @return if this object is more true than false
     */
    boolean truthy();
}
