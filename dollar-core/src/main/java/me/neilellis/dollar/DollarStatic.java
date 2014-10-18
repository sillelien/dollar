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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import me.neilellis.dollar.monitor.Monitor;
import me.neilellis.dollar.pubsub.Sub;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.types.DollarVoid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import spark.Spark;
import spark.route.HttpMethod;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * To use the $ class you need to statically import all of the methods from this class. This is effectively a factory
 * class for the $ class with additional convenience methods.
 *
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarStatic {


    @NotNull
    static ThreadLocal<DollarThreadContext> threadContext = new ThreadLocal<DollarThreadContext>() {
    @NotNull
    @Override
    protected DollarThreadContext initialValue() {
        return new DollarThreadContext();
    }
    };

    @NotNull
    private static ConcurrentHashMap<Object, Map> context = new ConcurrentHashMap<>();

    @NotNull
    private static ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();

    @NotNull
    public static var $(@NotNull String name, MultiMap multiMap) {
        return DollarFactory.fromValue().$(name, multiMap);
    }

    @NotNull
    public static var $(@NotNull String name, Number value) {
        return DollarFactory.fromValue().$(name, value);
    }

    @NotNull
    public static var $(@NotNull String name, Date value) {
        return DollarFactory.fromValue().$(name, value);
    }

    @NotNull
    public static var $(@NotNull String name, JsonArray value) {
        return DollarFactory.fromValue().$(name, value);
    }

    @NotNull
    public static var $(@NotNull var... values) {
        var v = $();
        for (var value : values) {
            v = v.$append(value);
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
            v = v.$append(value);
    }
        return $(name, v);
    }

    @NotNull
    public static var $(@NotNull String key, var value) {
        return DollarFactory.fromValue().$(key, value);
    }

    @NotNull
    public static var $(@NotNull String key, String value) {
        return DollarFactory.fromValue().$(key, value);
    }

    @NotNull
    public static var $(JsonObject json) {
        return DollarFactory.fromValue(ImmutableList.of(), json);
    }

    @NotNull
    public static var $(@NotNull String key, JsonObject jsonObject) {
        return DollarFactory.fromValue().$(key, jsonObject);
    }

    public static var $(long start, long finish) {
        return $(Range.closed(start, finish));
    }

    public static var $(@Nullable Object o) {
        return DollarStatic.tracer()
                           .trace(DollarVoid.INSTANCE,
                                  DollarFactory.fromValue(ImmutableList.of(), o),
                                  StateTracer.Operations.CREATE,
                                  o == null ? "null" : o.getClass().getName());
    }

    @NotNull
    public static StateTracer tracer() {
        return new SimpleLogStateTracer();
    }

    public static Monitor monitor() {
        return threadContext.get().getMonitor();
    }

    @NotNull
    public static JsonObject mapToJson(@NotNull MultiMap map) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, String> entry : map) {
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

    @NotNull
    public static DollarHttp $DELETE(String path, @NotNull DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.delete.name(), path, handler);
    }

    @NotNull
    public static DollarHttp $GET(String path, @NotNull DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.get.name(), path, handler);
    }

    @NotNull
    public static DollarHttp $HEAD(String path, @NotNull DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.head.name(), path, handler);
    }

    @NotNull
    public static DollarHttp $OPTIONS(String path, @NotNull DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.options.name(), path, handler);
    }

    @NotNull
    public static DollarHttp $PATCH(String path, @NotNull DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.patch.name(), path, handler);
    }

    @NotNull
    public static DollarHttp $POST(String path, @NotNull DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.post.name(), path, handler);
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
//        log(throwable);
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
        return $().$pipe(js);
    }

    @NotNull
    public static DollarFuture $fork(@NotNull Callable<var> call) {
        DollarThreadContext child = threadContext.get().child();
        return new DollarFuture(threadPoolExecutor.submit(() -> $call(child, call)));

    }

    @NotNull
    public static JsonArray $jsonArray(@NotNull Object... values) {
        return new JsonArray(values);
    }

    @NotNull
    public static var $list(Object... values) {
        return DollarFactory.fromValue(ImmutableList.of(), values);
    }

    @NotNull
    public static var $load(@NotNull String location) {
        return $().$load(location);
    }

    @NotNull
    public static var $pop(@NotNull String location, int timeoutInMillis) {
        return $().$pop(location, timeoutInMillis);
    }

    public static void $pub(@NotNull var value, String... locations) {
        value.$pub(locations);
    }

    public static void $push(@NotNull String location, @NotNull var value) {
        value.$push(location);
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
    public static void $run(@NotNull Runnable run) {
        try {
            run.run();
        } finally {
            threadContext.remove();
    }
    }

    public static void $save(@NotNull var value, @NotNull String location) {
        value.$save(location);
    }

    public static void $save(@NotNull String location, @NotNull var value, int expiryInMilliseconds) {
        value.$save(location, expiryInMilliseconds);
    }

    @NotNull
    public static Sub $sub(Consumer<var> action, String... locations) {
        return threadContext.get().getPubsub().sub(action, locations);
    }

    //Kotlin compatible versions
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
    public static <R> R logAndRethrow(@NotNull Throwable throwable) {
        log(throwable.getMessage());
        throwable.printStackTrace();
        if (throwable instanceof DollarException) {
            throw (DollarException) throwable;
        } else {
            throw new DollarException(throwable);
    }

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

    @NotNull
    public static var $void() {
        return DollarFactory.newVoid();
    }
}
