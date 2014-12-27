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

public class DollarControlFlowTest {
    private static var map;

    @BeforeClass
    public static void setUp() {
        map = $($("Neil", 0), $("Dimple", 10), $("Charlie", 100));
    }

    @Test
    public void testBasics() throws InterruptedException {
        assertEquals(100, (long) $("Charlie").$choose(map).toLong());

    }


}