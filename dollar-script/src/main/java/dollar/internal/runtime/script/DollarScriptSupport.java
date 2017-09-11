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

import dollar.api.ConstraintLabel;
import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.Scope;
import dollar.api.Type;
import dollar.api.TypePrediction;
import dollar.api.VarFlags;
import dollar.api.Variable;
import dollar.api.script.Source;
import dollar.api.types.DollarFactory;
import dollar.api.types.meta.MetaConstants;
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

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static dollar.api.DollarStatic.*;
import static dollar.api.types.meta.MetaConstants.VARIABLE;
import static dollar.internal.runtime.script.DollarParserImpl.NAMED_PARAMETER_META_ATTR;
import static dollar.internal.runtime.script.parser.Symbols.VAR_USAGE_OP;

public final class DollarScriptSupport {

    public static final double MIN_PROBABILITY = 0.5;
    @NotNull
    static final String ANSI_CYAN = "36";
    @NotNull
    static final ThreadLocal<List<Scope>> scopes = ThreadLocal.withInitial(() -> {
        ArrayList<Scope> list = new ArrayList<>();
        list.add(new ScriptScope("thread-" + Thread.currentThread().getId(), false, false));
        return list;
    });
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(DollarScriptSupport.class);

    public static void addParameterstoCurrentScope(@NotNull Scope scope, @NotNull List<var> parameters) {
        //Add the special $* value for all the parameters
        int count = 0;
        List<var> fixedParams = new ArrayList<>();
        for (var parameter : parameters) {
            var fixedParam = parameter.$fix(1, false);
            fixedParams.add(fixedParam);
            scope.parameter(String.valueOf(++count), fixedParam);

            //If the parameter is a named parameter then use the name (set as metadata on the value).
            String paramMetaAttribute = fixedParam.metaAttribute(NAMED_PARAMETER_META_ATTR);
            if (paramMetaAttribute != null) {
                scope.parameter(paramMetaAttribute, fixedParam);
            }
        }
        scope.parameter("*", $(fixedParams));
    }

    private static void addScope(boolean runtime, @NotNull Scope scope) {
        boolean newScope = scopes.get().isEmpty() || !scope.equals(currentScope());
        scopes.get().add(scope);
        if (DollarStatic.getConfig().debugScope()) {
            log.info("{}{}BEGIN {}", indent(scopes.get().size() - 1), runtime ? "**** " : "", scope);
        }

    }

    public static void checkLearntType(@NotNull Token token, @Nullable Type type, @NotNull var rhs, @NotNull Double threshold) {
        final TypePrediction prediction = rhs.predictType();
        if ((type != null) && (prediction != null)) {
            final Double probability = prediction.probability(type);
            log.info("Predicted " + prediction.probableType() + " at " + (new SourceCode(currentScope(),
                                                                                         token)).getShortSourceMessage());
            if ((probability < threshold) && !prediction.empty()) {
                log.warn("Type assertion may fail, expected {} most likely type is {} ({}%) at {}", type,
                         prediction.probableType(),
                         (int) (prediction.probability(prediction.probableType()) * 100),
                         new SourceCode(currentScope(), token).getSourceMessage()
                );
                if (getConfig().failFast()) {
                    throw new DollarScriptException("Type prediction failed, was expecting " + type + " but most likely type is " + prediction.probableType() + " if this prediction is wrong please add an explicit cast (using 'as " + type.name() + "')");
                }
            }
        }
    }

    public static var constrain(@NotNull Scope scope,
                                @NotNull var value,
                                @Nullable var constraint,
                                @Nullable ConstraintLabel label) {
//        System.err.println("(" + label + ") " + rhs.$type().constraint());
        ConstraintLabel valueLabel = value.constraintLabel();
        if (Objects.equals(valueLabel, label)) {
            if ((valueLabel != null) && !valueLabel.isEmpty()) {
//                System.err.println("Fingerprint: " + rhs.$type().constraint());
            }
        } else {
            if ((label != null) && (valueLabel != null) && !valueLabel.isEmpty()) {
                scope.handleError(
                        new DollarScriptException("Trying to assign an invalid constrained variable " + valueLabel + " vs " + label,
                                                  value));
            }
        }
        if (constraint == null) {
            return value;
        } else {
            return value.$constrain(constraint, label);
        }
    }

    @NotNull
    private static String createId(@NotNull String operation) {
        return operation + "-" + UUID.randomUUID();
    }

    @NotNull
    public static Scope currentScope() {
        return scopes.get().get(scopes.get().size() - 1);
    }

