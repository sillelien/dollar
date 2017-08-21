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

package com.sillelien.dollar.api.types;

import com.sillelien.dollar.api.exceptions.DollarFailureException;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;

public class ConstraintViolation extends DollarFailureException {
    public ConstraintViolation(@NotNull var constrainedVar,
                               @NotNull var constraint,
                               @NotNull String constrainedFingerprint,
                               @NotNull String constraintFingerPrint) {
        super(ErrorType.ASSERTION,
              "Constrained var fingerprint " + constrainedFingerprint + " did not match constraint fingerprint for " + constraintFingerPrint);
    }
}
