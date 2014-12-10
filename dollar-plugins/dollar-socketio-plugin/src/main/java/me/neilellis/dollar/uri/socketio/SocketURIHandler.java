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

package me.neilellis.dollar.uri.socketio;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import me.neilellis.dollar.DollarException;
import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.uri.URI;
import me.neilellis.dollar.uri.URIHandler;
import me.neilellis.dollar.var;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class SocketURIHandler implements URIHandler {
    public static final int BLOCKING_TIMEOUT = 10;
    private static final ConcurrentHashMap<String, SocketIOServer> servers = new ConcurrentHashMap<>();
    private final URI uri;
    private final ConcurrentHashMap<String, SocketIOSubscription> subscriptions = new ConcurrentHashMap<>();
    private SocketIOServer server;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            servers.values().forEach(SocketIOServer::stop);
        }));
    }

    public SocketURIHandler(String scheme, String uri) throws IOException {
        if (uri.startsWith("//")) {
            this.uri = URI.parse(scheme + ":" + uri);
        } else {
            this.uri = URI.parse(uri);
        }
    }

    @Override
    public var all() {
        throw new UnsupportedOperationException();
    }

    @Override public void destroy() {
        for (SocketIOSubscription socketIOSubscription : subscriptions.values()) {
            socketIOSubscription.destroy();
        }
        server.stop();

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
        try {
            server = getServerFor(this.uri.host(), this.uri.port());
        } catch (IOException e) {
            throw new DollarException(e);
        }

    }

    @Override public void pause() {
        server.stop();
    }

    @Override public var publish(var value) {
        ArrayList<var> responses = new ArrayList<>();
        server.getBroadcastOperations().sendEvent(value.getPairKey(), value.getPairValue().json().toMap());
        for (SocketIOSubscription subscription : subscriptions.values()) {
        }
        return DollarFactory.fromValue(responses);
    }

    @Override
    public var send(var value, boolean blocking, boolean mutating) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var receive(boolean blocking, boolean mutating) {
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

    public var set(var key, var value) {
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
    public void subscribe(Pipeable consumer, String id) throws IOException {
        final SocketIOSubscription listener = new SocketIOSubscription(consumer, id, uri);
        server.addMessageListener(listener);
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

    @Override public void unsubscribe(String subId) {
        subscriptions.remove(subId).destroy();
    }

    private static SocketIOServer getServerFor(String hostname, int port) throws IOException {
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
