package com.cazcade.dollar;

import com.cazcade.dollar.store.DollarStore;
import com.cazcade.dollar.store.RedisStore;
import org.vertx.java.core.eventbus.EventBus;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public abstract class AbstractDollar implements $ {

    private static ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
    private static DollarStore store = new RedisStore();

    @Override
    public $ eval(DollarEval lambda) {
        return eval("anon", lambda);
    }

    public $ eval(String label, DollarEval lambda) {
        return lambda.eval(copy());
    }

    @Override
    public $ eval(Class clazz) {
        List<String> list = this.$list();
        try {
            try {
                Method callMethod = clazz.getMethod("call", $.class);
                callMethod.invoke(null, this);
                return this;
            } catch (NoSuchMethodException e) {
                //
            }
            try {
                Method mainMethod = clazz.getMethod("main", String[].class);
                return DollarFactory.fromValue(mainMethod.invoke(null, list.toArray(new String[list.size()])));
            } catch (NoSuchMethodException e) {
                throw new DollarException(e);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new DollarException(e);
        }
    }

    @Override
    public $ eval(String js) {
        return eval("anon", js);
    }

    public $ eval(String js, String label) {
        try {
            SimpleScriptContext context = new SimpleScriptContext();
            context.setAttribute("$", copy(), context.getScopes().get(0));
            return DollarFactory.fromValue(nashorn.eval(js, context));
        } catch (ScriptException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public $ load(String location) {
        return store.get(location);
    }

    @Override
    public $ pop(String location, int timeoutInMillis) {
        return store.pop(location, timeoutInMillis);
    }

    @Override
    public void push(String location) {
        store.push(location, this);
    }

    @Override
    public void save(String location, int expiryInMilliseconds) {
        store.set(location, this, expiryInMilliseconds);
    }

    @Override
    public void save(String location) {
        store.set(location, this);
    }

    public FutureDollar send(EventBus e, String destination) {
        throw new UnsupportedOperationException();

    }

}
