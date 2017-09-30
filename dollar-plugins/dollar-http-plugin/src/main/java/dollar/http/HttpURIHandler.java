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

package dollar.http;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import dollar.api.DollarException;
import dollar.api.Pipeable;
import dollar.api.Value;
import dollar.api.types.DollarFactory;
import dollar.api.types.SerializedType;
import dollar.api.uri.URI;
import dollar.api.uri.URIHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dollar.api.DollarStatic.$;

public class HttpURIHandler implements URIHandler {

    public static final int BLOCKING_TIMEOUT = 10;
    @NotNull
    private static final Logger log = LoggerFactory.getLogger(HttpURIHandler.class);
    @NotNull
    private static final ConcurrentHashMap<String, RouteableNanoHttpd> servers = new ConcurrentHashMap<>();
    @NotNull
    private final ConcurrentHashMap<String, String> subscriptions = new ConcurrentHashMap<>();
    @NotNull
    private final URI uri;
    @NotNull
    private RouteableNanoHttpd httpd;
    @Nullable
    private String method = "GET";

    public HttpURIHandler(@NotNull String scheme, @NotNull URI uri) {
        if (uri.hasSubScheme()) {
            this.uri = URI.of(scheme + ":" + uri.sub().sub().asString());
            method = this.uri.sub().scheme();
        } else {
            this.uri = uri;
        }
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

    @NotNull
    @Override
    public Value all() {
        //TODO: decide better implementation
        return read(false, false);
    }

    @Override
    public void destroy() {
        //TODO
    }

    @NotNull
    @Override
    public Value drain() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Value get(@NotNull Value key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void init() {
        //TODO
    }

    @Override
    public void pause() {
        //TODO
    }

    @NotNull
    @Override
    public Value read(boolean blocking, boolean mutating) {
        try {
            return DollarFactory.fromIOStream(SerializedType.JSON, Unirest.get(uri.toString())
                                                                         .header("Accept", "application/json")
                                                                         .asJson().getRawBody());
        } catch (UnirestException | IOException e) {
            throw new DollarException(e);
        }
    }

    @NotNull
    @Override
    public Value remove(@NotNull Value key) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public Value removeValue(@NotNull Value v) {
        throw new UnsupportedOperationException();
    }

    @Override
    @NotNull
    public Value set(@NotNull Value key, @NotNull Value value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void start() {
        //TODO
    }

    @Override
    public void stop() {
        //TODO
    }

    @Override
    public void subscribe(@NotNull Pipeable consumer, @NotNull String id) throws IOException {
        httpd = getHttpServerFor(uri.host(), (uri.port() > 0) ? uri.port() : 80);
        final String path = uri.path();
        httpd.handle(path, new RequestHandler(consumer));
        subscriptions.put(id, path);
    }

    @Override
    public void unpause() {
        //TODO
    }

    @Override
    public void unsubscribe(@NotNull String subId) {
        httpd.remove(subscriptions.get(subId));
    }

    @NotNull
    @Override
    public Value write(@NotNull Value value, boolean blocking, boolean mutating) {
        throw new UnsupportedOperationException();
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

        @NotNull
        @Override
        public Response serve(@NotNull IHTTPSession session) {
            URI uri;
            uri = URI.of(session.getUri());
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

        @NotNull
        public NanoHttpdServer.Response invoke(@NotNull NanoHttpdServer.IHTTPSession session) {
            try {
                Value in = $()
                                   .$set($("headers"), session.getHeaders())
                                   .$set($("params"), session.getParms())
                                   .$set($("uri"), session.getUri())
                                   .$set($("query"), session.getQueryParameterString())
                                   .$set($("method"), session.getMethod().name())
                                   .$set($("body"), "");
//                session.getInputStream().close();
                Value out = consumer.pipe(in);
                Value body = out.$get($("body"));
                NanoHttpdServer.Response
                        response =
                        new NanoHttpdServer.Response(new NanoHttpdServer.Response.IStatus() {
                            @NotNull
                            @Override
                            public String getDescription() {
                                return out.$get($("reason")).$default($("")).toHumanString();
                            }

                            @Override
                            public int getRequestStatus() {
                                return out.$get($("status")).$default($(200)).toInteger();
                            }
                        }, body.$mimeType().$S(), body.toStream());
                out.$get($("headers")).$map().toVarMap().forEach((s, v) -> response.addHeader(s.$S(), v.$S()));
                return response;
            } catch (Exception e) {
                log.debug(e.getMessage(), e);
                return new NanoHttpdServer.Response(NanoHttpdServer.Response.Status.INTERNAL_ERROR, "text/plain",
                                                    e.getMessage());
            }
        }
    }
}
