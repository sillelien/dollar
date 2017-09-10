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

package dollar.api.types;

import dollar.api.DollarStatic;
import dollar.api.Type;
import dollar.api.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DollarError extends DollarVoid {

    @NotNull
    private final String errorMessage;
    @NotNull
    private final ErrorType errorType;

    public DollarError(@NotNull ErrorType errorType,
                       @NotNull String errorMessage) {
        super();
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }

    public DollarError(@NotNull ErrorType errorType, @NotNull Throwable t) {

        super();
        this.errorType = errorType;
        errorMessage = t.getMessage();
    }


    public DollarError(@NotNull ErrorType errorType, @NotNull String errorMessage, @NotNull Throwable t) {

        super();
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }

    @NotNull
    @Override
    public Type $type() {
        return Type._ERROR;
    }

    @Override
    public boolean is(@NotNull Type... types) {
        for (Type type : types) {
            if (type.is(Type._ERROR) || type.is(Type._VOID)) return true;
        }
        return false;
    }

    @Override
    public boolean isVoid() {
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), errorType, errorMessage);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;
        if (!super.equals(o)) return false;
        DollarError that = (DollarError) o;
        return (errorType == that.errorType) &&
                       Objects.equals(errorMessage, that.errorMessage);
    }

    @NotNull
    @Override
    public String toDollarScript() {
        return "ERROR('" + errorType.name() + "'," + DollarStatic.$(errorMessage).toDollarScript() + ")";
    }

    @NotNull
    @Override
    public String toHumanString() {
        return errorType + " " + errorMessage;
    }

    public boolean isError() {
        return true;
    }

    @Nullable
    @Override
    public Object toJsonType() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.putString("errorType", errorType.name());
        jsonObject.putString("errorMessage", errorMessage);
        return jsonObject;
    }
}
