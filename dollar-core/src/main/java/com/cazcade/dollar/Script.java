package com.cazcade.dollar;

import java.util.Arrays;
import java.util.List;

/**
 * A parent class for writing cool Dollar scripts
 *
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class Script extends DollarStatic {

    public static Class<? extends Script> $THIS;
    protected static List<String> args;
    private static Script $this;
    protected var in = DollarStatic.threadContext.get() != null ? DollarStatic.threadContext.get().getPassValue() : $();
    protected var out;

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        Script.args = Arrays.asList(args);
        $run(() -> {
            try {
                $this = $THIS.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new Error(e.getCause());
            }
        });
    }


    public var result() {
        return out;
    }
}
