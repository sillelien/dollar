package com.cazcade.dollar;

import spark.SparkBase;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarHttp extends SparkBase {

    public DollarHttp(String method, String path, DollarHttpHandler handler) {
        addRoute(method, wrap(path, (request, response) -> {
            return DollarStatic.call(() -> handler.handle(new DollarHttpContext(request, response)).$$());
        }));
    }
}
