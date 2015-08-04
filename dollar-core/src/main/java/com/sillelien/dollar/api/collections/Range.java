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

package com.sillelien.dollar.api.collections;

import com.sillelien.dollar.api.var;
import org.jetbrains.annotations.Nullable;

public final class Range {
    private final var start;
    private final var finish;

    public Range(var start, var finish) {
        this.start = start;
        this.finish = finish;
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + finish.hashCode();
        return result;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        Range range = (Range) o;

        if (!finish.equals(range.finish)) { return false; }
        return start.equals(range.start);

    }

    public boolean isEmpty() {
        return false;
    }

    public var lowerEndpoint() {
        return start;
    }

    public var upperEndpoint() {
        return finish;
    }
}
