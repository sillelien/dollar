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

package dollar.internal.runtime.script.parser;

import dollar.api.Scope;
import dollar.api.SubType;
import dollar.api.Type;
import dollar.api.Value;
import dollar.api.VarFlags;
import dollar.api.VarKey;
import dollar.api.execution.DollarExecutor;
import dollar.api.plugin.Plugins;
import dollar.api.script.DollarParser;
import dollar.api.script.ModuleResolver;
import dollar.api.script.Source;
import dollar.api.time.Scheduler;
import dollar.api.types.DollarFactory;
import dollar.internal.runtime.script.Builtins;
import dollar.internal.runtime.script.Constants;
import dollar.internal.runtime.script.api.exceptions.DollarAssertionException;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dollar.api.DollarStatic.*;
import static dollar.api.types.meta.MetaConstants.*;
import static dollar.internal.runtime.script.DollarUtilFactory.util;
import static dollar.internal.runtime.script.parser.SourceNodeOptions.SCOPE_WITH_CLOSURE;
import static dollar.internal.runtime.script.parser.Symbols.*;
import static java.util.Collections.singletonList;

/**
 * A collection of functions largely used by the {{@link DollarParserImpl} class to support the execution of Dollar expressions
 */
@SuppressWarnings({"UtilityClassCanBeSingleton", "UtilityClassCanBeEnum"})
public final class Func {

    public static final double ONE_DAY = 24.0 * 60.0 * 60.0 * 1000.0;
    @NotNull
    static final DollarExecutor executor = Objects.requireNonNull(Plugins.sharedInstance(DollarExecutor.class));

    @NotNull
    static Value assertEqualsFunc(@NotNull Value lhs, @NotNull Value rhs) {
        final Value lhsFix = lhs.$fixDeep();
        final Value rhsFix = rhs.$fixDeep();
        if (lhsFix.equals(rhsFix)) {
            return $(true);
        } else {
            throw new DollarAssertionException(lhsFix.toDollarScript() + " != " + rhsFix.toDollarScript(), lhs);
        }
    }

    @NotNull
    static Value assertFunc(@Nullable Value message, @NotNull Value condition) {
        if (!condition.isTrue()) {
            throw new DollarAssertionException("Assertion failed: " + (message != null ? message : ""), condition);

        }
        return $void();
    }

    @NotNull
    static Value blockFunc(int depth, @NotNull List<Value> l) {
        if (l.isEmpty()) {
            return $void();
        } else {
            int bound = l.size() - 1;
            for (int i = 0; i < bound; i++) {
                l.get(i).$fix(depth, false);
            }
            return l.get(l.size() - 1).$fix(1, false);
        }
    }

    @NotNull
    static Value castFunc(@NotNull Value lhs, @NotNull String typeName) {
        return lhs.$as(Type.of(typeName));
    }

    @NotNull
    static Value causesFunc(boolean pure, @NotNull Value lhs, @NotNull Value rhs) {
        lhs.$fixDeep(false);
        return lhs.$listen(vars -> rhs.$fix(1, false));
    }

    @NotNull
    public static Value definitionFunc(@NotNull Token token,
                                       boolean export,
                                       @NotNull Value value,
                                       @NotNull Value variableName,
                                       @Nullable Value constraint,
                                       @Nullable SubType constraintSource,
                                       @NotNull DollarParser parser,
                                       boolean pure,
                                       boolean readonly) {
        VarKey key = VarKey.of(variableName);

        util().setVariable(util().scope(), key, value,
                           parser, token, constraint, constraintSource,
                           new VarFlags(readonly, false, false, pure, false, true));

        if (export) {
            parser.export(key, util().node(DEFINITION, "export-" + DEFINITION.name(),
                                           pure, SCOPE_WITH_CLOSURE, parser,
                                           new SourceImpl(util().scope(), token),
                                           null, singletonList(value), exportArgs -> value.$fix(2, false)));
        }
        return $void();
    }

    @NotNull
    static Value eachFunc(boolean pure, @NotNull Value lhs, @NotNull Value rhs) {
        assert EACH.validForPure(pure);
        return lhs.$each(i -> util().inSubScope(true, pure, EACH.name(),
                                                newScope -> {
                                                    newScope.parameter(VarKey.ONE, i[0]);
                                                    return rhs.$fixDeep(false);
                                                }).orElseThrow(() -> new AssertionError("Optional should not be null here")));
    }

