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

package me.neilellis.dollar.types;

import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.var;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarLambda implements java.lang.reflect.InvocationHandler {

    private static final ThreadLocal<List<DollarLambda>> notifyStack = new ThreadLocal<List<DollarLambda>>() {
        @Override
        protected List<DollarLambda> initialValue() {
            return new ArrayList<DollarLambda>();
        }
    };
    private final var in;
    private Pipeable lambda;
    private ConcurrentHashMap<String, Pipeable> listeners = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> meta = new ConcurrentHashMap<>();

    public DollarLambda(var in, Pipeable lambda) {

        this.in = in;
        this.lambda = lambda;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (proxy == null) {
                return null;
            } else if (Object.class == method.getDeclaringClass()) {
                String name = method.getName();
                if ("equals".equals(name)) {
                    return lambda.pipe(in).equals(args[0]);
                } else if ("hashCode".equals(name)) {
                    return lambda.pipe(in).hashCode();
                } else if ("toString".equals(name)) {
                    return lambda.pipe(in).toString();
                } else {
                    throw new IllegalStateException(String.valueOf(method));
                }
            } else if (method.getName().equals("isLambda")) {
                return true;
            } else if (method.getName().equals("getMetaAttribute")) {
                return meta.get(args[0]);
            } else if (method.getName().equals("setMetaAttribute")) {
                meta.put(String.valueOf(args[0]), String.valueOf(args[1]));
                return null;
            } else if (method.getName().equals("$listen")) {
                String listenerId = UUID.randomUUID().toString();
                if (args.length == 2) {
                    listenerId = String.valueOf(args[1]);
                }
                listeners.put(listenerId, (Pipeable) args[0]);
                return listenerId;
            } else if (method.getName().equals("$notify")) {
                if (notifyStack.get().contains(this)) {
                    //throw new IllegalStateException("Recursive notify loop detected");
                    return proxy;
                }
                notifyStack.get().add(this);
                for (Pipeable pipeable : listeners.values()) {
                    pipeable.pipe((var) args[0]);
                }
                notifyStack.get().remove(this);
                return null;
            } else if (method.getName().equals("$remove")) {
                listeners.remove(args[0]);
                return null;
            } else if (method.getName().equals("hasErrors")) {
                return false;
            } else if (method.getName().equals("$copy") || method.getName().equals("copy")) {
                return proxy;
            } else {
//            System.err.println(method);

                var out = lambda.pipe(in);
                if (out == null) {
                    return null;
                }
                return method.invoke(out, args);
            }
        } catch (Exception e) {
            if (method.getReturnType().isAssignableFrom(var.class)) {
                return DollarStatic.handleError(e, null);
            } else {
                throw e;
            }
        }
    }
}
