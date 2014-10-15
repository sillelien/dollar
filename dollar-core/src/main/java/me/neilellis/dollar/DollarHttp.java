package me.neilellis.dollar;

import spark.Route;
import spark.SparkBase;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarHttp extends SparkBase {

    public DollarHttp(String method, String path, DollarHttpHandler handler) {
        DollarThreadContext context = DollarStatic.childContext();

        Route route = (request, response) -> {
            var result = DollarStatic.$call(context,() -> handler.handle(new DollarHttpContext(request, response)));
            response.type(result.mimeType());
            return result.$$();
        };
        addRoute(method, wrap(path, route));
    }
}