package com.cazcade.dollar;

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

    private List<$> list = new ArrayList<>();

    public DollarList(JsonArray array) {
        list = list.stream().map((i) -> DollarFactory.fromValue(i)).collect(Collectors.toList());
    }

    public DollarList(List<$> list) {
        this.list.addAll(list);
    }

    public DollarList(Object... values) {
        for (Object value : values) {
            list.add(DollarFactory.fromValue(value));
        }
    }

    @Override
    public $ $(String age, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public $ $(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public $ $(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String $$(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String $$() {
        return list.toString();
    }

    @Override
    public Integer $int() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer $int(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonObject $json() {
        throw new UnsupportedOperationException();
    }

    @Override
    public JsonObject $json(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> $map() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Number $number(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JSONObject $orgjson() {
        throw new UnsupportedOperationException();
    }

    @Override
    public $ add(Object value) {
        DollarList copy = (DollarList) copy();
        copy.list.add(DollarFactory.fromValue(value));
        return copy;

    }

    @Override
    public $ copy() {
        return new DollarList(list.stream().map($::copy).collect(Collectors.toList()));
    }

    @Override
    public Stream<$> children() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream children(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public $ decode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean has(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public Stream<Map.Entry<String, $>> keyValues() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<String> keys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String mimeType() {
        return "application/json";
    }

    @Override
    public $ remove(Object value) {
        List<$> newList = new ArrayList<>();
        for ($ val : list) {
            if (!val.equals(value)) {
                newList.add(val);
            }
        }
        return new DollarList(newList);
    }

    @Override
    public $ rm(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, $> split() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> splitValues() {
        return $list();
    }

    @Override
    public List<String> $list() {
        return list.stream().map(Object::toString).collect(Collectors.toList());
    }

    @Override
    public Stream<$> stream() {
        return list.stream();
    }

    @Override
    public String toString() {
        return $().toString();
    }

    @Override
    public JsonArray $() {
        JsonArray array = new JsonArray();
        list.forEach((i) -> {
            array.add(i.$());
        });
        return array;
    }

    @Override
    public JsonArray val() {
        return $();
    }

}
