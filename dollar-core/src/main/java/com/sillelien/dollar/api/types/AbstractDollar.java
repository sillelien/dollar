/*
 * Copyright (c) 2014-2015 Neil Ellis
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

package com.sillelien.dollar.api.types;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.sillelien.dollar.api.*;
import com.sillelien.dollar.api.collections.ImmutableList;
import com.sillelien.dollar.api.json.JsonArray;
import com.sillelien.dollar.api.json.JsonObject;
import com.sillelien.dollar.api.types.prediction.SingleValueTypePrediction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    AbstractDollar(@NotNull ImmutableList<Throwable> errors) {
        this.errors = errors;
    }

    @NotNull @Override
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


    @NotNull @Override
    public var $choose(@NotNull var map) {
        return map.$($S());
    }

    @Override
    public var $each(@NotNull Pipeable pipe) {
        List<var> result = new LinkedList<>();
        for (var v : $list()) {
            var res = null;
            try {
                res = pipe.pipe(v);
            } catch (Exception e) {
                return DollarFactory.failure(ErrorType.EXCEPTION, e, false);
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

    @NotNull static StateMachineConfig<ResourceState, Signal> getDefaultStateMachineConfig() {
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
    public var $default(@NotNull var v) {
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
            nashorn.eval("var $=" + toJsonObject().toString() + ";", context);
            value = nashorn.eval(js, context);
        } catch (Exception e) {
            return DollarStatic.handleError(e, this);
        }
        return DollarFactory.fromValue(value, ImmutableList.of());
    }

    @NotNull
    @Override
    public var $pipe(@NotNull Class<? extends Pipeable> clazz) {
        DollarStatic.threadContext.get().setPassValue(this._copy());
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
    public var _copy() {
        return DollarFactory.fromValue(toJavaObject(), ImmutableList.copyOf(errors()));
    }

    @NotNull
    public var _copy(@NotNull ImmutableList<Throwable> errors) {
        return DollarFactory.fromValue(toJavaObject(), ImmutableList.copyOf(errors(), errors));
    }

    @NotNull final @Override public var _fix(boolean parallel) {
        return _fix(1, parallel);
    }

    @NotNull @Override public var _fix(int depth, boolean parallel) {
        return this;
    }

    @NotNull @Override public final var _fixDeep(boolean parallel) {
        return _fix(Integer.MAX_VALUE, parallel);
    }

    @NotNull @Override public TypePrediction _predictType() {
        return new SingleValueTypePrediction($type());
    }


    @NotNull
    @Override
    public var _unwrap() {
        return this;
    }

    public void clear() {
        DollarFactory.failure(ErrorType.INVALID_OPERATION);
    }

    @NotNull @Override
    public var debug(@NotNull Object message) {
        logger.debug(message.toString());
        return this;
    }

    @NotNull @Override
    public var debug() {
        logger.debug(this.toString());
        return this;
    }

    @NotNull @Override
    public var debugf(String message, Object... values) {
        logger.debug(message, values);
        return this;
    }

    @NotNull @Override
    public var error(@NotNull Throwable exception) {
        logger.error(exception.getMessage(), exception);
        return this;
    }

    @NotNull @Override
    public var error(@NotNull Object message) {
        logger.error(message.toString());
        return this;

    }

    @NotNull @Override
    public var error() {
        logger.error(this.toString());
        return this;
    }

    @NotNull @Override
    public var errorf(String message, Object... values) {
        logger.error(message, values);
        return this;
    }

    @NotNull @Override
    public var info(@NotNull Object message) {
        logger.info(message.toString());
        return this;
    }

    @NotNull @Override
    public var info() {
        logger.info(this.toString());
        return this;
    }

    @NotNull @Override
    public var infof(String message, Object... values) {
        logger.info(message, values);
        return this;
    }

    @Override
    public boolean dynamic() {
        return false;
    }

    @Override
    public boolean list() {
        return false;
    }

    @Override
    public boolean map() {
        return false;
    }

    @Override
    public boolean number() {
        return false;
    }

    @Override
    public boolean decimal() {
        return false;
    }

    @Override
    public boolean integer() {
        return false;
    }

    @Override public boolean pair() {
        return false;
    }

    @Override
    public boolean singleValue() {
        return false;
    }

    @Override
    public boolean string() {
        return false;
    }

    @NotNull @Override
    public InputStream toStream() {
        return new ByteArrayInputStream($serialized().getBytes());
    }

    @Override
    public boolean uri() {
        return false;
    }

    @Override
    public String getMetaAttribute(@NotNull String key) {
        return meta.get(key);
    }

    @Override
    public void setMetaAttribute(@NotNull String key, @NotNull String value) {
        if (meta.containsKey(key)) {
            DollarFactory.failure(ErrorType.METADATA_IMMUTABLE);
            return;

        }
        meta.put(key, value);
    }

    @Override
    public int hashCode() {
        return toJavaObject().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        Object val = toJavaObject();
        if (val == null) {
            return false;
        }
        Object dollarVal = toJavaObject();
        if (dollarVal == null) {
            return false;
        }
        if (obj instanceof var) {
            var unwrapped = ((var) obj)._unwrap();
            if (unwrapped == null) {
                return false;
            }
            Object unwrappedDollar = unwrapped.toJavaObject();
            if (unwrappedDollar == null) {
                return false;
            }
            Object unwrappedVal = unwrapped.toJavaObject();
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
        return toHumanString();
    }

    @Nullable @Override
    public Double toDouble() {
        return 0.0;
    }

    @Override
    public Long toLong() {
        return 0L;
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
                if (!DollarStatic.getConfig().production()) {
                    errorJson.putString("stack", Arrays.toString(error.getStackTrace()));
                } else {
                    errorJson.putString("hash",
                                        hash(Arrays.toString(error.getStackTrace()).getBytes()));
                }
                errorArray.addObject(errorJson);
            }
            json.putArray("errors", errorArray);
        }
        return DollarFactory.fromValue(json);
    }

    @NotNull String hash(@NotNull byte[] bytes) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new DollarException(e);
        }

        md.update(bytes);
        byte[] digest = md.digest();
        StringBuilder hexString = new StringBuilder();

        for (byte aDigest : digest) {
            String hex = Integer.toHexString(0xff & aDigest);
            if (hex.length() == 1) { hexString.append('0'); }
            hexString.append(hex);
        }

        return hexString.toString();
    }


    @NotNull
    @Override
    public var $error(@NotNull String errorMessage, @NotNull ErrorType type) {
        return _copy();
    }


    @NotNull
    @Override
    public var $error(@NotNull String errorMessage) {
        return DollarFactory.failure(ErrorType.VALIDATION, errorMessage, true);
    }


    @NotNull
    @Override
    public var $error(@NotNull Throwable error) {
        return DollarFactory.failure(error);
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
        return DollarFactory.fromValue(toJavaObject(), ImmutableList.of());
    }


    @NotNull
    @Override
    public var $fail(@NotNull Consumer<ImmutableList<Throwable>> handler) {
        if (hasErrors()) {
            handler.accept(errors());
            return DollarFactory.fromValue(null, errors());
        } else {
            return this;
        }
    }

}
