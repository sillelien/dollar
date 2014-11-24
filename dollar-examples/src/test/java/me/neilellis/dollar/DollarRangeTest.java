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
import static me.neilellis.dollar.DollarStatic.$list;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DollarRangeTest {
    private static var range;

    @BeforeClass
    public static void setUp() {
        range = $($(1), $(4));
    }

    @Test
    public void testBasics() {
        System.err.println(range.toList());
        final var actual = $list(1).$plus($(2)).$plus($(3)).$plus($(4));
        assertTrue(range.equals(actual));
        assertTrue(actual.equals(range));
        System.err.println(actual._unwrap().getClass());
        assertEquals(range, actual);
        assertEquals(range, $($(1), $(4)));
    }


}