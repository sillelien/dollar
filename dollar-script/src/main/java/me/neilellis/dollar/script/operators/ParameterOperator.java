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

import me.neilellis.dollar.script.Builtins;
import me.neilellis.dollar.script.DollarParser;
import me.neilellis.dollar.script.ScriptScope;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;
import org.codehaus.jparsec.functors.Map;

import java.util.List;

import static me.neilellis.dollar.DollarStatic.$;
import static me.neilellis.dollar.DollarStatic.fix;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class ParameterOperator implements Map<List<var>, Map<? super var, ? extends var>> {
    private final ScriptScope scope;
    private DollarParser dollarParser;

    public ParameterOperator(DollarParser dollarParser, ScriptScope scope) {
        this.dollarParser = dollarParser;
        this.scope = scope;
    }

    @Override public Map<? super var, ? extends var> map(List<var> rhs) {
        return lhs -> {

            var lambda = DollarFactory.fromLambda(i -> dollarParser.inScope(scope, newScope -> {
                        //Add the special $* value for all the parameters
                        newScope.setParameter("*", $(rhs));
                        int count = 0;
                        for (var param : rhs) {
                            newScope.setParameter(String.valueOf(++count), param);
                            //If the parameter is a named parameter then use the name (set as metadata
                            //on the value).
                            if (param.getMetaAttribute(DollarParser.NAMED_PARAMETER_META_ATTR) != null) {
                                newScope.set(param.getMetaAttribute(DollarParser.NAMED_PARAMETER_META_ATTR), param,
                                             true, null);
                            }
                        }
                        var result;
                        if (lhs.isLambda()) {
                            System.out.println("RESULT: " + lhs._unwrap().getClass());
                            result = fix(lhs, false);
                            System.out.println("RESULT: " + result._unwrap().getClass());
                        } else {
                            String lhsString = lhs.toString();
                            //The lhs is a string, so let's see if it's a builtin function
                            //if not then assume it's a variable.
                            if (Builtins.exists(lhsString)) {
                                result = Builtins.execute(lhsString, rhs, newScope);
                            } else {
                                result = fix(newScope.get(lhsString), false);
                            }
                        }

                        return result;
                    }));
            //reactive links
            lhs.$listen(i -> lambda.$notify());
            for (var param : rhs) {
                param.$listen(i -> lambda.$notify());
            }
            return lambda;
        };
    }
}
