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

package com.sillelien.dollar.plugins.pipe;

import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.collections.ImmutableMap;
import com.sillelien.dollar.api.script.ModuleResolver;
import com.sillelien.dollar.api.var;
import com.sillelien.dollar.deps.DependencyRetriever;
import com.sillelien.dollar.script.DollarParserImpl;
import com.sillelien.dollar.script.api.Scope;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Collectors;

public class GithubModuleResolver implements ModuleResolver {
    private static final Logger logger = LoggerFactory.getLogger(GithubModuleResolver.class);
    private static final String BASE_PATH = System.getProperty("user.home") + "/.dollar/repo";

    @NotNull @Override
    public ModuleResolver copy() {
        return this;
    }

    @NotNull @Override
    public String getScheme() {
        return "github";
    }

    @NotNull @Override
    public <T> Pipeable resolve(@NotNull String uriWithoutScheme, @NotNull T scope) throws Exception {
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
            CloneCommand clone = Git.cloneRepository();
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
                                                       .$stream(false)
                                                       .map(var::toString)
                                                       .collect(Collectors.toList()));
        }
        return (params) -> ((Scope)scope).getDollarParser().inScope(false, "github-module", ((Scope)scope), newScope -> {

            final ImmutableMap<var, var> paramMap = params[0].$map().toVarMap();
            for (Map.Entry<var, var> entry : paramMap.entrySet()) {
                newScope.set(entry.getKey().$S(), entry.getValue(), true, null, null, false, false, false);
            }
            return new DollarParserImpl(((Scope) scope).getDollarParser().options(), classLoader, dir).parse(newScope,
                                                                                                             content);
        });
    }
}
