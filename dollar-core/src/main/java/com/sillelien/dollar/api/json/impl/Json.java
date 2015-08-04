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

package com.sillelien.dollar.api.json.impl;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sillelien.dollar.api.json.DecodeException;
import com.sillelien.dollar.api.json.EncodeException;
import com.sillelien.dollar.api.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Json {

    private final static ObjectMapper mapper = new ObjectMapper();
    private final static ObjectMapper prettyMapper = new ObjectMapper();

    static {
        // Non-standard JSON but we allow C style comments in our JSON
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    public static String encode(Object obj) throws EncodeException {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
        }
    }

    public static String encodePrettily(Object obj) throws EncodeException {
        try {
            return prettyMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
        }
    }

    @NotNull @SuppressWarnings("unchecked")
    public static <T> T decodeValue(String str, Class<?> clazz) throws DecodeException {
        try {
            return (T) mapper.readValue(str, clazz);
        } catch (Exception e) {
            throw new DecodeException("Failed to decode:" + e.getMessage());
        }
    }

    @NotNull public static JsonObject fromJavaObject(Object o) {

        return new JsonObject(mapper.convertValue(o, Map.class));

    }

    static {
        prettyMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

}
