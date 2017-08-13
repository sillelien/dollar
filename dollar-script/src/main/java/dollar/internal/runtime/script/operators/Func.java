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

import com.sillelien.dollar.api.collections.Range;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.types.ErrorType;
import com.sillelien.dollar.api.var;
import dollar.internal.runtime.script.DollarParserImpl;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import dollar.internal.runtime.script.api.exceptions.DollarScriptFailureException;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.sillelien.dollar.api.DollarStatic.*;
import static com.sillelien.dollar.api.types.DollarFactory.fromValue;
import static dollar.internal.runtime.script.DollarScriptSupport.currentScope;
import static dollar.internal.runtime.script.DollarScriptSupport.inSubScope;
import static dollar.internal.runtime.script.Symbols.EACH;
import static dollar.internal.runtime.script.Symbols.REDUCE;

public class Func {
    @NotNull
    public static var reduce(@NotNull boolean pure, var lhs, var rhs) {
        return lhs.$list().$stream(false).reduce((x, y) -> {
            try {
                return inSubScope(false, pure, REDUCE.name(), newScope -> {
                    newScope.setParameter("1", x);
                    newScope.setParameter("2", y);
                    return rhs._fixDeep(false);
                });
            } catch (Exception e) {
                throw new DollarScriptException(e);
            }
        }).get();
    }

    public static var each(@NotNull boolean pure, var lhs, var rhs) {
        return lhs.$each(i -> inSubScope(false, pure, EACH.name(),
                                         newScope -> {
                                             newScope.setParameter(
                                                     "1",
                                                     i[0]);
                                             return rhs._fixDeep(
                                                     false);
                                         }));
    }

    public static var elseFunc(var lhs, var rhs) {
        final var fixLhs = lhs._fixDeep();
        if (fixLhs.isBoolean() && fixLhs.isFalse()) {
            return rhs._fix(2,
                            false);
        } else {
            return fixLhs;
        }
    }

    public static var multiply(var lhs, var rhs) {
        final var lhsFix = lhs._fix(false);
        if (Arrays.asList("block", "map", "list").contains(
                lhs.getMetaAttribute("operation"))) {
            var newValue = lhsFix._fixDeep(false);
            Long max = rhs.toLong();
            for (int i = 1; i < max; i++) {
                newValue = newValue.$plus(lhs._fixDeep());
            }
            return newValue;
        } else {
            return lhsFix.$multiply(rhs);
        }
    }

    @NotNull
    public static var error(var v) {
        return currentScope().addErrorHandler(v);
    }

    @NotNull
    public static var truthy(var i) {
        return $(i.truthy());
    }

    public static var pair(var lhs, var rhs) {
        return $(lhs.$S(), rhs);
    }

    @NotNull
    public static var gte(var lhs, var rhs) {
        return $(lhs.compareTo(rhs) >= 0);
    }

    @NotNull
    public static var lte(var lhs, var rhs) {
        return $(lhs.compareTo(rhs) <= 0);
    }

    @NotNull
    public static var gt(var lhs, var rhs) {
        return $(lhs.compareTo(rhs) > 0);
    }

    @NotNull
    public static var lt(var lhs, var rhs) {
        return $(lhs.compareTo(rhs) < 0);
    }

    @NotNull
    public static var range(var lhs, var rhs) {
        return fromValue(new Range(lhs, rhs));
    }

    @NotNull
    public static var orFunc(var lhs, var rhs) {
        return $(lhs.isTrue() || rhs.isTrue());
    }

    @NotNull
    public static var and(var lhs, var rhs) {
        return $(lhs.isTrue() && rhs.isTrue());
    }

    public static var equality(var lhs, var rhs) {
        return $(lhs.equals(rhs));
    }

    @NotNull
    public static var inequality(var lhs, var rhs) {
        return $(!lhs.equals(rhs));
    }

    @NotNull
    public static var notFunc(var v) {
        return $(!v.isTrue());
    }

    public static var parallelFunc(var v) {
        return v._fixDeep(true);
    }

    @NotNull
    public static var fork(var v) {
        return DollarFactory.fromFuture(
                DollarParserImpl.executor.executeInBackground(() -> fix(v, false)));
    }

    public static var subscribeFunc(@NotNull boolean pure, var lhs, var rhs) {
        return lhs.$subscribe(
                i -> DollarScriptSupport.inSubScope(true, pure, "subscribe-param", newScope -> {
                    final var it = fix(i[0], false);
                    currentScope().setParameter("1", it);
                    currentScope().setParameter("it", it);
                    return fix(rhs, false);
                }));
    }

    public static var writeFunc(var lhs, var rhs) {
        return rhs.$write(lhs);
    }

    public static var publishFunc(var lhs, var rhs) {
        return rhs.$publish(lhs);
    }

    @NotNull
    public static var errFunc(var i) {
        i.err();
        return $void();
    }

    @NotNull
    public static var debugFunc(var i) {
        i.debug();
        return $void();
    }

    @NotNull
    public static var outFunc(var i) {
        i.out();
        return $void();
    }

    @NotNull
    public static var assertEquals(var lhs2, var rhs2) {
        final var lhsFix1 = lhs2._fixDeep(false);
        final var rhsFix1 = rhs2._fixDeep(false);
        if (lhsFix1.equals(rhsFix1)) {
            return $(true);
        } else {
            throw new DollarScriptFailureException(ErrorType.ASSERTION,
                                                   lhsFix1.toDollarScript() +
                                                           " != " +
                                                           rhsFix1.toDollarScript());
        }
    }

    @NotNull
    public static var listenFunc(var lhs, var rhs) {
        return lhs.isTrue() ? fix(rhs, false) : $void();
    }
}
