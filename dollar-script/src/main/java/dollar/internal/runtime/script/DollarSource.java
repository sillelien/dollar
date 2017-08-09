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

package dollar.internal.runtime.script;

import com.sillelien.dollar.api.DollarException;
import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.MetadataAware;
import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.TypePrediction;
import com.sillelien.dollar.api.VarInternal;
import com.sillelien.dollar.api.plugin.Plugins;
import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.script.TypeLearner;
import com.sillelien.dollar.api.types.DollarLambda;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static dollar.internal.runtime.script.DollarScriptSupport.currentScope;

public class DollarSource extends DollarLambda {
    @NotNull
    public static final TypeLearner typeLearner = Plugins.sharedInstance(TypeLearner.class);

    @NotNull
    private static final Logger log = LoggerFactory.getLogger("DollarSource");


    @NotNull
    private static final List<String> nonScopeOperations = Arrays.asList(
            "dynamic", "_constrain");
    @NotNull
    private final SourceSegment source;
    @NotNull
    private List<var> inputs;
    @Nullable
    private String operation;
    private volatile TypePrediction prediction;
    @NotNull
    private DollarParser parser;
    private boolean newScope;
    @NotNull
    private String id;

    public DollarSource(@NotNull Pipeable lambda,
                        @NotNull SourceSegment source,
                        @NotNull List<var> inputs,
                        @NotNull String operation,
                        @NotNull DollarParser parser, boolean newScope, @NotNull String id) {
        super(lambda);
        this.parser = parser;
        this.newScope = newScope;
        this.id = id;
        if (operation == null) {
            throw new NullPointerException();
        }
        if (inputs == null) {
            throw new NullPointerException();
        }
        this.inputs = inputs;
        this.operation = operation;
        this.source = source;
        this.meta.put("operation", operation);
        this.meta.put("id", id);
    }


    @Nullable
    @Override
    public Object invoke(Object proxy, @NotNull Method method, @Nullable Object[] args) throws Throwable {
        try {
            if (Objects.equals(method.getName(), "_source")) {
                return source;
            }
            if (Objects.equals(method.getName(), "dynamic")) {
                return true;
            }

            if (Objects.equals(method.getName(), "_constrain")) {
                if (args != null) {
                    return _constrain((var) proxy, (var) args[0], String.valueOf(args[1]));
                }
            }

            if (Objects.equals(method.getName(), "_predictType")) {
                if (this.prediction == null) {
                    this.prediction = typeLearner.predict(operation, source, inputs);
                }
                return this.prediction;
            }

            final Object result;
            //Some operations do not require a valid scope
            if ((!method.getName().startsWith("_fix") && (nonScopeOperations.contains(
                    method.getName()) || method.getDeclaringClass().equals(
                    MetadataAware.class) || method.getDeclaringClass().equals(
                    VarInternal.class)))) {
                //This method does not require a valid scope for execution
                try {
                    result = super.invoke(proxy, method, args);
                } catch (Throwable throwable) {
                    log.debug(throwable.getMessage(), throwable);
                    throw new DollarException(throwable);
                }
            } else {
                //This method does require a scope
                Scope useScope;

                Object attachedScopes = meta.get("scopes");
                if (attachedScopes != null) {
                    List<Scope> scopes = new ArrayList<>((List<Scope>) attachedScopes);
                    for (Scope scope : scopes) {
                        DollarScriptSupport.pushScope(scope);
                    }
                }
                try {
                    if (newScope) {
                        useScope = new ScriptScope(currentScope(), operation, false);
                    } else {
                        useScope = currentScope();
                    }

                    result = DollarScriptSupport.inScope(true, useScope, newScope -> {
                        if (DollarStatic.getConfig().debugScope()) {
                            log.info(
                                    "EXE: " + operation + " " + method.getName() + " for " + source.getShortSourceMessage());
                        }
                        try {
                            return super.invoke(proxy, method, args);
                        } catch (Throwable throwable) {
                            log.debug(throwable.getMessage(), throwable);
                            throw new DollarException(throwable);
                        }
                    });
                } finally {
                    if (attachedScopes != null) {
                        List<Scope> scopes = new ArrayList<Scope>((List<Scope>) attachedScopes);
                        Collections.reverse(scopes);
                        for (Scope scope : scopes) {
                            DollarScriptSupport.popScope(scope);
                        }
                    }
                }

            }


            if (method.getName().startsWith("_fixDeep") && result != null && result instanceof var) {
                typeLearner.learn(operation, source, inputs, ((var) result).$type());
            }
            return result;
        } catch (AssertionError e) {
            log.warn(e.getMessage(), e);
            return parser.getErrorHandler().handle(currentScope(), source, e);
        } catch (DollarException e) {
            log.warn(e.getMessage(), e);
            return parser.getErrorHandler().handle(currentScope(), source, e);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return parser.getErrorHandler().handle(currentScope(), source, e);
        }
    }


}
