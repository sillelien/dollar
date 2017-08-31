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

package dollar.internal.runtime.script;

import dollar.api.Scope;
import dollar.api.StateAware;
import dollar.api.types.DollarFactory;
import dollar.api.types.ErrorType;
import dollar.api.var;
import dollar.internal.runtime.script.api.exceptions.BuiltinNotFoundException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import static dollar.api.DollarStatic.$;
import static dollar.internal.runtime.script.Constants.*;

public final class Builtins {
    @NotNull
    private static final HashMap<String, Builtin<?>> map;


    static {
        map = new HashMap<>();
        addDollarStyle(1, 1, (pure, args, scope) -> args.get(0).$abs(), true, "ABS");

        addJavaStyle(1, Integer.MAX_VALUE, (pure, args, scope) -> {
            String message = args.get(0).$S();
            ArrayList<var> values = new ArrayList<>(args);
            values.remove(0);
            return $(String.format(message, values.stream().map(var::toJavaObject).toArray()));
        }, true, "FORMAT");

        addDollarSingleNoScope(false, StateAware::$start, "START");
        addDollarSingleNoScope(false, StateAware::$stop, "STOP");
        addDollarSingleNoScope(false, StateAware::$create, "CREATE");
        addDollarSingleNoScope(false, StateAware::$destroy, "DESTROY");
        addDollarSingleNoScope(false, StateAware::$pause, "PAUSE");
        addDollarSingleNoScope(false, StateAware::$unpause, "UNPAUSE");
        addDollarSingleNoScope(false, StateAware::$state, "STATE");

        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(0).toDouble() == null) ? args.get(0) : (args.get(
                0).toDouble() / DAY_IN_MILLIS), true, "MS", "MILLIS", "MILLISECONDS", "MILLISECOND");
        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(0).toDouble() == null) ? args.get(0) : (args.get(
                0).toDouble() / DAY_IN_SECONDS), true, "S", "SEC", "SECS", "SECONDS");
        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(0).toDouble() == null) ? args.get(0) : (args.get(
                0).toDouble() / DAY_IN_MINUTES), true, "M", "MINUTES",
                     "MINUTE");
        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(0).toDouble() == null) ? args.get(0) : (args.get(
                0).toDouble() / DAY_IN_HOURS), true, "H", "HOUR", "HOURS");
        addJavaStyle(1, 1, (pure, args, scope) -> {
            try {
                Thread.sleep((long) (args.get(0).toDouble() * DAY_IN_MILLIS));
                return args.get(0);
            } catch (InterruptedException e) {
                return scope.handleError(e);
            }
        }, false, "SLEEP");
        addDollarStyle(1, 2, (pure, args, scope) -> {
            if (args.size() == 1) {
                return DollarFactory.failure(ErrorType.valueOf(args.get(0).toString()), "", true);
            } else {
                return DollarFactory.failure(ErrorType.valueOf(args.get(0).toString()), args.get(1).toString(), true);
            }
        }, true, "ERROR");
        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(0).toDouble() == null) ? args.get(0) : (args.get(0).toDouble()), true,
                     "DAYS", "DAY", "D");
        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(0).toDouble() == null) ? args.get(0) : (args.get(
                0).toDouble() * WEEK_IN_DAYS), true, "WEEKS", "WEEK", "W");
        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(0).toDouble() == null) ? args.get(0) : (args.get(
                0).toDouble() * MONTH_IN_DAYS), true, "MONTHS", "MONTH", "MTH", "MTHS");
        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(0).toDouble() == null) ? args.get(0) : (args.get(
                0).toDouble() * YEAR_IN_DAYS), true, "YEARS", "YEAR", "YRS", "YR", "Y");
        addJavaStyle(1, 1, (pure, args, scope) -> args.get(0).toString().length(), true, "LEN");
        addJavaStyle(0, 0, (pure, args, scope) -> $(new Date()), false, "DATE");
        addJavaStyle(0, 0, (pure, args, scope) -> System.currentTimeMillis(), false, "TIME");
        addJavaStyle(2, 2, (pure, args, scope) -> args.get(0).toString().matches(args.get(1).$S()), true, "MATCHES");

        //todo: use a service loader to load additional builtins :- http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html
        //todo: this will be used to provide libraries
    }

    @NotNull
    public static var execute(@NotNull String name, @NotNull List<var> parameters, boolean pure) {
        final Builtin<var> builtin = (Builtin<var>) map.get(name);
        if (builtin == null) {
            throw new BuiltinNotFoundException(name);
        }
        return builtin.execute(pure, parameters, DollarScriptSupport.currentScope());
    }

    public static boolean exists(@NotNull String name) {
        return map.containsKey(name);
    }

    private static <T> void addJavaStyle(int minargs, int maxargs, @NotNull Builtin.JavaStyle<T> lambda, boolean pure,
                                         @NotNull String... names) {
        for (String name : names) {
            map.put(name, new Builtin.BuiltinImpl(name, lambda, minargs, maxargs, pure));
        }
    }

    private static void addDollarStyle(int minargs, int maxargs, @NotNull Builtin.DollarStyle lambda, boolean pure,
                                       @NotNull String... names) {
        for (String name : names) {
            map.put(name, new Builtin.BuiltinImpl(name, lambda, minargs, maxargs, pure));
        }
    }

    private static void addDollarSingleNoScope(boolean isPure, @NotNull Function<var, var> lambda,
                                               @NotNull String... names) {
        for (String name : names) {
            map.put(name, new Builtin.BuiltinImpl(name, (Builtin<var>) (boolean pure, List<var> args, Scope scope) -> {
                var v = args.get(0);
                return lambda.apply(v);
            }, 1, 1, isPure));
        }
    }

    public static boolean isPure(@NotNull String lhsString) {
        return (map.get(lhsString) != null) && ((Builtin.BuiltinImpl) map.get(lhsString)).isPure();
    }
}
