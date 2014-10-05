package com.cazcade.dollar;

import com.cazcade.dollar.pubsub.DollarPubSub;
import com.cazcade.dollar.pubsub.RedisPubSub;
import com.cazcade.dollar.pubsub.Sub;
import com.cazcade.dollar.store.DollarStore;
import com.cazcade.dollar.store.RedisStore;
import org.jetbrains.annotations.NotNull;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import spark.Spark;
import spark.route.HttpMethod;

import java.util.Date;
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


    static ThreadLocal<var> threadContext = new ThreadLocal<var>();
    private static ConcurrentHashMap<Object, Map> context = new ConcurrentHashMap<>();
    private static ThreadLocal<DollarPubSub> pubsub = new ThreadLocal<DollarPubSub>() {
        @Override
        protected DollarPubSub initialValue() {
            return new RedisPubSub();
        }
    };
    private static ThreadLocal<DollarStore> store = new ThreadLocal<DollarStore>() {
        @Override
        protected DollarStore initialValue() {
            return new RedisStore();
        }
    };
    private static ThreadLocal<String> threadKey = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return UUID.randomUUID().toString();
        }
    };


    private static ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();


    public static var $(String name, MultiMap multiMap) {
        return DollarFactory.fromValue().$(name, multiMap);
    }

    public static var $(Object o) {
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
        return DollarFactory.fromValue(json);
    }

    public static var $(String key, JsonObject jsonObject) {
        return DollarFactory.fromValue().$(key, jsonObject);
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

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run/call method. This creates an identifier used to link context's together.
     *
     * @param call the lambda to run.
     */
    public static <R> R $call(Callable<R> call) {
        try {
            return call.call();
        } catch (Exception e) {
            throw new DollarException(e);
        } finally {
            threadKey.remove();
        }
    }

    public static var $eval(String label, String js) {
        return $().eval(label, js);
    }

    public static var $eval(String js) {
        return $().eval(js);
    }

    public static DollarFuture $fork(Callable<var> call) {
        String contextKey = contextKey();
        return new DollarFuture(threadPoolExecutor.submit(() -> $call(contextKey, call)));

    }

    public static String contextKey() {
        return threadKey.get();
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run/call method. This creates an identifier used to link context's together.
     *
     * @param call the lambda to run.
     */
    public static <R> R $call(String contextKey, Callable<R> call) {
        threadKey.set(contextKey);
        try {
            return call.call();
        } catch (Exception e) {
            throw new DollarException(e);
        } finally {
            threadKey.remove();
        }
    }

    public static JsonArray $jsonArray(Object... values) {
        return new JsonArray(values);
    }

    public static var $list(Object... values) {
        return new DollarList(values);
    }

    public static var $load(String location) {
        return store.get().get(location);
    }

    public static var $pop(String location, int timeoutInMillis) {
        return store.get().pop(location, timeoutInMillis);
    }

    public static void $pub(var value, String... locations) {
        pubsub.get().pub(value, locations);
    }

    public static void $push(String location, var value) {
        store.get().push(location, value);
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run method.
     *
     * @param contextKey this is an identifier used to link context's together, it should be unique to the request being processed.
     * @param run        the lambda to run.
     */
    public static void $run(String contextKey, Runnable run) {
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
    public static void $run(Runnable run) {
        try {
            run.run();
        } finally {
            threadKey.remove();
        }
    }

    public static void $save(var value, String location) {
        store.get().set(location, value);
    }


    public static void $save(String location, var value, int expiryInMilliseconds) {
        store.get().set(location, value, expiryInMilliseconds);
    }

    public static Sub $sub(Consumer<var> action, String... locations) {
        return pubsub.get().sub(action, locations);
    }

    //Kotlin compatible versions
    public static
    @NotNull
    var create() {
        return $();
    }

    public static
    @NotNull
    var create(@NotNull String value) {
        return $();
    }

    public static void stopHttpServer() {
        Spark.stop();
    }


}
