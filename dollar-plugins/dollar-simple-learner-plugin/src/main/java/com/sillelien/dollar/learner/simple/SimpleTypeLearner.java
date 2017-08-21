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

package com.sillelien.dollar.learner.simple;

import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.TypePrediction;
import com.sillelien.dollar.api.execution.DollarExecutor;
import com.sillelien.dollar.api.plugin.Plugins;
import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.script.TypeLearner;
import com.sillelien.dollar.api.types.prediction.CountBasedTypePrediction;
import com.sillelien.dollar.api.var;
import dollar.internal.mapdb.DB;
import dollar.internal.mapdb.DBMaker;
import dollar.internal.mapdb.HTreeMap;
import dollar.internal.mapdb.Serializer;
import dollar.internal.runtime.script.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleTypeLearner implements TypeLearner {


    public static final int MAX_POSSIBLE_RETURN_VALUES = 5;
    @Nullable
    private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);



    private transient boolean modified;
    @Nullable
    private DB db;

    public SimpleTypeLearner() {
       start();
       Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }


    @NotNull
    @Override
    public TypeLearner copy() {
        return this;
    }

    @Override
    public void learn(@NotNull String name, @NotNull SourceSegment source, @NotNull List<var> inputs, @NotNull Type type) {
        final ArrayList<String> perms = TypeLearner.perms(inputs);
        for (String perm : perms) {
            HTreeMap<String, Long> usageCounters = getMap(name, perm);
            Long count = usageCounters.getOrDefault(type.toString(), 0L);
            usageCounters.put(type.toString(),count);
        }
        db.commit();
        modified = true;

    }

    @Nullable
    @Override
    public TypePrediction predict(@NotNull String name, @NotNull SourceSegment source, @NotNull List<var> inputs) {
        final ArrayList<String> perms = TypeLearner.perms(inputs);
        CountBasedTypePrediction prediction = new CountBasedTypePrediction(name);

        for (String perm : perms) {
            HTreeMap<String, Long> tally = getMap(name, perm);
            for (Object type : tally.keySet()) {
                String typeStr = type.toString();
                prediction.addCount(Type.valueOf(typeStr), tally.getOrDefault(typeStr,0L));
                if (prediction.types().size() > MAX_POSSIBLE_RETURN_VALUES) {
                    return new TypePrediction() {
                        @Override
                        public boolean empty() {
                            return false;
                        }

                        @NotNull
                        @Override
                        public Double probability(@NotNull Type type) {
                            return 1.0;
                        }

                        @Nullable
                        @Override
                        public Type probableType() {
                            return Type._ANY;
                        }

                        @NotNull
                        @Override
                        public Set<Type> types() {
                            return new HashSet<>(Collections.singletonList(Type._ANY));
                        }
                    };
                }
            }
        }
        return prediction;

    }

    @NotNull
    private HTreeMap<String, Long> getMap(@NotNull String name, @NotNull String perm) {
        assert db != null;
        return db.hashMap(name + "." + perm, Serializer.STRING, Serializer.LONG).createOrOpen();
    }

    @Override
    public void start() {
        db = DBMaker.fileDB(new File(FileUtil.getTempDir("types"), "typelearner.db")).fileMmapEnable().make();
    }

    @Override
    public void stop() {
        if (db != null) {
            db.close();
            db= null;
        }
    }

}
