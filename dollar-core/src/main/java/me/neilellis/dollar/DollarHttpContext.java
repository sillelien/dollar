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

    public var queryParams() {
        return DollarFactory.fromValue(Collections.emptyList(),request.queryMap());
    }

    public Request request() {
        return request;
    }

    public Response response() {
        return response;
    }

    public void status(int statusCode) {
        response.status(statusCode);
    }

    public void body(var body) {
        response.type(body.$mimeType());
        response.body(body.$$());
    }
    public void body(String body) {
        response.body(body);
    }

    public void cookie(String name, String value, int maxAge) {
        response.cookie(name, value, maxAge);
    }

    public void header(String header, String value) {
        response.header(header, value);
    }

    public void cookie(String name, String value, int maxAge, boolean secured) {
        response.cookie(name, value, maxAge, secured);
    }

    public void cookie(String name, String value) {
        response.cookie(name, value);
    }

    public void cookie(String path, String name, String value, int maxAge, boolean secured) {
        response.cookie(path, name, value, maxAge, secured);
    }

    public String body() {
        return response.body();
    }

    public void redirect(String location) {
        response.redirect(location);
    }

    public void redirect(String location, int httpStatusCode) {
        response.redirect(location, httpStatusCode);
    }

    public void removeCookie(String name) {
        response.removeCookie(name);
    }

    public void type(String contentType) {
        response.type(contentType);
    }
}
