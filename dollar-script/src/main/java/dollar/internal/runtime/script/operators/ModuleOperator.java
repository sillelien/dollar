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

package dollar.internal.runtime.script.operators;

import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.script.ModuleResolver;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarParserImpl;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.api.DollarParser;
import org.jetbrains.annotations.NotNull;
import org.jparsec.Token;
import org.jparsec.functors.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static dollar.internal.runtime.script.DollarScriptSupport.currentScope;

public class ModuleOperator implements Map<Token, var> {

    @NotNull
    private static final Logger log = LoggerFactory.getLogger("ModuleOperator");


    @NotNull
    private DollarParser parser;

    public ModuleOperator(@NotNull DollarParser parser) {

        this.parser = parser;
    }

    @NotNull
    @Override
    public var map(@NotNull Token token) {

        Object[] objects= (Object[]) token.value();
        return DollarScriptSupport.createNode(true, "module", parser, token, Collections.emptyList(),
                                              in->{
                                                  String moduleName = ((var) objects[1]).$S();
                                                  String[] parts = moduleName.split(":", 2);
                                                  if (parts.length < 2) {
                                                      throw new IllegalArgumentException("Module " + moduleName + " needs to have a scheme");
                                                  }
                                                  final List<var> params = (List<var>) objects[2];
                                                  HashMap<String, var> paramMap = new HashMap<>();
                                                  if (params != null) {
                                                      for (var param : params) {
                                                          paramMap.put(param.getMetaAttribute(DollarParserImpl.NAMED_PARAMETER_META_ATTR), param);
                                                      }
                                                  }
                                                  System.out.println(params);
                                                  try {

                                                      return ModuleResolver.resolveModule(parts[0])
                                                                     .resolve(parts[1], currentScope(),parser)
                                                                     .pipe(DollarStatic.$(paramMap))
                                                                     ._fix(true);

                                                  } catch (Exception e) {
                                                      return DollarStatic.logAndRethrow(e);
                                                  }

                                              });

    }
}

