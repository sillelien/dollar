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

package com.sillelien.dollar;

import org.junit.BeforeClass;
import org.junit.Test;

import static com.sillelien.dollar.api.DollarStatic.$;
import static org.junit.Assert.*;

public class DollarTypeTest {

    @BeforeClass
    public static void setUp() {

    }

    @Test
    public void testDouble() {
        //Number types
        assertEquals(1L, (long) $(1.0).toLong());

        //identity
        assertEquals($(1.0), $(1.0).toDouble());
        assertEquals($(1.0), $(1.0));
        assertNotEquals($(1.0).toDouble(), $(1.0));

        //Lambda

        assertEquals($((v) -> $(1)), $(1));
        assertEquals($(1), $((v) -> $(1)));
        assertTrue($((v) -> $(1.0)).dynamic());
        assertTrue($((v) -> $(1.0)).decimal());
        assertTrue($((v) -> $(1.0)).number());
        assertTrue($((v) -> $(1.0)).singleValue());

        //isChecks
        assertTrue($(1.0).decimal());
        assertTrue($(1.0).number());
        assertTrue($(1.0).singleValue());

        assertFalse($(1.0).integer());
        assertFalse($(1.0).map());
        assertFalse($(1.0).list());
        assertFalse($(1.0).dynamic());
        assertFalse($(1.0).string());
        assertFalse($(1.0).isVoid());
        assertFalse($(1.0).$isEmpty().isTrue());

        //numeric functions
        assertEquals(2.0, $(1.0).$inc().toDouble(), 0.00001);
    }

    @Test
    public void testIntegers() {
        //Number types
        assertEquals(1, (int) $(1).toInteger());
        assertEquals(1, (long) $(1).toLong());
        assertEquals(1.0, $(1).toDouble(), 0.000001);

        //identity
        assertEquals($(1), $(1).toLong());
        assertEquals($(1), $(1));
        assertNotEquals($(1).toLong(), $(1));

        //Lambda

        assertEquals($((v) -> $(1)), $(1));
        assertEquals($(1), $((v) -> $(1)));
        assertTrue($((v) -> $(1)).dynamic());
        assertTrue($((v) -> $(1)).integer());
        assertTrue($((v) -> $(1)).number());
        assertTrue($((v) -> $(1)).singleValue());


        //isChecks
        assertTrue($(1).integer());
        assertTrue($(1).number());
        assertTrue($(1).singleValue());

        assertFalse($(1).decimal());
        assertFalse($(1).map());
        assertFalse($(1).list());
        assertFalse($(1).dynamic());
        assertFalse($(1).string());
        assertFalse($(1).isVoid());
        assertFalse($(1).$isEmpty().isTrue());

        //numeric functions
        assertEquals(2, (long) $(1).$inc().toLong());


    }


}