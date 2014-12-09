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

package me.neilellis.dollar.script.operators;

import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.script.DollarParser;
import me.neilellis.dollar.script.ModuleResolver;
import me.neilellis.dollar.script.Scope;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.functors.Map;

import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class ModuleOperator implements Map<Object[], var> {
    private final Scope scope;

    public ModuleOperator(Scope scope) {this.scope = scope;}

    @Override public var map(Object[] objects) {

        String moduleName = ((var) objects[1]).$S();
        String[] parts = moduleName.split(":", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Module " + moduleName + " needs to have a scheme");
        }
        final List<var> params = (List<var>) objects[2];
        HashMap<String, var> paramMap = new HashMap<>();
        if (params != null) {
            for (var param : params) {
                paramMap.put(param.getMetaAttribute(DollarParser.NAMED_PARAMETER_META_ATTR), param);
            }
        }
        System.out.println(params);
        try {

            return ModuleResolver.resolveModule(parts[0])
                                 .resolve(parts[1],
                                          scope.getDollarParser()
                                               .currentScope())
                                 .pipe(DollarStatic.$(paramMap));

        } catch (Exception e) {
            return DollarStatic.logAndRethrow(e);
        }

    }
}

