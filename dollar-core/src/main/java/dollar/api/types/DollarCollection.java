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

package dollar.api.types;

import dollar.api.DollarException;
import dollar.api.Pipeable;
import dollar.api.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dollar.api.DollarStatic.$void;

public abstract class DollarCollection extends AbstractDollar {
    public DollarCollection() {super();}

    @NotNull
    @Override
    public Value $avg(boolean parallel) {
        return $sum(parallel).$divide($size());
    }

    @NotNull
    @Override
    public Value $max(boolean parallel) {
        return getStream(false).max(Comparator.comparing(Value::toDouble)).orElseThrow(
                () -> new DollarException("Null encountered"));
    }

    @NotNull
    @Override
    public Value $min(boolean parallel) {
        return getStream(false).min(Comparator.comparing(Value::toDouble)).orElseThrow(
                () -> new DollarException("Null encountered"));
    }

    @NotNull
    @Override
    public Value $size() {
        return DollarFactory.fromValue(getStream(false).count());
    }

    @Override
    public boolean collection() {
        return true;
    }

    @Override
    public @NotNull Stream<Value> stream(boolean parallel) {
        return getStream(false);
    }

    @NotNull
    @Override
    public Value $each(@NotNull Pipeable pipe) {
        return DollarFactory.fromList(getStream(true).map(i -> {
            try {
                return pipe.pipe(i);
            } catch (Exception e) {
                throw new DollarException(e);
            }
        }).collect(Collectors.toList()));
    }

    @NotNull
    @Override
    public Value $product(boolean parallel) {
        return getStream(false).reduce(Value::$multiply).orElse($void());
    }

    @NotNull
    @Override
    public Value $sort(boolean parallel) {
        return DollarFactory.fromList(getStream(true).sorted().collect(Collectors.toList()));
    }

    @NotNull
    @Override
    public Value $sum(boolean parallel) {
        return DollarFactory.fromValue(
                getStream(false).reduce(Value::$plus).orElseThrow(() -> new DollarException("Reduce returned null")));
    }

    @NotNull
    @Override
    public Value $unique(boolean parallel) {
        return DollarFactory.fromList(getStream(true).distinct().collect(Collectors.toList()));
    }

    @NotNull
    protected abstract Stream<Value> getStream(boolean chain);
}
