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

package dollar.uri.mapdb;

import dollar.api.DollarStatic;
import dollar.api.Type;
import dollar.api.Value;
import dollar.api.types.DollarFactory;
import dollar.internal.mapdb.DataInput2;
import dollar.internal.mapdb.DataOutput2;
import dollar.internal.mapdb.serializer.GroupSerializerObjectArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;

public class VarSerializer extends GroupSerializerObjectArray<Value> implements Serializable {
    @Override
    public void serialize(@NotNull DataOutput2 out, @Nullable Value value) throws IOException {
        if ((value != null) && !value.isVoid()) {
            out.writeUTF(value.$type().name());
            out.writeUTF(DollarFactory.serialize(value));
        } else {
            out.writeUTF(Type._VOID.name());
            out.writeUTF("");
        }
    }

    @NotNull
    @Override
    public Value deserialize(@NotNull DataInput2 in, int available) throws IOException {
        final Type type = Type.of(in.readUTF());
        if (type.is(Type._VOID)) {
            in.readUTF();
            return DollarStatic.$void();
        } else {
            final String s = in.readUTF();
            return DollarFactory.deserialize(s);
        }
    }


    @Override
    public int fixedSize() {
        return -1;
    }
}
