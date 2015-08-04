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
import com.sillelien.dollar.api.collections.ImmutableList;
import com.sillelien.dollar.api.collections.ImmutableMap;
import com.sillelien.dollar.api.json.impl.Json;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class JsonArray extends JsonElement implements Iterable<Object> {

  final List list;

  public JsonArray(List list) {
    this(list, true);
  }

  protected JsonArray(List list, boolean copy) {
    this.list = copy ? convertList(list) : list;
  }

  public JsonArray(Object[] array) {
    this(new ArrayList<>(Arrays.asList(array)), true);
  }

  public JsonArray() {
    this.list = new ArrayList<>();
  }

  public JsonArray(String jsonString) {
    list = Json.decodeValue(jsonString, List.class);
  }

    @NotNull public JsonArray add(@Nullable Object value) {
    if (value == null) {
      list.add(null);
    } else if (value instanceof JsonObject) {
      addObject((JsonObject) value);
    } else if (value instanceof JsonArray) {
      addArray((JsonArray) value);
    } else if (value instanceof String) {
      addString((String) value);
    } else if (value instanceof Number) {
      addNumber((Number) value);
    } else if (value instanceof Boolean) {
      addBoolean((Boolean) value);
    } else if (value instanceof byte[]) {
      addBinary((byte[]) value);
    } else if (value instanceof Character) {
      addString(value.toString());
    } else {
      throw new DollarException("Cannot add objects of class " + value.getClass() + " to JsonArray");
    }
    return this;
  }

    @NotNull public JsonArray addObject(@Nullable JsonObject value) {
    list.add(value == null ? null : value.map);
    return this;
  }

    @NotNull JsonArray addArray(@Nullable JsonArray value) {
    list.add(value == null ? null : value.list);
    return this;
  }

    @NotNull JsonArray addString(String str) {
    list.add(str);
    return this;
  }

    @NotNull JsonArray addNumber(Number value) {
    list.add(value);
    return this;
  }

    @NotNull JsonArray addBoolean(Boolean value) {
    list.add(value);
    return this;
  }

    @NotNull JsonArray addBinary(@Nullable byte[] value) {
    String encoded = (value == null) ? null : Base64.getEncoder().encodeToString(value);
    list.add(encoded);
    return this;
  }

    @NotNull public JsonArray addElement(@Nullable JsonElement value) {
    if (value == null) {
      list.add(null);
      return this;
    }
    if (value.isArray()) {
      return addArray(value.asArray());
    }
    return addObject(value.asObject());
  }

  public boolean contains(Object value) {
    return list.contains(value);
  }

  /**
   * @return a copy of the JsonArray
   */
  @NotNull public JsonArray copy() {
    return new JsonArray(list, true);
  }

    @Nullable public <T> T get(final int index) {
    return convertObject(list.get(index));
  }

    @Nullable @SuppressWarnings("unchecked")
    private <T> T convertObject(@Nullable final Object obj) {
    Object retVal = obj;
    if (obj != null) {
      if (obj instanceof List) {
        retVal = new JsonArray((List) obj, false);
      } else if (obj instanceof ImmutableList) {
        retVal = new JsonArray((((ImmutableList) obj).mutable()), false);
      } else if (obj instanceof ImmutableMap) {
        retVal = new JsonObject(((ImmutableMap) obj).mutable(), false);
      } else if (obj instanceof Map) {
        retVal = new JsonObject((Map<String, Object>) obj, false);
      }
    }
    return (T) retVal;
  }

  @Override
  public int hashCode() {
    return list.hashCode();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) { return true; }
    if (o == null || getClass() != o.getClass()) { return false; }
    JsonArray that = (JsonArray) o;
    return list.equals(that.list);
  }

  @Override
  public String toString() {
    return encodePrettily();
  }

  public String encodePrettily() throws EncodeException {
    return Json.encodePrettily(this.list);
  }

  @NotNull @Override
  public Iterator<Object> iterator() {
    return new Iterator<Object>() {

      final Iterator<Object> iter = list.iterator();

      @Override
      public boolean hasNext() {
        return iter.hasNext();
      }

        @Nullable @Override
      public Object next() {
        return convertObject(iter.next());
      }

      @Override
      public void remove() {
        iter.remove();
      }
    };
  }

  public int size() {
    return list.size();
  }

    @NotNull public Object[] toArray() {
    return convertList(list).toArray();
  }

  public List toList() {
    return convertList(list);
  }

    @NotNull public List toStringList() {
    final ArrayList<Object> strings = new ArrayList<>();
    for (Object o : list) {
      strings.add(Json.encode(o));
    }
    return strings;
  }

  String encode() throws EncodeException {
    return Json.encode(this.list);
  }
}
