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

import java.util.Arrays;
import java.util.List;

/**
 * A parent class for writing cool Dollar scripts
 *
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class Script extends DollarStatic {

    public static Class<? extends Script> $THIS;
    protected static List<String> args;
    private static Script $this;

    @NotNull
    protected var in = DollarStatic.threadContext.get() != null ? DollarStatic.threadContext.get().getPassValue() : $();
    protected var out;

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        Script.args = Arrays.asList(args);
        $run(() -> {
            try {
                $this = $THIS.newInstance();
            } catch (@NotNull InstantiationException | IllegalAccessException e) {
                throw new Error(e.getCause());
            }
        });
    }


    @NotNull
    public var result() {
        return out != null ? out : in;
    }
}
