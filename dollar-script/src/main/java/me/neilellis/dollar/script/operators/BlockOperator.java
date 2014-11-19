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

import me.neilellis.dollar.script.DollarParser;
import me.neilellis.dollar.script.ScriptScope;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.functors.Map;

import java.util.List;

import static me.neilellis.dollar.DollarStatic.$void;
import static me.neilellis.dollar.types.DollarFactory.fromLambda;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class BlockOperator implements Map<List<var>, var> {
    private final ScriptScope scope;
    private DollarParser dollarParser;

    public BlockOperator(DollarParser dollarParser, ScriptScope scope) {
        this.dollarParser = dollarParser;
        this.scope = scope;
    }

    @Override public var map(List<var> l) {
        return fromLambda(delayed -> dollarParser.inScope(scope, newScope -> {
                              if (l.size() > 0) {
                                  for (int i = 0; i < l.size() - 1; i++) {
                                      l.get(i).$S();
                                  }

                                  return l.get(l.size() - 1);
//                        return $(l);
                              } else {
                                  return $void();
                              }
                          })
        );
    }
}
