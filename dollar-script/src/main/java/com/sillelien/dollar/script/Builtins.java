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

package com.sillelien.dollar.script;

import com.sillelien.dollar.api.StateAware;
import com.sillelien.dollar.api.collections.ImmutableList;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.types.ErrorType;
import com.sillelien.dollar.api.var;
import com.sillelien.dollar.script.api.Scope;
import com.sillelien.dollar.script.api.exceptions.BuiltinNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.sillelien.dollar.api.DollarStatic.$;
import static com.sillelien.dollar.api.DollarStatic.$void;

public class Builtins {

    private static final double DAY_IN_MILLIS = 24.0 * 60.0 * 60.0 * 1000.0;
    @NotNull private static final HashMap<String, Builtin<var>> map;

    static {
        map = new HashMap<>();
        addDollarStyle(1, 1, (pure, args, scope) -> args.get(0).$abs(), true, "ABS");
        addJavaStyle(1, 1, (pure, args, scope) -> args.get(0).$size(), true, "COUNT");

        addJavaStyle(1, Integer.MAX_VALUE, (pure, args, scope) -> {
            String message = args.get(0).$S();
            ArrayList<var> values = new ArrayList<>(args);
            values.remove(0);
            return $(String.format(message, values.stream().map(var::toJavaObject).toArray()));
        }, true, "FORMAT");
        addJavaStyle(1, 1, (pure, args, scope) -> {
            return args.get(0).$list().stream().min((o1, o2) -> (int) Math.signum(o1.toDouble() - o2.toDouble())).get();
        }, true, "MIN");
        addJavaStyle(1, 1, (pure, args, scope) -> {
            return args.get(0).$list().stream().max((o1, o2) -> (int) Math.signum(o1.toDouble() - o2.toDouble())).get();
        }, true, "MAX");
        addJavaStyle(1, 1, (pure, args, scope) -> {
            return $(args.get(0).$list().stream().sorted().collect(Collectors.toList()));
        }, true, "SORT");

        addJavaStyle(1, 1, (pure, args, scope) -> {
            return $(args.get(0).$list().get(0));
        }, true, "FIRST");

        addJavaStyle(1, 1, (pure, args, scope) -> {
            ImmutableList<var> list = args.get(0).$list();
            return $(list.get(list.size() - 1));
        }, true, "LAST");
        addDollarSingleNoScope(false, StateAware::$start, "START");
        addDollarSingleNoScope(false, StateAware::$stop, "STOP");
        addDollarSingleNoScope(false, StateAware::$create, "CREATE");
        addDollarSingleNoScope(false, StateAware::$destroy, "DESTROY");
        addDollarSingleNoScope(false, StateAware::$pause, "PAUSE");
        addDollarSingleNoScope(false, StateAware::$unpause, "UNPAUSE");
        addDollarSingleNoScope(false, StateAware::$state, "STATE");

        addJavaStyle(1, 1, (pure, args, scope) -> args.get(0).toDouble() / DAY_IN_MILLIS, true, "Millis", "Milli", "MS",
                     "Milliseconds", "Millisecond");
        addJavaStyle(1, 1, (pure, args, scope) -> args.get(0).toDouble() / (24.0 * 60.0 * 60.0), true, "Secs", "S",
                     "Sec",
                     "Seconds",
                     "Second");
        addJavaStyle(1, 1, (pure, args, scope) -> args.get(0).toDouble() / (24.0 * 60.0), true, "M", "Minutes",
                     "Minute");
        addJavaStyle(1, 1, (pure, args, scope) -> {
            return args.get(0).toDouble() / 24.0;
        }, true, "Hrs", "Hours", "H", "Hour");
        addJavaStyle(1, 1, (pure, args, scope) -> {
            try {
                Thread.sleep((long) (args.get(0).toDouble() * DAY_IN_MILLIS));
                return args.get(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return $void();
            }
        }, false, "SLEEP");
        addDollarStyle(1, 2, (pure, args, scope) -> {
            if (args.size() == 1) {
                return DollarFactory.failure(ErrorType.valueOf(args.get(0).toString()), "", true);
            } else {
                return DollarFactory.failure(ErrorType.valueOf(args.get(0).toString()), args.get(1).toString(), true);
            }
        }, true, "ERROR");
        addJavaStyle(1, 1, (pure, args, scope) -> args.get(0).toDouble(), true, "Days", "Day", "D");
        addJavaStyle(1, 1, (pure, args, scope) -> args.get(0).toString().length(), true, "LEN");
        addJavaStyle(0, 0, (pure, args, scope) -> $(new Date()), false, "DATE");
        addJavaStyle(0, 0, (pure, args, scope) -> System.currentTimeMillis(), false, "TIME");
        addJavaStyle(2, 2, (pure, args, scope) -> args.get(0).toString().matches(args.get(1).$S()), true, "MATCHES");
    }

    @NotNull public static var execute(String name, List<var> parameters, Scope scope, boolean pure) {
        final Builtin<var> builtin = map.get(name);
        if (builtin == null) {
            throw new BuiltinNotFoundException(name);
        }
        return builtin.execute(pure, parameters, scope);
    }

    public static boolean exists(String name) {
        return map.containsKey(name);
    }

    private static <T> void addJavaStyle(int minargs, int maxargs, Builtin.JavaStyle<T> lambda, boolean pure,
                                         @NotNull String... names) {
        for (String name : names) {
            map.put(name, new Builtin.BuiltinImpl(name, lambda, minargs, maxargs, pure));
        }
    }

    private static void addDollarStyle(int minargs, int maxargs, Builtin.DollarStyle lambda, boolean pure,
                                       @NotNull String... names) {
        for (String name : names) {
            map.put(name, new Builtin.BuiltinImpl(name, lambda, minargs, maxargs, pure));
        }
    }

    private static void addDollarSingleNoScope(boolean isPure, @NotNull Function<var, var> lambda,
                                               @NotNull String... names) {
        for (String name : names) {
            map.put(name, new Builtin.BuiltinImpl(name, (pure, args, scope) -> lambda.apply((var) args.get(0)), 1, 1,
                                                  isPure));
        }
    }

    public static boolean isPure(String lhsString) {
        return map.get(lhsString) != null && ((Builtin.BuiltinImpl) map.get(lhsString)).isPure();
    }
}
