package technology.neil.dollar.pipe;

import technology.neil.dollar.Script;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class ExtractName extends Script {
    static {
        $THIS = ExtractName.class;
    }

    {
        out = in.$("name");
    }
}
