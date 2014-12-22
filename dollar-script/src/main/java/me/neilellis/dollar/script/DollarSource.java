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

package me.neilellis.dollar.script;

import me.neilellis.dollar.DollarException;
import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.TypePrediction;
import me.neilellis.dollar.plugin.Plugins;
import me.neilellis.dollar.types.DollarLambda;
import me.neilellis.dollar.var;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarSource extends DollarLambda {
    public static TypeLearner typeLearner = Plugins.sharedInstance(TypeLearner.class);
    private final Scope scope;
    private final Source source;
    private List<var> inputs;
    private String operation;
    private volatile TypePrediction prediction;

    public DollarSource(Pipeable lambda, Scope scope, Source source, List<var> inputs, String operation) {
        super(lambda);
        if (operation == null) {
            throw new NullPointerException();
        }

        this.inputs = inputs;
        this.operation = operation;
        if (scope == null) {
            throw new NullPointerException();
        }
        this.scope = scope;
        this.source = source;
    }

    public DollarSource(Pipeable lambda, Scope scope, Source source, boolean fixable, List<var> inputs,
                        String operation) {
        super(lambda, fixable);
        this.inputs = inputs;
        this.operation = operation;
        if (scope == null) {
            throw new NullPointerException();
        }
        this.scope = scope;
        this.source = source;
    }

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (Objects.equals(method.getName(), "_source")) {
                return source;
            }
            if (Objects.equals(method.getName(), "_predictType")) {
                if (this.prediction == null) {
                    this.prediction = typeLearner.predict(operation, source, inputs);
                }
                return this.prediction;
            }
            final Object result = super.invoke(proxy, method, args);
            if (method.getName().startsWith("_fixDeep")) {
                typeLearner.learn(operation, source, inputs, ((var) result).$type());
            }
            return result;
        } catch (AssertionError e) {
            return scope.getDollarParser().getErrorHandler().handle(scope, source, e);
        } catch (DollarException e) {
            return scope.getDollarParser().getErrorHandler().handle(scope, source, e);
        } catch (Exception e) {
            return scope.getDollarParser().getErrorHandler().handle(scope, source, e);
        }
    }


}
