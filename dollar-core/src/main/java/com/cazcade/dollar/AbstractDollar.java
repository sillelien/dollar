package com.cazcade.dollar;

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
public abstract class AbstractDollar implements var {

    private static ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof var) {
            return $() != null && $().equals(((var) obj).$());
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
        List<String> list = this.$list();
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
                return DollarFactory.fromValue(mainMethod.invoke(null, list.toArray(new String[list.size()])));
            } catch (NoSuchMethodException e) {
                throw new DollarException(e);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new DollarException(e);
        }
    }

    @Override
    public var eval(String js) {
        return eval("anon", js);
    }

    public var eval(String js, String label) {
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
    public int hashCode() {
        return $().hashCode();
    }

    @Override
    public var load(String location) {
        return DollarStatic.$load(location);
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
    public var pop(String location, int timeoutInMillis) {
        return DollarStatic.$pop(location, timeoutInMillis);

    }

    @Override
    public void pub(String... locations) {
        DollarStatic.$pub(this, locations);
    }

    @Override
    public void push(String location) {
        DollarStatic.$push(location, this);
    }

    @Override
    public var save(String location, int expiryInMilliseconds) {
        DollarStatic.$save(location, this, expiryInMilliseconds);
        return this;
    }

    @Override
    public var save(String location) {
        DollarStatic.$save(this, location);
        return this;
    }

    public FutureDollar send(EventBus e, String destination) {
        throw new UnsupportedOperationException();

    }

}
