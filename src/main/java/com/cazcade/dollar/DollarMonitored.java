package com.cazcade.dollar;

import com.cazcade.dollar.monitor.Monitor;
import org.json.JSONObject;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarMonitored implements $ {

    private Monitor monitor;
    private $ value;

    public DollarMonitored($ value, Monitor monitor) {
        this.value = value;
        this.monitor = monitor;
    }

    public static void config(String key, String value) {
        $.config(key, value);
    }

    public static String config(String s) {
        return $.config(s);
    }

    @Override
    public $ $(String age, long l) {
        return getValue().$(age, l);
    }

    $ getValue() {
        return value;
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
    public $ eval(String label, String js) {
        return monitor.run("$eval", "dollar.eval.js." + sanitize(label), "Evaluating: " + js, () -> getValue().eval(label, js));
    }

    private static String sanitize(String location) {
        return location.replaceAll("[^\\w.]+", "_");

    }

    @Override
    public $ eval(DollarEval lambda) {
        return eval("anon", lambda);
    }

    @Override
    public $ eval(String label, DollarEval lambda) {
        return monitor.run("$fun", "dollar.eval.java." + sanitize(label), "Evaluating Lambda", () -> getValue().eval(label, lambda));
    }

    @Override
    public $ eval(Class clazz) {
        return monitor.run("save", "dollar.run." + clazz.getName(), "Running class " + clazz, () -> getValue().eval(clazz));
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
        return monitor.run("save", "dollar.persist.temp.load." + sanitize(location), "Loading value at " + location, () -> getValue().load(location));
    }

    @Override
    public $ pop(String location, int timeoutInMillis) {
        return monitor.run("pop", "dollar.persist.temp.pop." + sanitize(location), "Popping value from " + location, () -> getValue().pop(location, timeoutInMillis));
    }

    @Override
    public void push(String location) {
        monitor.run("push", "dollar.persist.temp.push." + sanitize(location), "Pushing value to " + location, () -> getValue().push(location));
    }

    @Override
    public $ rm(String value) {
        return getValue().rm(value);
    }

    @Override
    public void save(String location) {
        monitor.run("save", "dollar.persist.temp.save." + sanitize(location), "Saving value at " + location, () -> getValue().save(location));
    }

    @Override
    public void save(String location, int expiryInMilliseconds) {
        monitor.run("save", "dollar.persist.temp.save." + sanitize(location), "Saving value at " + location + " with expiry " + expiryInMilliseconds, () -> getValue().save(location, expiryInMilliseconds));
    }

    @Override
    @Deprecated
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
