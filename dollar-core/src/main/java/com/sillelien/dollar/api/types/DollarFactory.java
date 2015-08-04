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

package com.sillelien.dollar.api.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sillelien.dollar.api.*;
import com.sillelien.dollar.api.collections.*;
import com.sillelien.dollar.api.exceptions.DollarFailureException;
import com.sillelien.dollar.api.json.DecodeException;
import com.sillelien.dollar.api.json.ImmutableJsonObject;
import com.sillelien.dollar.api.json.JsonArray;
import com.sillelien.dollar.api.json.JsonObject;
import com.sillelien.dollar.api.json.impl.Json;
import com.sillelien.dollar.api.monitor.DollarMonitor;
import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.uri.URI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Future;

import static com.sillelien.dollar.api.DollarStatic.$void;

public class DollarFactory {
    /**
     * The constant VALUE_KEY.
     */
    public static final String VALUE_KEY = "value";
    /**
     * The constant VALUE_KEY.
     */
    public static final String POSITIVE_KEY = "positive";
    /**
     * The constant TYPE_KEY.
     */
    public static final String TYPE_KEY = "$type";
    /**
     * The constant LOWERBOUND_KEY.
     */
    public static final String LOWERBOUND_KEY = "lower";
    /**
     * The constant UPPERBOUND_KEY.
     */
    public static final String UPPERBOUND_KEY = "upper";
    /**
     * The constant TEXT_KEY.
     */
    public static final String TEXT_KEY = "text";
    /**
     * The constant MILLISECOND_KEY.
     */
    public static final String MILLISECOND_KEY = "millis";
    /**
     * The constant TRUE.
     */
    public static final var TRUE = wrap(new DollarBoolean(ImmutableList.of(), true));
    /**
     * The constant FALSE.
     */
    public static final var FALSE = wrap(new DollarBoolean(ImmutableList.of(), false));
    /**
     * The constant VOID.
     */
    public static final var VOID = wrap(new DollarVoid());
    /**
     * The constant DOUBLE_ZERO.
     */
    public static final var DOUBLE_ZERO = wrap(new DollarDecimal(ImmutableList.of(), 0.0));
    /**
     * The constant INTEGER_ZERO.
     */
    public static final var INTEGER_ZERO = wrap(new DollarInteger(ImmutableList.of(), 0L));

    public static final var INFINITY = wrap(new DollarInfinity(true));


    /**
     * The Monitor.
     */
    static DollarMonitor monitor = DollarStatic.monitor();
    /**
     * The constant tracer.
     */
    @NotNull
    static StateTracer tracer = DollarStatic.tracer();


    /**
     * From value.
     *
     * @param o      the o
     * @param errors the errors
     *
     * @return the var
     */
    @SafeVarargs @NotNull
    public static var fromValue(Object o, @NotNull ImmutableList<Throwable>... errors) {
        return create(ImmutableList.copyOf(errors), o);
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
            if (errors.size() == 0) {
                return VOID;
            }
            return wrap(new DollarVoid(errors));
        }
        if (o instanceof var) {
            return (var) o;
        }
        if (o instanceof Boolean) {
            if (errors.size() == 0) {
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
        if (o instanceof JsonArray) {
            return wrap(new DollarList(errors, (JsonArray) o));
        }
        if (o instanceof JsonObject) {
            return wrap(new DollarMap(errors, new ImmutableJsonObject((JsonObject) o)));
        }
        if (o instanceof JSONObject || o instanceof ObjectNode || o instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
            return wrap(new DollarMap(errors, new JsonObject(o.toString())));
        }
        if (o instanceof JSONArray || o instanceof ArrayNode || o instanceof com.fasterxml.jackson.databind.node.ArrayNode) {
            return wrap(new DollarList(errors, new JsonArray(o.toString())));
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
        if (o instanceof java.net.URI || o instanceof java.net.URL) {
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
            if (errors.size() == 0 && (Double) o == 0.0) {
                return DOUBLE_ZERO;
            }
            return wrap(new DollarDecimal(errors, (Double) o));
        }
        if (o instanceof BigDecimal) {
            if (errors.size() == 0 && ((BigDecimal) o).doubleValue() == 0.0) {
                return DOUBLE_ZERO;
            }
            return wrap(new DollarDecimal(errors, ((BigDecimal) o).doubleValue()));
        }
        if (o instanceof Float) {
            if (errors.size() == 0 && (Float) o == 0.0) {
                return DOUBLE_ZERO;
            }
            return wrap(new DollarDecimal(errors, ((Float) o).doubleValue()));
        }
        if (o instanceof Long) {
            if (errors.size() == 0 && (Long) o == 0) {
                return INTEGER_ZERO;
            }
            return wrap(new DollarInteger(errors, (Long) o));
        }
        if (o instanceof Integer) {
            if (errors.size() == 0 && (Integer) o == 0) {
                return INTEGER_ZERO;
            }
            return wrap(new DollarInteger(errors, ((Integer) o).longValue()));
        }
        if (o instanceof Short) {
            if (errors.size() == 0 && (Short) o == 0) {
                return INTEGER_ZERO;
            }
            return wrap(new DollarInteger(errors, ((Short) o).longValue()));
        }
        if (o instanceof Range) {
            return wrap(new DollarRange(errors, (Range) o));
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
                    de.printStackTrace(System.err);
                    return wrap(new DollarString(errors, (String) o));
                }
            } else {
                return wrap(new DollarString(errors, (String) o));
            }
        }
        JsonObject json;
        if (o instanceof MultiMap) {
            json = DollarStatic.mapToJson((MultiMap) o);
        } else {
            json = Json.fromJavaObject(o);
        }
        return wrap(new DollarMap(errors, json));
    }

//
//    /**
//     * From gson object.
//     *
//     * @param o the o
//     * @return the var
//     */
//    @NotNull
//    public static var fromGsonObject(Object o) {
//        Gson gson = new Gson();
//        String json = gson.toJson(o);
//        return create(ImmutableList.<Throwable>of(), json);
//    }

