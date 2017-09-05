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

package dollar.api.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import dollar.api.DollarException;
import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.StateTracer;
import dollar.api.Type;
import dollar.api.collections.CollectionUtil;
import dollar.api.exceptions.DollarFailureException;
import dollar.api.json.DecodeException;
import dollar.api.json.ImmutableJsonObject;
import dollar.api.json.JsonArray;
import dollar.api.json.JsonObject;
import dollar.api.json.impl.Json;
import dollar.api.monitor.DollarMonitor;
import dollar.api.script.SourceSegment;
import dollar.api.uri.URI;
import dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

public final class DollarFactory {

    /**
     * The constant VALUE_KEY.
     */
    @NotNull
    public static final String VALUE_KEY = "value";
    /**
     * The constant VALUE_KEY.
     */
    @NotNull
    public static final String POSITIVE_KEY = "positive";
    /**
     * The constant TYPE_KEY.
     */
    @NotNull
    public static final String TYPE_KEY = "$type";
    /**
     * The constant LOWERBOUND_KEY.
     */
    @NotNull
    public static final String LOWERBOUND_KEY = "lower";
    /**
     * The constant UPPERBOUND_KEY.
     */
    @NotNull
    public static final String UPPERBOUND_KEY = "upper";
    /**
     * The constant TEXT_KEY.
     */
    @NotNull
    public static final String TEXT_KEY = "text";
    /**
     * The constant MILLISECOND_KEY.
     */
    @NotNull
    public static final String MILLISECOND_KEY = "millis";
    /**
     * The constant TRUE.
     */
    @NotNull
    public static final var TRUE = wrap(new DollarBoolean(ImmutableList.of(), true));
    /**
     * The constant FALSE.
     */
    @NotNull
    public static final var FALSE = wrap(new DollarBoolean(ImmutableList.of(), false));
    /**
     * The constant VOID.
     */
    @NotNull
    public static final var VOID = wrap(new DollarVoid());
    /**
     * The constant DOUBLE_ZERO.
     */
    @NotNull
    public static final var DOUBLE_ZERO = wrap(new DollarDecimal(ImmutableList.of(), 0.0));
    /**
     * The constant INTEGER_ZERO.
     */
    @NotNull
    public static final var INTEGER_ZERO = wrap(new DollarInteger(ImmutableList.of(), 0L));
    @NotNull
    public static final var INFINITY = wrap(new DollarInfinity(true));
    @NotNull
    public static final var NEGATIVE_INFINITY = wrap(new DollarInfinity(false));
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("DollarSource");
    /**
     * The Monitor.
     */
    @NotNull
    static DollarMonitor monitor = DollarStatic.monitor();
    /**
     * The constant tracer.
     */
    @NotNull
    static StateTracer tracer = DollarStatic.tracer();

    private DollarFactory() {
    }

    /**
     * From value.
     *
     * @param o      the o
     * @param errors the errors
     * @return the var
     */
    @SafeVarargs
    @NotNull
    public static var fromValue(@Nullable Object o, @NotNull ImmutableList<Throwable>... errors) {
        return create(ImmutableList.copyOf(mergeErrors(errors)), o);
    }

    @NotNull
    private static List<Throwable> mergeErrors(@NotNull ImmutableList<Throwable>[] errors) {
        List<Throwable> mergedErrors = new ArrayList<>();
        for (ImmutableList<Throwable> errorList : errors) {
            mergedErrors.addAll(errorList);
        }
        return mergedErrors;
    }

    /**
     * From value.
     *
     * @return the var
     */
    @NotNull
    public static var fromValue() {
        return create(ImmutableList.of(), new JsonObject());
    }


