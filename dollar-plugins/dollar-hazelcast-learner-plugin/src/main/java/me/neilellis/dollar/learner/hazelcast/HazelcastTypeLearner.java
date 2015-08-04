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

package com.sillelien.dollar.learner.hazelcast;

import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.TypePrediction;
import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.script.TypeLearner;
import com.sillelien.dollar.api.types.prediction.CountBasedTypePrediction;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HazelcastTypeLearner implements TypeLearner {


    private final HazelcastInstance hz;

    public HazelcastTypeLearner() {
        final XmlConfigBuilder
                configBuilder =
                new XmlConfigBuilder(getClass().getResourceAsStream("/hazelcast.xml"));

        hz = Hazelcast.newHazelcastInstance(configBuilder.build());
    }

    @NotNull @Override public TypeLearner copy() {
        return this;
    }

    @Override public void learn(String name, SourceSegment source, @NotNull List<var> inputs, Type type) {
        IMap<String, CountBasedTypePrediction> typeLearning = hz.getMap("typeLearner");
        final ArrayList<String> perms = TypeLearner.perms(inputs);
        for (String perm : perms) {
            final String key = createKey(name, perm);
            final CountBasedTypePrediction
                    usageCounters =
                    typeLearning.getOrDefault(key, new CountBasedTypePrediction(name));
            usageCounters.addCount(type, 1);
            typeLearning.put(key, usageCounters);
        }

    }

    @Nullable @Override public TypePrediction predict(String name, SourceSegment source, @NotNull List<var> inputs) {
        IMap<String, CountBasedTypePrediction> typeLearning = hz.getMap("typeLearner");
        final ArrayList<String> perms = TypeLearner.perms(inputs);
        CountBasedTypePrediction prediction = new CountBasedTypePrediction(name);
        try {
            for (String perm : perms) {
                final CountBasedTypePrediction tally = typeLearning.get(createKey(name, perm));
                if (tally != null) {
                    for (Type type : tally.types()) {
                        prediction.addCount(type, tally.getCount(type));
                        System.out.println("Count for " +
                                           name +
                                           " type " +
                                           perm +
                                           " with prediction " +
                                           type +
                                           " was " +
                                           tally.getCount(type));
                    }
                }
            }

            return prediction;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @NotNull public String createKey(String name, String perm) {return name + ":" + perm;}

    @Override public void start() {

    }

    @Override public void stop() {

    }

}
