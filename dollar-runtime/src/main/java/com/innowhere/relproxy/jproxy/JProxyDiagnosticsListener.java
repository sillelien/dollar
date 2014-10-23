/*
 *
 *  * See: https://github.com/jmarranz
 *  *
 *  * Copyright (c) 2014 Jose M. Arranz (additional work by Neil Ellis)
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.innowhere.relproxy.jproxy;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

/**
 * Is the interface to implement diagnostic listeners to capture compilation errors and warnings.
 *
 * @author Jose Maria Arranz Santamaria
 * @see com.innowhere.relproxy.jproxy.JProxyConfig#setJProxyDiagnosticsListener(com.innowhere.relproxy.jproxy.JProxyDiagnosticsListener)
 */
public interface JProxyDiagnosticsListener {
    /**
     * This method is called when compilation Java code has generated diagnostics.
     *
     * @param diagnostics the list of diagnostics.
     */
    public void onDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics);
}
