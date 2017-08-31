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

package dollar.api;

public class SystemPropertyConfiguration implements Configuration {

    private final boolean debugEvents = Boolean.parseBoolean(System.getProperty("dollar.debug.events", "false"));
    private final boolean debugExecution = Boolean.parseBoolean(System.getProperty("dollar.debug.execution", "false"));
    private final boolean safe = Boolean.parseBoolean(System.getProperty("dollar.safe", "true"));
    private final boolean monitor = Boolean.parseBoolean(System.getProperty("dollar.monitor", "false"));
    private final boolean production = Boolean.parseBoolean(System.getProperty("dollar.production", "false"));
    private final boolean debugScope = Boolean.parseBoolean(System.getProperty("dollar.debug.scope", "false"));
    private final boolean debugParallel = Boolean.parseBoolean(System.getProperty("dollar.debug.parallel", "false"));
    private boolean failFast = Boolean.parseBoolean(System.getProperty("dollar.fail.fast", "false"));


    public SystemPropertyConfiguration() {
    }

    @Override
    public boolean debugScope() {
        return debugScope;
    }

    @Override
    public boolean debugEvents() {
        return debugEvents;
    }

    @Override
    public boolean debugExecution() {
        return debugExecution;
    }

    @Override
    public boolean failFast() {
        return failFast;
    }

    @Override
    public void failFast(boolean failFast) {
        this.failFast = failFast;
    }

    @Override
    public boolean production() {
        return production;
    }

    @Override
    public boolean wrapForGuards() {
        return production || safe;
    }

    @Override
    public boolean wrapForMonitoring() {
        return production || monitor;
    }

    @Override
    public boolean debugParallel() {
        return debugParallel;
    }
}
