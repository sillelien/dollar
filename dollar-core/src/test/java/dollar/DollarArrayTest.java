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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DollarArrayTest {
    private static Value list;

    @BeforeAll
    public static void setUp() {
        list = DollarStatic.$list("Neil", "Dimple", "Charlie");
    }

    @Test
    public void testBasics() {
        assertEquals(list, DollarStatic.$list("Neil").$plus(DollarStatic.$("Dimple")).$plus(DollarStatic.$("Charlie")));
        assertEquals(list.$remove(DollarStatic.$("Neil")), DollarStatic.$list("Dimple").$plus(DollarStatic.$("Charlie")));
        assertEquals(list.$remove(DollarStatic.$("Dimple")), DollarStatic.$list("Neil").$plus(DollarStatic.$("Charlie")));
        assertEquals(list.$remove(DollarStatic.$("Charlie")), DollarStatic.$list("Neil").$plus(DollarStatic.$("Dimple")));
        Object value = list.toJavaObject();
        assertTrue(value instanceof List);
        assertEquals("[ \"Neil\", \"Dimple\", \"Charlie\" ]", list.toString());
        assertEquals("[Neil, Dimple, Charlie]", value.toString());
        assertTrue(list.stream(false).anyMatch((i) -> i.equalsString("Neil")));
    }


}
