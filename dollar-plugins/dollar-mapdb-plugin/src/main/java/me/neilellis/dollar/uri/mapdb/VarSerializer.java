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

package me.neilellis.dollar.uri.mapdb;

import me.neilellis.dollar.Type;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import static me.neilellis.dollar.DollarStatic.$void;

public class VarSerializer implements Serializer<var>, Serializable {
    @Override public void serialize(@NotNull DataOutput out, @Nullable var value) throws IOException {
        if (value != null && !value.isVoid()) {
            out.writeUTF(value.$type().name());
            out.writeUTF(DollarFactory.serialize(value));
        } else {
            out.writeUTF(Type.VOID.name());
            out.writeUTF("");
        }
    }

    @NotNull @Override public var deserialize(@NotNull DataInput in, int available) throws IOException {
        final Type type = Type.valueOf(in.readUTF());
        if (Objects.equals(type, Type.VOID)) {
            in.readUTF();
            return $void();
        } else {
            final String s = in.readUTF();
            return DollarFactory.deserialize(s);
        }
    }

    @Override public int fixedSize() {
        return -1;
    }
}
