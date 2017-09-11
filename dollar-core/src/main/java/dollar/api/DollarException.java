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

package dollar.api;

import dollar.api.script.Source;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DollarException extends RuntimeException {

    @NotNull
    public static final String
            STARS =
            "*******************************************************************************\n";
    @NotNull
    private final List<Source> sourceList = new ArrayList<>();

    /**
     * Instantiates a new Dollar exception.
     *
     * @param cause the cause of this exception
     */
    public DollarException(@NotNull Throwable cause) {
        super(unravel(cause));
    }

    /**
     * Instantiates a new Dollar exception.
     *
     * @param message the error message associated with this exception
     */
    public DollarException(@NotNull String message) {
        super(message);
    }

    /**
     * Instantiates a new Dollar exception.
     *
     * @param cause   the cause of this exception
     * @param message the error message associated with this exception
     */
    public DollarException(@NotNull Throwable cause, @NotNull String message) {
        super(message, unravel(cause));
    }

    public static @NotNull
    Throwable unravel(@NotNull Throwable e) {
        if ((e instanceof InvocationTargetException) || (e instanceof ExecutionException)) {
            return unravel(e.getCause());
        } else {
            return e;
        }
    }

    /**
     * Add source information, this is useful if the exception is thrown while executing DollarScript.
     *
     * @param source the source code to which the exception relates
     */
    public void addSource(@NotNull Source source) {
        if (source == null) {
            throw new NullPointerException();
        }
        if (!sourceList.contains(source)) {
            sourceList.add(source);
        }
    }

//    @NotNull @Override public String toString() {
//        String s = getClass().getName();
//        String message = this.getLocalizedMessage();
//        if (message != null) { return (STARS + s + ": " + message + "\n" + STARS); } else { return s; }
//    }

    @NotNull
    @Override
    public String getMessage() {
        if (sourceList.isEmpty()) {
            return super.getMessage();

        } else {
            StringBuilder builder = new StringBuilder(super.getMessage() + "\n");
            for (Source sourceEntry : sourceList) {
                builder.append(sourceEntry.getSourceMessage()).append("\n");
            }
            return builder.toString();
        }
    }

    /**
     * A HTTP code that is appropriate for this exception.
     *
     * @return the int
     */
    public int httpCode() {
        return 500;
    }
}
