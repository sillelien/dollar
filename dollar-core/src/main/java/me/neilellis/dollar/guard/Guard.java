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

package me.neilellis.dollar.guard;

import java.lang.reflect.Method;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface Guard {

    String description();

    void preCondition(Object guarded, Method method, Object[] args);

    void postCondition(Object guarded, Method method, Object[] args, Object result);

    default void assertNotNull(Object o, Method method) {
        assertNotNull("-", o, method);
    }

    default void assertNotNull(String message, Object o, Method method) {
        if (o == null) {
            throw new AssertionError(description() + " " + message + " Not Null FAILED for " + method);
        }
    }

    default void assertTrue(boolean condition, Method method) {
        if (!condition) {
            throw new AssertionError(description() + " FAILED for " + method);
        }
    }

}
