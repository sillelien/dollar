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

package dollar.api.types.meta;

import dollar.api.MetaKey;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"UtilityClassCanBeSingleton", "UtilityClassCanBeEnum"})
public final class MetaConstants {

    @NotNull
    public static final MetaKey ASSIGNMENT_TYPE = MetaKey.of("dollar.internal.assignment.type");

    @NotNull
    public static final MetaKey CONSTRAINT_FINGERPRINT = MetaKey.of("dollar.internal.constraint.fingerprint");

    @NotNull
    public static final MetaKey CONSTRAINT_SOURCE = MetaKey.of("dollar.internal.constraint.source");

    @NotNull
    public static final MetaKey ID = MetaKey.of("dollar.internal.id");

    @NotNull
    public static final MetaKey IMPURE = MetaKey.of("dollar.internal.pure");

    @NotNull
    public static final MetaKey IS_BUILTIN = MetaKey.of("dollar.internal.builtin");

    @NotNull
    public static final MetaKey OPERATION = MetaKey.of("dollar.internal.operation");

    @NotNull
    public static final MetaKey OPERATION_NAME = MetaKey.of("dollar.internal.operation.name");

    @NotNull
    public static final MetaKey SCOPES = MetaKey.of("dollar.internal.scopes");

    @NotNull
    public static final MetaKey TYPE_HINT = MetaKey.of("dollar.internal.type.hint");

    @NotNull
    public static final MetaKey VARIABLE = MetaKey.of("dollar.internal.variable");

}
