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

package me.neilellis.dollar.script;

import me.neilellis.dollar.api.DollarException;
import me.neilellis.dollar.api.exceptions.LambdaRecursionException;
import me.neilellis.dollar.api.script.SourceSegment;
import me.neilellis.dollar.api.types.DollarFactory;
import me.neilellis.dollar.api.types.ErrorType;
import me.neilellis.dollar.api.var;
import me.neilellis.dollar.script.api.Scope;
import me.neilellis.dollar.script.api.exceptions.DollarParserException;
import me.neilellis.dollar.script.api.exceptions.DollarScriptException;
import me.neilellis.dollar.script.api.exceptions.ErrorReporter;
import me.neilellis.dollar.script.api.exceptions.VariableNotFoundException;
import org.codehaus.jparsec.error.ParserException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public class ParserErrorHandler {
    private final boolean missingVariables;
    private final boolean failfast;
    private final boolean faultTolerant;

    public ParserErrorHandler() {
        missingVariables = true;
        failfast = true;
        faultTolerant = false;
    }


    public var handle(@NotNull Scope scope, @NotNull SourceSegment source, @NotNull AssertionError e) {
        AssertionError throwable = new AssertionError(e.getMessage() + " at " + source.getSourceMessage(), e);
        if (!faultTolerant) {
            return scope.handleError(e);
        } else {
            return DollarFactory.failure(throwable);
        }
    }

    public var handle(@NotNull Scope scope, @NotNull SourceSegment source, @NotNull DollarException e) {
        DollarParserException
                throwable =
                new DollarParserException(e.getMessage() + " at " + source.getSourceMessage(), e);
        if ((e instanceof VariableNotFoundException && missingVariables) || failfast) {
            return scope.handleError(e);
        } else {
            return DollarFactory.failure(throwable);
        }
    }

    @NotNull public var handle(Scope scope, @Nullable SourceSegment source, Exception e) {
        if (e instanceof LambdaRecursionException) {
            throw new DollarParserException(
                    "Excessive recursion detected, this is usually due to a recursive definition of lazily defined " +
                    "expressions. The simplest way to solve this is to use the 'fix' operator or the '=' operator to " +
                    "reduce the amount of lazy evaluation. The error occured at " +
                    source);
        }

        if (e instanceof DollarException && source != null) {
            ((DollarException) e).addSource(source);
            throw (DollarException) e;
        } else if (source != null) {
            return DollarFactory.failureWithSource(ErrorType.EXCEPTION, unravel(e), source);
        } else {
            return DollarFactory.failure(ErrorType.EXCEPTION, unravel(e), false);
        }

    }

    private Throwable unravel(Throwable e) {
        if (e instanceof InvocationTargetException) {
            return e.getCause();
        } else {
            return e;
        }
    }

    public void handleTopLevel(Throwable t) throws Throwable {
        if (t instanceof AssertionError) {
            System.err.println(t.getMessage());
        }
        if (t instanceof DollarException) {
            ErrorReporter.report(getClass(), t);
            System.err.println(t.getMessage());
        }
        if (t instanceof ParserException) {
            ErrorReporter.report(getClass(), t);
            System.err.println(t.getMessage());
        }
        if (t instanceof DollarScriptException) {
            ErrorReporter.report(getClass(), t);
            System.err.println(t.getMessage());
        } else { throw t; }
    }

    private void log(@NotNull Throwable e) {
        e.printStackTrace();
    }
}
