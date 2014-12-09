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

package me.neilellis.dollar.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import me.neilellis.dollar.*;
import me.neilellis.dollar.exceptions.DollarFailureException;
import me.neilellis.dollar.json.DecodeException;
import me.neilellis.dollar.json.ImmutableJsonObject;
import me.neilellis.dollar.json.JsonArray;
import me.neilellis.dollar.json.JsonObject;
import me.neilellis.dollar.json.impl.Json;
import me.neilellis.dollar.monitor.DollarMonitor;
import me.neilellis.dollar.script.SourceAware;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import spark.QueryParamsMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarFactory {
    static DollarMonitor monitor = DollarStatic.monitor();
    @NotNull
    static StateTracer tracer = DollarStatic.tracer();

    @NotNull
    public static var fromField(@NotNull ImmutableList<Throwable> errors, Object field) {
//            return new DollarWrapper(DollarNull.INSTANCE, monitor, tracer);
//        }
//        if (field instanceof String) {
//            return new DollarWrapper(new DollarString((String) field), monitor, tracer);
//        }
//        if (field instanceof Number) {
//            return new DollarWrapper(new DollarNumber((Number) field), monitor, tracer);
//        }
//        if (field instanceof JsonObject) {
//            return DollarStatic.$(field);
//        }
//        return new DollarWrapper(DollarStatic.$(field.toString()), monitor, tracer);
        return create(errors, field);
    }


    @NotNull
    public static var fromValue(Object o, @NotNull ImmutableList<Throwable>... errors) {
        ImmutableList.Builder<Throwable> builder = ImmutableList.builder();
        for (ImmutableList<Throwable> error : errors) {
            builder = builder.addAll(error);
        }
        return create(builder.build(), o);
    }

    @NotNull
    public static var fromValue() {
        return create(ImmutableList.of(), new JsonObject());
    }


    @NotNull
    private static var create(@NotNull ImmutableList<Throwable> errors, @Nullable Object o) {
        if (o == null) {
            return wrap(new DollarVoid(errors));
        }
        if (o instanceof var) {
            return (var) o;
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
            return wrap(new DollarMap(errors, (Map<String, Object>) o));
        }
        if (o instanceof QueryParamsMap) {
            return create(errors, DollarStatic.paramMapToJson(((QueryParamsMap) o).toMap()));
        }

        if (o instanceof List) {
            return wrap(new DollarList(errors, (List<Object>) o));
        }
        if (o.getClass().isArray()) {
            return wrap(new DollarList(errors, (Object[]) o));
        }
        if (o instanceof Boolean) {
            return wrap(new DollarBoolean(errors, (Boolean) o));
        }
        if (o instanceof Date) {
            return wrap(new DollarDate(errors, ((Date) o).getTime()));
        }
        if (o instanceof LocalDateTime) {
            return wrap(new DollarDate(errors, (LocalDateTime) o));
        }
        if (o instanceof Double) {
            return wrap(new DollarDecimal(errors, (Double) o));
        }
        if (o instanceof Float) {
            return wrap(new DollarDecimal(errors, ((Float) o).doubleValue()));
        }
        if (o instanceof Long) {
            return wrap(new DollarInteger(errors, (Long) o));
        }
        if (o instanceof Integer) {
            return wrap(new DollarInteger(errors, ((Integer) o).longValue()));
        }
        if (o instanceof Short) {
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
                return create(errors, CharStreams.toString(new InputStreamReader((InputStream) o)));
            } catch (IOException e) {
                return failure(e);
            }
        }
        if (o instanceof String) {
            if (((String) o).matches("^[a-zA-Z0-9]+$")) {
                return wrap(new DollarString(errors, (String) o));
            } else {
                try {
                    return wrap(new DollarMap(errors, new JsonObject((String) o)));
                } catch (DecodeException de) {
                    return wrap(new DollarString(errors, (String) o));
                }
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


    @NotNull
    public static var fromGsonObject(Object o) {
        Gson gson = new Gson();
        String json = gson.toJson(o);
        return create(ImmutableList.<Throwable>of(), json);
    }

    @NotNull
    public static var fromValue(Object o) {
        return fromValue(o, ImmutableList.of());
    }

    @NotNull
    public static var failure(FailureType failureType) {
        if (DollarStatic.config.failFast()) {
            throw new DollarFailureException(failureType);
        } else {
            return wrap(new DollarFail(failureType));
        }
    }

    @NotNull
    public static var wrap(var value) {
        return wrap(value, DollarStatic.monitor(), DollarStatic.tracer(), DollarStatic.errorLogger());
    }

    @NotNull
    private static var wrap(var value, DollarMonitor monitor, StateTracer tracer, ErrorLogger errorLogger) {
        final var val;
        if (DollarStatic.config.wrapForMonitoring()) {
            val = new DollarWrapper(value, monitor, tracer, errorLogger);
        } else {
            val = value;
        }
        if (DollarStatic.config.wrapForGuards()) {
            return (var) java.lang.reflect.Proxy.newProxyInstance(
                    DollarStatic.class.getClassLoader(),
                    new Class<?>[]{var.class},
                    new DollarGuard(val));
        } else {
            return val;
        }


    }

    @NotNull
    public static var failure(FailureType failureType, Throwable t) {
        if (DollarStatic.config.failFast()) {
            throw new DollarFailureException(t, failureType);
        } else {
            return wrap(new DollarFail(Arrays.asList(t), failureType));
        }
    }

    public static var failure(FailureType failureType, String message) {
        if (DollarStatic.config.failFast()) {
            throw new DollarFailureException(failureType, message);
        } else {
            return wrap(new DollarFail(Arrays.asList(new DollarException(message)), failureType));
        }
    }

    public static var failure(Throwable throwable) {
        return failure(FailureType.EXCEPTION, throwable);
    }

    public static var newVoid() {
        return wrap(new DollarVoid());
    }

    public static var fromStringValue(String body) {
        return create(ImmutableList.<Throwable>of(), body);
    }

    public static var fromLambda(Pipeable pipeable) {
        return fromValue(pipeable);
    }

    public static var fromURI(var from) {
        if (from.isUri()) {
            return from;
        } else {
            return fromURI(from.$S());
        }
    }

    public static var fromURI(String uri) {
        try {
            return wrap(new DollarURI(ImmutableList.of(), uri));
        } catch (Exception e) {
            return DollarStatic.handleError(e, null);
        }
    }

    public static var fromStream(SerializedType type, InputStream rawBody) throws IOException {
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

    public static var fromFuture(Future<var> future) {
        return wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarLambda(i -> future.get(), false)));
    }

    public static var failureWithSource(FailureType failureType, Throwable throwable, SourceAware source) {
        if (source == null) {
            throw new NullPointerException();
        }
        if (DollarStatic.config.failFast()) {
            final DollarFailureException dollarFailureException = new DollarFailureException(throwable, failureType);
            dollarFailureException.addSource(source);
            throw dollarFailureException;
        } else {
            return wrap(new DollarFail(Arrays.asList(throwable), failureType));
        }
    }

    public static var blockCollection(List<var> var) {
        return wrap(new DollarBlockCollection(var));
    }
}
