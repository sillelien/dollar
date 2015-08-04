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

package com.sillelien.dollar.script;

import com.google.common.io.CharStreams;
import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.script.api.ParserOptions;
import com.sillelien.dollar.test.CircleCiParallelRule;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class ParserTest {

    @ClassRule public static final CircleCiParallelRule className = new CircleCiParallelRule();
    @NotNull private final ParserOptions options = new ParserOptions();
    private boolean parallel;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testArrays() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_arrays.ds"), "/test_arrays.ds",
                                            parallel);
    }

    @Test
    public void testBasics1() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test1.ds"), "/test1.ds", parallel);
    }

    @Test
    public void testBasics3() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test3.ds"), "/test3.ds", parallel);
    }

    @Test
    public void testBuiltins() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_builtins.ds"), "/test_builtins.ds",
                                        parallel);
    }

    @Test
    public void testCasting() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_casting.ds"), "/test_casting.ds",
                                        parallel);
    }

    @Test
    public void testConcurrency() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_concurrency.ds"),
                                            "/test_concurrency.ds",
                                 parallel);
    }

    @Test
    public void testControlFlow() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_control_flow.ds"),
                                        "/test_control_flow.ds",
                                 parallel);
    }

    @Test
    public void testDate() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_date.ds"), "/test_date.ds", parallel);
    }

    @Test
    public void testFix() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_fix.ds"), "/test_fix.ds", parallel);
    }

    @Test
    public void testIteration() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_iteration.ds"), "/test_iteration.ds",
                                        parallel);
    }

    @Test
    public void testJava() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_java.ds"), "/test_java.ds", parallel);
    }

    @Test
    public void testLogic() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_logic.ds"), "/test_logic.ds",
                                            parallel);
    }

    @Test
    public void testMarkdown1() throws IOException {
        new DollarParserImpl(options).parseMarkdown(
                CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("/test1.md"))));
    }

    @Test
    public void testModules() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_modules.ds"), "/test_modules.ds",
                                        parallel);
    }

    @Test
    public void testNumeric() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_numeric.ds"), "/test_numeric.ds",
                                        parallel);
    }

    //    @Test
    public void testOperators() throws Exception {
        DollarStatic.getConfig().failFast(false);
        final List<String>
                operatorTestFiles =
                Arrays.asList("divide.all.ds", "plus.all.ds", "plus.minimal.ds", "divide.minimal.ds",
                              "minus.minimal.ds", "minus.small.ds");
        for (String operatorTestFile : operatorTestFiles) {
            System.out.println(operatorTestFile);
            new DollarParserImpl(options).parse(
                    getClass().getResourceAsStream("/regression/operators/" + operatorTestFile),
                                     operatorTestFile, parallel);
        }
    }

    @Test
    public void testParameters() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_parameters.ds"),
                                            "/test_parameters.ds",
                                 parallel);
    }

    @Test
    public void testPure() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_pure.ds"), "/test_pure.ds", parallel);
    }

    @Test
    public void testRanges() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_ranges.ds"), "/test_ranges.ds",
                                            parallel);
    }

    @Test
    public void testReactive() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_reactive.ds"), "/test_reactive.ds",
                                        parallel);
    }

    @Test
    public void testRedis() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_redis.ds"), "/test_redis.ds",
                                            parallel);
    }

    @Test
    public void testStrings() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_strings.ds"), "/test_strings.ds",
                                        parallel);
    }

    @Test
    public void testURIs() {
//        new DollarParser().parse(getClass().getResourceAsStream("/test_uris.ds"), parallel);
    }

    @Test
    public void testVariables() throws Exception {
        new DollarParserImpl(options).parse(getClass().getResourceAsStream("/test_variables.ds"), "/test_variables.ds",
                                        parallel);
    }
}