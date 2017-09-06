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

package dollar.internal.runtime.script.api.exceptions;

import dollar.api.exceptions.DollarFailureException;
import dollar.api.script.SourceSegment;
import dollar.api.types.ErrorType;
import dollar.api.var;
import dollar.internal.runtime.script.parser.OpDef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DollarScriptException extends DollarFailureException {
    @NotNull
    private String rawMessage;
    @Nullable
    private SourceSegment source;
    @Nullable
    private OpDef operation;

    public DollarScriptException(@NotNull Throwable e) {
        super(e);
    }

    public DollarScriptException(@NotNull String errorMessage) {
        super(errorMessage);
    }

    public DollarScriptException(@NotNull Throwable t, @NotNull String s) {
        super(t, s);
    }

    public DollarScriptException(@NotNull String s, @NotNull var rhs) {
        super(s + ":\n" + optionalSource(rhs.source()));
        rawMessage = s;
        source = rhs.source();
    }

    public DollarScriptException(@NotNull String s, @NotNull SourceSegment source) {
        super(s + ":\n" + optionalSource(source));
        rawMessage = s;
        this.source = source;
    }

    public DollarScriptException(@NotNull ErrorType errorType, @NotNull SourceSegment source) {
        super(errorType, optionalSource(source));
        rawMessage = errorType.name();
        this.source = source;
    }

    public DollarScriptException(@NotNull ErrorType errorType, @NotNull String additional, @NotNull SourceSegment source) {
        super(errorType, additional + ":\n" + optionalSource(source));
        rawMessage = errorType.name();
        this.source = source;
    }

    public DollarScriptException(@NotNull String s, @Nullable SourceSegment source, OpDef operation) {
        super((s + ":\n" + optionalSource(source) + "\n\n") + (operation != null ? operation.helpText() : ""));
        rawMessage = s;
        this.source = source;
        this.operation = operation;
    }


    public DollarScriptException(Throwable cause, @Nullable SourceSegment source) {
        super(cause, cause.getMessage() + ":\n" + optionalSource(source));
        this.source = source;
    }

    public DollarScriptException(Throwable cause, var context) {
        super(cause, context.source().getSourceMessage());
        source = context.source();
    }

    @NotNull
    private static String optionalSource(@Nullable SourceSegment source) {
        return (source == null) ? "" : source.getSourceMessage();
    }

    public String rawMessage() {
        return rawMessage;
    }

    public SourceSegment source() {
        return source;
    }

    public OpDef operation() {
        return operation;
    }
}
