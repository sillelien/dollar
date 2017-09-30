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

package dollar.api.uri;

import dollar.api.DollarStatic;
import dollar.api.Pipeable;
import dollar.api.Value;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface URIHandler {

    @NotNull
    default Value all() {return DollarStatic.$void();}

    @NotNull
    default Value append(@NotNull Value value) {return write(value, true, true);}


    default void destroy() {}

    @NotNull
    default Value drain() {
        return DollarStatic.$void();
    }

    @NotNull
    default Value get(@NotNull Value key) {return DollarStatic.$void();}

    default void init() {}

    @NotNull
    default Value insert(@NotNull Value $) {
        return DollarStatic.$void();
    }

    default void pause() {}

    @NotNull
    default Value prepend(@NotNull Value value) {return DollarStatic.$void();}

    @NotNull
    default Value publish(@NotNull Value value) {
        return write(value, false, false);
    }

    @NotNull
    default Value read(boolean blocking, boolean mutating) {return DollarStatic.$void();}

    @NotNull
    default Value remove(@NotNull Value v) {return DollarStatic.$void();}

    @NotNull
    default Value removeValue(@NotNull Value v) {return DollarStatic.$void();}

    @NotNull
    default Value set(@NotNull Value key, @NotNull Value value) {return DollarStatic.$void();}

    default int size() { return 0;}

    default void start() {}

    default void stop() {}

    default void subscribe(@NotNull Pipeable consumer, @NotNull String id) throws IOException {}

    default void unpause() {}

    default void unsubscribe(@NotNull String subId) {}

    @NotNull
    default Value write(@NotNull Value value, boolean blocking, boolean mutating) {return DollarStatic.$void();}

    default @NotNull Value writeAll(Value value) {return value;}
}
