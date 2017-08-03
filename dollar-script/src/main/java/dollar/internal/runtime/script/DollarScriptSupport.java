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

package dollar.internal.runtime.script;

import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.Scope;
import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import dollar.internal.runtime.script.api.exceptions.VariableNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static com.sillelien.dollar.api.DollarStatic.$;

public class DollarScriptSupport {

    private static final Logger log = LoggerFactory.getLogger(DollarScriptSupport.class);
    private static final ThreadLocal<List<Scope>> scopes = new ThreadLocal<List<Scope>>() {
        @NotNull
        @Override
        protected List<Scope> initialValue() {
            ArrayList<Scope> list = new ArrayList<>();
            return list;
        }
    };


    public static List<Scope> scopes() {
        return scopes.get();
    }

    private static void addScope(Scope scope) {
        scopes.get().add(scope);
    }

    private static Scope endScope() {
        return scopes.get().remove(scopes.get().size() - 1);
    }

    @NotNull
    public static var createReactiveNode(@NotNull Callable<var> callable, Token token,
                                         String operation,
                                         @NotNull var lhs, DollarParser parser) {

        return createReactiveNode(callable, new SourceSegmentValue(currentScope(), token), operation, lhs, parser);
    }

    @NotNull
    public static var createReactiveNode(@NotNull Callable<var> callable, SourceSegment source,
                                         String operation,
                                         @NotNull var lhs, DollarParser parser) {

        final var lambda = createNode(currentScope(), callable, source, Arrays.asList(lhs), operation, parser);
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }


