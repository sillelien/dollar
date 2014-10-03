package com.cazcade.dollar;

import com.cazcade.dollar.pubsub.DollarPubSub;
import com.cazcade.dollar.pubsub.RedisPubSub;
import com.cazcade.dollar.pubsub.Sub;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import spark.route.HttpMethod;

import java.util.Map;
import java.util.UUID;
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


    private static ConcurrentHashMap<Object, Map> context = new ConcurrentHashMap<>();
    private static ThreadLocal<DollarPubSub> pubsub = new ThreadLocal<DollarPubSub>() {
        @Override
        protected DollarPubSub initialValue() {
            return new RedisPubSub();
        }
    };
    private static ThreadLocal<String> threadKey = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return UUID.randomUUID().toString();
        }
    };
    private static ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();

    public static $ $(String name, MultiMap multiMap) {
        return DollarFactory.fromValue().$(name, multiMap);
    }

    public static $ $(Object o) {
        if (o == null) {
            return new DollarMonitored(DollarNull.INSTANCE, DollarFactory.monitor);
        }
        if (o instanceof Number) {
            return new DollarMonitored(new DollarNumber((Number) o), DollarFactory.monitor);
        }
        if (o instanceof String) {
            try {
                return new DollarMonitored(new DollarJson(new JsonObject((String) o)), DollarFactory.monitor);
            } catch (DecodeException de) {
                return new DollarMonitored(new DollarString((String) o), DollarFactory.monitor);
            }
        }
        JsonObject json;
        if (o instanceof JsonObject) {
            json = ((JsonObject) o);
        }
        if (o instanceof JsonObject) {
            json = ((JsonObject) o);
        } else if (o instanceof MultiMap) {
            json = mapToJson((MultiMap) o);
        } else if (o instanceof Map) {
            json = new JsonObject((Map<String, Object>) o);
        } else if (o instanceof Message) {
            json = ((JsonObject) ((Message) o).body());
            if (json == null) {
                return new DollarMonitored(DollarNull.INSTANCE, DollarFactory.monitor);
            }
        } else {
            json = new JsonObject(o.toString());
        }
        return new DollarMonitored(new DollarJson(json), DollarFactory.monitor);
    }

    static JsonObject mapToJson(MultiMap map) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, String> entry : map) {
            jsonObject.putString(entry.getKey(), entry.getValue());
        }
        return jsonObject;
    }

    public static $ $(String name, JsonArray value) {
        return DollarFactory.fromValue().$(name, value);
    }

    public static $ $(String key, String value) {
        return DollarFactory.fromValue().$(key, value);
    }

    public static $ $(JsonObject json) {
        return DollarFactory.fromValue(json);
    }

    public static $ $(String key, $ value) {
        return DollarFactory.fromValue().$(key, value);
    }

    public static $ $(String key, JsonObject jsonObject) {
        return DollarFactory.fromValue().$(key, jsonObject);
    }

    public static $ $eval(String label, String js) {
        return $().eval(label, js);
    }

    public static $ $() {
        return DollarFactory.fromValue();
    }

    public static $ $eval(String js) {
        return $().eval(js);
    }

    public static $ $list(Object... values) {
        return new DollarList(values);
    }

    public static DollarHttp DELETE(String path, DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.delete.name(), path, handler);
    }

    public static DollarHttp GET(String path, DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.get.name(), path, handler);
    }

    public static DollarHttp HEAD(String path, DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.head.name(), path, handler);
    }

    public static DollarHttp OPTIONS(String path, DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.options.name(), path, handler);
    }

    public static DollarHttp PATCH(String path, DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.patch.name(), path, handler);
    }

    public static DollarHttp POST(String path, DollarHttpHandler handler) {
        return new DollarHttp(HttpMethod.post.name(), path, handler);
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run/call method. This creates an identifier used to link context's together.
     *
     * @param call the lambda to run.
     */
    public static <R> R call(Callable<R> call) {
        try {
            return call.call();
        } catch (Exception e) {
            throw new DollarException(e);
        } finally {
            threadKey.remove();
        }
    }

    public static DollarFuture fork(Callable<$> call) {
        String contextKey = contextKey();
        return new DollarFuture(threadPoolExecutor.submit(() -> call(contextKey, call)));

    }

    public static String contextKey() {
        return threadKey.get();
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run/call method. This creates an identifier used to link context's together.
     *
     * @param call the lambda to run.
     */
    public static <R> R call(String contextKey, Callable<R> call) {
        threadKey.set(contextKey);
        try {
            return call.call();
        } catch (Exception e) {
            throw new DollarException(e);
        } finally {
            threadKey.remove();
        }
    }

    public static JsonArray jsonArray(Object... values) {
        return new JsonArray(values);
    }

    public static void pub($ value, String... locations) {
        pubsub.get().pub(value, locations);
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run method.
     *
     * @param contextKey this is an identifier used to link context's together, it should be unique to the request being processed.
     * @param run        the lambda to run.
     */
    public static void run(String contextKey, Runnable run) {
        threadKey.set(contextKey);
        try {
            run.run();
        } finally {
            threadKey.remove();
        }
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run/call method. This creates an identifier used to link context's together.
     *
     * @param run the lambda to run.
     */
    public static void run(Runnable run) {
        try {
            run.run();
        } finally {
            threadKey.remove();
        }
    }

    public static Sub sub(Consumer<$> action, String... locations) {
        return pubsub.get().sub(action, locations);
    }

}
