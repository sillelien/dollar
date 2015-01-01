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

package me.neilellis.dollar.learner.simple;

import com.thoughtworks.xstream.XStream;
import me.neilellis.dollar.api.Type;
import me.neilellis.dollar.api.TypePrediction;
import me.neilellis.dollar.api.execution.DollarExecutor;
import me.neilellis.dollar.api.plugin.Plugins;
import me.neilellis.dollar.api.script.SourceSegment;
import me.neilellis.dollar.api.script.TypeLearner;
import me.neilellis.dollar.api.types.prediction.AnyTypePrediction;
import me.neilellis.dollar.api.types.prediction.CountBasedTypePrediction;
import me.neilellis.dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleTypeLearner implements TypeLearner {


    public static final int MAX_POSSIBLE_RETURN_VALUES = 5;
    @Nullable private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);

    private transient boolean modified;
    @NotNull private ConcurrentHashMap<String, CountBasedTypePrediction> map = new ConcurrentHashMap<>();

    public SimpleTypeLearner() {
        File file = new File(System.getProperty("user.home") + "/.dollar/typelearning.xml");
        File backupFile = new File(System.getProperty("user.home") + "/.dollar/typelearning.backup.xml");
        file.getParentFile().mkdirs();
        executor.scheduleEvery(200, () -> {
            if (modified) {

                try (FileOutputStream out = new FileOutputStream(file)) {
                    new XStream().toXML(map, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try (FileOutputStream out = new FileOutputStream(backupFile)) {
                    new XStream().toXML(map, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        if (file.exists()) {
            try {
                map = (ConcurrentHashMap<String, CountBasedTypePrediction>) new XStream().fromXML(file);
            } catch (Exception e) {
                file.delete();
                if (backupFile.exists()) {
                    map = (ConcurrentHashMap<String, CountBasedTypePrediction>) new XStream().fromXML(backupFile);
                }
            }
        } else if (backupFile.exists()) {
            map = (ConcurrentHashMap<String, CountBasedTypePrediction>) new XStream().fromXML(backupFile);
        }
    }


    @NotNull @Override public TypeLearner copy() {
        return this;
    }

    @Override public void learn(String name, SourceSegment source, @NotNull List<var> inputs, Type type) {
        final ArrayList<String> perms = TypeLearner.perms(inputs);
        for (String perm : perms) {
            final String key = createKey(name, perm);
            final CountBasedTypePrediction usageCounters = map.getOrDefault(key, new CountBasedTypePrediction(name));
            usageCounters.addCount(type, 1);
            map.put(key, usageCounters);
        }
        this.modified = true;

    }

    @Nullable @Override public TypePrediction predict(String name, SourceSegment source, @NotNull List<var> inputs) {
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

    @NotNull public String createKey(String name, String perm) {return name + ":" + perm;}

    @Override public void start() {

    }

    @Override public void stop() {

    }

}
