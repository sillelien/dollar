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

import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.Scope;
import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.TypePrediction;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.SourceSegmentValue;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.jparsec.functors.Map;

import java.util.Arrays;

import static com.sillelien.dollar.api.DollarStatic.*;
import static dollar.internal.runtime.script.DollarScriptSupport.currentScope;
import static dollar.internal.runtime.script.DollarScriptSupport.inScope;

public class AssignmentOperator implements Map<Token, Map<? super var, ? extends var>> {
    private final boolean pure;
    private DollarParser parser;

    public AssignmentOperator(boolean push, boolean pure, DollarParser parser) {
        this.pure = pure;
        this.parser = parser;
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
        if (objects[2] != null) {
            type = Type.valueOf(objects[2].toString().toUpperCase());
            constraint = DollarScriptSupport.createNode(token, i -> {
                return $(currentScope().getParameter("it")
                        .is(type) &&
                        (objects[3] == null || ((var) objects[3]).isTrue()));
            }, Arrays.asList(), "constraint", parser);
        } else {
            type = null;
            constraint = (var) objects[3];

        }
        boolean constant;
        boolean isVolatile;
        final Object mutability = objects[1];
        boolean decleration = mutability != null;
        constant = mutability != null && mutability.toString().equals("const");
        isVolatile = mutability != null && mutability.toString().equals("volatile");
        if (((var) objects[4]).getMetaAttribute("__builtin") != null) {
            throw new DollarScriptException("The variable '" +
                    objects[4] +
                    "' cannot be assigned as this name is the name of a builtin function.");
        }
        final String varName = objects[4].toString();

        return new Map<var, var>() {

            public var map(@NotNull var rhs) {
                Scope scope = currentScope();
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
                                new SourceSegmentValue(currentScope(), token).getSourceMessage());
                    }
                }

                final String operator = objects[5].toString();
                if (operator.equals("?=") || operator.equals("*=")) {
                    final String useSource;
                    var useConstraint;
                    if (constraint != null) {
                        useConstraint = constraint;
                        useSource = constraintSource;
                    } else {
                        useConstraint = scope.getConstraint(varName);
                        useSource = scope.getConstraintSource(varName);
                    }
                    if (operator.equals("?=")) {
                        scope.set(varName, $void(), false, null, useSource, isVolatile, false, pure);
                        Pipeable pipeable = c -> $($(rhs.$listen(
                                i -> DollarScriptSupport.setVariable(scope, varName, fix(i[0], false), false,
                                        useConstraint, useSource, isVolatile, false, pure, decleration, token, parser))));
                        return DollarScriptSupport.createNode("listen-assign", parser, token,
                                                              Arrays.asList(DollarScriptSupport.constrain(scope, rhs, constraint, useSource)),
                                                              pipeable
                        );

                    } else if (operator.equals("*=")) {
                        scope.set(varName, $void(), false, null, useSource, true, true, pure);
                        Pipeable pipeable = c -> $(rhs.$subscribe(
                                i -> DollarScriptSupport.setVariable(scope, varName, fix(i[0], false), false,
                                        useConstraint, useSource, true, false, pure, decleration, token, parser)));
                        return DollarScriptSupport.createNode("subscribe-assign", parser, token,
                                                              Arrays.asList(DollarScriptSupport.constrain(scope, rhs, constraint, useSource)),
                                                              pipeable
                        );
                    }
                }
                return assign(rhs, objects, constraint, constant, isVolatile, constraintSource, scope, token, decleration);
            }
        };
    }

    private var assign(@NotNull var rhs, Object[] objects, @Nullable var constraint, boolean constant,
                       boolean isVolatile, String constraintSource, Scope scope, Token token, boolean decleration) {

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
        Pipeable callable = new Pipeable() {
            @Override
            public var pipe(var ...args) throws Exception {

                Scope currentScope = currentScope();
                final var rhsFixed = rhs._fix(1, false);

                if (useConstraint != null) {
                    inScope(true, pure, "assignment-constraint", newScope -> {
                        newScope.setParameter("it", rhsFixed);
                        newScope.setParameter("previous", scope.get(varName));
                        if (useConstraint.isFalse()) {
//                        System.out.println(rhsFixed.toDollarScript());
//                        System.out.println(useConstraint.isFalse());
                            newScope.handleError(new DollarScriptException(
                                    "Constraint failed for variable " + varName + ""));
                        }
                        return null;
                    });
                }
                if (objects[0] != null) {
                    parser.export(varName, rhsFixed);
                }
                return DollarScriptSupport.setVariable(currentScope, varName, rhsFixed, constant,
                        constraint, useSource, isVolatile, constant, pure, decleration, token, parser);

            }
        };
        return DollarScriptSupport.createNode("assignment", parser, token,
                                              Arrays.asList(DollarScriptSupport.constrain(scope, rhs, constraint, useSource)),
                                              callable);
    }
}
