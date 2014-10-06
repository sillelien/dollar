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

package com.cazcade.dollar;

import com.cazcade.dollar.monitor.Monitor;
import org.vertx.java.core.json.JsonObject;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarFactory {
    static Monitor monitor = DollarStatic.monitor();

    public static var fromField(Object field) {
        if (field == null) {
            return new DollarMonitored(DollarNull.INSTANCE, monitor);
        }
        if (field instanceof String) {
            return new DollarMonitored(new DollarString((String) field), monitor);
        }
        if (field instanceof Number) {
            return new DollarMonitored(new DollarNumber((Number) field), monitor);
        }
        if (field instanceof JsonObject) {
            return DollarStatic.$(field);
        }
        return new DollarMonitored(DollarStatic.$(field.toString()), monitor);
    }

    public static var fromValue() {
        return new DollarMonitored(new DollarJson(), monitor);
    }

    public static var fromValue(JsonObject json) {
        if (json == null) {
            return new DollarMonitored(DollarNull.INSTANCE, monitor);
        }
        return new DollarMonitored(new DollarJson(json), monitor);
    }

    public static var fromValue(String json) {
        if (json == null) {
            return new DollarMonitored(DollarNull.INSTANCE, monitor);
        }
        return DollarStatic.$(json);
    }

    public static var fromValue(Object o) {
        if (o == null) {
            return new DollarMonitored(DollarNull.INSTANCE, monitor);
        }
        return DollarStatic.$(o);
    }
}
