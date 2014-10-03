package com.cazcade.dollar;

import spark.Route;
import spark.SparkBase;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarHttp extends SparkBase {

    public DollarHttp(String method, String path, DollarHttpHandler handler) {
        Route route = (request, response) -> {
            return DollarStatic.call(() -> handler.handle(new DollarHttpContext(request, response)).$$());
        };
        addRoute(method, wrap(path, route));
    }
}
