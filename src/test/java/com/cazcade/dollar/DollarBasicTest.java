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

package com.cazcade.dollar;

import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.cazcade.dollar.DollarStatic.$;
import static com.cazcade.dollar.DollarStatic.$array;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DollarBasicTest {
    @Test
    public void testBuild() {
        $ profile = $("name", "Neil")
                .$("age", new Date().getYear() + 1900 - 1970)
                .$("gender", "male")
                .$("projects", $array("snapito", "dollar_vertx"))
                .$("location",
                        $("city", "brighton")
                                .$("postcode", "bn1 6jj")
                                .$("number", 343)
                );
        assertEquals("{\"name\":\"Neil\",\"age\":44,\"gender\":\"male\",\"projects\":[\"snapito\",\"dollar_vertx\"],\"location\":{\"city\":\"brighton\",\"postcode\":\"bn1 6jj\",\"number\":343}}", profile.toString());
    }

    @Test
    public void testMapCreation() {
        Map map = new HashMap();
        map.put("foo", "bar");
        Map submap = new HashMap();
        submap.put("thing", 1);
        map.put("sub", submap);
        assertEquals("bar", $(map).$("foo").val());
        assertEquals("bar", $(map).$map().get("foo"));
        assertEquals(1, $(map).$("sub").$map().get("thing"));
        assertEquals("1", $(map).$("sub").$("thing").$$());
        assertEquals("{\"thing\":1}", $(map).$("sub").$$());
        assertEquals(1, $(map).$("sub").$int("thing").longValue());
    }

    @Test
    public void testSplit() {
        assertEquals("{}", $("{\"foo\":\"bar\",\"thing\":1}").split().toString());
        assertEquals("{\"foo\":\"bar\"}", $("{\"a\":{\"foo\":\"bar\"},\"thing\":1}").split().get("a").toString());
        assertEquals(2, $("{\"foo\":\"bar\",\"thing\":1}").splitValues().stream().count());
    }

    @Test
    public void testStringCreation() {
        assertEquals("bar", new DollarJson("{\"foo\":\"bar\"}").$("foo").val());
        assertEquals("bar", new DollarJson("{\"foo\":\"bar\"}").$().getString("foo"));
    }

    @Test
    public void testNull() {
        assertEquals("bar", $((Object)null).$("foo","bar").$("foo").val());
        assertTrue($((Object) null).isNull());
        assertNull($((Object) null).val());
        assertTrue(!$((Object) null).$("foo", "bar").$("foo").isNull());
    }


}