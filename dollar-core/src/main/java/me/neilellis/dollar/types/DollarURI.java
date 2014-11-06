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

package me.neilellis.dollar.types;

import com.google.common.collect.ImmutableList;
import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.integration.IntegrationProvider;
import me.neilellis.dollar.json.JsonObject;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarURI extends AbstractDollar {

    private String uri;
    private IntegrationProvider integrationProvider = DollarStatic.integrationProvider();

    public DollarURI(@NotNull List<Throwable> errors, String uri) {
        super(errors);
        this.uri = uri;
    }

    @NotNull
    @Override
    public var $(@NotNull String key, long value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $(@NotNull String key, double value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public var $plus(@Nullable Object value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @NotNull
    @Override
    public Stream<var> $children() {
        return Stream.empty();
    }

    @NotNull
    @Override
    public Stream<var> $children(@NotNull String key) {
        return Stream.empty();
    }

    @Override
    public boolean $has(@NotNull String key) {
        return false;
    }

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {
        return ImmutableMap.of();
    }

    @Nullable
    @Override
    public String S(@NotNull String key) {
        return null;
    }

    @NotNull
    @Override
    public var $rm(@NotNull String key) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);

    }

    @NotNull
    @Override
    public var $minus(@NotNull Object value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);

    }

    @NotNull
    @Override
    public var $(@NotNull String key, @Nullable Object value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);

    }

    @NotNull
    @Override
    public var $(@NotNull String key) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);

    }

    @NotNull
    @Override
    public <R> R $() {
        return (R) uri;
    }

    @Override
    public Stream<String> keyStream() {
        return Stream.empty();
    }

    @Override
    public var $(Number n) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public boolean isBoolean() {
        return false;
    }

    @Override
    public boolean isTrue() {
        return false;
    }

    @Override
    public boolean isTruthy() {
        return false;
    }

    @Override
    public boolean isFalse() {
        return false;
    }

    @Override
    public boolean isNeitherTrueNorFalse() {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public var remove(Object key) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);

    }

    @Override
    public var $dec(long amount) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public var $inc(long amount) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public var $negate() {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public var $multiply(var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public var $divide(var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public var $modulus(var v) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public var $abs() {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public var decode() {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_URI_OPERATION);
    }

    @Override
    public String S() {
        return uri;
    }

    @NotNull
    @Override
    public ImmutableList<var> toList() {
        return ImmutableList.of();
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @NotNull
    @Override
    public Integer I() {
        return 0;
    }

    @NotNull
    @Override
    public Integer I(@NotNull String key) {
        return 0;
    }

    @Nullable
    @Override
    public JsonObject json(@NotNull String key) {
        return new JsonObject();
    }

    @Nullable
    @Override
    public Number number(@NotNull String key) {
        return 0;
    }

    @Nullable
    @Override
    public JsonObject json() {
        return new JsonObject();
    }

    @Nullable
    @Override
    public ImmutableList<String> strings() {
        return ImmutableList.of();
    }

    @Nullable
    @Override
    public Map<String, Object> toMap() {
        return Collections.emptyMap();
    }

    @NotNull
    @Override
    public Number N() {
        return 0;
    }

    @Override
    public String $listen(Pipeable pipe) {
        integrationProvider.listen(uri, i -> {
            try {
                pipe.pipe(i);
            } catch (Exception e) {
                DollarStatic.logAndRethrow(e);
            }
        });
        return "no cancellation mechanism yet";
    }

    @Override
    public var $notify(var v) {
        return integrationProvider.dispatch(uri, v);
    }

    @Override
    public String $listen(Pipeable pipe, String key) {
        return $listen(pipe);
    }

    @Override
    public var $receive(var v) {
        return integrationProvider.send(uri, v);
    }

    @Override
    public var $each(Pipeable pipe) throws Exception {
        return super.$each(pipe);
    }

    @Override
    public var $take() {
        return integrationProvider.poll(uri);
    }


    @Override
    public int compareTo(var o) {
        return Comparator.<String>naturalOrder().compare(uri, o.toString());
    }
}
