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

package com.sillelien.dollar.uri.mapdb;

import com.sillelien.dollar.api.uri.URI;
import com.sillelien.dollar.api.uri.URIHandler;
import dollar.internal.mapdb.DB;
import dollar.internal.mapdb.DBMaker;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractMapDBURI implements URIHandler {
    private static final ConcurrentHashMap<String, DB> txs = new ConcurrentHashMap<>();
    @NotNull
    protected final String scheme;
    @NotNull protected final URI uri;
    @NotNull
    protected final DB tx;
    private final String host;

    public AbstractMapDBURI(
                                   @NotNull URI uri, @NotNull String scheme) {
        this.uri = uri;
        this.scheme = scheme;
        tx = getDB(uri.path());
        host = uri.host();
    }

    @NotNull
    protected static DB getDB(@NotNull String path) {

        final DB newDb = DBMaker.fileDB(new File(path))
                                 .executorEnable()
                                 .closeOnJvmShutdown()
                                 .transactionEnable()
                                 .make();
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
