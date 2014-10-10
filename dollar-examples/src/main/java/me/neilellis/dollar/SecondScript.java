package me.neilellis.dollar;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class SecondScript extends Script {
    static {
        $THIS = SecondScript.class;
    }

    {
        out = in.$("test", 123);
    }
}
