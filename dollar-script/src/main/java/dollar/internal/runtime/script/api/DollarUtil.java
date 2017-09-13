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

package dollar.internal.runtime.script.api;

import dollar.api.Pipeable;
import dollar.api.Scope;
import dollar.api.SubType;
import dollar.api.Type;
import dollar.api.VarFlags;
import dollar.api.VarKey;
import dollar.api.Variable;
import dollar.api.script.DollarParser;
import dollar.api.script.Source;
import dollar.api.var;
import dollar.internal.runtime.script.parser.Op;
import dollar.internal.runtime.script.parser.SourceNodeOptions;
import dollar.internal.runtime.script.parser.scope.ScriptScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jparsec.Token;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DollarUtil {

    @NotNull String ANSI_CYAN = "36";

    double MIN_PROBABILITY = 0.5;

    void addParameterstoCurrentScope(@NotNull Scope scope, @NotNull List<var> parameters);

    void addScope(boolean runtime, @NotNull Scope scope);

    void checkLearntType(@NotNull Token token, @Nullable Type type, @NotNull var rhs, @NotNull Double threshold);

    @NotNull var constrain(@NotNull Scope scope,
                           @NotNull var value,
                           @Nullable var constraint,
                           @Nullable SubType label);

    @NotNull String createId(@NotNull String operation);

    @NotNull Scope endScope(boolean runtime);

    @NotNull var fix(@Nullable var v, boolean parallel);

    @NotNull var fix(@Nullable var v);

    @NotNull Scope getRootScope();

    void setRootScope(@NotNull ScriptScope rootScope);

    @Nullable Scope getScopeForVar(boolean pure,
                                   @NotNull VarKey key,
                                   boolean numeric,
                                   @Nullable Scope initialScope);

    @NotNull var getVar(@NotNull VarKey key,
                        @NotNull UUID id,
                        @NotNull Scope scopeForKey,
                        @NotNull Source sourceCode,
                        boolean pure,
                        @NotNull var node);

    @NotNull String highlight(@NotNull String text, @NotNull String color);

    @NotNull <T> Optional<T> inScope(boolean runtime,
                                     @NotNull Scope parent,
                                     boolean pure,
                                     @NotNull String scopeName,
                                     @NotNull ScopeExecutable<T> r);

    @NotNull <T> Optional<T> inScope(boolean runtime,
                                     @NotNull Scope scope,
                                     @NotNull ScopeExecutable<T> r);

    @NotNull <T> Optional<T> inSubScope(boolean runtime, boolean pure, @NotNull String scopeName,
                                        @NotNull ScopeExecutable<T> r);

    String indent(int i);

    var node(@NotNull Op operation,
             @NotNull String name,
             boolean pure,
             @NotNull SourceNodeOptions sourceNodeOptions,
             @NotNull DollarParser parser,
             @NotNull Source source,
             @Nullable Type suggestedType, @NotNull List<var> inputs,
             @NotNull Pipeable pipeable);

    @NotNull var node(@NotNull Op operation,
                      boolean pure,
                      @NotNull DollarParser parser,
                      @NotNull Source source,
                      @NotNull List<var> inputs,
                      @NotNull Pipeable pipeable);

    @NotNull var node(@NotNull Op operation,
                      boolean pure,
                      @NotNull DollarParser parser,
                      @NotNull Token token,
                      @NotNull List<var> inputs,
                      @NotNull Pipeable callable);

    @NotNull var node(@NotNull Op operation,
                      @NotNull String name,
                      boolean pure,
                      @NotNull DollarParser parser,
                      @NotNull Token token,
                      @NotNull List<var> inputs,
                      @NotNull Pipeable callable);

    void popScope(@NotNull Scope scope);

    void pushScope(@NotNull Scope scope);

    String randomId();

    @NotNull var reactiveNode(@NotNull Op operation, @NotNull String name,
                              boolean pure, @NotNull SourceNodeOptions sourceNodeOptions,
                              @NotNull DollarParser parser,
                              @NotNull Source source,
                              @NotNull var lhs,
                              @NotNull var rhs,
                              @NotNull Pipeable callable);

    @NotNull var reactiveNode(@NotNull Op operation,
                              boolean pure,
                              @NotNull Token token,
                              @NotNull var lhs,
                              @NotNull var rhs,
                              @NotNull DollarParser parser,
                              @NotNull Pipeable callable);

    @NotNull var reactiveNode(@NotNull Op operation,
                              @NotNull String name,
                              boolean pure,
                              @NotNull Token token,
                              @NotNull var lhs,
                              @NotNull var rhs,
                              @NotNull DollarParser parser,
                              @NotNull Pipeable callable);

    @NotNull var reactiveNode(@NotNull Op operation,
                              boolean pure,
                              @NotNull DollarParser parser, @NotNull Source source,
                              @NotNull var lhs,
                              @NotNull var rhs,
                              @NotNull Pipeable callable);

    @NotNull var reactiveNode(@NotNull Op operation,
                              boolean pure,
                              @NotNull var lhs,
                              @NotNull Token token,
                              @NotNull DollarParser parser,
                              @NotNull Pipeable callable);

    @NotNull var reactiveNode(@NotNull Op operation,
                              boolean pure,
                              @NotNull Source source,
                              @NotNull DollarParser parser,
                              @NotNull var lhs,
                              @NotNull Pipeable callable);

    @NotNull Scope scope();

    @NotNull List<Scope> scopes();

    @NotNull Variable setVariable(@NotNull Scope scope,
                                  @NotNull VarKey key,
                                  @NotNull var value,
                                  @Nullable DollarParser parser,
                                  @NotNull Token token,
                                  @Nullable var useConstraint,
                                  @Nullable SubType useSource,
                                  @NotNull VarFlags varFlags);

    @NotNull String shortHash(@NotNull Token token);

    @NotNull
    Variable updateVariable(@NotNull Scope scope,
                            @NotNull VarKey key,
                            @NotNull var value,
                            @NotNull VarFlags varFlags, @Nullable var useConstraint,
                            @Nullable SubType useSource);

    @NotNull var variableNode(boolean pure, @NotNull VarKey key, @NotNull Token token, @NotNull DollarParser parser);

    @NotNull var variableNode(boolean pure, @NotNull VarKey key,
                              boolean numeric, @Nullable var defaultValue,
                              @NotNull Token token, @NotNull DollarParser parser);
}
