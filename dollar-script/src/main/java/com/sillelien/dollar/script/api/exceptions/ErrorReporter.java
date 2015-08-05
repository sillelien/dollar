/*
 * Copyright (c) 2014-2015 Neil Ellis
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

package com.sillelien.dollar.script.api.exceptions;

import net.kencochrane.raven.Raven;
import net.kencochrane.raven.RavenFactory;
import net.kencochrane.raven.dsn.Dsn;
import net.kencochrane.raven.event.Event;
import net.kencochrane.raven.event.EventBuilder;
import net.kencochrane.raven.event.interfaces.ExceptionInterface;
import org.jetbrains.annotations.NotNull;

public class ErrorReporter {
    public static void report(@NotNull Class<?> clazz, @NotNull Throwable t) {
        String
                rawDsn =
                "https://fd22364656ca4cf0af0de76aeb394876:34c481215db2487fae8308665be2c106@app.getsentry.com/34485";
        Raven raven = RavenFactory.ravenInstance(new Dsn(rawDsn));

        // record a simple message
        EventBuilder eventBuilder = new EventBuilder()
                .setMessage(t.getMessage())
                .setLevel(Event.Level.ERROR)
                .setLogger(clazz.getName())
                .addSentryInterface(new ExceptionInterface(t));

        raven.runBuilderHelpers(eventBuilder);
        raven.sendEvent(eventBuilder.build());
    }
}
