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

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class JsonUtil {
  public static JsonObject argsToJson(List<String> args) {
    JsonObject json = new JsonObject();
    String key = "";
    Object value = "";
    for (String arg : args) {
      if (arg.startsWith("-")) {
        if (!key.isEmpty()) {
          if (value instanceof String && ((String) value).isEmpty()) {
            value = true;
          }
          json.putValue(key, value);
        }
        key = arg.replaceAll("^\\-+", "");
        value = "";
      } else {
        Object argConverted = arg;
        if (arg.equals("true") || arg.equals("false")) {
          argConverted = Boolean.valueOf(arg);
        } else if (arg.matches("^\\d+$")) {
          argConverted = Long.valueOf(arg);
        } else if (arg.matches("^[0-9]+(|.\\d*[0-9])+$")) {
          argConverted = Double.valueOf(arg);
        }
        if (value instanceof String && ((String) value).isEmpty()) {
          value = argConverted;
        } else if (value instanceof JsonArray) {
          ((JsonArray) value).addString(argConverted.toString());
        } else {
          value = new JsonArray(Arrays.asList(value, argConverted));
        }
      }
    }
    if (value instanceof String && ((String) value).isEmpty()) {
      value = true;
    }
    json.put(key, value);
    return json;
  }
}
