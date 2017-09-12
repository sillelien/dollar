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

import java.util.UUID;

public class VarKey {

    @NotNull
    public static final VarKey COLLECTED = of("collected");
    @NotNull
    public static final VarKey COUNT = of("count");
    @NotNull
    public static final VarKey IT = of("it");
    @NotNull
    public static final VarKey ONE = of(1);
    @NotNull
    public static final VarKey PREVIOUS = of("previous");
    @NotNull
    public static final VarKey STAR = of("*");
    @NotNull
    public static final VarKey THIS = of("this");
    @NotNull
    public static final VarKey THREE = of(3);
    @NotNull
    public static final VarKey TWO = of(2);
    @NotNull
    private final String key;


    private VarKey(@NotNull String key) {
        this.key = removePrefix(key);
    }

    public static VarKey of(@NotNull String key) {
        return new VarKey(key);
    }

    public static VarKey of(int i) {
        return new VarKey(String.valueOf(i));
    }

    public static VarKey of(@NotNull var variableName) {
        return VarKey.of(variableName.toString());
    }

    public static VarKey random() {
        return new VarKey(UUID.randomUUID().toString());
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
        return key;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VarKey varKey = (VarKey) o;
        return Objects.equal(key, varKey.key);
    }

    @NotNull
    public String toString() {
        return key;
    }

    public boolean isNumeric() {
        return key.matches("[0-9]+");
    }
}
