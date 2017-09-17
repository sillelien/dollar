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

package dollar.api.types;

import dollar.api.DollarStatic;
import dollar.api.MetaKey;
import dollar.api.Pipeable;
import dollar.api.SubType;
import dollar.api.Value;
import dollar.api.exceptions.LambdaRecursionException;
import dollar.api.execution.DollarExecutor;
import dollar.api.plugin.Plugins;
import dollar.api.types.meta.MetaConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DollarLambda implements java.lang.reflect.InvocationHandler {

    private static final int MAX_STACK_DEPTH = 100;
    @Nullable
    private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);
    @NotNull
    private static final ThreadLocal<List<DollarLambda>> notifyStack = ThreadLocal.withInitial(ArrayList::new);
    @NotNull
    private static final ThreadLocal<List<DollarLambda>> stack = ThreadLocal.withInitial(() -> new ArrayList<>());
    @NotNull
    protected final Pipeable lambda;
    @NotNull
    protected final ConcurrentHashMap<MetaKey, Object> meta = new ConcurrentHashMap<>();
    private final boolean fixable;
    @NotNull
    private final Value in;
    @NotNull
    private final ConcurrentHashMap<String, Pipeable> listeners = new ConcurrentHashMap<>();

    public DollarLambda(@NotNull Pipeable lambda) {
        this(lambda, true);
    }

    /**
     * @param lambda  the lambda to lazily evaluate.
     * @param fixable can we fix the value of the lambda with the use of $fix(boolean) - if you're waiting for a future
     *                to complete then false is a good value.
     */
    public DollarLambda(@NotNull Pipeable lambda, boolean fixable) {
        this.fixable = fixable;

        in = DollarStatic.$(false);
        this.lambda = lambda;
    }


    @NotNull
    public Value _constrain(@NotNull Value source, @Nullable Value constraint, @Nullable SubType constraintSource) {
        if ((constraint == null) || (constraintSource == null)) {
            return source;
        }
        SubType constraintFingerprint = (SubType) meta.get(MetaConstants.CONSTRAINT_FINGERPRINT);
        if ((constraintFingerprint == null) || constraintSource.equals(constraintFingerprint)) {
            meta.put(MetaConstants.CONSTRAINT_FINGERPRINT, constraintSource);
            return source;
        } else {
            throw new ConstraintViolation(source, constraint, constraintFingerprint, constraintSource);
        }
    }

    @NotNull
    public Value execute() throws Exception {return executor.executeNow(() -> lambda.pipe(in)).get();}

    @Nullable
    @Override
    public Object invoke(@Nullable Object proxy, @NotNull Method method, @Nullable Object[] args) throws Throwable {
        if (stack.get().size() > MAX_STACK_DEPTH) {
            in.error("Stack consists of the following Lambda types: ");
            for (DollarLambda stackEntry : stack.get()) {
                in.error(stackEntry.lambda.getClass() + ":" + stackEntry.meta);
            }
            throw new LambdaRecursionException(stack.get().size());

        }
        try {
            if (proxy == null) {
                return null;
            } else if (Objects.equals(Object.class, method.getDeclaringClass())) {
                String name = method.getName();
                switch (name) {
                    case "equals":
                        return execute().equals(args[0]);
                    case "hashCode":
                        return execute().hashCode();
                    case "toString":
                        return execute().toString();
                    default:
                        throw new IllegalStateException(String.valueOf(method));
                }
            } else if ("dynamic".equals(method.getName())) {
                return true;
            } else if ("$unwrap".equals(method.getName())) {
                return proxy;
//                return lambda.pipe(in).$unwrap();
            } else if ("$fix".equals(method.getName())) {
                if (fixable) {
                    if (args.length == 1) {
                        return lambda.pipe(DollarFactory.fromValue(args[0])).$fix((Boolean) args[0]);
                    } else {
                        return lambda.pipe(DollarFactory.fromValue(args[1])).$fix((int) args[0], (Boolean) args[1]);

                    }
                } else {
                    return proxy;
                }
            } else if ("$fixDeep".equals(method.getName())) {
                if (fixable) {
                    if ((args == null) || (args.length == 0)) {
                        return lambda.pipe(DollarFactory.fromValue(false)).$fixDeep();
                    } else {
                        return lambda.pipe(DollarFactory.fromValue(args[0])).$fixDeep((Boolean) args[0]);
                    }
                } else {
                    return proxy;
                }
            } else if ("$constrain".equals(method.getName())) {
                return _constrain((Value) proxy, (Value) args[0], (SubType) args[1]);
            } else if ("constraintLabel".equals(method.getName())) {
                return meta.get(MetaConstants.CONSTRAINT_FINGERPRINT);
            } else if ("metaAttribute".equals(method.getName()) && (args.length == 1)) {
                return meta.get(MetaKey.of(args[0]));
            } else if ("metaAttribute".equals(method.getName()) && (args.length == 2)) {
                meta.put(MetaKey.of(args[0]), String.valueOf(args[1]));
                return null;
            } else if ("_scope".equals(method.getName())) {
                throw new Exception("Deprectated (_scope)");
//                if(args.length == 0) {
//                    return meta.get("scope");
//                } else {
//                    meta.put("scope",args[0]);
//                    return this;
//                }
//            } else if ("metaAttribute".equals(method.getName())) {
//                meta.put(String.of(args[0]), String.of(args[1]));
//                return null;
            } else if ("meta".equals(method.getName()) && (args.length == 1)) {
                return meta.get(MetaKey.of(args[0]));
            } else if ("meta".equals(method.getName()) && (args.length == 2)) {
                meta.put(MetaKey.of(args[0]), args[1]);
                return null;
            } else if ("$listen".equals(method.getName())) {
                String listenerId = UUID.randomUUID().toString();
                if (args.length == 2) {
                    listenerId = String.valueOf(args[1]);
                }
                listeners.put(listenerId, (Pipeable) args[0]);
                return DollarStatic.$(listenerId);
            } else if ("$notify".equals(method.getName())) {
                if (notifyStack.get().contains(this)) {
                    //throw new IllegalStateException("Recursive notify loop detected");
                    return proxy;
                }
                notifyStack.get().add(this);
                final Value value = execute();
                for (Pipeable listener : listeners.values()) {
                    listener.pipe(value);
                }
                notifyStack.get().remove(this);
                return proxy;
            } else if ("$remove".equals(method.getName())) {
                listeners.remove(args[0].toString());
                return args[0];
            } else if ("hasErrors".equals(method.getName())) {
                return false;
            } else if ("$copy".equals(method.getName()) || "copy".equals(method.getName())) {
                return proxy;
            } else {
//            System.err.println(method);

                Value out = execute();
                if (out == null) {
                    return null;
                }
                try {
                    stack.get().add(this);
                    return method.invoke(out, args);
                } finally {
                    stack.get().remove(stack.get().size() - 1);
                }
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
