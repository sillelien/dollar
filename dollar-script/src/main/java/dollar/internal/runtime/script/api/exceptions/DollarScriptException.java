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

import dollar.api.Value;
import dollar.api.exceptions.DollarFailureException;
import dollar.api.script.Source;
import dollar.api.types.ErrorType;
import dollar.internal.runtime.script.parser.Op;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DollarScriptException extends DollarFailureException {
    @Nullable
    private Op operation;
    @NotNull
    private String rawMessage;
    @Nullable
    private Source source;

    public DollarScriptException(@NotNull Throwable e) {
        super(e);
    }

    public DollarScriptException(@NotNull String errorMessage) {
        super(errorMessage);
    }

    public DollarScriptException(@NotNull Throwable t, @NotNull String s) {
        super(t, s);
    }

    public DollarScriptException(@NotNull String s, @NotNull Value rhs) {
        super(s + ":\n" + optionalSource(rhs.source()));
        rawMessage = s;
        source = rhs.source();
    }

    public DollarScriptException(@NotNull String s, @NotNull Source source) {
        super(s + ":\n" + optionalSource(source));
        rawMessage = s;
        this.source = source;
    }

    public DollarScriptException(@NotNull ErrorType errorType, @NotNull Source source) {
        super(errorType, optionalSource(source));
        rawMessage = errorType.name();
        this.source = source;
    }

    public DollarScriptException(@NotNull ErrorType errorType, @NotNull String additional, @NotNull Source source) {
        super(errorType, additional + ":\n" + optionalSource(source));
        rawMessage = errorType.name();
        this.source = source;
    }

    public DollarScriptException(@NotNull String s, @Nullable Source source, @NotNull Op operation) {
        super((s + ":\n" + optionalSource(source) + "\n\n") + (operation != null ? operation.helpText() : ""));
        rawMessage = s;
        this.source = source;
        this.operation = operation;
    }


    public DollarScriptException(@NotNull Throwable cause, @Nullable Source source) {
        super(cause, cause.getMessage() + ":\n" + optionalSource(source));
        this.source = source;
    }

    public DollarScriptException(@NotNull Throwable cause, @NotNull Value context) {
        super(cause, context.source().getSourceMessage());
        source = context.source();
    }

    @NotNull
    private static String optionalSource(@Nullable Source source) {
        return (source == null) ? "" : source.getSourceMessage();
    }

    @NotNull
    public Op operation() {
        return operation;
    }

    @NotNull
    public String rawMessage() {
        return rawMessage;
    }

    @NotNull
    public Source source() {
        return source;
    }
}
