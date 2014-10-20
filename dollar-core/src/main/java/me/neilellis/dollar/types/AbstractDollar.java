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
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import me.neilellis.dollar.*;
import me.neilellis.dollar.exceptions.ValidationException;
import me.neilellis.dollar.json.JsonArray;
import me.neilellis.dollar.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

import static com.jayway.restassured.RestAssured.given;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public abstract class AbstractDollar implements var {

    private static
    @NotNull
    ScriptEngine nashorn = new ScriptEngineManager().getEngineByName("nashorn");
    private final
    @NotNull
    ImmutableList<Throwable> errors;

    protected AbstractDollar(@NotNull List<Throwable> errors) {
        this.errors = new ImmutableList.Builder<Throwable>().addAll(errors).build();
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
        Object val = val();
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
            Object unwrappedVal = unwrapped.val();
            if (unwrappedVal == null) {
                return false;
            }
            return dollarVal.equals(unwrappedDollar) || (val.equals(unwrappedVal));
        } else {
            return dollarVal.equals(obj) || val.equals(obj);
        }
    }

    @Override
    public boolean isEmpty() {
        return size() > 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return $has(String.valueOf(key));
    }

    @NotNull
    @Override
    public var put(@NotNull String key, var value) {
        return $(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends var> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return keyStream().collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public var eval(@NotNull DollarEval lambda) {
        return eval("anon", lambda);
    }

    @NotNull
    @Override
    public Collection<var> values() {
        return toList();
    }

    @NotNull
    @Override
    public Set<Entry<String, var>> entrySet() {
        return kvStream().collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public var getOrDefault(@NotNull Object key, @NotNull var defaultValue) {
        return $has(String.valueOf(key)) ? get(key) : defaultValue;
    }

    @Override
    public void forEach(@NotNull BiConsumer<? super String, ? super var> action) {
        $map().forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super String, ? super var, ? extends var> function) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public var putIfAbsent(String key, var value) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public var eval(String label, @NotNull DollarEval lambda) {
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

    @NotNull
    @Override
    public var replace(String key, var value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var computeIfAbsent(String key, @NotNull Function<? super String, ? extends var> mappingFunction) {
        return $map().computeIfAbsent(key, mappingFunction);
    }

    @Override
    public var computeIfPresent(String key,
                                @NotNull BiFunction<? super String, ? super var, ? extends var> remappingFunction) {
        return $map().computeIfPresent(key, remappingFunction);
    }

    @Override
    public var compute(String key, @NotNull BiFunction<? super String, ? super var, ? extends var> remappingFunction) {
        return $map().compute(key, remappingFunction);
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
    public var merge(String key, @NotNull var value,
                     @NotNull BiFunction<? super var, ? super var, ? extends var> remappingFunction) {
        return $map().mutable().merge(key, value, remappingFunction);
    }


    @NotNull
    @Override
    public var $pipe(@NotNull String js) {
        return $pipe("anon", js);
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
    @Override
    public var $load(@NotNull String location) {
        return DollarStatic.context().getStore().get(location);
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
    public String $mimeType() {
        return "text/plain";
    }


    @NotNull
    @Override
    public var $pipe(@NotNull Class<? extends Script> clazz) {
        DollarStatic.threadContext.get().setPassValue(this.$copy());
        Script script = null;
        try {
            script = clazz.newInstance();
        } catch (InstantiationException e) {
            return DollarStatic.handleError(e.getCause(), this);
        } catch (Exception e) {
            return DollarStatic.handleError(e, this);
        }
        return script.result();
    }


    @NotNull
    @Override
    public var $pop(@NotNull String location, int timeoutInMillis) {
        return DollarStatic.context().getStore().pop(location, timeoutInMillis);

    }


    @NotNull
    @Override
    public var $pub(@NotNull String... locations) {
        DollarStatic.context().getPubsub().pub(this, locations);
        return this;
    }


    @NotNull
    @Override
    public var $push(@NotNull String location) {
        DollarStatic.context().getStore().push(location, this);
        return this;
    }


    @NotNull
    @Override
    public var $save(@NotNull String location, int expiryInMilliseconds) {
        DollarStatic.context().getStore().set(location, this, expiryInMilliseconds);
        return this;
    }


    @NotNull
    @Override
    public var $save(@NotNull String location) {
        DollarStatic.context().getStore().set(location, this);
        return this;
    }


    public Stream<Entry<String, var>> kvStream() {
        return $map().entrySet().stream();

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


    @NotNull
    @Override
    public var copy(@NotNull ImmutableList<Throwable> errors) {
        return DollarFactory.fromValue(new ImmutableList.Builder<Throwable>().addAll(errors()).addAll(errors).build(),
                                       val());
    }


    @NotNull
    @Override
    public var $copy() {
        return DollarFactory.fromValue(new ImmutableList.Builder<Throwable>().addAll(errors()).build(), val());
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

    @Nullable
    @Override
    public Double D() {
        return null;
    }

    @Nullable
    @Override
    public Long L() {
        return null;
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

    @Override
    public var clearErrors() {
        return DollarFactory.fromValue(ImmutableList.<Throwable>builder().build(), val());
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

}
