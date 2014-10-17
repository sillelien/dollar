package me.neilellis.dollar.types;

import me.neilellis.dollar.AbstractDollar;
import me.neilellis.dollar.exceptions.ListException;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarList extends AbstractDollar {

    private List<var> list = new ArrayList<>();

     DollarList(@NotNull List<Throwable> errors, @NotNull JsonArray array) {
         super(errors);
         list = list.stream().map((i) -> DollarFactory.fromValue(errors, i)).collect(Collectors.toList());
    }

     DollarList(@NotNull List<Throwable> errors, @NotNull List<var> list) {
         super(errors);
         this.list.addAll(list);
    }

     DollarList(@NotNull List<Throwable> errors, @NotNull Object... values) {
         super(errors);
         for (Object value : values) {
            list.add(DollarFactory.fromValue(errors,value));
        }
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public var $(@NotNull String age, long l) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @NotNull
    @Override
    public var $append(Object value) {
        DollarList copy = (DollarList) $copy();
        copy.list.add(DollarFactory.fromValue(errors(), value));
        return copy;

    }

    @NotNull
    @Override
    public Stream<var> $children() {
        return list.stream();
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
    public List<var> $list() {
        return new ArrayList<>(list);
    }

    @NotNull
    @Override
    public Map<String, var> $map() {
        return null;
    }

    @Nullable
    @Override
    public String string(@NotNull String key) {
        return null;
    }

    @NotNull
    @Override
    public var $rm(@NotNull String value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @NotNull
    @Override
    public var $(@NotNull String key, Object value) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @Override
    public boolean $void() {
        return false;
    }

    @Override
    public Integer I() {
        return $stream().collect(Collectors.summingInt((i) -> i.I()));
    }

    @Override
    public Integer I(@NotNull String key) {
        throw new ListException();
    }

    @NotNull
    @Override
    public var decode() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public var $(@NotNull String key) {
        return DollarFactory.failure(DollarFail.FailureType.INVALID_LIST_OPERATION);
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public JsonObject json() {
        JsonArray array = $array();
        JsonObject jsonObject = new JsonObject();
        jsonObject.putArray("value", array);
        return jsonObject;
    }

    @org.jetbrains.annotations.NotNull
    @Override
    public JsonObject json(@NotNull String key) {
        throw new ListException();
    }

    @Override
    public Stream<String> keyStream() {
        List<String> strings = strings();
        if (strings == null) {
            return Stream.empty();
        }
        return strings.stream();
    }

    @Override
    public Number number(@NotNull String key) {
        throw new ListException();
    }

    @NotNull
    @Override
    public JSONObject orgjson() {
        return new JSONObject(json().toMap());
    }

    @Override
    public List<String> strings() {
        return list.stream().map(Object::toString).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> toMap() {
        throw new ListException();
    }

    @Override
    public <R> R val() {
        return (R) list;
    }

    @NotNull
    @Override
    public <R> R $() {
        return (R) $array();
    }

    @Override
    public Stream<Map.Entry<String, var>> kvStream() {
        return null;
    }

    @NotNull
    @Override
    public Stream<var> $stream() {
        return list.stream();
    }

    @NotNull
    @Override
    public var $copy() {
        return new DollarList(errors(), list.stream().map(var::$copy).collect(Collectors.toList()));
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean containsValue(Object value) {
        return list.contains(value);
    }

    @NotNull
    @Override
    public var remove(Object value) {
        List<var> newList = new ArrayList<>();
        for (var val : list) {
            if (!val.equals(value)) {
                newList.add(val);
            }
        }
        return new DollarList(errors(), newList);
    }

    @NotNull
    @Override
    public String toString() {
        return $array().toString();
    }



}
