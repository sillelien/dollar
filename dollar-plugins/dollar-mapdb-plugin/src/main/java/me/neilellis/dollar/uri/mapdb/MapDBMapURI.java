/*
 * Copyright (c) 2014-2015 Neil Ellis
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

import me.neilellis.dollar.api.Pipeable;
import me.neilellis.dollar.api.types.DollarFactory;
import me.neilellis.dollar.api.types.ErrorType;
import me.neilellis.dollar.api.uri.URI;
import me.neilellis.dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import static me.neilellis.dollar.api.DollarStatic.$;
import static me.neilellis.dollar.api.DollarStatic.$void;

public class MapDBMapURI extends AbstractMapDBURI {

    private static final ConcurrentHashMap<String, Bind.MapListener<String, var>>
            subscribers =
            new ConcurrentHashMap<>();

    public MapDBMapURI(String scheme, URI uri) {
        super(uri, scheme);
        tx.execute((DB d) -> {
            if (!d.exists(getHost())) {
                d.createTreeMap(getHost()).valueSerializer(new VarSerializer());
            }
        });

    }

    @Override public var all() {
        return tx.execute((DB d) -> DollarFactory.fromValue(getTreeMap(d).snapshot()));
    }

    @Override public var write(@NotNull var value, boolean blocking, boolean mutating) {
        if (value.pair()) {
            return set($(value.getPairKey()), value.getPairValue());
        } else {
            throw new UnsupportedOperationException("Can only write pairs to a map");
        }
    }

    @Override public var drain() {
        return tx.execute((DB d) -> {
            final BTreeMap<String, var> treeMap = getTreeMap(d);
            final var result = DollarFactory.fromValue(treeMap.snapshot());
            treeMap.clear();
            return result;
        });
    }

    @Override public var get(@NotNull var key) {
        return tx.execute((DB d) -> DollarFactory.fromValue(getTreeMap(d).get(key._fixDeep())));
    }

    @NotNull @Override public var read(boolean blocking, boolean mutating) {
        throw new UnsupportedOperationException();
    }

    @Override public var remove(@NotNull var v) {
        return tx.execute((DB d) -> DollarFactory.fromValue(getTreeMap(d).remove(v._fixDeep())));

    }

    @NotNull @Override public var removeValue(var v) {
        throw new UnsupportedOperationException();
    }

    @Override public var set(@NotNull var key, @NotNull var value) {
        final var fixedValue = value._fixDeep();
        final String keyString = key.$S();
        if (!value.isVoid()) {
            return tx.execute(
                    (DB d) -> {
                        final var putVal = getTreeMap(d).put(keyString, fixedValue);
                        if (putVal == null) {
                            return $void();
                        }
                        return putVal;
                    });
        } else {
            return $void();
        }
    }

    @Override public int size() {
        return tx.execute((DB d) -> getTreeMap(d).size());
    }

    @Override public void subscribe(@NotNull Pipeable consumer, @NotNull String id) throws IOException {
        tx.execute((DB d) -> {
            final Bind.MapListener<String, var> listener = (key, oldVal, newVal) -> {
                try {
                    consumer.pipe($(key, newVal));
                } catch (Exception e) {
                    DollarFactory.failure(ErrorType.EXCEPTION, e, false);
                }
            };
            getTreeMap(d).modificationListenerAdd(listener);
            subscribers.put(id, listener);
        });
    }

    @Override public void unsubscribe(@NotNull String subId) {
        tx.execute((DB d) -> getTreeMap(d).modificationListenerRemove(subscribers.get(subId)));
    }

    private BTreeMap<String, var> getTreeMap(@NotNull DB d) {return d.getTreeMap(getHost());}
}
