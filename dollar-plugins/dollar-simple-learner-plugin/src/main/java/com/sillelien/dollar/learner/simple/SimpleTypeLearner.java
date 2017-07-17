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

package com.sillelien.dollar.learner.simple;

import com.sillelien.dollar.api.Type;
import com.sillelien.dollar.api.TypePrediction;
import com.sillelien.dollar.api.execution.DollarExecutor;
import com.sillelien.dollar.api.plugin.Plugins;
import com.sillelien.dollar.api.script.SourceSegment;
import com.sillelien.dollar.api.script.TypeLearner;
import com.sillelien.dollar.api.types.prediction.AnyTypePrediction;
import com.sillelien.dollar.api.types.prediction.CountBasedTypePrediction;
import com.sillelien.dollar.api.var;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SimpleTypeLearner implements TypeLearner {


    static {
    }
    public static final int MAX_POSSIBLE_RETURN_VALUES = 5;
    @Nullable
    private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);

    private transient boolean modified;
    @NotNull
    private ConcurrentHashMap<String, CountBasedTypePrediction> map = new ConcurrentHashMap<>();

    public SimpleTypeLearner() {
        File file = new File(System.getProperty("user.home") + "/.dollar/typelearning.xml");
        File backupFile = new File(System.getProperty("user.home") + "/.dollar/typelearning.backup.xml");
        file.getParentFile().mkdirs();
        assert executor != null;
        XStream xStream = new XStream();
        xStream.addPermission(AnyTypePermission.ANY);
        executor.scheduleEvery(200, () -> {
            if (modified) {

                try (FileOutputStream out = new FileOutputStream(file)) {
                    xStream.toXML(map, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try (FileOutputStream out = new FileOutputStream(backupFile)) {
                    xStream.toXML(map, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        if (file.exists()) {
            try {
                map = (ConcurrentHashMap<String, CountBasedTypePrediction>) xStream.fromXML(file);
            } catch (Exception e) {
                file.delete();
                if (backupFile.exists()) {
                    try {
                        map = (ConcurrentHashMap<String, CountBasedTypePrediction>) xStream.fromXML(backupFile);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        } else if (backupFile.exists()) {
            try {
                map = (ConcurrentHashMap<String, CountBasedTypePrediction>) xStream.fromXML(backupFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @NotNull
    @Override
    public TypeLearner copy() {
        return this;
    }

    @Override
    public void learn(String name, SourceSegment source, @NotNull List<var> inputs, Type type) {
        final ArrayList<String> perms = TypeLearner.perms(inputs);
        for (String perm : perms) {
            final String key = createKey(name, perm);
            final CountBasedTypePrediction usageCounters = map.getOrDefault(key, new CountBasedTypePrediction(name));
            usageCounters.addCount(type, 1);
            map.put(key, usageCounters);
        }
        this.modified = true;

    }

    @Nullable
    @Override
    public TypePrediction predict(String name, SourceSegment source, @NotNull List<var> inputs) {
        final ArrayList<String> perms = TypeLearner.perms(inputs);
        CountBasedTypePrediction prediction = new CountBasedTypePrediction(name);
        try {
            for (String perm : perms) {
                final CountBasedTypePrediction tally = map.get(createKey(name, perm));
                if (tally != null) {
                    for (Type type : tally.types()) {
                        prediction.addCount(type, tally.getCount(type));
                        if (prediction.types().size() > MAX_POSSIBLE_RETURN_VALUES) {
                            return new AnyTypePrediction();
                        }
//                        System.out.println("Count for " + name + " type " + perm + " with prediction " + type + "
// was " + tally.getCount(type));
                    }
                }
            }

            return prediction;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    public String createKey(String name, String perm) {
        return name + ":" + perm;
    }

    @Override
    public void start() {
        DB db = DBMaker.fileDB("file.db").make();
       map = db.hashMap("map").createOrOpen();
    }

    @Override
    public void stop() {

    }

}
