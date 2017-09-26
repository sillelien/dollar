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

import dollar.api.DollarException;
import dollar.api.Scope;
import dollar.api.Value;
import dollar.api.script.Source;
import dollar.internal.runtime.script.api.ParserErrorHandler;
import dollar.internal.runtime.script.api.exceptions.DollarAssertionException;
import dollar.internal.runtime.script.api.exceptions.DollarExitError;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import dollar.internal.runtime.script.api.exceptions.ErrorReporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.error.Location;
import org.jparsec.error.ParseErrorDetails;
import org.jparsec.error.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;

import static dollar.api.DollarException.unravel;

public class ErrorHandlerFactory implements ParserErrorHandler {

    @NotNull
    private static final Logger log = LoggerFactory.getLogger("ErrorHandlerFactory");

    private ErrorHandlerFactory() {
    }

    public static ParserErrorHandler instance() {
        return new ErrorHandlerFactory();
    }


    @NotNull
    @Override
    public Value handle(@NotNull Scope scope, @Nullable Source source, @NotNull DollarAssertionException e) {
        return handleInternal(scope, source, e);
    }

    @NotNull
    @Override
    public Value handle(@NotNull Scope scope, @Nullable Source source, @NotNull DollarException e) {
        return handleInternal(scope, source, e);
    }

    @Override
    @NotNull
    public Value handle(@NotNull Scope scope, @Nullable Source source, @NotNull Exception e) {
        return handleInternal(scope, source, e);

    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    @Override
    public void handleTopLevel(@NotNull Exception t, @Nullable String name, @Nullable File file) throws RuntimeException {
        if ("true".equals(System.getProperty("dollar.internal.negative.test", "false"))) {
            log.error(t.getMessage(), t);
            System.getProperty("dollar.internal.negative.test.exception", t.getClass().getCanonicalName());
            System.getProperty("dollar.internal.negative.test.message", t.getMessage());
            throw new DollarExitError(t);
        }
        log.debug(t.getMessage(), t);
        if (t instanceof DollarAssertionException) {
            System.out.println("ASSERTION FAILURE");

        }
        if (t instanceof DollarException) {
            ErrorReporter.report(getClass(), t);
            System.out.println(t.getMessage());
            return;
        } else if (t instanceof ParserException) {
            Location location = ((ParserException) t).getLocation();
            ParseErrorDetails errorDetails = ((ParserException) t).getErrorDetails();
            System.out.println();
            if (file != null) {
                System.out.print("A parser (syntax) error occurred while parsing " + file + ":" + location.line);
            } else {
                if (name != null) {
                    System.out.print("A parser (syntax) error occurred while parsing " + name);
                } else {
                    System.out.print("Problem parsing this script");
                }
            }
            if (location != null) {
                if (errorDetails != null) {
                    System.out.println(
                            " the error occurred on line " + location.line + " column " + location.column + " near '" + errorDetails.getEncountered() + "'.");
                } else {
                    System.out.println(
                            " the error occurred on line " + location.line + " column " + location.column);
                }
            }
            System.out.println();
            if (t.getCause() instanceof DollarScriptException) {
                System.out.println(t.getCause().getMessage());
            } else {
                System.out.println(t.getMessage());
            }
            throw new DollarExitError(t);
        } else {
            if (!Objects.equals(unravel(t), t)) {
                handleTopLevel(unravel(t), name, file);
                return;
            }
        }
        throw new DollarException(t);
    }

    @NotNull
    private Value handleInternal(@NotNull Scope scope, @Nullable Source source, @NotNull Exception e) {

        if ((e instanceof DollarException) && (source != null)) {
            return scope.handleError(e, source);
        } else if (source != null) {
            return scope.handleError(new DollarScriptException(e, source));
        } else {
            return scope.handleError(new DollarScriptException(e));
        }
    }

}
