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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import dollar.api.Type;
import dollar.api.script.SourceSegment;
import dollar.api.var;
import org.datavec.api.util.ClassPathResource;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.models.embeddings.inmemory.InMemoryLookupTable;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.text.documentiterator.FileLabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.documentiterator.LabelledDocument;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.jetbrains.annotations.NotNull;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is basic example for documents classification done with DL4j ParagraphVectors.
 * The overall idea is to use ParagraphVectors in the same way we use LDA:
 * topic space modelling.
 * <p>
 * In this example we assume we have few labeled categories that we can use
 * for training, and few unlabeled documents. And our goal is to determine,
 * which category these unlabeled documents fall into
 * <p>
 * <p>
 * Please note: This example could be improved by using learning cascade
 * for higher accuracy, but that's beyond basic example paradigm.
 *
 * @author raver119@gmail.com
 */
public class ParagraphVectorsClassifierExample {

    public static final File TYPE_LEARNING_DIR = new File(System.getProperty("java.io.tmpdir") + "/dollar/runtime/", "types");
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(ParagraphVectorsClassifierExample.class);
    @NotNull ParagraphVectors paragraphVectors;
    @NotNull LabelAwareIterator iterator;
    @NotNull TokenizerFactory tokenizerFactory;

    public static void main(@NotNull String[] args) throws Exception {

        ParagraphVectorsClassifierExample app = new ParagraphVectorsClassifierExample();
        app.makeParagraphVectors();
        app.checkUnlabeledData();
        /*
                Your output should be like this:

                Document 'health' falls into the following categories:
                    health: 0.29721372296220205
                    science: 0.011684473733853906
                    finance: -0.14755302887323793

                Document 'finance' falls into the following categories:
                    health: -0.17290237675941766
                    science: -0.09579267574606627
                    finance: 0.4460859189453788

                    so,now we know categories for yet unseen documents
         */
    }

    void makeParagraphVectors() throws Exception {

        // build a iterator for our dataset
        File dir = TYPE_LEARNING_DIR;
        dir.mkdirs();
        iterator = new FileLabelAwareIterator.Builder()
                           .addSourceFolder(new File(dir, "corpus"))
                           .build();

        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());

        // ParagraphVectors training configuration
        paragraphVectors = new ParagraphVectors.Builder()
                                   .learningRate(0.025)
                                   .minLearningRate(0.001)
                                   .batchSize(1000)
                                   .epochs(5)
                                   .iterate(iterator)
                                   .trainWordVectors(true)
                                   .tokenizerFactory(tokenizerFactory)
                                   .build();

        // Start model training
        paragraphVectors.fit();
    }

    void checkUnlabeledData() throws FileNotFoundException {
      /*
      At this point we assume that we have model built and we can check
      which categories our unlabeled document falls into.
      So we'll start loading our unlabeled documents and checking them
     */
        ClassPathResource unClassifiedResource = new ClassPathResource("paravec/unlabeled");
        FileLabelAwareIterator unClassifiedIterator = new FileLabelAwareIterator.Builder()
                                                              .addSourceFolder(unClassifiedResource.getFile())
                                                              .build();

     /*
      Now we'll iterate over unlabeled data, and check which label it could be assigned to
      Please note: for many domains it's normal to have 1 document fall into few labels at once,
      with different "weight" for each.
     */
        MeansBuilder meansBuilder = new MeansBuilder(
                                                            (InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable(),
                                                            tokenizerFactory);
        LabelSeeker seeker = new LabelSeeker(iterator.getLabelsSource().getLabels(),
                                             (InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable());

        while (unClassifiedIterator.hasNextDocument()) {
            LabelledDocument document = unClassifiedIterator.nextDocument();
            INDArray documentAsCentroid = meansBuilder.documentAsVector(document);
            List<Pair<String, Double>> scores = seeker.getScores(documentAsCentroid);

         /*
          please note, document.getLabel() is used just to show which document we're looking at now,
          as a substitute for printing out the whole document name.
          So, labels on these two documents are used like titles,
          just to visualize our classification done properly
         */
            log.info("Document '" + document.getLabel() + "' falls into the following categories: ");
            for (Pair<String, Double> score : scores) {
                log.info("        " + score.getFirst() + ": " + score.getSecond());
            }
        }

    }

    public List<Pair<String, Double>> predict(@NotNull String name, @NotNull SourceSegment source, @NotNull List<var> inputs) {

     /*
      Now we'll iterate over unlabeled data, and check which label it could be assigned to
      Please note: for many domains it's normal to have 1 document fall into few labels at once,
      with different "weight" for each.
     */
        MeansBuilder meansBuilder = new MeansBuilder((InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable(),
                                                     tokenizerFactory);
        LabelSeeker seeker = new LabelSeeker(iterator.getLabelsSource().getLabels(),
                                             (InMemoryLookupTable<VocabWord>) paragraphVectors.getLookupTable());


        LabelledDocument document = new LabelledDocument();
        document.setContent(signatureToText(name, inputs));
        INDArray documentAsCentroid = meansBuilder.documentAsVector(document);
        List<Pair<String, Double>> scores = seeker.getScores(documentAsCentroid);
        return scores;

    }

    public void learn(@NotNull String name, @NotNull SourceSegment source, @NotNull List<var> inputs, @NotNull Type type) {
        File corpus = new File(new File(new File(TYPE_LEARNING_DIR, "corpus"), type.name()), type.name() + ".txt");
        corpus.getParentFile().mkdirs();
        try {
            Files.append(signatureToText(name, inputs) + "\n",
                         corpus,
                         Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    private String signatureToText(@NotNull String name, @NotNull List<var> inputs) {
        return name + " " + (inputs.stream().filter(Objects::nonNull).filter(i -> i.$type() != null).map(
                i -> i.$type().toString()).collect(
                Collectors.joining(" "))) + " ";
    }

    public void stop() {
        log.info("Saving to " + serializeFile().getAbsolutePath());
        WordVectorSerializer.writeParagraphVectors(paragraphVectors, serializeFile());
    }

    public void start() throws Exception {
        if (serializeFile().exists()) {
            try {
                log.info("Loading from " + serializeFile().getAbsolutePath());
                paragraphVectors = WordVectorSerializer.readParagraphVectors(serializeFile());
            } catch (Exception e) {
                log.debug(e.getMessage(), e);
                makeParagraphVectors();
            }
        } else {
            makeParagraphVectors();
        }
    }

    @NotNull
    private File serializeFile() {
        return new File(TYPE_LEARNING_DIR, "paragraph.ser");
    }
}
