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

import org.jetbrains.annotations.NotNull;

public class SourceNodeOptions {
    @NotNull
    public static final SourceNodeOptions NO_SCOPE = new SourceNodeOptions(false, false, false);
    @NotNull
    public static final SourceNodeOptions NEW_SCOPE = new SourceNodeOptions(true, false, false);
    @NotNull
    public static final SourceNodeOptions SCOPE_WITH_CLOSURE = new SourceNodeOptions(true, true, false);


    private final boolean newScope;
    private final boolean scopeClosure;
    private final boolean parallel;

    public SourceNodeOptions(boolean newScope, boolean scopeClosure, boolean parallel) {
        this.newScope = newScope;
        this.scopeClosure = scopeClosure;
        this.parallel = parallel;
    }

    public boolean isNewScope() {
        return newScope;
    }

    public boolean isScopeClosure() {
        return scopeClosure;
    }

    public boolean isParallel() {
        return parallel;
    }
}
