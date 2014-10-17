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

package me.neilellis.dollar;

import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static me.neilellis.dollar.DollarStatic.*;
import static org.junit.Assert.*;

public class DollarBasicTest {
    @Test
    public void testBuild() {
        var profile = $("name", "Neil")
                .$("age", new Date().getYear() + 1900 - 1970)
                .$("gender", "male")
                .$("projects", $jsonArray("snapito", "dollar_vertx"))
                .$("location",
                        $("city", "brighton")
                                .$("postcode", "bn1 6jj")
                                .$("number", 343)
                );
        assertEquals("{\"name\":\"Neil\",\"age\":44,\"gender\":\"male\",\"projects\":[\"snapito\",\"dollar_vertx\"],\"location\":{\"city\":\"brighton\",\"postcode\":\"bn1 6jj\",\"number\":343}}", profile.toString());
    }


    @Test
    public void testBuildAlt() {
        var profile = $(
                $("name", "Neil"),
                $("age", new Date().getYear() + 1900 - 1970),
                $("gender", "male"),
                $("projects", $jsonArray("snapito", "dollar_vertx")),
                $("location",
                        $("city", "brighton"),
                        $("postcode", "bn1 6jj"),
                        $("number", 343)
                ));
        assertEquals("{\"name\":\"Neil\",\"age\":44,\"gender\":\"male\",\"projects\":[\"snapito\",\"dollar_vertx\"],\"location\":{\"city\":\"brighton\",\"postcode\":\"bn1 6jj\",\"number\":343}}", profile.toString());
    }


    @Test
    public void testEquality() {
        assertNotEquals($((String) null), $("null"));
        assertNotEquals($("null"), $((String) null));
        assertNotEquals($(1), $((String) null));
        assertNotEquals($((Number) null), $(1));
        assertNotEquals($(), $((String) null));
        assertNotEquals($((String) null), $());
        assertNotEquals($(""), $());
        assertNotEquals($(), $(""));
        assertNotEquals($(), $(0));
        assertNotEquals($(0), $());

        assertEquals($(), $());
        assertEquals($("1"), $(1));
        assertEquals($(1), $("1"));
        assertEquals($((String) null), $((String) null));
    }

    @Test
    public void testLambda() {
        var profile = $("name", "Neil")
                .$("age", 1)
                .$("gender", "male")
                .$("projects", $jsonArray("snapito", "dollar_vertx"))
                .$("location",
                        $("city", "brighton")
                                .$("postcode", "bn1 6jj")
                                .$("number", 343)
                );
        var result = profile.eval("getWeight", (var value) -> value.$("weight", "none of your business"));
        String weight = result.$("weight").$$();
        assertEquals(weight, "none of your business");
        assertTrue("Profile's state was mutated!!!", profile.$("weight").isNull());
    }

    @Test
    public void testMapCreation() {
        Map map = new HashMap();
        map.put("foo", "bar");
        Map submap = new HashMap();
        submap.put("thing", 1);
        map.put("sub", submap);
        assertEquals("bar", $(map).$("foo").$val());
        assertEquals("bar", $(map).$map().get("foo"));
        assertEquals(1, $(map).$("sub").$map().get("thing"));
        assertEquals("1", $(map).$("sub").$("thing").$$());
        assertEquals("{\"thing\":1}", $(map).$("sub").$$());
        assertEquals(1, $(map).$("sub").$int("thing").longValue());
    }

    @Test
    public void testNashorn() {
        int age = new Date().getYear() + 1900 - 1970;
        var profile = $("name", "Neil")
                .$("age", age)
                .$("gender", "male")
                .$("projects", $jsonArray("snapito", "dollar_vertx"))
                .$("location",
                        $("city", "brighton")
                                .$("postcode", "bn1 6jj")
                                .$("number", 343)
                );
        assertEquals(age, (int) profile.$("$['age']").$int());
        assertEquals(age / 11, (int) profile.$("($['age'].$int() / 11)").$int());
        assertEquals("male", profile.$("$.gender").$());
        assertEquals(10, (int) profile.$("5*2").$int());
        assertEquals(10, (int) $eval("10").$int());
        assertEquals($("{\"name\":\"Dave\"}").$("name").$$(), "Dave");
        assertEquals($().$("({name:'Dave'})").$("name").$$(), "Dave");
    }

    @Test
    public void testNull() {
        assertNull($((Object) null).$("foo", "bar").$("foo").$val());
        assertTrue($((Object) null).isNull());
        assertTrue($((Object) null).$("bar").isNull());
        assertNull($((Object) null).$val());
        assertFalse($((Object) null).$("bar").has("foo"));
        assertFalse($((Object) null).has("foo"));
        assertTrue($((Object) null).$("foo", "bar").$("foo").isNull());
        assertEquals("twasnull", $((Object) null).ifNull(() -> $("twasnull")).$$());
    }

    @Test
    public void testSplit() {
        assertEquals("{}", $("{\"foo\":\"bar\",\"thing\":1}").split().toString());
        assertEquals("{\"foo\":\"bar\"}", $("{\"a\":{\"foo\":\"bar\"},\"thing\":1}").split().get("a").toString());
        assertEquals(2, $("{\"foo\":\"bar\",\"thing\":1}").$keys().count());
    }

    @Test
    public void testStringCreation() {
        assertEquals("bar", $("{\"foo\":\"bar\"}").$("foo").$val());
        assertEquals("bar", $("{\"foo\":\"bar\"}").$json().getString("foo"));
    }


}