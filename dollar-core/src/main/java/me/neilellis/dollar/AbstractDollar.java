package me.neilellis.dollar;

import org.vertx.java.core.eventbus.EventBus;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public abstract class AbstractDollar implements var {

    private static ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
    private final List<Throwable> errors;

    protected AbstractDollar(List<Throwable> errors) {
        this.errors = new ArrayList<>(errors);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof var) {
            var unwrapped = ((var) obj).unwrap();
            return $() != null && $().equals(unwrapped.$());
        } else {
            return false;
        }
    }

    @Override
    public var eval(DollarEval lambda) {
        return eval("anon", lambda);
    }

    public var eval(String label, DollarEval lambda) {
        return lambda.eval(copy());
    }

    @Override
    public var eval(Class clazz) {
        List<String> list = this.strings();
        try {
            try {
                Method callMethod = clazz.getMethod("call", var.class);
                callMethod.invoke(null, this);
                return this;
            } catch (NoSuchMethodException e) {
                //
            }
            try {
                Method mainMethod = clazz.getMethod("main", String[].class);
                Object value;
                try {
                    value = mainMethod.invoke(null, list.toArray(new String[list.size()]));
                } catch (Exception e) {
                    return DollarStatic.handleError(e);
                }
                return DollarFactory.fromValue(Collections.emptyList(), value);
            } catch (NoSuchMethodException e) {
                throw new DollarException(e);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new DollarException(e);
        }
    }

    @Override
    public var eval(String js) {
        return eval( "anon",js);
    }

    public var eval(String label, String js) {
        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("$", copy(), context.getScopes().get(0));
        Object value;
        try {
            value = nashorn.eval(js, context);
        } catch (Exception e) {
            return DollarStatic.handleError(e);
        }
        return DollarFactory.fromValue(Collections.emptyList(), value);
    }

    @Override
    public int hashCode() {
        return $().hashCode();
    }

    @Override
    public var load(String location) {
        return DollarStatic.context().getStore().get(location);
    }

    @Override
    public String mimeType() {
        return "text/plain";
    }

    @Override
    public var pipe(Class<? extends Script> clazz) {
        DollarStatic.threadContext.get().setPassValue(this);
        Script script = null;
        try {
            script = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new DollarException(e.getCause());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return script.result();
    }

    @Override
    public var pipe(Function<var, var> function) {
        return function.apply(this);
    }

    @Override
    public var pop(String location, int timeoutInMillis) {
        return DollarStatic.context().getStore().pop(location, timeoutInMillis);

    }

    @Override
    public var pub(String... locations) {
        DollarStatic.context().getPubsub().pub(this, locations);
        return this;
    }

    @Override
    public var push(String location) {
        DollarStatic.context().getStore().push(location, this);
        return this;
    }

    @Override
    public var save(String location, int expiryInMilliseconds) {
        DollarStatic.context().getStore().set(location, this, expiryInMilliseconds);
        return this;
    }

    @Override
    public var save(String location) {
        DollarStatic.context().getStore().set(location, this);
        return this;
    }

    public FutureDollar send(EventBus e, String destination) {
        throw new UnsupportedOperationException();

    }

    @Override
    public Stream<var> stream() {
        return list().stream();
    }

    @Override
    public var error(String errorMessage) {
        errors.add(new Exception(errorMessage));
        return this;
    }

    @Override
    public var error(Throwable error) {
        errors.add(error);
        return this;
    }

    @Override
    public var error() {
        errors.add(new Exception("Unspecified Error"));
        return this;
    }

    @Override
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Override
    public List<String> errorTexts() {
        return errors.stream().map(Throwable::getMessage).collect(Collectors.toList());
    }

    @Override
    public List<Throwable> errors() {
        return new ArrayList<>(errors);
    }

    @Override
    public void clearErrors() {
        errors.clear();
    }

    @Override
    public var onErrors(Consumer<List<Throwable>> handler) {
        if(hasErrors()) {
            handler.accept(errors());
            return DollarFactory.fromValue(errors(),null);
        } else {
            return this;
        }
    }


    @Override
    public boolean isEmpty() {
        return size() > 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return has(String.valueOf(key));
    }



    @Override
    public var put(String key, var value) {
        return $(key,value);
    }




    @Override
    public Set<String> keySet() {
        return keys().collect(Collectors.toSet());
    }

    @Override
    public Collection<var> values() {
        return list();
    }

    @Override
    public Set<Entry<String, var>> entrySet() {
        return keyValues().collect(Collectors.toSet());
    }

    @Override
    public var getOrDefault(Object key, var defaultValue) {
        return has(String.valueOf(key)) ? get(key) : defaultValue;
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super var> action) {
        map().forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super var, ? extends var> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var putIfAbsent(String key, var value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(String key, var oldValue, var newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var replace(String key, var value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var computeIfAbsent(String key, Function<? super String, ? extends var> mappingFunction) {
        return map().computeIfAbsent(key,mappingFunction);
    }

    @Override
    public var computeIfPresent(String key, BiFunction<? super String, ? super var, ? extends var> remappingFunction) {
        return map().computeIfPresent(key,remappingFunction);
    }

    @Override
    public var compute(String key, BiFunction<? super String, ? super var, ? extends var> remappingFunction) {
        return map().compute(key,remappingFunction);
    }

    @Override
    public var merge(String key, var value, BiFunction<? super var, ? super var, ? extends var> remappingFunction) {
        return map().merge(key,value,remappingFunction);
    }


    @Override
    public void putAll(Map<? extends String, ? extends var> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
