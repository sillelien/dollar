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

    @Test
    public void testDivide() {

        var lhs = $(40.1);
        var rhs = $(5.3);
        assertEquals(7, (long) lhs.$divide(rhs).I());
        assertEquals(0, (long) rhs.$divide(lhs).I());

        assertEquals(5.3 / 40.1, rhs.$divide(lhs).D(), 0.01);
        assertEquals(40.1 / 5.3, lhs.$divide(rhs).D(), 0.01);

        var intLhs = $(30);
        var intRhs = $(2);

        assertEquals(5, (long) intLhs.$divide(rhs).I());
        assertEquals(0, (long) rhs.$divide(intLhs).I());
        assertEquals(15, (long) intLhs.$divide(intRhs).I());

        assertEquals(0.177, rhs.$divide(intLhs).D(), 0.01);
        assertEquals(5.660377358490567, intLhs.$divide(rhs).D(), 0.01);
        assertEquals(15.0, intLhs.$divide(intRhs).D(), 0.01);

    }

    @Test
    public void testMod() {
        var lhs = $(40.1);
        var rhs = $(5.3);
        assertEquals(3, (long) lhs.$modulus(rhs).I());
        assertEquals(5, (long) rhs.$modulus(lhs).I());

        assertEquals(5.3 % 40.1, rhs.$modulus(lhs).D(), 0.01);
        assertEquals(40.1 % 5.3, lhs.$modulus(rhs).D(), 0.01);

        var intLhs = $(30);
        var intRhs = $(2);

        assertEquals(3, (long) intLhs.$modulus(rhs).L());
        assertEquals(5, (long) rhs.$modulus(intLhs).L());
        assertEquals(0, (long) intLhs.$modulus(intRhs).I());

        assertEquals(5.3, rhs.$modulus(intLhs).D(), 0.01);
        assertEquals(3.5, intLhs.$modulus(rhs).D(), 0.01);
        assertEquals(0.0, intLhs.$modulus(intRhs).D(), 0.01);


    }

    @Test
    public void testMultiply() {
        var lhs = $(4.1);
        var rhs = $(5.3);
        assertEquals(21, (long) lhs.$multiply(rhs).I());
        assertEquals(21, (long) rhs.$multiply(lhs).I());
        assertEquals(4.1 * 5.3, rhs.$multiply(lhs).D(), 0.01);
        assertEquals(4.1 * 5.3, lhs.$multiply(rhs).D(), 0.01);

        var intRhs = $(2);
        var intLhs = $(3);

        assertEquals(15, (long) intLhs.$multiply(rhs).I());
        assertEquals(15, (long) rhs.$multiply(intLhs).I());
        assertEquals(6, (long) intLhs.$multiply(intRhs).I());

        assertEquals(15.9, rhs.$multiply(intLhs).D(), 0.01);
        assertEquals(15.9, intLhs.$multiply(rhs).D(), 0.01);
        assertEquals(6, intLhs.$multiply(intRhs).D(), 0.01);
    }

}