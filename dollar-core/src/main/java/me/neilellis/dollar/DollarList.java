package me.neilellis.dollar;

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

     DollarList(List<Throwable> errors, JsonArray array) {
         super(errors);
         list = list.stream().map((i) -> DollarFactory.fromValue(errors, i)).collect(Collectors.toList());
    }

     DollarList(List<Throwable> errors,List<var> list) {
         super(errors);
         this.list.addAll(list);
    }

     DollarList(List<Throwable> errors,Object... values) {
         super(errors);
         for (Object value : values) {
            list.add(DollarFactory.fromValue(errors,value));
        }
    }

    @Override
    public var $(String age, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var $(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var $(String key, Object value) {
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
    public List<var> list() {
        return new ArrayList<>(list);
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
    public var add(Object value) {
        DollarList copy = (DollarList) copy();
        copy.list.add(DollarFactory.fromValue($errors(),value));
        return copy;

    }

    @Override
    public var copy() {
        return new DollarList($errors(),list.stream().map(var::copy).collect(Collectors.toList()));
    }

    @Override
    public Stream<var> children() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream children(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var decode() {
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
    public Stream<Map.Entry<String, var>> keyValues() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<String> $keys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String $mimeType() {
        return "application/json";
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean containsValue(Object value) {
        return list.contains(value);
    }

    @Override
    public var remove(Object value) {
        List<var> newList = new ArrayList<>();
        for (var val : list) {
            if (!val.equals(value)) {
                newList.add(val);
            }
        }
        return new DollarList($errors(),newList);
    }

    @Override
    public var rm(String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, var> split() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<var> stream() {
        return list.stream();
    }

    @Override
    public List<String> $strings() {
        return list.stream().map(Object::toString).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return $().toString();
    }

    @Override
    public Map<String, var> map() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> R $() {
        JsonArray array = new JsonArray();
        list.forEach((i) -> {
            array.add(i.$());
        });
        return (R) array;
    }

    @Override
    public JsonArray $val() {
        return $();
    }


    @Override
    public var copy(List<Throwable> errors) {
        List<Throwable> errorList = $errors();
        errorList.addAll(errors);
        return DollarFactory.fromValue(errorList, list);
    }


}
