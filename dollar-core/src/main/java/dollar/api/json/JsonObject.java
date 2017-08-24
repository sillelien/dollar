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

package dollar.api.json;


import dollar.api.DollarException;
import dollar.api.json.impl.Json;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("SuspiciousMethodCalls")
public class JsonObject extends JsonElement {

    @NotNull
    final Map<String, Object> map;

    /**
     * Create a JSON object based on the specified Map
     */
    public JsonObject(@NotNull Map<String, Object> map) {
        this(map, true);
    }

    JsonObject(@NotNull Map<String, Object> map, boolean copy) {
        super();
        this.map = copy ? convertMap(map) : map;
    }

    /**
     * Create an empty JSON object
     */
    public JsonObject() {
        super();
        map = new LinkedHashMap<>();
    }


    /**
     * Create a JSON object from a string form of a JSON object
     *
     * @param jsonString The string form of a JSON object
     */
    public JsonObject(@NotNull String jsonString) {
        super();
        map = Json.decodeValue(jsonString, Map.class);
    }

    /**
     * The containsField() method returns a boolean indicating whether the object has the specified property.
     *
     * @param fieldName to lookup
     * @return true if property exist (null value is also considered to exist).
     */
    public boolean containsField(@NotNull String fieldName) {
        return map.containsKey(fieldName);
    }

    /**
     * @return a copy of this JsonObject such that changes in the original are not reflected in the copy, and vice versa
     */
    @NotNull
    public JsonObject copy() {
        return new JsonObject(map, true);
    }

    @NotNull
    public String encodePrettily() {
        return Json.encodePrettily(map);
    }

    @Nullable
    public Object get(@NotNull Object key) {
        return map.get(key);
    }

