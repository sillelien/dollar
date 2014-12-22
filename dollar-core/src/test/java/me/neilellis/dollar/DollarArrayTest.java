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

import me.neilellis.dollar.json.JsonArray;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static me.neilellis.dollar.DollarStatic.$;
import static me.neilellis.dollar.DollarStatic.$list;
import static org.junit.Assert.assertEquals;

public class DollarArrayTest {
    private static var list;

    @BeforeClass
    public static void setUp() {
        list = $list("Neil", "Dimple", "Charlie");
    }

    @Test
    public void testBasics() {
        assertEquals(list, $list("Neil").$plus($("Dimple")).$plus($("Charlie")));
        assertEquals(list.remove("Neil"), $list("Dimple").$plus($("Charlie")));
        assertEquals(list.remove("Dimple"), $list("Neil").$plus($("Charlie")));
        assertEquals(list.remove("Charlie"), $list("Neil").$plus($("Dimple")));
        Object value = list.toJavaObject();
        assertEquals(JsonArray.class, value.getClass());
        assertEquals("[ \"Neil\", \"Dimple\", \"Charlie\" ]", list.toString());
        assertEquals("[ \"Neil\", \"Dimple\", \"Charlie\" ]", value.toString());
        assertTrue(list.$stream(false).anyMatch((i) -> i.equals("Neil")));
    }


}