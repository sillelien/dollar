package me.neilellis.dollar;

import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface DollarEval {

    @NotNull
    var eval(var in);
}
