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

import com.sillelien.dollar.api.var;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.sillelien.dollar.api.DollarStatic.$;
import static org.junit.Assert.assertEquals;

public class DollarNumberTest {
    private static var list;

    @BeforeClass
    public static void setUp() {

    }

    @Test
    public void testBasics() {
        var map = $("foo", 1).$("bar", 10);
        assertEquals(2, (long) $(1).$inc().toInteger());
        assertEquals(4, (long) $(1).$plus(3).toInteger());
        assertEquals(10, (long) map.$("bar").toInteger());
        assertEquals(1, (long) map.$("foo").toInteger());
        assertEquals(0, (long) map.$("foobar").toInteger());

    }

    @Test
    public void testDivide() {

        var lhs = $(40.1);
        var rhs = $(5.3);
        assertEquals(7, (long) lhs.$divide(rhs).toInteger());
        assertEquals(0, (long) rhs.$divide(lhs).toInteger());

        assertEquals(5.3 / 40.1, rhs.$divide(lhs).toDouble(), 0.01);
        assertEquals(40.1 / 5.3, lhs.$divide(rhs).toDouble(), 0.01);

        var intLhs = $(30);
        var intRhs = $(2);

        assertEquals(5, (long) intLhs.$divide(rhs).toInteger());
        assertEquals(0, (long) rhs.$divide(intLhs).toInteger());
        assertEquals(15, (long) intLhs.$divide(intRhs).toInteger());

        assertEquals(0.177, rhs.$divide(intLhs).toDouble(), 0.01);
        assertEquals(5.660377358490567, intLhs.$divide(rhs).toDouble(), 0.01);
        assertEquals(15.0, intLhs.$divide(intRhs).toDouble(), 0.01);

    }

    @Test
    public void testMod() {
        var lhs = $(40.1);
        var rhs = $(5.3);
        assertEquals(3, (long) lhs.$modulus(rhs).toInteger());
        assertEquals(5, (long) rhs.$modulus(lhs).toInteger());

        assertEquals(5.3 % 40.1, rhs.$modulus(lhs).toDouble(), 0.01);
        assertEquals(40.1 % 5.3, lhs.$modulus(rhs).toDouble(), 0.01);

        var intLhs = $(30);
        var intRhs = $(2);

        assertEquals(3, (long) intLhs.$modulus(rhs).toLong());
        assertEquals(5, (long) rhs.$modulus(intLhs).toLong());
        assertEquals(0, (long) intLhs.$modulus(intRhs).toInteger());

        assertEquals(5.3, rhs.$modulus(intLhs).toDouble(), 0.01);
        assertEquals(3.5, intLhs.$modulus(rhs).toDouble(), 0.01);
        assertEquals(0.0, intLhs.$modulus(intRhs).toDouble(), 0.01);


    }

    @Test
    public void testMultiply() {
        var lhs = $(4.1);
        var rhs = $(5.3);
        assertEquals(21, (long) lhs.$multiply(rhs).toInteger());
        assertEquals(21, (long) rhs.$multiply(lhs).toInteger());
        assertEquals(4.1 * 5.3, rhs.$multiply(lhs).toDouble(), 0.01);
        assertEquals(4.1 * 5.3, lhs.$multiply(rhs).toDouble(), 0.01);

        var intRhs = $(2);
        var intLhs = $(3);

        assertEquals(15, (long) intLhs.$multiply(rhs).toInteger());
        assertEquals(15, (long) rhs.$multiply(intLhs).toInteger());
        assertEquals(6, (long) intLhs.$multiply(intRhs).toInteger());

        assertEquals(15.9, rhs.$multiply(intLhs).toDouble(), 0.01);
        assertEquals(15.9, intLhs.$multiply(rhs).toDouble(), 0.01);
        assertEquals(6, intLhs.$multiply(intRhs).toDouble(), 0.01);
    }

}