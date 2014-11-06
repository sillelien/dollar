/*
 * Copyright (c) 2014 Neil Ellis
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

package me.neilellis.dollar.script;

import com.google.common.io.CharStreams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStreamReader;

public class ParserTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testBasics1() throws Exception {
        new DollarParser().parse(getClass().getResourceAsStream("/test1.ds"));
    }

    @Test
    public void testArrays() throws Exception {
        new DollarParser().parse(getClass().getResourceAsStream("/test_arrays.ds"));
    }

    @Test
    public void testBasics3() throws Exception {
        new DollarParser().parse(getClass().getResourceAsStream("/test3.ds"));
    }

    @Test
    public void testURIs() throws Exception {
        new DollarParser().parse(getClass().getResourceAsStream("/test_uris.ds"));
    }

    @Test
    public void testLogic() throws Exception {
        new DollarParser().parse(getClass().getResourceAsStream("/test_logic.ds"));
    }

    @Test
    public void testNumeric() throws Exception {
        new DollarParser().parse(getClass().getResourceAsStream("/test_numeric.ds"));
    }


    @Test
    public void testBuiltins() throws Exception {
        new DollarParser().parse(getClass().getResourceAsStream("/test_builtins.ds"));
    }

    @Test
    public void testParameters() throws Exception {
        new DollarParser().parse(getClass().getResourceAsStream("/test_parameters.ds"));
    }

    @Test
    public void testJava() throws Exception {
        new DollarParser().parse(getClass().getResourceAsStream("/test_java.ds"));
    }

    @Test
    public void testMarkdown1() throws Exception {
        new DollarParser().parseMarkdown(CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("/test1.md"))));
    }
}