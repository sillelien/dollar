/*
 * Copyright (c) 2014-2014 Cazcade Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package technology.neil.dollar;

import org.junit.BeforeClass;
import org.junit.Test;
import org.vertx.java.core.json.JsonArray;

import static technology.neil.dollar.DollarStatic.$list;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class DollarArrayTest {
    private static var list;

    @BeforeClass
    public static void setUp() {
        list = $list("Neil", "Dimple", "Charlie");
    }

    @Test
    public void testBasics() {
        assertEquals(list, $list("Neil").add("Dimple").add("Charlie"));
        assertEquals(list.remove("Neil"), $list("Dimple").add("Charlie"));
        assertEquals(list.remove("Dimple"), $list("Neil").add("Charlie"));
        assertEquals(list.remove("Charlie"), $list("Neil").add("Dimple"));
        Object value = list.$();
        assertEquals(JsonArray.class, value.getClass());
        assertEquals("[\"Neil\",\"Dimple\",\"Charlie\"]", list.toString());
        assertEquals("[\"Neil\",\"Dimple\",\"Charlie\"]", value.toString());
        assertTrue(list.stream().anyMatch((i) -> i.equals("Neil")));
    }


}