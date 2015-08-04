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

package com.sillelien.dollar.api.types;

import com.sillelien.dollar.api.DollarException;
import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.collections.ImmutableList;
import com.sillelien.dollar.api.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class DollarError extends DollarVoid {

    private final ErrorType errorType;
    private final String errorMessage;

    public DollarError(@NotNull ImmutableList<Throwable> errors, ErrorType errorType,
                       String errorMessage) {
        super(errors);
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }

    public DollarError(ErrorType errorType, @NotNull Throwable t) {

        super(ImmutableList.of(t));
        this.errorType = errorType;
        this.errorMessage = t.getMessage();
    }


    public DollarError(ErrorType errorType, String errorMessage, Throwable t) {

        super(ImmutableList.of(t));
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }


    public DollarError(ErrorType errorType, String errorMessage) {

        super(ImmutableList.of(new DollarException(errorMessage)));
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }

    @Override public Type $type() {
        return Type.ERROR;
    }

    @Override public boolean is(@NotNull Type... types) {
        return Arrays.asList(types).contains(Type.ERROR) || Arrays.asList(types).contains(Type.VOID);
    }

    @Override public boolean isVoid() {
        return false;
    }

    @NotNull @Override public String toHumanString() {
        return errorType.toString() + " " + errorMessage + " " + errors();
    }

    @NotNull @Override public String toDollarScript() {
        return "ERROR('" + errorType.name() + "'," + DollarStatic.$(errorMessage).toDollarScript() + ")";
    }

    @Override public boolean isError() {
        return true;
    }

    @Nullable @Override public Object toJsonType() {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.putString("errorType", errorType.name());
        jsonObject.putString("errorMessage", errorMessage);
        return jsonObject;
    }
}
