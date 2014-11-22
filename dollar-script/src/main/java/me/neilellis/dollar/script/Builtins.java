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
        return map.get(name).execute(parameters, scope);
    }

    public static boolean exists(String name) {
        return map.containsKey(name);
    }

    public static <T> void addJavaStyle(String name, int minargs, int maxargs, Builtin.JavaStyle<T> lambda) {
        map.put(name, new Builtin.BuiltinImpl(lambda, minargs, maxargs));
    }

    public static void addDollarStyle(String name, int minargs, int maxargs, Builtin.DollarStyle lambda) {
        map.put(name, new Builtin.BuiltinImpl(lambda, minargs, maxargs));
    }

    public static void addDollarSingleNoScope(String name, Function<var, var> lambda) {
        map.put(name, new Builtin.BuiltinImpl((args, scope) -> lambda.apply((var) args.get(0)), 1, 1));
    }


    static {
        addDollarStyle("abs", 1, 1, (args, scope) -> args.get(0).$abs());
        addJavaStyle("count", 1, 1, (args, scope) -> args.get(0).$size());

        addJavaStyle("format", 1, Integer.MAX_VALUE, (args, scope) -> {
            String message = args.get(0).$S();
            ArrayList<var> values = new ArrayList<>(args);
            values.remove(0);
            return $(String.format(message, values.stream().map(i -> i.$()).toArray()));
        });
        addJavaStyle("min", 1, 1, (args, scope) -> {
            return args.get(0).toList().stream().min((o1, o2) -> (int) Math.signum(o1.D() - o2.D())).get();
        });
        addJavaStyle("max", 1, 1, (args, scope) -> {
            return args.get(0).toList().stream().max((o1, o2) -> (int) Math.signum(o1.D() - o2.D())).get();
        });
        addJavaStyle("sort", 1, 1, (args, scope) -> {
            return $(args.get(0).toList().stream().sorted().collect(Collectors.toList()));
        });

        addJavaStyle("first", 1, 1, (args, scope) -> {
            return $(args.get(0).toList().get(0));
        });

        addJavaStyle("last", 1, 1, (args, scope) -> {
            ImmutableList<var> list = args.get(0).toList();
            return $(list.get(list.size() - 1));
        });
        addDollarSingleNoScope("start", StateAware::$start);
        addDollarSingleNoScope("stop", StateAware::$stop);
        addDollarSingleNoScope("create", StateAware::$create);
        addDollarSingleNoScope("destroy", StateAware::$destroy);
        addDollarSingleNoScope("pause", StateAware::$pause);
        addDollarSingleNoScope("unpause", StateAware::$unpause);
        addDollarSingleNoScope("state", StateAware::$state);

        addJavaStyle("strlen", 1, 1, (args, scope) -> args.get(0).toString().length());
        addJavaStyle("date", 0, 0, (args, scope) -> new Date().toString());
        addJavaStyle("matches", 2, 2, (args, scope) -> args.get(0).toString().matches(args.get(1).$S()));
    }
}
