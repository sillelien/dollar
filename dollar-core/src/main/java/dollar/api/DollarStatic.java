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
import dollar.api.script.SourceSegment;
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
    private static final ExecutorService threadPoolExecutor = Executors.newCachedThreadPool();
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(DollarStatic.class);
    @NotNull
    private static Configuration config = new SystemPropertyConfiguration();

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
     * $ range.
     *
     * @param from the from
     * @param to   the to
     * @return the var
     */
    @NotNull
    public static var $range(@NotNull var from, @NotNull var to) {
        return DollarFactory.fromRange(from, to);
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     * @return the var
     */
    @NotNull
    public static var $range(long from, long to) {
        return DollarFactory.fromRange(DollarStatic.$(from), DollarStatic.$(to));
    }

    /**
     * $ var.
     *
     * @param o the o
     * @return the var
     */
    @NotNull
    public static var $(@Nullable Object o) {
        return $(o, false);
    }

    @NotNull
    private static var $(@Nullable Object o, boolean parallel) {
        return DollarFactory.fromValue(o, ImmutableList.of());
    }

    /**
     * Fix var.
     *
     * @param v        the v
     * @param parallel the parallel
     * @return the var
     */
    @NotNull
    public static var fix(@Nullable var v, boolean parallel) {
        return (v != null) ? DollarFactory.wrap(v.$fix(parallel)) : $void();
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
    public static var $null(@NotNull Type type) {
        return DollarFactory.newNull(type);
    }

    /**
     * Fix, i.e. evaluate lambdas to the depth supplied, optionally hinting that parallel behaviour is fine
     *
     * @param v        the object to be fixed
     * @param depth    the depth at which to stop evaluation, 1 means do not penetrate any layers of maps/blocks, 2
     *                 means penetrate one layer of maps.
     * @param parallel if true parallel evaluation if fine
     * @return the 'fixed' var
     */
    @NotNull
    public static var fix(@Nullable var v, int depth, boolean parallel) {
        return (v != null) ? DollarFactory.wrap(v.$fix(depth, parallel)) : $void();
    }

    /**
     * Fix var.
     *
     * @param v the v
     * @return the var
     */
    @NotNull
    public static var fix(@Nullable var v) {
        return (v != null) ? DollarFactory.wrap(v.$fix(false)) : $void();
    }

    /**
     * Fix deep.
     *
     * @param v the v
     * @return the var
     */
    @NotNull
    public static var fixDeep(@Nullable var v) {
        return (v != null) ? DollarFactory.wrap(v.$fixDeep(false)) : $void();
    }

    /**
     * Fix deep.
     *
     * @param v        the v
     * @param parallel the parallel
     * @return the var
     */
    @NotNull
    public static var fixDeep(@Nullable var v, boolean parallel) {
        return (v != null) ? DollarFactory.wrap(v.$fixDeep(parallel)) : $void();
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     * @return the var
     */
    @NotNull
    public static var $range(double from, double to) {
        return DollarFactory.fromRange($(from), $(to));
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     * @return the var
     */
    @NotNull
    public static var $range(@NotNull String from, @NotNull String to) {
        return DollarFactory.fromRange($(from), $(to));
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     * @return the var
     */
    @NotNull
    public static var $range(@NotNull Date from, @NotNull Date to) {
        return DollarFactory.fromRange($(from), $(to));
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     * @return the var
     */
    @NotNull
    public static var $range(@NotNull LocalDateTime from, @NotNull LocalDateTime to) {
        return DollarFactory.fromRange($(from), $(to));
    }

    /**
     * $ range.
     *
     * @param from the from
     * @param to   the to
     * @return the var
     */
    @NotNull
    public static var $range(@NotNull Instant from, @NotNull Instant to) {
        return DollarFactory.fromRange($(from), $(to));
    }

    /**
     * $ uri.
     *
     * @param uri the uri
     * @return the var
     */
    @NotNull
    public static var $uri(@NotNull URI uri) {
        return DollarFactory.fromValue(uri);
    }

    /**
     * $ uri.
     *
     * @param uri the uri
     * @return the var
     */
    @NotNull
    public static var $uri(@NotNull String uri) {
        return DollarFactory.fromValue(URI.parse(uri));
    }

    /**
     * $ date.
     *
     * @param date the date
     * @return the var
     */
    @NotNull
    public static var $date(@NotNull Date date) {
        return DollarFactory.fromValue(date);
    }

    /**
     * $ date.
     *
     * @param date the date
     * @return the var
     */
    @NotNull
    public static var $date(@NotNull LocalDateTime date) {
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
        return $map(values);
    }

    @NotNull
    public static var $map(@NotNull var... values) {
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
     * @param name   the name
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
     * @param o    the o
     * @return the var
     */
    @NotNull
    public static var $(@NotNull Object name, @NotNull Object o) {
        if ((name instanceof var) && (o instanceof var)) {
            if (((var) name).map()) {
                return ((var) name).$plus((var) o);

            }
        }
        return DollarFactory.fromPair(name, o);
    }

    /**
     * $ var.
     *
     * @param json the json
     * @return the var
     */
    @NotNull
    public static var $(@NotNull JsonObject json) {
        return DollarFactory.fromValue(json, ImmutableList.of());
    }

    @NotNull
    public static var $json(@NotNull String json) {
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

    public static var $string(@NotNull String s) {
        return DollarFactory.fromStringValue(s);
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
     * The beginning of any Dollar Code should start with a DollarStatic.run/call method. This creates an identifier
     * used to link context's together.
     *
     * @param context the current thread context
     * @param call    the lambda to run.
     * @return the var
     */
    @NotNull
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
     * @param failee    the failee
     * @return the var
     */
    @NotNull
    public static var handleError(@NotNull Throwable throwable, @Nullable var failee) {
        if (failee == null) {
            return DollarFactory.failure(throwable);
        }
        return failee.$copy(ImmutableList.of(throwable));

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
     * $ fork.
     *
     * @param source
     * @param in
     * @param call   the call  @return the var
     */
    public static var $fork(SourceSegment source, var in, Function<var, var> call) {
        return executor.fork(source, in, call);
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

    public static var $list(@NotNull Object... values) {
        return DollarFactory.fromValue(values, ImmutableList.of());
    }

    public static var $blockingQueue() {
        return DollarFactory.fromQueue(new LinkedBlockingDeque<>());
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

    /**
     * Monitor dollar monitor.
     *
     * @return the dollar monitor
     */
    @NotNull
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
    public static var create(@NotNull Object value) {
        return $(value);
    }

    /**
     * Log void.
     *
     * @param message the message
     */
    public static void log(@NotNull Object message) {
        log.info("{}:{}", threadContext.get().getLabels(), message);
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
     * Log void.
     *
     * @param message the message
     */
    public static void log(@NotNull String message) {
        log.info("{}:{}", threadContext.get().getLabels(), message);
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
     * Label void.
     *
     * @param label the label
     */
    public static void label(@NotNull String label) {
        context().pushLabel(label);
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
     * $ var.
     *
     * @param lambda the lambda
     * @return the var
     */
    @NotNull
    public static var $(@NotNull Pipeable lambda) {
        return DollarFactory.fromValue(lambda);
    }


    /**
     * Creates a var from a YAML formatted string as a var
     *
     * @param yaml the YAML
     * @return a var
     */
    @NotNull
    public static var $yaml(@NotNull var yaml) {
        return DollarFactory.fromYaml(yaml.toString());
    }

    /**
     * Creates a var from a YAML file
     *
     * @param yamlFile the YAML as a file
     * @return a var
     */
    @NotNull
    public static var $yaml(@NotNull File yamlFile) {
        return DollarFactory.fromYaml(yamlFile);
    }


    /**
     * Creates a var from YAML
     *
     * @param yaml the YAML
     * @return a var
     */
    @NotNull
    public static var $yaml(@NotNull String yaml) {
        return DollarFactory.fromYaml(yaml);
    }

    /**
     * The shared configuration for Dollar.
     */
    @NotNull
    public static Configuration getConfig() {
        return config;
    }

    public static void setConfig(@NotNull Configuration config) {
        DollarStatic.config = config;
    }

    @NotNull
    public static var $truthy(@NotNull var v) {
        return $(v.truthy());
    }

    @NotNull
    public static var $gte(@NotNull var lhs, @NotNull var rhs) {
        return $(lhs.compareTo(rhs) >= 0);
    }

    @NotNull
    public static var $lte(@NotNull var lhs, @NotNull var rhs) {
        return $(lhs.compareTo(rhs) <= 0);
    }

    @NotNull
    public static var $gt(@NotNull var lhs, @NotNull var rhs) {
        return $(lhs.compareTo(rhs) > 0);
    }

    @NotNull
    public static var $lt(@NotNull var lhs, @NotNull var rhs) {
        return $(lhs.compareTo(rhs) < 0);
    }

    @NotNull
    public static var $or(@NotNull var lhs, @NotNull var rhs) {
        return $(lhs.isTrue() || rhs.isTrue());
    }

    @NotNull
    public static var $and(@NotNull var lhs, @NotNull var rhs) {
        return $(lhs.isTrue() && rhs.isTrue());
    }

    @NotNull
    public static var $not(@NotNull var v) {
        return $(!v.isTrue());
    }

    @NotNull
    public static var $err(@NotNull var v) {
        v.err();
        return $void();
    }

    @NotNull
    public static var $debug(@NotNull var v) {
        v.debug();
        return $void();
    }

    @NotNull
    public static var $out(@NotNull var v) {
        v.out();
        return $void();
    }
}
