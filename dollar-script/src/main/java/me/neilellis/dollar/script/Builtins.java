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

package me.neilellis.dollar.script;

import com.google.common.collect.ImmutableList;
import me.neilellis.dollar.StateAware;
import me.neilellis.dollar.script.exceptions.BuiltinNotFoundException;
import me.neilellis.dollar.var;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static me.neilellis.dollar.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class Builtins {

    private static final HashMap<String, Builtin<var>> map = new HashMap<>();


    public static var execute(String name, List<var> parameters, ScriptScope scope) {
        final Builtin<var> builtin = map.get(name);
        if (builtin == null) {
            throw new BuiltinNotFoundException(name);
        }
        return builtin.execute(parameters, scope);
    }

    public static boolean exists(String name) {
        return map.containsKey(name);
    }

    public static <T> void addJavaStyle(int minargs, int maxargs, Builtin.JavaStyle<T> lambda, String... names) {
        for (String name : names) {
            map.put(name, new Builtin.BuiltinImpl(lambda, minargs, maxargs));
        }
    }

    public static void addDollarStyle(int minargs, int maxargs, Builtin.DollarStyle lambda, String... names) {
        for (String name : names) {
            map.put(name, new Builtin.BuiltinImpl(lambda, minargs, maxargs));
        }
    }

    public static void addDollarSingleNoScope(Function<var, var> lambda, String... names) {
        for (String name : names) {
            map.put(name, new Builtin.BuiltinImpl((args, scope) -> lambda.apply((var) args.get(0)), 1, 1));
        }
    }


    static {
        addDollarStyle(1, 1, (args, scope) -> args.get(0).$abs(), "abs");
        addJavaStyle(1, 1, (args, scope) -> args.get(0).$size(), "count");

        addJavaStyle(1, Integer.MAX_VALUE, (args, scope) -> {
            String message = args.get(0).$S();
            ArrayList<var> values = new ArrayList<>(args);
            values.remove(0);
            return $(String.format(message, values.stream().map(i -> i.$()).toArray()));
        }, "format");
        addJavaStyle(1, 1, (args, scope) -> {
            return args.get(0).toList().stream().min((o1, o2) -> (int) Math.signum(o1.D() - o2.D())).get();
        }, "min");
        addJavaStyle(1, 1, (args, scope) -> {
            return args.get(0).toList().stream().max((o1, o2) -> (int) Math.signum(o1.D() - o2.D())).get();
        }, "max");
        addJavaStyle(1, 1, (args, scope) -> {
            return $(args.get(0).toList().stream().sorted().collect(Collectors.toList()));
        }, "sort");

        addJavaStyle(1, 1, (args, scope) -> {
            return $(args.get(0).toList().get(0));
        }, "first");

        addJavaStyle(1, 1, (args, scope) -> {
            ImmutableList<var> list = args.get(0).toList();
            return $(list.get(list.size() - 1));
        }, "last");
        addDollarSingleNoScope(StateAware::$start, "start");
        addDollarSingleNoScope(StateAware::$stop, "stop");
        addDollarSingleNoScope(StateAware::$create, "create");
        addDollarSingleNoScope(StateAware::$destroy, "destroy");
        addDollarSingleNoScope(StateAware::$pause, "pause");
        addDollarSingleNoScope(StateAware::$unpause, "unpause");
        addDollarSingleNoScope(StateAware::$state, "state");

        addJavaStyle(1, 1, (args, scope) -> args.get(0).D() / (24.0 * 60.0 * 60.0 * 1000.0), "millis", "milli", "ms",
                     "milliseconds", "millisecond");
        addJavaStyle(1, 1, (args, scope) -> args.get(0).D() / (24.0 * 60.0 * 60.0), "secs", "s", "sec", "seconds",
                     "second");
        addJavaStyle(1, 1, (args, scope) -> args.get(0).D() / (24.0 * 60.0), "m", "minutes", "minute");
        addJavaStyle(1, 1, (args, scope) -> args.get(0).D() / 24.0, "hrs", "hours", "h", "hour");
        addJavaStyle(1, 1, (args, scope) -> args.get(0).D(), "days", "day", "d");
        addJavaStyle(1, 1, (args, scope) -> args.get(0).toString().length(), "strlen");
        addJavaStyle(0, 0, (args, scope) -> $(new Date()), "date");
        addJavaStyle(0, 0, (args, scope) -> System.currentTimeMillis(), "time");
        addJavaStyle(2, 2, (args, scope) -> args.get(0).toString().matches(args.get(1).$S()), "matches");
    }
}
