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

package me.neilellis.dollar.script.api;

import com.beust.jcommander.Parameter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class ParserOptions {

    @Parameter(names = "--profile", description = "The configuration profile to use")
    private String profile;
    @Parameter(names = "--safe", description = "Provide extra safety from errors")
    private boolean safe;
    @Parameter(names = "--usage", description = "Record usage metrics")
    private boolean monitor;
    @Parameter(names = "--tolerate-errors", description = "Continue execution after an error")
    private boolean tolerateErrors;
    @Parameter(names = "--debug-scope")
    private boolean debugScope;
    @Parameter(description = "The file to execute")
    private List<File> files;
    private boolean production;

    @NotNull public File getFile() {
        return files.get(0);
    }

    public boolean isDebugScope() {
        return debugScope;
    }

    public boolean isMonitor() {
        return monitor;
    }

    public boolean isProduction() {
        return production;
    }

    public boolean isSafe() {
        return safe;
    }

    public boolean isTolerateErrors() {
        return tolerateErrors;
    }

    public void setTolerateErrors(boolean tolerateErrors) {
        this.tolerateErrors = tolerateErrors;
    }
}
