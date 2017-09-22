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

import dollar.api.Pipeable;
import dollar.api.Scope;
import dollar.api.SubType;
import dollar.api.Type;
import dollar.api.Value;
import dollar.api.VarFlags;
import dollar.api.VarKey;
import dollar.api.script.DollarParser;
import dollar.api.script.Source;
import dollar.internal.runtime.script.SimpleSubType;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import dollar.internal.runtime.script.parser.SourceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static dollar.api.DollarStatic.$;
import static dollar.api.DollarStatic.$void;
import static dollar.api.types.meta.MetaConstants.*;
import static dollar.internal.runtime.script.DollarUtilFactory.util;
import static dollar.internal.runtime.script.api.DollarUtil.MIN_PROBABILITY;
import static dollar.internal.runtime.script.parser.SourceNodeOptions.NEW_SCOPE;
import static dollar.internal.runtime.script.parser.Symbols.*;
import static java.util.Collections.emptyList;

public class AssignmentOperator implements Function<Token, Function<? super Value, ? extends Value>> {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger("AssignmentOperator");
    @NotNull
    private final DollarParser parser;
    private final boolean pure;

    public AssignmentOperator(boolean pure, @NotNull DollarParser parser) {
        this.pure = pure;
        this.parser = parser;
    }

    @Override
    @Nullable
    public Function<? super Value, ? extends Value> apply(@NotNull Token token) {
        @Nullable Type type;
        Object[] objects = (Object[]) token.value();
        Value constraint = null;
        @Nullable final SubType constraintSource;

        if (objects[3] instanceof Value) {
            SourceImpl meta = ((Value) objects[3]).meta(CONSTRAINT_SOURCE);
            constraintSource = (meta == null) ? null : new SimpleSubType(meta);
        } else {
            constraintSource = null;
        }

        if (objects[2] != null) {
            type = Type.of(objects[2].toString());
            constraint = util().node(ASSIGNMENT_CONSTRAINT, "assignment-constraint",
                                     pure, NEW_SCOPE, parser,
                                     new SourceImpl(token), type, emptyList(),
                                     i -> {
                                         Value it = util().scope().parameter(
                                                 VarKey.IT).getValue();
                                         return $(
                                                 it.is(type) && ((objects[3] == null) || ((Value) objects[3]).isTrue()));
                                     });
        } else {
            type = null;
            if (objects[3] instanceof Value) constraint = (Value) objects[3];

        }

        boolean constant;
        boolean isVolatile;
        final Object mutability = objects[1];
        boolean declaration = (mutability != null) || (objects[2] instanceof Value) || (objects[3] instanceof Value);
        constant = (mutability != null) && "const".equals(mutability.toString());
        isVolatile = (mutability != null) && "volatile".equals(mutability.toString());
        if (((Value) objects[4]).metaAttribute(IS_BUILTIN) != null) {
            throw new DollarScriptException(String.format(
                    "The variable '%s' cannot be assigned as this name is the name of a builtin function.", objects[4]));
        }
        final VarKey varName = VarKey.of((Value) objects[4]);

        Value finalConstraint = constraint;
        return (Function<Value, Value>) rhs -> {
            util().checkLearntType(token, type, rhs, MIN_PROBABILITY);

            final String op = ((Value) objects[5]).metaAttribute(ASSIGNMENT_TYPE);

//                , util().constrain(scope, rhs, finalConstraint, useSource)
            List<Value> inputs = Arrays.asList(rhs);
            if ("when".equals(op)) {
                log.debug("DYNAMIC: {}", rhs.dynamic());

                return util().node(WHEN_ASSIGN, pure, parser, token, inputs,
                                   new WhenAssignAction(finalConstraint, constraintSource, varName, objects, rhs, isVolatile,
                                                        declaration, token)
                );

            } else if ("subscribe".equals(op)) {
                return util().node(SUBSCRIBE_ASSIGN, pure, parser, token, inputs,
                                   c -> {
                                       Scope scope = util().scope();
                                       final SubType useSource;
                                       Value useConstraint;
                                       if (finalConstraint != null) {
                                           useConstraint = finalConstraint;
                                           useSource = constraintSource;
                                       } else {
                                           useConstraint = scope.constraintOf(varName);
                                           useSource = scope.subTypeOf(varName);
                                       }
                                       scope.set(varName, $void(), null, useSource,
                                                 new VarFlags(false, true, true, pure, false, declaration));

                                       return $(rhs.$subscribe(
                                               i -> util().setVariable(scope, varName,
                                                                       util().fix(
                                                                               i[0]),
                                                                       parser, token,
                                                                       useConstraint, useSource,
                                                                       new VarFlags(false, true,
                                                                                    false, pure,
                                                                                    false,
                                                                                    declaration)).getValue()));
                                   });
            }

            return assign(rhs, objects, finalConstraint, new VarFlags(constant, isVolatile, declaration, pure),
                          constraintSource, token, type, new SourceImpl(util().scope(), token));
        };
    }

