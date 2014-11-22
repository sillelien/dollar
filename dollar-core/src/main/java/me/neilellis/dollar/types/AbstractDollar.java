/*
 * Copyright (c) 2014 Neil Ellis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.neilellis.dollar.types;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import me.neilellis.dollar.*;
import me.neilellis.dollar.exceptions.ValidationException;
import me.neilellis.dollar.json.JsonArray;
import me.neilellis.dollar.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.jayway.restassured.RestAssured.given;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public abstract class AbstractDollar implements var {

    private static
    @NotNull
    ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");

    protected static StateMachineConfig<ResourceState, Signal> getDefaultStateMachineConfig() {
        final StateMachineConfig<ResourceState, Signal> stateMachineConfig = new StateMachineConfig<>();
        stateMachineConfig.configure(ResourceState.STOPPED)
                          .permitReentry(Signal.STOP)
                          .permit(Signal.START, ResourceState.RUNNING);
        stateMachineConfig.configure(ResourceState.RUNNING)
                          .permit(Signal.STOP, ResourceState.STOPPED)
                          .permitReentry(Signal.START);
        stateMachineConfig.configure(ResourceState.PAUSED)
                          .permit(Signal.STOP, ResourceState.STOPPED)
                          .permit(Signal.UNPAUSE, ResourceState.RUNNING)
                          .permitReentry(Signal.PAUSE);
        stateMachineConfig.configure(ResourceState.DESTROYED).permitReentry(Signal.DESTROY);
        stateMachineConfig.configure(ResourceState.INITIAL)
                          .permit(Signal.CREATE, ResourceState.STOPPED)
                          .permit(Signal.START, ResourceState.RUNNING)
                          .permit(Signal.PAUSE, ResourceState.PAUSED)
                          .permit(Signal.STOP, ResourceState.STOPPED)
                          .permit(Signal.DESTROY, ResourceState.DESTROYED);
        return stateMachineConfig;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @NotNull @Override public var _fix() {
        return this;
    }
    private final
    @NotNull
    ImmutableList<Throwable> errors;

    @Override public void $signal(Signal signal) {
        getStateMachine().fire(signal);
    }

    private String src;

    @Override public var $start() {
        getStateMachine().fire(Signal.START);
        return this;
    }

    private ConcurrentHashMap<String, String> meta = new ConcurrentHashMap<>();

    @Override public var $stop() {
        getStateMachine().fire(Signal.STOP);
        return this;
    }

    protected AbstractDollar(@NotNull List<Throwable> errors) {
        this.errors = new ImmutableList.Builder<Throwable>().addAll(errors).build();
    }

    @Override public var $create() {
        getStateMachine().fire(Signal.CREATE);
        return this;
    }

    @NotNull
    @Override
    public var $(@NotNull Pipeable lambda) {
        return (var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarLambda(DollarFactory.wrap(this), lambda));
    }

    @Override public var $destroy() {
        getStateMachine().fire(Signal.DESTROY);
        return this;
    }

    @NotNull
    @Override
    public var $(@NotNull var key) {
        if (key.isNumber()) {
            return $(key.N());
        } else {
            return $get(key.$S());
        }
    }

    @Override public var $pause() {
        getStateMachine().fire(Signal.PAUSE);
        return this;
    }

    @Override
    public var $default(Object o) {
        if (isVoid()) {
            return DollarStatic.$(o);
        } else {
            return this;
        }
    }

    @Override public var $unpause() {
        getStateMachine().fire(Signal.UNPAUSE);
        return this;
    }

    @Override
    public var $isEmpty() {
        return DollarFactory.fromValue($size().I() > 0);
    }

    @Override public var $state() {
        System.out.println("State for " + this.getClass() + " is " + getStateMachine().getState());
        return DollarStatic.$(getStateMachine().getState().toString());
    }

    @NotNull
    @Override
    public var $mimeType() {
        return DollarStatic.$("text/plain");
    }

    @Override
    public var $post(String url) {
        return DollarFactory.fromValue(errors(), given().parameters(toMap()).post(url).thenReturn().body().print());
    }

    @NotNull
    @Override
    public Stream<var> $stream() {
        return toList().stream();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean containsKey(Object key) {
        return $has(String.valueOf(key)).isTrue();
    }

    @NotNull
    public Set<Map.Entry<String, var>> entrySet() {
        return kvStream().collect(Collectors.toSet());
    }

    @NotNull
    public Stream<Map.Entry<String, var>> kvStream() {
        return $map().entrySet().stream();

    }

    @NotNull
    public Collection<var> values() {
        return toList();
    }

    @Override
    public var $choose(var map) {
        return map.$get($S());
    }

    @Override
    public var $each(Pipeable pipe) {
        List<var> result = new LinkedList<>();
        for (var v : toList()) {
            var res = null;
            try {
                res = pipe.pipe(v);
            } catch (Exception e) {
                throw new DollarException(e);
            }
            result.add(res);

        }
        debug(result);
        final var resultvar = DollarFactory.fromValue(result);
        return resultvar;
    }

    @NotNull
    @Override
    public var eval(@NotNull DollarEval lambda) {
        return eval("anon", lambda);
    }

    @NotNull
    @Override
    public var $dec(@NotNull var key, @NotNull var amount) {
        return $(key, $(key).L() - amount.L());
    }

    @NotNull
    @Override
    public var $inc(@NotNull var key, @NotNull var amount) {
        return $(key, $(key).L() + amount.L());
    }

    @Override
    public String $listen(Pipeable pipe) {
//        do nothing, not a reactive type
//        System.err.println("Cannot listen on a non reactive type " + getClass().getName());
        return "<unknown>";
    }

    @NotNull
    @Override
    public var _unwrap() {
        return this;
    }

    @Override
    public var $notify(var value) {
//        do nothing, not a reactive type
        return this;
    }

    @NotNull
    @Override
    public var copy(@NotNull ImmutableList<Throwable> errors) {
        return DollarFactory.fromValue(new ImmutableList.Builder<Throwable>().addAll(errors()).addAll(errors).build(),
                                       $());
    }

    @Override
    public String $listen(Pipeable pipe, String key) {
//        System.err.println("Cannot listen on a non reactive type " + getClass().getName());
        return "<unknown>";
    }

    @Override
    public var $send(var value, boolean blocking, boolean mutating) {
        return this;
    }

    @Override
    public var $send(var given) {
        debug("Cannot send to " + getClass().getName());
        return this;
    }

    @Override
    public var $receive(boolean blocking, boolean mutating) {
        return this;
    }

    @Override
    public var $drain() {
        return DollarStatic.$void();
    }

    @NotNull
    public var eval(String label, @NotNull DollarEval lambda) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var $all() {
        return DollarStatic.$void();
    }

    @Override
    public var $subscribe(Pipeable subscription) {
        return this;
    }

    @Override
    public var $publish(var lhs) {
        return this;
    }

    @NotNull
    @Override
    public Double D() {
        return 0.0;
    }

    @NotNull
    @Override
    public Long L() {
        return 0L;
    }

    @Override
    public boolean isDecimal() {
        return false;
    }

    @Override
    public boolean isInteger() {
        return false;
    }

    @Override
    public boolean isLambda() {
        return false;
    }

    @Override
    public boolean isList() {
        return false;
    }

    @Override
    public boolean isMap() {
        return false;
    }

    @Override
    public boolean isNumber() {
        return false;
    }

    @Override
    public boolean isSingleValue() {
        return false;
    }

    @NotNull
    @Override
    public var eval(@NotNull Class clazz) {
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
                    return DollarStatic.handleError(e, this);
                }
                return DollarFactory.fromValue(ImmutableList.of(), value);
            } catch (NoSuchMethodException e) {
                throw new DollarException(e);
            }
        } catch (@NotNull IllegalAccessException | InvocationTargetException e) {
            throw new DollarException(e);
        }
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public boolean isUri() {
        return false;
    }

    @Override
    public InputStream toStream() {
        return new ByteArrayInputStream($S().getBytes());
    }

    @Override
    public var assertNotVoid(String message) throws AssertionError {
        return assertFalse(TypeAware::isVoid, message);
    }

    @Override
    public var assertTrue(Function<var, Boolean> assertion, String message) throws AssertionError {
        try {
            if (!assertion.apply(this)) {
                throw new AssertionError(message);
            } else {
                return this;
            }
        } catch (Exception e) {
            return DollarStatic.logAndRethrow(e);
        }
    }

    @Override
    public var assertFalse(Function<var, Boolean> assertion, String message) throws AssertionError {
        try {
            if (assertion.apply(this)) {
                throw new AssertionError(message);
            } else {
                return this;
            }
        } catch (Exception e) {
            return DollarStatic.logAndRethrow(e);
        }
    }

    public var compute(String key, @NotNull BiFunction<? super String, ? super var, ? extends var> remappingFunction) {
        return $map().compute(key, remappingFunction);
    }

    @NotNull
    @Override
    public var $eval(@NotNull String js) {
        return $pipe("anon", js);
    }

    public var computeIfAbsent(String key, @NotNull Function<? super String, ? extends var> mappingFunction) {
        return $map().computeIfAbsent(key, mappingFunction);
    }

    public var computeIfPresent(String key,
                                @NotNull BiFunction<? super String, ? super var, ? extends var> remappingFunction) {
        return $map().computeIfPresent(key, remappingFunction);
    }

    @Override
    public var debugf(String message, Object... values) {
        logger.debug(message, values);
        return this;
    }

    @NotNull
    public var $pipe(@NotNull String label, @NotNull String js) {
        SimpleScriptContext context = new SimpleScriptContext();
        Object value;
        try {
            nashorn.eval("var $=" + json().toString() + ";", context);
            value = nashorn.eval(js, context);
        } catch (Exception e) {
            return DollarStatic.handleError(e, this);
        }
        return DollarFactory.fromValue(ImmutableList.of(), value);
    }

    @Override
    public var debug(Object message) {
        logger.debug(message.toString());
        return this;
    }

    @NotNull
    @Override
    public var $pipe(@NotNull String label, @NotNull Pipeable pipe) {
        try {
            return pipe.pipe(this);
        } catch (Exception e) {
            return DollarStatic.handleError(e, this);
        }
    }

    @Override
    public var debug() {
        logger.debug(this.toString());
        return this;
    }

    @NotNull
    @Override
    public var $errors() {
        JsonObject json = new JsonObject();
        if (errors.size() > 0) {
            if (errors.get(0) instanceof DollarException) {
                json.putNumber("httpCode", ((DollarException) errors.get(0)).httpCode());
            }
            json.putString("message", errors.get(0).getMessage());
            JsonArray errorArray = new JsonArray();
            for (Throwable error : errors) {
                JsonObject errorJson = new JsonObject();
                errorJson.putString("message", error.getMessage());
                if (!DollarStatic.config.production()) {
                    errorJson.putString("stack", Arrays.toString(error.getStackTrace()));
                } else {
                    errorJson.putString("hash",
                                        Hashing.sha1()
                                               .hashBytes(Arrays.toString(error.getStackTrace()).getBytes())
                                               .toString());
                }
                errorArray.addObject(errorJson);
            }
            json.putArray("errors", errorArray);
        }
        return DollarFactory.fromValue(json);
    }

    @Override
    public var infof(String message, Object... values) {
        logger.info(message, values);
        return this;
    }

    @Override
    public var info(Object message) {
        logger.info(message.toString());
        return this;
    }

    @NotNull
    @Override
    public var $pipe(@NotNull Class<? extends Pipeable> clazz) {
        DollarStatic.threadContext.get().setPassValue(this.$copy());
        Pipeable script = null;
        try {
            script = clazz.newInstance();
        } catch (InstantiationException e) {
            return DollarStatic.handleError(e.getCause(), this);
        } catch (Exception e) {
            return DollarStatic.handleError(e, this);
        }
        try {
            return script.pipe(this);
        } catch (Exception e) {
            return DollarStatic.handleError(e, this);
        }
    }

    @Override
    public var info() {
        logger.info(this.toString());
        return this;
    }

    @Override
    public var errorf(String message, Object... values) {
        logger.error(message, values);
        return this;
    }

    @Override
    public var error(Throwable exception) {
        logger.error(exception.getMessage(), exception);
        return this;
    }

    @Override
    public var error(Object message) {
        logger.error(message.toString());
        return this;

    }

    @Override
    public var error() {
        logger.error(this.toString());
        return this;
    }

    @NotNull
    @Override
    public var $copy() {
        return DollarFactory.fromValue(new ImmutableList.Builder<Throwable>().addAll(errors()).build(), $());
    }

    public void forEach(@NotNull BiConsumer<? super String, ? super var> action) {
        $map().forEach(action);
    }

    @NotNull
    @Override
    public var $error(@NotNull String errorMessage, @NotNull ErrorType type) {
        switch (type) {
            case SYSTEM:
                return $error(new DollarException(errorMessage));
            case VALIDATION:
                return $error(new ValidationException(errorMessage));
            default:
                return $error(errorMessage);
        }
    }

    @NotNull
    public var getOrDefault(@NotNull Object key, @NotNull var defaultValue) {
        return $has(String.valueOf(key)).isTrue() ? $get(key) : defaultValue;
    }

    @NotNull
    @Override
    public var $error(@NotNull String errorMessage) {
        return $error(new Exception(errorMessage));
    }

    @Override
    public int hashCode() {
        return $().hashCode();
    }

    @NotNull
    @Override
    public var $error(@NotNull Throwable error) {
        return copy(new ImmutableList.Builder<Throwable>().add(error).build());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        Object val = $();
        if (val == null) {
            throw new NullPointerException();
        }
        Object dollarVal = $();
        if (dollarVal == null) {
            throw new NullPointerException();
        }
        if (obj instanceof var) {
            var unwrapped = ((var) obj)._unwrap();
            if (unwrapped == null) {
                return false;
            }
            Object unwrappedDollar = unwrapped.$();
            if (unwrappedDollar == null) {
                return false;
            }
            Object unwrappedVal = unwrapped.$();
            if (unwrappedVal == null) {
                return false;
            }
            return dollarVal.equals(unwrappedDollar) || (val.equals(unwrappedVal));
        } else {
            return dollarVal.equals(obj) || val.equals(obj);
        }
    }

    @NotNull
    @Override
    public var $error() {
        return $error("Unspecified Error");
    }

    @NotNull
    @Override
    public String toString() {
        return S();
    }

    @Override
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @NotNull
    public Set<String> keySet() {
        return keyStream().collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public List<String> errorTexts() {
        return errors.stream().map(Throwable::getMessage).collect(Collectors.toList());
    }

    public var merge(String key, @NotNull var value,
                     @NotNull BiFunction<? super var, ? super var, ? extends var> remappingFunction) {
        return $map().mutable().merge(key, value, remappingFunction);
    }

    @NotNull
    @Override
    public ImmutableList<Throwable> errors() {
        return errors;
    }

    @NotNull
    public var put(@NotNull String key, var value) {
        return $($get(key), value);
    }

    @Override
    public var clearErrors() {
        return DollarFactory.fromValue(ImmutableList.<Throwable>builder().build(), $());
    }

    public void putAll(Map<? extends String, ? extends var> m) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public var $fail(@NotNull Consumer<List<Throwable>> handler) {
        if (hasErrors()) {
            handler.accept(errors());
            return DollarFactory.fromValue(errors(), null);
        } else {
            return this;
        }
    }

    @NotNull
    public var putIfAbsent(String key, var value) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    public boolean replace(String key, var oldValue, var newValue) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public var replace(String key, var value) {
        throw new UnsupportedOperationException();
    }

    public void replaceAll(BiFunction<? super String, ? super var, ? extends var> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMetaAttribute(String key, String value) {
        if (meta.containsKey(key)) {
            throw new DollarException("Cannot change a metadata attribute once set.");
        }
        meta.put(key, value);
    }

    @Override
    public String getMetaAttribute(String key) {
        return meta.get(key);
    }



    @NotNull
    @Override
    public var $pipe(@NotNull String classModule) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String _src() {
        return this.src;
    }

    @Override
    public void _src(String src) {
        this.src = src;
    }


    @Override public StateMachine<ResourceState, Signal> getStateMachine() {
        return new StateMachine<ResourceState, Signal>(ResourceState.INITIAL, getDefaultStateMachineConfig());
    }
}
