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

package dollar.internal.runtime.script.api;

import com.beust.jcommander.Parameter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class ParserOptions {

    @NotNull
    @Parameter(names = "--profile", description = "The configuration profile to use")
    private String profile;

    @Parameter(names = "--safe", description = "Provide extra safety from errors")
    private boolean safe;

    @Parameter(names = "--usage", description = "Record usage metrics")
    private boolean monitor;

    @Parameter(names = "--server", description = "Run in server mode")
    private boolean server;

    @Parameter(names = {"--fail-fast", "-e"}, description = "Fail on error")
    private boolean failFast;

    @Parameter(names = "--debug-scope", description = "Debug scope usage")
    private boolean debugScope;

    @NotNull
    @Parameter(description = "The file to execute")
    private List<File> files;

    private boolean production;
    @Parameter(names = "--debug-parallel", description = "Debug parallel execution and parallel scopes")
    private boolean debugParallel;
    @Parameter(names = "--debug-events", description = "Debug listen/notify internal events")
    private boolean debugEvents;
    @Parameter(names = "--debug-execution", description = "Debug execution (fork, immediate, schedule etc.)")
    private boolean debugExecution;

    @NotNull
    public File getFile() {
        return files.get(0);
    }

    public boolean isDebugScope() {
        return debugScope && !isProduction();
    }

    public boolean isProduction() {
        return "prod".equalsIgnoreCase(profile);
    }

    public boolean isDevelopment() {
        return "dev".equalsIgnoreCase(profile);
    }

    public boolean isMonitor() {
        return monitor;
    }

    public boolean isSafe() {
        return safe;
    }

    public boolean isServer() {
        return server;
    }

    public boolean isTest() {
        return "test".equalsIgnoreCase(profile);
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public boolean isDebugParallel() {
        return debugParallel;
    }

    public void setDebugParallel(boolean debugParallel) {
        this.debugParallel = debugParallel;
    }

    public boolean isDebugEvents() {
        return debugEvents;
    }

    public void setDebugEvents(boolean debugEvents) {
        this.debugEvents = debugEvents;
    }

    public boolean isDebugExecution() {
        return debugExecution;
    }

    public void setDebugExecution(boolean debugExecution) {
        this.debugExecution = debugExecution;
    }
}
