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

import static com.sillelien.dollar.api.DollarStatic.$;

public class DollarScriptSupport {

    @NotNull
    private static final Logger log = LoggerFactory.getLogger(DollarScriptSupport.class);
    @NotNull
    private static final ThreadLocal<List<Scope>> scopes = new ThreadLocal<List<Scope>>() {
        @NotNull
        @Override
        protected List<Scope> initialValue() {
            ArrayList<Scope> list = new ArrayList<>();
            list.add(new ScriptScope("thread-" + Thread.currentThread().getId(), false));
            return list;
        }
    };


    @NotNull
    public static List<Scope> scopes() {
        return scopes.get();
    }

    private static String indent(int i) {
        StringBuilder b = new StringBuilder();
        for (int j = 0; j < i; j++) {
            b.append(" ");
        }
        return b.toString();
    }

    private static void addScope(boolean runtime, @NotNull Scope scope) {
        boolean newScope = scopes.get().size() == 0 || !scope.equals(currentScope());
        scopes.get().add(scope);
        if (DollarStatic.getConfig().debugScope()) {
            log.info(indent(scopes.get().size() - 1) + (runtime ? "**** " : "") + "BEGIN " + scope);
        }

    }

    @NotNull
    private static Scope endScope(boolean runtime) {

        Scope remove = scopes.get().remove(scopes.get().size() - 1);
        if (DollarStatic.getConfig().debugScope()) {

            log.info(indent(scopes.get().size()) + (runtime ? "**** " : "") + "END:  " + remove);

        }

        return remove;
    }

    @NotNull
    public static var createReactiveNode(@NotNull String operation,
                                         @NotNull DollarParser parser,
                                         @NotNull Token token,
                                         @NotNull var lhs, @NotNull Pipeable callable) {

        return createReactiveNode(operation, new SourceSegmentValue(currentScope(), token), parser,
                                  lhs, callable
        );
    }

    @NotNull
    public static var createReactiveNode(@NotNull String operation,
                                         @NotNull SourceSegment source,
                                         @NotNull DollarParser parser,
                                         @NotNull var lhs,
                                         @NotNull Pipeable callable) {

        final var lambda = createNode(operation, parser, currentScope(), source,
                                      Collections.singletonList(lhs),
                                      callable
        );
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }


