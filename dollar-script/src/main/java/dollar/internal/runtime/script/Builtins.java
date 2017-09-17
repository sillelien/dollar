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

import dollar.api.StateAware;
import dollar.api.Type;
import dollar.api.Value;
import dollar.api.types.DollarFactory;
import dollar.api.types.ErrorType;
import dollar.internal.runtime.script.api.Builtin;
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
        addDollarStyle(1, 1, (pure, args, scope) -> args.get(0).$abs(), true, Type._ANY, "ABS");

        addJavaStyle(1, Integer.MAX_VALUE, (pure, args, scope) -> {
            String message = args.get(0).$S();
            ArrayList<Value> values = new ArrayList<>(args);
            values.remove(0);
            return $(String.format(message, values.stream().map(Value::toJavaObject).toArray()));
        }, true, Type._STRING, "FORMAT");

        addDollarSingleNoScope(false, StateAware::$start, Type._VOID, "START");
        addDollarSingleNoScope(false, StateAware::$stop, Type._VOID, "STOP");
        addDollarSingleNoScope(false, StateAware::$create, Type._VOID, "CREATE");
        addDollarSingleNoScope(false, StateAware::$destroy, Type._VOID, "DESTROY");
        addDollarSingleNoScope(false, StateAware::$pause, Type._VOID, "PAUSE");
        addDollarSingleNoScope(false, StateAware::$unpause, Type._VOID, "UNPAUSE");
        addDollarSingleNoScope(false, StateAware::$state, Type._VOID, "STATE");

        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(
                0).toDouble() / DAY_IN_MILLIS), true, Type._DECIMAL, "MS", "MILLIS", "MILLISECONDS", "MILLISECOND");
        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(
                0).toDouble() / DAY_IN_SECONDS), true, Type._DECIMAL, "S", "SEC", "SECS", "SECONDS");
        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(
                0).toDouble() / DAY_IN_MINUTES), true, Type._DECIMAL, "M", "MINUTES",
                     "MINUTE");
        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(
                0).toDouble() / DAY_IN_HOURS), true, Type._DECIMAL, "H", "HOUR", "HOURS");
        addJavaStyle(1, 1, (pure, args, scope) -> {
            try {
                long millis = (long) (args.get(0).toDouble() * DAY_IN_MILLIS);
                Thread.sleep(millis);
                return args.get(0);
            } catch (InterruptedException e) {
                return scope.handleError(e);
            }
        }, false, Type._VOID, "SLEEP");
        addDollarStyle(1, 2, (pure, args, scope) -> {
            if (args.size() == 1) {
                return DollarFactory.failure(ErrorType.valueOf(args.get(0).toString()), "", true);
            } else {
                return DollarFactory.failure(ErrorType.valueOf(args.get(0).toString()), args.get(1).toString(), true);
            }
        }, true, Type._ANY, "ERROR");
        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(0).toDouble()), true,
                     Type._DECIMAL, "DAYS", "DAY", "D");
        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(
                0).toDouble() * WEEK_IN_DAYS), true, Type._DECIMAL, "WEEKS", "WEEK", "W");
        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(
                0).toDouble() * MONTH_IN_DAYS), true, Type._DECIMAL, "MONTHS", "MONTH", "MTH", "MTHS");
        addJavaStyle(1, 1, (pure, args, scope) -> (args.get(
                0).toDouble() * YEAR_IN_DAYS), true, Type._DECIMAL, "YEARS", "YEAR", "YRS", "YR", "Y");
        addJavaStyle(1, 1, (pure, args, scope) -> args.get(0).toString().length(), true, Type._INTEGER, "LEN");
        addJavaStyle(0, 0, (pure, args, scope) -> $(new Date()), false, Type._DATE, "DATE");
        addJavaStyle(0, 0, (pure, args, scope) -> System.currentTimeMillis(), false, Type._INTEGER, "TIME");
        addJavaStyle(2, 2, (pure, args, scope) -> args.get(0).toString().matches(args.get(1).$S()), true, Type._BOOLEAN, "MATCHES");

        //todo: use a service loader to load additional builtins :- http://docs.oracle.com/javase/6/docs/api/java/util/ServiceLoader.html
        //todo: this will be used to provide libraries
    }

    private static void addDollarSingleNoScope(boolean isPure, @NotNull Function<Value, Value> lambda,
                                               @NotNull Type type, @NotNull String... names) {
        for (String name : names) {
            map.put(name, new Builtin.BuiltinImpl(name, (Builtin<Value>) (pure, args, scope) -> {
                Value v = args.get(0);
                return lambda.apply(v);
            }, 1, 1, isPure, type));
        }
    }

    private static void addDollarStyle(int minargs, int maxargs, @NotNull Builtin.DollarStyle lambda, boolean pure,
                                       @NotNull Type type, @NotNull String... names) {
        for (String name : names) {
            map.put(name, new Builtin.BuiltinImpl(name, lambda, minargs, maxargs, pure, type));
        }
    }

    private static <T> void addJavaStyle(int minargs, int maxargs, @NotNull Builtin.JavaStyle<T> lambda, boolean pure,
                                         @NotNull Type type, @NotNull String... names) {
        for (String name : names) {
            map.put(name, new Builtin.BuiltinImpl(name, lambda, minargs, maxargs, pure, type));
        }
    }

    @NotNull
    public static Value execute(@NotNull String name, @NotNull List<Value> parameters, boolean pure) {
        final Builtin<Value> builtin = (Builtin<Value>) map.get(name);
        if (builtin == null) {
            throw new BuiltinNotFoundException(name);
        }
        return builtin.execute(pure, parameters, DollarUtilFactory.util().scope());
    }

    public static boolean exists(@NotNull String name) {
        return map.containsKey(name);
    }

    public static boolean isPure(@NotNull String lhsString) {
        return (map.get(lhsString) != null) && ((Builtin.BuiltinImpl) map.get(lhsString)).isPure();
    }

    @NotNull
    public static Type type(@NotNull String s) {
        return (((Builtin.BuiltinImpl<?>) map.get(s)).type());
    }
}
