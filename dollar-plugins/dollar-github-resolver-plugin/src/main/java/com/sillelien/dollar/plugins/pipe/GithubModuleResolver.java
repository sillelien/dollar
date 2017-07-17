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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.sillelien.dollar.api.DollarException;
import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.collections.ImmutableMap;
import com.sillelien.dollar.api.script.ModuleResolver;
import com.sillelien.dollar.api.var;
import com.sillelien.dollar.deps.DependencyRetriever;
import com.sillelien.dollar.script.DollarParserImpl;
import com.sillelien.dollar.script.api.Scope;
import com.sillelien.dollar.script.util.FileUtil;
import com.sillelien.github.GHRepository;
import com.sillelien.github.GitHub;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class GithubModuleResolver implements ModuleResolver {
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(GithubModuleResolver.class);

    @NotNull
    private static final String BASE_PATH = FileUtil.TMP_PATH + "/modules/github";

    @NotNull
    private static final LoadingCache<String, File> repos;


    @NotNull
    private static final ExecutorService executor;

    static {
        executor = Executors.newSingleThreadExecutor();
        repos = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.MINUTES)
//                .removalListener((RemovalListener<String, File>) notification -> delete(notification.getValue()))
                .build(new CacheLoader<String, File>() {
                    @NotNull
                    public File load(@NotNull String key) throws IOException, GitAPIException, ExecutionException, InterruptedException {

                        return executor.submit(() -> getFile(key)).get();

                    }

                });


    }

    @NotNull
    private synchronized static File getFile(@NotNull String uriWithoutScheme) throws IOException, GitAPIException {

        String[] githubRepo = uriWithoutScheme.split(":");
        GitHub github = GitHub.connect();
        final String githubUser = githubRepo[0];
        final GHRepository repository = github.getUser(githubUser).getRepository(githubRepo[1]);
        final String branch = githubRepo[2].length() > 0 ? githubRepo[2] : "master";
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        final File dir = new File(BASE_PATH + "/" + githubUser + "/" + githubRepo[1] + "/" + branch);
        dir.mkdirs();


        File lockFile = new File(dir.getPath() + ".lock");

        if (!lockFile.exists()) {
            log.debug("Lock file does not exist for module {}", uriWithoutScheme);
            Files.createFile(lockFile.toPath());
        }

        try (FileChannel channel = new RandomAccessFile(lockFile, "rw").getChannel()) {

            log.debug("Attempting to get lock file {}", lockFile);

            try (FileLock lock = channel.lock()) {

                final File gitDir = new File(dir, ".git");

                if (gitDir.exists()) {
                    log.debug(".git file {} exists so pulling", gitDir);

                    Repository localRepo = builder
                            .setGitDir(gitDir)
                            .readEnvironment()
                            .findGitDir()
                            .build();

                    new Git(localRepo).pull().call();

                } else {
                    log.debug(".git file {} does not exist so cloning", gitDir);

                    Git.cloneRepository()
                            .setBranch(branch)
                            .setBare(false)
//                            .setCloneAllBranches(false)
                            .setDirectory(dir)
                            .setURI("https://github.com/" + githubRepo[0] + "/" + githubRepo[1])
                            .call();

                }

                lock.release();
                log.debug("Lock file {} released", lockFile);
            } catch (JGitInternalException ie) {
                log.error(ie.getMessage() + ": in dir " + dir, ie);
                throw new DollarException(ie, ie.getMessage() + ": in dir " + dir);
            }

        } catch (OverlappingFileLockException e) {
            log.error(e.getMessage(), e);
            throw new DollarException("Attempted to update a module that is currently locked");
        } finally {
            FileUtil.delete(lockFile);
        }


        return dir;
    }


    @NotNull
    @Override
    public ModuleResolver copy() {
        return this;
    }

    @NotNull
    @Override
    public String getScheme() {
        return "github";
    }

    @NotNull
    @Override
    public <T> Pipeable resolve(@NotNull String uriWithoutScheme, @NotNull T scope) throws Exception {
        log.debug(uriWithoutScheme);
        File dir = repos.get(uriWithoutScheme);

        String[] githubRepo = uriWithoutScheme.split(":");

        final ClassLoader classLoader;
        final String content;
        final File mainFile;

        if (githubRepo.length == 4) {

            classLoader = getClass().getClassLoader();
            mainFile = new File(dir, githubRepo[3]);
            content = new String(Files.readAllBytes(mainFile.toPath()));

        } else {

            final File moduleFile = new File(dir, "module.json");
            final var module = DollarStatic.$(new String(Files.readAllBytes(moduleFile.toPath())));
            mainFile = new File(dir, module.$("main").$S());
            content = new String(Files.readAllBytes(mainFile.toPath()));
            classLoader =
                    DependencyRetriever.retrieve(module.$("dependencies")
                            .$list()
                            .$stream(false)
                            .map(var::toString)
                            .collect(Collectors.toList()));

        }
        return (params) -> ((Scope) scope).getDollarParser().inScope(false, "github-module", ((Scope) scope), newScope -> {

            final ImmutableMap<var, var> paramMap = params[0].$map().toVarMap();
            for (Map.Entry<var, var> entry : paramMap.entrySet()) {
                newScope.set(entry.getKey().$S(), entry.getValue(), true, null, null, false, false, false);
            }
            return new DollarParserImpl(((Scope) scope).getDollarParser().options(), classLoader, dir).parse(newScope,
                    content);
        });
    }
}
