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
 * Configuration values used by Dollar API and DollarScript
 *
 * @author  <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
@SuppressWarnings("PointlessBooleanExpression") public class Configuration {

    private boolean safe = Boolean.parseBoolean(System.getProperty("dollar.safe", "true"));
    private boolean monitor = Boolean.parseBoolean(System.getProperty("dollar.monitor", "false"));
    private boolean production = Boolean.parseBoolean(System.getProperty("dollar.production", "false"));
    private boolean debugScope = Boolean.parseBoolean(System.getProperty("dollar.debug.scope", "false"));
    private boolean failFast = true;


    public Configuration(boolean safe, boolean monitor, boolean production, boolean debugScope) {
        this.safe = safe;
        this.monitor = monitor;
        this.production = production;
        this.debugScope = debugScope;
    }

    public Configuration() {
    }

    /**
     * Returns true if debug messages will be produced for scope activity (i.e. variables in DollarScript)
     *
     * @return true if the scope is debugged
     */
    public boolean debugScope() {
        return debugScope;
    }

    /**
     * Returns true if Dollar should throw exceptions rather than return errors.
     *
     * @return true if we're failing fast
     */
    public boolean failFast() {
        return failFast;
    }

    public void failFast(boolean failFast) {
        this.failFast = failFast;
    }

    /**
     * Returns true if we're running in a production environment
     *
     * @return true if in production
     */
    public boolean production() {
        return production;
    }

    /**
     * Returns true if values are being wrapped with safety guards to avoid nulls etc. This is mostly useful for beta
     * releases to identify problems.
     *
     * @return true if objects are being wrapped.
     */
    public boolean wrapForGuards() {
        return production || safe;
    }

    /**
     * True if {@link var} objects are being wrapped for monitoring. This includes metrics and trace
     * analysis.
     *
     * @return true if
     * objects are being wrapped.
     */
    public boolean wrapForMonitoring() {
        return production || monitor;
    }
}
