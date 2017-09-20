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

package dollar.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import dollar.api.execution.DollarExecutor;
import dollar.api.json.JsonArray;
import dollar.api.json.JsonObject;
import dollar.api.monitor.DollarMonitor;
import dollar.api.monitor.SimpleLogStateTracer;
import dollar.api.plugin.Plugins;
import dollar.api.script.Source;
import dollar.api.types.DollarFactory;
import dollar.api.uri.URI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;

public class DollarStatic {

    /**
     * The thread context that all Dollar classes have access to.
     */
    @NotNull
    public static final ThreadLocal<DollarThreadContext> threadContext = ThreadLocal.withInitial(DollarThreadContext::new);
    @NotNull
    static final DollarExecutor executor = Objects.requireNonNull(Plugins.sharedInstance(DollarExecutor.class));
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(DollarStatic.class);
    @NotNull
    private static final ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();
    @NotNull
    private static Configuration config = new SystemPropertyConfiguration();

    /**
     * $ Value.
     *
     * @param o the o
     * @return the Value
     */
    @NotNull
    public static Value $(@Nullable Object o) {
        return $(o, false);
    }

    @NotNull
    private static Value $(@Nullable Object o, boolean parallel) {
        return DollarFactory.fromValue(o);
    }

    /**
     * $ Value.
     *
     * @param values the values
     * @return the Value
     */
    @NotNull
    public static Value $(@NotNull Value... values) {
        return $map(values);
    }

    /**
     * $ Value.
     *
     * @return the Value
     */
    @NotNull
    public static Value $() {
        return DollarFactory.fromValue();
    }

    /**
     * $ Value.
     *
     * @param name   the name
     * @param values the values
     * @return the Value
     */
    @NotNull
    public static Value $(@NotNull String name, @NotNull Value... values) {
        Value v = $();
        for (Value value : values) {
            v = v.$plus(value);
        }
        return $(name, v);
    }

    /**
     * $ Value.
     *
     * @param name the name
     * @param o    the o
     * @return the Value
     */
    @NotNull
    public static Value $(@NotNull Object name, @NotNull Object o) {
        if ((name instanceof Value) && (o instanceof Value)) {
            if (((Value) name).map()) {
                return ((Value) name).$plus((Value) o);

            }
        }
        return DollarFactory.fromPair(name, o);
    }

    /**
     * $ Value.
     *
     * @param json the json
     * @return the Value
     */
    @NotNull
    public static Value $(@NotNull JsonObject json) {
        return DollarFactory.fromValue(json);
    }

    /**
     * $ Value.
     *
     * @param lambda the lambda
     * @return the Value
     */
    @NotNull
    public static Value $(@NotNull Pipeable lambda) {
        return DollarFactory.fromValue(lambda);
    }

    @NotNull
    public static Value $and(@NotNull Value lhs, @NotNull Value rhs) {
        return $(lhs.isTrue() && rhs.isTrue());
    }

    public static Value $blockingQueue() {
        return DollarFactory.fromQueue(new LinkedBlockingDeque<>());
    }

