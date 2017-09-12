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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dollar.api.Pipeable;
import dollar.api.Scope;
import dollar.api.VarKey;
import dollar.api.script.DollarParser;
import dollar.api.time.Scheduler;
import dollar.api.types.DollarFactory;
import dollar.api.types.meta.MetaConstants;
import dollar.api.var;
import dollar.internal.runtime.script.parser.Func;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static dollar.api.DollarStatic.$void;
import static dollar.internal.runtime.script.DollarUtilFactory.util;
import static dollar.internal.runtime.script.parser.Symbols.WINDOW_OP;
import static java.util.Collections.singletonList;

public class WindowOperator implements Function<Token, var> {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(WindowOperator.class);
    @NotNull
    private final DollarParser parser;
    private final boolean pure;

    public WindowOperator(@NotNull DollarParser dollarParser, boolean pure) {
        parser = dollarParser;
        this.pure = pure;
        assert WINDOW_OP.validForPure(pure);

    }

    @NotNull
    @Override

    public var apply(@NotNull Token token) {
        Object[] objects = (Object[]) token.value();

        var expression = (var) objects[0];
        var over = (var) objects[1];
        var period = (objects[2] == null) ? over : (var) objects[2];
        var unless = (var) objects[3];
        var until = (var) objects[4];
        var block = (var) objects[5];

//        log.debug("Listening to {}", expression.metaAttribute(OPERATION_NAME));
//        String varName = expression.metaAttribute(VARIABLE);


        String id = UUID.randomUUID().toString();
        return util().node(WINDOW_OP, pure, parser, token, singletonList(expression),
                           parallel -> {
//                        assert varName != null;
//                        Scope scopeForVar = getScopeForVar(pure, varName, false, null);
//
//                        if (scopeForVar == null) {
//                            throw new VariableNotFoundException(varName, scope());
//                        }
//
                               Double duration = period.toDouble();
                               assert duration != null;
                               Scope scope = util().scope();
                               NotifyListener notifyListener = new NotifyListener(unless, until, block,
                                                                                  (long) (over.toDouble() * Func
                                                                                                                    .ONE_DAY));
                               log.info("Before expression listen");
                               expression.$fixDeep(false);
                               var listenerId = expression.$listen(notifyListener,
                                                                   "window-expression-listener-" + expression.meta(
                                                                           MetaConstants.OPERATION_NAME) + "-" + id);
                               expression.$notify();
                               log.info("After expression listen");
                               Scheduler.schedule(i -> util().inSubScope(true, pure,
                                                                         "window-operator-period",
                                                                         newScope -> {
                                                                             log.info(
                                                                                     "Schedule called on WindowOperator");
                                                                             newScope.parameter(
                                                                                     VarKey.COUNT,
                                                                                     DollarFactory.fromValue(
                                                                                             notifyListener.count.get()));
                                                                             newScope.parameter(
                                                                                     VarKey.COLLECTED,
                                                                                     DollarFactory.fromList(
                                                                                             notifyListener.collected
                                                                                                     .asMap()
                                                                                                     .entrySet()
                                                                                                     .stream()
                                                                                                     .sorted(Comparator.comparing(
                                                                                                             Map.Entry::getKey))
                                                                                                     .map(Map.Entry::getValue)
                                                                                                     .collect(
                                                                                                             Collectors.toList())));
                                                                             block.$fixDeep(
                                                                                     false);
                                                                             log.info(
                                                                                     "Clearing collected");
                                                                             if (notifyListener.finished) {
                                                                                 log.debug(
                                                                                         "Cancelling");
                                                                                 Scheduler.cancel(
                                                                                         i[0].$S());
                                                                                 expression.$cancel(
                                                                                         listenerId);
                                                                             }
                                                                             return $void();

                                                                         }),
                                                  ((long) (duration * Func.ONE_DAY)));
                               return $void();

                           });


    }


    private final class NotifyListener implements Pipeable {
        @NotNull
        final AtomicLong count = new AtomicLong(-1);
        @NotNull
        private final Cache<Long, var> collected;
        @NotNull
        private final AtomicLong collectedId = new AtomicLong();
        @NotNull
        private final var loop;
        @Nullable
        private final var unless;
        @Nullable
        private final var until;
        private boolean finished;

        private NotifyListener(@Nullable var unless, @Nullable var until, @NotNull var loop, long windowLength) {
            this.unless = unless;
            this.until = until;
            this.loop = loop;
            collected = CacheBuilder.newBuilder().expireAfterWrite(windowLength, TimeUnit.MILLISECONDS).build();

        }

        @NotNull
        @Override
        public var pipe(var... in2) {
            var value = in2[0].$fixDeep();
            count.incrementAndGet();
            log.debug("Count is {} value is {} size is {}", count.get(), value, collected.size());

            if (!finished) {
                if ((unless != null) && unless.isTrue()) {
                    log.debug("Skipping {}", value);
                } else {
                    log.debug("Adding {}", value);
                    collected.put(collectedId.incrementAndGet(), value);
                }
            }
            if ((until != null) && until.isTrue()) {
                log.debug("End reached");
                finished = true;

            }

            return $void();

        }
    }
}