    @NotNull
    static Value elseFunc(@NotNull Value lhs, @NotNull Value rhs) {
        final Value fixLhs = lhs.$fixDeep(false);
        if (fixLhs.isBoolean() && fixLhs.isFalse()) {
            return rhs.$fix(2, false);
        } else {
            return fixLhs;
        }
    }

    @NotNull
    static Value errorFunc(@NotNull Value v) {
        return util().scope().addErrorHandler(v);
    }

    @NotNull
    static Value everyFunc(@NotNull AtomicInteger count,
                           @NotNull Value durationValue,
                           @Nullable Value until,
                           @Nullable Value unless,
                           @NotNull Value block) {
        Scope scope = util().scope();
        double duration = durationValue.toDouble();
        Scheduler.schedule(i -> {
            count.incrementAndGet();
            return util().inScope(true, scope, newScope -> {

                newScope.parameter(VarKey.ONE, $(count.get()));
                if ((until != null) && until.isTrue()) {
                    Scheduler.cancel(i[0].$S());
                    return i[0];
                } else {
                    if ((unless != null) && unless.isTrue()) {
                        return $void();
                    } else {
                        return block.$fixDeep(false);
                    }
                }

            }).orElseThrow(() -> new AssertionError("Optional should not be null here"));
        }, ((long) (duration * ONE_DAY)));
        return $void();
    }

    static Value fixFunc(@NotNull Value v) {
        return v.$fix(Integer.MAX_VALUE, false);
    }

    @NotNull
    static Value forFunc(boolean pure, @NotNull String varName, @NotNull Value iterable, @NotNull Value block) {
        assert FOR_OP.validForPure(pure);
        return iterable.$each(i -> util().inSubScope(true, pure, "for-loop", newScope -> {
            util().scope()
                    .set(VarKey.of(varName),
                         util().fix(i[0]),
                         null, null,
                         new VarFlags(false, false, false, pure, false, false));
            return block.$fixDeep(false);
        }).orElseThrow(() -> new AssertionError("Optional should not be null here")));
    }

    @NotNull
    static Future<Value> forkFunc(@NotNull Value v) {
        return executor.executeInBackground(() -> util().fix(v));
    }

    @NotNull
    static Value ifFunc(boolean pure, @NotNull Value lhs, @NotNull Value rhs) {
        final Value lhsFix = lhs.$fixDeep(false);
        boolean isTrue = lhsFix.isBoolean() && lhsFix.isTrue();
        assert IF_OP.validForPure(pure);
        return isTrue ? rhs.$fix(2, false) : DollarFactory.FALSE;
    }

    static Value inFunc(@NotNull Value lhs, @NotNull Value rhs) {return rhs.$contains(lhs);}

    @NotNull
    static Value isFunc(@NotNull Value lhs, @NotNull List<Value> value) {
        return $(value.stream()
                         .map(Value::$S)
                         .map(Type::of)
                         .filter(lhs::is).count() > 0);
    }

    @NotNull
    static Value listenFunc(@NotNull Value lhs, @NotNull Value rhs) {
        lhs.$fixDeep();
        return lhs.$listen(var -> lhs.isTrue() ? util().fix(rhs) : $void());
    }

    @NotNull
    static Value mapFunc(boolean parallel, @NotNull List<Value> entries) {
        if (entries.size() == 1) {
            return blockFunc(2, entries);
        } else {
            Stream<Value> stream = parallel ? entries.stream().parallel() : entries.stream();
            return $(stream.map(v -> v.$fix(2, parallel))
                             .collect(Collectors.toConcurrentMap(
                                     v -> v.pair() ? v.$pairKey() : v.$S(),
                                     v -> v.pair() ? v.$pairValue() : v)));
        }
    }

    @NotNull
    static Value moduleFunc(@NotNull DollarParserImpl parser,
                            @NotNull String moduleName,
                            @Nullable Iterable<Value> params) throws Exception {
        String[] parts = moduleName.split(":", 2);
        if (parts.length < 2) {
            throw new DollarScriptException("Module " + moduleName + " needs to have a scheme");
        }
        Map<String, Value> paramMap = new HashMap<>();
        if (params != null) {
            for (Value param1 : params) {
                paramMap.put(param1.metaAttribute(DollarParserImpl.NAMED_PARAMETER_META_ATTR), param1);
            }
        }

        return ModuleResolver
                       .resolveModuleScheme(parts[0])
                       .retrieveModule(parts[1], util().scope(), parser)
                       .pipe($(paramMap))
                       .$fix(false);


    }

