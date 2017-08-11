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
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.Builtins;
import dollar.internal.runtime.script.DollarParserImpl;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.SourceNodeOptions;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.jparsec.functors.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.sillelien.dollar.api.DollarStatic.$;
import static dollar.internal.runtime.script.DollarScriptSupport.createNode;
import static dollar.internal.runtime.script.DollarScriptSupport.currentScope;

public class ParameterOperator implements Map<Token, Map<? super var, ? extends var>> {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("ParameterOperator");
    @NotNull
    private final DollarParser dollarParser;
    private final boolean pure;


    public ParameterOperator(@NotNull DollarParser dollarParser, boolean pure) {
        this.dollarParser = dollarParser;
        this.pure = pure;
    }

    @Nullable
    @Override
    public Map<? super var, ? extends var> map(@NotNull Token token) {
        List<var> rhs = (List<var>) token.value();
        return lhs -> {
            boolean functionName;
            boolean builtin;
            log.debug(lhs.getMetaAttribute("operation"));
            if ("function-name".equals(lhs.getMetaAttribute("operation"))) {
                String lhsString = lhs.toString();
                log.debug("BUILTIN?: " + lhsString);
                if (Builtins.exists(lhsString)) {
                    log.debug("BUILTIN: " + lhsString);
                    builtin = true;
                } else {
                    builtin = false;
                }
                if (pure && Builtins.exists(lhsString) && !Builtins.isPure(lhsString)) {
                    throw new DollarScriptException(
                                                           "Cannot call the impure function '" + lhsString + "' in a pure expression.");
                }
                functionName = true;
            } else {
                functionName = false;
                builtin = false;
            }

            String constraintSource = null;
            var lambda = createNode("parameter", SourceNodeOptions.SCOPE_WITH_CLOSURE, dollarParser, token, rhs,
                                    new Function(rhs, lhs, token, constraintSource, functionName, builtin)
            );
            //reactive links
            lhs.$listen(i -> lambda.$notify());
            for (var param : rhs) {
                param.$listen(i -> lambda.$notify());
            }
            return lambda;
        };
    }

    private class Function implements Pipeable {
        @NotNull
        private final List<var> rhs;
        @NotNull
        private final var lhs;
        @NotNull
        private final Token token;
        @Nullable
        private final String constraintSource;
        private boolean functionName;
        private boolean builtin;

        public Function(@NotNull List<var> rhs,
                        @NotNull var lhs,
                        @NotNull Token token,
                        @Nullable String constraintSource, boolean functionName, boolean builtin) {
            this.rhs = rhs;
            this.lhs = lhs;
            this.token = token;
            this.constraintSource = constraintSource;
            this.functionName = functionName;
            this.builtin = builtin;
        }

        @NotNull
        @Override
        public var pipe(var... args) {
            //Add the special $*
            // value for all the
            // parameters
            currentScope().setParameter(
                    "*",
                    $(rhs));
            int count = 0;
            for (var param : rhs) {
                currentScope().setParameter(String.valueOf(++count), param);
                //If the parameter is a named parameter then use the name (set as metadata on the value).
                if (param.getMetaAttribute(DollarParserImpl.NAMED_PARAMETER_META_ATTR) != null) {
                    currentScope().set(
                            param.getMetaAttribute(DollarParserImpl.NAMED_PARAMETER_META_ATTR),
                            param, true, null,
                            constraintSource, false, false, pure);
                }
            }
            var result;
            if (!functionName) {
                result = lhs._fix(2, false);
            } else {
                String lhsString = lhs.toString();
                //The lhs is a
                // string, so
                // let's
                // see if it's a
                // builtin function
                //if not then
                // assume
                // it's a variable.
                if (builtin) {
                    result = Builtins.execute(lhsString, rhs, pure);
                } else {
                    final var valueUnfixed = DollarScriptSupport.getVariable(
                            pure, lhsString, false, null, token, dollarParser);
                    result = valueUnfixed._fix(2, false);
                }
            }

            return result;
        }
    }
}
