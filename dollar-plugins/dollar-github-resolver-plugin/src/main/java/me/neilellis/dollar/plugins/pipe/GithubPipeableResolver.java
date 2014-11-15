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
import me.neilellis.dollar.script.DollarParser;
import me.neilellis.dollar.script.PipeableResolver;
import me.neilellis.dollar.script.ScriptScope;
import me.neilellis.dollar.var;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class GithubPipeableResolver implements PipeableResolver {
    private static final Logger logger = LoggerFactory.getLogger(GithubPipeableResolver.class);
    @Override
    public String getScheme() {
        return "github";
    }

    @Override
    public Pipeable resolve(String uriWithoutScheme, ScriptScope scope) throws Exception {
        logger.debug(uriWithoutScheme);
        String[] githubParts = uriWithoutScheme.split(":");
        GitHub github = GitHub.connect();
        GHRepository repository = github.getUser(githubParts[0]).getRepository(githubParts[1]);
        final ClassLoader classLoader;
        String content;
        if (githubParts.length == 3) {
            classLoader = getClass().getClassLoader();
            GHContent fileContent = repository.getFileContent(githubParts[2]);
            content = fileContent.getContent();
        } else {
            GHContent moduleContent = repository.getFileContent("module.json");
            var module = DollarStatic.$(moduleContent.getContent());
            GHContent fileContent = repository.getFileContent(module.$get("main").$S());
            content = fileContent.getContent();
            classLoader = DependencyRetriever.retrieve(module.$get("dependencies").toList().stream().map((i) -> i.$S()).collect(Collectors.toList()));
        }
        return (in) -> scope.getDollarParser().withinNewScope(scope, newScope -> {
            try {
                return new DollarParser(classLoader).parse(newScope, content);
            } catch (IOException e) {
                return DollarStatic.logAndRethrow(e);
            }
        });
    }

    @Override
    public PipeableResolver copy() {
        return this;
    }
}
