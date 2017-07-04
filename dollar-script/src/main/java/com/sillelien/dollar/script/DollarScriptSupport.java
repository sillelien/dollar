/*
 * Copyright (c) 2014-2015 Neil Ellis
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

package com.sillelien.dollar.script;

import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.var;
import com.sillelien.dollar.script.api.Scope;
import com.sillelien.dollar.script.api.exceptions.DollarScriptException;
import com.sillelien.dollar.script.api.exceptions.VariableNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static com.sillelien.dollar.api.DollarStatic.$;

public class DollarScriptSupport {

    private static final Logger log = LoggerFactory.getLogger(DollarScriptSupport.class);

    @NotNull
    public static var wrapReactive(@Nullable Scope scope, @NotNull Callable<var> callable, SourceSegment source,
                                   String operation,
                                   @NotNull var lhs) {
        if (scope == null) {
            throw new NullPointerException();
        }
        final var lambda = toLambda(scope, callable, source, Arrays.asList(lhs), operation);
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }


    @NotNull public static var toLambda(@Nullable Scope scope, @NotNull Callable<var> callable, SourceSegment source,
                                        List<var> inputs,
                               String operation) {
        if (scope == null) {
            throw new NullPointerException();
        }
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarSource(i -> callable.call(), scope, source, inputs, operation)));
    }

    @NotNull
    public static var wrapReactive(Scope scope, @NotNull Callable<var> callable, SourceSegment source, String operation,
                                   @NotNull var lhs,
                                   @NotNull var rhs) {
        final var lambda = toLambda(scope, callable, source, Arrays.asList(lhs, rhs), operation);

        rhs.$listen(i -> lambda.$notify());
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }

    @NotNull
    public static var wrapLambda(SourceSegment source, Scope scope, @NotNull Pipeable pipeable, List<var> inputs,
                                 String operation) {
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarSource(pipeable::pipe, scope, source, inputs, operation)));
    }

    @Nullable
    public static var getVariable(boolean pure, @NotNull Scope scope, String key, boolean numeric,
                                  @Nullable var defaultValue,
                                  @NotNull SourceSegment source) {
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
        }, source, $(key).$list().toVarList().mutable(), "get-variable-" + key + "-" + source.getShortHash());
        scope.listen(key, lambda);
        lambda.setMetaAttribute("variable", key);
        return lambda;

    }
}
