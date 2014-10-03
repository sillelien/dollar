package com.cazcade.dollar.monitor;

import java.util.function.Supplier;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface Monitor {
    <R> R run(String simpleLabel, String namespacedLabel, String info, Supplier<R> code);

    void run(String simpleLabel, String namespacedLabel, String info, Runnable code);
}
