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

package com.sillelien.dollar.api.uri;

import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.sillelien.dollar.api.DollarStatic.$void;

public interface URIHandler {

    default var all() {return $void();}

    default var append(var value) {return write(value, true, true);}

    default var write(var value, boolean blocking, boolean mutating) {return $void();}

    default void destroy() {}

    default var drain() {return $void();}

    default var get(var key) {return $void();}

    default void init() {}

    default void pause() {}

    @NotNull default var prepend(var value) {return $void();}

    default var publish(var value) {
        return write(value, false, false);
    }

    default var read(boolean blocking, boolean mutating) {return $void();}

    default var remove(var v) {return $void();}

    default var removeValue(var v) {return $void();}

    default var set(var key, var value) {return $void();}

    default int size() { return 0;}

    default void start() {}

    default void stop() {}

    default void subscribe(Pipeable consumer, String id) throws IOException {}

    default void unpause() {}

    default void unsubscribe(String subId) {}
}
