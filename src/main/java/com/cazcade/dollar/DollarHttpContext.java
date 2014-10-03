package com.cazcade.dollar;

import spark.Request;
import spark.Response;

import java.util.Set;

import static com.cazcade.dollar.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarHttpContext {
    private final Request request;
    private final Response response;

    public DollarHttpContext(Request request, Response response) {

        this.request = request;
        this.response = response;
    }

    public $ cookies() {
        return DollarFactory.fromValue(request.cookies());
    }

    public $ headers() {
        $ result = $();
        Set<String> headers = request.headers();
        for (String header : headers) {
            result = result.$(header, request.headers(header));
        }
        return result;
    }

    public $ json() {
        return DollarFactory.fromValue(request.body());
    }

    public $ params() {
        return DollarFactory.fromValue(request.params());
    }

    public Request request() {
        return request;
    }

    public Response response() {
        return response;
    }
}