    @NotNull
    public static var createNode(@NotNull String operation,
                                 @NotNull DollarParser parser,
                                 @Nullable Scope scope,
                                 @NotNull SourceSegment source,
                                 @NotNull List<var> inputs,
                                 @NotNull Pipeable callable) {
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarSource(i -> callable.pipe(), scope, source, inputs, operation, parser)));
    }

    @NotNull
    public static var createNode(@NotNull String operation,
                                 @NotNull DollarParser parser,
                                 @NotNull Token token,
                                 @NotNull List<var> inputs,
                                 @NotNull Pipeable callable) {
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarSource(callable,
                                 null,
                                 new SourceSegmentValue(currentScope(), token),
                                 inputs,
                                 operation,
                                 parser)));
    }


    @NotNull
    public static var createNode(@NotNull String operation,
                                 @NotNull DollarParser parser,
                                 @NotNull Token token,
                                 @NotNull Scope scope,
                                 @NotNull List<var> inputs,
                                 @NotNull Pipeable callable) {
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarSource(callable, scope, new SourceSegmentValue(scope, token), inputs,
                                 operation, parser)));
    }

    @NotNull
    public static var createReactiveNode(@NotNull String operation,
                                         @NotNull DollarParser parser,
                                         @NotNull SourceSegment source,
                                         @NotNull var lhs,
                                         @NotNull var rhs,
                                         @NotNull Pipeable callable) {
        final var lambda = createNode(operation, parser, null, source, Arrays.asList(lhs, rhs),
                                      callable
        );

        rhs.$listen(i -> lambda.$notify());
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }

    @NotNull
    public static var createReactiveNode(String operation,
                                         DollarParser parser,
                                         Token token,
                                         @NotNull var lhs,
                                         @NotNull var rhs, @NotNull Pipeable callable) {
        final var lambda = createNode(operation, parser, null,
                                      new SourceSegmentValue(currentScope(), token),
                                      Arrays.asList(lhs, rhs), callable
        );

        rhs.$listen(i -> lambda.$notify());
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }


    @NotNull
    public static var createNode(@NotNull Token token,
                                 @NotNull Pipeable pipeable,
                                 @NotNull List<var> inputs,
                                 @NotNull String operation,
                                 @NotNull DollarParser parser) {
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarSource(pipeable::pipe, null,
                                 new SourceSegmentValue(currentScope(), token), inputs, operation,
                                 parser)));
    }


    @NotNull
    public static Scope currentScope() {
        return scopes.get().get(scopes.get().size() - 1);
    }

    @SuppressWarnings("ThrowFromFinallyBlock")
    public static <T> T inScope(boolean runtime, boolean pure, @NotNull String scopeName,
                                @NotNull ScopeExecutable<T> r) {
        return inScope(runtime,
                       new ScriptScope(DollarScriptSupport.currentScope(), scopeName, false),
                       pure,
                       scopeName,
                       r);
    }


    public static <T> T inScope(boolean runtime,
                                Scope parent,
                                boolean pure,
                                @NotNull String scopeName,
                                @NotNull ScopeExecutable<T> r) {
        Scope newScope;
        if (pure) {
            newScope = new PureScope(parent, parent.getSource(), scopeName, parent.getFile());
        } else {
            if ((parent instanceof PureScope)) {
                throw new IllegalStateException(
                                                       "trying to switch to an impure scope in a pure scope.");
            }
            newScope = new ScriptScope(parent, parent.getFile(), parent.getSource(), scopeName,
                                       false);
        }
        addScope(runtime, parent);
        addScope(runtime, newScope);
        try {
            return r.execute(newScope);
        } catch (Exception e) {
            throw new DollarScriptException(e);
        } finally {
            Scope poppedScope = endScope(runtime);
            if (poppedScope != newScope) {
                throw new IllegalStateException("Popped wrong scope");
            }
            final Scope poppedScope2 = endScope(runtime);
            if (poppedScope2 != parent) {
                throw new IllegalStateException("Popped wrong scope");
            }
        }
    }


    @SuppressWarnings("ThrowFromFinallyBlock")
    public static <T> T inScope(boolean runtime,
                                @NotNull Scope scope,
                                @NotNull ScopeExecutable<T> r) {

        boolean addedDynamicScope = scopes.get().size() > 0;
        if (addedDynamicScope) {
            addScope(runtime, currentScope());
        }
        addScope(runtime, scope);
        try {
            return r.execute(scope);
        } catch (Exception e) {
            throw new DollarScriptException(e);
        } finally {
            Scope poppedScope = endScope(runtime);
            if (poppedScope != scope) {
                throw new IllegalStateException("Popped wrong scope");
            }
            if (addedDynamicScope) {
                final Scope poppedScope2 = endScope(runtime);
                if (poppedScope2 != currentScope()) {
                    throw new IllegalStateException("Popped wrong scope");
                }
            }
        }
    }

    @Nullable
    public static var getVariable(boolean pure, @NotNull String key, boolean numeric,
                                  @Nullable var defaultValue,
                                  @NotNull Token token, DollarParser parser) {
//       if(pure && !(scope instanceof  PureScope)) {
//           throw new IllegalStateException("Attempting to get a pure variable in an impure scope");
//       }
        SourceSegment source = new SourceSegmentValue(currentScope(), token);

        var lambda[] = new var[1];
        lambda[0] = createNode("get-variable-" + key + "-" + source.getShortHash(), parser, token,
                               $(key).$list().toVarList().mutable(), (i) -> {
                    Scope scope = (Scope) lambda[0].getMetaObject("scope");
                    scope.listen(key, lambda[0]);
                    log.debug(
                            "LOOKUP " + key + " " + scope + " in " + scopes.get().size() + " scopes ");
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
                }
        );
        lambda[0].setMetaAttribute("variable", key);
        return lambda[0];

    }

    public static Scope getRootScope() {
        return scopes.get().get(0);
    }

    public static void setRootScope(@NotNull ScriptScope rootScope) {
        scopes.get().add(0, rootScope);
    }

    @NotNull
    public static var setVariableDefinition(@NotNull Scope scope,
                                            @NotNull DollarParser parser,
                                            @NotNull Token token,
                                            boolean pure, boolean decleration, @NotNull String key,
                                            @NotNull var value,
                                            @Nullable var useConstraint,
                                            @Nullable String useSource) {

        return setVariable(scope, key, value,
                           true, useConstraint, useSource,
                           false, false, pure,
                           decleration, token, parser);
    }

    @NotNull
    public static var setVariable(@NotNull Scope scope,
                                  @NotNull String key,
                                  @NotNull var value,
                                  boolean readonly,
                                  @Nullable var useConstraint,
                                  @Nullable String useSource,
                                  boolean isVolatile,
                                  boolean fixed,
                                  boolean pure,
                                  boolean decleration,
                                  @NotNull Token token,
                                  @Nullable DollarParser parser) {

        SourceSegment source = new SourceSegmentValue(scope, token);
        boolean numeric = key.matches("^\\d+$");
        var lambda[] = new var[1];
        lambda[0] = createNode("get-variable-" + key + "-" + source.getShortHash(), parser, token,
                               $(key).$list().toVarList().mutable(), i -> {
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
                                    throw new DollarScriptException("Variable " + key + " already defined",
                                                                    value);
                                } else {
                                    log.debug(
                                            "SETTING " + key + " " + scope + " " + currentScope());
                                    return scriptScope.set(key,
                                                           value,
                                                           readonly,
                                                           useConstraint,
                                                           useSource,
                                                           isVolatile,
                                                           fixed,
                                                           pure);
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
                        return scope.set(key, value, readonly, useConstraint, useSource, isVolatile,
                                         fixed,
                                         pure);
                    } else {
                        throw new VariableNotFoundException(key, scope);
                    }
                }
        );
        lambda[0].setMetaAttribute("variable", key);
        return lambda[0];
    }

    @NotNull
    public static var constrain(Scope scope, @NotNull var value, var constraint, String source) {
//        System.err.println("(" + source + ") " + rhs.$type().constraint());
        if (!Objects.equals(value._constraintFingerprint(), source)) {
            if (value._constraintFingerprint() != null && !value._constraintFingerprint().isEmpty()) {
                scope.handleError(new DollarScriptException(
                                                                   "Trying to assign an invalid constrained variable " + value._constraintFingerprint() + " vs " + source,
                                                                   value));
            }
        } else {
            if (value._constraintFingerprint() != null && !value._constraintFingerprint().isEmpty()) {
//                System.err.println("Fingerprint: " + rhs.$type().constraint());
            }
        }
        return value._constrain(constraint, source);
    }
}
