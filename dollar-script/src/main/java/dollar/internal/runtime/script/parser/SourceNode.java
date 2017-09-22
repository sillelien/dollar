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

package dollar.internal.runtime.script.parser;

import dollar.api.DollarStatic;
import dollar.api.MetaKey;
import dollar.api.Pipeable;
import dollar.api.Scope;
import dollar.api.SubType;
import dollar.api.Type;
import dollar.api.TypePrediction;
import dollar.api.Value;
import dollar.api.exceptions.LambdaRecursionException;
import dollar.api.execution.DollarExecutor;
import dollar.api.plugin.Plugins;
import dollar.api.script.DollarParser;
import dollar.api.script.Source;
import dollar.api.script.TypeLearner;
import dollar.api.types.ConstraintViolation;
import dollar.api.types.DollarFactory;
import dollar.api.types.meta.MetaConstants;
import dollar.api.types.prediction.SingleValueTypePrediction;
import dollar.internal.runtime.script.DollarUtilFactory;
import dollar.internal.runtime.script.ErrorHandlerFactory;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import dollar.internal.runtime.script.parser.scope.PureScope;
import dollar.internal.runtime.script.parser.scope.ScriptScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static dollar.api.DollarStatic.$;
import static dollar.api.DollarStatic.getConfig;
import static dollar.api.types.meta.MetaConstants.*;

