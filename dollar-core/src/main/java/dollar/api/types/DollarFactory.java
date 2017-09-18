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
import com.thoughtworks.xstream.XStream;
import dollar.api.DollarException;
import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.StateTracer;
import dollar.api.Type;
import dollar.api.Value;
import dollar.api.collections.CollectionUtil;
import dollar.api.exceptions.DollarFailureException;
import dollar.api.json.DecodeException;
import dollar.api.json.ImmutableJsonObject;
import dollar.api.json.JsonArray;
import dollar.api.json.JsonObject;
import dollar.api.json.impl.Json;
import dollar.api.monitor.DollarMonitor;
import dollar.api.script.Source;
import dollar.api.uri.URI;
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
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Stream;

public final class DollarFactory {

    /**
     * The constant DOUBLE_ZERO.
     */
    @NotNull
    public static final Value DOUBLE_ZERO = wrap(new DollarDecimal(0.0));
    /**
     * The constant FALSE.
     */
    @NotNull
    public static final Value FALSE = wrap(new DollarBoolean(false));
    @NotNull
    public static final Value INFINITY = wrap(new DollarInfinity(true));
    /**
     * The constant INTEGER_ZERO.
     */
    @NotNull
    public static final Value INTEGER_ZERO = wrap(new DollarInteger(0L));
    /**
     * The constant LOWERBOUND_KEY.
     */
    @NotNull
    public static final String LOWERBOUND_KEY = "lower";
    /**
     * The constant MILLISECOND_KEY.
     */
    @NotNull
    public static final String MILLISECOND_KEY = "millis";
    @NotNull
    public static final Value NEGATIVE_INFINITY = wrap(new DollarInfinity(false));
    /**
     * The constant VALUE_KEY.
     */
    @NotNull
    public static final String POSITIVE_KEY = "positive";
    /**
     * The constant TEXT_KEY.
     */
    @NotNull
    public static final String TEXT_KEY = "text";
    /**
     * The constant TRUE.
     */
    @NotNull
    public static final Value TRUE = wrap(new DollarBoolean(true));
    /**
     * The constant TYPE_KEY.
     */
    @NotNull
    public static final String TYPE_KEY = "$type";
    /**
     * The constant UPPERBOUND_KEY.
     */
    @NotNull
    public static final String UPPERBOUND_KEY = "upper";
    /**
     * The constant VALUE_KEY.
     */
    @NotNull
    public static final String VALUE_KEY = "value";
    /**
     * The constant VOID.
     */
    @NotNull
    public static final Value VOID = wrap(new DollarVoid());
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
     * Block collection.
     *
     * @param Value the Value
     * @return the Value
     */
    @NotNull
    public static Value blockCollection(@NotNull List<Value> Value) {
        return wrap(new DollarBlockCollection(Value));
    }