    /**
     * The beginning of any Dollar Code should start with a DollarStatic.run/call method. This creates an identifier
     * used to link context's together.
     *
     * @param context the current thread context
     * @param call    the lambda to run.
     * @return the Value
     */
    @NotNull
    public static Value $call(@NotNull DollarThreadContext context, @NotNull Callable<Value> call) {
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
     * $ date.
     *
     * @param date the date
     * @return the Value
     */
    @NotNull
    public static Value $date(@NotNull Date date) {
        return DollarFactory.fromValue(date);
    }

    /**
     * $ date.
     *
     * @param date the date
     * @return the Value
     */
    @NotNull
    public static Value $date(@NotNull LocalDateTime date) {
        return DollarFactory.fromValue(date);
    }

    @NotNull
    public static Value $debug(@NotNull Value v) {
        v.debug();
        return $void();
    }

    /**
     * $ dump.
     */
    public static void $dump() {
        threadContext.get().monitor().dump();
    }

    /**
     * $ dump thread.
     */
    public static void $dumpThread() {
        threadContext.get().monitor().dumpThread();
    }

    /**
     * $ end.
     *
     * @param value the value
     */
    public static void $end(@NotNull String value) {
        threadContext.get().popLabel(value);
    }

    @NotNull
    public static Value $err(@NotNull Value v) {
        v.err();
        return $void();
    }

    /**
     * $ fork.
     *
     * @param source the source
     * @param in     the in
     * @param call   the call
     * @return the Value
     */
    @NotNull
    public static Value $fork(@NotNull Source source, @NotNull Value in, @NotNull Function<Value, Value> call) {
        return executor.fork(source, in, call);
    }

    @NotNull
    public static Value $gt(@NotNull Value lhs, @NotNull Value rhs) {
        return $(lhs.compareTo(rhs) > 0);
    }

    @NotNull
    public static Value $gte(@NotNull Value lhs, @NotNull Value rhs) {
        return $(lhs.compareTo(rhs) >= 0);
    }

    @NotNull
    public static Value $json(@NotNull String json) {
        if (json.matches("^\\s*\\{.*")) {
            return $(new JsonObject(json));
        } else if (json.matches("^\\s*\\[.*")) {
            return $(new JsonArray(json));
        } else if (json.matches("^\\s*\".*\"")) {
            return $string(json.substring(1, json.length() - 1));
        } else if (json.matches("^\\s*[0-9+-]+\\s*$")) {
            return $(Integer.parseInt(json));
        } else if (json.matches("^\\s*[0-9+-.eE]+\\s*$")) {
            return $(Double.parseDouble(json));
        } else {
            throw new DollarException("Could not parse as json: " + json);
        }
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
     * @return the Value
     */
    @NotNull

    public static Value $list(@NotNull Object... values) {
        return DollarFactory.fromValue(values);
    }

    @NotNull
    public static Value $lt(@NotNull Value lhs, @NotNull Value rhs) {
        return $(lhs.compareTo(rhs) < 0);
    }

    @NotNull
    public static Value $lte(@NotNull Value lhs, @NotNull Value rhs) {
        return $(lhs.compareTo(rhs) <= 0);
    }

    @NotNull
    public static Value $map(@NotNull Value... values) {
        Value v = $();
        for (Value value : values) {
            v = v.$plus(value);
        }
        return v;
    }

    @NotNull
    public static Value $not(@NotNull Value v) {
        return $(!v.isTrue());
    }

    @NotNull
    public static Value $null(@NotNull Type type) {
        return DollarFactory.newNull(type);
    }

    @NotNull
    public static Value $or(@NotNull Value lhs, @NotNull Value rhs) {
        return $(lhs.isTrue() || rhs.isTrue());
    }

    @NotNull
    public static Value $out(@NotNull Value v) {
        v.out();
        return $void();
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     * @return the Value
     */
    @NotNull
    public static Value $range(@NotNull Value from, @NotNull Value to) {
        return DollarFactory.fromRange(from, to);
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     * @return the Value
     */
    @NotNull
    public static Value $range(long from, long to) {
        return DollarFactory.fromRange(DollarStatic.$(from), DollarStatic.$(to));
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     * @return the Value
     */
    @NotNull
    public static Value $range(double from, double to) {
        return DollarFactory.fromRange($(from), $(to));
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     * @return the Value
     */
    @NotNull
    public static Value $range(@NotNull String from, @NotNull String to) {
        return DollarFactory.fromRange($(from), $(to));
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     * @return the Value
     */
    @NotNull
    public static Value $range(@NotNull Date from, @NotNull Date to) {
        return DollarFactory.fromRange($(from), $(to));
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     * @return the Value
     */
    @NotNull
    public static Value $range(@NotNull LocalDateTime from, @NotNull LocalDateTime to) {
        return DollarFactory.fromRange($(from), $(to));
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     * @return the Value
     */
    @NotNull
    public static Value $range(@NotNull Instant from, @NotNull Instant to) {
        return DollarFactory.fromRange($(from), $(to));
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

    public static Value $string(@NotNull String s) {
        return DollarFactory.fromStringValue(s);
    }

    @NotNull
    public static Value $truthy(@NotNull Value v) {
        return $(v.truthy());
    }

    /**
     * $ uri.
     *
     * @param uri the uri
     * @return the Value
     */
    @NotNull
    public static Value $uri(@NotNull URI uri) {
        return DollarFactory.fromValue(uri);
    }

    /**
     * $ uri.
     *
     * @param uri the uri
     * @return the Value
     */
    @NotNull
    public static Value $uri(@NotNull String uri) {
        return DollarFactory.fromValue(URI.parse(uri));
    }

    /**
     * $ void.
     *
     * @return the Value
     */
    @NotNull
    public static Value $void() {
        return DollarFactory.newVoid();
    }

    /**
     * Creates a Value from a YAML formatted string as a Value
     *
     * @param yaml the YAML
     * @return a Value
     */
    @NotNull
    public static Value $yaml(@NotNull Value yaml) {
        return DollarFactory.fromYaml(yaml.toString());
    }

    /**
     * Creates a Value from a YAML file
     *
     * @param yamlFile the YAML as a file
     * @return a Value
     */
    @NotNull
    public static Value $yaml(@NotNull File yamlFile) {
        return DollarFactory.fromYaml(yamlFile);
    }

    /**
     * Creates a Value from YAML
     *
     * @param yaml the YAML
     * @return a Value
     */
    @NotNull
    public static Value $yaml(@NotNull String yaml) {
        return DollarFactory.fromYaml(yaml);
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
     * Child context.
     *
     * @param s the s
     * @return the dollar thread context
     */
    @NotNull
    public static DollarThreadContext childContext(@NotNull String s) {
        return threadContext.get().child(s);
    }

    /**
     * Context dollar thread context.
     *
     * @return the dollar thread context
     */
    @NotNull
    public static DollarThreadContext context() {
        return threadContext.get();
    }

    /**
     * Create Value.
     *
     * @return the Value
     */
    @NotNull
    public static Value create() {
        return $();
    }

    /**
     * Create Value.
     *
     * @param value the value
     * @return the Value
     */
    @NotNull
    public static Value create(@NotNull Object value) {
        return $(value);
    }

    /**
     * Fix Value.
     *
     * @param v        the v
     * @param parallel the parallel
     * @return the Value
     */
    @NotNull
    public static Value fix(@Nullable Value v, boolean parallel) {
        return (v != null) ? DollarFactory.wrap(v.$fix(parallel)) : $void();
    }

    /**
     * Fix, i.e. evaluate lambdas to the depth supplied, optionally hinting that parallel behaviour is fine
     *
     * @param v        the object to be fixed
     * @param depth    the depth at which to stop evaluation, 1 means do not penetrate any layers of maps/blocks, 2
     *                 means penetrate one layer of maps.
     * @param parallel if true parallel evaluation if fine
     * @return the 'fixed' Value
     */
    @NotNull
    public static Value fix(@Nullable Value v, int depth, boolean parallel) {
        return (v != null) ? DollarFactory.wrap(v.$fix(depth, parallel)) : $void();
    }

    /**
     * Fix Value.
     *
     * @param v the v
     * @return the Value
     */
    @NotNull
    public static Value fix(@Nullable Value v) {
        return (v != null) ? DollarFactory.wrap(v.$fix(false)) : $void();
    }

    /**
     * Fix deep.
     *
     * @param v the v
     * @return the Value
     */
    @NotNull
    public static Value fixDeep(@Nullable Value v) {
        return (v != null) ? DollarFactory.wrap(v.$fixDeep(false)) : $void();
    }

    /**
     * Fix deep.
     *
     * @param v        the v
     * @param parallel the parallel
     * @return the Value
     */
    @NotNull
    public static Value fixDeep(@Nullable Value v, boolean parallel) {
        return (v != null) ? DollarFactory.wrap(v.$fixDeep(parallel)) : $void();
    }

    /**
     * The shared configuration for Dollar.
     *
     * @return the Configuration
     */
    @NotNull
    public static Configuration getConfig() {
        return config;
    }

    public static void setConfig(@NotNull Configuration config) {
        DollarStatic.config = config;
    }

    /**
     * Handle error.
     *
     * @param throwable the throwable
     * @param failee    the failee
     * @return the Value
     */
    @NotNull
    public static Value handleError(@NotNull Throwable throwable, @Nullable Value failee) {
        if (failee == null) {
            return DollarFactory.failure(throwable);
        }
        return failee.$copy(ImmutableList.of(throwable));

    }

    /**
     * Handle interrupt.
     *
     * @param <R> the type parameter
     * @param ie  the ie
     * @return the r
     */
    @NotNull
    public static <R> R handleInterrupt(@NotNull InterruptedException ie) {
        if (Thread.interrupted()) {
            log("Interrupted");
        }
        throw new Error("Interrupted");
    }

    /**
     * Label void.
     *
     * @param label the label
     */
    public static void label(@NotNull String label) {
        context().pushLabel(label);
    }

    /**
     * Log void.
     *
     * @param message the message
     */
    public static void log(@NotNull Object message) {
        log.info("{}:{}", threadContext.get().labels(), message);
    }

    /**
     * Log void.
     *
     * @param message the message
     */
    public static void log(@NotNull String message) {
        log.info("{}:{}", threadContext.get().labels(), message);
    }

    /**
     * Log and rethrow.
     *
     * @param <R>       the type parameter
     * @param throwable the throwable
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
     * Map to json.
     *
     * @param map the map
     * @return the json object
     */
    @NotNull
    public static JsonObject mapToJson(@NotNull Multimap<String, String> map) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, Collection<String>> entry : map.asMap().entrySet()) {
            jsonObject.putString(entry.getKey(), entry.getValue().iterator().next());
        }
        return jsonObject;
    }

    /**
     * Monitor dollar monitor.
     *
     * @return the dollar monitor
     */
    @NotNull
    public static DollarMonitor monitor() {
        return threadContext.get().monitor();
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
     * Tracer state tracer.
     *
     * @return the state tracer
     */
    @NotNull
    public static StateTracer tracer() {
        return new SimpleLogStateTracer();
    }
}
