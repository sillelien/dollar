/*
 *    Copyright (c) 2014-2017 Neil Ellis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.sillelien.dollar.http;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.types.SerializedType;
import com.sillelien.dollar.api.uri.URI;
import com.sillelien.dollar.api.uri.URIHandler;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.sillelien.dollar.api.DollarStatic.$;

public class HttpURIHandler implements URIHandler {
    public static final int BLOCKING_TIMEOUT = 10;
    @NotNull
    private static final ConcurrentHashMap<String, RouteableNanoHttpd> servers = new ConcurrentHashMap<>();
    @NotNull private final URI uri;
    @NotNull
    private final ConcurrentHashMap<String, String> subscriptions = new ConcurrentHashMap<>();
    @NotNull
    private RouteableNanoHttpd httpd;
    @Nullable private String method = "GET";

    public HttpURIHandler(@NotNull String scheme, @NotNull URI uri) {
        if (uri.hasSubScheme()) {
            this.uri = URI.parse(scheme + ":" + uri.sub().sub().asString());
            method = this.uri.sub().scheme();
        } else {
            this.uri = uri;
        }
    }

    @NotNull @Override
    public var all() {
        throw new UnsupportedOperationException();
    }

    @NotNull @Override
    public var write(@NotNull var value, boolean blocking, boolean mutating) {
        throw new UnsupportedOperationException();
    }

    @Override public void destroy() {
        //TODO
    }

    @NotNull @Override
    public var drain() {
        throw new UnsupportedOperationException();
    }

    @NotNull @Override
    public var get(@NotNull var key) {
        throw new UnsupportedOperationException();
    }

    @Override public void init() {
        //TODO
    }

    @Override public void pause() {
        //TODO
    }

    @NotNull @Override
    public var read(boolean blocking, boolean mutating) {
        try {
            return DollarFactory.fromStream(SerializedType.JSON, Unirest.get(uri.toString())
                    .header("Accept","application/json")
                    .asJson().getRawBody());
        } catch (UnirestException | IOException e) {
            return DollarStatic.handleError(e, null);
        }
    }

    @NotNull @Override
    public var remove(@NotNull var key) {
        throw new UnsupportedOperationException();
    }

    @NotNull @Override
    public var removeValue(@NotNull var v) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public var set(@NotNull var key, @NotNull var value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override public void start() {
        //TODO
    }

    @Override public void stop() {
        //TODO
    }

    @Override
    public void subscribe(@NotNull Pipeable consumer, @NotNull String id) throws IOException {
        httpd = getHttpServerFor(uri.host(), (uri.port() > 0) ? uri.port() : 80);
        final String path = uri.path();
        httpd.handle(path, new RequestHandler(consumer));
        subscriptions.put(id, path);
    }

    @Override public void unpause() {
        //TODO
    }

    @Override public void unsubscribe(@NotNull String subId) {
        httpd.remove(subscriptions.get(subId));
    }

    @NotNull
    private static RouteableNanoHttpd getHttpServerFor(@NotNull String hostname, int port) throws IOException {
        String key = hostname + ":" + port;
        if (servers.containsKey(key)) {
            return servers.get(key);
        } else {
            RouteableNanoHttpd nanoHttpd = new RouteableNanoHttpd(hostname, port);
            servers.putIfAbsent(key, nanoHttpd);
            nanoHttpd.start();
            return nanoHttpd;
        }
    }

    public static class RouteableNanoHttpd extends NanoHttpdServer {

        @NotNull
        private final Map<String, RequestHandler> handlers = new HashMap<>();

        public RouteableNanoHttpd(@NotNull String hostname, int port) {
            super(hostname, port);
        }

        public void handle(@NotNull String key, @NotNull RequestHandler handler) {
            handlers.put(key, handler);
        }

        public void remove(@NotNull String key) {
            handlers.remove(key);
        }

        @NotNull @Override
        public Response serve(@NotNull IHTTPSession session) {
            URI uri;
            uri = URI.parse(session.getUri());
            RequestHandler requestHandler = handlers.get(uri.path());
            if (requestHandler == null) {
                return new Response(Response.Status.NOT_FOUND, "text/plain", "");
            }
            return requestHandler.invoke(session);
        }

    }

    public class RequestHandler {
        @NotNull
        private final Pipeable consumer;

        public RequestHandler(@NotNull Pipeable consumer) {
            this.consumer = consumer;
        }

        @NotNull public NanoHttpdServer.Response invoke(@NotNull NanoHttpdServer.IHTTPSession session) {
            try {
                var in = $()
                        .$set($("headers"), session.getHeaders())
                        .$set($("params"), session.getParms())
                        .$set($("uri"), session.getUri())
                        .$set($("query"), session.getQueryParameterString())
                        .$set($("method"), session.getMethod().name())
                        .$set($("body"), "");
//                session.getInputStream().close();
                var out = consumer.pipe(in);
                var body = out.$("body");
                NanoHttpdServer.Response
                        response =
                        new NanoHttpdServer.Response(new NanoHttpdServer.Response.IStatus() {
                            @NotNull
                            @Override
                            public String getDescription() {
                                return out.$("reason").$default($("")).toHumanString();
                            }

                    @Override
                    public int getRequestStatus() {
                        return out.$("status").$default($(200)).toInteger();
                    }
                        }, body.$mimeType().$S(), body.toStream());
                out.$("headers").$map().toVarMap().forEach((s, v) -> response.addHeader(s.$S(), v.$S()));
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                return new NanoHttpdServer.Response(NanoHttpdServer.Response.Status.INTERNAL_ERROR, "text/plain",
                                                    e.getMessage());
            }
        }
    }
}
