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

import me.neilellis.dollar.uri.URI;
import me.neilellis.dollar.uri.URIHandler;
import org.mapdb.DBMaker;
import org.mapdb.TxMaker;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public abstract class AbstractMapDBURI implements URIHandler {
    private static final ConcurrentHashMap<String, TxMaker> txs = new ConcurrentHashMap<>();
    protected final String scheme;
    protected final URI uri;
    protected TxMaker tx;
    private String host;

    public AbstractMapDBURI(
            URI uri, String scheme) {
        this.uri = uri;
        this.scheme = scheme;
        tx = getDB(uri.path());
        host = uri.host();
    }

    protected static TxMaker getDB(String path) {

        final TxMaker newDb = DBMaker.newFileDB(new File(path))
                                     .closeOnJvmShutdown()
                                     .makeTxMaker();
        txs.putIfAbsent(path, newDb);
        return newDb;

    }

    @Override public void destroy() {
        //TODO
    }

    @Override public void init() {
        //TODO
    }

    @Override public void pause() {
        //TODO
    }

    @Override public void start() {
        //TODO
    }

    @Override public void stop() {
        //TODO
    }

    @Override public void unpause() {
        //TODO
    }

    protected String getHost() {return host;}
}
