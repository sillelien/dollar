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

import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * This is primitive seeker for nearest labels.
 * It's used instead of basic wordsNearest method because for ParagraphVectors
 * only labels should be taken into account, not individual words
 *
 * @author raver119@gmail.com
 */
public class LabelSeeker {
    private final List<String> labelsUsed;
    private final InMemoryLookupTable<VocabWord> lookupTable;

    public LabelSeeker(@Nonnull List<String> labelsUsed, @Nonnull InMemoryLookupTable<VocabWord> lookupTable) {
        if (labelsUsed.isEmpty()) throw new IllegalStateException("You can't have 0 labels used for ParagraphVectors");
        this.lookupTable = lookupTable;
        this.labelsUsed = labelsUsed;
    }

    /**
     * This method accepts vector, that represents any document,
     * and returns distances between this document, and previously trained categories
     *
     * @return
     */
    @Nonnull
    public List<Pair<String, Double>> getScores(@Nonnull INDArray vector) {
        List<Pair<String, Double>> result = new ArrayList<>();
        for (String label : labelsUsed) {
            INDArray vecLabel = lookupTable.vector(label);
            if (vecLabel == null) throw new IllegalStateException("Label '" + label + "' has no known vector!");

            double sim = Transforms.cosineSim(vector, vecLabel);
            result.add(new Pair<>(label, sim));
        }
        return result;
    }
}
