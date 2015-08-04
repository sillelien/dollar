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
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CountBasedTypePrediction implements TypePrediction, Serializable {
    @NotNull private final HashMap<Type, Long> values = new HashMap<>();
    private String name;
    private long total;

    public CountBasedTypePrediction() {
    }

    public CountBasedTypePrediction(String name) {

        this.name = name;
    }

    public void addCount(Type type, long l) {
        total += l;
        if (values.containsKey(type)) {
            values.put(type, values.get(type) + l);
        } else {
            values.put(type, l);
        }
    }

    @Override public boolean empty() {
        return values.isEmpty();
    }

    @NotNull @Override public Double probability(Type type) {
        final Long value = values.get(type);
        if (value == null) {
            return 0.0;
        }
        return ((double) value) / total;
    }

    @Nullable @Override public Type probableType() {
        long max = 0;
        Type result = null;
        for (Map.Entry<Type, Long> entry : values.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                result = entry.getKey();
            }
        }
        return result;
    }

    @NotNull @Override public Set<Type> types() {
        return values.keySet();
    }

    public long getCount(Type type) {
        return values.get(type);
    }

    @NotNull @Override public String toString() {
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
