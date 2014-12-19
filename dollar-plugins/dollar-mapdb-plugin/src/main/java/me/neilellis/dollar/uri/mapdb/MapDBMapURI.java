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
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.types.ErrorType;
import me.neilellis.dollar.uri.URI;
import me.neilellis.dollar.var;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import static me.neilellis.dollar.DollarStatic.$;
import static me.neilellis.dollar.DollarStatic.$void;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
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

    @Override public var write(var value, boolean blocking, boolean mutating) {
        if (value.isPair()) {
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

    @Override public var get(var key) {
        return tx.execute((DB d) -> DollarFactory.fromValue(getTreeMap(d).get(key._fixDeep())));
    }

    @Override public var read(boolean blocking, boolean mutating) {
        throw new UnsupportedOperationException();
    }

    @Override public var remove(var v) {
        return tx.execute((DB d) -> DollarFactory.fromValue(getTreeMap(d).remove(v._fixDeep())));

    }

    @Override public var removeValue(var v) {
        throw new UnsupportedOperationException();
    }

    @Override public var set(var key, var value) {
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

    @Override public void subscribe(Pipeable consumer, String id) throws IOException {
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

    @Override public void unsubscribe(String subId) {
        tx.execute((DB d) -> getTreeMap(d).modificationListenerRemove(subscribers.get(subId)));
    }

    private BTreeMap<String, var> getTreeMap(DB d) {return d.getTreeMap(getHost());}
}