    @NotNull
    public JsonArray getArray(@NotNull String fieldName, @NotNull JsonArray def) {
        JsonArray arr = getArray(fieldName);
        return (arr == null) ? def : arr;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public JsonArray getArray(@NotNull String fieldName) {
        List<Object> l = (List<Object>) map.get(fieldName);
        return (l == null) ? null : new JsonArray(l, false);
    }

    @NotNull
    public byte[] getBinary(@NotNull String fieldName, @NotNull byte[] def) {
        byte[] b = getBinary(fieldName);
        return (b == null) ? def : b;
    }

    @Nullable
    public byte[] getBinary(@NotNull String fieldName) {
        String encoded = (String) map.get(fieldName);
        return (encoded == null) ? null : Base64.getDecoder().decode(encoded);
    }

    public boolean getBoolean(@NotNull String fieldName, boolean def) {
        Boolean b = getBoolean(fieldName);
        return (b == null) ? def : b;
    }

    @NotNull
    public Boolean getBoolean(@NotNull String fieldName) {
        return (Boolean) map.get(fieldName);
    }

    @NotNull
    public JsonElement getElement(@NotNull String fieldName, @NotNull JsonElement def) {
        JsonElement elem = getElement(fieldName);
        return (elem == null) ? def : elem;
    }

    @Nullable
    public JsonElement getElement(@NotNull String fieldName) {
        Object element = map.get(fieldName);
        if (element == null) { return null; }

        if (element instanceof Map<?, ?>) {
            return getObject(fieldName);
        }
        if (element instanceof List<?>) {
            return getArray(fieldName);
        }
        throw new ClassCastException();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public JsonObject getObject(@NotNull String fieldName) {
        Map<String, Object> m = (Map<String, Object>) map.get(fieldName);
        return (m == null) ? null : new JsonObject(m, false);
    }

    @NotNull
    public Set<String> getFieldNames() {
        return map.keySet();
    }

    @Nullable
    public Integer getInteger(@NotNull String fieldName) {
        Number num = (Number) map.get(fieldName);
        return (num == null) ? null : num.intValue();
    }

    @NotNull
    public Integer getInteger(@NotNull String fieldName, int def) {
        Number num = (Number) map.get(fieldName);
        return (num == null) ? def : num.intValue();
    }

    @Nullable
    public Long getLong(@NotNull String fieldName) {
        Number num = (Number) map.get(fieldName);
        return (num == null) ? null : num.longValue();
    }

    @NotNull
    public Long getLong(@NotNull String fieldName, long def) {
        Number num = (Number) map.get(fieldName);
        return (num == null) ? def : num.longValue();
    }

    @NotNull
    public Number getNumber(@NotNull String fieldName, int def) {
        Number n = getNumber(fieldName);
        return (n == null) ? def : n;
    }

    @NotNull
    public Number getNumber(@NotNull String fieldName) {
        return (Number) map.get(fieldName);
    }

    @NotNull
    public JsonObject getObject(@NotNull String fieldName, @NotNull JsonObject def) {
        JsonObject obj = getObject(fieldName);
        return (obj == null) ? def : obj;
    }

    @NotNull
    public String getString(@NotNull String fieldName, @NotNull String def) {
        String str = getString(fieldName);
        return (str == null) ? def : str;
    }

    @NotNull
    public String getString(@NotNull String fieldName) {
        return (String) map.get(fieldName);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <T> T getValue(@NotNull String fieldName) {
        return getField(fieldName);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public <T> T getField(@NotNull String fieldName) {
        Object obj = map.get(fieldName);
        if (obj instanceof Map) {
            obj = new JsonObject((Map) obj, false);
        } else if (obj instanceof List) {
            obj = new JsonArray((List) obj, false);
        }
        return (T) obj;
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) { return true; }
        if ((o == null) || (getClass() != o.getClass())) { return false; }
        JsonObject that = (JsonObject) o;
        return map.equals(that.map);
    }


    @NotNull
    @Override
    public String toString() {
        return encode();
    }

    @NotNull
    public String encode() {
        return Json.encode(map);
    }

    @NotNull
    public JsonObject mergeIn(@NotNull JsonObject other) {
        map.putAll(other.map);
        return this;
    }

    @NotNull
    public Object put(@NotNull String key, @NotNull Object value) {
        return map.put(key, value);
    }

    @NotNull
    public JsonObject putElement(@NotNull String fieldName, @Nullable JsonElement value) {
        if (value == null) {
            map.put(fieldName, null);
            return this;
        } else if (value.isArray()) {
            return putArray(fieldName, value.asArray());
        } else {
            return putObject(fieldName, value.asObject());
        }
    }

    @NotNull
    public JsonObject putArray(@NotNull String fieldName, @Nullable JsonArray value) {
        map.put(fieldName, (value == null) ? null : value.list);
        return this;
    }

    @NotNull
    JsonObject putObject(@NotNull String fieldName, @Nullable JsonObject value) {
        map.put(fieldName, (value == null) ? null : value.map);
        return this;
    }

    @NotNull
    public JsonObject putValue(@NotNull String fieldName, @Nullable Object value) {
        if (value == null) {
            putObject(fieldName, null);
        } else if (value instanceof JsonObject) {
            putObject(fieldName, (JsonObject) value);
        } else if (value instanceof JsonArray) {
            putArray(fieldName, (JsonArray) value);
        } else if (value instanceof String) {
            putString(fieldName, (String) value);
        } else if (value instanceof Number) {
            putNumber(fieldName, (Number) value);
        } else if (value instanceof Boolean) {
            putBoolean(fieldName, (Boolean) value);
        } else if (value instanceof byte[]) {
            putBinary(fieldName, (byte[]) value);
        } else {
            throw new DollarException("Cannot putValue objects of class " + value.getClass() + " in JsonObject");
        }
        return this;
    }

    @NotNull
    public JsonObject putString(@NotNull String fieldName, @NotNull String value) {
        map.put(fieldName, value);
        return this;
    }

    @NotNull
    public JsonObject putNumber(@NotNull String fieldName, @NotNull Number value) {
        map.put(fieldName, value);
        return this;
    }

    @NotNull
    JsonObject putBoolean(@NotNull String fieldName, @NotNull Boolean value) {
        map.put(fieldName, value);
        return this;
    }

    @NotNull
    JsonObject putBinary(@NotNull String fieldName, @Nullable byte[] binary) {
        map.put(fieldName, (binary == null) ? null : Base64.getEncoder().encodeToString(binary));
        return this;
    }

    @NotNull
    public Object removeField(@NotNull String fieldName) {
        return map.remove(fieldName);
    }

    public int size() {
        return map.size();
    }

    /**
     * @return the underlying Map for this JsonObject
     */
    @NotNull
    public Map<String, Object> toMap() {
        return convertMap(map);
    }

}
