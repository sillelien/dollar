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

package com.sillelien.dollar.docs;

import com.google.common.io.CharStreams;
import com.sillelien.dollar.script.DollarParserImpl;
import com.sillelien.dollar.script.api.ParserOptions;
import org.jetbrains.annotations.NotNull;
import org.pegdown.Extensions;
import org.pegdown.PegDownProcessor;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

public class ParseDocs {

    public static void main(String[] args) throws IOException {
        parseDoc("scripting", new File(args[0]));
    }

    private static void parseDoc(String page, @NotNull File outDir) throws IOException {
        outDir.mkdirs();
        String source = CharStreams.toString(
                new InputStreamReader(ParseDocs.class.getResourceAsStream("/pages/" + page + ".md")));
        String toParse;
        if (source.startsWith("#!")) {
            toParse = "\n" + source.substring(source.indexOf("\n"));
        } else {
            toParse = source;
        }
        new DollarParserImpl(new ParserOptions()).parseMarkdown(toParse);
        PegDownProcessor pegDownProcessor = new PegDownProcessor(Extensions.FENCED_CODE_BLOCKS);
        String result = pegDownProcessor.markdownToHtml(toParse);
        Files.write(new File(outDir, page + ".html").toPath(), result.getBytes());
        Files.write(new File(outDir, page + ".md").toPath(), toParse.getBytes());
        System.exit(0);
    }
}