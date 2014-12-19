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
public class DollarSource extends DollarLambda {
    private final Scope scope;
    private final Source source;

    public DollarSource(Pipeable lambda, Scope scope, Source source) {
        super(lambda);
        if (scope == null) {
            throw new NullPointerException();
        }
        this.scope = scope;
        this.source = source;
    }

    public DollarSource(Pipeable lambda, Scope scope, Source source, boolean fixable) {
        super(lambda, fixable);
        if (scope == null) {
            throw new NullPointerException();
        }
        this.scope = scope;
        this.source = source;
    }

    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return super.invoke(proxy, method, args);
        } catch (AssertionError e) {
            return scope.getDollarParser().getErrorHandler().handle(scope, source, e);
        } catch (DollarException e) {
            return scope.getDollarParser().getErrorHandler().handle(scope, source, e);
        } catch (Exception e) {
            return scope.getDollarParser().getErrorHandler().handle(scope, source, e);
        }
    }


}
