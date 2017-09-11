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

package dollar.learner.simple;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import dollar.api.Type;
import dollar.api.TypePrediction;
import dollar.api.execution.DollarExecutor;
import dollar.api.plugin.Plugins;
import dollar.api.script.Source;
import dollar.api.script.TypeLearner;
import dollar.api.types.prediction.CountBasedTypePrediction;
import dollar.api.var;
import dollar.internal.runtime.script.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class SimpleTypeLearner implements TypeLearner {


    @Nullable
    private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(SimpleTypeLearner.class);
    @NotNull
    private static final XStream xstream = new XStream();
    @NotNull
    private ConcurrentHashMap<String, TypeScoreMap> map = new ConcurrentHashMap<>();


    public SimpleTypeLearner() {
        start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    @NotNull
    @Override
    public TypeLearner copy() {
        return this;
    }

    private String key(@NotNull String name, @NotNull Source source, @NotNull List<var> inputs) {
        return name + "(" + inputs.stream().limit(30).filter(Objects::nonNull).limit(10).map(
                i -> (i.$type() != null) ? i.$type() : Type._ANY).map(
                Type::toString).collect(
                Collectors.joining(",")) + ")";
    }

    @Override
    public void learn(@NotNull String name, @NotNull Source source, @NotNull List<var> inputs, @NotNull Type type) {
        String key = key(name, source, inputs);
        TypeScoreMap typeScoreMap = map.get(key);
        if (typeScoreMap == null) {
            typeScoreMap = map.getOrDefault(key, new TypeScoreMap(key));
        }

        typeScoreMap.increment(type);
        map.put(key, typeScoreMap);
    }

    @Nullable
    @Override
    public TypePrediction predict(@NotNull String name, @NotNull Source source, @NotNull List<var> inputs) {
        String key = key(name, source, inputs);
        TypeScoreMap typeAtomicLongMap = map.get(key);
        if (typeAtomicLongMap == null) {
            return null;
        } else {
            return new CountBasedTypePrediction(key, typeAtomicLongMap.map());
        }

    }

    @NotNull
    private File mapFile() {
        return new File(FileUtil.getRuntimeDir("simple.type.learner"), "map.xml");
    }

    @Override
    public void start() {
        xstream.alias("count", java.util.concurrent.atomic.AtomicLong.class);
        xstream.alias("scoreMap", TypeScoreMap.class);
        xstream.registerConverter(new SingleValueConverter() {
            @Override
            public boolean canConvert(@NotNull Class type) {
                return type.equals(AtomicLong.class);
            }

            @NotNull
            @Override
            public String toString(@NotNull Object obj) {
                return obj.toString();
            }

            @NotNull
            @Override
            public Object fromString(@NotNull String str) {
                return new AtomicLong(Long.parseLong(str));
            }
        });
        if (mapFile().exists()) {
            log.info("Loading learnt types from {}", mapFile());
            map = (ConcurrentHashMap<String, TypeScoreMap>) xstream.fromXML(mapFile());
            log.info("Loaded {} entries", map.size());
            Set<Map.Entry<String, TypeScoreMap>> entries = new HashSet<>(map.entrySet());
            for (Map.Entry<String, TypeScoreMap> entry : entries) {
                if (entry.getValue().expired()) {
                    log.debug("Expired {}", entry.getKey());
                    map.remove(entry.getKey());
                }
            }
        }
    }

    @Override
    public void stop() {
        try {
            log.info("Saving learnt types to {}", mapFile());
            xstream.toXML(map, new FileWriter(mapFile()));
            log.info("Saved {} entries", map.size());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
