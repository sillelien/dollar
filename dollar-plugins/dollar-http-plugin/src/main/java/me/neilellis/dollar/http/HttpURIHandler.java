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

package me.neilellis.dollar.http;

import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.uri.URIHandler;
import me.neilellis.dollar.var;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.neilellis.dollar.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class HttpURIHandler implements URIHandler {
    public static final int BLOCKING_TIMEOUT = 10;
    private static final ConcurrentHashMap<String, RouteableNanoHttpd> servers = new ConcurrentHashMap<>();
    private final URI uri;
    private final RouteableNanoHttpd httpd;
    private String method = "GET";

    public HttpURIHandler(String scheme, String uri) throws URISyntaxException, IOException {
        if (uri.startsWith("://")) {
            this.uri = new URI(scheme + uri);
        } else {
            this.uri = new URI(uri);
            this.method = this.uri.getScheme();
        }
        httpd = getHttpServerFor(this.uri.getHost(), this.uri.getPort());
        System.out.println(httpd);
    }

    private static RouteableNanoHttpd getHttpServerFor(String hostname, int port) throws IOException {
        String key = hostname + ":" + port;
        if (servers.contains(key)) {
            return servers.get(key);
        } else {
            RouteableNanoHttpd nanoHttpd = new RouteableNanoHttpd(hostname, port);
            servers.putIfAbsent(key, nanoHttpd);
            nanoHttpd.start();
            System.out.println(nanoHttpd);
            return nanoHttpd;
        }
    }

    @Override
    public var dispatch(var value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void subscribe(Pipeable consumer) throws IOException {
        httpd.handle(this.uri.getPath(), new RequestHandler(consumer));
    }

    @Override
    public var poll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public var receive() {
        throw new UnsupportedOperationException();
    }

    @Override
    public var send(var value) {
        return dispatch(value);
    }

    @Override
    public var push(var value) {
        return dispatch(value);
    }

    @Override
    public var peek() {
        throw new UnsupportedOperationException();
    }

    @Override
    public var set(var key, var value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var get(var key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var all() {
        throw new UnsupportedOperationException();
    }

    @Override
    public var remove(var key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var removeValue(var v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public var give(var value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var drain() {
        throw new UnsupportedOperationException();
    }

    public static class RouteableNanoHttpd extends NanoHttpd {

        private Map<String, RequestHandler> handlers = new HashMap<>();

        public RouteableNanoHttpd(String hostname, int port) {
            super(hostname, port);
        }

        public void handle(String key, RequestHandler handler) {
            handlers.put(key, handler);
        }


        @Override
        public Response serve(IHTTPSession session) {
            URI uri;
            try {
                uri = new URI(session.getUri());
                RequestHandler requestHandler = handlers.get(uri.getPath());
                if (requestHandler == null) {
                    return new Response(Response.Status.NOT_FOUND, "text/plain", "");
                }
                return requestHandler.invoke(session);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return new Response(Response.Status.BAD_REQUEST, "text/plain", "");
            }
        }

    }

    public class RequestHandler {
        private final Pipeable consumer;

        public RequestHandler(Pipeable consumer) {
            this.consumer = consumer;
        }

        public NanoHttpd.Response invoke(NanoHttpd.IHTTPSession session) {
            try {
                var in = $()
                        .$("headers", session.getHeaders())
                        .$("params", session.getParms())
                        .$("uri", session.getUri())
                        .$("query", session.getQueryParameterString())
                        .$("method", session.getMethod().name())
                        .$("body", "");
//                session.getInputStream().close();
                var out = consumer.pipe(in);
                var body = out.$("body");
                NanoHttpd.Response response = new NanoHttpd.Response(new NanoHttpd.Response.IStatus() {
                    @Override
                    public int getRequestStatus() {
                        return out.$("status").$default(200).I();
                    }

                    @Override
                    public String getDescription() {
                        return out.$("reason").$default("").S();
                    }
                }, body.$mimeType(), body.S());
                out.$("headers").$map().forEach((s, v) -> response.addHeader(s, v.$S()));
                response.setData(body.toStream());
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                return new NanoHttpd.Response(NanoHttpd.Response.Status.INTERNAL_ERROR, "text/plain", e.getMessage());
            }
        }
    }
}
