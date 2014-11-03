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
import me.neilellis.dollar.DollarEval;
import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.json.JsonObject;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * To better understand the rationale behind this class, take a look at http://homepages.ecs.vuw.ac.nz/~tk/publications/papers/void.pdf
 *
 * Dollar does not have the concept of null. Instead null {@link me.neilellis.dollar.var} objects are instances of this class.
 *
 * Void is equivalent to 0,"",null except that unlike these values it has behavior that corresponds to a void object.
 *
 * Therefore actions taken against a void object are ignored. Any method that returns a {@link me.neilellis.dollar.var} will return a {@link DollarVoid}.
 *
 * <pre>
 *
 *  var nulled= $null();
 *  nulled.$pipe((i)-&gt;{System.out.println("You'll never see this."});
 *
 * </pre>
 *
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarVoid extends AbstractDollar implements var {


    public static final var INSTANCE = DollarFactory.newVoid();

    public DollarVoid(@NotNull List<Throwable> errors) {
        super(errors);
    }

    public DollarVoid() {

        super(Collections.emptyList());
    }

    @NotNull
    @Override
    public var $(@NotNull String age, long l) {
        return $copy();
    }

    @NotNull
    @Override
    public var $(@NotNull String age, double d) {
        return $copy();
    }

    @NotNull
    @Override
    public var $plus(Object value) {
        return $copy();
    }

    @NotNull
    @Override
    public Stream<var> $children() {
        return Collections.<var>emptyList().stream();
    }

    @NotNull
    @Override
    public Stream $children(@NotNull String key) {
        return Collections.emptyList().stream();
    }

    @Override
    public var $dec(long amount) {
        return this;
    }

    @Override
    public var $inc(long amount) {
        return this;
    }

    @Override
    public var $multiply(var v) {
        return this;
    }

    @Override
    public var $divide(var v) {
        return this;
    }

    /**
     * If you stare into the void, the void will stare back at you.
     *
     * @param you - you.
     */
    public void stare(var you) {
        this.stare(you);
    }


    @Override
    public var $modulus(var v) {
        return this;
    }

    @Override
    public boolean $has(@NotNull String key) {
        return false;
    }

    @NotNull
    @Override
    public ImmutableList<var> toList() {
        return ImmutableList.of();
    }

    @NotNull
    @Override
    public ImmutableMap<String, var> $map() {
        return ImmutableMap.of();
    }

    @NotNull
    @Override
    public String S(@NotNull String key) {
        return "";
    }

    @NotNull
    @Override
    public var $rm(@NotNull String value) {
        return $copy();
    }

    @NotNull
    @Override
    public var $minus(@NotNull Object value) {
        return this;
    }

    @NotNull
    @Override
    public var $(@NotNull String key, Object value) {
        return $copy();
    }

    @Override
    public boolean isVoid() {
        return true;
    }

    @Override
    public Integer I() {
        return 0;
    }

    @Override
    public Integer I(@NotNull String key) {
        return 0;
    }

    @NotNull
    @Override
    public var decode() {
        return $copy();
    }

    @NotNull
    @Override
    public var $(@NotNull String key) {
        return $copy();
    }

    @NotNull
    @Override
    public JsonObject json(@NotNull String key) {
        return new JsonObject();
    }

    @NotNull
    @Override
    public <R> R $() {
        return (R) new JsonObject();
    }

    @Override
    public Stream<String> keyStream() {
        return Collections.<String>emptyList().stream();
    }

    @Override
    public Number number(@NotNull String key) {
        return 0;
    }

    @NotNull
    @Override
    public JSONObject orgjson() {
        return new JSONObject();
    }

    @NotNull
    @Override
    public JsonObject json() {
        return new JsonObject();
    }

    @Override
    public ImmutableList<String> strings() {
        return ImmutableList.of();
    }

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
    public <R> R val() {
        return null;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return (obj instanceof var && ((var) obj).val() == null) || obj == null;
    }

    @NotNull
    @Override
    public var eval(@NotNull DollarEval lambda) {
        return $copy();
    }

    @NotNull
    @Override
    public var eval(String label, @NotNull DollarEval lambda) {
        return $copy();
    }

    @NotNull
    @Override
    public var eval(@NotNull Class clazz) {
        return $copy();
    }

    @NotNull
    @Override
    public var $eval(@NotNull String js) {
        return $copy();
    }

    @NotNull
    @Override
    public var $pipe(@NotNull String js, @NotNull String label) {
        return $copy();
    }

    @NotNull
    @Override
    public var $load(@NotNull String location) {
        return $copy();
    }

    @NotNull
    @Override
    public var $pipe(@NotNull Class<? extends Pipeable> clazz) {
        return $copy();
    }

    @NotNull
    @Override
    public var $pop(@NotNull String location, int timeoutInMillis) {
        return $copy();
    }

    @NotNull
    @Override
    public var $pub(@NotNull String... locations) {
        return $copy();
    }

    @NotNull
    @Override
    public var $push(@NotNull String location) {
        return $copy();
    }

    @NotNull
    @Override
    public var $save(@NotNull String location, int expiryInMilliseconds) {
        return $copy();
    }

    @NotNull
    @Override
    public var $save(@NotNull String location) {
        return $copy();
    }

    @Override
    public Stream<Map.Entry<String, var>> kvStream() {
        return Collections.<String, var>emptyMap().entrySet().stream();
    }

    @NotNull
    @Override
    public Stream<var> $stream() {
        return Stream.empty();
    }


    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @NotNull
    @Override
    public var remove(Object value) {
        return $copy();
    }

    @NotNull
    public List<String> splitValues() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public String S() {
        return "";
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
}
