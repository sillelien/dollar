package me.neilellis.dollar.pipe;

import me.neilellis.dollar.Script;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class WelcomeMessage extends Script {
    static {
        $THIS = WelcomeMessage.class;
    }

    {
        out = $("Welcome, " + in.$$());
    }
}
