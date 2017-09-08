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

import dollar.api.script.SourceSegment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface Scope {

    interface Listener extends Pipeable {
        @NotNull
        String getId();
    }

    @NotNull
    var addErrorHandler(@NotNull var handler);

    void addListener(@NotNull String key, @NotNull Listener listener);

    /**
     * Remove all variables, listeners etc from this scope.
     */
    void clear();

    @Nullable var constraint(@NotNull String key);

    @Nullable
    String constraintSource(@NotNull String key);

    @NotNull
    Scope copy();

    void destroy();

    /**
     * Returns a {@link DollarClass} definition named 'name'.
     *
     * @param name the name of the Class
     * @return the {@link DollarClass} definition
     */
    @NotNull DollarClass dollarClassByName(@NotNull String name);

    /**
     * The file associated with this scope or one of it's ancestors.
     *
     * @return a file name that is passable to new File().
     */
    @Nullable String file();

    /**
     * Get a variable's value by name.
     *
     * @param key the name of the variable
     * @return the value of the variable or $void() unless mustFind is true, in which case a VariableNotFoundException is thrown
     */
    @NotNull var get(@NotNull String key, boolean mustFind);

    /**
     * Get a variable's value by name.
     *
     * @param key the name of the variable
     * @return the value of the variable or $void()
     */
    @NotNull var get(@NotNull String key);

    @NotNull var handleError(@NotNull Throwable t);

    @NotNull
    var handleError(@NotNull Throwable t, @NotNull var context);

    @NotNull
    var handleError(@NotNull Throwable t, @NotNull SourceSegment context);

    /**
     * Return's true if this scope contains a variable.
     *
     * @param key the name of the variable
     * @return true if in this scope or a parent
     */
    boolean has(@NotNull String key);

    boolean hasParameter(@NotNull String key);

    /**
     * Returns true if the supplied scope is this, or an ancestor of this, scope.
     *
     * @param scope the scope
     * @return true if the scope is an ancestor or this scope
     */
    boolean hasParent(@Nullable Scope scope);

    /**
     * Returns true if the scope is being captured to create a class instance.
     *
     * @return true if class scope
     */
    boolean isClassScope();

    boolean isRoot();

    /**
     * Listen to a variable ('key') if the variable changes then {@link var#$notify()} will
     * be called.
     *
     * @param key      the name of the variable
     * @param id       an id to associate with this listener
     * @param listener the var object to be notified
     */
    void listen(@NotNull String key, @NotNull String id, @NotNull var listener);

    /**
     * Listen to a variable ('key') if the variable changes then {@link Pipeable#pipe(var...)}
     * be called.
     *
     * @param key      the name of the variable
     * @param id       an id to associate with this listener
     * @param listener the var object to be notified
     */
    void listen(@NotNull String key, @NotNull String id, @NotNull Pipeable listener);

    /**
     * Notify all listeners that the value of a variable has changed.
     *
     * @param key the name of the variable
     * @return its new value
     */
    @Nullable var notify(@NotNull String key);

    /**
     * Notify all listeners that the value of a variable has changed
     *
     * @param key   the name of the variable
     * @param value its new value
     */
    void notifyScope(@NotNull String key, @NotNull var value);


    /**
     * Gets the value of a parameter from this scope or it's ancestors.
     *
     * @param key the name of the parameter
     * @return the value associated or throws an exception if not found.
     */
    @NotNull
    Variable parameter(@NotNull String key);

    /**
     * Add a parameter to this scope.
     *
     * @param key   the name of the parameter
     * @param value the value of the parameter
     * @return the value
     */
    @NotNull
    Variable parameter(@NotNull String key, @NotNull var value);

    @NotNull List<var> parametersAsVars();

    /**
     * Return the parent scope or null if it does not have one.
     *
     * @return null if no parent.
     */
    @Nullable
    Scope parent();

    /**
     * Is this scope a 'pure' scope, pure in the sense of pure functions. Pure scopes are created using the 'pure' operator.
     *
     * @return true if this is a pure scope
     */
    boolean pure();

    /**
     * Register a class definition
     *
     * @param name        the name of the class
     * @param dollarClass the class definition
     */
    void registerClass(@NotNull String name, @NotNull DollarClass dollarClass);

    /**
     * Find which ancestor scope, or this scope, that contains a variable with the name.
     *
     * @param key name of the variable.
     * @return the Scope associated with the variable or null of none found
     */
    @Nullable Scope scopeForKey(@NotNull String key);

    /**
     * Creates or updates a variable.
     *
     * @param key              the name of the variable
     * @param value            the value to assign
     * @param constraint       a constraint on this variable that will be checked on assignment
     * @param constraintSource an arbitrary string with a near one to one mapping with the source of the constraint
     * @param varType
     * @return the variable definition
     */
    @NotNull Variable set(@NotNull String key,
                          @NotNull var value,
                          @Nullable var constraint,
                          @Nullable String constraintSource,
                          @NotNull VarType varType);

    /**
     * Returns the Dollar source code for this scope.
     *
     * @return the source code
     */
    @Nullable String source();

    /**
     * Returns the details of a variable.
     *
     * @param key the name of the variable
     * @return the full Variable definition
     */
    @NotNull Variable variable(@NotNull String key);

    @NotNull <T> Map<String, T> variables();
}
