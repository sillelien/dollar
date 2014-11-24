/*
 * Copyright (c) 2014 Neil Ellis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.neilellis.dollar;

import com.google.common.collect.ImmutableList;
import me.neilellis.dollar.types.DollarFactory;
import org.jetbrains.annotations.NotNull;
import spark.Request;
import spark.Response;

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

    public void body(@NotNull var body) {
        response.type(body.$mimeType().$S());
        response.body(body.S());
    }

    public void body(String body) {
        response.body(body);
    }

    public String body() {
        return response.body();
    }

    public void cookie(String name, String value, int maxAge) {
        response.cookie(name, value, maxAge);
    }

    public void cookie(String name, String value, int maxAge, boolean secured) {
        response.cookie(name, value, maxAge, secured);
    }

    public void cookie(String name, String value) {
        response.cookie(name, value);
    }

    public void cookie(String path, @NotNull String name, String value, int maxAge, boolean secured) {
        response.cookie(path, name, value, maxAge, secured);
    }

    @NotNull
    public var cookies() {
        return DollarFactory.fromValue(request.cookies(), ImmutableList.of());
    }

    public void header(String header, String value) {
        response.header(header, value);
    }

    @NotNull
    public var headers() {
        var result = $();
        Set<String> headers = request.headers();
        for (String header : headers) {
            result = result.$($(header), request.headers(header));
        }
        return result;
    }

    @NotNull
    public var json() {
        return DollarFactory.fromValue(request.body(), ImmutableList.of());
    }

    @NotNull
    public var params() {
        return DollarFactory.fromValue(request.params(), ImmutableList.of());
    }

    @NotNull
    public var queryParams() {
        return DollarFactory.fromValue(request.queryMap(), ImmutableList.of());
    }

    public void redirect(String location) {
        response.redirect(location);
    }

    public void redirect(String location, int httpStatusCode) {
        response.redirect(location, httpStatusCode);
    }

    public void removeCookie(@NotNull String name) {
        response.removeCookie(name);
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

    public void type(String contentType) {
        response.type(contentType);
    }
}
