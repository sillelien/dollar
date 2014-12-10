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

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.types.FailureType;
import me.neilellis.dollar.var;

import java.net.URI;

import static me.neilellis.dollar.DollarStatic.$void;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
class SocketIOSubscription implements DataListener<String>, ConnectListener, DisconnectListener {
    private final Pipeable consumer;
    private final String id;
    private final URI uri;
    private boolean destroyed;
    private SocketIOClient client;

    public SocketIOSubscription(Pipeable consumer, String id, URI uri) {

        this.consumer = consumer;
        this.id = id;
        this.uri = uri;
    }

    public void destroy() {
        destroyed = true;
        client.leaveRoom(getPath());
        client = null;
    }

    private String getPath() {return uri.getPath().substring(1);}

    @Override public void onConnect(SocketIOClient client) {
        if (!destroyed) {
            this.client = client;
            client.joinRoom(getPath());
        }
    }

    @Override public void onData(SocketIOClient client, String data, AckRequest ackSender) throws Exception {
        if (!destroyed) {
            final var result = consumer.pipe(DollarFactory.fromStringValue(data));
            if (ackSender.isAckRequested()) {
                ackSender.sendAckData(result.S());
            }
        }

    }

    @Override public void onDisconnect(SocketIOClient client) {
        this.client = null;
    }

    public var push(var value) {
        if (!destroyed) {
            if (this.client == null) {
                return DollarFactory.failure(FailureType.IO, "Could not push value as client has disconnected", true);
            }
            client.sendJsonObject(value.S());
            return $void();
        } else {
            return DollarFactory.failure(FailureType.IO, "Subscription is destroyed", true);
        }
    }
}
