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

public interface Configuration {
    /**
     * Returns true if debug messages will be produced for scope activity (i.e. variables in DollarScript)
     *
     * @return true if the scope is debugged
     */
    boolean debugScope();

    /**
     * Returns true if Dollar should throw exceptions rather than return errors.
     *
     * @return true if we're failing fast
     */
    boolean failFast();

    void failFast(boolean failFast);

    /**
     * Returns true if we're running in a production environment
     *
     * @return true if in production
     */
    boolean production();

    /**
     * Returns true if values are being wrapped with safety guards to avoid nulls etc. This is mostly useful for beta
     * releases to identify problems.
     *
     * @return true if objects are being wrapped.
     */
    boolean wrapForGuards();

    /**
     * True if {@link var} objects are being wrapped for monitoring. This includes metrics and trace
     * analysis.
     *
     * @return true if
     * objects are being wrapped.
     */
    boolean wrapForMonitoring();
}
