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
import me.neilellis.dollar.TypeAware;
import me.neilellis.dollar.collections.ImmutableMap;
import me.neilellis.dollar.deps.DependencyRetriever;
import me.neilellis.dollar.script.DollarParser;
import me.neilellis.dollar.script.ModuleResolver;
import me.neilellis.dollar.script.ScriptScope;
import me.neilellis.dollar.var;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class GithubModuleResolver implements ModuleResolver {
    private static final Logger logger = LoggerFactory.getLogger(GithubModuleResolver.class);
    private static final String BASE_PATH = System.getProperty("user.home") + "/.dollar/repo";

    @Override
    public ModuleResolver copy() {
        return this;
    }

    @Override
    public String getScheme() {
        return "github";
    }

    @Override
    public Pipeable resolve(String uriWithoutScheme, ScriptScope scope) throws Exception {
        logger.debug(uriWithoutScheme);
        String[] githubRepo = uriWithoutScheme.split(":");
        GitHub github = GitHub.connect();
        final String githubUser = githubRepo[0];
        GHRepository repository = github.getUser(githubUser).getRepository(githubRepo[1]);
        final String branch = githubRepo[2].length() > 0 ? githubRepo[2] : "master";
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        final File dir = new File(BASE_PATH + "/" + githubUser + "/" + githubRepo[1] + "/" + branch);
        dir.mkdirs();

        final File gitDir = new File(dir, ".git");
        if (gitDir.exists()) {
            Repository localRepo = builder.setGitDir(gitDir).readEnvironment().findGitDir().build();
            Git git = new Git(localRepo);
            PullCommand pull = git.pull();
            pull.call();
        } else {
            Repository localRepo = builder.setGitDir(dir).readEnvironment().findGitDir().build();
            Git git = new Git(localRepo);
            CloneCommand clone = git.cloneRepository();
            clone.setBranch(branch);
            clone.setBare(false);
            clone.setCloneAllBranches(false);
            clone.setDirectory(dir).setURI(repository.getGitTransportUrl());
//        UsernamePasswordCredentialsProvider user = new UsernamePasswordCredentialsProvider(login, password);
//        clone.setCredentialsProvider(user);
            clone.call();
        }
        final ClassLoader classLoader;
        String content;
        File mainFile;
        if (githubRepo.length == 4) {
            classLoader = getClass().getClassLoader();
            mainFile = new File(dir, githubRepo[3]);
            content = new String(Files.readAllBytes(mainFile.toPath()));
        } else {
            final File moduleFile = new File(dir, "module.json");
            var module = DollarStatic.$(new String(Files.readAllBytes(moduleFile.toPath())));
            mainFile = new File(dir, module.$("main").$S());
            content = new String(Files.readAllBytes(mainFile.toPath()));
            classLoader =
                    DependencyRetriever.retrieve(module.$("dependencies")
                                                       .$list()
                                                       .stream()
                                                       .map(TypeAware::$S)
                                                       .collect(Collectors.toList()));
        }
        return (params) -> scope.getDollarParser().inScope("github-module", scope, newScope -> {
            try {


                final ImmutableMap<String, var> paramMap = params.$map();
                for (Map.Entry<String, var> entry : paramMap.entrySet()) {
                    newScope.set(entry.getKey(), entry.getValue(), true, null, false);
                }
                return new DollarParser(classLoader, dir, mainFile).parse(newScope, content, false);
            } catch (IOException e) {
                return DollarStatic.logAndRethrow(e);
            }
        });
    }
}
