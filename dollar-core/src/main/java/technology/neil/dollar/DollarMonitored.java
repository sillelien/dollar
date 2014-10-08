package technology.neil.dollar;

import technology.neil.dollar.monitor.Monitor;
import org.json.JSONObject;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarMonitored implements var {

    private Monitor monitor;
    private var value;

    public DollarMonitored(var value, Monitor monitor) {
        this.value = value;
        this.monitor = monitor;
    }

    public static void config(String key, String value) {
        var.config(key, value);
    }

    public static String config(String s) {
        return var.config(s);
    }

    @Override
    public var $(String age, long l) {
        return getValue().$(age, l);
    }

    var getValue() {
        return value;
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
    public List<var> $list() {
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
    public var eval(String label, String js) {
        return monitor.run("$eval", "dollar.eval.js." + sanitize(label), "Evaluating: " + js, () -> getValue().eval(label, js));
    }

    private static String sanitize(String location) {
        return location.replaceAll("[^\\w.]+", "_");

    }

    @Override
    public var eval(DollarEval lambda) {
        return eval("anon", lambda);
    }

    @Override
    public var eval(String label, DollarEval lambda) {
        return monitor.run("$fun", "dollar.eval.java." + sanitize(label), "Evaluating Lambda", () -> getValue().eval(label, lambda));
    }

    @Override
    public var eval(Class clazz) {
        return monitor.run("save", "dollar.run." + clazz.getName(), "Running class " + clazz, () -> getValue().eval(clazz));
    }

    @Override
    public var get(String key) {
        return getValue().get(key);
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
    public boolean isNull() {
        return getValue().isNull();
    }

    @Override
    public Stream<Map.Entry<String, var>> keyValues() {
        return getValue().keyValues();
    }

    @Override
    public Stream<String> keys() {
        return getValue().keys();
    }

    @Override
    public var load(String location) {
        return monitor.run("save", "dollar.persist.temp.load." + sanitize(location), "Loading value at " + location, () -> getValue().load(location));
    }

    @Override
    public String mimeType() {
        return getValue().mimeType();
    }

    @Override
    public void out() {
        getValue().out();
    }

    @Override
    public var pipe(Class<? extends Script> clazz) {
        return monitor.run("pipe", "dollar.run.pipe." + sanitize(clazz), "Piping to " + clazz.getName(), () -> getValue().pipe(clazz));
    }

    private String sanitize(Class<? extends Script> clazz) {
        return clazz.getName().toLowerCase();
    }

    @Override
    public var pop(String location, int timeoutInMillis) {
        return monitor.run("pop", "dollar.persist.temp.pop." + sanitize(location), "Popping value from " + location, () -> getValue().pop(location, timeoutInMillis));
    }

    @Override
    public void pub(String... locations) {
        monitor.run("pub", "dollar.message.pub." + sanitize(Arrays.toString(locations)), "Publishing value to " + Arrays.toString(locations), () -> getValue().pub(locations));
    }

    @Override
    public void push(String location) {
        monitor.run("push", "dollar.persist.temp.push." + sanitize(location), "Pushing value to " + location, () -> getValue().push(location));
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
        return monitor.run("save", "dollar.persist.temp.save." + sanitize(location), "Saving value at " + location, () -> getValue().save(location));
    }

    @Override
    public var save(String location, int expiryInMilliseconds) {
        return monitor.run("save", "dollar.persist.temp.save." + sanitize(location), "Saving value at " + location + " with expiry " + expiryInMilliseconds, () -> getValue().save(location, expiryInMilliseconds));
    }

    @Override
    @Deprecated
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
    public List<String> strings() {
        return getValue().strings();
    }

    @Override
    public String toString() {
        return getValue().toString();
    }

    @Override
    public var unwrap() {
        return value;
    }

    @Override
    public <R> R val() {
        return getValue().val();
    }


}
