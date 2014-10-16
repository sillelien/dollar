package me.neilellis.dollar;

import org.json.JSONObject;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarFuture implements var {

    private Future<var> value = new CompletableFuture<>();

    public DollarFuture(Future<var> value) {
        this.value = value;
    }

    @Override
    public var $(String age, long l) {
        return getValue().$(age, l);
    }

    @Override
    public var $(String key) {
        return getValue().$(key);
    }

    @Override
    public var $(String key, Object value) {
        return getValue().$(key, value);
    }

    @Override
    public <R> R $() {
        return getValue().$();
    }

    @Override
    public String $$(String key) {
        return getValue().$$(key);
    }

    @Override
    public String $$() {
        return getValue().$$();
    }

    @Override
    public Integer $int() {
        return getValue().$int();
    }

    @Override
    public Integer $int(String key) {
        return getValue().$int(key);
    }

    @Override
    public JsonObject $json() {
        return getValue().$json();
    }

    @Override
    public JsonObject $json(String key) {
        return getValue().$json(key);
    }

    @Override
    public List<var> list() {
        return getValue().list();
    }

    @Override
    public Map<String, Object> $map() {
        return getValue().$map();
    }

    @Override
    public Number $number(String key) {
        return getValue().$number(key);
    }

    @Override
    public JSONObject $orgjson() {
        return getValue().$orgjson();
    }

    @Override
    public var add(Object value) {
        return getValue().add(value);
    }

    @Override
    public Stream<var> children() {
        return getValue().children();
    }

    @Override
    public Stream children(String key) {
        return getValue().children(key);
    }

    @Override
    public var copy() {
        return getValue().copy();
    }

    @Override
    public var decode() {
        return getValue().decode();
    }

    @Override
    public boolean equals(Object obj) {
        return getValue().equals(obj);
    }

    @Override
    public void err() {
        getValue().err();
    }

    @Override
    public var eval(String js) {
        return eval("anon", js);
    }

    @Override
    public var eval(String js, String label) {
        return getValue().eval(js, label);
    }

    @Override
    public var eval(DollarEval lambda) {
        return eval("anon", lambda);
    }

    @Override
    public var eval(String label, DollarEval eval) {
        return getValue().eval(label, eval);
    }

    @Override
    public var eval(Class clazz) {
        return getValue().eval(clazz);
    }

    @Override
    public var get(Object key) {
        return getValue().get(key);
    }

    public Future<var> future() {
        return value;
    }


    @Override
    public boolean has(String key) {
        return getValue().has(key);
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    @Override
    public var getOrDefault(Object key, var defaultValue) {
        return getValue().getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super var> action) {
        getValue().forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super var, ? extends var> function) {
        getValue().replaceAll(function);
    }

    @Override
    public var putIfAbsent(String key, var value) {
        return getValue().putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return getValue().remove(key, value);
    }

    @Override
    public boolean replace(String key, var oldValue, var newValue) {
        return getValue().replace(key, oldValue, newValue);
    }

    @Override
    public var replace(String key, var value) {
        return getValue().replace(key, value);
    }

    @Override
    public var computeIfAbsent(String key, Function<? super String, ? extends var> mappingFunction) {
        return getValue().computeIfAbsent(key, mappingFunction);
    }

    @Override
    public var computeIfPresent(String key, BiFunction<? super String, ? super var, ? extends var> remappingFunction) {
        return getValue().computeIfPresent(key, remappingFunction);
    }

    @Override
    public var compute(String key, BiFunction<? super String, ? super var, ? extends var> remappingFunction) {
        return getValue().compute(key, remappingFunction);
    }

    @Override
    public var merge(String key, var value, BiFunction<? super var, ? super var, ? extends var> remappingFunction) {
        return getValue().merge(key, value, remappingFunction);
    }

    @Override
    public boolean isNull() {
        return getValue().isNull();
    }

    @Override
    public Stream<Map.Entry<String, var>> keyValues() {
        return getValue().keyValues();
    }

    @Override
    public Stream<String> $keys() {
        return getValue().$keys();
    }

    @Override
    public var load(String location) {
        return getValue().load(location);
    }

    @Override
    public String $mimeType() {
        return getValue().$mimeType();
    }

    @Override
    public void out() {
        getValue().out();
    }

    @Override
    public var pipe(Class<? extends Script> clazz) {
        return getValue().pipe(clazz);
    }

    @Override
    public var pipe(Function<var, var> function) {
        return getValue().pipe(function);
    }

    @Override
    public var pop(String location, int timeoutInMillis) {
        return getValue().pop(location, timeoutInMillis);
    }

    @Override
    public var pub(String... locations) {
        return getValue().pub(locations);
    }

    @Override
    public var push(String location) {
       return  getValue().push(location);
    }

    @Override
    public var remove(Object value) {
        return getValue().remove(value);
    }

    @Override
    public var rm(String value) {
        return getValue().rm(value);
    }

    @Override
    public var save(String location) {
        return getValue().save(location);
    }

    @Override
    public var save(String location, int expiryInMilliseconds) {
        return getValue().save(location, expiryInMilliseconds);
    }

    @Override
    public FutureDollar send(EventBus e, String destination) {
        return getValue().send(e, destination);
    }

    @Override
    public var set(String key, Object value) {
        return getValue().set(key, value);
    }

    @Override
    public Map<String, var> split() {
        return getValue().split();
    }

    @Override
    public Stream<var> stream() {
        return getValue().stream();
    }

    @Override
    public List<String> $strings() {
        return getValue().$strings();
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    @Override
    public var _unwrap() {
        return getValue();
    }

    @Override
    public Map<String, var> map() {
        return getValue().map();
    }

    var getValue() {
        try {
            return value.get();
        } catch (InterruptedException ie) {
            return DollarStatic.handleInterrupt(ie);
        } catch (ExecutionException e) {
            return DollarStatic.handleError(e.getCause());
        } catch (Exception e) {
            return DollarStatic.handleError(e);
        }
    }

    public void setValue(var newValue) {
        if (value instanceof CompletableFuture) {
            ((CompletableFuture) value).complete(newValue);
        }
    }

    @Override
    public <R> R $val() {
        return getValue().$val();
    }

    @Override
    public var error(String errorMessage) {
        return getValue().error(errorMessage);
    }

    @Override
    public var error(Throwable error) {
        return getValue().error(error);
    }

    @Override
    public var error() {
        return getValue().error();
    }

    @Override
    public boolean hasErrors() {
        return getValue().hasErrors();
    }

    @Override
    public List<String> $errorTexts() {
        return getValue().$errorTexts();
    }

    @Override
    public List<Throwable> $errors() {
        return getValue().$errors();
    }

    @Override
    public void clearErrors() {
        getValue().clearErrors();
    }

    @Override
    public var fail(Consumer<List<Throwable>> handler) {
        return getValue().fail(handler);
    }

    @Override
    public var copy(List<Throwable> errors) {
        return getValue().copy(errors);
    }

    @Override
    public int size() {
        return getValue().size();
    }

    @Override
    public boolean isEmpty() {
        return getValue().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return getValue().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return getValue().containsValue(value);
    }

    @Override
    public var put(String key, var value) {
        return getValue().put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends var> m) {
        getValue().putAll(m);
    }

    @Override
    public void clear() {
        getValue().clear();
    }

    @Override
    public Set<String> keySet() {
        return getValue().keySet();
    }

    @Override
    public Collection<var> values() {
        return getValue().values();
    }

    @Override
    public Set<Entry<String, var>> entrySet() {
        return getValue().entrySet();
    }
}
