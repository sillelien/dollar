/*
 * Copyright (c) 2014-2014 Cazcade Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.neilellis.dollar;

import com.google.common.collect.Range;
import me.neilellis.dollar.monitor.Monitor;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonObject;
import spark.QueryParamsMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarFactory {
    static Monitor monitor = DollarStatic.monitor();
    static StateTracer tracer= DollarStatic.tracer();
    public static var fromField(List<Throwable> errors, Object field) {
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
        return create(errors,field);
    }




    public static var fromValue(List<Throwable> errors,Object o) {
        return create(errors,o);
    }

    public static var fromValue() {
        return create(Collections.emptyList(),new JsonObject());
    }


    static var create(List<Throwable> errors,Object o) {
        if (o == null) {
                return new DollarWrapper(new DollarNull(errors), DollarStatic.monitor(), DollarStatic.tracer(), DollarStatic.errorLogger());
            }
        if(o instanceof QueryParamsMap) {
            return create(errors,DollarStatic.paramMapToJson(((QueryParamsMap) o).toMap()));
        }
        if( o instanceof  var) {
            return ((var) o).copy(errors);
        }
        if(o.getClass().isArray()) {
            return new DollarWrapper(new DollarList(errors,(Object[]) o),DollarStatic.monitor(),DollarStatic.tracer(),  DollarStatic.errorLogger());
        }
        if (o instanceof Number) {
            return new DollarWrapper(new DollarNumber(errors,(Number) o), DollarStatic.monitor(), DollarStatic.tracer(),  DollarStatic.errorLogger());
        }
        if (o instanceof Range) {
            return new DollarWrapper(new DollarRange(errors,(Range) o), DollarStatic.monitor(), DollarStatic.tracer(),  DollarStatic.errorLogger());
        }
        if (o instanceof String) {
            try {
                return new DollarWrapper(new DollarJson(errors,new JsonObject((String) o)), DollarStatic.monitor(), DollarStatic.tracer(),  DollarStatic.errorLogger());
            } catch (DecodeException de) {
                return new DollarWrapper(new DollarString(errors,(String) o), DollarStatic.monitor(), DollarStatic.tracer(),  DollarStatic.errorLogger());
            }
        }
        JsonObject json;
        if (o instanceof JsonObject) {
            json = ((JsonObject) o);
        }
        if (o instanceof JsonObject) {
            json = ((JsonObject) o);
        } else if (o instanceof MultiMap) {
            json = DollarStatic.mapToJson((MultiMap) o);
        } else if (o instanceof Map) {
            json = new JsonObject((Map<String, Object>) o);
        } else if (o instanceof Message) {
            json = ((JsonObject) ((Message) o).body());
            if (json == null) {
                return new DollarWrapper(new DollarNull(errors), DollarStatic.monitor(), DollarStatic.tracer(),  DollarStatic.errorLogger());
            }
        } else {
            json = new JsonObject(o.toString());
        }
        return new DollarWrapper(new DollarJson(errors,json), DollarStatic.monitor(), DollarStatic.tracer(),  DollarStatic.errorLogger());
    }

    private static var innerCreate(List<Throwable> errors, Object o) {
        if (o == null) {
                return new DollarWrapper(new DollarNull(errors), DollarStatic.monitor(), DollarStatic.tracer(),  DollarStatic.errorLogger());
            }
        if(o.getClass().isArray()) {
            return new DollarWrapper(new DollarList(errors,(Object[])o),DollarStatic.monitor(),DollarStatic.tracer(),  DollarStatic.errorLogger());
        }
        if (o instanceof Number) {
            return new DollarWrapper(new DollarNumber(errors,(Number) o), DollarStatic.monitor(), DollarStatic.tracer(),  DollarStatic.errorLogger());
        }
        if (o instanceof Range) {
            return new DollarWrapper(new DollarRange(errors,(Range) o), DollarStatic.monitor(), DollarStatic.tracer(),  DollarStatic.errorLogger());
        }
        if (o instanceof String) {
            try {
                return new DollarWrapper(new DollarJson(errors,new JsonObject((String) o)), DollarStatic.monitor(), DollarStatic.tracer(),  DollarStatic.errorLogger());
            } catch (DecodeException de) {
                return new DollarWrapper(new DollarString(errors,(String) o), DollarStatic.monitor(), DollarStatic.tracer(),  DollarStatic.errorLogger());
            }
        }
        JsonObject json;
        if (o instanceof JsonObject) {
            json = ((JsonObject) o);
        }
        if (o instanceof JsonObject) {
            json = ((JsonObject) o);
        } else if (o instanceof MultiMap) {
            json = DollarStatic.mapToJson((MultiMap) o);
        } else if (o instanceof Map) {
            json = new JsonObject((Map<String, Object>) o);
        } else if (o instanceof Message) {
            json = ((JsonObject) ((Message) o).body());
            if (json == null) {
                return new DollarWrapper(new DollarNull(errors), DollarStatic.monitor(), DollarStatic.tracer(),  DollarStatic.errorLogger());
            }
        } else {
            json = new JsonObject(o.toString());
        }
        return new DollarWrapper(new DollarJson(errors,json), DollarStatic.monitor(), DollarStatic.tracer(),  DollarStatic.errorLogger());
    }

    public static var fromValue(Object o) {
        return fromValue(Collections.emptyList(),o);
    }
}
