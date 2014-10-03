package com.cazcade.dollar;

import org.json.JSONObject;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarFuture implements $ {

    private Future<$> value = new CompletableFuture<>();

    public DollarFuture(Future<$> value) {
        this.value = value;
    }

    @Override
    public $ $(String age, long l) {
        return getValue().$(age, l);
    }

    $ getValue() {
        try {
            return value.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new DollarException(e);
        }
    }

    public void setValue($ newValue) {
        if (value instanceof CompletableFuture) {
            ((CompletableFuture) value).complete(newValue);
        }
    }

    @Override
    public $ $(String key) {
        return getValue().$(key);
    }

    @Override
    public $ $(String key, Object value) {
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
    public List<String> $list() {
        return getValue().$list();
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
    public Stream<$> children() {
        return getValue().children();
    }

    @Override
    public Stream children(String key) {
        return getValue().children(key);
    }

    @Override
    public $ copy() {
        return getValue().copy();
    }

    @Override
    public $ decode() {
        return getValue().decode();
    }

    @Override
    public $ eval(String js) {
        return eval("anon", js);
    }

    @Override
    public $ eval(String js, String label) {
        return getValue().eval(js, label);
    }

    @Override
    public $ eval(DollarEval lambda) {
        return eval("anon", lambda);
    }

    @Override
    public $ eval(String label, DollarEval eval) {
        return getValue().eval(label, eval);
    }

    @Override
    public $ eval(Class clazz) {
        return getValue().eval(clazz);
    }

    public Future<$> future() {
        return value;
    }

    @Override
    public boolean has(String key) {
        return getValue().has(key);
    }

    @Override
    public boolean isNull() {
        return getValue().isNull();
    }

    @Override
    public Stream<Map.Entry<String, $>> keyValues() {
        return getValue().keyValues();
    }

    @Override
    public Stream<String> keys() {
        return getValue().keys();
    }

    @Override
    public $ load(String location) {
        return getValue().load(location);
    }

    @Override
    public $ pop(String location, int timeoutInMillis) {
        return getValue().pop(location, timeoutInMillis);
    }

    @Override
    public void pub(String... locations) {
        getValue().pub(locations);
    }

    @Override
    public void push(String location) {
        getValue().push(location);
    }

    @Override
    public $ rm(String value) {
        return getValue().rm(value);
    }

    @Override
    public void save(String location) {
        getValue().save(location);
    }

    @Override
    public void save(String location, int expiryInMilliseconds) {
        getValue().save(location, expiryInMilliseconds);
    }

    @Override
    public FutureDollar send(EventBus e, String destination) {
        return getValue().send(e, destination);
    }

    @Override
    public Map<String, $> split() {
        return getValue().split();
    }

    @Override
    public List<String> splitValues() {
        return getValue().splitValues();
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    @Override
    public <R> R val() {
        return getValue().val();
    }
}
