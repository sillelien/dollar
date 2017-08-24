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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class ImmutableJsonObject {

    private final @NotNull
    JsonObject json;

    public ImmutableJsonObject(@NotNull JsonObject json) {
        this.json = json;
    }

    @NotNull
    public JsonArray asArray() {
        return json.asArray().copy();
    }

    @NotNull
    public JsonObject asObject() {
        return json.copy().asObject();
    }

    public boolean containsField(@NotNull String fieldName) {
        return json.containsField(fieldName);
    }

    @NotNull
    public String encode() {
        return json.encode();
    }

    @NotNull
    public String encodePrettily() {
        return json.encodePrettily();
    }

    @NotNull
    public JsonArray getArray(@NotNull String fieldName, @NotNull JsonArray def) {
        return json.getArray(fieldName, def);
    }

    @Nullable
    public JsonArray getArray(@NotNull String fieldName) {
        return json.getArray(fieldName);
    }

    @Nullable
    public byte[] getBinary(@NotNull String fieldName) {
        return json.getBinary(fieldName);
    }

    @NotNull
    public byte[] getBinary(@NotNull String fieldName, @NotNull byte[] def) {
        return json.getBinary(fieldName, def);
    }

    @NotNull
    public Boolean getBoolean(@NotNull String fieldName) {
        return json.getBoolean(fieldName);
    }

    public boolean getBoolean(@NotNull String fieldName, boolean def) {
        return json.getBoolean(fieldName, def);
    }

    @Nullable
    public JsonElement getElement(@NotNull String fieldName) {
        return json.getElement(fieldName);
    }

    @NotNull
    public JsonElement getElement(@NotNull String fieldName, @NotNull JsonElement def) {
        return json.getElement(fieldName, def);
    }

    @NotNull
    public <T> T getField(@NotNull String fieldName) {
        return json.getField(fieldName);
    }

    @NotNull
    public Set<String> getFieldNames() {
        return json.getFieldNames();
    }

    @Nullable
    public Integer getInteger(@NotNull String fieldName) {
        return json.getInteger(fieldName);
    }

    @NotNull
    public Integer getInteger(@NotNull String fieldName, int def) {
        return json.getInteger(fieldName, def);
    }

    @Nullable
    public Long getLong(@NotNull String fieldName) {
        return json.getLong(fieldName);
    }

    @NotNull
    public Long getLong(@NotNull String fieldName, long def) {
        return json.getLong(fieldName, def);
    }

    @NotNull
    public Number getNumber(@NotNull String fieldName, int def) {
        return json.getNumber(fieldName, def);
    }

    @NotNull
    public Number getNumber(@NotNull String fieldName) {
        return json.getNumber(fieldName);
    }

    @NotNull
    public JsonObject getObject(@NotNull String fieldName, @NotNull JsonObject def) {
        return json.getObject(fieldName, def);
    }

    @Nullable
    public JsonObject getObject(@NotNull String fieldName) {
        return json.getObject(fieldName);
    }

    @NotNull
    public String getString(@NotNull String fieldName) {
        return json.getString(fieldName);
    }

    @NotNull
    public String getString(@NotNull String fieldName, @NotNull String def) {
        return json.getString(fieldName, def);
    }

    @NotNull
    public <T> T getValue(@NotNull String fieldName) {
        return json.getValue(fieldName);
    }

    @Override
    public int hashCode() {
        return json.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JsonObject) {
            return json.equals(obj);
        }
        return (obj instanceof ImmutableJsonObject) && json.equals(((ImmutableJsonObject) obj).json);
    }

    @NotNull
    @Override
    public String toString() {
        return json.toString();
    }

    public boolean isArray() {
        return json.isArray();
    }

    public boolean isObject() {
        return json.isObject();
    }

    @NotNull
    public JsonObject mutable() {
        return json.copy();
    }

    @NotNull
    public JsonObject putNumber(@NotNull String fieldName, @NotNull Number value) {
        return json.putNumber(fieldName, value);
    }

    public int size() {
        return json.size();
    }

    @NotNull
    public Map<String, Object> toMap() {
        return json.toMap();
    }
}
