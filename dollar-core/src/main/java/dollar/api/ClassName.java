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

package dollar.api;

import com.google.common.base.Objects;
import org.jetbrains.annotations.NotNull;

public class ClassName {

    @NotNull
    private final String value;


    private ClassName(@NotNull String value) {
        this.value = removePrefix(value);
    }

    public static ClassName of(@NotNull String key) {
        return new ClassName(key);
    }

    public static ClassName of(@NotNull Object key) {
        return new ClassName(key.toString());
    }


    public static ClassName of(@NotNull Value variableName) {
        return ClassName.of(variableName.toString());
    }


    @NotNull
    public static String removePrefix(@NotNull String key) {
        if (key.startsWith("_")) {
            return key.substring(1);
        } else {
            return key;
        }
    }

    @NotNull
    public String asString() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassName varKey = (ClassName) o;
        return Objects.equal(value, varKey.value);
    }

    @NotNull
    public String toString() {
        return value;
    }


}
