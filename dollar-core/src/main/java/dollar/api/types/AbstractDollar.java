/*
 *    Copyright (c) 2014-2017 Neil Ellis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dollar.api.types;

import com.github.oxo42.stateless4j.StateMachine;
import com.github.oxo42.stateless4j.StateMachineConfig;
import com.google.common.collect.ImmutableList;
import dollar.api.DollarException;
import dollar.api.DollarStatic;
import dollar.api.MetaKey;
import dollar.api.Pipeable;
import dollar.api.Signal;
import dollar.api.SubType;
import dollar.api.TypePrediction;
import dollar.api.Value;
import dollar.api.exceptions.DollarFailureException;
import dollar.api.types.prediction.SingleValueTypePrediction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dollar.api.DollarStatic.$void;
import static dollar.api.types.meta.MetaConstants.CONSTRAINT_FINGERPRINT;

public abstract class AbstractDollar implements Value {

    @NotNull
    private final Logger logger = LoggerFactory.getLogger(getClass());


    @NotNull
    private final ConcurrentHashMap<MetaKey, Object> meta = new ConcurrentHashMap<>();

    protected AbstractDollar() {

    }

    @NotNull
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

    @NotNull
    @Override
    public Value $all() {
        return $void();
    }

    @NotNull
    @Override
    public Value $avg(boolean parallel) {
        return $sum(parallel).$divide($size());
    }

    @NotNull
    @Override
    public Value $choose(@NotNull Value map) {
        return map.$get(DollarStatic.$($S()));
    }

    @Override
    public Value $constrain(@Nullable Value constraint, SubType constraintFingerprint) {
        if ((constraint == null) || (constraintFingerprint == null)) {
            return this;
        }
        SubType thisConstraintFingerprint = constraintLabel();
        if (thisConstraintFingerprint == null) {
            meta(CONSTRAINT_FINGERPRINT, constraintFingerprint);
            return this;
        } else if (thisConstraintFingerprint.equals(constraintFingerprint)) {
            return this;
        } else {
            throw new ConstraintViolation(this, constraint, constraintFingerprint, constraintFingerprint);
        }
    }

    @NotNull
    @Override
    public Value $copy() {
        return DollarFactory.fromValue(toJavaObject());
    }

    @Override
    @NotNull
    public Value $copy(@NotNull ImmutableList<Throwable> errors) {
        return DollarFactory.fromValue(toJavaObject());
    }

    @NotNull
    @Override
    public Value $default(@NotNull Value v) {
        if (isVoid()) {
            return v;
        } else {
            return this;
        }
    }

    @NotNull
    @Override
    public Value $drain() {
        return $void();
    }

    @NotNull
    @Override
    public Value $each(@NotNull Pipeable pipe) {
        List<Value> result = new LinkedList<>();
        for (Value v : toVarList()) {
            Value res;
            try {
                res = pipe.pipe(v);
            } catch (Exception e) {
                return DollarFactory.failure(ErrorType.EXCEPTION, e, false);
            }
            result.add(res);

        }
        return DollarFactory.fromValue(result);
    }

    @NotNull
    final @Override
    public Value $fix(boolean parallel) {
        return $fix(1, parallel);
    }

    @NotNull
    @Override
    public Value $fix(int depth, boolean parallel) {
        return this;
    }

    @NotNull
    @Override
    public final Value $fixDeep(boolean parallel) {
        return $fix(Integer.MAX_VALUE, parallel);
    }

    @NotNull
    @Override
    public Value $max(boolean parallel) {
        return $stream(parallel).max(Comparable::compareTo).orElse($void());
    }

    @NotNull
    @Override
    public Value $min(boolean parallel) {
        return $stream(parallel).min(Comparable::compareTo).orElse($void());
    }

    @NotNull
    @Override
    public Value $notify() {
//        do nothing, not a reactive type
        return this;
    }

    @NotNull
    @Override
    public Value $product(boolean parallel) {
        return $stream(parallel).reduce(Value::$multiply).orElse($void());
    }

    @NotNull
    @Override
    public Value $publish(@NotNull Value lhs) {
        return this;
    }

    @NotNull
    @Override
    public Value $read(boolean blocking, boolean mutating) {
        return this;
    }

    @NotNull
    @Override
    public Value $reverse(boolean parallel) {
        throw new UnsupportedOperationException("Cannot reverse a " + type());
    }

    @NotNull
    @Override
    public Value $sort(boolean parallel) {
        return DollarFactory.fromList($stream(parallel).sorted().collect(Collectors.toList()));
    }

    @NotNull
    @Override
    public Stream<Value> $stream(boolean parallel) {
        return toVarList().stream();
    }

    @NotNull
    @Override
    public Value $sum(boolean parallel) {
        return $stream(parallel).reduce(Value::$plus).orElse($void());

    }


//    @NotNull
//    public Value $pipe(@NotNull String label, @NotNull Pipeable pipe) {
//        try {
//            return pipe.pipe(this);
//        } catch (Exception e) {
//            return DollarStatic.handleError(e, this);
//        }
//    }
//
//    @NotNull
//    public Value $pipe(@NotNull String label, @NotNull String js) {
//        SimpleScriptContext context = new SimpleScriptContext();
//        Object value;
//        try {
//            nashorn.eval("Value $=" + toJsonObject() + ";", context);
//            value = nashorn.eval(js, context);
//        } catch (Exception e) {
//            return DollarStatic.handleError(e, this);
//        }
//        return DollarFactory.fromValue(value, ImmutableList.of());
//    }
//
//    @NotNull
//    public Value $pipe(@NotNull Class<? extends Pipeable> clazz) {
//        DollarStatic.threadContext.get().passValue($copy());
//        Pipeable script = null;
//        try {
//            script = clazz.newInstance();
//        } catch (InstantiationException e) {
//            return DollarStatic.handleError(e.getCause(), this);
//        } catch (Exception e) {
//            return DollarStatic.handleError(e, this);
//        }
//        try {
//            return script.pipe(this);
//        } catch (Exception e) {
//            return DollarStatic.handleError(e, this);
//        }
//    }

    @NotNull
    @Override
    public Value $unique(boolean parallel) {
        return DollarFactory.fromSet($stream(parallel).collect(Collectors.toSet()));
    }

    @NotNull
    @Override
    public Value $unwrap() {
        return this;
    }

    @NotNull
    @Override
    public Value $write(@NotNull Value value, boolean blocking, boolean mutating) {
        return this;
    }

    @Nullable
    @Override
    public SubType constraintLabel() {
        return meta(CONSTRAINT_FINGERPRINT);
    }

    @NotNull
    @Override
    public Value debug(@NotNull Object message) {
        logger.debug(message.toString());
        return this;
    }

    @NotNull
    @Override
    public Value debug() {
        logger.debug(toString());
        return this;
    }

    @NotNull
    @Override
    public Value debugf(@NotNull String message, Object... values) {
        logger.debug(message, values);
        return this;
    }

    @Override
    public boolean decimal() {
        return false;
    }

    @Override
    public boolean dynamic() {
        return false;
    }

    @NotNull
    @Override
    public Value error(@NotNull Throwable exception) {
        logger.error(exception.getMessage(), exception);
        return this;
    }

    @NotNull
    @Override
    public Value error(@NotNull Object message) {
        logger.error(message.toString());
        return this;

    }

    @NotNull
    @Override
    public Value error() {
        logger.error(toString());
        return this;
    }

    @NotNull
    @Override
    public Value errorf(@NotNull String message, Object... values) {
        logger.error(message, values);
        return this;
    }

    @NotNull
    @Override
    public Value info(@NotNull Object message) {
        logger.info(message.toString());
        return this;
    }

    @NotNull
    @Override
    public Value info() {
        logger.info(toString());
        return this;
    }

    @NotNull
    @Override
    public Value infof(@NotNull String message, Object... values) {
        logger.info(message, values);
        return this;
    }

    @Override
    public boolean integer() {
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

    @Nullable
    @Override
    public <T> T meta(@NotNull MetaKey key) {
        return (T) meta.get(key);
    }

    @Override
    public void meta(@NotNull MetaKey key, @NotNull Object value) {
        meta.put(key, value);
    }

    @Override
    public void metaAttribute(@NotNull MetaKey key, @NotNull String value) {
        if (meta.containsKey(key)) {
            @NotNull Value result;
            throw new DollarFailureException(ErrorType.METADATA_IMMUTABLE);
        }
        meta.put(key, value);
    }

    @Nullable
    @Override
    public String metaAttribute(@NotNull MetaKey key) {
        return (String) meta.get(key);
    }

    @Override
    public boolean number() {
        return false;
    }

    @Override
    public boolean pair() {
        return false;
    }

    @NotNull
    @Override
    public TypePrediction predictType() {
        return new SingleValueTypePrediction($type());
    }

    @Override
    public boolean queue() {
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

    @Nullable
    @Override
    public double toDouble() {
        return 0.0;
    }

    @NotNull
    @Override
    public long toLong() {
        return 0L;
    }

    @NotNull
    @Override
    public InputStream toStream() {
        return new ByteArrayInputStream($serialized().getBytes());
    }

    @Override
    public boolean uri() {
        return false;
    }

    @NotNull
    @Override
    public Value $create() {
        getStateMachine().fire(Signal.CREATE);
        return this;
    }

    @NotNull
    @Override
    public Value $destroy() {
        getStateMachine().fire(Signal.DESTROY);
        return this;
    }

    @NotNull
    @Override
    public Value $pause() {
        getStateMachine().fire(Signal.PAUSE);
        return this;
    }

    @Override
    public void $signal(@NotNull Signal signal) {
        getStateMachine().fire(signal);
    }

    @NotNull
    @Override
    public Value $start() {
        getStateMachine().fire(Signal.START);
        return this;
    }

    @NotNull
    @Override
    public Value $state() {
        return DollarStatic.$(getStateMachine().getState().toString());
    }

    @NotNull
    @Override
    public Value $stop() {
        getStateMachine().fire(Signal.STOP);
        return this;
    }

    @NotNull
    @Override
    public Value $unpause() {
        getStateMachine().fire(Signal.UNPAUSE);
        return this;
    }

    @NotNull
    @Override
    public StateMachine<ResourceState, Signal> getStateMachine() {
        return new StateMachine<>(ResourceState.INITIAL, getDefaultStateMachineConfig());
    }

    @NotNull
    private String hash(@NotNull byte[] bytes) {
        MessageDigest md;
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
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    @Override
    public int hashCode() {
        Object o = toJavaObject();
        if (o != null) {
            return o.hashCode();
        } else {
            return 0;
        }
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
        if (obj instanceof Value) {
            Value unwrapped = ((Value) obj).$unwrap();
            if (unwrapped == null) {
                return false;
            }
            Object unwrappedDollar = unwrapped.toJavaObject();
            if (unwrappedDollar == null) {
                return false;
            }
            Object unwrappedVal = unwrapped.toJavaObject();
            return (unwrappedVal != null) && (dollarVal.equals(unwrappedDollar) || (val.equals(unwrappedVal)));
        } else {
            return dollarVal.equals(obj) || val.equals(obj);
        }
    }

    @NotNull
    @Override
    public String toString() {
        return toHumanString();
    }
}
