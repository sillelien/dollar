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

package dollar.api.types.prediction;

import dollar.api.Type;
import dollar.api.TypePrediction;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class SingleValueTypePrediction implements TypePrediction {

    @NotNull
    private final Type type;

    public SingleValueTypePrediction(@NotNull Type type) {this.type = type;}

    @Override
    public boolean empty() {
        return false;
    }

    @Override
    public String name() {
        return "single-value";
    }

    @NotNull
    @Override
    public Double probability(@NotNull Type type) {
        return type.is(this.type) ? 1.0 : 0.0;
    }

    @Override
    public Type probableType() {
        return type;
    }

    @NotNull
    @Override
    public Set<Type> types() {
        return Collections.singleton(type);
    }

}