    @NotNull
    private static Value create(@Nullable Object o) {
        if (o == null) {
            return wrap(new DollarVoid());
        }
        if (o instanceof Value) {
            return (Value) o;
        }
        if (o instanceof Boolean) {
            return wrap(new DollarBoolean((Boolean) o));
        }

        if (o instanceof Pipeable) {
            return wrap((Value) java.lang.reflect.Proxy.newProxyInstance(
                    DollarStatic.class.getClassLoader(),
                    new Class<?>[]{Value.class},
                    new DollarLambda((Pipeable) o)));
        }
        if (o instanceof File) {
            try {
                try (FileInputStream stream = new FileInputStream((File) o)) {
                    return create(stream);
                }
            } catch (IOException e) {
                return failure(e);
            }
        }
        if (o instanceof JsonArray) {
            return wrap(new DollarList((JsonArray) o));
        }
        if (o instanceof JsonObject) {
            return wrap(new DollarMap(new ImmutableJsonObject((JsonObject) o)));
        }
        if ("JSONObject".equals(o.getClass().getSimpleName()) || (o instanceof ObjectNode)) {
            return wrap(new DollarMap(new JsonObject(o.toString())));
        }
        if ("JSONObject".equals(o.getClass().getSimpleName()) || (o instanceof ArrayNode)) {
            return wrap(new DollarList(new JsonArray(o.toString())));
        }
        if (o instanceof ImmutableMap) {
            return wrap(new DollarMap(((ImmutableMap<?, ?>) o)));
        }
        if (o instanceof Map) {
            return wrap(new DollarMap((Map) o));
        }
        if (o instanceof ImmutableList) {
            return wrap(new DollarList((ImmutableList<?>) o));
        }
        if (o instanceof List) {
            return wrap(new DollarList(ImmutableList.copyOf((List) o)));
        }
        if (o instanceof Collection) {
            return wrap(new DollarList(ImmutableList.copyOf(new ArrayList<>((Collection<?>) o))));
        }
        if (o.getClass().isArray()) {
            return wrap(new DollarList((Object[]) o));
        }
        if (o instanceof URI) {
            return wrap(new DollarURI((URI) o));
        }
        if ((o instanceof java.net.URI) || (o instanceof java.net.URL)) {
            return wrap(new DollarURI(URI.parse(o.toString())));
        }
        if (o instanceof Date) {
            return wrap(new DollarDate(((Date) o).getTime()));
        }
        if (o instanceof LocalDateTime) {
            return wrap(new DollarDate((LocalDateTime) o));
        }
        if (o instanceof Instant) {
            return wrap(new DollarDate((Instant) o));
        }
        if (o instanceof Double) {
            if (((Double) o == 0.0)) {
                return DOUBLE_ZERO;
            }
            return wrap(new DollarDecimal((Double) o));
        }
        if (o instanceof BigDecimal) {
            if ((((BigDecimal) o).doubleValue() == 0.0)) {
                return DOUBLE_ZERO;
            }
            return wrap(new DollarDecimal(((BigDecimal) o).doubleValue()));
        }
        if (o instanceof Float) {
            if (((Float) o == 0.0)) {
                return DOUBLE_ZERO;
            }
            return wrap(new DollarDecimal(((Float) o).doubleValue()));
        }
        if (o instanceof Long) {
            if (((Long) o == 0)) {
                return INTEGER_ZERO;
            }
            return wrap(new DollarInteger((Long) o));
        }
        if (o instanceof Integer) {
            if (((Integer) o == 0)) {
                return INTEGER_ZERO;
            }
            return wrap(new DollarInteger(((Integer) o).longValue()));
        }
        if (o instanceof Short) {
            if (((Short) o == 0)) {
                return INTEGER_ZERO;
            }
            return wrap(new DollarInteger(((Short) o).longValue()));
        }
        if (o instanceof Range) {
            return wrap(new DollarRange((Range) o, false));
        }
        if (o instanceof ImmutableJsonObject) {
            return wrap(new DollarMap((ImmutableJsonObject) o));
        }
        if (o instanceof InputStream) {
            try {
                return create(CollectionUtil.fromStream((InputStream) o));
            } catch (IOException e) {
                return failure(e);
            }
        }
        if (o instanceof String) {
            if (((String) o).matches("^[a-zA-Z0-9]+$")) {
                return wrap(new DollarString((String) o));

            } else if (((String) o).matches("^\\s*\\[.*")) {
                return wrap(new DollarList(new JsonArray(o.toString())));
            } else if (((String) o).matches("^\\s*\\{.*")) {
                try {
                    return wrap(new DollarMap(new JsonObject((String) o)));
                } catch (DecodeException de) {
                    log.error(de.getMessage(), de);
                    return wrap(new DollarString((String) o));
                }
            } else {
                return wrap(new DollarString((String) o));
            }
        }
        JsonObject json;
        if (o instanceof Multimap) {
            json = DollarStatic.mapToJson((Multimap) o);
        } else {
            json = Json.fromJavaObject(o);
        }
        return wrap(new DollarMap(json));
    }

    /**
     * Deserialize Value.
     *
     * @param s the s
     * @return the Value
     */
    @NotNull
    public static Value deserialize(@NotNull String s) {
        JsonObject jsonObject = new JsonObject(s);
        return fromJson(jsonObject);
    }

    /**
     * Deserialize Value.
     *
     * @param s the s
     * @return the Value
     */
    @NotNull
    public static Value deserialize64(@NotNull String s) {
        return (Value) new XStream().fromXML(new String(Base64.getDecoder().decode(s)));
    }

