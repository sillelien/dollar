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

import dollar.api.DollarException;
import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.Scope;
import dollar.api.Variable;
import dollar.api.script.SourceSegment;
import dollar.api.types.DollarFactory;
import dollar.api.var;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.exceptions.DollarAssertionException;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import dollar.internal.runtime.script.api.exceptions.PureFunctionException;
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

import static dollar.api.DollarStatic.$;
import static dollar.api.DollarStatic.$void;
import static dollar.api.types.meta.MetaConstants.VARIABLE;
import static dollar.internal.runtime.script.parser.Symbols.VAR_USAGE_OP;

public final class DollarScriptSupport {

    @NotNull
    static final String ANSI_CYAN = "36";
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(DollarScriptSupport.class);
    @NotNull
    private static final ThreadLocal<List<Scope>> scopes = ThreadLocal.withInitial(() -> {
        ArrayList<Scope> list = new ArrayList<>();
        list.add(new ScriptScope("thread-" + Thread.currentThread().getId(), false, false));
        return list;
    });

    @NotNull
    static List<Scope> scopes() {
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

    public static var node(@NotNull OpDef operation,
                           @NotNull String name,
                           boolean pure,
                           @NotNull SourceNodeOptions sourceNodeOptions,
                           @NotNull DollarParser parser,
                           @NotNull SourceSegment source,
                           @NotNull List<var> inputs,
                           @NotNull Pipeable pipeable) {
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new SourceNode(pipeable, source, inputs, name, parser,
                               sourceNodeOptions, createId(name), pure, operation)));
    }

    public static var node(@NotNull OpDef operation,
                           boolean pure,
                           @NotNull DollarParser parser,
                           @NotNull SourceSegment source,
                           @NotNull List<var> inputs,
                           @NotNull Pipeable pipeable) {
        return node(operation, operation.name(), pure, operation.nodeOptions(), parser, source, inputs, pipeable);
    }


    public static var node(@NotNull OpDef operation,
                           boolean pure,
                           @NotNull DollarParser parser,
                           @NotNull Token token,
                           @NotNull List<var> inputs,
                           @NotNull Pipeable callable) {
        return node(operation, operation.name(), pure, operation.nodeOptions(), parser,
                    new SourceSegmentValue(currentScope(), token), inputs, callable);
    }


    @NotNull
    static var reactiveNode(@NotNull OpDef operation, @NotNull String name,
                            boolean pure, @NotNull SourceNodeOptions sourceNodeOptions,
                            @NotNull DollarParser parser,
                            @NotNull SourceSegment source,
                            @NotNull var lhs,
                            @NotNull var rhs,
                            @NotNull Pipeable callable) {
        final var lambda = node(operation, name, pure, sourceNodeOptions, parser, source, Arrays.asList(lhs, rhs),
                                callable
        );

        rhs.$listen(i -> lambda.$notify());
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }

    @NotNull
    static var reactiveNode(@NotNull OpDef operation,
                            boolean pure,
                            @NotNull Token token,
                            @NotNull var lhs,
                            @NotNull var rhs,
                            @NotNull DollarParser parser,
                            @NotNull Pipeable callable) {
        return reactiveNode(operation, operation.name(), pure, operation.nodeOptions(), parser,
                            new SourceSegmentValue(currentScope(),
                                                   token), lhs,
                            rhs, callable);

    }


    static var reactiveNode(@NotNull OpDef operation,
                            boolean pure,
                            @NotNull DollarParser parser, @NotNull SourceSegment source,
                            @NotNull var lhs,
                            @NotNull var rhs,
                            @NotNull Pipeable callable) {
        return reactiveNode(operation, operation.name(), pure, operation.nodeOptions(), parser, source, lhs,
                            rhs, callable);

    }

    @NotNull
    static var reactiveNode(@NotNull OpDef operation,
                            boolean pure,
                            @NotNull var lhs,
                            @NotNull Token token,
                            @NotNull DollarParser parser,
                            @NotNull Pipeable callable) {

        return reactiveNode(operation, pure, new SourceSegmentValue(currentScope(), token), parser, lhs, callable
        );
    }

    @NotNull
    static var reactiveNode(@NotNull OpDef operation,
                            boolean pure,
                            @NotNull SourceSegment source,
                            @NotNull DollarParser parser,
                            @NotNull var lhs,
                            @NotNull Pipeable callable) {

        final var lambda = node(operation, operation.name(),
                                pure, operation.nodeOptions(), parser, source,
                                Collections.singletonList(lhs),
                                callable
        );
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }

    @NotNull
    private static String createId(@NotNull String operation) {
        return operation + "-" + UUID.randomUUID();
    }


    @NotNull
    public static Scope currentScope() {
        return scopes.get().get(scopes.get().size() - 1);
    }

    public static <T> T inSubScope(boolean runtime, boolean pure, @NotNull String scopeName,
                                   @NotNull ScopeExecutable<T> r) {
        return inScope(runtime, new ScriptScope(currentScope(), scopeName, false, currentScope().parallel()), r);
    }


    @SuppressWarnings("ThrowFromFinallyBlock")
    public static <T> T inSubScope(boolean runtime, boolean pure, boolean parallel, @NotNull String scopeName,
                                   @NotNull ScopeExecutable<T> r) {
        return inScope(runtime, new ScriptScope(currentScope(), scopeName, false, parallel), r);
    }


    public static <T> T inScope(boolean runtime,
                                @NotNull Scope parent,
                                boolean pure,
                                boolean parallel, @NotNull String scopeName,
                                @NotNull ScopeExecutable<T> r) {
        Scope newScope;
        if (pure) {
            newScope = new PureScope(parent, parent.getSource(), scopeName, parent.getFile(), parallel);
        } else {
            if ((parent instanceof PureScope)) {
                throw new IllegalStateException(
                                                       "trying to switch to an impure scope in a pure scope.");
            }
            newScope = new ScriptScope(parent, parent.getFile(), parent.getSource(), scopeName,
                                       false, parallel);
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
        } catch (DollarException e) {
            throw e;
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
        lambda[0] = node(VAR_USAGE_OP, pure, parser, token, $(key).$list().toVarList().mutable(),
                         (i) -> {
                             Scope scope = currentScope();

                             log.debug("{} {} in {} scopes ", ansiColor("LOOKUP " + key, ANSI_CYAN), scope, scopes.get().size());
                             if (numeric) {
                                 if (scope.hasParameter(key)) {
                                     return scope.parameter(key);
                                 }
                             } else {
                                 if (scope.has(key)) {
                                     Scope scopeForKey = scope.getScopeForKey(key);
                                     assert scopeForKey != null;
                                     scopeForKey.listen(key, id.toString(), lambda[0]);
                                     Variable v = scopeForKey.getVariable(key);
                                     if (!v.isPure() && (pure || scope.pure())) {
                                         currentScope().handleError(
                                                 new PureFunctionException("Attempted to use an impure variable in a " +
                                                                                   "pure context"));
                                     }
                                     return v.getValue();
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
                                             return scriptScope.parameter(key);
                                         }
                                     } else {
                                         if (scriptScope.has(key)) {
                                             Scope scopeForKey = scriptScope.getScopeForKey(key);
                                             assert scopeForKey != null;
                                             scopeForKey.listen(key, id.toString(), lambda[0]);
                                             Variable v = scopeForKey.getVariable(key);
                                             if (!v.isPure() && (pure || scope.pure())) {
                                                 scope.handleError(
                                                         new PureFunctionException("Attempted to use an impure " +
                                                                                           "variable in a pure " +
                                                                                           "context"));
                                             }
                                             return v.getValue();
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
                                 throw new VariableNotFoundException(key, scope);
                             }

                             if (defaultValue != null) {
                                 return defaultValue;
                             } else {
                                 throw new VariableNotFoundException(key, scope);
                             }
                         }
        );
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
                                  isVolatile, fixed, pure, decleration);
        }
        try {
            List<Scope> scopes = new ArrayList<>(DollarScriptSupport.scopes.get());
            Collections.reverse(scopes);
            for (Scope scriptScope : scopes) {
                if (!(scriptScope instanceof PureScope) && pure) {
                    log.debug("Skipping {}", scriptScope);
                }

                if (scriptScope.has(key)) {
                    return updateVariable(scriptScope, key, value, readonly, useConstraint, useSource,
                                          isVolatile, fixed, pure, decleration);
                }

            }
        } catch (DollarAssertionException e) {
            return parser.getErrorHandler().handle(scope, null, e);
        } catch (DollarScriptException e) {
            return parser.getErrorHandler().handle(scope, null, e);
        } catch (RuntimeException e) {
            return parser.getErrorHandler().handle(scope, null, e);
        }
        if (numeric) {
            return scope.parameter(key);
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

            return scope.set(key, value, readonly, useConstraint, useSource, isVolatile, fixed, pure);
        }
    }

    @NotNull
    public static var constrain(@NotNull Scope scope, @NotNull var value, @Nullable var constraint, @Nullable String source) {
//        System.err.println("(" + source + ") " + rhs.$type().constraint());
        if (!Objects.equals(value.constraintLabel(), source)) {
            if ((source != null) && (value.constraintLabel() != null) && !value.constraintLabel().isEmpty()) {
                scope.handleError(
                        new DollarScriptException("Trying to assign an invalid constrained variable " + value.constraintLabel() + " vs " + source,
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


    public static String randomId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Fix var.
     *
     * @param v        the v
     * @param parallel Should execution be in parallel?
     * @return the var
     */
    @NotNull
    public static var fix(@Nullable var v, boolean parallel) {
        return (v != null) ? DollarFactory.wrap(v.$fix(parallel)) : $void();
    }

    /**
     * Fix var.
     *
     * @param v the v
     * @return the var
     */
    @NotNull
    public static var fix(@Nullable var v) {
        return (v != null) ? DollarFactory.wrap(v.$fix(currentScope().parallel())) : $void();
    }

}
