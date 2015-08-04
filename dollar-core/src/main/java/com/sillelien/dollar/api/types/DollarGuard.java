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
import com.sillelien.dollar.api.guard.Guarded;
import com.sillelien.dollar.api.guard.Guards;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DollarGuard implements java.lang.reflect.InvocationHandler {

    private final var in;


    public DollarGuard(var in) {

        this.in = in;

    }

    @Override
    public Object invoke(Object proxy, @NotNull Method method, Object[] args) throws Throwable {
        try {
            String name = method.getName();

            if ("_unwrap".equals(name)) {
                return in._unwrap();
            }
            if ("_fix".equals(name)) {
                if (args.length == 1) {
                    return in._fix((Boolean) args[0]);
                } else {
                    return in._fix((Integer) args[0], (Boolean) args[1]);
                }
            }
            if ("_fixDeep".equals(name)) {
                if (args == null) {
                    return in._fixDeep();
                } else {
                    return in._fixDeep((Boolean) args[0]);
                }
            }
            if ("$notify".equals(name)) {
                return in.$notify();
            }


            if (Object.class == method.getDeclaringClass()) {
                if ("equals".equals(name)) {
                    return in.equals(args[0]);
                } else if ("hashCode".equals(name)) {
                    return in.hashCode();
                } else if ("toString".equals(name)) {
                    return in.toString();
                } else {
                    throw new IllegalStateException(String.valueOf(method));
                }
            }
            Guards guardsAnnotations = method.getAnnotation(Guards.class);

            if (guardsAnnotations != null) {
                Guarded[] guards = guardsAnnotations.value();
                return invokeWithGuards(method, args, guards);
            } else {
                Guarded[] guardedAnnotations = method.getAnnotationsByType(Guarded.class);
                if (guardedAnnotations.length > 0) {
                    return invokeWithGuards(method, args, guardedAnnotations);
                } else {
                    return method.invoke(in, args);
                }
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (Exception e) {
            return DollarStatic.logAndRethrow(e);
        }
    }

    Object invokeWithGuards(@NotNull Method method, Object[] args, @NotNull Guarded[] guards) throws
                                                                                              InstantiationException,
                                                                                              IllegalAccessException,
                                                                                              InvocationTargetException {
        for (Guarded guard : guards) {
            guard.value().newInstance().preCondition(in, method, args);
        }
        Object invoke = method.invoke(in, args);
        for (Guarded guard : guards) {
            guard.value().newInstance().postCondition(in, method, args, invoke);
        }
        return invoke;
    }
}
