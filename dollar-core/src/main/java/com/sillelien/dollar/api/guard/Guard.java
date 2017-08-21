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

package com.sillelien.dollar.api.guard;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public interface Guard {

    /**
     * Assert not null.
     *
     * @param value  the value to check for null
     * @param method the method that the assertion relates to
     */
    default void assertNotNull(@NotNull Object value, @NotNull Method method) {
        assertNotNull("-", value, method);
    }

    /**
     * Assert not null.
     * <p>
     * Helper method for guards that asserts a condition is true.
     *
     * @param value  the value to check for null
     * @param method the method that the assertion relates to
     */
    default void assertNotNull(@NotNull String message, @Nullable Object value, @NotNull Method method) {
        if (value == null) {
            throw new AssertionError(description() + " " + message + " Not Null FAILED for " + method);
        }
    }

    /**
     * Description of this guard.
     *
     * @return the description
     */
    @NotNull
    String description();

    /**
     * Helper method for guards that asserts a condition is true.
     *
     * @param condition the condition to check
     * @param method    the method that the assertion relates to
     */
    default void assertTrue(boolean condition, @NotNull Method method) {
        if (!condition) {
            throw new AssertionError(description() + " FAILED for " + method);
        }
    }

    /**
     * Post condition check that is called after the method is executed.
     *
     * @param guarded the guarded object
     * @param method  the method that is being guarded
     * @param args    the arguments being passed in
     * @param result  the result that was returned from the method
     */
    void postCondition(@NotNull Object guarded, @NotNull Method method, @NotNull Object[] args, @NotNull Object result);

    /**
     * Pre condition check that is called before the method is executed.
     *
     * @param guarded the guarded object
     * @param method  the method that is being guarded
     * @param args    the arguments being passed in
     */
    void preCondition(@NotNull Object guarded, @NotNull Method method, @NotNull Object[] args);

}
