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
public class SimpleErrorLogger implements ErrorLogger {
    @Override
    public void log(String errorMessage) {
      DollarStatic.log("ERROR: "+errorMessage);
    }

    @Override
    public void log(@NotNull Throwable error) {
        DollarStatic.log("ERROR: "+error.getMessage());
    }

    @Override
    public void log() {
        DollarStatic.log("ERROR");
    }

    @Override
    public void log(String errorMessage, var.ErrorType type) {
        DollarStatic.log(type+" ERROR: "+errorMessage);
    }
}
