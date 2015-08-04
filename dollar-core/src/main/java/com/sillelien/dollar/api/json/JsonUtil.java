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

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class JsonUtil {
    @NotNull public static JsonObject argsToJson(@NotNull List<String> args) {
        JsonObject json = new JsonObject();
        if (!args.isEmpty()) {
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
                    Object argConverted = convert(arg);
                    if (value instanceof String && ((String) value).isEmpty()) {
                        value = argConverted;
                    } else if (value instanceof JsonArray) {
                        ((JsonArray) value).add(argConverted);
                    } else {
                        value = new JsonArray(Arrays.asList(value, argConverted));
                    }
                }
            }
            if (value instanceof String && ((String) value).isEmpty()) {
                value = true;
            }
            json.putValue(key, value);
        }
        return json;
    }

    private static Object convert(@NotNull String arg) {
        Object argConverted = arg;
        if (arg.equals("true") || arg.equals("false")) {
            argConverted = Boolean.valueOf(arg);
        } else if (arg.matches("^\\d+$")) {
            argConverted = Long.valueOf(arg);
        } else if (arg.matches("^[0-9]+(|.\\d*[0-9])+$")) {
            argConverted = Double.valueOf(arg);
        }
        return argConverted;
    }
}