    @NotNull
    private static Scope endScope(boolean runtime) {

        Scope remove = scopes.get().remove(scopes.get().size() - 1);
        if (DollarStatic.getConfig().debugScope()) {

            log.info("{}{}END:  {}", indent(scopes.get().size()), runtime ? "**** " : "", remove);

        }

        return remove;
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
        return (v != null) ? DollarFactory.wrap(v.$fix(false)) : $void();
    }

    @NotNull
    public static DollarParser getParser() {
        return DollarParser.parser.get();
    }

    @NotNull
    public static Scope getRootScope() {
        return scopes.get().get(0);
    }

    public static void setRootScope(@NotNull ScriptScope rootScope) {
        scopes.get().add(0, rootScope);
    }

    @Nullable
    public static Scope getScopeForVar(boolean pure,
                                       @NotNull String key,
                                       boolean numeric,
                                       @Nullable Scope initialScope) {

        if (initialScope == null) {
            initialScope = currentScope();
        }
        if (getConfig().debugScope()) {
            log.info("{} {} in {} scopes ", highlight("LOOKUP " + key, ANSI_CYAN), initialScope, scopes.get().size());
        }
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

    private static var getVar(@NotNull String key,
                              @NotNull UUID id,
                              @NotNull Scope scopeForKey,
                              @NotNull Source sourceCode,
                              boolean pure,
                              @NotNull var node) {
        assert scopeForKey != null;
        log.debug("Listening to scope {} for key {}", scopeForKey, key);
        scopeForKey.listen(key, id.toString(), node);
        Variable v = scopeForKey.variable(key);
        if (!v.isPure() && (pure || scopeForKey.pure())) {
            scopeForKey.handleError(
                    new PureFunctionException("Attempted to use an impure variable " + key + " in a " +
                                                      "pure context " + scopeForKey, sourceCode));
        }
        return v.getValue();
    }

    public static String highlight(@NotNull String text, @NotNull String color) {
        if (getConfig().colorHighlighting()) {
            return "\u001b["  // Prefix
                           + "0"        // Brightness
                           + ";"        // Separator
                           + color       // Red foreground
                           + "m"        // Suffix
                           + text       // the text to output
                           + "\u001b[m"; // Prefix + Suffix to reset color
        } else {
            return "***" + text + "***";
        }
    }

    public static <T> T inScope(boolean runtime,
                                @NotNull Scope parent,
                                boolean pure,
                                @NotNull String scopeName,
                                @NotNull ScopeExecutable<T> r) {
        Scope newScope;
        if (pure) {
            newScope = new PureScope(parent, parent.source(), scopeName, parent.file());
        } else {
            if ((parent instanceof PureScope)) {
                throw new IllegalStateException(
                                                       "trying to switch to an impure scope in a pure scope.");
            }
            newScope = new ScriptScope(parent, parent.source(), scopeName,
                                       false, false);
        }
        addScope(runtime, parent);
        addScope(runtime, newScope);
        try {
            return r.execute(newScope);
        } catch (Exception e) {
            newScope.handleError(e);
            return null;
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
    public static <T> T inScope(boolean runtime,
                                @NotNull Scope scope,
                                @NotNull ScopeExecutable<T> r) {

        final boolean scopeAdded;
        if (scopes.get().get(scopes.get().size() - 1).equals(scope)) {
            scopeAdded = false;
        } else {
            addScope(runtime, scope);
            scopeAdded = true;
        }
        try {
            return r.execute(scope);
        } catch (Exception e) {
            scope.handleError(e);
            return null;
        } finally {
            if (scopeAdded) {
                Scope poppedScope = endScope(runtime);
                if (!Objects.equals(poppedScope, scope)) {
                    throw new IllegalStateException("Popped wrong scope");
                }
            }

        }
    }

    @Nullable
    public static <T> T inSubScope(boolean runtime, boolean pure, @NotNull String scopeName,
                                   @NotNull ScopeExecutable<T> r) {
        return inScope(runtime, currentScope(), pure, scopeName, r);
    }

    private static String indent(int i) {
        StringBuilder b = new StringBuilder();
        for (int j = 0; j < i; j++) {
            b.append(" ");
        }
        return b.toString();
    }

    public static var node(@NotNull OpDef operation,
                           @NotNull String name,
                           boolean pure,
                           @NotNull SourceNodeOptions sourceNodeOptions,
                           @NotNull DollarParser parser,
                           @NotNull Source source,
                           @Nullable Type suggestedType, @NotNull List<var> inputs,
                           @NotNull Pipeable pipeable) {
        var result = DollarFactory.wrap((var) Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new SourceNode(pipeable, source, inputs, name, parser,
                               sourceNodeOptions, createId(name), pure, operation)));
        try {
            if (suggestedType != null) {
                result.meta(MetaConstants.TYPE_HINT, suggestedType);
            } else {
                Type type = operation.typeFor(inputs.toArray(new var[inputs.size()]));
                if (type != null) {
                    result.meta(MetaConstants.TYPE_HINT, type);
                }
            }
            return result;
        } catch (Exception e) {
            throw new DollarScriptException(e, source);
        }
    }

    @NotNull
    public static var node(@NotNull OpDef operation,
                           boolean pure,
                           @NotNull DollarParser parser,
                           @NotNull Source source,
                           @NotNull List<var> inputs,
                           @NotNull Pipeable pipeable) {
        return node(operation, operation.name(), pure, operation.nodeOptions(), parser, source, null, inputs, pipeable);
    }

    @NotNull
    public static var node(@NotNull OpDef operation,
                           boolean pure,
                           @NotNull DollarParser parser,
                           @NotNull Token token,
                           @NotNull List<var> inputs,
                           @NotNull Pipeable callable) {
        return node(operation, operation.name(), pure, operation.nodeOptions(), parser,
                    new SourceCode(currentScope(), token), null, inputs, callable);
    }

    @NotNull
    public static var node(@NotNull OpDef operation,
                           @NotNull String name,
                           boolean pure,
                           @NotNull DollarParser parser,
                           @NotNull Token token,
                           @NotNull List<var> inputs,
                           @NotNull Pipeable callable) {
        return node(operation, name, pure, operation.nodeOptions(), parser,
                    new SourceCode(currentScope(), token), null, inputs, callable);
    }

    public static void popScope(@NotNull Scope scope) {
        Scope poppedScope = endScope(true);
        if (!poppedScope.equals(scope)) {
            throw new DollarAssertionException("Popped scope does not equal expected scope");
        }
    }

    public static void pushScope(@NotNull Scope scope) {
        addScope(true, scope);
    }

    public static String randomId() {
        return UUID.randomUUID().toString();
    }

    @NotNull
    static var reactiveNode(@NotNull OpDef operation, @NotNull String name,
                            boolean pure, @NotNull SourceNodeOptions sourceNodeOptions,
                            @NotNull DollarParser parser,
                            @NotNull Source source,
                            @NotNull var lhs,
                            @NotNull var rhs,
                            @NotNull Pipeable callable) {
        final var node = node(operation, name, pure, sourceNodeOptions, parser, source, null, Arrays.asList(lhs, rhs),
                              callable
        );


        rhs.$listen(i -> node.$notify());
        lhs.$listen(i -> node.$notify());
        return node;
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
                            new SourceCode(currentScope(),
                                           token), lhs,
                            rhs, callable);

    }

    @NotNull
    static var reactiveNode(@NotNull OpDef operation,
                            @NotNull String name,
                            boolean pure,
                            @NotNull Token token,
                            @NotNull var lhs,
                            @NotNull var rhs,
                            @NotNull DollarParser parser,
                            @NotNull Pipeable callable) {
        return reactiveNode(operation, name, pure, operation.nodeOptions(), parser,
                            new SourceCode(currentScope(),
                                           token), lhs,
                            rhs, callable);

    }

    @NotNull
    static var reactiveNode(@NotNull OpDef operation,
                            boolean pure,
                            @NotNull DollarParser parser, @NotNull Source source,
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

        return reactiveNode(operation, pure, new SourceCode(currentScope(), token), parser, lhs, callable
        );
    }

    @NotNull
    static var reactiveNode(@NotNull OpDef operation,
                            boolean pure,
                            @NotNull Source source,
                            @NotNull DollarParser parser,
                            @NotNull var lhs,
                            @NotNull Pipeable callable) {

        final var node = node(operation, operation.name(),
                              pure, operation.nodeOptions(), parser, source,
                              null, Collections.singletonList(lhs),
                              callable);
        lhs.$listen(i -> node.$notify());
        return node;
    }

    @NotNull
    public static String removePrefix(@NotNull String key) {
        if (key.startsWith("_")) {
            return key.substring(1);
        } else {
            return key;
        }
    }

    @NotNull
    static List<Scope> scopes() {
        return scopes.get();
    }

    @NotNull
    public static Variable setVariable(@NotNull Scope scope,
                                       @NotNull String key,
                                       @NotNull var value,
                                       @Nullable DollarParser parser,
                                       @NotNull Token token,
                                       @Nullable var useConstraint,
                                       @Nullable ConstraintLabel useSource,
                                       @NotNull VarFlags varFlags) {

        Source source = new SourceCode(scope, token);
        boolean numeric = key.matches("^\\d+$");


        if (scope.has(key)) {
            return updateVariable(scope, key, value, varFlags, useConstraint, useSource);
        }
        try {
            List<Scope> scopes = new ArrayList<>(DollarScriptSupport.scopes.get());
            Collections.reverse(scopes);
            for (Scope scriptScope : scopes) {
                if (!(scriptScope instanceof PureScope) && varFlags.isPure()) {
                    log.debug("Skipping {}", scriptScope);
                }

                if (scriptScope.has(key)) {
                    return updateVariable(scriptScope, key, value, varFlags, useConstraint, useSource
                    );
                }

            }
        } catch (DollarAssertionException e) {
            ErrorHandlerFactory.instance().handle(scope, source, e);
            throw e;
        } catch (DollarScriptException e) {
            ErrorHandlerFactory.instance().handle(scope, source, e);
            throw e;
        } catch (RuntimeException e) {
            ErrorHandlerFactory.instance().handle(scope, source, e);
            throw e;
        }
        if (numeric) {
            return scope.parameter(key);
        }

        if (varFlags.isDeclaration()) {
            if (getConfig().debugScope()) {
                log.info("{} {} {}", highlight("SETTING  " + key, ANSI_CYAN), scope, scope);
            }
            return scope.set(key, value, useConstraint, useSource, varFlags);
        } else {
            throw new VariableNotFoundException(key, scope);
        }


    }

    @NotNull
    static String shortHash(@NotNull Token token) {
        return new SourceCode(currentScope(), token).getShortHash();
    }

    private static @NotNull Variable updateVariable(@NotNull Scope scope,
                                                    @NotNull String key,
                                                    @NotNull var value,
                                                    @NotNull VarFlags varFlags, @Nullable var useConstraint,
                                                    @Nullable ConstraintLabel useSource) {
        if (getConfig().debugScope()) {
            log.info("{}{} {}", highlight("UPDATING ", ANSI_CYAN), key, scope);
        }
        if (varFlags.isDeclaration()) {
            throw new DollarScriptException("Variable " + key + " already defined in " + scope);
        } else {

            return scope.set(key, value, useConstraint, useSource, varFlags);
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
        var node[] = new var[1];
        UUID id = UUID.randomUUID();
        SourceCode sourceCode = new SourceCode(currentScope(), token);
        node[0] = node(VAR_USAGE_OP, "var-usage-" + key + "-" + sourceCode.getShortHash(), pure, parser, token,
                       $(key).$list().toVarList(),
                       (i) -> {
                           Scope scope = currentScope();

                           if (getConfig().debugScope()) {
                               log.info("{} {} in {} scopes ", highlight("LOOKUP " + key, ANSI_CYAN), scope,
                                        scopes.get().size());

                           }

                           if (scope.has(key)) {
                               return getVar(key, id, scope.scopeForKey(key), sourceCode, pure, node[0]);
                           }

                           try {
                               List<Scope> scopes = new ArrayList<>(DollarScriptSupport.scopes.get());
                               Collections.reverse(scopes);
                               for (Scope scriptScope : scopes) {
                                   if (!(scriptScope instanceof PureScope) && pure) {
                                       log.debug("Skipping {}", scriptScope);
                                   }

                                   if (scriptScope.has(key)) {
                                       return getVar(key, id, scriptScope.scopeForKey(key), sourceCode, pure, node[0]);

                                   }
                               }
                           } catch (DollarAssertionException e) {
                               throw e;
                           } catch (DollarScriptException e) {
                               return ErrorHandlerFactory.instance().handle(scope, null, e);
                           } catch (RuntimeException e) {
                               return ErrorHandlerFactory.instance().handle(scope, null, e);
                           }
                           if (numeric) {
                               throw new VariableNotFoundException(key, scope, sourceCode);
                           }

                           if (defaultValue != null) {
                               return defaultValue;
                           } else {
                               throw new VariableNotFoundException(key, scope);
                           }
                       }
        );
        node[0].metaAttribute(VARIABLE, key);
        return node[0];

    }
}
