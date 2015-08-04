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

package com.sillelien.dollar.api;

import com.sillelien.dollar.api.collections.ImmutableList;
import com.sillelien.dollar.api.collections.MultiMap;
import com.sillelien.dollar.api.json.JsonArray;
import com.sillelien.dollar.api.json.JsonObject;
import com.sillelien.dollar.api.monitor.DollarMonitor;
import com.sillelien.dollar.api.monitor.SimpleLogStateTracer;
import com.sillelien.dollar.api.types.*;
import com.sillelien.dollar.api.uri.URI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DollarStatic {

    /**
     * The thread context that all Dollar classes have access to.
     */
    @NotNull
    public static final ThreadLocal<DollarThreadContext> threadContext = new ThreadLocal<DollarThreadContext>() {
        @NotNull
        @Override
        protected DollarThreadContext initialValue() {
            return new DollarThreadContext();
        }
    };
    @NotNull
    private static final ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();
    @NotNull private static Configuration config = new SystemPropertyConfiguration();

    /**
     * Log and rethrow.
     *
     * @param <R>       the type parameter
     * @param throwable the throwable
     *
     * @return the r
     */
    @NotNull
    @Deprecated
    public static <R> R logAndRethrow(@NotNull Throwable throwable) {
        if (throwable instanceof DollarException) {
            throw (DollarException) throwable;
        } else {
            throw new DollarException(throwable);
        }

    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     *
     * @return the var
     */
    @NotNull public static var $range(var from, var to) {
        return DollarFactory.fromRange(from, to);
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     *
     * @return the var
     */
    @NotNull public static var $range(long from, long to) {
        return DollarFactory.fromRange($(from), $(to));
    }

    /**
     * $ var.
     *
     * @param o the o
     *
     * @return the var
     */
    @NotNull public static var $(@Nullable Object o) {
        return $(o, false);
    }

    @NotNull private static var $(@Nullable Object o, boolean parallel) {
        return DollarFactory.fromValue(o, ImmutableList.of());
    }

    /**
     * Fix var.
     *
     * @param v        the v
     * @param parallel the parallel
     *
     * @return the var
     */
    @NotNull public static var fix(@Nullable var v, boolean parallel) {
        return v != null ? DollarFactory.wrap(v._fix(parallel)) : $void();
    }

    /**
     * $ void.
     *
     * @return the var
     */
    @NotNull
    public static var $void() {
        return DollarFactory.newVoid();
    }

    @NotNull
    public static var $null(Type type) {
        return DollarFactory.newNull(type);
    }


    /**
     * Fix, i.e. evaluate lambdas to the depth supplied, optionally hinting that parallel behaviour is fine
     *
     * @param v        the object to be fixed
     * @param depth    the depth at which to stop evaluation, 1 means do not penetrate any layers of maps/blocks, 2
     *                 means penetrate one layer of maps.
     * @param parallel if true parallel evaluation if fine
     *
     * @return the 'fixed' var
     */
    @NotNull public static var fix(@Nullable var v, int depth, boolean parallel) {
        return v != null ? DollarFactory.wrap(v._fix(depth, parallel)) : $void();
    }

    /**
     * Fix var.
     *
     * @param v the v
     *
     * @return the var
     */
    @NotNull public static var fix(@Nullable var v) {
        return v != null ? DollarFactory.wrap(v._fix(false)) : $void();
    }

    /**
     * Fix deep.
     *
     * @param v the v
     *
     * @return the var
     */
    @NotNull public static var fixDeep(@Nullable var v) {
        return v != null ? DollarFactory.wrap(v._fixDeep(false)) : $void();
    }

    /**
     * Fix deep.
     *
     * @param v        the v
     * @param parallel the parallel
     *
     * @return the var
     */
    @NotNull public static var fixDeep(@Nullable var v, boolean parallel) {
        return v != null ? DollarFactory.wrap(v._fixDeep(parallel)) : $void();
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     *
     * @return the var
     */
    @NotNull public static var $range(double from, double to) {
        return DollarFactory.fromRange($(from), $(to));
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     *
     * @return the var
     */
    @NotNull public static var $range(String from, String to) {
        return DollarFactory.fromRange($(from), $(to));
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     *
     * @return the var
     */
    @NotNull public static var $range(Date from, Date to) {
        return DollarFactory.fromRange($(from), $(to));
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     *
     * @return the var
     */
    @NotNull public static var $range(LocalDateTime from, LocalDateTime to) {
        return DollarFactory.fromRange($(from), $(to));
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     *
     * @return the var
     */
    @NotNull public static var $range(Instant from, Instant to) {
        return DollarFactory.fromRange($(from), $(to));
    }

    /**
     * $ uri.
     *
     * @param uri the uri
     *
     * @return the var
     */
    @NotNull public static var $uri(URI uri) {
        return DollarFactory.fromValue(uri);
    }

    /**
     * $ uri.
     *
     * @param uri the uri
     *
     * @return the var
     */
    @NotNull public static var $uri(String uri) {
        return DollarFactory.fromValue(URI.parse(uri));
    }

    /**
     * $ date.
     *
     * @param date the date
     *
     * @return the var
     */
    @NotNull public static var $date(Date date) {
        return DollarFactory.fromValue(date);
    }

    /**
     * $ date.
     *
     * @param date the date
     *
     * @return the var
     */
    @NotNull public static var $date(LocalDateTime date) {
        return DollarFactory.fromValue(date);
    }

    /**
     * $ var.
     *
     * @param values the values
     * @return the var
     */
    @NotNull
    public static var $(@NotNull var... values) {
        var v = $();
        for (var value : values) {
            v = v.$plus(value);
        }
        return v;
    }

    /**
     * $ var.
     *
     * @return the var
     */
    @NotNull
    public static var $() {
        return DollarFactory.fromValue();
    }

    /**
     * $ var.
     *
     * @param name the name
     * @param values the values
     * @return the var
     */
    @NotNull
    public static var $(@NotNull String name, @NotNull var... values) {
        var v = $();
        for (var value : values) {
            v = v.$plus(value);
        }
        return $(name, v);
    }

    /**
     * $ var.
     *
     * @param name the name
     * @param o the o
     * @return the var
     */
    @NotNull
    public static var $(@NotNull Object name, Object o) {
        return DollarFactory.fromValue().$set($(name), o);
    }

    /**
     * $ var.
     *
     * @param json the json
     * @return the var
     */
    @NotNull
    public static var $(JsonObject json) {
        return DollarFactory.fromValue(json, ImmutableList.of());
    }

    public static var $json(String json) {
        if (json.matches("^\\s*\\{.*")) {
            return $(new JsonObject(json));
        } else if (json.matches("^\\s*\\[.*")) {
            return $(new JsonArray(json));
        } else if (json.matches("^\\s*\".*\"")) {
            return $string(json.substring(1,json.length()-1));
        } else if (json.matches("^\\s*[0-9+-]+\\s*$")) {
            return $(Integer.parseInt(json));
        } else if (json.matches("^\\s*[0-9+-.eE]+\\s*$")) {
            return $(Double.parseDouble(json));
        } else {
          throw new DollarException("Could not parse as json: "+json);
        }
    }

    public static var $string(String s) {return DollarFactory.fromStringValue(s);}

    /**
     * Tracer state tracer.
     *
     * @return the state tracer
     */
    @NotNull
    public static StateTracer tracer() {
        return new SimpleLogStateTracer();
    }

    /**
     * Map to json.
     *
     * @param map the map
     * @return the json object
     */
    @NotNull
    public static JsonObject mapToJson(@NotNull MultiMap<String, String> map) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, Collection<String>> entry : map.entries()) {
            jsonObject.putString(entry.getKey(), entry.getValue().iterator().next());
        }
        return jsonObject;
    }

    /**
     * Param map to json.
     *
     * @param map the map
     * @return the json object
     */
    @NotNull
    public static JsonObject paramMapToJson(@NotNull Map<String, String[]> map) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            if (entry.getValue().length == 1) {
                jsonObject.putString(entry.getKey(), entry.getValue()[0]);
            } else {
                jsonObject.putArray(entry.getKey(), new JsonArray(entry.getValue()));
            }
        }
        return jsonObject;
    }

    /**
     * $ begin.
     *
     * @param value the value
     */
    public static void $begin(String value) {
        threadContext.get().pushLabel(value);
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run/call method. This creates an identifier
     * used to link context's together.
     *
     * @param call the lambda to run.
     * @return the var
     */
    public static var $call(@NotNull Callable<var> call) {
        return $call(threadContext.get().child(), call);
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run/call method. This creates an identifier
     * used to link context's together.
     *
     * @param context the current thread context
     * @param call the lambda to run.
     * @return the var
     */
    public static var $call(@NotNull DollarThreadContext context, @NotNull Callable<var> call) {
        threadContext.set(context.child());
        try {
            return call.call();
        } catch (Exception e) {
            return handleError(e, null);
        } finally {
            threadContext.remove();
        }
    }

    /**
     * Handle error.
     *
     * @param throwable the throwable
     * @param failee the failee
     * @return the var
     */
    @NotNull
    public static var handleError(@NotNull Throwable throwable, @Nullable var failee) {
        if (failee == null) {
            return DollarFactory.failure(throwable);
        }
        return failee._copy(ImmutableList.of(throwable));

    }

    /**
     * $ dump.
     */
    public static void $dump() {
        threadContext.get().getMonitor().dump();
    }

    /**
     * $ dump thread.
     */
    public static void $dumpThread() {
        threadContext.get().getMonitor().dumpThread();
    }

    /**
     * $ end.
     *
     * @param value the value
     */
    public static void $end(@NotNull String value) {
        threadContext.get().popLabel(value);
    }

    /**
     * $ eval.
     *
     * @param label the label
     * @param js the js
     * @return the var
     */
    @NotNull
    public static var $eval(@NotNull String label, @NotNull String js) {
        return $().$pipe(label, js);
    }

    /**
     * $ eval.
     *
     * @param js the js
     * @return the var
     */
    @NotNull
    public static var $eval(@NotNull String js) {
        return $().$eval(js);
    }

    /**
     * $ fork.
     *
     * @param call the call
     * @return the var
     */
    @NotNull
    public static var $fork(@NotNull Callable<var> call) {
        DollarThreadContext child = threadContext.get().child();
        return (var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarFuture(threadPoolExecutor.submit(() -> $call(child, call))));

    }

    /**
     * $ json array.
     *
     * @param values the values
     * @return the json array
     */
    @NotNull
    public static JsonArray $jsonArray(@NotNull Object... values) {
        return new JsonArray(values);
    }

    /**
     * $ list.
     *
     * @param values the values
     * @return the var
     */
    @NotNull

    public static var $list(Object... values) {
        return DollarFactory.fromValue(values, ImmutableList.of());
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run method.
     *
     * @param context this is an identifier used to link context's together, it should be unique to the request being
     *                processed.
     * @param run the lambda to run.
     */
    public static void $run(@NotNull DollarThreadContext context, @NotNull Runnable run) {
        threadContext.set(context.child());
        try {
            run.run();
        } finally {
            threadContext.remove();
        }
    }

    /**
     * The beginning of any new Dollar Code should start with a DollarStatic.run/call method. This creates an object
     * used to link context's together.
     *
     * @param run the lambda to run.
     */
    static void $run(@NotNull Runnable run) {
        try {
            run.run();
        } finally {
            threadContext.remove();
        }
    }


    /**
     * Monitor dollar monitor.
     *
     * @return the dollar monitor
     */
    public static DollarMonitor monitor() {
        return threadContext.get().getMonitor();
    }


    /**
     * Create var.
     *
     * @return the var
     */
    @NotNull
    public static var create() {
        return $();
    }

    /**
     * Create var.
     *
     * @param value the value
     * @return the var
     */
    @NotNull
    public static var create(Object value) {
        return $(value);
    }

    /**
     * Log void.
     *
     * @param message the message
     */
    public static void log(Object message) {
        System.out.println(threadContext.get().getLabels().toString() + ":" + message);
    }

    /**
     * Child context.
     *
     * @return the dollar thread context
     */
    @NotNull
    public static DollarThreadContext childContext() {
        return threadContext.get().child();
    }

    /**
     * Handle interrupt.
     *
     * @param <R>  the type parameter
     * @param ie the ie
     * @return the r
     */
    @NotNull
    public static <R> R handleInterrupt(InterruptedException ie) {
        if (Thread.interrupted()) {
            log("Interrupted");
        }
        throw new Error("Interrupted");
    }

    /**
     * Log void.
     *
     * @param message the message
     */
    public static void log(String message) {
        System.out.println(threadContext.get().getLabels().toString() + ":" + message);
    }

    /**
     * Child context.
     *
     * @param s the s
     * @return the dollar thread context
     */
    @NotNull
    public static DollarThreadContext childContext(String s) {
        return threadContext.get().child(s);
    }

    /**
     * Label void.
     *
     * @param label the label
     */
    public static void label(String label) {
        context().pushLabel(label);
    }

    /**
     * Context dollar thread context.
     *
     * @return the dollar thread context
     */
    public static DollarThreadContext context() {
        return threadContext.get();
    }

    /**
     * $ var.
     *
     * @param lambda the lambda
     * @return the var
     */
    @NotNull public static var $(Pipeable lambda) {
        return DollarFactory.fromValue(lambda);
    }

    /**
     * The shared configuration for Dollar.
     */
    @NotNull public static Configuration getConfig() {
        return config;
    }

    public static void setConfig(@NotNull Configuration config) {
        DollarStatic.config = config;
    }
}
