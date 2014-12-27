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
import me.neilellis.dollar.var;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
class SocketIOSubscription implements DataListener<String>, ConnectListener, DisconnectListener {
    private final Pipeable consumer;
    private final String id;
    private final me.neilellis.dollar.uri.URI uri;
    private boolean destroyed;

    public SocketIOSubscription(Pipeable consumer, String id, me.neilellis.dollar.uri.URI uri) {

        this.consumer = consumer;
        this.id = id;
        this.uri = uri;
    }

    public void destroy() {
        destroyed = true;
    }

    @Override public void onConnect(SocketIOClient client) {
        if (!destroyed) {
            client.joinRoom(getPath());
        }
    }

    private String getPath() {return uri.path().substring(1);}

    @Override public void onData(SocketIOClient client, String data, AckRequest ackSender) throws Exception {
        if (!destroyed) {
            final var result = consumer.pipe(DollarFactory.fromStringValue(data));
            if (ackSender.isAckRequested()) {
                ackSender.sendAckData(result.toHumanString());
            }
        }

    }

    @Override public void onDisconnect(SocketIOClient client) {
        client.leaveRoom(getPath());
    }

}
