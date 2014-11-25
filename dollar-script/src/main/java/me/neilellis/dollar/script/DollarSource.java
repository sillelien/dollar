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

package me.neilellis.dollar.script;

import me.neilellis.dollar.DollarException;
import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.types.DollarLambda;

import java.lang.reflect.Method;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarSource extends DollarLambda implements SourceAware {
    private final ScriptScope scope;

    public DollarSource(Pipeable lambda, ScriptScope scope) {
        super(lambda);
        this.scope = scope;
    }

    public DollarSource(Pipeable lambda, ScriptScope scope, boolean fixable) {
        super(lambda, fixable);
        this.scope = scope;
    }

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return super.invoke(proxy, method, args);
        } catch (AssertionError e) {
            return scope.getDollarParser().getErrorHandler().handle(scope, this, e);
        } catch (DollarException e) {
            return scope.getDollarParser().getErrorHandler().handle(scope, this, e);
        } catch (Exception e) {
            return scope.getDollarParser().getErrorHandler().handle(scope, this, e);
        }
    }

    @Override public int getStart() {
        final String parseStart = meta.get("__parse_start");
        if (parseStart != null) {
            return Integer.parseInt(parseStart);
        } else {
            return -1;
        }
    }

    @Override public int getLength() {
        final String parseStart = meta.get("__parse_length");
        if (parseStart != null) {
            return Integer.parseInt(parseStart);
        } else {
            return -1;
        }
    }

    @Override public String getSource() {
        return scope.getSource();
    }

    @Override public String getSourceMessage() {
        int index = getStart();
        int length = getLength();
        if (index < 0 || length < 0) {
            return "<unknown location>";
        }
        String theSource = scope.getSource();
        int end = theSource.indexOf('\n', index + length);
        int start = index > 10 ? index - 10 : 0;
        String
                highlightedSource =
                "... " +
                theSource.substring(start, index) +
                " \u261E " +
                theSource.substring(index, index + length) +
                " \u261C " +
                theSource.substring(index + length, end) +
                " ...";
        return highlightedSource.replaceAll("\n", "\\\\n");
    }

}
