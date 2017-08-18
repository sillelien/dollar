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

import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.Builtins;
import dollar.internal.runtime.script.SourceNodeOptions;
import dollar.internal.runtime.script.api.DollarParser;
import org.jetbrains.annotations.NotNull;
import org.jparsec.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static dollar.internal.runtime.script.DollarScriptSupport.*;
import static java.util.Collections.singletonList;

public class PipeOperator implements Function<Token, Function<var, var>> {

    @NotNull
    private static final Logger log = LoggerFactory.getLogger("PipeOperator");
    @NotNull
    private final DollarParser parser;
    private final boolean pure;


    public PipeOperator(@NotNull DollarParser parser, boolean pure) {

        this.parser = parser;
        this.pure = pure;
    }


    @NotNull
    @Override
    public Function<var, var> apply(@NotNull Token token) {
        var rhs = (var) token.value();
        return lhs -> reactiveNode(
                "pipe", pure, SourceNodeOptions.NEW_SCOPE, parser, token, rhs,
                i -> inSubScope(false, pure, "pipe-runtime",
                                runtimeScope -> {
                                    currentScope().setParameter("1", lhs);
                                    var rhsVal = rhs._fix(false);
                                    if ("function-call".equals(rhs.getMetaAttribute("operation"))) {
                                        log.debug("PIPED TO FUNCTION");
                                        return rhsVal;
                                    } else {
                                        String rhsStr = rhsVal.toString();
                                        log.debug("OPERATION: {}", rhsStr);
                                        if (rhs.getMetaAttribute("__builtin") != null) {
                                            return Builtins.execute(rhsStr,
                                                                    singletonList(lhs), pure);
                                        } else {
                                            //fixed to level 2 as we are executing the function
                                            // in this scope
                                            var variable = variableNode(pure, rhsStr, false, null, token,
                                                                        parser);
                                            assert variable != null;
                                            return variable._fix(2, false);
                                        }
                                    }

                                }));
    }
}
