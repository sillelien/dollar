/*
 * Copyright (c) 2014-2015 Neil Ellis
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

package com.sillelien.dollar.api.types.prediction;

import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.TypePrediction;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AnyTypePrediction implements TypePrediction {

    public static final HashSet<Type> TYPES = new HashSet<>(Arrays.asList(Type.values()));

    @Override public boolean empty() {
        return false;
    }

    @NotNull @Override public Double probability(@NotNull Type type) {
        if (type.equals(Type.ANY)) {
            return 1.0;
        }
        return 1.0d / Type.values().length;
    }

    @Override public Type probableType() {
        return Type.ANY;
    }

    @NotNull @Override public Set<Type> types() {
        return TYPES;
    }
}
