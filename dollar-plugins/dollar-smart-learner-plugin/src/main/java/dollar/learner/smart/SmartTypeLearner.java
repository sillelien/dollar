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

package dollar.learner.smart;

import dollar.api.Type;
import dollar.api.TypePrediction;
import dollar.api.execution.DollarExecutor;
import dollar.api.plugin.Plugins;
import dollar.api.script.SourceSegment;
import dollar.api.script.TypeLearner;
import dollar.api.types.prediction.SingleValueTypePrediction;
import dollar.api.var;
import org.deeplearning4j.berkeley.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SmartTypeLearner implements TypeLearner {

    public static final int MAX_POSSIBLE_RETURN_VALUES = 5;
    @Nullable
    private static final DollarExecutor executor = Plugins.sharedInstance(DollarExecutor.class);
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(SmartTypeLearner.class);
    @NotNull ParagraphVectorsClassifierExample classifier = new ParagraphVectorsClassifierExample();


    public SmartTypeLearner() {
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
        classifier.learn(name, source, inputs, type);

    }

    @Nullable
    @Override
    public TypePrediction predict(@NotNull String name, @NotNull SourceSegment source, @NotNull List<var> inputs) {
        try {
            List<Pair<String, Double>> scores = classifier.predict(name, source, inputs);
            log.info("Predictions: ");
            log.info("{}", scores);
            return new SmartTypePrediction(scores);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            return new SingleValueTypePrediction(Type._ANY);
        }


    }


    @Override
    public void start() {
        try {
            classifier.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        classifier.stop();
    }

    private class SmartTypePrediction implements TypePrediction {
        private final List<Pair<String, Double>> scores;

        public SmartTypePrediction(List<Pair<String, Double>> scores) {this.scores = scores;}

        @Override
        public boolean empty() {
            return scores.isEmpty();
        }

        @Override
        public @NotNull Double probability(@NotNull Type type) {
            for (Pair<String, Double> score : scores) {
                if (score.getFirst().equals(type.name())) {
                    return score.getSecond() / 2 + 0.5;
                }
            }
            return 0.0;
        }

        @Override
        public @Nullable Type probableType() {
            Pair<String, Double> max = scores.stream().max(Comparator.comparing(Pair::getSecond)).orElse(null);
            if (max != null) {
                return Type.of(max.getFirst());
            }
            return null;
        }


        @Override
        public @NotNull Set<Type> types() {
            return scores.stream().map(i -> Type.of(i.getFirst())).collect(Collectors.toSet());
        }
    }
}
