package com.sillelien.dollar.script.util;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;


@SuppressWarnings("WeakerAccess")
public class FileUtil {
    @NotNull
    public static final UUID JVM_ID = UUID.randomUUID();

    @NotNull
    public static final String TMP_PATH = System.getProperty("java.io.tmpdir") + "/dollar/runtime/"+ JVM_ID;

    static {
        new File(TMP_PATH).mkdirs();
        Runtime.getRuntime().addShutdownHook(new Thread(()->delete(new File(TMP_PATH))));
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

}