    /**
     * Failure Value.
     *
     * @param errorType the failure type
     * @param t         the t
     * @param quiet     to always avoid failing fast
     * @return the Value
     */
    @NotNull
    public static Value failure(@NotNull ErrorType errorType, @NotNull Throwable t, boolean quiet) {
        if (DollarStatic.getConfig().failFast() && !quiet) {
            throw new DollarFailureException(t, errorType);
        } else {
            return wrap(new DollarError(errorType, t.getMessage()));
        }
    }

    /**
     * Failure Value.
     *
     * @param errorType the failure type
     * @param t         the t
     * @return the Value
     */
    @NotNull
    public static Value failure(@NotNull ErrorType errorType, @NotNull Throwable t) {
        if (DollarStatic.getConfig().failFast()) {
            throw new DollarFailureException(t, errorType);
        } else {
            return wrap(new DollarError(errorType, t.getMessage()));
        }
    }

    /**
     * Failure Value.
     *
     * @param errorType the failure type
     * @param message   the message
     * @return the Value
     */
    @NotNull
    public static Value failure(@NotNull ErrorType errorType, @NotNull String message) {
        if (DollarStatic.getConfig().failFast()) {
            throw new DollarFailureException(errorType, message);
        } else {
            return wrap(new DollarError(errorType, message));
        }
    }

    /**
     * Failure Value.
     *
     * @param errorType the failure type
     * @param message   the message
     * @param quiet     the quiet
     * @return the Value
     */
    @NotNull
    public static Value failure(@NotNull ErrorType errorType, @NotNull String message, boolean quiet) {
        if (DollarStatic.getConfig().failFast() && !quiet) {
            throw new DollarFailureException(errorType, message);
        } else {
            return wrap(new DollarError(errorType, message));
        }
    }

    /**
     * Failure Value.
     *
     * @param throwable the throwable
     * @return the Value
     */
    @NotNull
    public static Value failure(@NotNull Throwable throwable) {
        return failure(ErrorType.EXCEPTION, throwable, false);
    }

    /**
     * Failure with source.
     *
     * @param errorType the failure type
     * @param throwable the throwable
     * @param source    the source
     * @return the Value
     */
    @NotNull
    public static Value failureWithSource(@NotNull ErrorType errorType, @NotNull Throwable throwable,
                                          @Nullable Source source) {
        if (source == null) {
            throw new NullPointerException();
        }
        if (DollarStatic.getConfig().failFast()) {
            final DollarFailureException dollarFailureException = new DollarFailureException(throwable, errorType);
            dollarFailureException.addSource(source);
            throw dollarFailureException;
        } else {
            return wrap(new DollarError(errorType, throwable.getMessage()));
        }
    }

