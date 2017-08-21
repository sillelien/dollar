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
import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.Scope;
import dollar.internal.runtime.script.api.exceptions.DollarAssertionException;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import dollar.internal.runtime.script.api.exceptions.VariableNotFoundException;
import dollar.internal.runtime.script.parser.OpDef;
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
import java.util.UUID;

import static com.sillelien.dollar.api.DollarStatic.$;
import static com.sillelien.dollar.api.types.meta.MetaConstants.VARIABLE;
import static dollar.internal.runtime.script.parser.Symbols.VAR_USAGE_OP;

public final class DollarScriptSupport {

    @NotNull
    public static final String ANSI_CYAN = "36";
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(DollarScriptSupport.class);
    @NotNull
    private static final ThreadLocal<List<Scope>> scopes = ThreadLocal.withInitial(() -> {
        ArrayList<Scope> list = new ArrayList<>();
        list.add(new ScriptScope("thread-" + Thread.currentThread().getId(), false));
        return list;
    });

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
        boolean newScope = scopes.get().isEmpty() || !scope.equals(currentScope());
        scopes.get().add(scope);
        if (DollarStatic.getConfig().debugScope()) {
            log.info("{}{}BEGIN {}", indent(scopes.get().size() - 1), runtime ? "**** " : "", scope);
        }

    }

    @NotNull
    private static Scope endScope(boolean runtime) {

        Scope remove = scopes.get().remove(scopes.get().size() - 1);
        if (DollarStatic.getConfig().debugScope()) {

            log.info("{}{}END:  {}", indent(scopes.get().size()), runtime ? "**** " : "", remove);

        }

        return remove;
    }

    @NotNull
    public static var reactiveNode(boolean pure,
                                   @NotNull SourceNodeOptions sourceNodeOptions,
                                   @NotNull var lhs, @NotNull Token token, @NotNull DollarParser parser,
                                   @NotNull Pipeable callable, OpDef operation) {
        return reactiveNode(operation.name(), pure, sourceNodeOptions, lhs, token, parser, callable, operation);
    }

    @NotNull
    public static var reactiveNode(@NotNull String name,
                                   boolean pure,
                                   @NotNull SourceNodeOptions sourceNodeOptions,
                                   @NotNull var lhs, @NotNull Token token, @NotNull DollarParser parser,
                                   @NotNull Pipeable callable, OpDef operation) {

        return reactiveNode(name, pure, sourceNodeOptions,
                            new SourceSegmentValue(currentScope(), token), parser,
                            lhs, callable,
                            operation);
    }

    @NotNull
    public static var reactiveNode(boolean pure, @NotNull SourceNodeOptions sourceNodeOptions,
                                   @NotNull SourceSegment source,
                                   @NotNull DollarParser parser,
                                   @NotNull var lhs,
                                   @NotNull Pipeable callable, OpDef operation) {
        return reactiveNode(operation.name(), pure, sourceNodeOptions, source, parser, lhs, callable, operation);
    }

    @NotNull
    public static var reactiveNode(@NotNull String name,
                                   boolean pure, @NotNull SourceNodeOptions sourceNodeOptions,
                                   @NotNull SourceSegment source,
                                   @NotNull DollarParser parser,
                                   @NotNull var lhs,
                                   @NotNull Pipeable callable, OpDef operation) {

        final var lambda = node(name,
                                pure, sourceNodeOptions, parser, source,
                                Collections.singletonList(lhs),
                                callable,
                                operation);
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }


    @NotNull
    public static var node(boolean pure,
                           @NotNull SourceNodeOptions sourceNodeOptions,
                           @NotNull DollarParser parser,
                           @NotNull SourceSegment source,
                           @NotNull List<var> inputs,
                           @NotNull Pipeable pipeable, @NotNull OpDef operation) {
        return node(operation.name(), pure, sourceNodeOptions, parser, source, inputs, pipeable, operation);
    }

    @NotNull
    public static var node(@NotNull String name,
                           boolean pure,
                           @NotNull SourceNodeOptions sourceNodeOptions,
                           @NotNull DollarParser parser,
                           @NotNull SourceSegment source,
                           @NotNull List<var> inputs,
                           @NotNull Pipeable pipeable, @NotNull OpDef operation) {
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new SourceNode(pipeable, source, inputs, name, parser,
                               sourceNodeOptions, createId(name), pure, operation)));
    }

    @NotNull
    public static var node(boolean pure, @NotNull SourceNodeOptions sourceNodeOptions,
                           @NotNull List<var> inputs, @NotNull Token token, @NotNull DollarParser parser,
                           @NotNull Pipeable callable, @NotNull OpDef operation) {
        return node(operation.name(), pure, sourceNodeOptions, inputs, token, parser, callable, operation);
    }

    @NotNull
    public static var node(@NotNull String name,
                           boolean pure, @NotNull SourceNodeOptions sourceNodeOptions,
                           @NotNull List<var> inputs, @NotNull Token token, @NotNull DollarParser parser,
                           @NotNull Pipeable callable, @NotNull OpDef operation) {
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new SourceNode(callable,
                               new SourceSegmentValue(currentScope(), token),
                               inputs,
                               name,
                               parser, sourceNodeOptions, createId(name), pure, operation)));
    }


    @NotNull
    public static var reactiveNode(boolean pure, @NotNull SourceNodeOptions sourceNodeOptions,
                                   @NotNull DollarParser parser,
                                   @NotNull SourceSegment source,
                                   @NotNull var lhs,
                                   @NotNull var rhs,
                                   @NotNull Pipeable callable, @NotNull OpDef operation) {
        return reactiveNode(operation.name(), pure, sourceNodeOptions, parser, source, lhs, rhs, callable, operation);
    }

    @NotNull
    public static var reactiveNode(@NotNull String name,
                                   boolean pure, @NotNull SourceNodeOptions sourceNodeOptions,
                                   @NotNull DollarParser parser,
                                   @NotNull SourceSegment source,
                                   @NotNull var lhs,
                                   @NotNull var rhs,
                                   @NotNull Pipeable callable, @NotNull OpDef operation) {
        final var lambda = node(name, pure, sourceNodeOptions, parser, source, Arrays.asList(lhs, rhs),
                                callable,
                                operation);

        rhs.$listen(i -> lambda.$notify());
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }

    @NotNull
    public static var reactiveNode(@NotNull String name,
                                   boolean pure,
                                   @NotNull SourceNodeOptions sourceNodeOptions,
                                   @NotNull Token token, @NotNull var lhs, @NotNull var rhs, @NotNull DollarParser parser,
                                   @NotNull Pipeable callable, OpDef operation) {
        final var lambda = node(name, pure, sourceNodeOptions, parser,
                                new SourceSegmentValue(currentScope(), token),
                                Arrays.asList(lhs, rhs), callable,
                                operation);

        rhs.$listen(i -> lambda.$notify());
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }


    @NotNull
    public static var node(@NotNull String name,
                           boolean pure, @NotNull SourceNodeOptions sourceNodeOptions, @NotNull Token token,
                           @NotNull List<var> inputs,
                           @NotNull DollarParser parser,
                           @NotNull Pipeable pipeable, OpDef operation) {
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new SourceNode(pipeable::pipe,
                               new SourceSegmentValue(currentScope(), token), inputs, name,
                               parser, sourceNodeOptions, createId(name), pure, operation)));
    }

    @NotNull
    private static String createId(@NotNull String operation) {
        return operation + "-" + UUID.randomUUID();
    }


    @NotNull
    public static Scope currentScope() {
        return scopes.get().get(scopes.get().size() - 1);
    }

    @Nullable
    @SuppressWarnings("ThrowFromFinallyBlock")
    public static <T> T inSubScope(boolean runtime, boolean pure, @NotNull String scopeName,
                                   @NotNull ScopeExecutable<T> r) {
        return inScope(runtime, new ScriptScope(DollarScriptSupport.currentScope(), scopeName, false), r);
    }


    @NotNull
    public static <T> T inScope(boolean runtime,
                                @NotNull Scope parent,
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
//            poppedScope.destroy();
            if (!Objects.equals(poppedScope, newScope)) {
                throw new IllegalStateException("Popped wrong scope");
            }
            final Scope poppedScope2 = endScope(runtime);
            if (!Objects.equals(poppedScope2, parent)) {
                throw new IllegalStateException("Popped wrong scope");
            }
        }
    }


    @Nullable
    @SuppressWarnings("ThrowFromFinallyBlock")
    public static <T> T inScope(boolean runtime,
                                @NotNull Scope scope,
                                @NotNull ScopeExecutable<T> r) {

        boolean addedDynamicScope = !scopes.get().isEmpty();

        addScope(runtime, scope);
        try {
            return r.execute(scope);
        } catch (Exception e) {
            throw new DollarScriptException(e);
        } finally {
            Scope poppedScope = endScope(runtime);
            if (!Objects.equals(poppedScope, scope)) {
                throw new IllegalStateException("Popped wrong scope");
            }

        }
    }

    @NotNull
    public static var variableNode(boolean pure, @NotNull String key, @NotNull Token token, @NotNull DollarParser parser) {
        return variableNode(pure, key, false, null, token, parser);
    }
    @NotNull
    public static var variableNode(boolean pure, @NotNull String key,
                                   boolean numeric, @Nullable var defaultValue,
                                   @NotNull Token token, @NotNull DollarParser parser) {
        var lambda[] = new var[1];
        UUID id = UUID.randomUUID();
        lambda[0] = node("get-variable-" + key + "-" + id, pure, SourceNodeOptions.NO_SCOPE, $(key).$list().toVarList().mutable(),
                         token, parser,
                         (i) -> {
                    Scope scope = currentScope();

                    log.debug("{} {} in {} scopes ", ansiColor("LOOKUP " + key, ANSI_CYAN), scope, scopes.get().size());
                    if (numeric) {
                        if (scope.hasParameter(key)) {
                            return scope.getParameter(key);
                        }
                    } else {
                        if (scope.has(key)) {
                            Scope scopeForKey = scope.getScopeForKey(key);
                            assert scopeForKey != null;
                            scopeForKey.listen(key, id.toString(), lambda[0]);
                            return scope.get(key);
                        }
                    }
                    try {
                        List<Scope> scopes = new ArrayList<>(DollarScriptSupport.scopes.get());
                        Collections.reverse(scopes);
                        for (Scope scriptScope : scopes) {
                            if (!(scriptScope instanceof PureScope) && pure) {
                                log.debug("Skipping {}", scriptScope);
                            }
                            if (numeric) {
                                if (scriptScope.hasParameter(key)) {
                                    return scriptScope.getParameter(key);
                                }
                            } else {
                                if (scriptScope.has(key)) {
                                    Scope scopeForKey = scriptScope.getScopeForKey(key);
                                    assert scopeForKey != null;
                                    scopeForKey.listen(key, id.toString(), lambda[0]);
                                    return scriptScope.get(key);
                                }
                            }
                        }
                    } catch (DollarAssertionException e) {
                        throw e;
                    } catch (DollarScriptException e) {
                        return parser.getErrorHandler().handle(scope, null, e);
                    } catch (RuntimeException e) {
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
                         },
                         VAR_USAGE_OP);
        lambda[0].metaAttribute(VARIABLE, key);
        return lambda[0];

    }


    @Nullable
    public static Scope getScopeForVar(boolean pure,
                                       @NotNull String key,
                                       boolean numeric,
                                       @Nullable Scope initialScope) {

        if (initialScope == null) {
            initialScope = currentScope();
        }
        log.debug("{} {} in {} scopes ", ansiColor("LOOKUP " + key, ANSI_CYAN), initialScope, scopes.get().size());
        if (numeric) {
            if (initialScope.hasParameter(key)) {
                return initialScope;
            }
        } else {
            if (initialScope.has(key)) {
                return initialScope;
            }
        }

        List<Scope> scopes = new ArrayList<>(DollarScriptSupport.scopes.get());
        Collections.reverse(scopes);
        for (Scope scriptScope : scopes) {
            if (!(scriptScope instanceof PureScope) && pure) {
                log.debug("Skipping {}", scriptScope);
            }
            if (numeric) {
                if (scriptScope.hasParameter(key)) {
                    return scriptScope;
                }
            } else {
                if (scriptScope.has(key)) {

                    return scriptScope;
                }
            }
        }

        return null;


    }

    @NotNull
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


        if (scope.has(key)) {
            return updateVariable(scope, key, value, readonly, useConstraint, useSource,
                                  isVolatile, fixed, pure,
                                  decleration);
        }
//        if (decleration) {
//            scope.listen(key, value);
//        }
        try {
            List<Scope> scopes = new ArrayList<>(DollarScriptSupport.scopes.get());
            Collections.reverse(scopes);
            for (Scope scriptScope : scopes) {
                if (!(scriptScope instanceof PureScope) && pure) {
                    log.debug("Skipping {}", scriptScope);
                }

                if (scriptScope.has(key)) {
                    return updateVariable(scriptScope, key, value, readonly, useConstraint, useSource,
                                          isVolatile, fixed, pure,
                                          decleration);
                }

            }
        } catch (DollarAssertionException e) {
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
            log.debug("{} {} {}", ansiColor("SETTING  " + key, ANSI_CYAN), scope, scope);
            return scope.set(key, value, readonly, useConstraint, useSource, isVolatile,
                             fixed,
                             pure);
        } else {
            throw new VariableNotFoundException(key, scope);
        }


    }

    private static var updateVariable(@NotNull Scope scope,
                                      @NotNull String key,
                                      @NotNull var value,
                                      boolean readonly,
                                      @Nullable var useConstraint,
                                      @Nullable String useSource,
                                      boolean isVolatile,
                                      boolean fixed,
                                      boolean pure, boolean decleration) {
        log.debug("{}{} {}", ansiColor("UPDATING ", ANSI_CYAN), key, scope);

        if (decleration) {
            throw new DollarScriptException("Variable " + key + " already defined in " + scope);
        } else {

            return scope.set(key,
                             value,
                             readonly,
                             useConstraint,
                             useSource,
                             isVolatile,
                             fixed,
                             pure);
        }
    }

    @NotNull
    public static var constrain(@NotNull Scope scope, @NotNull var value, @Nullable var constraint, @Nullable String source) {
//        System.err.println("(" + source + ") " + rhs.$type().constraint());
        if (!Objects.equals(value.constraintLabel(), source)) {
            if ((value.constraintLabel() != null) && !value.constraintLabel().isEmpty()) {
                scope.handleError(new DollarScriptException(
                                                                   "Trying to assign an invalid constrained variable " + value.constraintLabel() + " vs " + source,
                                                                   value));
            }
        } else {
            if ((value.constraintLabel() != null) && !value.constraintLabel().isEmpty()) {
//                System.err.println("Fingerprint: " + rhs.$type().constraint());
            }
        }
        return value.$constrain(constraint, source);
    }

    public static String ansiColor(@NotNull String text, @NotNull String color) {
        return "\u001b["  // Prefix
                       + "0"        // Brightness
                       + ";"        // Separator
                       + color       // Red foreground
                       + "m"        // Suffix
                       + text       // the text to output
                       + "\u001b[m"; // Prefix + Suffix to reset color
    }

    public static void pushScope(@NotNull Scope scope) {
        addScope(true, scope);
    }

    public static void popScope(@NotNull Scope scope) {
        Scope poppedScope = endScope(true);
        if (!poppedScope.equals(scope)) {
            throw new DollarAssertionException("Popped scope does not equal expected scope");
        }
    }


}