    @NotNull
    private static var create(@NotNull ImmutableList<Throwable> errors, @Nullable Object o) {
        if (o == null) {
            if (errors.isEmpty()) {
                return VOID;
            }
            return wrap(new DollarVoid(errors));
        }
        if (o instanceof var) {
            return (var) o;
        }
        if (o instanceof Boolean) {
            if (errors.isEmpty()) {
                if ((Boolean) o) {
                    return TRUE;
                } else {
                    return FALSE;
                }
            }
            return wrap(new DollarBoolean(errors, (Boolean) o));
        }

        if (o instanceof Pipeable) {
            return wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                    DollarStatic.class.getClassLoader(),
                    new Class<?>[]{var.class},
                    new DollarLambda((Pipeable) o)));
        }
        if (o instanceof File) {
            try {
                try (FileInputStream stream = new FileInputStream((File) o)) {
                    return create(errors, stream);
                }
            } catch (IOException e) {
                return failure(e);
            }
        }
        if (o instanceof JsonArray) {
            return wrap(new DollarList(errors, (JsonArray) o));
        }
        if (o instanceof JsonObject) {
            return wrap(new DollarMap(errors, new ImmutableJsonObject((JsonObject) o)));
        }
        if ("JSONObject".equals(o.getClass().getSimpleName()) || (o instanceof ObjectNode)) {
            return wrap(new DollarMap(errors, new JsonObject(o.toString())));
        }
        if ("JSONObject".equals(o.getClass().getSimpleName()) || (o instanceof ArrayNode)) {
            return wrap(new DollarList(errors, new JsonArray(o.toString())));
        }
        if (o instanceof ImmutableMap) {
            return wrap(new DollarMap(errors, ((ImmutableMap<?, ?>) o)));
        }
        if (o instanceof Map) {
            return wrap(new DollarMap(errors, (Map) o));
        }
        if (o instanceof ImmutableList) {
            return wrap(new DollarList(errors, (ImmutableList<?>) o));
        }
        if (o instanceof List) {
            return wrap(new DollarList(errors, ImmutableList.copyOf((List) o)));
        }
        if (o instanceof Collection) {
            return wrap(new DollarList(errors, ImmutableList.copyOf(new ArrayList<>((Collection<?>) o))));
        }
        if (o.getClass().isArray()) {
            return wrap(new DollarList(errors, (Object[]) o));
        }
        if (o instanceof URI) {
            return wrap(new DollarURI(errors, (URI) o));
        }
        if ((o instanceof java.net.URI) || (o instanceof java.net.URL)) {
            return wrap(new DollarURI(errors, URI.parse(o.toString())));
        }
        if (o instanceof Date) {
            return wrap(new DollarDate(errors, ((Date) o).getTime()));
        }
        if (o instanceof LocalDateTime) {
            return wrap(new DollarDate(errors, (LocalDateTime) o));
        }
        if (o instanceof Instant) {
            return wrap(new DollarDate(errors, (Instant) o));
        }
        if (o instanceof Double) {
            if (errors.isEmpty() && ((Double) o == 0.0)) {
                return DOUBLE_ZERO;
            }
            return wrap(new DollarDecimal(errors, (Double) o));
        }
        if (o instanceof BigDecimal) {
            if (errors.isEmpty() && (((BigDecimal) o).doubleValue() == 0.0)) {
                return DOUBLE_ZERO;
            }
            return wrap(new DollarDecimal(errors, ((BigDecimal) o).doubleValue()));
        }
        if (o instanceof Float) {
            if (errors.isEmpty() && ((Float) o == 0.0)) {
                return DOUBLE_ZERO;
            }
            return wrap(new DollarDecimal(errors, ((Float) o).doubleValue()));
        }
        if (o instanceof Long) {
            if (errors.isEmpty() && ((Long) o == 0)) {
                return INTEGER_ZERO;
            }
            return wrap(new DollarInteger(errors, (Long) o));
        }
        if (o instanceof Integer) {
            if (errors.isEmpty() && ((Integer) o == 0)) {
                return INTEGER_ZERO;
            }
            return wrap(new DollarInteger(errors, ((Integer) o).longValue()));
        }
        if (o instanceof Short) {
            if (errors.isEmpty() && ((Short) o == 0)) {
                return INTEGER_ZERO;
            }
            return wrap(new DollarInteger(errors, ((Short) o).longValue()));
        }
        if (o instanceof Range) {
            return wrap(new DollarRange(errors, (Range) o, false));
        }
        if (o instanceof ImmutableJsonObject) {
            return wrap(new DollarMap(errors, (ImmutableJsonObject) o));
        }
        if (o instanceof InputStream) {
            try {
                return create(errors, CollectionUtil.fromStream((InputStream) o));
            } catch (IOException e) {
                return failure(e);
            }
        }
        if (o instanceof String) {
            if (((String) o).matches("^[a-zA-Z0-9]+$")) {
                return wrap(new DollarString(errors, (String) o));

            } else if (((String) o).matches("^\\s*\\[.*")) {
                return wrap(new DollarList(errors, new JsonArray(o.toString())));
            } else if (((String) o).matches("^\\s*\\{.*")) {
                try {
                    return wrap(new DollarMap(errors, new JsonObject((String) o)));
                } catch (DecodeException de) {
                    log.error(de.getMessage(), de);
                    return wrap(new DollarString(errors, (String) o));
                }
            } else {
                return wrap(new DollarString(errors, (String) o));
            }
        }
        JsonObject json;
        if (o instanceof Multimap) {
            json = DollarStatic.mapToJson((Multimap) o);
        } else {
            json = Json.fromJavaObject(o);
        }
        return wrap(new DollarMap(errors, json));
    }

    /**
     * From value.
     *
     * @param o the o
     * @return the var
     */
    @NotNull
    public static var fromValue(@Nullable Object o) {
        return fromValue(o, ImmutableList.of());
    }

    /**
     * Wrap var.
     *
     * @param value the value
     * @return the var
     */
    @NotNull
    public static var wrap(@NotNull var value) {
        return wrap(value, DollarStatic.monitor(), DollarStatic.tracer());
    }

    @NotNull
    private static var wrap(@NotNull var value, @NotNull DollarMonitor monitor, @NotNull StateTracer tracer) {
        final var val;
        if (DollarStatic.getConfig().wrapForMonitoring()) {
            val = new DollarWrapper(value, monitor, tracer);
        } else {
            val = value;
        }
        if (DollarStatic.getConfig().wrapForGuards()) {
            return (var) java.lang.reflect.Proxy.newProxyInstance(
                    DollarStatic.class.getClassLoader(),
                    new Class<?>[]{var.class},
                    new DollarGuard(val));
        } else {
            return val;
        }

    }

    /**
     * Failure var.
     *
     * @param errorType the failure type
     * @param t         the t
     * @param quiet     to always avoid failing fast
     * @return the var
     */
    @NotNull
    public static var failure(@NotNull ErrorType errorType, @NotNull Throwable t, boolean quiet) {
        if (DollarStatic.getConfig().failFast() && !quiet) {
            throw new DollarFailureException(t, errorType);
        } else {
            return wrap(new DollarError(ImmutableList.of(t), errorType, t.getMessage()));
        }
    }

    /**
     * Failure var.
     *
     * @param errorType the failure type
     * @param t         the t
     * @return the var
     */
    @NotNull
    public static var failure(@NotNull ErrorType errorType, @NotNull Throwable t) {
        if (DollarStatic.getConfig().failFast()) {
            throw new DollarFailureException(t, errorType);
        } else {
            return wrap(new DollarError(ImmutableList.of(t), errorType, t.getMessage()));
        }
    }


    /**
     * Failure var.
     *
     * @param errorType the failure type
     * @param message   the message
     * @return the var
     */
    @NotNull
    public static var failure(@NotNull ErrorType errorType, @NotNull String message) {
        if (DollarStatic.getConfig().failFast()) {
            throw new DollarFailureException(errorType, message);
        } else {
            return wrap(new DollarError(ImmutableList.of(new DollarException(message)), errorType, message));
        }
    }

    /**
     * Failure var.
     *
     * @param errorType the failure type
     * @param message   the message
     * @param quiet     the quiet
     * @return the var
     */
    @NotNull
    public static var failure(@NotNull ErrorType errorType, @NotNull String message, boolean quiet) {
        if (DollarStatic.getConfig().failFast() && !quiet) {
            throw new DollarFailureException(errorType, message);
        } else {
            return wrap(new DollarError(ImmutableList.of(new DollarException(message)), errorType, message));
        }
    }


    /**
     * Failure var.
     *
     * @param throwable the throwable
     * @return the var
     */
    @NotNull
    public static var failure(@NotNull Throwable throwable) {
        return failure(ErrorType.EXCEPTION, throwable, false);
    }

    /**
     * New void.
     *
     * @return the var
     */
    @NotNull
    public static var newVoid() {
        return wrap(new DollarVoid());
    }

    /**
     * From string value.
     *
     * @param body the body
     * @return the var
     */
    @NotNull
    public static var fromStringValue(@NotNull String body) {
        return wrap(new DollarString(ImmutableList.of(), body));
    }

    /**
     * From string value.
     *
     * @param body the body
     * @return the var
     */
    @SafeVarargs
    @NotNull
    public static var fromStringValue(@NotNull String body, ImmutableList<Throwable>... errors) {
        return wrap(new DollarString(ImmutableList.copyOf(mergeErrors(errors)), body));
    }

    /**
     * From lambda.
     *
     * @param pipeable the pipeable
     * @return the var
     */
    @NotNull
    public static var fromLambda(@NotNull Pipeable pipeable) {
        return fromValue(pipeable);
    }

    /**
     * From uRI.
     *
     * @param from the from
     * @return the var
     */
    @NotNull
    public static var fromURI(@NotNull var from) {
        if (from.uri()) {
            return from;
        } else {
            return fromURI(from.$S());
        }
    }

    /**
     * From uRI.
     *
     * @param uri the uri
     * @return the var
     */
    @NotNull
    public static var fromURI(@NotNull String uri) {
        try {
            return wrap(new DollarURI(ImmutableList.of(), URI.parse(uri)));
        } catch (Exception e) {
            return DollarStatic.handleError(e, null);
        }
    }

    /**
     * From stream.
     *
     * @param type    the type
     * @param rawBody the raw body
     * @return the var
     * @throws IOException the iO exception
     */
    @NotNull
    public static var fromStream(@NotNull SerializedType type, @NotNull InputStream rawBody) throws IOException {
        if (type == SerializedType.JSON) {
            ObjectMapper mapper = new ObjectMapper();
            final JsonNode jsonNode = mapper.readTree(rawBody);
            if (jsonNode.isArray()) {
                return create(ImmutableList.of(), jsonNode);
            } else if (jsonNode.isObject()) {
                return create(ImmutableList.of(), jsonNode);
            } else {
                throw new DollarException("Could not deserialize JSON, not array or object");
            }
        } else {
            throw new DollarException("Could not deserialize " + type);
        }
    }

    /**
     * From future.
     *
     * @param future the future
     * @return the var
     */
    @NotNull
    public static var fromFuture(@NotNull Future<var> future) {
        return wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarLambda(i -> future.get(), false)));
    }

    /**
     * Failure with source.
     *
     * @param errorType the failure type
     * @param throwable the throwable
     * @param source    the source
     * @return the var
     */
    @NotNull
    public static var failureWithSource(@NotNull ErrorType errorType, @NotNull Throwable throwable,
                                        @Nullable SourceSegment source) {
        if (source == null) {
            throw new NullPointerException();
        }
        if (DollarStatic.getConfig().failFast()) {
            final DollarFailureException dollarFailureException = new DollarFailureException(throwable, errorType);
            dollarFailureException.addSource(source);
            throw dollarFailureException;
        } else {
            return wrap(new DollarError(ImmutableList.of(throwable), errorType, throwable.getMessage()));
        }
    }

    /**
     * Block collection.
     *
     * @param var the var
     * @return the var
     */
    @NotNull
    public static var blockCollection(@NotNull List<var> var) {
        return wrap(new DollarBlockCollection(var));
    }

    /**
     * Deserialize var.
     *
     * @param s the s
     * @return the var
     */
    @NotNull
    public static var deserialize(@NotNull String s) {
        JsonObject jsonObject = new JsonObject(s);
        return fromJson(jsonObject);
    }

    @NotNull
    private static var fromJson(@NotNull JsonObject jsonObject) {
        final Type type;
        if (!jsonObject.containsField(TYPE_KEY)) {
            type = Type._MAP;
        } else {
            type = Type.of(jsonObject.getString(TYPE_KEY));
        }

        if (type.is(Type._VOID)) {
            return DollarStatic.$void();
        } else if (type.is(Type._INTEGER)) {
            return fromValue(jsonObject.getLong(VALUE_KEY));
        } else if (type.is(Type._BOOLEAN)) {
            return fromValue(jsonObject.getBoolean(VALUE_KEY));
        } else if (type.is(Type._DATE)) {
            return wrap(new DollarDate(ImmutableList.of(), Instant.parse(jsonObject.getString(TEXT_KEY))));
        } else if (type.is(Type._DECIMAL)) {
            return fromValue(jsonObject.getNumber(VALUE_KEY));
        } else if (type.is(Type._LIST)) {
            final JsonArray array = jsonObject.getArray(VALUE_KEY);
            ArrayList<Object> arrayList = new ArrayList<>();
            for (Object o : array) {
                arrayList.add(fromJson(o));
            }
            return wrap(new DollarList(ImmutableList.of(), ImmutableList.copyOf(arrayList)));
        } else if (type.is(Type._MAP)) {
            final JsonObject json;
            json = jsonObject;
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            final Set<String> fieldNames = json.getFieldNames();
            for (String fieldName : fieldNames) {
                if (!fieldName.equals(TYPE_KEY)) {
                    map.put(fieldName, fromJson(json.get(fieldName)));
                }
            }
            return wrap(new DollarMap(ImmutableList.of(), map));
        } else if (type.is(Type._ERROR)) {
            final String errorType = jsonObject.getString("errorType");
            final String errorMessage = jsonObject.getString("errorMessage");
            return wrap(new DollarError(ImmutableList.of(), ErrorType.valueOf(errorType), errorMessage));
        } else if (type.is(Type._RANGE)) {
            final var lower = fromJson(jsonObject.get(LOWERBOUND_KEY));
            final var upper = fromJson(jsonObject.get(UPPERBOUND_KEY));
            return wrap(new DollarRange(ImmutableList.of(), lower, upper));
        } else if (type.is(Type._URI)) {
            return wrap(new DollarURI(ImmutableList.of(), URI.parse(jsonObject.getString(VALUE_KEY))));
        } else if (type.is(Type._INFINITY)) {
            return wrap(new DollarInfinity(ImmutableList.of(), jsonObject.getBoolean(POSITIVE_KEY)));
        } else if (type.is(Type._STRING)) {
            if (!(jsonObject.get(VALUE_KEY) instanceof String)) {
                log.error("_STRING type is not a a java String, {}", jsonObject.get(VALUE_KEY));
            }
            return wrap(new DollarString(ImmutableList.of(), jsonObject.getString(VALUE_KEY)));
        } else {
            throw new DollarException("Unrecognized type " + type);
        }
    }

    @NotNull
    private static var fromJson(@Nullable Object value) {
        if (value == null) {
            return DollarStatic.$void();
        } else if (value instanceof LinkedHashMap) {
            JsonObject json = new JsonObject((Map<String, Object>) value);
            return fromJson(json);
        } else if (value instanceof ArrayList) {
            ArrayList list = (ArrayList) value;
            ArrayList<var> result = new ArrayList<>();
            for (Object o : list) {
                result.add(fromJson(o));
            }
            return fromValue(result);

        } else if (value instanceof JsonObject) {
            if (((JsonObject) value).containsField(TYPE_KEY)) {
                return fromJson((JsonObject) value);
            } else {
                return fromValue(value);
            }
        } else if (value instanceof JsonArray) {
            return fromValue(value);
        } else if (value instanceof String) {
            return fromValue(value);
        } else if (value instanceof Number) {
            return fromValue(value);
        } else if (value instanceof Boolean) {
            return fromValue(value);
        } else if (value instanceof byte[]) {
            return fromValue(value);
        } else {
            throw new DollarException("Unrecognized type " + value.getClass() + " for " + value);
        }
    }

    /**
     * Serialize string.
     *
     * @param value the value
     * @return the string
     */
    @Nullable
    public static String serialize(@NotNull var value) {
        final Object jsonObject = toJson(value.$fixDeep());
        return (jsonObject == null) ? null : jsonObject.toString();
    }

    @NotNull
    private static JsonObject valueToJson(@NotNull var value) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.putString(TYPE_KEY, value.$type().name());
        jsonObject.putValue(VALUE_KEY, value.toJavaObject());
        return jsonObject;
    }

    /**
     * To json.
     *
     * @param value the value
     * @return the object
     */
    @Nullable
    public static Object toJson(@NotNull var value) {
        Type i = value.$type();
        if (i.is(Type._VOID) ||
                    i.is(Type._INTEGER) ||
                    i.is(Type._BOOLEAN) ||
                    i.is(Type._DECIMAL) ||
                    i.is(
                            Type._STRING)) {
            return value.toJavaObject();
        } else if (i.is(Type._DATE)) {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.putString(TYPE_KEY, value.$type().name());
            jsonObject.putString(TEXT_KEY, value.$S());
            jsonObject.putNumber(MILLISECOND_KEY, (long) (value.toDouble() * 24 * 60 * 60 * 1000));
            return jsonObject;
        } else if (i.is(Type._URI)) {
            final JsonObject uriJsonObject = new JsonObject();
            uriJsonObject.putString(TYPE_KEY, value.$type().name());
            uriJsonObject.putString(VALUE_KEY, value.$S());
            return uriJsonObject;
        } else if (i.is(Type._ERROR)) {
            final JsonObject errorJsonObject = new JsonObject();
            errorJsonObject.putString(TYPE_KEY, value.$type().name());
            errorJsonObject.putValue(VALUE_KEY, value.toJsonType());
            return errorJsonObject;
        } else if (i.is(Type._INFINITY)) {
            final JsonObject infinityJsonObject = new JsonObject();
            infinityJsonObject.putString(TYPE_KEY, value.$type().name());
            infinityJsonObject.putValue(POSITIVE_KEY, value.positive());
            return infinityJsonObject;
        } else if (i.is(Type._LIST) || i.is(Type._QUEUE)) {
            final JsonArray array = new JsonArray();
            ImmutableList<var> arrayList = value.toVarList();
            for (var v : arrayList) {
                array.add(toJson(v));
            }

            return array;
        } else if (i.is(Type._MAP)) {
            return mapToJsonInternal(value);
        } else if (i.is(Type._RANGE)) {
            final JsonObject rangeObject = new JsonObject();
            rangeObject.putString(TYPE_KEY, value.$type().name());
            final Range<var> range = value.toJavaObject();
            rangeObject.putValue(LOWERBOUND_KEY, toJson(range.lowerEndpoint()));
            rangeObject.putValue(UPPERBOUND_KEY, toJson(range.upperEndpoint()));
            return rangeObject;
        } else if (i.is(Type._ANY)) {
            return null;
        } else {
            return mapToJsonInternal(value);
        }
    }

    @NotNull
    @Nullable
    private static Object mapToJsonInternal(@NotNull var value) {
        final JsonObject json = new JsonObject();
        ImmutableMap<var, var> map = value.toVarMap();
        final Set<var> fieldNames = map.keySet();
        for (var fieldName : fieldNames) {
            var v = map.get(fieldName);
            json.putValue(fieldName.toString(), toJson(v));
        }
        return json;
    }

    /**
     * From range.
     *
     * @param from the from
     * @param to   the to
     * @return the var
     */
    @NotNull
    public static var fromRange(@NotNull var from,
                                @NotNull var to) {
        return wrap(new DollarRange(ImmutableList.of(), from, to));
    }

    @SafeVarargs
    @NotNull
    public static var infinity(boolean positive, ImmutableList<Throwable>... errors) {
        return wrap(new DollarInfinity(ImmutableList.copyOf(mergeErrors(errors)), positive));
    }

    @SafeVarargs
    @NotNull
    public static var newNull(@NotNull Type type, ImmutableList<Throwable>... errors) {
        return new DollarNull(ImmutableList.copyOf(mergeErrors(errors)), type);
    }

    @NotNull
    public static var fromYaml(@NotNull String yamlString) {
        Yaml yaml = new Yaml();
        return wrap(fromValue(yaml.load(yamlString)));
    }

    @NotNull
    public static var fromYaml(@NotNull File yamlFile) {
        Yaml yaml = new Yaml();
        try (FileInputStream fileInputStream = new FileInputStream(yamlFile)) {
            return wrap(fromValue(yaml.load(fileInputStream)));
        } catch (IOException e) {
            return failure(e);
        }
    }

    @NotNull
    public static var fromMap(@NotNull ImmutableMap<var, var> entries) {
        return wrap(new DollarMap(ImmutableList.of(), entries));
    }

    @NotNull
    public static var fromList(@NotNull ImmutableList<var> vars) {
        return wrap(new DollarList(ImmutableList.of(), vars));
    }

    @NotNull
    public static var fromList(@NotNull List<var> vars) {
        return wrap(new DollarList(ImmutableList.of(), ImmutableList.copyOf(vars)));
    }

    @NotNull
    public static var fromQueue(@NotNull LinkedBlockingDeque<var> linkedBlockingDeque) {
        return wrap(new DollarQueue(ImmutableList.of(), linkedBlockingDeque));
    }

    public static var fromPair(@NotNull Object k, @NotNull Object v) {
        ImmutableList<Throwable> empty = ImmutableList.of();
        return wrap(new DollarMap(empty, ImmutableMap.of(create(empty, k), create(empty, v))));
    }

    public static var fromSet(@NotNull Set<var> vars) {
        return wrap(new DollarList(ImmutableList.of(), ImmutableList.of(vars)));
    }
}
