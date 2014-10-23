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

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarLambda implements java.lang.reflect.InvocationHandler {

    private final var in;
    private Pipeable lambda;

    public DollarLambda(var in, Pipeable lambda) {

        this.in = in;
        this.lambda = lambda;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (Object.class == method.getDeclaringClass()) {
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
            }
            if (method.getName().equals("isLambda")) {
                return true;
            }
            if (method.getName().equals("hasErrors")) {
                return false;
            }
            if (method.getName().equals("$copy") || method.getName().equals("copy")) {
                return proxy;
            }
            System.err.println(method);

            return method.invoke(lambda.pipe(in), args);
        } catch (Exception e) {
            if (method.getReturnType().isAssignableFrom(var.class)) {
                return DollarStatic.handleError(e, null);
            } else {
                DollarStatic.handleError(e, null);
                return null;
            }
        }
    }
}
