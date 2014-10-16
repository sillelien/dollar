package me.neilellis.dollar;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import org.json.JSONObject;
import org.vertx.java.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarRange extends AbstractDollar {

    private final Range<Long> range;

    public DollarRange(List<Throwable> errors, long start, long finish) {
        super(errors);
        range = Range.closed(start, finish);
    }

    public DollarRange(List<Throwable> errors, Range range) {
        super(errors);
        this.range = range;
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
    public Range<Long> $() {
        return range;
    }

    @Override
    public String $$(String key) {
        return range.toString();
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
    public var add(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<var> children() {
        return list().stream();
    }

    @Override
    public List<var> list() {
        return ContiguousSet.create(range, DiscreteDomain.longs()).stream().map(DollarStatic::$).collect(Collectors.toList());
    }

    @Override
    public Stream children(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var copy() {
        return DollarFactory.fromValue($errors(),range);
    }

    @Override
    public var decode() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof var) {
            var unwrapped = ((var) obj)._unwrap();
            if (unwrapped instanceof DollarRange) {
                return range.equals(((DollarRange) unwrapped).range);
            }
            if (unwrapped instanceof DollarList) {
                return unwrapped.list().equals(list());
            }
        }
        return false;

    }

    @Override
    public boolean has(String key) {
        return false;
    }

    @Override
    public int hashCode() {
        return range.hashCode();
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
    public var remove(Object value) {
        throw new UnsupportedOperationException();

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
        return list().stream();
    }

    @Override
    public List<String> $strings() {
        return ContiguousSet.create(range, DiscreteDomain.longs()).stream().map(Object::toString).collect(Collectors.toList());
    }

    @Override
    public Map<String, var> map() {
        throw new UnsupportedOperationException();
    }


    @Override
    public var copy(List<Throwable> errors) {
        List<Throwable> errorList = $errors();
        errorList.addAll(errors);
        return DollarFactory.fromValue(errorList, range);
    }

    @Override
    public int size() {
        return (int) (range.upperEndpoint()-range.lowerEndpoint());
    }

    @Override
    public boolean containsValue(Object value) {
        if(value instanceof Number) {
            return range.contains(((Number) value).longValue());
        }
        return range.contains(Long.valueOf(value.toString()));
    }
}
