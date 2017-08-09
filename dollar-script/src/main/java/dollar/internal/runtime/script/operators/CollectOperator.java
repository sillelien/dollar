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

import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.Scope;
import dollar.internal.runtime.script.api.exceptions.VariableNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jparsec.Token;
import org.jparsec.functors.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import static com.sillelien.dollar.api.DollarStatic.$;
import static com.sillelien.dollar.api.DollarStatic.$void;
import static dollar.internal.runtime.script.DollarScriptSupport.*;

public class CollectOperator implements Map<Token, var> {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("CollectOperator");
    @NotNull
    private final DollarParser parser;
    private final boolean pure;

    public CollectOperator(@NotNull DollarParser dollarParser, boolean pure) {
        this.parser = dollarParser;
        this.pure = pure;
    }

    @NotNull
    @Override

    public var map(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();
        var variable = (var) objects[0];
        Object until = objects[1];
        Object unless = objects[2];
        Object loop = objects[3];
        log.debug("Listening to " + variable.getMetaObject("operation"));
        log.debug("Listening to " + variable._source().getSourceMessage());
        String varName = variable.getMetaAttribute("variable");


        String id = UUID.randomUUID().toString();
        return createNode(true, "collect", parser, token, Collections.<var>singletonList(variable),
                          (var... in) -> {
                              Scope scopeForVar = DollarScriptSupport.getScopeForVar(pure, varName,
                                                                                     false, null);
                              if (scopeForVar == null) {
                                  throw new VariableNotFoundException(varName, currentScope());
                              }


                              scopeForVar.listen(varName, id, new Pipeable() {
                                  @NotNull
                                  final int[] count = new int[]{-1};
                                  @NotNull
                                  final ArrayList<var> collected = new ArrayList<>();

                                  @NotNull
                                  @Override
                                  public var pipe(var... in2) throws Exception {
                                      var value = in2[1]._fixDeep();
                                      count[0]++;
                                      log.debug("Count is " + count[0] + " value is " + value);
                                      inSubScope(true, pure, "collect-body",
                                                 ns -> {
                                                     ns.setParameter("count", $(count[0]));
                                                     ns.setParameter("it", value);
                                                     //noinspection StatementWithEmptyBody
                                                     if (unless instanceof var && ((var) unless).isTrue()) {
                                                         log.debug("Skipping " + value);
                                                     } else {
                                                         log.debug("Adding " + value);
                                                         collected.add(value);
                                                     }
                                                     var returnValue = $void();
                                                     ns.setParameter("collected", DollarFactory.fromList(collected));
                                                     log.debug("Collected " + DollarFactory.fromList(collected));
                                                     final boolean endValue = until instanceof var && ((var) until).isTrue();
                                                     if (endValue) {
                                                         returnValue = ((var) loop)._fixDeep();
                                                         collected.clear();
                                                         count[0] = -1;
                                                         log.debug("Return value  " + returnValue);
                                                     }
                                                     return returnValue;
                                                 });
                                      return $void();

                                  }
                              });
                              return $void();

                          });


    }


}
