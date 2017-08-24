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

package dollar.plugins.pipe;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dollar.api.DollarException;
import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.Scope;
import dollar.api.collections.ImmutableMap;
import dollar.api.script.ModuleResolver;
import dollar.api.var;
import dollar.deps.DependencyRetriever;
import dollar.internal.runtime.script.DollarParserImpl;
import dollar.internal.runtime.script.DollarScriptSupport;
import dollar.internal.runtime.script.ScriptScope;
import dollar.internal.runtime.script.api.DollarParser;
import dollar.internal.runtime.script.util.FileUtil;
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

import static dollar.internal.runtime.script.util.FileUtil.delete;

public class GithubModuleResolver implements ModuleResolver {
    public static final int GRACEPERIOD = 10 * 1000;
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(GithubModuleResolver.class);
    @NotNull
    private static final LoadingCache<String, File> repos;
    @NotNull
    private static final ExecutorService executor;

    static {
        executor = Executors.newSingleThreadExecutor();
        repos = CacheBuilder.newBuilder()
                        .maximumSize(10000)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
//                .removalListener((RemovalListener<String, File>) notification -> delete(notification.getValue()))
                        .build(new CacheLoader<String, File>() {
                            @NotNull
                            public File load(@NotNull String key) throws IOException, ExecutionException, InterruptedException {

                                return executor.submit(() -> getFile(key)).get();

                            }

                        });


    }

    @NotNull
    private static File getFile(@NotNull String uriWithoutScheme) throws IOException, InterruptedException {
        log.debug("GithubModuleResolver.getFile({})", uriWithoutScheme);

        String[] githubRepo = uriWithoutScheme.split(":");
        final String githubUser = githubRepo[0];
        final String branch = !githubRepo[2].isEmpty() ? githubRepo[2] : "master";

        final File dir = new File((FileUtil.SHARED_RUNTIME_PATH + "/modules/github") + "/" + githubUser + "/" + githubRepo[1] + "/" + branch);
        final String url = "https://github.com/" + githubRepo[0] + "/" + githubRepo[1] + ".git";
        final File lockFile = new File((FileUtil.SHARED_RUNTIME_PATH + "/modules/github") + "/." + githubUser + "." + githubRepo[1] + "." + branch + ".clone.lock");
        dir.mkdirs();


        if (lockFile.exists()) {
            log.debug("Lock file exists or branch ready.");
            //Git is annoyingly asynchronous so we wait to make sure the initial clone operation has completely finished
            if (lockFile.exists()) {
                log.debug("Lock file still exists so starting grace period before any operation");
                Thread.sleep(GRACEPERIOD);
            } else {
                Files.createFile(lockFile.toPath());
            }
            try (FileChannel channel = new RandomAccessFile(lockFile, "rw").getChannel()) {

                log.debug("Attempting to get lock file {}", lockFile);
                try (FileLock lock = channel.lock()) {
                    GitUtil.pull(dir);
                    lock.release();
                    log.debug("Lock file {} released", lockFile);
                } finally {
                    delete(lockFile);
                }

            } catch (OverlappingFileLockException e) {
                log.error(e.getMessage(), e);
                throw new DollarException("Attempted to update a module that is currently locked");
            }
        } else {
            log.debug("Lock file does not exist for module {} and it is not cloned, so we can assume initial state",
                      uriWithoutScheme);
            Files.createFile(lockFile.toPath());
            delete(dir);
            log.debug("Recreating dir");
            dir.mkdirs();

            try (FileChannel channel = new RandomAccessFile(lockFile, "rw").getChannel()) {
                try (FileLock lock = channel.lock()) {
                    GitUtil.clone(dir, url);
                    GitUtil.checkout(dir, branch);
                    lock.release();
                }
            }

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
    public <T, P> Pipeable resolve(@NotNull String uriWithoutScheme, @NotNull T scope, @NotNull P parser) throws Exception {
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
        return (params) -> DollarScriptSupport.inSubScope(false, false, "github-module", newScope -> {

            final ImmutableMap<var, var> paramMap = params[0].$map().toVarMap();
            for (Map.Entry<var, var> entry : paramMap.entrySet()) {
                newScope.set(entry.getKey().$S(), entry.getValue(), true, null, null, false, false, false);
            }
            return new DollarParserImpl(((DollarParser) parser).options(), classLoader).parse(
                    new ScriptScope((Scope) scope, mainFile.getAbsolutePath(), content, "github-module-scope", false), content);
        });
    }
}
