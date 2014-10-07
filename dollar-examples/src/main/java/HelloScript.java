import com.cazcade.dollar.Script;
import com.cazcade.dollar.SecondScript;
import com.cazcade.dollar.var;

import java.util.Date;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class HelloScript extends Script {

    static {
        $THIS = HelloScript.class;
    }

    {
        var a = $("A");
        var profile = $("name", "Neil")
                .$("age", new Date().getYear() + 1900 - 1970)
                .$("gender", "male")
                .$("projects", $jsonArray("snapito", "dollar_vertx"))
                .$("location",
                        $("city", "brighton")
                                .$("postcode", "bn1 6jj")
                                .$("number", 343)
                );
        profile.pipe(SecondScript.class).out();
        profile.out();
        System.out.println(args);
        System.out.println(a);
        System.out.println(in);
    }


}
