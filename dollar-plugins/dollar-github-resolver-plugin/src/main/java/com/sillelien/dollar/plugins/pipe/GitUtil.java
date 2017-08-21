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

package com.sillelien.dollar.plugins.pipe;

import com.google.common.io.CharStreams;
import com.sillelien.dollar.api.DollarException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public final class GitUtil {

    static {
        try {
            check();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void check() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("git", "--version");
        Process p = pb.start();
        p.waitFor();
        String version = CharStreams.toString(new InputStreamReader(p.getInputStream()));
        if (!version.split("\n")[0].matches("^git version [2|3]\\.\\d\\d.*")) {
            throw new DollarException("Git is not installed, please install git version 2.x or later, result from check was " + version);
        }
    }

    public static void clone(@NotNull File dir, @NotNull String url) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("git", "clone", url, dir.getAbsolutePath());
        pb.directory(dir);
        Process p = pb.start();
        p.waitFor();
//        System.out.println(CharStreams.toString(new InputStreamReader(p.getInputStream())));
    }

    public static void checkout(@NotNull File dir, @NotNull String branch) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("git", "checkout", branch);
        pb.directory(dir);
        Process p = pb.start();
        p.waitFor();
//        System.out.println(CharStreams.toString(new InputStreamReader(p.getInputStream())));
    }

    public static void pull(@NotNull File dir) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("git", "pull");
        pb.directory(dir);
        Process p = pb.start();
        p.waitFor();
//        System.out.println(CharStreams.toString(new InputStreamReader(p.getInputStream())));
    }
}
