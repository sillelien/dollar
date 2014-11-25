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

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.types.SerializedType;
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
    private RouteableNanoHttpd httpd;
    private String method = "GET";
    private ConcurrentHashMap<String, String> subscriptions = new ConcurrentHashMap<>();

    public HttpURIHandler(String scheme, String uri) throws URISyntaxException, IOException {
        if (uri.startsWith("//")) {
            this.uri = new URI(scheme + ":" + uri);
        } else {
            this.uri = new URI(uri);
            this.method = this.uri.getScheme();
        }
    }

    @Override
    public var all() {
        throw new UnsupportedOperationException();
    }

    @Override public void destroy() {
        //TODO
    }

    @Override
    public var drain() {
        throw new UnsupportedOperationException();
    }

    @Override
    public var get(var key) {
        throw new UnsupportedOperationException();
    }

    @Override public void init() {
        //TODO
    }

    @Override public void pause() {
        //TODO
    }

    @Override
    public var send(var value, boolean blocking, boolean mutating) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var receive(boolean blocking, boolean mutating) {
        try {
            return DollarFactory.fromStream(SerializedType.JSON, Unirest.get(uri.toString())
                    .asJson().getRawBody());
        } catch (UnirestException e) {
            return DollarStatic.handleError(e, null);
        } catch (IOException e) {
            return DollarStatic.handleError(e, null);
        }
    }

    @Override
    public var remove(var key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var removeValue(var v) {
        throw new UnsupportedOperationException();
    }

    public var set(var key, var value) {
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
    public void subscribe(Pipeable consumer, String id) throws IOException {
        httpd = getHttpServerFor(this.uri.getHost(), this.uri.getPort());
        httpd.handle(this.uri.getPath(), new RequestHandler(consumer));
        subscriptions.put(id, this.uri.getPath());
    }

    @Override public void unpause() {
        //TODO
    }

    @Override public void unsubscribe(String subId) {
        httpd.remove(subscriptions.get(subId));
    }

    private static RouteableNanoHttpd getHttpServerFor(String hostname, int port) throws IOException {
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

    public static class RouteableNanoHttpd extends NanoHttpd {

        private Map<String, RequestHandler> handlers = new HashMap<>();

        public RouteableNanoHttpd(String hostname, int port) {
            super(hostname, port);
        }

        public void handle(String key, RequestHandler handler) {
            handlers.put(key, handler);
        }

        public void remove(String key) {
            handlers.remove(key);
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
                        .$($("headers"), session.getHeaders())
                        .$($("params"), session.getParms())
                        .$($("uri"), session.getUri())
                        .$($("query"), session.getQueryParameterString())
                        .$($("method"), session.getMethod().name())
                        .$($("body"), "");
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
                }, body.$mimeType().$S(), body.S());
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
