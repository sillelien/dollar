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

import me.neilellis.dollar.AbstractDollar;
import me.neilellis.dollar.DollarEval;
import me.neilellis.dollar.Script;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
 *  nulled.$pipe((i)->{System.out.println("You'll never see this."});
 *
 * </pre>
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarVoid extends AbstractDollar implements var {


    public static final var INSTANCE = DollarFactory.fromValue(Collections.emptyList(), null);

    public DollarVoid(@NotNull List<Throwable> errors) {
        super(errors);
    }

    public DollarVoid() {

        super(Collections.emptyList());
    }

    @NotNull
    @Override
    public var $(@NotNull String age, long l) {
        return this;
    }

    @NotNull
    @Override
    public var $(@NotNull String age, double d) {
        return this;
    }

    @NotNull
    @Override
    public var $append(Object value) {
        return this;
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
    public boolean $has(@NotNull String key) {
        return false;
    }

    @NotNull
    @Override
    public List<var> list() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public Map<String, var> $map() {
        return Collections.emptyMap();
    }

    @NotNull
    @Override
    public String string(@NotNull String key) {
        return "";
    }

    @NotNull
    @Override
    public var $rm(@NotNull String value) {
        return this;
    }

    @NotNull
    @Override
    public var $(@NotNull String key, Object value) {
        return this;
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
        return this;
    }

    @NotNull
    @Override
    public var $(@NotNull String key) {
        return this;
    }

    @NotNull
    @Override
    public JsonObject json() {
        return new JsonObject();
    }

    @NotNull
    @Override
    public JsonObject json(@NotNull String key) {
        return new JsonObject();
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

    @Override
    public List<String> strings() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> toMap() {
        return Collections.emptyMap();
    }

    @Override
    public <R> R val() {
        return null;
    }

    @NotNull
    @Override
    public <R> R $() {
        return (R) new JsonObject();
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
        return this;
    }

    @NotNull
    @Override
    public var eval(String label, @NotNull DollarEval lambda) {
        return this;
    }

    @NotNull
    @Override
    public var eval(@NotNull Class clazz) {
        return this;
    }

    @NotNull
    @Override
    public var $pipe(@NotNull String js) {
        return this;
    }

    @NotNull
    @Override
    public var $pipe(@NotNull String js, @NotNull String label) {
        return this;
    }

    @NotNull
    @Override
    public var $load(@NotNull String location) {
        return this;
    }

    @NotNull
    @Override
    public var $pipe(@NotNull Class<? extends Script> clazz) {
        return this;
    }

    @NotNull
    @Override
    public var $pipe(@NotNull Function<var, var> function) {
        return this;
    }

    @NotNull
    @Override
    public var $pop(@NotNull String location, int timeoutInMillis) {
        return this;
    }

    @NotNull
    @Override
    public var $pub(@NotNull String... locations) {
        return this;
    }

    @NotNull
    @Override
    public var $push(@NotNull String location) {
        return this;
    }

    @NotNull
    @Override
    public var $save(@NotNull String location, int expiryInMilliseconds) {
        return this;
    }

    @NotNull
    @Override
    public var $save(@NotNull String location) {
        return this;
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

    @NotNull
    @Override
    public DollarVoid $copy() {
        return new DollarVoid(new ArrayList<>(errors()));
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
        return this;
    }

    @NotNull
    public List<String> splitValues() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public String toString() {
        return "void";
    }

}