    /**
     * From value.
     *
     * @param o the o
     * @return the var
     */
    @NotNull
    public static var fromValue(Object o) {
        return fromValue(o, ImmutableList.of());
    }

    /**
     * Failure var.
     *
     * @param errorType the failure type
     * @return the var
     */
    @NotNull
    public static var failure(ErrorType errorType) {
        if (DollarStatic.getConfig().failFast()) {
            throw new DollarFailureException(errorType);
        } else {
            return wrap(new DollarError(errorType, ""));
        }
    }

    /**
     * Wrap var.
     *
     * @param value the value
     * @return the var
     */
    @NotNull
    public static var wrap(var value) {
        return wrap(value, DollarStatic.monitor(), DollarStatic.tracer());
    }

    @NotNull
    private static var wrap(var value, DollarMonitor monitor, StateTracer tracer) {
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
     * @param t the t
     * @param quiet to always avoid failing fast
     * @return the var
     */
    @NotNull
    public static var failure(ErrorType errorType, @NotNull Throwable t, boolean quiet) {
        if (DollarStatic.getConfig().failFast() && !quiet) {
            throw new DollarFailureException(t, errorType);
        } else {
//            t.printStackTrace(System.err);
            return wrap(new DollarError(ImmutableList.of(t), errorType, t.getMessage()));
        }
    }

    /**
     * Failure var.
     *
     * @param errorType the failure type
     * @param message the message
     * @param quiet the quiet
     * @return the var
     */
    @NotNull public static var failure(ErrorType errorType, String message, boolean quiet) {
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
    @NotNull public static var failure(@NotNull Throwable throwable) {
        return failure(ErrorType.EXCEPTION, throwable, false);
    }

    /**
     * New void.
     *
     * @return the var
     */
    @NotNull public static var newVoid() {
        return wrap(new DollarVoid());
    }

    /**
     * From string value.
     *
     * @param body the body
     * @return the var
     */
    @NotNull public static var fromStringValue(@NotNull String body) {
        return wrap(new DollarString(ImmutableList.of(), body));
    }

    /**
     * From string value.
     *
     * @param body the body
     * @return the var
     */
    @SafeVarargs @NotNull public static var fromStringValue(@NotNull String body, ImmutableList<Throwable>... errors) {
        return wrap(new DollarString(ImmutableList.copyOf(errors), body));
    }

    /**
     * From lambda.
     *
     * @param pipeable the pipeable
     * @return the var
     */
    @NotNull public static var fromLambda(Pipeable pipeable) {
        return fromValue(pipeable);
    }

    /**
     * From uRI.
     *
     * @param from the from
     * @return the var
     */
    @NotNull public static var fromURI(@NotNull var from) {
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
    @NotNull public static var fromURI(String uri) {
        try {
            return wrap(new DollarURI(ImmutableList.of(), URI.parse(uri)));
        } catch (Exception e) {
            return DollarStatic.handleError(e, null);
        }
    }

    /**
     * From stream.
     *
     * @param type the type
     * @param rawBody the raw body
     * @return the var
     * @throws IOException the iO exception
     */
    @NotNull public static var fromStream(SerializedType type, InputStream rawBody) throws IOException {
        if (type == SerializedType.JSON) {
            ObjectMapper mapper = new ObjectMapper();
            final JsonNode jsonNode = mapper.readTree(rawBody);
            if (jsonNode.isArray()) {
                return create(ImmutableList.<Throwable>of(), jsonNode);
            } else if (jsonNode.isObject()) {
                return create(ImmutableList.<Throwable>of(), jsonNode);
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
    @NotNull public static var fromFuture(@NotNull Future<var> future) {
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
     * @param source the source
     * @return the var
     */
    @NotNull public static var failureWithSource(ErrorType errorType, Throwable throwable,
                                                 @Nullable SourceSegment source) {
        if (source == null) {
            throw new NullPointerException();
        }
        if (DollarStatic.getConfig().failFast()) {
            final DollarFailureException dollarFailureException = new DollarFailureException(throwable, errorType);
            dollarFailureException.addSource(source);
            throw dollarFailureException;
        } else {
            return wrap(new DollarError(ImmutableList.of(throwable), errorType, null));
        }
    }

    /**
     * Block collection.
     *
     * @param var the var
     * @return the var
     */
    @NotNull public static var blockCollection(List<var> var) {
        return wrap(new DollarBlockCollection(var));
    }

    /**
     * Deserialize var.
     *
     * @param s the s
     * @return the var
     */
    @NotNull public static var deserialize(String s) {
        JsonObject jsonObject = new JsonObject(s);
        return fromJson(jsonObject);
    }

    @NotNull private static var fromJson(@NotNull JsonObject jsonObject) {
        final Type type;
        if (!jsonObject.containsField(TYPE_KEY)) {
            type = Type.MAP;
        } else {
            type = Type.valueOf(jsonObject.getString(TYPE_KEY));
        }

        if (type.equals(Type.VOID)) {
            return $void();
        } else if (type.equals(Type.INTEGER)) {
            return fromValue(jsonObject.getLong(VALUE_KEY));
        } else if (type.equals(Type.BOOLEAN)) {
            return fromValue(jsonObject.getBoolean(VALUE_KEY));
        } else if (type.equals(Type.DATE)) {
            return wrap(new DollarDate(ImmutableList.of(), Instant.parse(jsonObject.getString(TEXT_KEY))));
        } else if (type.equals(Type.DECIMAL)) {
            return fromValue(jsonObject.getNumber(VALUE_KEY));
        } else if (type.equals(Type.LIST)) {
            final JsonArray array = jsonObject.getArray(VALUE_KEY);
            ArrayList<Object> arrayList = new ArrayList<>();
            for (Object o : array) {
                arrayList.add(fromJson(o));
            }
            return wrap(new DollarList(ImmutableList.of(), ImmutableList.copyOf(arrayList)));
        } else if (type.equals(Type.MAP)) {
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
        } else if (type.equals(Type.ERROR)) {
            final String errorType = jsonObject.getString("errorType");
            final String errorMessage = jsonObject.getString("errorMessage");
            return wrap(new DollarError(ImmutableList.<Throwable>of(), ErrorType.valueOf(errorType), errorMessage));
        } else if (type.equals(Type.RANGE)) {
            final var lower = fromJson(jsonObject.get(LOWERBOUND_KEY));
            final var upper = fromJson(jsonObject.get(UPPERBOUND_KEY));
            return wrap(new DollarRange(ImmutableList.of(), lower, upper));
        } else if (type.equals(Type.URI)) {
            return wrap(new DollarURI(ImmutableList.of(), URI.parse(jsonObject.getString(VALUE_KEY))));
        } else if (type.equals(Type.INFINITY)) {
            return wrap(new DollarInfinity(ImmutableList.of(), jsonObject.getBoolean(POSITIVE_KEY)));
        } else if (type.equals(Type.STRING)) {
            if (!(jsonObject.get(VALUE_KEY) instanceof String)) {
                System.out.println(jsonObject.get(VALUE_KEY));
            }
            return wrap(new DollarString(ImmutableList.of(), jsonObject.getString(VALUE_KEY)));
        } else {
            throw new DollarException("Unrecognized type " + type);
        }
    }

    @NotNull private static var fromJson(@Nullable Object value) {
        if (value == null) {
            return $void();
        } else if (value instanceof LinkedHashMap) {
            JsonObject json = new JsonObject((Map<String, Object>) value);
//            if (json.containsField(TYPE_KEY)) {
                return fromJson(json);
//            } else {
//                return fromValue(value);
//            }
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
    public static String serialize(@NotNull var value) {
        final Object jsonObject = toJson(value._fixDeep());
        return jsonObject.toString();
    }

    @NotNull private static JsonObject valueToJson(@NotNull var value) {
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
    @Nullable public static Object toJson(@NotNull var value) {
        Type i = value.$type();
        if (i.equals(Type.VOID) ||
            i.equals(Type.INTEGER) ||
            i.equals(Type.BOOLEAN) ||
            i.equals(Type.DECIMAL) ||
            i.equals(
                    Type.STRING)) {
            return value.toJavaObject();
        } else if (i.equals(Type.DATE)) {
            final JsonObject jsonObject = new JsonObject();
            jsonObject.putString(TYPE_KEY, value.$type().name());
            jsonObject.putString(TEXT_KEY, value.$S());
            jsonObject.putNumber(MILLISECOND_KEY, (long) (value.toDouble() * 24 * 60 * 60 * 1000));
            return jsonObject;
        } else if (i.equals(Type.URI)) {
            final JsonObject uriJsonObject = new JsonObject();
            uriJsonObject.putString(TYPE_KEY, value.$type().name());
            uriJsonObject.putString(VALUE_KEY, value.$S());
            return uriJsonObject;
        } else if (i.equals(Type.ERROR)) {
            final JsonObject errorJsonObject = new JsonObject();
            errorJsonObject.putString(TYPE_KEY, value.$type().name());
            errorJsonObject.putValue(VALUE_KEY, value.toJsonType());
            return errorJsonObject;
        } else if (i.equals(Type.INFINITY)) {
            final JsonObject infinityJsonObject = new JsonObject();
            infinityJsonObject.putString(TYPE_KEY, value.$type().name());
            infinityJsonObject.putValue(POSITIVE_KEY, value.positive());
            return infinityJsonObject;
        } else if (i.equals(Type.LIST)) {
            final JsonArray array = new JsonArray();
            ImmutableList<var> arrayList = value.$list();
            for (var v : arrayList) {
                array.add(toJson(v));
            }

            return array;
        } else if (i.equals(Type.MAP)) {
            final JsonObject json = new JsonObject();
            ImmutableMap<var, var> map = value.$map();
            final Set<var> fieldNames = map.keySet();
            for (var fieldName : fieldNames) {
                var v = map.get(fieldName);
                json.putValue(fieldName.toString(), toJson(v));
            }
            final JsonObject containerObject = new JsonObject();
//            json.putString(TYPE_KEY, value.$type().name());
            return json;
        } else if (i.equals(Type.RANGE)) {
            final JsonObject rangeObject = new JsonObject();
            rangeObject.putString(TYPE_KEY, value.$type().name());
            final Range range = value.toJavaObject();
            rangeObject.putValue(LOWERBOUND_KEY, toJson(range.lowerEndpoint()));
            rangeObject.putValue(UPPERBOUND_KEY, toJson(range.upperEndpoint()));
            return rangeObject;
        } else if (i.equals(Type.ANY)) {
            return null;
        } else {
            throw new DollarException("Unrecognized type " + value.$type());
        }
    }

    /**
     * From range.
     *
     * @param from the from
     * @param to   the to
     *
     * @return the var
     */
    @NotNull public static var fromRange(var from,
                                var to) {
        return wrap(new DollarRange(ImmutableList.of(), from, to));}

    @SafeVarargs @NotNull public static var infinity(boolean positive, ImmutableList<Throwable>... errors) {
        return wrap(new DollarInfinity(ImmutableList.copyOf(errors), positive));
    }

    @SafeVarargs @NotNull public static var newNull(Type type, ImmutableList<Throwable>... errors) {
        return new DollarNull(ImmutableList.copyOf(errors), type);
    }
}
