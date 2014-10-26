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

package me.neilellis.dollar.plugins.pipe;

import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.deps.DependencyRetriever;
import me.neilellis.dollar.deps.JarFileLoader;
import me.neilellis.dollar.pipe.PipeResolver;
import me.neilellis.dollar.script.DollarParser;
import me.neilellis.dollar.var;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.util.stream.Collectors;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class GithubPipeResolver implements PipeResolver {
    @Override
    public String getScheme() {
        return "github";
    }

    @Override
    public Pipeable resolve(var v, String uriWithoutScheme) throws Exception {
        String[] githubParts = uriWithoutScheme.split("/");
        GitHub github = GitHub.connect();
        GHRepository repository = github.getUser(githubParts[0]).getRepository(githubParts[1]);
        GHContent moduleContent = repository.getFileContent("module.json");
        var module = DollarStatic.$(moduleContent.getContent());
        GHContent fileContent = repository.getFileContent(module.$("main").$S());
        String content = fileContent.getContent();
        JarFileLoader classLoader = DependencyRetriever.retrieve(module.$("dependencies").toList().stream().map((i) -> i.$S()).collect(Collectors.toList()));
        return (in) -> new DollarParser(classLoader).parse(content);
    }

    @Override
    public PipeResolver copy() {
        return this;
    }
}
