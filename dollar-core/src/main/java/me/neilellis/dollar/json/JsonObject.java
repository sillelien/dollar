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

package me.neilellis.dollar.json;


import me.neilellis.dollar.DollarException;
import me.neilellis.dollar.json.impl.Json;

import java.util.*;

/**
 * Represents a JSON object.<p> Instances of this class are not thread-safe.<p>
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@SuppressWarnings("SuspiciousMethodCalls") public class JsonObject extends JsonElement {

  final Map<String, Object> map;

  /**
   * Create a JSON object based on the specified Map
   */
  public JsonObject(Map<String, Object> map) {
    this(map, true);
  }

  JsonObject(Map<String, Object> map, boolean copy) {
    this.map = copy ? convertMap(map) : map;
  }

  /**
   * Create an empty JSON object
   */
  public JsonObject() {
    this.map = new LinkedHashMap<>();
  }


  /**
   * Create a JSON object from a string form of a JSON object
   *
   * @param jsonString The string form of a JSON object
   */
  public JsonObject(String jsonString) {
    map = Json.decodeValue(jsonString, Map.class);
  }

  /**
   * The containsField() method returns a boolean indicating whether the object has the specified property.
   *
   * @param fieldName to lookup
   *
   * @return true if property exist (null value is also considered to exist).
   */
  public boolean containsField(String fieldName) {
    return map.containsKey(fieldName);
  }

  /**
   * @return a copy of this JsonObject such that changes in the original are not reflected in the copy, and vice versa
   */
  public JsonObject copy() {
    return new JsonObject(map, true);
  }

  public String encodePrettily() {
    return Json.encodePrettily(this.map);
  }

  public Object get(Object key) {
    return map.get(key);
  }

  public JsonArray getArray(String fieldName, JsonArray def) {
    JsonArray arr = getArray(fieldName);
    return arr == null ? def : arr;
  }

  @SuppressWarnings("unchecked")
  public JsonArray getArray(String fieldName) {
    List<Object> l = (List<Object>) map.get(fieldName);
    return l == null ? null : new JsonArray(l, false);
  }

  public byte[] getBinary(String fieldName, byte[] def) {
    byte[] b = getBinary(fieldName);
    return b == null ? def : b;
  }

  public byte[] getBinary(String fieldName) {
    String encoded = (String) map.get(fieldName);
    return encoded == null ? null : Base64.getDecoder().decode(encoded);
  }

  public boolean getBoolean(String fieldName, boolean def) {
    Boolean b = getBoolean(fieldName);
    return b == null ? def : b;
  }

  public Boolean getBoolean(String fieldName) {
    return (Boolean) map.get(fieldName);
  }

  public JsonElement getElement(String fieldName, JsonElement def) {
    JsonElement elem = getElement(fieldName);
    return elem == null ? def : elem;
  }

  public JsonElement getElement(String fieldName) {
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

  @SuppressWarnings("unchecked")
  public JsonObject getObject(String fieldName) {
    Map<String, Object> m = (Map<String, Object>) map.get(fieldName);
    return m == null ? null : new JsonObject(m, false);
  }

  public Set<String> getFieldNames() {
    return map.keySet();
  }

  public Integer getInteger(String fieldName) {
    Number num = (Number) map.get(fieldName);
    return num == null ? null : num.intValue();
  }

  public Integer getInteger(String fieldName, int def) {
    Number num = (Number) map.get(fieldName);
    return num == null ? def : num.intValue();
  }

  public Long getLong(String fieldName) {
    Number num = (Number) map.get(fieldName);
    return num == null ? null : num.longValue();
  }

  public Long getLong(String fieldName, long def) {
    Number num = (Number) map.get(fieldName);
    return num == null ? def : num.longValue();
  }

  public Number getNumber(String fieldName, int def) {
    Number n = getNumber(fieldName);
    return n == null ? def : n;
  }

  public Number getNumber(String fieldName) {
    return (Number) map.get(fieldName);
  }

  public JsonObject getObject(String fieldName, JsonObject def) {
    JsonObject obj = getObject(fieldName);
    return obj == null ? def : obj;
  }

  public String getString(String fieldName, String def) {
    String str = getString(fieldName);
    return str == null ? def : str;
  }

  public String getString(String fieldName) {
    return (String) map.get(fieldName);
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(String fieldName) {
    return getField(fieldName);
  }

  @SuppressWarnings("unchecked")
  public <T> T getField(String fieldName) {
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
  public boolean equals(Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }
    JsonObject that = (JsonObject) o;
    return map.equals(that.map);
  }


  @Override
  public String toString() {
    return encode();
  }

  public String encode() {
    return Json.encode(this.map);
  }

  public JsonObject mergeIn(JsonObject other) {
    map.putAll(other.map);
    return this;
  }

  public Object put(String key, Object value) {
    return map.put(key, value);
  }

  public JsonObject putElement(String fieldName, JsonElement value) {
    if (value == null) {
      map.put(fieldName, null);
      return this;
    } else if (value.isArray()) {
      return putArray(fieldName, value.asArray());
    } else {
      return putObject(fieldName, value.asObject());
    }
  }

  public JsonObject putArray(String fieldName, JsonArray value) {
    map.put(fieldName, value == null ? null : value.list);
    return this;
  }

  JsonObject putObject(String fieldName, JsonObject value) {
    map.put(fieldName, value == null ? null : value.map);
    return this;
  }

  public JsonObject putValue(String fieldName, Object value) {
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
      throw new DollarException("Cannot put objects of class " + value.getClass() + " in JsonObject");
    }
    return this;
  }

  public JsonObject putString(String fieldName, String value) {
    map.put(fieldName, value);
    return this;
  }

  public JsonObject putNumber(String fieldName, Number value) {
    map.put(fieldName, value);
    return this;
  }

  JsonObject putBoolean(String fieldName, Boolean value) {
    map.put(fieldName, value);
    return this;
  }

  JsonObject putBinary(String fieldName, byte[] binary) {
    map.put(fieldName, binary == null ? null : Base64.getEncoder().encodeToString(binary));
    return this;
  }

  public Object removeField(String fieldName) {
    return map.remove(fieldName);
  }

  public int size() {
    return map.size();
  }

  /**
   * @return the underlying Map for this JsonObject
   */
  public Map<String, Object> toMap() {
    return convertMap(map);
  }

}
