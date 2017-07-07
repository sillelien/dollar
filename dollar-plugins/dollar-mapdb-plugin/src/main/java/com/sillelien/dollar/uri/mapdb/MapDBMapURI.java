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

package com.sillelien.dollar.uri.mapdb;

import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.types.DollarFactory;
import com.sillelien.dollar.api.types.ErrorType;
import com.sillelien.dollar.api.uri.URI;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.MapModificationListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.sillelien.dollar.api.DollarStatic.$;
import static com.sillelien.dollar.api.DollarStatic.$void;

public class MapDBMapURI extends AbstractMapDBURI implements MapModificationListener<var, var> {

    private static final ConcurrentHashMap<String, MapListener<var, var, var>>
            subscribers =
            new ConcurrentHashMap<>();
    private BTreeMap<var, var> bTreeMap;

    public MapDBMapURI(String scheme, URI uri) {
        super(uri, scheme);
        bTreeMap = tx.treeMap(getHost(), new VarSerializer(), new VarSerializer()).modificationListener(this).createOrOpen();
    }

    @Override
    public var all() {
        HashMap<var, var> result = new HashMap<>(bTreeMap);
        return DollarFactory.fromValue(result);
    }

    @Override
    public var write(@NotNull var value, boolean blocking, boolean mutating) {
        if (value.pair()) {
            return set($(value.getPairKey()), value.getPairValue());
        } else {
            throw new UnsupportedOperationException("Can only write pairs to a map");
        }
    }

    @Override
    public var drain() {
        HashMap<var, var> result = new HashMap<>(bTreeMap);
        bTreeMap.clear();
        tx.commit();
        return DollarFactory.fromValue(result);

    }

    @Override
    public var get(@NotNull var key) {
        return bTreeMap.get(key._fixDeep());
    }

    @NotNull
    @Override
    public var read(boolean blocking, boolean mutating) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var remove(@NotNull var v) {
        return bTreeMap.remove(v._fixDeep());

    }

    @NotNull
    @Override
    public var removeValue(var v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public var set(@NotNull var key, @NotNull var value) {
        if (!value.isVoid()) {
            return bTreeMap.put(key, value._fixDeep());
        } else {
            return $void();
        }
    }

    @Override
    public int size() {
        return bTreeMap.size();
    }

    @Override
    public void subscribe(@NotNull Pipeable consumer, @NotNull String id) throws IOException {

        subscribers.put(id, new MapListener<var, var, var>() {
            @Override
            public void apply(@NotNull var var, @Nullable var oldValue, @Nullable var newValue) {
                try {
                    consumer.pipe($(var, newValue));
                } catch (Exception e) {
                    DollarFactory.failure(ErrorType.EXCEPTION, e, false);
                }
            }
        });


    }

    @Override
    public void unsubscribe(@NotNull String subId) {
       subscribers.remove(subId);
    }

    private BTreeMap<var, var> getTreeMap(@NotNull DB d) {
        return bTreeMap;
    }

    @Override
    public void modify(@NotNull var key, @Nullable var oldValue, @Nullable var newValue, boolean triggered) {
        for (MapListener<var, var, var> listener : subscribers.values()) {
            listener.apply(key,oldValue,newValue);
        }
    }
}