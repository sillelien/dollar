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

import dollar.api.script.SourceSegment;
import dollar.api.var;
import dollar.internal.runtime.script.parser.OpDef;
import org.jetbrains.annotations.NotNull;

public class PureFunctionException extends DollarScriptException {
    public PureFunctionException(@NotNull Throwable e) {
        super(e);
    }

    public PureFunctionException(@NotNull String errorMessage) {
        super(errorMessage);
    }

    public PureFunctionException(@NotNull Throwable t, @NotNull String s) {
        super(t, s);
    }

    public PureFunctionException(@NotNull String s, @NotNull var rhs) {
        super(s, rhs);
    }

    public PureFunctionException(@NotNull String s, @NotNull SourceSegment source) {
        super(s, source);
    }

    public PureFunctionException(@NotNull String s,
                                 @NotNull SourceSegment source,
                                 OpDef operation) {
        super(s, source, operation);
    }

    public PureFunctionException() {
        super("Attempted to perform an impure operation in a pure scope.");
    }
}
