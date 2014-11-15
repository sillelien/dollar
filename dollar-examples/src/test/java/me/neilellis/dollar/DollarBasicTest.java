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
        assertEquals(
                "{\"name\":\"Neil\",\"age\":44,\"gender\":\"male\",\"projects\":[\"snapito\",\"dollar_vertx\"],\"location\":{\"city\":\"brighton\",\"postcode\":\"bn1 6jj\",\"number\":343}}",
                profile.toString());
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
        assertEquals(
                "{\"name\":\"Neil\",\"age\":44,\"gender\":\"male\",\"projects\":[\"snapito\",\"dollar_vertx\"],\"location\":{\"city\":\"brighton\",\"postcode\":\"bn1 6jj\",\"number\":343}}",
                profile.toString());
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
        String weight = result.$get("weight").S();
        assertEquals(weight, "none of your business");
        assertTrue("Profile's state was mutated!!!", profile.$get("weight").isVoid());
    }

    @Test
    public void testMapCreation() {
        Map map = new HashMap();
        map.put("foo", "bar");
        Map submap = new HashMap();
        submap.put("thing", 1);
        map.put("sub", submap);
        assertEquals("bar", $(map).$get("foo").val());
        assertEquals("bar", $(map).toMap().get("foo"));
        assertEquals(1, $(map).$get("sub").toMap().get("thing"));
        assertEquals("1", $(map).$get("sub").$get("thing").S());
        assertEquals("{\"thing\":1}", $(map).$get("sub").S());
        assertEquals(1, $(map).$get("sub").I("thing").longValue());
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
        var ageRetrieved = profile.$eval("$['age']");
        System.out.println(profile.$eval("$"));
        System.out.println(ageRetrieved);
        assertEquals(age, (int) ageRetrieved.I());
        assertEquals(age / 11, (int) profile.$eval("($['age'] / 11)").I());
        assertEquals("male", profile.$eval("$.gender").$());
        assertEquals(10, (int) profile.$eval("5*2").I());
        assertEquals(10, (int) $eval("10").I());
        assertEquals($("{\"name\":\"Dave\"}").$get("name").S(), "Dave");
        assertEquals($().$eval("({name:'Dave'})").$get("name").S(), "Dave");
    }

    @Test
    public void testNull() {
        assertNull($((Object) null).$("foo", "bar").$get("foo").val());
        assertTrue($((Object) null).isVoid());
        assertTrue($((Object) null).$get("bar").isVoid());
        assertNull($((Object) null).val());
        assertFalse($((Object) null).$get("bar").$has("foo"));
        assertFalse($((Object) null).$has("foo"));
        assertTrue($((Object) null).$("foo", "bar").$get("foo").isVoid());
        assertEquals("twasnull", $((Object) null).$void(() -> $("twasnull")).S());
    }


    @Test
    public void testStringCreation() {
        System.out.println($("{\"foo\":\"bar\"}"));
        assertEquals("bar", $("{\"foo\":\"bar\"}").$get("foo").val());
        assertEquals("bar", $("{\"foo\":\"bar\"}").json().getString("foo"));
    }

}