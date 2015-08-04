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

@SuppressWarnings("PointlessBooleanExpression") public class SystemPropertyConfiguration implements Configuration {

    private boolean safe = Boolean.parseBoolean(System.getProperty("dollar.safe", "true"));
    private boolean monitor = Boolean.parseBoolean(System.getProperty("dollar.monitor", "false"));
    private boolean production = Boolean.parseBoolean(System.getProperty("dollar.production", "false"));
    private boolean debugScope = Boolean.parseBoolean(System.getProperty("dollar.debug.scope", "false"));
    private boolean failFast = true;


    public SystemPropertyConfiguration(boolean safe, boolean monitor, boolean production, boolean debugScope) {
        this.safe = safe;
        this.monitor = monitor;
        this.production = production;
        this.debugScope = debugScope;
    }

    public SystemPropertyConfiguration() {
    }

    @Override public boolean debugScope() {
        return debugScope;
    }

    @Override public boolean failFast() {
        return failFast;
    }

    @Override public void failFast(boolean failFast) {
        this.failFast = failFast;
    }

    @Override public boolean production() {
        return production;
    }

    @Override public boolean wrapForGuards() {
        return production || safe;
    }

    @Override public boolean wrapForMonitoring() {
        return production || monitor;
    }
}
