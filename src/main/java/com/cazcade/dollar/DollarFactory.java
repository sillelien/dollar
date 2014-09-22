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

import org.vertx.java.core.json.JsonObject;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
class DollarFactory {
    static $ fromField(Object field) {
        if (field == null) {
            return DollarNull.INSTANCE;
        }
        if (field instanceof String) {
            return new DollarString((String) field);
        }
        if (field instanceof Number) {
            return new DollarNumber((Number) field);
        }
        if (field instanceof JsonObject) {
            return new DollarJson(field);
        }
        return new DollarJson(field.toString());
    }

     static $ fromValue() {
        return new DollarJson();
    }

     static $ fromValue(JsonObject json) {
        if (json == null) {
            return DollarNull.INSTANCE;
        }
        return new DollarJson(json);
    }

     static $ fromValue(String json) {
        if (json == null) {
            return DollarNull.INSTANCE;
        }
        return new DollarJson(json);
    }

     static $ fromValue(Object o) {
        if (o == null) {
            return DollarNull.INSTANCE;
        }
        return new DollarJson(o);
    }
}
