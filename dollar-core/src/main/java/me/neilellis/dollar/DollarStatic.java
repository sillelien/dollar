package me.neilellis.dollar;

import com.google.common.collect.Range;
import me.neilellis.dollar.monitor.Monitor;
import me.neilellis.dollar.pubsub.Sub;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import spark.Spark;
import spark.route.HttpMethod;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * To use the $ class you need to statically import all of the methods from this class.
 * This is effectively a factory class for the $ class with additional convenience methods.
 *
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarStatic {


    static ThreadLocal<DollarThreadContext> threadContext = new ThreadLocal<DollarThreadContext>() {
        @Override
        protected DollarThreadContext initialValue() {
            return new DollarThreadContext();
        }
    };

    private static ConcurrentHashMap<Object, Map> context = new ConcurrentHashMap<>();

    private static ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();

    public static var $(String name, MultiMap multiMap) {
        return DollarFactory.fromValue().$(name, multiMap);
    }

    public static var $(String name, Number value) {
        return DollarFactory.fromValue().$(name, value);
    }

    public static var $(String name, Date value) {
        return DollarFactory.fromValue().$(name, value);
    }

    public static var $(String name, JsonArray value) {
        return DollarFactory.fromValue().$(name, value);
    }

    public static var $(var... values) {
        var v = $();
        for (var value : values) {
            v = v.add(value);
        }
        return v;
    }

    public static var $() {
        return DollarFactory.fromValue();
    }

    public static var $(String name, var... values) {
        var v = $();
        for (var value : values) {
            v = v.add(value);
        }
        return $(name, v);
    }

    public static var $(String key, var value) {
        return DollarFactory.fromValue().$(key, value);
    }

    public static var $(String key, String value) {
        return DollarFactory.fromValue().$(key, value);
    }

    public static var $(JsonObject json) {
        return DollarFactory.fromValue(Collections.<Throwable>emptyList(),json);
    }

    public static var $(String key, JsonObject jsonObject) {
        return DollarFactory.fromValue().$(key, jsonObject);
    }

    public static var $(long start, long finish) {
        return $(Range.closed(start, finish));
    }

    public static var $(Object o) {
        return DollarStatic.tracer().trace(DollarNull.INSTANCE, DollarFactory.create(Collections.emptyList(),o), StateTracer.Operations.CREATE, o == null ? "null" : o.getClass().getName());
    }

    public static Monitor monitor() {
        return threadContext.get().getMonitor();
    }

    static JsonObject mapToJson(MultiMap map) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, String> entry : map) {
            jsonObject.putString(entry.getKey(), entry.getValue());
        }
        return jsonObject;
    }

    public static DollarHttp $DELETE(String path, DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.delete.name(), path, handler);
    }

    public static DollarHttp $GET(String path, DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.get.name(), path, handler);
    }

    public static DollarHttp $HEAD(String path, DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.head.name(), path, handler);
    }

    public static DollarHttp $OPTIONS(String path, DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.options.name(), path, handler);
    }

    public static DollarHttp $PATCH(String path, DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.patch.name(), path, handler);
    }

    public static DollarHttp $POST(String path, DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.post.name(), path, handler);
    }

    public static void $begin(String value) {
        threadContext.get().pushLabel(value);
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run/call method. This creates an identifier used to link context's together.
     *
     * @param call the lambda to run.
     */
    public static var $call(Callable<var> call) {
        return $call(threadContext.get().child(), call);
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run/call method. This creates an identifier used to link context's together.
     *
     * @param context the current thread context
     * @param call    the lambda to run.
     */
    public static var $call(DollarThreadContext context, Callable<var> call) {
        threadContext.set(context.child());
        try {
            return call.call();
        } catch (Exception e) {
            throw new DollarException(e);
        } finally {
            threadContext.remove();
        }
    }

    public static void $dump() {
        threadContext.get().getMonitor().dump();
    }

    public static void $dumpThread() {
        threadContext.get().getMonitor().dumpThread();
    }

    public static void $end(String value) {
        threadContext.get().popLabel(value);
    }

    public static var $eval(String label, String js) {
        return $().eval(label, js);
    }

    public static var $eval(String js) {
        return $().eval(js);
    }

    public static DollarFuture $fork(Callable<var> call) {
        DollarThreadContext child = threadContext.get().child();
        return new DollarFuture(threadPoolExecutor.submit(() ->$call(child, call)));

    }

    public static JsonArray $jsonArray(Object... values) {
        return new JsonArray(values);
    }

    public static var $list(Object... values) {
        return DollarFactory.fromValue(Collections.emptyList(),values);
    }

    public static var $load(String location) {
        return $().load(location);
    }

    public static var $pop(String location, int timeoutInMillis) {
        return $().pop(location, timeoutInMillis);
    }

    public static void $pub(var value, String... locations) {
        value.pub(locations);
    }

    public static void $push(String location, var value) {
        value.push(location);
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run method.
     *
     * @param context this is an identifier used to link context's together, it should be unique to the request being processed.
     * @param run     the lambda to run.
     */
    public static void $run(DollarThreadContext context, Runnable run) {
        threadContext.set(context.child());
        try {
            run.run();
        } finally {
            threadContext.remove();
        }
    }

    /**
     * The beginning of any new Dollar Code should start with a DollarStatic.run/call method. This creates an object used to link context's together.
     *
     * @param run the lambda to run.
     */
    public static void $run(Runnable run) {
        try {
            run.run();
        } finally {
            threadContext.remove();
        }
    }

    public static void $save(var value, String location) {
        value.save(location);
    }

    public static void $save(String location, var value, int expiryInMilliseconds) {
        value.save(location, expiryInMilliseconds);
    }

    public static Sub $sub(Consumer<var> action, String... locations) {
        return threadContext.get().getPubsub().sub(action, locations);
    }

    //Kotlin compatible versions
    public static var create() {
        return $();
    }

    public static var create(String value) {
        return $();
    }

    public static void log(String message) {
        System.out.println(threadContext.get().getLabels().toString() + ":" + message);
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

    public static StateTracer tracer() {
        return new SimpleLogStateTracer();
    }

    public static DollarThreadContext context() {
        return threadContext.get();
    }

    public static DollarThreadContext childContext() {
        return threadContext.get().child();
    }

    public static <R> R handleInterrupt(InterruptedException ie) {
        if (Thread.interrupted()) {
            log("Interrupted");
        }
        throw new Error("Interrupted");
    }

    public static <R> R handleError(Throwable throwable) {
        log(throwable.getMessage());
        throwable.printStackTrace();
        if (throwable instanceof DollarException) {
            throw (DollarException) throwable;
        } else {
            throw new DollarException(throwable);
        }

    }

    public static DollarThreadContext childContext(String s) {
        return threadContext.get().child(s);
    }

    public static void label(String label) {
        context().pushLabel(label);
    }
}
