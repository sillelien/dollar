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

package me.neilellis.dollar.learner.hazelcast;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import me.neilellis.dollar.Type;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class UsageCounter implements com.hazelcast.nio.serialization.DataSerializable {
    private Type resultType;
    private AtomicLong usage;

    public UsageCounter() {
    }

    public UsageCounter(Type resultType, int usage) {

        this.resultType = resultType;
        this.usage = new AtomicLong(usage);
    }

    @Override
    public int hashCode() {
        int result = resultType.hashCode();
        result = 31 * result + usage.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        UsageCounter that = (UsageCounter) o;

        if (!resultType.equals(that.resultType)) { return false; }
        return true;
    }

    public void inc() {
        usage.incrementAndGet();
    }

    public Type type() {
        return resultType;
    }

    public long usage() {
        return usage.get();
    }


    @Override public void writeData(ObjectDataOutput out) throws IOException {
        out.writeLong(usage.get());
        out.writeInt(resultType.ordinal());
    }

    @Override public void readData(ObjectDataInput in) throws IOException {
        usage = new AtomicLong(in.readLong());
        resultType = Type.values()[in.readInt()];
    }
}
