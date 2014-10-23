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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import me.neilellis.dollar.*;
import me.neilellis.dollar.json.DecodeException;
import me.neilellis.dollar.json.ImmutableJsonObject;
import me.neilellis.dollar.json.JsonArray;
import me.neilellis.dollar.json.JsonObject;
import me.neilellis.dollar.monitor.DollarMonitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import spark.QueryParamsMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    public static var fromValue(@NotNull ImmutableList<Throwable> errors, Object o) {
        return create(errors, o);
    }

    @NotNull
    public static var fromValue() {
        return create(ImmutableList.of(), new JsonObject());
    }


    @NotNull
    static var create(@NotNull ImmutableList<Throwable> errors, @Nullable Object o) {
        if (o == null) {
            return wrap(new DollarVoid(errors));
        }
        if (o instanceof var) {
            return ((var) o).$copy();
        }
        if (o instanceof Pipeable) {
            return wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                    DollarStatic.class.getClassLoader(),
                    new Class<?>[]{var.class},
                    new DollarLambda(DollarStatic.$void(), (Pipeable) o)));
        }
        if (o instanceof JsonArray) {
            return wrap(new DollarList(errors, (JsonArray) o));
        }
        if (o instanceof JsonObject) {
            return wrap(new DollarMap(errors, new ImmutableJsonObject((JsonObject) o)));
        }
        if (o instanceof Map) {
            return wrap(new DollarMap(errors, (Map<String, Object>) o));
        }
        if (o instanceof QueryParamsMap) {
            return create(errors, DollarStatic.paramMapToJson(((QueryParamsMap) o).toMap()));
        }

        if (o instanceof List) {
            return wrap(new DollarList(errors, (List<var>) o));
        }
        if (o.getClass().isArray()) {
            return wrap(new DollarList(errors, (Object[]) o));
        }
        if (o instanceof Number) {
            return wrap(new DollarNumber(errors, (Number) o));
        }
        if (o instanceof Range) {
            return wrap(new DollarRange(errors, (Range) o));
        }
        if (o instanceof ImmutableJsonObject) {
            return wrap(new DollarMap(errors, (ImmutableJsonObject) o));
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
//        if (o instanceof JsonObject) {
//            json = ((JsonObject) o);
//        }
        if (o instanceof JsonObject) {
            json = ((JsonObject) o);
        } else if (o instanceof Multimap) {
            json = DollarStatic.mapToJson((Multimap) o);
        } else if (o instanceof Map) {
            json = new JsonObject((Map<String, Object>) o);
//    } else if (o instanceof Message) {
//      json = ((JsonObject) ((Message) o).body());
//      if (json == null) {
//        return wrap(new DollarVoid(errors));
//      }
        } else {
            System.out.println(o.getClass());
            json = new JsonObject(o.toString());
        }
        return wrap(new DollarMap(errors, json));
    }


    @NotNull
    public static var fromValue(Object o) {
        return fromValue(ImmutableList.of(), o);
    }

    @NotNull
    public static var failure(DollarFail.FailureType failureType) {
        return wrap(new DollarFail(failureType));
    }

    @NotNull
    public static var wrap(var value) {
        return wrap(value, DollarStatic.monitor(), DollarStatic.tracer(), DollarStatic.errorLogger());
    }

    @NotNull
    public static var wrap(var value, DollarMonitor monitor, StateTracer tracer, ErrorLogger errorLogger) {
        return (var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarGuard(new DollarWrapper(value, monitor, tracer, errorLogger)));

    }

    public static var failure(Throwable throwable) {
        return wrap(new DollarFail(Collections.singletonList(throwable), DollarFail.FailureType.EXCEPTION));
    }

    public static var newVoid() {
        return wrap(new DollarVoid());
    }

    public static var fromStringValue(String body) {
        return create(ImmutableList.<Throwable>of(), body);
    }
}