    @NotNull
    private Value assign(@NotNull Value rhs,
                         @NotNull Object[] objects,
                         @Nullable Value constraint,
                         @NotNull VarFlags varFlags,
                         @Nullable SubType constraintSource,

                         @NotNull Token token,
                         @Nullable Type type,
                         @NotNull Source source) {

        final VarKey varName = VarKey.of((Value) objects[4]);

        Pipeable pipeable = args -> {

            Scope currentScope = util().scope();
            final Value useConstraint;
            final SubType useSource;
            Scope varScope = util().getScopeForVar(pure, varName, false, util().scope());
            if ((constraint != null) || (varScope == null)) {
                useConstraint = constraint;
                useSource = constraintSource;
            } else {
                useConstraint = varScope.constraintOf(varName);
                useSource = varScope.subTypeOf(varName);
            }
            //Don't change this it is the 'instinctive' depth a programmer would expect
            final Value rhsFixed = rhs.$fixDeep(false);

            if (rhsFixed.$type() != null && type != null) {
                if (!rhsFixed.$type().canBe(type)) {
                    throw new DollarScriptException("Type mismatch expected " + type + " got " + rhsFixed.$type(), source);
                }

            }
            if (useConstraint != null) {
                //noinspection ResultOfMethodCallIgnored
                util().inSubScope(true, pure, "assignment-constraint",
                                  newScope -> {
                                      newScope.parameter(VarKey.IT, rhsFixed);
                                      newScope.parameter(VarKey.PREVIOUS, newScope.get(varName));
                                      if (useConstraint.isFalse()) {
                                          newScope.handleError(
                                                  new DollarScriptException(
                                                                                   "Constraint failed for variable " + varName + "",
                                                                                   source));
                                      }
                                      return null;
                                  })
                        .orElse(null);
            }
            if (objects[0] != null) {
                parser.export(varName, rhsFixed);
            }
            util().setVariable(currentScope, varName, rhsFixed, parser, token, constraint, useSource, varFlags);
            return rhsFixed;

        };
        //        node.$listen(i -> scope.notify(varName));
        return util().node(ASSIGNMENT, pure, parser, token, Arrays.asList(rhs), pipeable);
    }


    private class WhenAssignAction implements Pipeable {
        private @Nullable
        final SubType constraintSource;

        private final boolean declaration;

        @Nullable
        private final Value finalConstraint;

        private final boolean isVolatile;

        @NotNull
        private final Object[] objects;

        @NotNull
        private final Value rhs;
        @NotNull

        private final Token token;

        @NotNull
        private final VarKey varName;

        WhenAssignAction(@Nullable Value finalConstraint,
                         @Nullable SubType constraintSource,
                         @NotNull VarKey varName,
                         @NotNull Object[] objects,
                         @NotNull Value rhs,
                         boolean isVolatile, boolean declaration, @NotNull Token token) {
            this.finalConstraint = finalConstraint;
            this.constraintSource = constraintSource;
            this.varName = varName;
            this.objects = Arrays.copyOf(objects, objects.length);
            this.rhs = rhs;
            this.isVolatile = isVolatile;
            this.declaration = declaration;
            this.token = token;
        }

        @Override
        public @NotNull Value pipe(Value... c) throws Exception {
            Scope scope = util().scope();
            final SubType useSource;
            Value useConstraint;
            if (finalConstraint != null) {
                useConstraint = finalConstraint;
                useSource = constraintSource;
            } else {
                useConstraint = scope.constraintOf(varName);
                useSource = scope.subTypeOf(varName);
            }

            Value condition = (Value) objects[5];
            Value initial = rhs.$fixDeep(false);
            scope.set(varName, condition.isTrue() ? initial : $void(), useConstraint,
                      useSource,
                      new VarFlags(false, isVolatile, false, pure, false,
                                   declaration));
            return condition.$listen(
                    args -> {
                        Value value;
                        if (condition.isTrue()) {
                            value = rhs.$fixDeep(false);
                        } else {
                            value = $void();
                        }
                        util().setVariable(scope, varName, value, parser, token, useConstraint, useSource,
                                           new VarFlags(false, isVolatile, false, pure, false, false));

                        return value;
                    });
        }
    }
}
