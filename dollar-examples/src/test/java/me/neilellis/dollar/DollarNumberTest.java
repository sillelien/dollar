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

package me.neilellis.dollar;

import org.junit.BeforeClass;
import org.junit.Test;

import static me.neilellis.dollar.DollarStatic.$;
import static org.junit.Assert.assertEquals;

public class DollarNumberTest {
    private static var list;

    @BeforeClass
    public static void setUp() {

    }

    @Test
    public void testBasics() {
        var map = $("foo", 1).$("bar", 10);
        assertEquals(2, (long) $(1).$inc().I());
        assertEquals(4, (long) $(1).$inc(3).I());
        map = map.$inc("foobar", 1);
        map = map.$inc("bar", 10);
        map = map.$dec("foo", 5);
        assertEquals(20, (long) map.$("bar").I());
        assertEquals(-4, (long) map.$("foo").I());
        assertEquals(1, (long) map.$("foobar").I());

    }


}