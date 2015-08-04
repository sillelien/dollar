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

package com.sillelien.dollar.api.json;

import com.sillelien.dollar.api.DollarException;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class JsonElement implements Serializable {

    @NotNull public JsonArray asArray() {
        return (JsonArray) this;
    }

    @NotNull public JsonObject asObject() {
        return (JsonObject) this;
    }

    public boolean isArray() {
        return this instanceof JsonArray;
    }

    public boolean isObject() {
        return this instanceof JsonObject;
    }

    @NotNull @SuppressWarnings("unchecked") Map<String, Object> convertMap(@NotNull Map<String, Object> map) {
        Map<String, Object> converted = new LinkedHashMap<>(map.size());
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object obj = entry.getValue();
            if (obj instanceof Map) {
                Map<String, Object> jm = (Map<String, Object>) obj;
                converted.put(entry.getKey(), convertMap(jm));
            } else if (obj instanceof List) {
                List<Object> list = (List<Object>) obj;
                converted.put(entry.getKey(), convertList(list));
            } else if (obj instanceof CharSequence) {
                converted.put(entry.getKey(), obj.toString());
            } else if (obj instanceof JsonArray) {
                converted.put(entry.getKey(), ((JsonArray) obj).toList());
            } else if (obj instanceof JsonObject) {
                converted.put(entry.getKey(), ((JsonObject) obj).toMap());
            } else if (obj instanceof BigDecimal) {
                converted.put(entry.getKey(), ((BigDecimal) obj).doubleValue());
            } else if (obj instanceof BigInteger) {
                converted.put(entry.getKey(), ((BigInteger) obj).longValue());
            } else if (obj == null || obj instanceof Number || obj instanceof Boolean) {
                // OK
                converted.put(entry.getKey(), obj);
            } else {
                throw new DollarException("Cannot have objects of class " + obj.getClass() + " in JSON");
            }
        }
        return converted;
    }

    @NotNull @SuppressWarnings("unchecked") List<Object> convertList(@NotNull List<?> list) {
        List<Object> arr = new ArrayList<>(list.size());
        for (Object obj : list) {
            if (obj instanceof Map) {
                arr.add(convertMap((Map<String, Object>) obj));
            } else if (obj instanceof List) {
                arr.add(convertList((List<?>) obj));
            } else if (obj instanceof CharSequence) {
                arr.add(obj.toString());
            } else if (obj instanceof BigDecimal) {
                arr.add(((BigDecimal) obj).doubleValue());
            } else if (obj instanceof BigInteger) {
                arr.add(((BigInteger) obj).longValue());
            } else if (obj == null || obj instanceof Number || obj instanceof Boolean) {
                arr.add(obj);
            } else {
                throw new DollarException("Cannot have objects of class " + obj.getClass() + " in JSON");
            }
        }
        return arr;
    }

}