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
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;

import java.util.List;
import java.util.function.Function;

import static com.sillelien.dollar.api.DollarStatic.$;
import static com.sillelien.dollar.api.types.meta.MetaConstants.IMPURE;
import static com.sillelien.dollar.api.types.meta.MetaConstants.OPERATION_NAME;
import static dollar.internal.runtime.script.DollarParserImpl.NAMED_PARAMETER_META_ATTR;
import static dollar.internal.runtime.script.DollarScriptSupport.*;
import static dollar.internal.runtime.script.SourceNodeOptions.SCOPE_WITH_CLOSURE;
import static dollar.internal.runtime.script.parser.Symbols.FUNCTION_NAME_OP;
import static dollar.internal.runtime.script.parser.Symbols.PARAM_OP;

public class ParameterOperator implements Function<Token, Function<? super var, ? extends var>> {
    @NotNull
    private final DollarParser parser;
    private final boolean pure;


    public ParameterOperator(@NotNull DollarParser parser, boolean pure) {
        this.parser = parser;
        this.pure = pure;
    }

    @Nullable
    @Override
    public Function<? super var, ? extends var> apply(@NotNull Token token) {
        @SuppressWarnings("unchecked") List<var> parameters = (List<var>) token.value();
        return lhs -> {
            boolean functionName;
            boolean builtin;

            if (FUNCTION_NAME_OP.name().equals(lhs.metaAttribute(OPERATION_NAME))) {
                String lhsString = lhs.toString();
                builtin = Builtins.exists(lhsString);
                if (pure && builtin && !Builtins.isPure(lhsString)) {
                    throw new DollarScriptException("Cannot call the impure function '" + lhsString + "' in a pure expression.");
                }
                functionName = true;
            } else {
                functionName = false;
                builtin = false;
            }

            var node = node("parameter", pure, SCOPE_WITH_CLOSURE,
                            parameters, token, parser, i -> {
                        //Add the special $* value for all the parameters
                        currentScope().setParameter("*", $(parameters));
                        int count = 0;
                        for (var parameter : parameters) {
                            currentScope().setParameter(String.valueOf(++count), parameter);

                            //If the parameter is a named parameter then use the name (set as metadata on the value).
                            String paramMetaAttribute = parameter.metaAttribute(NAMED_PARAMETER_META_ATTR);
                            if (paramMetaAttribute != null) {
                                currentScope().set(paramMetaAttribute, parameter, true, null,
                                                   null, false, false, !parameter.hasMeta(IMPURE));
                            }
                        }
                        var result;
                        if (functionName) {
                            String lhsString = lhs.toString();
                            result = builtin ? Builtins.execute(lhsString, parameters, pure)
                                             : variableNode(pure, lhsString, false, null, token, parser)
                                                       .$fix(2,
                                                             false);
                        } else {
                            result = lhs.$fix(2, false);
                        }

                        return result;
                    },
                            PARAM_OP);

            //reactive links
            lhs.$listen(i -> node.$notify());
            for (var param : parameters) {
                param.$listen(i -> node.$notify());
            }
            return node;
        };
    }

}
