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
    protected var in = DollarStatic.threadContext.get() != null ? DollarStatic.threadContext.get() : $();
    protected var out;

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        Script.args = Arrays.asList(args);
        $this = $THIS.newInstance();
    }


    public var result() {
        return out;
    }
}
