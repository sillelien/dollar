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

import dollar.api.DollarException;
import dollar.api.Value;
import dollar.api.guard.Guarded;
import dollar.api.guard.Guards;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class DollarGuard implements java.lang.reflect.InvocationHandler {

    @NotNull
    private final Value in;


    public DollarGuard(@NotNull Value in) {

        this.in = in;

    }

    @Nullable
    @Override
    public Object invoke(@NotNull Object proxy, @NotNull Method method, @Nullable Object[] args) throws Throwable {
        try {
            String name = method.getName();

            if ("$unwrap".equals(name)) {
                return in.$unwrap();
            }
            if ("$fix".equals(name)) {
                if (args == null || args.length == 0) {
                    throw new AssertionError("Args for $fix was " + Arrays.toString(args));
                }
                if (args.length == 1) {
                    return in.$fix((Boolean) args[0]);
                } else {
                    return in.$fix((Integer) args[0], (Boolean) args[1]);
                }
            }
            if ("$fixDeep".equals(name)) {
                if (args == null) {
                    return in.$fixDeep();
                } else {
                    return in.$fixDeep((Boolean) args[0]);
                }
            }
            if ("$notify".equals(name)) {
                return in.$notify((NotificationType) args[0], (Value) args[1]);
            }


            if (Objects.equals(Object.class, method.getDeclaringClass())) {
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
            throw new DollarException(e);
        }
    }

    @Nullable
    Object invokeWithGuards(@NotNull Method method, @Nullable Object[] args, @NotNull Guarded[] guards) throws
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
