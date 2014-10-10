package me.neilellis.dollar.monitor;

import java.util.function.Supplier;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface Monitor {

    /**
     * Dump metrics and related information to the console.
     */
    void dump();

    void dumpThread();

    <R> R run(String simpleLabel, String namespacedLabel, String info, Supplier<R> code);

    void run(String simpleLabel, String namespacedLabel, String info, Runnable code);
}
