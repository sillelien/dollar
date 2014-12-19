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

package me.neilellis.dollar.script;

import com.google.common.collect.Multimap;
import me.neilellis.dollar.var;

import java.util.Map;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface Scope {

    var addErrorHandler(var handler);

    void clear();

    var get(String key, boolean mustFind);

    var get(String key);

    var getConstraint(String key);

    DollarParser getDollarParser();

    void setDollarParser(DollarParser dollarParser);

    String getFile();

    Multimap<String, var> getListeners();

    var getParameter(String key);

    Scope getScopeForKey(String key);

    Scope getScopeForParameters();

    String getSource();

    Map<String, Variable> getVariables();

    var handleError(Throwable t);

    boolean has(String key);

    boolean hasParameter(String key);

    void listen(String key, var listener);

    var notify(String variableName);

    void notifyScope(String key, var value);

    var set(String key, var value, boolean readonly, var constraint, boolean isVolatile, boolean fixed, boolean pure);

    var setParameter(String key, var value);

    void setParent(Scope scope);

}
