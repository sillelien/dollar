package com.sillelien.dollar.script.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;


@SuppressWarnings("WeakerAccess")
public final class FileUtil {
    @NotNull
    public static final UUID JVM_ID = UUID.randomUUID();

    @NotNull
    public static final String RUNTIME_TMP_PATH = System.getProperty("java.io.tmpdir") + "/dollar/runtime/" + JVM_ID;
    @NotNull
    public static final String SHARED_RUNTIME_PATH = System.getProperty("user.home") + "/.dollar/runtime/";

    static {
        new File(RUNTIME_TMP_PATH).mkdirs();
        new File(SHARED_RUNTIME_PATH).mkdirs();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> delete(new File(RUNTIME_TMP_PATH))));
    }

    @NotNull
    public static String threadTempPath() {
        return System.getProperty("java.io.tmpdir") + "/dollar/runtime/" + JVM_ID + "/" + Thread.currentThread().getId();
    }

    public static void delete(@NotNull File toDelete) {
        if (toDelete.isDirectory()) {
            File[] files = toDelete.listFiles();
            if (files != null) {
                for (File file : files) {
                    delete(file);
                }
            }
        }
        //noinspection ResultOfMethodCallIgnored
        toDelete.delete();
    }

    public static File getRuntimeDir(String dirName) {
        File dir = new File(SHARED_RUNTIME_PATH, dirName);
        dir.mkdirs();
        return dir;
    }
}