public class SourceNode implements java.lang.reflect.InvocationHandler {
    private static final int MAX_STACK_DEPTH = 100;
    @Nullable
    private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);
    //    public static final String RETURN_SCOPE = "return-scope";
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(SourceNode.class);
    @NotNull
    private static final List<String> nonScopeOperations = Arrays.asList(
            "dynamic", "$constrain");
    @NotNull
    private static final ThreadLocal<List<SourceNode>> notifyStack = ThreadLocal.withInitial(ArrayList::new);
    @NotNull
    private static final ThreadLocal<List<SourceNode>> stack = ThreadLocal.withInitial(ArrayList::new);
    @NotNull
    private static final TypeLearner typeLearner;
    @NotNull
    private final String id;
    @NotNull
    private final List<Value> inputs;
    @NotNull

    private final Pipeable lambda;
    @NotNull
    private final ConcurrentHashMap<String, Pipeable> listeners = new ConcurrentHashMap<>();
    @NotNull
    private final ConcurrentHashMap<MetaKey, Object> meta = new ConcurrentHashMap<>();
    @NotNull
    private final String name;
    private final boolean newScope;
    @NotNull
    private final Op operation;
    @NotNull
    private final DollarParser parser;
    private final boolean pure;
    private final boolean scopeClosure;
    @NotNull
    private final Source source;
    @NotNull
    private final SourceNodeOptions sourceNodeOptions;
    @Nullable
    private volatile TypePrediction prediction;

    static {
        try {
            typeLearner = Objects.requireNonNull(Plugins.sharedInstance(TypeLearner.class));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public SourceNode(@NotNull Pipeable lambda,
                      @NotNull Source source,
                      @NotNull List<Value> inputs,
                      @NotNull String name,
                      @NotNull DollarParser parser,
                      @NotNull SourceNodeOptions sourceNodeOptions,
                      @NotNull String id,
                      boolean pure, @NotNull Op operation) {

        this.pure = pure;
        this.operation = operation;
        this.sourceNodeOptions = sourceNodeOptions;

        if (sourceNodeOptions.isScopeClosure()) {
            this.lambda = vars -> {
                List<Scope> attachedScopes = DollarUtilFactory.util().scopes().stream().filter(i -> !i.isRoot()).collect(
                        Collectors.toList());
                return DollarUtilFactory.util().node(operation, name + "-closure", pure,
                                                     new SourceNodeOptions(false, false, sourceNodeOptions.isParallel()),
                                                     parser, source, null, inputs, vars2 -> {
                            for (Scope scope : attachedScopes) {
                                DollarUtilFactory.util().pushScope(scope);
                            }
                            try {
                                return lambda.pipe(vars);
                            } finally {
                                Collections.reverse(attachedScopes);
                                for (Scope scope : attachedScopes) {
                                    DollarUtilFactory.util().popScope(scope);
                                }

                            }

                        });
            };
        } else {
            this.lambda = lambda;
        }
        this.parser = parser;
        newScope = sourceNodeOptions.isNewScope();
        scopeClosure = sourceNodeOptions.isScopeClosure();
        this.id = id;
        this.inputs = inputs;
        this.name = name;
        this.source = source;
        meta.put(OPERATION_NAME, name);
        meta.put(OPERATION, operation);
        if (!this.pure) {
            meta.put(IMPURE, "true");
        }
        meta.put(ID, id);
        try {
            Type opType = operation.typeFor(inputs.toArray(new Value[inputs.size()]));
            if (opType != null) {
                log.debug(name + ":" + opType);
                meta.put(MetaConstants.TYPE_HINT, opType);
            }
        } catch (ArrayIndexOutOfBoundsException iobe) {
            throw new DollarScriptException(iobe,
                                            "Index out of bounds, the Op.typeFor() function might be incorrect for op " + operation.name() + " message was " + iobe.getMessage());
        }
    }

    @NotNull
    private Value _constrain(@NotNull Value source, @Nullable Value constraint, @Nullable SubType constraintSource) {
        if ((constraint == null) || (constraintSource == null)) {
            return source;
        }
        SubType constraintFingerprint = (SubType) meta.get(CONSTRAINT_FINGERPRINT);
        if ((constraintFingerprint == null) || constraintSource.equals(constraintFingerprint)) {
            meta.put(CONSTRAINT_FINGERPRINT, constraintSource);
            return source;
        } else {
            throw new ConstraintViolation(source, constraint, constraintFingerprint, constraintSource);
        }
    }

    @NotNull
    public Value execute(boolean parallel) throws Exception {return executor.executeNow(() -> executePipe($(parallel))).get();}

    @NotNull
    private Value executePipe(@NotNull Value Value) throws Exception {
        return lambda.pipe(Value);
    }

    @Nullable
    @Override
    public Object invoke(@NotNull Object proxy, @NotNull Method method, @Nullable Object[] args) {
        DollarStatic.context().source(source);
        try {
            if (Objects.equals(method.getName(), "source")) {
                return source;
            }
            if (Objects.equals(method.getName(), "dynamic")) {
                return true;
            }

            if (Objects.equals(method.getName(), "$constrain")) {
                if (args != null) {
                    return _constrain((Value) proxy, (Value) args[0], (SubType) args[1]);
                }
            }

            if (Objects.equals(method.getName(), "$type")) {
                if (meta.get(MetaConstants.TYPE_HINT) != null) {
                    return meta.get(MetaConstants.TYPE_HINT);
                } else if (prediction != null) {
                    return prediction.probableType();
                } else {
                    return Type._ANY;
                }
            }

            if (Objects.equals(method.getName(), "predictType")) {
                if (prediction == null) {
                    prediction = typeLearner.predict(name, source, inputs);
                }
                if (meta.get(MetaConstants.TYPE_HINT) != null) {
                    return new SingleValueTypePrediction((Type) meta.get(MetaConstants.TYPE_HINT));
                }
                return prediction;
            }

            Object result = null;
            //Some operations do not require a valid scope
            if ((!method.getName().startsWith("$fix") && (nonScopeOperations.contains(
                    method.getName()) || method.getDeclaringClass().equals(
                    Value.class) || method.getDeclaringClass().equals(
                    Value.class)))) {
                //This method does not require a valid scope for execution
                try {
                    result = invokeMain(proxy, method, args);
                } catch (Throwable throwable) {
                    return DollarUtilFactory.util().scope().handleError(throwable, source);
                }
            } else {
                //This method does require a scope

                List<Scope> attachedScopes = (List<Scope>) meta.get(SCOPES);
                if (attachedScopes != null) {
                    List<Scope> scopes = new ArrayList<>(attachedScopes);
                    for (Scope scope : scopes) {
                        DollarUtilFactory.util().pushScope(scope);
                    }
                }
                try {
                    Scope useScope;
                    if (newScope) {
                        if (pure || DollarUtilFactory.util().scope().pure()) {
                            useScope = new PureScope(DollarUtilFactory.util().scope(),
                                                     DollarUtilFactory.util().scope().source(), name,
                                                     DollarUtilFactory.util().scope().file());
                        } else {
                            if (DollarUtilFactory.util().scope().pure() && (operation.pure() != null) && !operation.pure()) {
                                throw new DollarScriptException("Attempted to create an impure scope within a pure scope " +
                                                                        "(" + DollarUtilFactory.util().scope() + ") for " + name,
                                                                source);
                            }
                            useScope = new ScriptScope(DollarUtilFactory.util().scope(), name, false, false);
                        }
                    } else {
                        useScope = DollarUtilFactory.util().scope();
                    }

                    result = DollarUtilFactory.util().inScope(true, useScope, s -> {
                        if (DollarStatic.getConfig().debugScope()) {
                            log.info("EXE: {} {} for {}", name, method.getName(), source.getShortSourceMessage());
                        }
                        if (DollarStatic.getConfig().debugParallel()) {
                            if (false) {
                                log.info("PARALLEL: {} {}", name, method);
                            }
                        }
                        try {
                            try {
                                stack.get().add(this);
                                return invokeMain(proxy, method, args);
                            } finally {
                                stack.get().remove(stack.get().size() - 1);
                            }

                        } catch (Throwable throwable) {
                            return DollarUtilFactory.util().scope().handleError(throwable, source);
                        }
                    }).get();
                } finally {

                    if (attachedScopes != null) {
                        List<Scope> scopes = new ArrayList<>(attachedScopes);
                        Collections.reverse(scopes);
                        for (Scope scope : scopes) {
                            DollarUtilFactory.util().popScope(scope);
                        }
                    }
                }

            }

            boolean dynamicVarResult = (result instanceof Value) && ((Value) result).dynamic();
            if (dynamicVarResult && (meta.get(SCOPES) != null)) {
                if (((Value) result).meta(SCOPES) == null) {
                    ((Value) result).meta(SCOPES, meta.get(SCOPES));
                } else {
                    ArrayList<Scope> newScopes = new ArrayList<>((Collection<? extends Scope>) meta.get(SCOPES));
                    newScopes.addAll(((Value) result).meta(SCOPES));
                    ((Value) result).meta(SCOPES, newScopes);
                }
            }

            if (method.getName().startsWith("$fixDeep") && (result instanceof Value)) {

                typeLearner.learn(name, source, inputs, ((Value) result).$type());
            }
            return result;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return ErrorHandlerFactory.instance().handle(DollarUtilFactory.util().scope(), source, e);
        }
    }

    @Nullable
    private Object invokeMain(@Nullable Object proxy, @NotNull Method method, @Nullable Object[] args) throws Throwable {
        if (stack.get().size() > MAX_STACK_DEPTH) {
            throw new LambdaRecursionException(stack.get().size());

        }
        try {
            if (proxy == null) {
                return null;
            } else if (Objects.equals(Object.class, method.getDeclaringClass())) {
                String name = method.getName();
                if ("equals".equals(name)) {
                    return execute(false).equals(args[0]);
                } else if ("hashCode".equals(name)) {
                    return execute(false).hashCode();
                } else if ("toString".equals(name)) {
                    return execute(false).toString();
                } else {
                    throw new IllegalStateException(String.valueOf(method));
                }

            } else if ("$unwrap".equals(method.getName())) {
                return proxy;
//                return lambda.pipe(in).$unwrap();
            } else if ("$fix".equals(method.getName())) {
                if (args.length == 1) {
                    return executePipe(DollarFactory.fromValue(args[0]));
                } else {
                    if ((int) args[0] > 1) {
                        return executePipe(DollarFactory.fromValue(args[1])).$fix((int) args[0] - 1, (Boolean) args[1]);
                    } else {
                        return executePipe(DollarFactory.fromValue(args[1]));
                    }

                }

            } else if ("$fixDeep".equals(method.getName())) {

                if ((args == null) || (args.length == 0)) {
                    return executePipe(DollarFactory.fromValue(false)).$fixDeep(false);
                } else {
                    return executePipe(DollarFactory.fromValue(args[0])).$fixDeep((Boolean) args[0]);
                }


            } else if ("constraintLabel".equals(method.getName())) {
                return meta.get(CONSTRAINT_FINGERPRINT);
            } else if ("metaAttribute".equals(method.getName()) && (((args != null) ? args.length : 0) == 1)) {
                return meta.get(MetaKey.of(args[0]));
            } else if ("metaAttribute".equals(method.getName()) && (((args != null) ? args.length : 0) == 2)) {
                meta.put(MetaKey.of(args[0]), String.valueOf(args[1]));
                return null;
            } else if ("meta".equals(method.getName()) && (((args != null) ? args.length : 0) == 1)) {
                return meta.get(MetaKey.of(args[0]));
            } else if ("meta".equals(method.getName()) && (((args != null) ? args.length : 0) == 2)) {
                meta.put(MetaKey.of(args[0]), args[1]);
                return null;
            } else if ("$listen".equals(method.getName())) {
                String listenerId = UUID.randomUUID().toString();
                if ((args != null) && (args.length == 2)) {
                    listenerId = String.valueOf(args[1]);
                }
                if (getConfig().debugEvents()) {
                    log.info("$listen called on a source node, id used is {} source is {} ", listenerId,
                             source.getShortSourceMessage());
                }
                listeners.put(listenerId, (Pipeable) args[0]);
                return $(listenerId);
            } else if ("$notify".equals(method.getName())) {
                if (notifyStack.get().contains(this)) {
                    throw new IllegalStateException("Recursive notify loop detected " + notifyStack.get());
//                    return proxy;
                }
                if (getConfig().debugEvents()) {
                    log.info("$notify called on a source node, id used is {} source is {} listener stack size is {}", id,
                             source.getShortSourceMessage(), listeners.size());
                }
                notifyStack.get().add(this);
                Value updateValue;

                if (args != null && (args.length == 2) && (args[1] != null)) {
                    updateValue = (Value) args[1];
                } else if (args != null && (args.length == 1) && (args[0] instanceof Value)) {
                    updateValue = (Value) args[0];
                } else {
                    updateValue = (Value) proxy;
                }
//                Value updateValue = ((Value) proxy).$fix(2, false);
                try {
                    for (Pipeable listener : listeners.values()) {
                        listener.pipe(updateValue);
                    }
                } finally {
                    notifyStack.get().remove(this);
                }
                return proxy;
            } else if ("$cancel".equals(method.getName())) {
                listeners.remove(String.valueOf(args[0]));
                return args[0];
            } else if ("hasErrors".equals(method.getName())) {
                return false;
            } else if ("$copy".equals(method.getName()) || "copy".equals(method.getName())) {
                return proxy;
            } else {
//            System.err.println(method);

                Value out = execute(false);
                if (out == null) {
                    return null;
                }
                return method.invoke(out, args);
            }
        } catch (InvocationTargetException e) {
            return DollarUtilFactory.util().scope().handleError(e, source);
        }
    }
}