    @NotNull
    public static var createNode(@Nullable Scope scope, @NotNull Callable<var> callable, SourceSegment source,
                                 List<var> inputs,
                                 String operation, DollarParser parser) {
        if (scope == null) {
            throw new NullPointerException();
        }
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarSource(i -> callable.call(), scope, source, inputs, operation, parser)));
    }

    @NotNull
    public static var createNode(@NotNull Callable<var> callable, Token token,
                                 List<var> inputs,
                                 String operation, DollarParser parser) {
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarSource(i -> callable.call(), currentScope(), new SourceSegmentValue(currentScope(), token), inputs, operation, parser)));
    }

    @NotNull
    public static var createReactiveNode(@NotNull Callable<var> callable, SourceSegment source, String operation,
                                         @NotNull var lhs,
                                         @NotNull var rhs, DollarParser parser) {
        final var lambda = createNode(currentScope(), callable, source, Arrays.asList(lhs, rhs), operation, parser);

        rhs.$listen(i -> lambda.$notify());
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }

    @NotNull
    public static var createReactiveNode(@NotNull Callable<var> callable, Token token, String operation,
                                         @NotNull var lhs,
                                         @NotNull var rhs, DollarParser parser) {
        final var lambda = createNode(currentScope(), callable, new SourceSegmentValue(currentScope(), token), Arrays.asList(lhs, rhs), operation, parser);

        rhs.$listen(i -> lambda.$notify());
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }


    @NotNull
    public static var createNode(@NotNull Token token, @NotNull Pipeable pipeable, @NotNull List<var> inputs,
                                 @NotNull String operation, @NotNull DollarParser parser) {
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarSource(pipeable::pipe, currentScope(), new SourceSegmentValue(currentScope(), token), inputs, operation, parser)));
    }


    @NotNull
    public static Scope currentScope() {
        return scopes.get().get(scopes.get().size() - 1);
    }

    @SuppressWarnings("ThrowFromFinallyBlock")
    public static <T> T inScope(boolean pure, @NotNull String scopeName, @NotNull Function<Scope, T> r) {
        return inScope(currentScope(), pure, scopeName, r);
    }

    public static <T> T inScope(Scope parent, boolean pure, @NotNull String scopeName, @NotNull Function<Scope, T> r) {
        Scope newScope;
        if (pure) {
            newScope = new PureScope(parent, parent.getSource(), scopeName, parent.getFile());
        } else {
            if ((parent instanceof PureScope)) {
                throw new IllegalStateException("trying to switch to an impure scope in a pure scope.");
            }
            newScope = new ScriptScope(parent, parent.getFile(), parent.getSource(), scopeName);
        }
        addScope(parent);
        addScope(newScope);
        try {
            return r.apply(newScope);
        } finally {
            Scope poppedScope = endScope();
            if (poppedScope != newScope) {
                throw new IllegalStateException("Popped wrong scope");
            }
            final Scope poppedScope2 = endScope();
            if (poppedScope2 != parent) {
                throw new IllegalStateException("Popped wrong scope");
            }
        }
    }


    @SuppressWarnings("ThrowFromFinallyBlock")
    public static <T> T inScope(Scope scope, @NotNull Function<Scope, T> r) throws Exception {

        log.debug("CHANGING SCOPE TO " + scope + " scopes size is " + scopes.get().size());
        boolean addedDynamicScope = scopes.get().size() > 0;
        if (addedDynamicScope) {
            addScope(currentScope());
        }
        addScope(scope);
        try {
            return r.apply(scope);
        } finally {
            Scope poppedScope = endScope();
            if (poppedScope != scope) {
                throw new IllegalStateException("Popped wrong scope");
            }
            if (addedDynamicScope) {
                final Scope poppedScope2 = endScope();
                if (poppedScope2 != currentScope()) {
                    throw new IllegalStateException("Popped wrong scope");
                }
            }
        }
    }

    @Nullable
    public static var getVariable(boolean pure, String key, boolean numeric,
                                  @Nullable var defaultValue,
                                  @NotNull Token token, DollarParser parser) {
//       if(pure && !(scope instanceof  PureScope)) {
//           throw new IllegalStateException("Attempting to get a pure variable in an impure scope");
//       }
        SourceSegment source = new SourceSegmentValue(currentScope(), token);

        var lambda[] = new var[1];
        lambda[0] = createNode(() -> {
            Scope scope = (Scope) lambda[0].getMetaObject("scope");
            scope.listen(key, lambda[0]);
            log.debug("LOOKUP " + key + " " + scope + " " + currentScope());
            if (numeric) {
                if (scope.hasParameter(key)) {
                    return scope.getParameter(key);
                }
            } else {
                if (scope.has(key)) {
                    return scope.get(key);
                }
            }
            try {
                List<Scope> scopes = new ArrayList<>(DollarScriptSupport.scopes.get());
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
                return parser.getErrorHandler().handle(scope, null, e);
            } catch (DollarScriptException e) {
                return parser.getErrorHandler().handle(scope, null, e);
            } catch (Exception e) {
                return parser.getErrorHandler().handle(scope, null, e);
            }
            if (numeric) {
                return scope.getParameter(key);
            }

            if (defaultValue != null) {
                return defaultValue;
            } else {
                throw new VariableNotFoundException(key, scope);
            }
        }, token, $(key).$list().toVarList().mutable(), "get-variable-" + key + "-" + source.getShortHash(), parser);
        lambda[0].setMetaAttribute("variable", key);
        return lambda[0];

    }

    public static Scope getRootScope() {
        return scopes.get().get(0);
    }

    public static void setRootScope(ScriptScope rootScope) {
        scopes.get().clear();
        scopes.get().add(0, rootScope);
    }

    public static var setVariable(Scope scope, String key, var value, boolean readonly, var useConstraint, String useSource, boolean isVolatile, boolean fixed, boolean pure, boolean decleration, Token token, DollarParser parser) {

        SourceSegment source = new SourceSegmentValue(currentScope(), token);
        boolean numeric = key.matches("^\\d+$");
        var lambda[] = new var[1];
        lambda[0] = createNode(() -> {
            scope.listen(key, lambda[0]);
            if (scope.has(key)) {
                log.debug("SETTING " + key + " " + scope + " " + currentScope());
                return scope.get(key);
            }
            try {
                List<Scope> scopes = new ArrayList<>(DollarScriptSupport.scopes.get());
                Collections.reverse(scopes);
                for (Scope scriptScope : scopes) {
                    if (!(scriptScope instanceof PureScope) && pure) {
                        log.debug("Skipping " + scriptScope);
                    }

                    if (scriptScope.has(key)) {
                        if (decleration) {
                            throw new DollarScriptException("Variable " + key + " already defined", value);
                        } else {
                            log.debug("SETTING " + key + " " + scope + " " + currentScope());
                            return scriptScope.set(key, value, readonly, useConstraint, useSource, isVolatile, fixed, pure);
                        }
                    }

                }
            } catch (AssertionError e) {
                return parser.getErrorHandler().handle(scope, null, e);
            } catch (DollarScriptException e) {
                return parser.getErrorHandler().handle(scope, null, e);
            } catch (Exception e) {
                return parser.getErrorHandler().handle(scope, null, e);
            }
            if (numeric) {
                return scope.getParameter(key);
            }

            if (decleration) {
                return scope.set(key, value, readonly, useConstraint, useSource, isVolatile, fixed, pure);
            } else {
                throw new VariableNotFoundException(key, scope);
            }
        }, token, $(key).$list().toVarList().mutable(), "get-variable-" + key + "-" + source.getShortHash(), parser);
        lambda[0].setMetaAttribute("variable", key);
        return lambda[0];
    }

    @NotNull
    public static var constrain(Scope scope, @NotNull var rhs, var constraint, String source) {
//        System.err.println("(" + source + ") " + rhs.$type().constraint());
        if (!Objects.equals(rhs._constraintFingerprint(), source)) {
            if (rhs._constraintFingerprint() != null && !rhs._constraintFingerprint().isEmpty()) {
                scope.handleError(new DollarScriptException("Trying to assign an invalid constrained variable " + rhs._constraintFingerprint() + " vs " + source, rhs));
            }
        } else {
            if (rhs._constraintFingerprint() != null && !rhs._constraintFingerprint().isEmpty()) {
//                System.err.println("Fingerprint: " + rhs.$type().constraint());
            }
        }
        return rhs._constrain(constraint, source);
    }
}
