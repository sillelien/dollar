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

package me.neilellis.dollar.deps;

import org.jetbrains.annotations.NotNull;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DependencyRetriever {

    static {
        dollarLib = new File(System.getProperty("user.home"), ".dollar/lib");
    }

    @NotNull private static final File dollarLib;

    public static JarFileLoader retrieve(String artifact) throws DependencyResolutionException {
        return retrieve(new DefaultArtifact(artifact));
    }

    @NotNull public static JarFileLoader retrieve(Artifact artifact) throws DependencyResolutionException {
        JarFileLoader jarFileClassLoader = new JarFileLoader(new URL[]{});
        loadInternal(artifact, jarFileClassLoader);
        return jarFileClassLoader;
    }

    private static void loadInternal(Artifact artifact, @NotNull JarFileLoader jarFileClassLoader) throws
                                                                                                   DependencyResolutionException {
        if (!dollarLib.exists()) {
            if (!dollarLib.mkdirs()) {
                System.err.println("Could not create the ~/.dollar directory");
                System.exit(-1);
            }
        }
        Collection<RemoteRepository> remotes = Arrays.asList(
                new RemoteRepository(
                        "maven-central",
                        "default",
                        "http://repo1.maven.org/maven2/"
                )
        );
//        final Aether aether = new Aether(remotes, dollarLib);
////        new DefaultArtifact("junit", "junit-dep", "", "jar", "4.10")
//        Collection<Artifact> deps = aether.resolve(artifact
//                , "runtime"
//        );
//        for (Artifact dep : deps) {
//            try {
//                jarFileClassLoader.addFile(dep.getFile().getAbsolutePath());
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            }
//        }
    }

    @NotNull public static JarFileLoader retrieve(@NotNull List<String> artifacts) throws
                                                                                   DependencyResolutionException {
        JarFileLoader jarFileClassLoader = new JarFileLoader(new URL[]{});
        for (String artifact : artifacts) {
            loadInternal(new DefaultArtifact(artifact), jarFileClassLoader);
        }
        return jarFileClassLoader;
    }
}
