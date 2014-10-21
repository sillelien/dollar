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

public class DollarRangeTest {
    private static var range;

    @BeforeClass
    public static void setUp() {
        range = $(1, 4);
    }

    @Test
    public void testBasics() {
        assertEquals(range, $list(1).$append(2).$append(3).$append(4));
        assertEquals(range, $(1, 4));
    }


}