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

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
@SuppressWarnings("PointlessBooleanExpression") public class Configuration {
    private final boolean safe = true;
    private final boolean monitor = false;
    private boolean production;
    private boolean debugScope = true;

    public boolean failFast() {
        return true;
    }

    public boolean isDebugScope() {
        return debugScope;
    }

    public void setDebugScope(boolean debugScope) {
        this.debugScope = debugScope;
    }

    public boolean production() {
        return production;
    }

    public boolean wrapForGuards() {
        return production || safe;
    }

    public boolean wrapForMonitoring() {
        return production || monitor;
    }
}
