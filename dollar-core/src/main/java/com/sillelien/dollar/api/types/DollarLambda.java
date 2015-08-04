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

package com.sillelien.dollar.api.types;

import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.exceptions.LambdaRecursionException;
import com.sillelien.dollar.api.execution.DollarExecutor;
import com.sillelien.dollar.api.plugin.Plugins;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DollarLambda implements java.lang.reflect.InvocationHandler {

    private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);

    private static final int MAX_STACK_DEPTH = 100;
    private static final ThreadLocal<List<DollarLambda>> notifyStack = new ThreadLocal<List<DollarLambda>>() {
        @NotNull @Override
        protected List<DollarLambda> initialValue() {
            return new ArrayList<>();
        }
    };
    private static final ThreadLocal<List<DollarLambda>> stack = new ThreadLocal<List<DollarLambda>>() {
        @NotNull @Override
        protected List<DollarLambda> initialValue() {
            return new ArrayList<>();
        }
    };
    protected final ConcurrentHashMap<String, String> meta = new ConcurrentHashMap<>();
    protected final Pipeable lambda;
    private final var in;
    private final boolean fixable;
    private final ConcurrentHashMap<String, Pipeable> listeners = new ConcurrentHashMap<>();

    public DollarLambda(Pipeable lambda) {
        this(lambda, true);
    }

    /**
     * @param lambda  the lambda to lazily evaluate.
     * @param fixable can we fix the value of the lambda with the use of _fix(boolean) - if you're waiting for a future
     *                to complete then false is a good value.
     */
    public DollarLambda(Pipeable lambda, boolean fixable) {
        this.fixable = fixable;

        this.in = DollarStatic.$(false);
        this.lambda = lambda;
    }

    @Nullable @Override
    public Object invoke(@Nullable Object proxy, @NotNull Method method, Object[] args) throws Throwable {
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
            } else if (Object.class == method.getDeclaringClass()) {
                String name = method.getName();
                if ("equals".equals(name)) {
                    return execute().equals(args[0]);
                } else if ("hashCode".equals(name)) {
                    return execute().hashCode();
                } else if ("toString".equals(name)) {
                    return execute().toString();
                } else {
                    throw new IllegalStateException(String.valueOf(method));
                }
            } else if (method.getName().equals("dynamic")) {
                return true;
            } else if (method.getName().equals("_unwrap")) {
                return proxy;
//                return lambda.pipe(in)._unwrap();
            } else if (method.getName().equals("_fix")) {
                if (fixable) {
                    if (args.length == 1) {
                        return lambda.pipe(DollarFactory.fromValue(args[0]))._fix((Boolean) args[0]);
                    } else {
                        return lambda.pipe(DollarFactory.fromValue(args[1]))._fix((int) args[0], (Boolean) args[1]);

                    }
                } else {
                    return proxy;
                }
            } else if (method.getName().equals("_fixDeep")) {
                if (fixable) {
                    if (args == null || args.length == 0) {
                        return lambda.pipe(DollarFactory.fromValue(false))._fixDeep();
                    } else {
                        return lambda.pipe(DollarFactory.fromValue(args[0]))._fixDeep((Boolean) args[0]);
                    }
                } else {
                    return proxy;
                }
            } else if (method.getName().equals("getMetaAttribute")) {
                return meta.get(args[0].toString());
            } else if (method.getName().equals("setMetaAttribute")) {
                meta.put(String.valueOf(args[0]), String.valueOf(args[1]));
                return null;
            } else if (method.getName().equals("$listen")) {
                String listenerId = UUID.randomUUID().toString();
                if (args.length == 2) {
                    listenerId = String.valueOf(args[1]);
                }
                listeners.put(listenerId, (Pipeable) args[0]);
                return DollarStatic.$(listenerId);
            } else if (method.getName().equals("$notify")) {
                if (notifyStack.get().contains(this)) {
                    //throw new IllegalStateException("Recursive notify loop detected");
                    return proxy;
                }
                notifyStack.get().add(this);
                final var value = execute();
                for (Pipeable listener : listeners.values()) {
                    listener.pipe(value);
                }
                notifyStack.get().remove(this);
                return proxy;
            } else if (method.getName().equals("$remove")) {
                //noinspection SuspiciousMethodCalls
                listeners.remove(args[0]);
                return args[0];
            } else if (method.getName().equals("hasErrors")) {
                return false;
            } else if (method.getName().equals("$copy") || method.getName().equals("copy")) {
                return proxy;
            } else {
//            System.err.println(method);

                var out = execute();
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

    public var execute() throws Exception {return executor.executeNow(() -> lambda.pipe(in)).get();}
}
