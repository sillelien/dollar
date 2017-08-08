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
import dollar.internal.runtime.script.api.exceptions.DollarParserError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static dollar.internal.runtime.script.DollarScriptSupport.currentScope;

public class DollarSource extends DollarLambda {
    @Nullable
    public static final TypeLearner typeLearner = Plugins.sharedInstance(TypeLearner.class);

    private static final Logger log = LoggerFactory.getLogger("DollarSource");


    private static final List<String> nonScopeOperations = Arrays.asList("$listen", "$notify",
                                                                         "dynamic", "_constrain");
    private final SourceSegment source;
    private List<var> inputs;
    @Nullable
    private String operation;
    private volatile TypePrediction prediction;
    private DollarParser parser;

    public DollarSource(Pipeable lambda,
                        @Nullable Scope scope,
                        SourceSegment source,
                        @NotNull List<var> inputs,
                        @Nullable String operation,
                        DollarParser parser) {
        super(vars -> lambda.pipe(vars));
        this.parser = parser;
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
//        if(scope == null) {
//            this.meta.put("scope", currentScope());
//        }
        if (scope != null) {
            this.meta.put("scope", scope);

        }
//        setScopes(inputs);

    }

    private void correctParentScope(@NotNull Scope scope, @NotNull var input) {
        Scope inputScope = (Scope) input.getMetaObject("scope");
        if (inputScope != null) {
            if (inputScope.getParent() == null && !inputScope.isRoot()) {
                if (DollarStatic.getConfig().debugScope()) {
                    log.info("Correcting scope " + inputScope + " to parent " + scope);
                }
                inputScope.setParent(scope);
            } else {
                if (scope.hasParent(inputScope)) {
                    throw new DollarParserError(input.getMetaAttribute(
                            "operation") + ": Child has " +
                                                        "parent's" +
                                                        " scope " +
                                                        source
                                                                .getSourceMessage() + " offending scope was " + inputScope + " which is a parent of " + scope);
                } else {
                    if (!inputScope.isRoot()) {
                        log.info("Setting parent scope of " + inputScope + " to " + scope);
                        inputScope.setParent(scope);
                    } else {
                        return;
                    }
                }

            }
        } else {
            input.setMetaObject("scope", scope);
        }
        List<var> children = (List<var>) input.getMetaObject("children");
        if (children != null) {
            for (var child : children) {
                correctParentScope(scope, child);
            }
        }
    }

    private void setScopes(List<var> inputs) {

        for (var input : inputs) {
            correctScope(input);
        }


    }

    private void correctScope(@NotNull var input) {
        Scope scope = getScope();
        if (scope == null) {
            scope = currentScope();
        }
        if (input.getMetaObject("scope") != scope) {
            correctParentScope(scope, input);
        }
        input.setMetaObject("parentScope", scope);
    }

    private Scope getScope() {
        return (Scope) meta.get("scope");
    }

//    /**
//     * Recursively correct missing scopes to this scope
//     *
//     * @param scope
//     * @param input
//     */
//    private void setMissingScope(Scope scope, @NotNull var input) {
//        if (input.getMetaObject("scope") == null) {
//            log.info("Adding missing child scope, now " + scope);
//            input.setMetaObject("scope", scope);
//            List<var> children = (List<var>) input.getMetaObject("children");
//            if (children != null) {
//                for (var child : children) {
//                    setMissingScope(scope, child);
//                }
//            }
//        }
//
//    }

    @Nullable
    @Override
    public Object invoke(Object proxy, @NotNull Method method, Object[] args) throws Throwable {
        try {
            if (Objects.equals(method.getName(), "_source")) {
                return source;
            }
            if (Objects.equals(method.getName(), "dynamic")) {
                return true;
            }

            if (Objects.equals(method.getName(), "_constrain")) {
                return _constrain((var) proxy, (var) args[0], String.valueOf(args[1]));
            }

            if (Objects.equals(method.getName(), "_predictType")) {
                if (this.prediction == null) {
                    this.prediction = typeLearner.predict(operation, source, inputs);
                }
                return this.prediction;
            }
            if (method.getName().startsWith("_fixDeep")) {
//                Thread.dumpStack();
            }
            final Object result;
//            if (method.getName().startsWith("_fix")) {
//                log.debug("Fixing "+operation);
//                if (meta.get("scope") == null) {
//                    meta.put("scope",currentScope());
//                    log.info("Added missing scope to "+source.getSourceMessage());
//                }
//
//            }
            //Some operations do not require a valid scope
            if ((!method.getName().startsWith("_fix") && (nonScopeOperations.contains(
                    method.getName()) || method.getDeclaringClass().equals(
                    MetadataAware.class) || method.getDeclaringClass().equals(
                    VarInternal.class)))) {
                //This method does not require a valid scope for execution
                try {
                    if (DollarStatic.getConfig().debugScope()) {
//                        log.debug("EXE (ignored): " + method);
                    }
                    result = super.invoke(proxy, method, args);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    throw new DollarException(throwable);
                }
            } else {
                //This method does require a scope
                Scope useScope = getScope();
                if (useScope == null) {
                    useScope = currentScope();
                } else {
                    useScope = useScope.copy();
                }
//                setScopes(inputs);
                result = DollarScriptSupport.inScope(true, useScope, newScope -> {
                    if (DollarStatic.getConfig().debugScope()) {
                        log.info(
                                "EXE: " + method.getName() + " for " + source.getShortSourceMessage());
                    }
                    try {
                        return super.invoke(proxy, method, args);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        throw new DollarException(throwable);
                    }
                });
                if (result instanceof var) {
                    if (((var) result).getMetaObject("scope") == null) {
                        log.debug("ATTACHING MISSING SCOPE FOR RESULT, SCOPE is " + useScope);
                        ((var) result).setMetaObject("scope", useScope);
                    }
                }
            }

            if (method.getName().startsWith("_fixDeep")) {
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
