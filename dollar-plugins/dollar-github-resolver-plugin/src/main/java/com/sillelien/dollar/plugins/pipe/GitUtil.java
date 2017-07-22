package com.sillelien.dollar.plugins.pipe;

import com.google.common.io.CharStreams;
import com.sillelien.dollar.api.DollarException;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class GitUtil {

    static {
        try {
            check();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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

    public static void clone(File dir, String url) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("git", "clone", url, dir.getAbsolutePath());
        pb.directory(dir);
        Process p = pb.start();
        p.waitFor();
        System.out.println(CharStreams.toString(new InputStreamReader(p.getInputStream())));
    }

    public static void checkout(File dir, String branch) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("git", "checkout", branch);
        pb.directory(dir);
        Process p = pb.start();
        p.waitFor();
        System.out.println(CharStreams.toString(new InputStreamReader(p.getInputStream())));
    }

    public static void pull(File dir) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("git", "pull");
        pb.directory(dir);
        Process p = pb.start();
        p.waitFor();
        System.out.println(CharStreams.toString(new InputStreamReader(p.getInputStream())));
    }
}
