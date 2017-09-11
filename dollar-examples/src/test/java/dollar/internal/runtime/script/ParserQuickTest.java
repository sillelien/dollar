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
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

public class ParserQuickTest {

    @ClassRule
    public static final CircleCiParallelRule className = new CircleCiParallelRule();
    @NotNull
    private static final String[] files = {
    };
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(ParserQuickTest.class);
    @NotNull
    private final ParserOptions options = new ParserOptions();
    private boolean parallel;

    @BeforeClass
    public static void after() {
        DollarStatic.getConfig().failFast(false);
    }

    @BeforeClass
    public static void before() {
        DollarStatic.getConfig().failFast(true);
    }

    public static Stream<String> fileNames() {
        return Stream.of(files);
    }

    public static List<String> operatorList() {
        return Symbols.OPERATORS.stream().map(i -> {
            String file = "/examples/op/" + i.name() + ".ds";
            InputStream resourceAsStream = ParserQuickTest.class.getResourceAsStream(file);
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

    @ParameterizedTest
    @ValueSource(
            strings = {"negative/neg_print1.ds", "negative/neg_constraints_1.ds", "negative/neg_scopes_1.ds", "negative/neg_pure_1.ds", "negative/neg_pure_2.ds",
                              "negative/neg_pure_3.ds",
                              "negative/neg_pure_4.ds", "negative/neg_is_1.ds", "negative/neg_types_1.ds"})

    public void negativeTestScripts(@NotNull String filename) throws Exception {
        System.out.println("Testing " + filename);
        try {
            new DollarParserImpl(options).parse(getClass().getResourceAsStream("/" + filename), filename, parallel);
            fail("Expected exception");
        } catch (AssertionError e) {
            fail(e);
        } catch (Throwable e) {
            log.debug(e.getMessage(), e);
        } finally {

        }
    }

    @Test

    public void singleScriptTest() throws Exception {
        try {
            new DollarParserImpl(options).parse(getClass().getResourceAsStream("/quick/test_classes.ds"),
                                                "/quick/test_classes.ds",
                                                parallel);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            throw e;
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

    @ParameterizedTest
    @ValueSource(
//            "bulletin.ds",
//            "example.ds",
            strings = {"quick/test_variable_declarations.ds", "quick/test_print1.ds", "quick/test_classes.ds", "quick/test_variables1.ds", "quick/test_variables2.ds", "quick/test_variables3.ds", "quick/test_variables4.ds", "quick/test_variables5.ds", "quick/test_date.ds", "quick/test_block_closure.ds", "quick/test_list_closure.ds", "quick/test_map_closure.ds", "quick/test_scopes.ds",
                              "quick/test1.ds",
                              "quick/test_arrays.ds", "quick/test_builtins.ds", "quick/test_casting.ds", "quick/test_date.ds", "quick/test_fix1.ds",
                              "quick/test_fix2.ds", "quick/test_fix3.ds", "quick/test_iteration.ds", "quick/test_java.ds", "quick/test_logic.ds",
                              "quick/test_numeric.ds", "quick/test_parameters.ds", "quick/test_pure1.ds", "quick/test_pure2.ds", "quick/test_pure3.ds", "quick/test_pure4.ds", "quick/test_ranges.ds", "quick/test_reactive1.ds",
                              "quick/test_reactive2.ds", "quick/test_reactive3.ds", "quick/test_reactive4.ds", "quick/test_reactive5.ds",
                              "quick/test_reactive6.ds", "quick/test_reactive7.ds", "quick/test_reactive8.ds", "quick/test_strings.ds",
                              "quick/test_control_flow.ds",
                              "quick/test3.ds", "quick/test_types_1.ds"})

    public void testScript(@NotNull String filename) throws Exception {
        System.out.println("Testing " + filename);
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/" + filename), filename, parallel);
    }
}