    /**
     * From future.
     *
     * @param future the future
     * @return the Value
     */
    @NotNull
    public static Value fromFuture(@NotNull Future<Value> future) {
        return wrap((Value) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{Value.class},
                new DollarLambda(i -> future.get(), false)));
    }

    /**
     * From stream.
     *
     * @param type    the type
     * @param rawBody the raw body
     * @return the Value
     * @throws IOException the iO exception
     */
    @NotNull
    public static Value fromIOStream(@NotNull SerializedType type, @NotNull InputStream rawBody) throws IOException {
        if (type == SerializedType.JSON) {
            ObjectMapper mapper = new ObjectMapper();
            final JsonNode jsonNode = mapper.readTree(rawBody);
            if (jsonNode.isArray()) {
                return create(jsonNode);
            } else if (jsonNode.isObject()) {
                return create(jsonNode);
            } else {
                throw new DollarException("Could not deserialize JSON, not array or object");
            }
        } else {
            throw new DollarException("Could not deserialize " + type);
        }
    }

    @NotNull
    private static Value fromJson(@NotNull JsonObject jsonObject) {
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
            return wrap(new DollarDate(Instant.parse(jsonObject.getString(TEXT_KEY))));
        } else if (type.is(Type._DECIMAL)) {
            return fromValue(jsonObject.getNumber(VALUE_KEY));
        } else if (type.is(Type._LIST)) {
            final JsonArray array = jsonObject.getArray(VALUE_KEY);
            ArrayList<Object> arrayList = new ArrayList<>();
            for (Object o : array) {
                arrayList.add(fromJson(o));
            }
            return wrap(new DollarList(ImmutableList.copyOf(arrayList)));
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
            return wrap(new DollarMap(map));
        } else if (type.is(Type._ERROR)) {
            final String errorType = jsonObject.getString("errorType");
            final String errorMessage = jsonObject.getString("errorMessage");
            return wrap(new DollarError(ErrorType.valueOf(errorType), errorMessage));
        } else if (type.is(Type._RANGE)) {
            final Value lower = fromJson(jsonObject.get(LOWERBOUND_KEY));
            final Value upper = fromJson(jsonObject.get(UPPERBOUND_KEY));
            return wrap(new DollarRange(lower, upper));
        } else if (type.is(Type._URI)) {
            return wrap(new DollarURI(URI.parse(jsonObject.getString(VALUE_KEY))));
        } else if (type.is(Type._INFINITY)) {
            return wrap(new DollarInfinity(jsonObject.getBoolean(POSITIVE_KEY)));
        } else if (type.is(Type._STRING)) {
            if (!(jsonObject.get(VALUE_KEY) instanceof String)) {
                log.error("_STRING type is not a a java String, {}", jsonObject.get(VALUE_KEY));
            }
            return wrap(new DollarString(jsonObject.getString(VALUE_KEY)));
        } else {
            throw new DollarException("Unrecognized type " + type);
        }
    }

    @NotNull
    private static Value fromJson(@Nullable Object value) {
        if (value == null) {
            return DollarStatic.$void();
        } else if (value instanceof LinkedHashMap) {
            JsonObject json = new JsonObject((Map<String, Object>) value);
            return fromJson(json);
        } else if (value instanceof ArrayList) {
            ArrayList list = (ArrayList) value;
            ArrayList<Value> result = new ArrayList<>();
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
     * From lambda.
     *
     * @param pipeable the pipeable
     * @return the Value
     */
    @NotNull
    public static Value fromLambda(@NotNull Pipeable pipeable) {
        return fromValue(pipeable);
    }

    @NotNull
    public static Value fromList(@NotNull ImmutableList<Value> Values) {
        return wrap(new DollarList(Values));
    }

    @NotNull
    public static Value fromList(@NotNull List<Value> Values) {
        return wrap(new DollarList(ImmutableList.copyOf(Values)));
    }

    @NotNull
    public static Value fromMap(@NotNull ImmutableMap<Value, Value> entries) {
        return wrap(new DollarMap(entries));
    }

    public static Value fromPair(@NotNull Object k, @NotNull Object v) {
        return wrap(new DollarMap(ImmutableMap.of(create(k), create(v))));
    }

    @NotNull
    public static Value fromQueue(@NotNull LinkedBlockingDeque<Value> linkedBlockingDeque) {
        return wrap(new DollarQueue(ImmutableList.of(), linkedBlockingDeque));
    }

    /**
     * From range.
     *
     * @param from the from
     * @param to   the to
     * @return the Value
     */
    @NotNull
    public static Value fromRange(@NotNull Value from,
                                  @NotNull Value to) {
        return wrap(new DollarRange(from, to));
    }

    public static Value fromSet(@NotNull Set<Value> Values) {
        return wrap(new DollarList(ImmutableList.of(Values)));
    }

    public static Value fromStream(@NotNull Stream<Value> stream) {
        return wrap(new DollarStream(stream));
    }

    public static Value fromStream(@NotNull List<Value> stream, boolean parallel) {
        return wrap(new DollarStream(stream, parallel));
    }

    /**
     * From string value.
     *
     * @param body the body
     * @return the Value
     */
    @NotNull
    public static Value fromStringValue(@NotNull String body) {
        return wrap(new DollarString(body));
    }

    /**
     * From uRI.
     *
     * @param from the from
     * @return the Value
     */
    @NotNull
    public static Value fromURI(@NotNull Value from) {
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
     * @return the Value
     */
    @NotNull
    public static Value fromURI(@NotNull String uri) {
        try {
            return wrap(new DollarURI(URI.parse(uri)));
        } catch (Exception e) {
            return DollarStatic.handleError(e, null);
        }
    }

    /**
     * From value.
     *
     * @return the Value
     */
    @NotNull
    public static Value fromValue() {
        return create(new JsonObject());
    }

    /**
     * From value.
     *
     * @param o the o
     * @return the Value
     */
    @NotNull
    public static Value fromValue(@Nullable Object o) {
        return create(o);
    }

    @NotNull
    public static Value fromYaml(@NotNull String yamlString) {
        Yaml yaml = new Yaml();
        return wrap(fromValue(yaml.load(yamlString)));
    }

    @NotNull
    public static Value fromYaml(@NotNull File yamlFile) {
        Yaml yaml = new Yaml();
        try (FileInputStream fileInputStream = new FileInputStream(yamlFile)) {
            return wrap(fromValue(yaml.load(fileInputStream)));
        } catch (IOException e) {
            return failure(e);
        }
    }

    @NotNull
    public static Value infinity(boolean positive) {
        return wrap(new DollarInfinity(positive));
    }

    @Nullable
    private static Object mapToJsonInternal(@NotNull Value value) {
        final JsonObject json = new JsonObject();
        ImmutableMap<Value, Value> map = value.toVarMap();
        final Set<Value> fieldNames = map.keySet();
        for (Value fieldName : fieldNames) {
            Value v = map.get(fieldName);
            json.putValue(fieldName.toString(), toJson(v));
        }
        return json;
    }

    @NotNull
    private static List<Throwable> mergeErrors(@NotNull ImmutableList<Throwable>[] errors) {
        List<Throwable> mergedErrors = new ArrayList<>();
        for (ImmutableList<Throwable> errorList : errors) {
            mergedErrors.addAll(errorList);
        }
        return mergedErrors;
    }

    @SafeVarargs
    @NotNull
    public static Value newNull(@NotNull Type type, ImmutableList<Throwable>... errors) {
        return new DollarNull(ImmutableList.copyOf(mergeErrors(errors)), type);
    }

    /**
     * New void.
     *
     * @return the Value
     */
    @NotNull
    public static Value newVoid() {
        return wrap(new DollarVoid());
    }

    /**
     * Serialize string.
     *
     * @param value the value
     * @return the string
     */
    @Nullable
    public static String serialize(@NotNull Value value) {
        final Object jsonObject = toJson(value.$fixDeep());
        return (jsonObject == null) ? null : jsonObject.toString();
    }

    /**
     * Serialize string.
     *
     * @param value the value
     * @return the string
     */
    @Nullable
    public static String serialize64(@NotNull Value value) {
        final String xmlObject = new XStream().toXML(value.$fixDeep());
        return Base64.getEncoder().encodeToString(xmlObject.getBytes());
    }

    /**
     * To json.
     *
     * @param value the value
     * @return the object
     */
    @Nullable
    public static Object toJson(@NotNull Value value) {
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
            ImmutableList<Value> arrayList = value.toVarList();
            for (Value v : arrayList) {
                array.add(toJson(v));
            }

            return array;
        } else if (i.is(Type._MAP)) {
            return mapToJsonInternal(value);
        } else if (i.is(Type._RANGE)) {
            final JsonObject rangeObject = new JsonObject();
            rangeObject.putString(TYPE_KEY, value.$type().name());
            final Range<Value> range = value.toJavaObject();
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
    private static JsonObject valueToJson(@NotNull Value value) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.putString(TYPE_KEY, value.$type().name());
        jsonObject.putValue(VALUE_KEY, value.toJavaObject());
        return jsonObject;
    }

    /**
     * Wrap Value.
     *
     * @param value the value
     * @return the Value
     */
    @NotNull
    public static Value wrap(@NotNull Value value) {
        return wrap(value, DollarStatic.monitor(), DollarStatic.tracer());
    }

    @NotNull
    private static Value wrap(@NotNull Value value, @NotNull DollarMonitor monitor, @NotNull StateTracer tracer) {
        final Value val;
        if (DollarStatic.getConfig().wrapForMonitoring()) {
            val = new DollarWrapper(value, monitor, tracer);
        } else {
            val = value;
        }
        if (DollarStatic.getConfig().wrapForGuards()) {
            return (Value) java.lang.reflect.Proxy.newProxyInstance(
                    DollarStatic.class.getClassLoader(),
                    new Class<?>[]{Value.class},
                    new DollarGuard(val));
        } else {
            return val;
        }

    }
}
