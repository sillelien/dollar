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
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class CountBasedTypePrediction implements TypePrediction, Serializable {
    @NotNull
    private final HashMap<String, AtomicLong> values = new HashMap<>();
    @NotNull
    private String name;
    private long total;

    public CountBasedTypePrediction(@NotNull String name, Map<String, AtomicLong> typeAtomicLongMap) {
        values.putAll(typeAtomicLongMap);
    }

    public CountBasedTypePrediction(@NotNull String name) {

        this.name = name;
    }

    public void addCount(@NotNull Type type, long l) {
        total += l;
        if (values.containsKey(type)) {
            values.get(type.toString()).addAndGet(l);
        } else {
            values.put(type.toString(), new AtomicLong(l));
        }
    }

    @Override
    public boolean empty() {
        return values.isEmpty();
    }

    @NotNull
    @Override
    public Double probability(@NotNull Type type) {
        final AtomicLong value = values.get(type);
        if (value == null) {
            return 0.0;
        }
        return value.doubleValue() / total;
    }

    @Nullable
    @Override
    public Type probableType() {
        long max = 0;
        Type result = null;
        for (Map.Entry<String, AtomicLong> entry : values.entrySet()) {
            if (entry.getValue().get() > max) {
                max = entry.getValue().get();
                result = Type.of(entry.getKey());
            }
        }
        return result;
    }

    @NotNull
    @Override
    public Set<Type> types() {
        return values.keySet().stream().map(Type::of).collect(Collectors.toSet());
    }

    public long getCount(@NotNull Type type) {
        return values.get(type).get();
    }

    @NotNull
    @Override
    public String toString() {
        return "TypePrediction{" +
                       "name='" + name + '\'' +
                       ", total=" + total +
                       ", values=" + values +
                       '}';
    }

    public long total() {
        return total;
    }
}
