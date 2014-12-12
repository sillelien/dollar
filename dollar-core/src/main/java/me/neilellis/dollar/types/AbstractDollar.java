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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public abstract class AbstractDollar implements var {

    private static
    @NotNull final
    ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final
    @NotNull
    ImmutableList<Throwable> errors;
    private final ConcurrentHashMap<String, String> meta = new ConcurrentHashMap<>();
    private String src;


    AbstractDollar(@NotNull List<Throwable> errors) {
        this.errors = new ImmutableList.Builder<Throwable>().addAll(errors).build();
    }

    @Override
    public var $all() {
        return DollarStatic.$void();
    }

    @Override
    public var $write(var value, boolean blocking, boolean mutating) {
        return this;
    }

    @Override
    public var $drain() {
        return DollarStatic.$void();
    }

    @Override
    public var $notify() {
//        do nothing, not a reactive type
        return this;
    }

    @Override
    public var $read(boolean blocking, boolean mutating) {
        return this;
    }

    @Override
    public var $publish(var lhs) {
        return this;
    }



    @Override
    public var $choose(var map) {
        return map.$($S());
    }

    @Override
    public var $each(Pipeable pipe) {
        List<var> result = new LinkedList<>();
        for (var v : $list()) {
            var res = null;
            try {
                res = pipe.pipe(v);
            } catch (Exception e) {
                return DollarFactory.failure(FailureType.EXCEPTION, e);
            }
            result.add(res);

        }
        final var resultvar = DollarFactory.fromValue(result);
        return resultvar;
    }

    @NotNull @Override public var $create() {
        getStateMachine().fire(Signal.CREATE);
        return this;
    }

    @NotNull @Override public var $destroy() {
        getStateMachine().fire(Signal.DESTROY);
        return this;
    }

    @NotNull @Override public var $pause() {
        getStateMachine().fire(Signal.PAUSE);
        return this;
    }

    @Override public void $signal(@NotNull Signal signal) {
        getStateMachine().fire(signal);
    }

    @NotNull @Override public var $start() {
        getStateMachine().fire(Signal.START);
        return this;
    }

    @NotNull @Override public var $state() {
        return DollarStatic.$(getStateMachine().getState().toString());
    }

    @NotNull @Override public var $stop() {
        getStateMachine().fire(Signal.STOP);
        return this;
    }

    @NotNull @Override public var $unpause() {
        getStateMachine().fire(Signal.UNPAUSE);
        return this;
    }

    @NotNull @Override public StateMachine<ResourceState, Signal> getStateMachine() {
        return new StateMachine<>(ResourceState.INITIAL, getDefaultStateMachineConfig());
    }

    static StateMachineConfig<ResourceState, Signal> getDefaultStateMachineConfig() {
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

    @NotNull @Override
    public var $default(var v) {
        if (isVoid()) {
            return v;
        } else {
            return this;
        }
    }



    @NotNull
    @Override
    public Stream<var> $stream(boolean parallel) {
        return $list().stream();
    }

    @NotNull
    @Override
    public var $eval(@NotNull String js) {
        return $pipe("anon", js);
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
        return DollarFactory.fromValue(value, ImmutableList.of());
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

    @NotNull
    @Override
    public var $copy() {
        return DollarFactory.fromValue($(), new ImmutableList.Builder<Throwable>().addAll(errors()).build());
    }

    @NotNull final @Override public var _fix(boolean parallel) {
        return _fix(1, parallel);
    }

    @Override public var _fix(int depth, boolean parallel) {
        return this;
    }

    @Override public final var _fixDeep(boolean parallel) {
        return _fix(Integer.MAX_VALUE, parallel);
    }

    @Override
    public void _src(String src) {
        this.src = src;
    }

    @Override
    public String _src() {
        return this.src;
    }

    @NotNull
    @Override
    public var _unwrap() {
        return this;
    }

    @NotNull
    @Override
    public var copy(@NotNull ImmutableList<Throwable> errors) {
        return DollarFactory.fromValue($(),
                                       new ImmutableList.Builder<Throwable>().addAll(errors()).addAll(errors).build()
        );
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

    @Override public boolean isPair() {
        return false;
    }

    @Override
    public boolean isSingleValue() {
        return false;
    }

    @Override
    public boolean isString() {
        return false;
    }

    @Override
    public boolean isUri() {
        return false;
    }

    @NotNull @Override
    public InputStream toStream() {
        return new ByteArrayInputStream($serialized().getBytes());
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

    public void clear() {
        DollarFactory.failure(FailureType.INVALID_OPERATION);
    }

    @Override
    public var debugf(String message, Object... values) {
        logger.debug(message, values);
        return this;
    }

    @Override
    public var debug(Object message) {
        logger.debug(message.toString());
        return this;
    }

    @Override
    public var debug() {
        logger.debug(this.toString());
        return this;
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

    @Override
    public int hashCode() {
        return $().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        Object val = $();
        if (val == null) {
            return false;
        }
        Object dollarVal = $();
        if (dollarVal == null) {
            return false;
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
    public String toString() {
        return S();
    }

    @NotNull
    public Stream<Map.Entry<String, var>> kvStream() {
        return $map().entrySet().stream();

    }

    @Override
    public void setMetaAttribute(String key, String value) {
        if (meta.containsKey(key)) {
            DollarFactory.failure(FailureType.METADATA_IMMUTABLE);
            return;

        }
        meta.put(key, value);
    }

    @Override
    public String getMetaAttribute(String key) {
        return meta.get(key);
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
    @Override
    public var $error(@NotNull String errorMessage) {
        return $error(new Exception(errorMessage));
    }


    @NotNull
    @Override
    public var $error(@NotNull Throwable error) {
        return copy(new ImmutableList.Builder<Throwable>().add(error).build());
    }



    @NotNull
    @Override
    public var $error() {
        return $error("Unspecified Error");
    }



    @Override
    public boolean hasErrors() {
        return !errors.isEmpty();
    }



    @NotNull
    @Override
    public List<String> errorTexts() {
        return errors.stream().map(Throwable::getMessage).collect(Collectors.toList());
    }



    @NotNull
    @Override
    public ImmutableList<Throwable> errors() {
        return errors;
    }


    @NotNull @Override
    public var clearErrors() {
        return DollarFactory.fromValue($(), ImmutableList.<Throwable>builder().build());
    }


    @NotNull
    @Override
    public var $fail(@NotNull Consumer<List<Throwable>> handler) {
        if (hasErrors()) {
            handler.accept(errors());
            return DollarFactory.fromValue(null, errors());
        } else {
            return this;
        }
    }

}
