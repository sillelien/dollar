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

import dollar.api.VarKey;
import dollar.api.script.DollarParser;
import dollar.api.var;
import dollar.internal.runtime.script.Builtins;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import dollar.internal.runtime.script.parser.SourceCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;

import static dollar.api.types.meta.MetaConstants.OPERATION_NAME;
import static dollar.internal.runtime.script.DollarUtilFactory.util;
import static dollar.internal.runtime.script.parser.Symbols.FUNCTION_NAME_OP;
import static dollar.internal.runtime.script.parser.Symbols.PARAM_OP;

public class ParameterOperator implements Function<Token, Function<? super var, ? extends var>> {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("ParameterOperator");
    @NotNull
    private final DollarParser parser;
    private final boolean pure;


    public ParameterOperator(@NotNull DollarParser parser, boolean pure) {
        this.parser = parser;
        this.pure = pure;
        log.info("Created {} param op", pure ? "pure" : "impure");
    }

    @Nullable
    @Override
    public Function<? super var, ? extends var> apply(@NotNull Token token) {
        List<var> parameters = (List<var>) token.value();
        return lhs -> {
            boolean functionName;
            boolean builtin;
            SourceCode sourceCode = new SourceCode(util().scope(), token);
            boolean isPure = pure;
            String name;
            if (FUNCTION_NAME_OP.name().equals(lhs.metaAttribute(OPERATION_NAME))) {
                String lhsString = lhs.toString();
                builtin = Builtins.exists(lhsString);
                name = "function-" + lhsString + "-" + sourceCode.getShortHash();
                if (pure && builtin && !Builtins.isPure(lhsString)) {
                    throw new DollarScriptException("Cannot call the impure function '" + lhsString + "' in a pure expression.");
                }
                functionName = true;
            } else {
                functionName = false;
                builtin = false;
                name = "parameterized-" + lhs.metaAttribute(OPERATION_NAME) + "-" + sourceCode.getShortHash();
            }

            @SuppressWarnings("ConstantConditions")
            var node = util().node(PARAM_OP, name, pure, parser, token, parameters,
                                   i -> util().inSubScope(true, pure, "param-disposable-scope",
                                                          newScope -> {
                                                              util().addParameterstoCurrentScope(
                                                                      util().scope(),
                                                                      parameters);

                                                              var result;
                                                              if (functionName) {
                                                                  if (builtin) {
                                                                      result = Builtins.execute(
                                                                              lhs.toString(),
                                                                              parameters, pure);
                                                                  } else {
                                                                      result = util().variableNode(
                                                                              pure,
                                                                              VarKey.of(lhs),
                                                                              false, null,
                                                                              token, parser)
                                                                                       .$fix(2,
                                                                                             false);
                                                                  }
                                                              } else {

                                                                  result = lhs.$fix(2, false);
                                                              }
                                                              assert result != null;
                                                              return result;
                                                          }).orElseThrow(() -> new AssertionError("Optional should not be null " +
                                                                                                          "here"))
            );


            //reactive links
            lhs.$listen(i -> node.$notify());
            for (var param : parameters) {
                param.$listen(i -> node.$notify());
            }
            return node;
        };
    }

}
