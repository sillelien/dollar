package technology.neil.dollar;

import spark.Route;
import spark.SparkBase;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarHttp extends SparkBase {

    public DollarHttp(String method, String path, DollarHttpHandler handler) {
        Route route = (request, response) -> {
            var result = DollarStatic.$call(() -> handler.handle(new DollarHttpContext(request, response)));
            response.type(result.mimeType());
            return result.$$();
        };
        addRoute(method, wrap(path, route));
    }
}
