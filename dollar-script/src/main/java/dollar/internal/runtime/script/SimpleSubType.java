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

import com.google.common.base.Objects;
import dollar.api.SubType;
import dollar.api.script.Source;
import org.jetbrains.annotations.NotNull;

public class SimpleSubType implements SubType {
    @NotNull
    private final String value;

    public SimpleSubType(@NotNull Source source) {value = source.getSourceSegment();}

    @NotNull
    @Override
    public String asString() {
        return value;
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleSubType that = (SimpleSubType) o;
        return Objects.equal(value, that.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
