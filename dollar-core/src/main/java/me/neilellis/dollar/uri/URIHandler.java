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

package me.neilellis.dollar.uri;

import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.var;

import java.io.IOException;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public interface URIHandler {

    /**
     * Subscribe, listen semantics.
     *
     * @param consumer
     */
    void subscribe(Pipeable consumer) throws IOException;



    /**
     * Send to a point with multiple listeners.
     *
     * Think Chat.
     *
     * @param value
     * @return
     */
    default var publish(var value) {
        return send(value, false, false);
    }


    /**
     * Think Map.
     *
     * @param key
     * @param value
     * @return
     */
    var set(var key, var value);

    /**
     * Think Map.
     *
     * Map behaviour.
     *
     * @param key
     * @return
     */
    var get(var key);

    var all();

    var remove(var v);

    var removeValue(var v);

    int size();

    var drain();

    var receive(boolean blocking, boolean mutating);

    var send(var value, boolean blocking, boolean mutating);
}
