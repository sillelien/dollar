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

package dollar.internal.runtime.script.operators;

import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.TypePrediction;
import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.SourceSegmentValue;
import dollar.internal.runtime.script.api.Scope;
import dollar.internal.runtime.script.api.exceptions.DollarParserException;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.jparsec.functors.Map;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;

import static com.sillelien.dollar.api.DollarStatic.*;

public class AssignmentOperator implements Map<Token, Map<? super var, ? extends var>> {
    private final Scope scope;
    private final boolean pure;

    public AssignmentOperator(Scope scope, boolean push, boolean pure) {
        this.scope = scope;
        this.pure = pure;
    }

    @Nullable
    public Map<? super var, ? extends var> map(@NotNull Token token) {
        Type type;
        Object[] objects = (Object[]) token.value();
        var constraint;
        final String constraintSource;
        if (objects[3] instanceof var) {
            constraintSource = ((var) objects[3])._source().getSourceSegment();
        } else {
            constraintSource = null;
        }
        final SourceSegmentValue source = new SourceSegmentValue(scope, token);
        if (objects[2] != null) {
            type = Type.valueOf(objects[2].toString().toUpperCase());
            constraint = DollarScriptSupport.wrapLambda(source, scope, i -> {
                return $(scope.getDollarParser().currentScope().getParameter("it")
                        .is(type) &&
                        (objects[3] == null || ((var) objects[3]).isTrue()));
            }, Arrays.asList(), "constraint");
        } else {
            type = null;
            constraint = (var) objects[3];

        }
        boolean constant;
        boolean isVolatile;
        final Object mutability = objects[1];
        constant = mutability != null && mutability.toString().equals("const");
        isVolatile = mutability != null && mutability.toString().equals("volatile");
        if (((var) objects[4]).getMetaAttribute("__builtin") != null) {
            throw new DollarParserException("The variable '" +
                    objects[4] +
                    "' cannot be assigned as this name is the name of a builtin function.");
        }
        final String varName = objects[4].toString();

        return new Map<var, var>() {

            public var map(@NotNull var rhs) {
                final TypePrediction prediction = rhs._predictType();
                if (type != null && prediction != null) {
                    final Double probability = prediction.probability(type);
                    if (probability < 0.5 && !prediction.empty()) {
                        System.err.println("Type assertion may fail, expected " +
                                type +
                                " most likely type is " +
                                prediction.probableType() +
                                " (" +
                                (int) (prediction.probability(prediction.probableType()) * 100) +
                                "%) at " +
                                source.getSourceMessage());
                    }
                }

                final String operator = objects[5].toString();
                if (operator.equals("?=") || operator.equals("*=")) {
                    final String useSource;
                    var useConstraint;
                    if (constraint != null) {
                        useConstraint = constraint;
                        useSource= constraintSource;
                    } else {
                        useConstraint = scope.getConstraint(varName);
                        useSource = scope.getConstraintSource(varName);
                    }
                    if (operator.equals("?=")) {
                        scope.set(varName, $void(), false, null, useSource, isVolatile, false, pure);
                        Callable<var> callable = () -> $($(rhs.$listen(
                                i -> scope.set(varName, fix(i[0], false), false,
                                        useConstraint, useSource, isVolatile, false, pure))));
                        return DollarScriptSupport.toLambda(scope, callable, source, Arrays.asList(constrain(scope, rhs, constraint, useSource)),
                                "listen-assign");

                    } else if (operator.equals("*=")) {
                        scope.set(varName, $void(), false, null, useSource, true, true, pure);
                        Callable<var> callable = () -> $(rhs.$subscribe(
                                i -> scope.set(varName, fix(i[0], false), false,
                                        useConstraint, useSource, true, false, pure)));
                        return DollarScriptSupport.toLambda(scope, callable, source, Arrays.asList(constrain(scope, rhs, constraint, useSource)),
                                "subscribe-assign");
                    }
                }
                return assign(rhs, objects, constraint, constant, isVolatile, source, constraintSource);
            }
        };
    }

    @NotNull
    private static var constrain(Scope scope, @NotNull var rhs, var constraint, String source) {
//        System.err.println("(" + source + ") " + rhs.$type().constraint());
        validateConstraint(scope, rhs, source);
        return rhs._constrain(constraint, source);
    }

    private static void validateConstraint(Scope scope, @NotNull var rhs, String source) {
        if (!Objects.equals(rhs.$type().constraint(), source)) {
            if (!rhs.$type().constraint().isEmpty()) {
                scope.handleError( new DollarScriptException("Trying to assign an invalid constrained variable " + rhs.$type().constraint() + " vs " + source,rhs));
            }
        } else {
            if (!rhs.$type().constraint().isEmpty()) {
//                System.err.println("Fingerprint: " + rhs.$type().constraint());
            }
        }
    }

    private var assign(@NotNull var rhs, Object[] objects, @Nullable var constraint, boolean constant,
                       boolean isVolatile,
                       SourceSegment source,
                       String constraintSource) {

        final String varName = objects[4].toString();
        final var useConstraint;
        final String useSource;
        if (constraint != null) {
            useConstraint = constraint;
            useSource = constraintSource;
        } else {
            useConstraint = scope.getConstraint(varName);
            useSource = scope.getConstraintSource(varName);
        }
        Callable<var> callable = new Callable<var>() {
            @Override
            public var call() throws Exception {

                return scope.getDollarParser().inScope(pure, "assignment-constraint", scope, newScope -> {
                    final var rhsFixed = rhs._fix(1, false);

                    if (useConstraint != null) {
                        newScope.setParameter("it", rhsFixed);
                        newScope.setParameter("previous", scope.get(varName));
                        if (useConstraint.isFalse()) {
//                        System.out.println(rhsFixed.toDollarScript());
//                        System.out.println(useConstraint.isFalse());
                            newScope.handleError(new DollarScriptException(
                                    "Constraint failed for variable " + varName + ""));
                        }
                    }
                    if (objects[0] != null) {
                        scope.getDollarParser().export(varName, rhsFixed);
                    }
                    return scope.set(varName, rhsFixed, constant,
                            constraint, useSource, isVolatile, constant, pure);
                });
            }
        };
         return scope.getDollarParser().inScope(pure, "constraint-validation", scope, newScope -> {
             return DollarScriptSupport.toLambda(scope, callable, source, Arrays.asList(constrain(newScope, rhs, constraint, useSource)), "assignment");
        });
    }
}
