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

import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.script.exceptions.DollarScriptException;
import me.neilellis.dollar.script.exceptions.VariableNotFoundException;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarScriptSupport {

    private static final Logger log = LoggerFactory.getLogger(DollarScriptSupport.class);

    public static var wrapReactiveUnary(Scope scope, var lhs, Callable<var> callable) {
        if (scope == null) {
            throw new NullPointerException();
        }
        final var lambda = toLambda(scope, callable);
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }

    private static var toLambda(Scope scope, Callable<var> callable) {
        if (scope == null) {
            throw new NullPointerException();
        }
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarSource(i -> callable.call(), scope)));
    }

    public static var wrapReactiveBinary(Scope scope, var lhs, var rhs, Callable<var> callable) {
        final var lambda = toLambda(scope, callable);

        rhs.$listen(i -> lambda.$notify());
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }

    public static var wrapUnary(Scope scope, Callable<var> callable) {
        return toLambda(scope, callable);
    }

    public static var wrapBinary(Scope scope, Callable<var> callable) {
        return toLambda(scope, callable);
    }

    public static var getVariable(boolean pure, Scope scope, String key, boolean numeric, var defaultValue) {
//       if(pure && !(scope instanceof  PureScope)) {
//           throw new IllegalStateException("Attempting to get a pure variable in an impure scope");
//       }
        var lambda = toLambda(scope, () -> {
            if (scope.has(key)) {
                return scope.get(key);
            }
            try {
                List<Scope> scopes = new ArrayList<>(scope.getDollarParser().scopes());
                Collections.reverse(scopes);
                for (Scope scriptScope : scopes) {
                    if (!(scriptScope instanceof PureScope) && pure) {
                        log.debug("Skipping " + scriptScope);
                    }
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
                return scope.getDollarParser().getErrorHandler().handle(scope, null, e);
            } catch (DollarScriptException e) {
                return scope.getDollarParser().getErrorHandler().handle(scope, null, e);
            } catch (Exception e) {
                return scope.getDollarParser().getErrorHandler().handle(scope, null, e);
            }
            if (numeric) {
                return scope.getParameter(key);
            }

                if (defaultValue != null) {
                    return defaultValue;
                } else {
                    throw new VariableNotFoundException(key, scope);
                }
        });
        scope.listen(key, lambda);
        lambda.setMetaAttribute("variable", key);
        return lambda;

    }
}
