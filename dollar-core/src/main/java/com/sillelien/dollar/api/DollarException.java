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

package com.sillelien.dollar.api;

import com.sillelien.dollar.api.script.SourceSegment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DollarException extends RuntimeException {

    public static final String
            STARS =
            "*******************************************************************************\n";
    private final List<SourceSegment> sourceList = new ArrayList<>();

    /**
     * Instantiates a new Dollar exception.
     *
     * @param cause the cause of this exception
     */
    public DollarException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new Dollar exception.
     *
     * @param message the error message associated with this exception
     */
    public DollarException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Dollar exception.
     *
     * @param cause   the cause of this exception
     * @param message the error message associated with this exception
     */
    public DollarException(Throwable cause, String message) {
        super(message, cause);
    }

    /**
     * Add source information, this is useful if the exception is thrown while executing DollarScript.
     *
     * @param source the source code to which the exception relates
     */
    public void addSource(@NotNull SourceSegment source) {
        if (source == null) {
            throw new NullPointerException();
        }
        sourceList.add(source);
    }

    @Override public String getMessage() {
        if (sourceList.size() == 0) {
            return super.getMessage();

        } else {
            StringBuilder builder = new StringBuilder(super.getMessage() + "\n");
            for (SourceSegment sourceEntry : sourceList) {
                builder.append(sourceEntry.getSourceMessage()).append("\n");
            }
            return builder.toString();
        }
    }

//    @NotNull @Override public String toString() {
//        String s = getClass().getName();
//        String message = this.getLocalizedMessage();
//        if (message != null) { return (STARS + s + ": " + message + "\n" + STARS); } else { return s; }
//    }

    /**
     * A HTTP code that is appropriate for this exception.
     *
     * @return the int
     */
    public int httpCode() {
        return 500;
    }
}
