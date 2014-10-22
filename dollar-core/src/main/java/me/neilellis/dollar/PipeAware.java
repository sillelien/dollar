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

package me.neilellis.dollar;

import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface PipeAware {
    @NotNull
    var $pipe(@NotNull String label, @NotNull String js);

    default
    @NotNull
    var $pipe(@NotNull Pipeable pipe) {
        return $pipe("anon", pipe);
    }

    @NotNull
    var $pipe(@NotNull String label, @NotNull Pipeable pipe);

    @NotNull
    var $pipe(@NotNull String js);

    @NotNull
    var $pipe(@NotNull Class<? extends Script> clazz);

    @Deprecated
    var eval(String label, DollarEval eval);

    @Deprecated
    var eval(DollarEval eval);

    //  /**
//   * If the class has a method $ call($ in) then that method is called otherwise converts this object to a set of string
//   * parameters and passes them to the main method of the clazz. <p> NB: This is the preferred way to pass values
//   * between classes as it preserves the stateless nature. Try where possible to maintain a stateless context to
//   * execution. </p>
//   *
//   * @param clazz the class to pass this to.
//   */
    @Deprecated
    var eval(Class clazz);
}