    @NotNull
    static Value multiplyFunc(@NotNull Value lhs, @NotNull Value rhs) {
        Op operation = lhs.meta(OPERATION);
        if (opIsCollection(operation)) {
            Value newValue = lhs.$fixDeep();
            Long max = rhs.toLong();
            if (max > Constants.MAX_MULTIPLY) {
                throw new DollarScriptException("Cannot multiply a block, map  or list by a factor larger than " +
                                                        Constants
                                                                .MAX_MULTIPLY, lhs.source());
            }
            for (int i = 1; i < max; i++) {
                newValue = newValue.$plus(lhs.$fixDeep());
            }
            return newValue;
        } else {
            final Value lhsFix = lhs.$fix(1, false);
            return lhsFix.$multiply(rhs);
        }
    }

    private static boolean opIsCollection(Op operation) {
        return (operation != null) && (operation.equals(BLOCK_OP) || operation.equals(LIST_OP) || operation.equals(MAP_OP));
    }

    @NotNull
    static Value pairFunc(@NotNull Value lhs, @NotNull Value rhs) {
        return $(lhs.$S(), rhs);
    }

    static Value pipeFunc(@NotNull DollarParser parser,
                          boolean pure,
                          @NotNull Token token,
                          @NotNull Value lhs, @NotNull Value rhs) {
        if (lhs.isVoid()) {
            return $void();
        }
        util().scope().parameter(VarKey.ONE, lhs);
        Value rhsVal = rhs.$fix(false);
        if (FUNCTION_NAME_OP.name().equals(rhs.metaAttribute(OPERATION_NAME))) {
            return rhsVal;
        } else {
            return (rhs.metaAttribute(IS_BUILTIN) != null)
                           ? Builtins.execute(rhsVal.toString(), singletonList(lhs), pure)
                           : util().variableNode(pure, VarKey.of(rhsVal), false, null, token, parser).$fix(1, false);
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    static Value printFunc(@NotNull DollarParser parser,
                           @NotNull Source segment,
                           @NotNull Op command,
                           @NotNull List<Value> Values) {
        return util().node(command, false, parser, segment, Values, args -> {
            String outStr = Values.stream().map(Value::toString).collect(Collectors.joining(""));
            if (command.equals(OUT)) {
                System.out.println(outStr);
            } else if (command.equals(ERR)) {
                System.err.println(outStr);
            } else if (command.equals(DEBUG)) {
                if (getConfig().debug()) {
                    System.out.println(segment.getShortSourceMessage() + ": " + outStr);
                }
            }
            return DollarFactory.fromStringValue(outStr);
        });
    }

    @NotNull
    static Value publishFunc(@NotNull Value lhs, @NotNull Value rhs) {
        return rhs.$publish(lhs);
    }

    @NotNull
    static Value readFunc(@NotNull Value from) {
        return DollarFactory.fromURI(from).$read();
    }

    @NotNull
    static Value reduceFunc(boolean pure, @NotNull Value lhs, @NotNull Value rhs) {
        assert REDUCE.validForPure(pure);
        return lhs.stream(false).reduce((x, y) -> {
            try {
                return util().inSubScope(false, pure, REDUCE.name(), newScope -> {
                    newScope.parameter(VarKey.ONE, x);
                    newScope.parameter(VarKey.TWO, y);
                    return rhs.$fixDeep(false);
                }).orElseThrow(() -> new AssertionError("Optional should not be null here"));
            } catch (RuntimeException e) {
                return util().scope().handleError(e);
            }
        }).orElse($null(Type._ANY));
    }

    @NotNull
    static Value subscribeFunc(@NotNull Value lhs, @NotNull Value rhs) {
        return lhs.$subscribe(
                i -> util().inSubScope(true, false,
                                       "subscribe-param", newScope -> {
                            final Value it = util().fix(i[0]);
                            util().scope().parameter(VarKey.ONE, it);
                            util().scope().parameter(VarKey.IT, it);
                            return util().fix(rhs);
                        }).orElseThrow(() -> new AssertionError("Optional should not be null here")));
    }

    @NotNull
    static Value whileFunc(boolean pure, @NotNull Value lhs, @NotNull Value rhs) {
        assert WHILE_OP.validForPure(pure);

        while (lhs.isTrue()) {
            rhs.$fixDeep();
        }
        return $(false);
    }

    @NotNull
    static Value writeFunc(@NotNull Value lhs, @NotNull Value rhs) {
        return rhs.$write(lhs);
    }


}
