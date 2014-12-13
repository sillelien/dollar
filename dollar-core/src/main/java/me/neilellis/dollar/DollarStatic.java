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

package me.neilellis.dollar;

import me.neilellis.dollar.collections.ImmutableList;
import me.neilellis.dollar.collections.MultiMap;
import me.neilellis.dollar.collections.Range;
import me.neilellis.dollar.js.JSFileScript;
import me.neilellis.dollar.json.JsonArray;
import me.neilellis.dollar.json.JsonObject;
import me.neilellis.dollar.monitor.DollarMonitor;
import me.neilellis.dollar.plugin.Plugins;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.uri.URIHandlerFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spark.Spark;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * To use the $ class you need to statically import all of the methods from this class. This is effectively a factory
 * class for the $ class with additional convenience methods.
 *
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarStatic {

    public static final Configuration config = new Configuration();

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


    private static final URIHandlerFactory URIHandler = Plugins.sharedInstance(URIHandlerFactory.class);

    public static Pipeable $jsFile(String name) {
        try {
            return new JSFileScript(new File(name));
        } catch (IOException e) {
            return DollarStatic.logAndRethrow(e);
        }
    }

    @NotNull
    public static <R> R logAndRethrow(@NotNull Throwable throwable) {
        if (throwable instanceof DollarException) {
            throw (DollarException) throwable;
        } else {
            throw new DollarException(throwable);
        }

    }

    public static Pipeable $jsResource(String name) {
        try {
            return new JSFileScript(DollarStatic.class.getResourceAsStream(name));
        } catch (IOException e) {
            return DollarStatic.logAndRethrow(e);
        }
    }

    @NotNull
    public static var $(@NotNull var... values) {
        var v = $();
        for (var value : values) {
            v = v.$plus(value);
        }
        return v;
    }

    @NotNull
    public static var $() {
        return DollarFactory.fromValue();
    }

    @NotNull
    public static var $(@NotNull String name, @NotNull var... values) {
        var v = $();
        for (var value : values) {
            v = v.$plus(value);
        }
        return $(name, v);
    }

    @NotNull
    public static var $(@NotNull String name, Object o) {
        return DollarFactory.fromValue().$set($(name), o);
    }

    public static var $(@Nullable Object o) {
        return $(o, false);
    }

    private static var $(@Nullable Object o, boolean parallel) {
        if (o instanceof var) {
            return fix((var) o, parallel);
        }
        return DollarFactory.fromValue(o, ImmutableList.of());
    }

    public static var fix(@Nullable var v, boolean parallel) {
        return v != null ? DollarFactory.wrap(v._fix(parallel)) : $void();
    }

    @NotNull
    public static var $void() {
        return DollarFactory.newVoid();
    }

    @NotNull
    public static var $(JsonObject json) {
        return DollarFactory.fromValue(json, ImmutableList.of());
    }

    public static var $(var start, var finish) {
        return $(new Range(start, finish));
    }

    public static List<var> fixList(@Nullable List list) {
        ArrayList<var> result = new ArrayList<>();
        for (Object value : list) {
            if (value instanceof var) {
                result.add(((var) value)._fix(false));
            } else {
                result.add($(value));
            }
        }
        return result;
    }

    @NotNull
    public static StateTracer tracer() {
        return new SimpleLogStateTracer();
    }

    @NotNull
    public static JsonObject mapToJson(@NotNull MultiMap<String, String> map) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, String> entry : map.entries()) {
            jsonObject.putString(entry.getKey(), entry.getValue());
        }
        return jsonObject;
    }

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

    public static void $begin(String value) {
        threadContext.get().pushLabel(value);
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run/call method. This creates an identifier used
     * to link context's together.
     *
     * @param call the lambda to run.
     */
    public static var $call(@NotNull Callable<var> call) {
        return $call(threadContext.get().child(), call);
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run/call method. This creates an identifier used
     * to link context's together.
     *
     * @param context the current thread context
     * @param call    the lambda to run.
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

    @NotNull
    public static var handleError(@NotNull Throwable throwable, var failee) {
//        log(throwable.getMessage());
////        log(throwable);
//        throwable.printStackTrace(System.err);
        if (failee == null) {
            return DollarFactory.failure(throwable);
        }
        return failee.copy(ImmutableList.of(throwable));

    }

    public static void $dump() {
        threadContext.get().getMonitor().dump();
    }

    public static void $dumpThread() {
        threadContext.get().getMonitor().dumpThread();
    }

    public static void $end(@NotNull String value) {
        threadContext.get().popLabel(value);
    }

    @NotNull
    public static var $eval(@NotNull String label, @NotNull String js) {
        return $().$pipe(label, js);
    }

    @NotNull
    public static var $eval(@NotNull String js) {
        return $().$eval(js);
    }

    @NotNull
    public static var $fork(@NotNull Callable<var> call) {
        DollarThreadContext child = threadContext.get().child();
        return (var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarFuture(threadPoolExecutor.submit(() -> $call(child, call))));


    }

    @NotNull
    public static JsonArray $jsonArray(@NotNull Object... values) {
        return new JsonArray(values);
    }

    @NotNull

    public static var $list(Object... values) {
        return DollarFactory.fromValue(values, ImmutableList.of());
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run method.
     *
     * @param context this is an identifier used to link context's together, it should be unique to the request being
     *                processed.
     * @param run     the lambda to run.
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
     * The beginning of any new Dollar Code should start with a DollarStatic.run/call method. This creates an object used
     * to link context's together.
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


    public static DollarMonitor monitor() {
        return threadContext.get().getMonitor();
    }


    @NotNull
    public static var create() {
        return $();
    }

    @NotNull
    public static var create(String value) {
        return $();
    }

    public static void log(Object message) {
        System.out.println(threadContext.get().getLabels().toString() + ":" + message);
    }

    public static void logf(String message, Object... values) {
        System.out.printf(threadContext.get().getLabels().toString() + ":" + message, values);
    }

    public static void stopHttpServer() {
        Spark.stop();
    }

    @NotNull
    public static DollarThreadContext childContext() {
        return threadContext.get().child();
    }

    @NotNull
    public static <R> R handleInterrupt(InterruptedException ie) {
        if (Thread.interrupted()) {
            log("Interrupted");
        }
        throw new Error("Interrupted");
    }

    public static void log(String message) {
        System.out.println(threadContext.get().getLabels().toString() + ":" + message);
    }

    @NotNull
    public static DollarThreadContext childContext(String s) {
        return threadContext.get().child(s);
    }

    public static void label(String label) {
        context().pushLabel(label);
    }

    public static DollarThreadContext context() {
        return threadContext.get();
    }

    @NotNull
    public static ErrorLogger errorLogger() {
        return new SimpleErrorLogger();
    }

    public static URIHandlerFactory integrationProvider() {
        return URIHandler;
    }

    public static var $(Pipeable lambda) {
        return DollarFactory.fromValue(lambda);
    }
}
