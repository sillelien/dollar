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

package com.sillelien.dollar.uri.socketio;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.uri.URI;
import com.sillelien.dollar.api.uri.URIHandler;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocketURIHandler implements URIHandler {
    public static final int BLOCKING_TIMEOUT = 10;
    private static final ConcurrentHashMap<String, SocketIOServer> servers = new ConcurrentHashMap<>();
    @NotNull private final URI uri;
    private final ConcurrentHashMap<String, SocketIOSubscription> subscriptions = new ConcurrentHashMap<>();
    private SocketIOServer server;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            servers.values().forEach(SocketIOServer::stop);
        }));
    }

    public SocketURIHandler(String scheme, @NotNull URI uri) {
        this.uri = uri.sub();
    }

    @NotNull @Override
    public var all() {
        throw new UnsupportedOperationException();
    }

    @NotNull @Override
    public var write(var value, boolean blocking, boolean mutating) {
        throw new UnsupportedOperationException();
    }

    @Override public void destroy() {
        for (SocketIOSubscription socketIOSubscription : subscriptions.values()) {
            socketIOSubscription.destroy();
        }
        server.stop();

    }

    @NotNull @Override
    public var drain() {
        throw new UnsupportedOperationException();
    }

    @NotNull @Override
    public var get(var key) {
        throw new UnsupportedOperationException();
    }

    @Override public void init() {
        server = getServerFor(uri.host(), uri.port());

    }

    @Override public void pause() {
        server.stop();
    }

    @NotNull @Override public var publish(@NotNull var value) {
        ArrayList<var> responses = new ArrayList<>();
        server.getBroadcastOperations()
              .sendEvent(value.getPairKey().toString(), value.getPairValue().toJsonObject().toMap());
        for (SocketIOSubscription subscription : subscriptions.values()) {
        }
        return DollarFactory.fromValue(responses);
    }

    @NotNull @Override
    public var read(boolean blocking, boolean mutating) {
        throw new UnsupportedOperationException();
    }

    @NotNull @Override
    public var remove(var key) {
        throw new UnsupportedOperationException();
    }

    @NotNull @Override
    public var removeValue(var v) {
        throw new UnsupportedOperationException();
    }

    @NotNull public var set(var key, var value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override public void start() {
        server.start();
    }

    @Override public void stop() {
        server.stop();
    }

    @Override
    public void subscribe(Pipeable consumer, @NotNull String id) throws IOException {
        final SocketIOSubscription listener = new SocketIOSubscription(consumer, id, uri);
        server.addListeners(listener);
        server.addConnectListener(listener);
        server.addDisconnectListener(listener);
        final Map<String, List<String>> query = uri.query();
        if (uri.hasParam("eventType")) {
            final List<String> eventTypes = query.getOrDefault("eventType", Collections.EMPTY_LIST);
            for (String eventType : eventTypes) {
                server.addEventListener(eventType, String.class, listener);
            }
        }
        subscriptions.put(id, listener);
    }

    @Override public void unpause() {
        server.start();
    }

    @Override public void unsubscribe(@NotNull String subId) {
        subscriptions.remove(subId).destroy();
    }

    private static SocketIOServer getServerFor(String hostname, int port) {
        String key = hostname + ":" + port;
        if (servers.containsKey(key)) {
            return servers.get(key);
        } else {
            Configuration config = new Configuration();
            config.setHostname(hostname);
            config.setPort(port);

            SocketIOServer server = new SocketIOServer(config);

            final SocketIOServer socketIOServer = servers.putIfAbsent(key, server);
            if (socketIOServer == null) {
//                server.start();
            }
            return server;
        }
    }

}
