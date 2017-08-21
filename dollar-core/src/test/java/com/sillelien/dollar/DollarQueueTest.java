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

package com.sillelien.dollar;

import com.sillelien.dollar.api.var;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static com.sillelien.dollar.api.DollarStatic.$;
import static com.sillelien.dollar.api.DollarStatic.$blockingQueue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DollarQueueTest {

    @BeforeClass
    public static void setUp() {

    }

    @Test
    public void testBasics() {
        var queue = $blockingQueue();
        assertTrue(queue.queue());
        queue.$push($("Hello World"));
        assertEquals(1, queue.size());
        queue.$push($("Goodbye"));
        assertEquals(2, queue.size());
        assertEquals("Hello World", queue.$peek().toString());
        assertEquals(2, queue.size());
        assertEquals("Hello World", queue.$pop().toString());
        assertEquals(1, queue.size());
        assertEquals("Goodbye", queue.$pop().toString());
        assertEquals(0, queue.size());
        assertTrue(queue.isEmpty());
        queue.$push($(1));
        queue.$push($(2));
        queue.$push($(3));
        var drain = queue.$drain();
        assertEquals(3, drain.size());
        assertEquals(0, queue.size());
    }

    @Test
    public void testSubscribe() {
        var queue = $blockingQueue();
        AtomicInteger integer = new AtomicInteger();
        queue.$subscribe(in -> {
            integer.incrementAndGet();
            assertEquals((long) integer.get(), (long) in[0].toInteger());
            return in[0];
        });
        queue.$push($(1));
        queue.$push($(2));
        queue.$push($(3));
        assertEquals(3, integer.get());
    }


}
