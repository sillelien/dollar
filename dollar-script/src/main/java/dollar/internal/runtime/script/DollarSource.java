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
import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.TypePrediction;
import com.sillelien.dollar.api.plugin.Plugins;
import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.script.TypeLearner;
import com.sillelien.dollar.api.types.DollarLambda;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.api.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

public class DollarSource extends DollarLambda {
    @Nullable
    public static final TypeLearner typeLearner = Plugins.sharedInstance(TypeLearner.class);
    @Nullable
    private final Scope scope;
    private final SourceSegment source;
    private List<var> inputs;
    @Nullable
    private String operation;
    private volatile TypePrediction prediction;

    public DollarSource(Pipeable lambda, @Nullable Scope scope, SourceSegment source, List<var> inputs,
                        @Nullable String operation) {
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

    public DollarSource(Pipeable lambda, @Nullable Scope scope, SourceSegment source, boolean fixable, List<var> inputs,
                        @Nullable String operation) {
        super(lambda, fixable);
        this.inputs = inputs;
        this.operation = operation;
        if (scope == null) {
            throw new NullPointerException();
        }
        this.scope = scope;
        this.source = source;
    }

    @Nullable
    @Override
    public Object invoke(Object proxy, @NotNull Method method, Object[] args) throws Throwable {
        try {
            if (Objects.equals(method.getName(), "_source")) {
                return source;
            }
            if (Objects.equals(method.getName(), "_constrain")) {
                return _constrain((var)proxy, (var)args[0], String.valueOf(args[1]));
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
