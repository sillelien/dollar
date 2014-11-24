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

import me.neilellis.dollar.script.exceptions.DollarScriptException;
import me.neilellis.dollar.script.exceptions.VariableNotFoundException;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarScriptSupport {


    public static var wrapReactiveUnary(ScriptScope scope, String source, var lhs, Callable<var> callable) {
        final var lambda = toLambda(scope, source, callable);
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }

    private static var toLambda(ScriptScope scope, String source, Callable<var> callable) {
        return DollarFactory.fromLambda(i -> {
            try {
                return callable.call();
            } catch (AssertionError e) {
                return scope.getDollarParser().getErrorHandler().handle(scope, source, e);
            } catch (DollarScriptException e) {
                return scope.getDollarParser().getErrorHandler().handle(scope, source, e);
            } catch (Exception e) {
                return scope.getDollarParser().getErrorHandler().handle(scope, source, e);
            }
        });
    }

    public static var wrapReactiveBinary(ScriptScope scope, String source, var lhs, var rhs, Callable<var> callable) {
        final var lambda = toLambda(scope, source, callable);

        rhs.$listen(i -> lambda.$notify());
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }

    public static var wrapUnary(ScriptScope scope, String source, Callable<var> callable) {
        return toLambda(scope, source, callable);
    }

    public static var wrapBinary(ScriptScope scope, String source, Callable<var> callable) {
        return toLambda(scope, source, callable);
    }

    public static var getVariable(ScriptScope scope, String key, boolean numeric, var defaultValue) {

        var lambda = DollarFactory.fromLambda(v -> {
            try {
                List<ScriptScope> scopes = new ArrayList<ScriptScope>(scope.getDollarParser().scopes());
                Collections.reverse(scopes);
                for (ScriptScope scriptScope : scopes) {
                    if (numeric) {
                        if (scriptScope.hasParameter(key)) {
                            return scriptScope.getParameter(key);
                        }
                    } else {
                        if (scriptScope.has(key)) {
                            return scriptScope.get(key);
                        }
                    }
                }
            } catch (AssertionError e) {
                return scope.getDollarParser().getErrorHandler().handle(scope, key, e);
            } catch (DollarScriptException e) {
                return scope.getDollarParser().getErrorHandler().handle(scope, key, e);
            } catch (Exception e) {
                return scope.getDollarParser().getErrorHandler().handle(scope, key, e);
            }
            if (numeric) {
                return scope.getParameter(key);
            }
            if (!scope.has(key)) {
                if (defaultValue != null) {
                    return defaultValue;
                } else {
                    throw new VariableNotFoundException(key);
                }
            }
            return scope.get(key);
        });
        scope.listen(key, lambda);
        lambda.setMetaAttribute("variable", key);
        return lambda;

    }
}
