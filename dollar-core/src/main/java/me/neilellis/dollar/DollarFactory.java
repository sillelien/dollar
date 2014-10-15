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
import org.vertx.java.core.json.JsonObject;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarFactory {
    static Monitor monitor = DollarStatic.monitor();
    static StateTracer tracer= DollarStatic.tracer();

    public static var fromField(Object field) {
        if (field == null) {
            return new DollarWrapper(DollarNull.INSTANCE, monitor, tracer);
        }
        if (field instanceof String) {
            return new DollarWrapper(new DollarString((String) field), monitor, tracer);
        }
        if (field instanceof Number) {
            return new DollarWrapper(new DollarNumber((Number) field), monitor, tracer);
        }
        if (field instanceof JsonObject) {
            return DollarStatic.$(field);
        }
        return new DollarWrapper(DollarStatic.$(field.toString()), monitor, tracer);
    }

    public static var fromValue() {
        return new DollarWrapper(new DollarJson(), monitor, tracer);
    }

    public static var fromValue(JsonObject json) {
        if (json == null) {
            return new DollarWrapper(DollarNull.INSTANCE, monitor, tracer);
        }
        return new DollarWrapper(new DollarJson(json), monitor, tracer);
    }

    public static var fromValue(String json) {
        if (json == null) {
            return new DollarWrapper(DollarNull.INSTANCE, monitor, tracer);
        }
        return DollarStatic.$(json);
    }

    public static var fromValue(Range range) {
        if (range == null) {
            return new DollarWrapper(DollarNull.INSTANCE, monitor, tracer);
        }
        return DollarStatic.$(range);
    }


    public static var fromValue(Object o) {
        if (o == null) {
            return new DollarWrapper(DollarNull.INSTANCE, monitor, tracer);
        }
        return DollarStatic.$(o);
    }
}