/*
 * Copyright (c) 2014 Neil Ellis
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

package me.neilellis.dollar;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class TypePrediction {
    private final String name;
    long total;
    HashMap<String, Long> values = new HashMap<>();

    public TypePrediction(String name) {

        this.name = name;
    }

    public void addCount(String type, long l) {
        total += l;
        if (values.containsKey(type)) {
            values.put(type, values.get(type) + l);
        } else {
            values.put(type, l);
        }
    }

    public long getCount(String type) {
        return values.get(type);
    }

    public Double probability(String type) {
        final Long value = values.get(type);
        if (value == null) {
            return 0.0;
        }
        return ((double) value) / total;
    }

    public String probableType() {
        long max = 0;
        String result = null;
        for (Map.Entry<String, Long> entry : values.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                result = entry.getKey();
            }
        }
        return result;
    }

    @Override public String toString() {
        return "TypePrediction{" +
               "name='" + name + '\'' +
               ", total=" + total +
               ", values=" + values +
               '}';
    }

    public long total() {
        return total;
    }

    public Set<String> types() {
        return values.keySet();
    }
}
