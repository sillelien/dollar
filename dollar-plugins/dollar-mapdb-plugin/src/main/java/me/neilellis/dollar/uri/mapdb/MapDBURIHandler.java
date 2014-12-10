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

package me.neilellis.dollar.uri.mapdb;

import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.uri.URI;
import me.neilellis.dollar.uri.URIHandler;
import me.neilellis.dollar.var;

import java.io.IOException;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class MapDBURIHandler implements URIHandler {
    public MapDBURIHandler(String scheme, URI uri) {}

    @Override public var all() {
        return null; //TODO
    }

    @Override public void destroy() {
        //TODO
    }

    @Override public var drain() {
        return null; //TODO
    }

    @Override public var get(var key) {
        return null; //TODO
    }

    @Override public void init() {
        //TODO
    }

    @Override public void pause() {
        //TODO
    }

    @Override public var send(var value, boolean blocking, boolean mutating) {
        return null; //TODO
    }

    @Override public var receive(boolean blocking, boolean mutating) {
        return null; //TODO
    }

    @Override public var remove(var v) {
        return null; //TODO
    }

    @Override public var removeValue(var v) {
        return null; //TODO
    }

    @Override public var set(var key, var value) {
        return null; //TODO
    }

    @Override public int size() {
        return 0; //TODO
    }

    @Override public void start() {
        //TODO
    }

    @Override public void stop() {
        //TODO
    }

    @Override public void subscribe(Pipeable consumer, String id) throws IOException {
        //TODO
    }

    @Override public void unpause() {
        //TODO
    }

    @Override public void unsubscribe(String subId) {
        //TODO
    }
}
