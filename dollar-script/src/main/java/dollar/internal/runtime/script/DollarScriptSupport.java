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

import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.var;
import com.sillelien.dollar.api.Scope;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.api.exceptions.DollarScriptException;
import dollar.internal.runtime.script.api.exceptions.VariableNotFoundException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import static com.sillelien.dollar.api.DollarStatic.$;

public class DollarScriptSupport {

    private static final Logger log = LoggerFactory.getLogger(DollarScriptSupport.class);
    private static final ThreadLocal<List<Scope>> scopes = new ThreadLocal<List<Scope>>() {
        @NotNull
        @Override
        protected List<Scope> initialValue() {
            ArrayList<Scope> list = new ArrayList<>();
            return list;
        }
    };


    public static List<Scope> scopes() {
        return scopes.get();
    }

    private static void addScope(Scope scope) {
        scopes.get().add(scope);
    }

    private static Scope endScope() {
        return scopes.get().remove(scopes.get().size() - 1);
    }

    @NotNull
    public static var wrapReactive(@NotNull Callable<var> callable, Token token,
                                   String operation,
                                   @NotNull var lhs, DollarParser parser) {

        return wrapReactive(callable,new SourceSegmentValue(currentScope(),token), operation,lhs, parser);
    }

    @NotNull
    public static var wrapReactive(@NotNull Callable<var> callable, SourceSegment source,
                                   String operation,
                                   @NotNull var lhs, DollarParser parser) {

        final var lambda = toLambda(currentScope(), callable, source, Arrays.asList(lhs), operation, parser);
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }



    @NotNull public static var toLambda(@Nullable Scope scope, @NotNull Callable<var> callable, SourceSegment source,
                                        List<var> inputs,
                                        String operation, DollarParser parser) {
        if (scope == null) {
            throw new NullPointerException();
        }
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarSource(i -> callable.call(), scope, source, inputs, operation, parser)));
    }

    @NotNull public static var toLambda(@NotNull Callable<var> callable, Token token,
                                        List<var> inputs,
                                        String operation, DollarParser parser) {
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarSource(i -> callable.call(), currentScope(), new SourceSegmentValue(currentScope(),token), inputs, operation, parser)));
    }

    @NotNull
    public static var wrapReactive(@NotNull Callable<var> callable, SourceSegment source, String operation,
                                   @NotNull var lhs,
                                   @NotNull var rhs, DollarParser parser) {
        final var lambda = toLambda(currentScope(), callable, source, Arrays.asList(lhs, rhs), operation, parser);

        rhs.$listen(i -> lambda.$notify());
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }

    @NotNull
    public static var wrapReactive(@NotNull Callable<var> callable, Token token, String operation,
                                   @NotNull var lhs,
                                   @NotNull var rhs, DollarParser parser) {
        final var lambda = toLambda(currentScope(), callable, new SourceSegmentValue(currentScope(),token), Arrays.asList(lhs, rhs), operation, parser);

        rhs.$listen(i -> lambda.$notify());
        lhs.$listen(i -> lambda.$notify());
        return lambda;
    }


    @NotNull
    public static var wrapLambda(Token token, @NotNull Pipeable pipeable, List<var> inputs,
                                 String operation, DollarParser parser) {
        return DollarFactory.wrap((var) java.lang.reflect.Proxy.newProxyInstance(
                DollarStatic.class.getClassLoader(),
                new Class<?>[]{var.class},
                new DollarSource(pipeable::pipe, currentScope(), new SourceSegmentValue(currentScope(),token), inputs, operation, parser)));
    }


    @NotNull
    public static Scope currentScope() {
        return scopes.get().get(scopes.get().size() - 1);
    }

    @SuppressWarnings("ThrowFromFinallyBlock")
    public static  <T> T  inScope(boolean pure, String scopeName,  @NotNull Function<Scope, T> r) {
        Scope currentScope= currentScope();
        Scope newScope;
        if (pure) {
            newScope = new PureScope(currentScope, currentScope.getSource(), scopeName, currentScope.getFile());
        } else {
            if ((currentScope instanceof PureScope)) {
                throw new IllegalStateException("trying to switch to an impure scope in a pure scope.");
            }
            newScope = new ScriptScope(currentScope, currentScope.getFile(), currentScope.getSource(), scopeName);
        }
        addScope(currentScope);
        addScope(newScope);
        try {
            return r.apply(newScope);
        } finally {
            Scope poppedScope = endScope();
            if (poppedScope != newScope) {
                throw new IllegalStateException("Popped wrong scope");
            }
            final Scope poppedScope2 = endScope();
            if (poppedScope2 != currentScope) {
                throw new IllegalStateException("Popped wrong scope");
            }
        }
    }


    @SuppressWarnings("ThrowFromFinallyBlock")
    public static  <T> T  inScope(Scope scope,  @NotNull Function<Scope,T> r) throws Exception {

        log.debug("CHANGING SCOPE TO "+scope);
        addScope(currentScope());
        addScope(scope);
        try {
            return r.apply(scope);
        } finally {
            Scope poppedScope = endScope();
            if (poppedScope != scope) {
                throw new IllegalStateException("Popped wrong scope");
            }
            final Scope poppedScope2 = endScope();
            if (poppedScope2 != currentScope()) {
                throw new IllegalStateException("Popped wrong scope");
            }
        }
    }

    @Nullable
    public static var getVariable(boolean pure, String key, boolean numeric,
                                  @Nullable var defaultValue,
                                  @NotNull Token token, DollarParser parser) {
//       if(pure && !(scope instanceof  PureScope)) {
//           throw new IllegalStateException("Attempting to get a pure variable in an impure scope");
//       }
        SourceSegment source= new SourceSegmentValue(currentScope(),token);

        var lambda[] =new var[1];
        lambda[0]=toLambda(() -> {
            Scope scope= currentScope();
            scope.listen(key, lambda[0]);
            log.debug("LOOKUP "+key+" "+scope+" "+currentScope());
            if (scope.has(key)) {
                return scope.get(key);
            }
            try {
                List<Scope> scopes = new ArrayList<>(DollarScriptSupport.scopes.get());
                Collections.reverse(scopes);
                for (Scope scriptScope : scopes) {
                    if (!(scriptScope instanceof PureScope) && pure) {
                        log.debug("Skipping " + scriptScope);
                    }
                    if (numeric) {
                        if (scriptScope.hasParameter(key)) {
                            return scriptScope.getParameter(key);
                        }
                    } else {
                        if (scriptScope.has(key)) {
                            return scriptScope.get(key);
                        }
                    }
                }
            } catch (AssertionError e) {
                return parser.getErrorHandler().handle(scope, null, e);
            } catch (DollarScriptException e) {
                return parser.getErrorHandler().handle(scope, null, e);
            } catch (Exception e) {
                return parser.getErrorHandler().handle(scope, null, e);
            }
            if (numeric) {
                return scope.getParameter(key);
            }

                if (defaultValue != null) {
                    return defaultValue;
                } else {
                    throw new VariableNotFoundException(key, scope);
                }
        }, token, $(key).$list().toVarList().mutable(), "get-variable-" + key + "-" + source.getShortHash(), parser);
        lambda[0].setMetaAttribute("variable", key);
        return lambda[0];

    }

    public static void setRootScope(ScriptScope rootScope) {
        scopes.get().clear();
        scopes.get().add(0,rootScope);
    }

    public static Scope getRootScope() {
        return scopes.get().get(0);
    }
}
