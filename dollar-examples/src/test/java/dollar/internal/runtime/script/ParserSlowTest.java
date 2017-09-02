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

package dollar.internal.runtime.script;

import com.google.common.io.CharStreams;
import dollar.api.DollarStatic;
import dollar.internal.runtime.script.api.ParserOptions;
import dollar.internal.runtime.script.parser.Symbols;
import dollar.test.CircleCiParallelRule;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

public class ParserSlowTest {

    @ClassRule
    public static final CircleCiParallelRule className = new CircleCiParallelRule();

    @NotNull
    private static final String[] files = {
    };

    @NotNull
    private final ParserOptions options = new ParserOptions();
    private boolean parallel;

    public static Stream<String> fileNames() {
        return Stream.of(files);
    }

    public static List<String> operatorList() {
        return Symbols.OPERATORS.stream().map(i -> {
            String file = "/examples/op/" + i.name() + ".ds";
            InputStream resourceAsStream = ParserSlowTest.class.getResourceAsStream(file);
            if (resourceAsStream != null) {
                try {
                    if (CharStreams.toString(new InputStreamReader(resourceAsStream)).matches("\\s*")) {
                        return null;
                    } else {
                        return file;
                    }
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                    return null;
                }
            } else {
                return null;
            }


        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @ParameterizedTest
    @ValueSource(
//            "bulletin.ds",
//            "example.ds",
            strings = {"slow/air_quality.ds", "slow/test_redis.ds",
                              "slow/test_uris.ds", "slow/test_concurrency.ds",
                              "slow/test_modules.ds"})

    public void testScript(@NotNull String filename) throws Exception {
        System.out.println("Testing " + filename);
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/" + filename), filename, parallel);
    }


    @ParameterizedTest
    @MethodSource("operatorList")
    public void operators(@NotNull String file) {

        InputStream resourceAsStream = getClass().getResourceAsStream(file);
        try {
            new DollarParserImpl(new ParserOptions()).parse(resourceAsStream, file, false);
            resourceAsStream.close();
        } catch (Exception e) {
            fail(e);
        }
    }


    @Test
    public void testMarkdown1() throws IOException {
        new DollarParserImpl(options).parseMarkdown(
                CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("/quick/test1.md"))));
    }


    @Test
    @Ignore("Regression test")
    public void testOperators() throws Exception {
        DollarStatic.getConfig().failFast(false);
        final List<String>
                operatorTestFiles =
                Arrays.asList();
        for (String operatorTestFile : operatorTestFiles) {
            System.out.println(operatorTestFile);
            new DollarParserImpl(options).parse(
                    getClass().getResourceAsStream("/regression/operators/" + operatorTestFile),
                    operatorTestFile, parallel);
        }
    }
}
