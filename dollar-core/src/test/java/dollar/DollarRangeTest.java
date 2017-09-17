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
import dollar.api.Value;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

public class DollarRangeTest {
    private static Value range;

    @BeforeAll
    public static void setUp() {
        range = DollarStatic.$(DollarStatic.$(1), DollarStatic.$(4));
    }

    @Test
    public void testBasics() {
        System.err.println(range.toVarList());
        final Value actual = DollarStatic.$list(1).$plus(DollarStatic.$(2)).$plus(DollarStatic.$(3)).$plus(DollarStatic.$(4));
        assertFalse(range.equals(actual));
        assertFalse(actual.equals(range));
        System.err.println(actual.$unwrap().getClass());
        assertNotEquals(range, actual);
        Assert.assertEquals(range, DollarStatic.$(DollarStatic.$(1), DollarStatic.$(4)));
    }


}
