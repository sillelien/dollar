package me.neilellis.dollar;

import spark.Request;
import spark.Response;

import java.util.Collections;
import java.util.Set;

import static me.neilellis.dollar.DollarStatic.$;

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

    public var cookies() {
        return DollarFactory.fromValue(Collections.emptyList(),request.cookies());
    }

    public var headers() {
        var result = $();
        Set<String> headers = request.headers();
        for (String header : headers) {
            result = result.$(header, request.headers(header));
        }
        return result;
    }

    public var json() {
        return DollarFactory.fromValue(Collections.emptyList(),request.body());
    }

    public var params() {
        return DollarFactory.fromValue(Collections.emptyList(),request.params());
    }

    public Request request() {
        return request;
    }

    public Response response() {
        return response;
    }
}
