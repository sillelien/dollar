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

import static junit.framework.TestCase.assertFalse;
import static me.neilellis.dollar.DollarStatic.$;
import static org.junit.Assert.*;

public class DollarTypeTest {

    @BeforeClass
    public static void setUp() {

    }

    @Test
    public void testIntegers() throws InterruptedException {
        //Number types
        assertEquals(1, (int) $(1).I());
        assertEquals(1, (long) $(1).L());
        assertEquals(1.0, $(1).D(), 0.000001);

        //identity
        assertEquals($(1), $(1).L());
        assertEquals($(1), $(1));
        assertNotEquals($(1).L(), $(1));

        //Lambda

        assertEquals($((v) -> $(1)), $(1));
        assertEquals($(1), $((v) -> $(1)));
        assertTrue($((v) -> $(1)).isLambda());
        assertTrue($((v) -> $(1)).isInteger());
        assertTrue($((v) -> $(1)).isNumber());
        assertTrue($((v) -> $(1)).isSingleValue());


        //isChecks
        assertTrue($(1).isInteger());
        assertTrue($(1).isNumber());
        assertTrue($(1).isSingleValue());

        assertFalse($(1).isDecimal());
        assertFalse($(1).isMap());
        assertFalse($(1).isList());
        assertFalse($(1).isLambda());
        assertFalse($(1).isString());
        assertFalse($(1).isVoid());
        assertFalse($(1).isEmpty());

        //numeric functions
        assertEquals(2, (long) $(1).$inc().L());


    }


    @Test
    public void testDouble() throws InterruptedException {
        //Number types
        assertEquals(1L, (long) $(1.0).L());

        //identity
        assertEquals($(1.0), $(1.0).D());
        assertEquals($(1.0), $(1.0));
        assertNotEquals($(1.0).D(), $(1.0));

        //Lambda

        assertEquals($((v) -> $(1)), $(1));
        assertEquals($(1), $((v) -> $(1)));
        assertTrue($((v) -> $(1.0)).isLambda());
        assertTrue($((v) -> $(1.0)).isDecimal());
        assertTrue($((v) -> $(1.0)).isNumber());
        assertTrue($((v) -> $(1.0)).isSingleValue());


        //isChecks
        assertTrue($(1.0).isDecimal());
        assertTrue($(1.0).isNumber());
        assertTrue($(1.0).isSingleValue());

        assertFalse($(1.0).isInteger());
        assertFalse($(1.0).isMap());
        assertFalse($(1.0).isList());
        assertFalse($(1.0).isLambda());
        assertFalse($(1.0).isString());
        assertFalse($(1.0).isVoid());
        assertFalse($(1.0).isEmpty());

        //numeric functions
        assertEquals(2.0, $(1.0).$inc().D(), 0.00001);
    }


}