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

package dollar;

import dollar.api.DollarStatic;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static dollar.api.DollarStatic.$;
import static org.junit.Assert.*;

public class DollarTypeTest {

    @BeforeAll
    public static void setUp() {

    }

    @Test
    public void testDouble() {
        //Number types
        assertEquals(1L, (long) DollarStatic.$(1.0).toLong());

        //identity
        assertEquals(DollarStatic.$(1.0), DollarStatic.$(1.0).toDouble());
        assertEquals(DollarStatic.$(1.0), DollarStatic.$(1.0));
        assertNotEquals(DollarStatic.$(1.0).toDouble(), DollarStatic.$(1.0));

        //Lambda

        assertEquals($((v) -> DollarStatic.$(1)), DollarStatic.$(1));
        assertEquals(DollarStatic.$(1), $((v) -> DollarStatic.$(1)));
        assertTrue($((v) -> DollarStatic.$(1.0)).dynamic());
        assertTrue($((v) -> DollarStatic.$(1.0)).decimal());
        assertTrue($((v) -> DollarStatic.$(1.0)).number());
        assertTrue($((v) -> DollarStatic.$(1.0)).singleValue());

        //isChecks
        assertTrue(DollarStatic.$(1.0).decimal());
        assertTrue(DollarStatic.$(1.0).number());
        assertTrue(DollarStatic.$(1.0).singleValue());

        assertFalse(DollarStatic.$(1.0).integer());
        assertFalse(DollarStatic.$(1.0).map());
        assertFalse(DollarStatic.$(1.0).list());
        assertFalse(DollarStatic.$(1.0).dynamic());
        assertFalse(DollarStatic.$(1.0).string());
        assertFalse(DollarStatic.$(1.0).isVoid());
        assertFalse(DollarStatic.$(1.0).$isEmpty().isTrue());

        //numeric functions
        assertEquals(2.0, DollarStatic.$(1.0).$inc().toDouble(), 0.00001);
    }

    @Test
    public void testIntegers() {
        //Number types
        assertEquals(1, (int) DollarStatic.$(1).toInteger());
        assertEquals(1, (long) DollarStatic.$(1).toLong());
        assertEquals(1.0, DollarStatic.$(1).toDouble(), 0.000001);

        //identity
        assertEquals(DollarStatic.$(1), DollarStatic.$(1).toLong());
        assertEquals(DollarStatic.$(1), DollarStatic.$(1));
        assertNotEquals(DollarStatic.$(1).toLong(), DollarStatic.$(1));

        //Lambda

        assertEquals($((v) -> DollarStatic.$(1)), DollarStatic.$(1));
        assertEquals(DollarStatic.$(1), $((v) -> DollarStatic.$(1)));
        assertTrue($((v) -> DollarStatic.$(1)).dynamic());
        assertTrue($((v) -> DollarStatic.$(1)).integer());
        assertTrue($((v) -> DollarStatic.$(1)).number());
        assertTrue($((v) -> DollarStatic.$(1)).singleValue());


        //isChecks
        assertTrue(DollarStatic.$(1).integer());
        assertTrue(DollarStatic.$(1).number());
        assertTrue(DollarStatic.$(1).singleValue());

        assertFalse(DollarStatic.$(1).decimal());
        assertFalse(DollarStatic.$(1).map());
        assertFalse(DollarStatic.$(1).list());
        assertFalse(DollarStatic.$(1).dynamic());
        assertFalse(DollarStatic.$(1).string());
        assertFalse(DollarStatic.$(1).isVoid());
        assertFalse(DollarStatic.$(1).$isEmpty().isTrue());

        //numeric functions
        assertEquals(2, (long) DollarStatic.$(1).$inc().toLong());


    }


}
