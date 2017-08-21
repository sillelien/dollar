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

package com.sillelien.dollar.api.uri;

import com.sillelien.dollar.api.Pipeable;
import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static com.sillelien.dollar.api.DollarStatic.$void;

public interface URIHandler {

    @NotNull
    default var all() {return $void();}

    @NotNull
    default var append(var value) {return write(value, true, true);}

    @NotNull
    default var write(var value, boolean blocking, boolean mutating) {return $void();}

    default void destroy() {}

    @NotNull
    default var drain() {return $void();}

    @NotNull
    default var get(var key) {return $void();}

    default void init() {}

    default void pause() {}

    @NotNull
    default var prepend(var value) {return $void();}

    @NotNull
    default var publish(var value) {
        return write(value, false, false);
    }

    @NotNull
    default var read(boolean blocking, boolean mutating) {return $void();}

    @NotNull
    default var remove(var v) {return $void();}

    @NotNull
    default var removeValue(var v) {return $void();}

    @NotNull
    default var set(var key, var value) {return $void();}

    default int size() { return 0;}

    default void start() {}

    default void stop() {}

    default void subscribe(Pipeable consumer, String id) throws IOException {}

    default void unpause() {}

    default void unsubscribe(String subId) {}

    @NotNull
    default var insert(var $) {
        return $void();
    }
}
