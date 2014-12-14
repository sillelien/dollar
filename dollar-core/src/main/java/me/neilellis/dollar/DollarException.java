/*
 * Copyright (c) 2014 Neil Ellis
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

package me.neilellis.dollar;

import me.neilellis.dollar.script.SourceAware;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Dollar exception.
 * @author  <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarException extends RuntimeException {

    private final List<SourceAware> sourceList = new ArrayList<>();

    /**
     * Instantiates a new Dollar exception.
     *
     * @param e the e
     */
    public DollarException(Throwable e) {
        super(e);
    }

    /**
     * Instantiates a new Dollar exception.
     *
     * @param errorMessage the error message
     */
    public DollarException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Instantiates a new Dollar exception.
     *
     * @param t the t
     * @param s the s
     */
    public DollarException(Throwable t, String s) {
        super(s, t);
    }

    /**
     * Add source.
     *
     * @param source the source
     */
    public void addSource(SourceAware source) {
        if (source == null) {
            throw new NullPointerException();
        }
        sourceList.add(source);
    }

    @Override public String getMessage() {
        if (sourceList.size() == 0) {
            return super.getMessage() + " (no source available)";

        } else {
            StringBuilder builder = new StringBuilder(super.getMessage() + "\n");
            for (SourceAware sourceEntry : sourceList) {
                builder.append(sourceEntry.getSourceMessage()).append("\n");
            }
            return builder.toString();
        }
    }

    /**
     * Http code.
     *
     * @return the int
     */
    public int httpCode() {
        return 500;
    }
}
