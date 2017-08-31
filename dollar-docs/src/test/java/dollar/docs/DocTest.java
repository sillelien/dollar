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

package dollar.docs;

import com.google.common.io.CharStreams;
import dollar.internal.runtime.script.DollarParserImpl;
import dollar.internal.runtime.script.api.ParserOptions;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStreamReader;

public class DocTest {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(DocTest.class);

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testDocs() throws Exception {
        try {
            new DollarParserImpl(new ParserOptions()).parseMarkdown(
                    CharStreams.toString(new InputStreamReader(getClass().getResourceAsStream("/pages/manual.md"))));
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            throw e;
        }
    }

    @Test
    public void testReadme() throws Exception {
        try {
            File dir = new File("").getAbsoluteFile();
            while (!new File(dir, ".git").exists()) {
                System.err.println(dir);
                dir = dir.getParentFile();
            }
            new DollarParserImpl(new ParserOptions()).parseMarkdown(new File(dir, "README.tmpl.md"));
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            throw e;
        }
    }
}
