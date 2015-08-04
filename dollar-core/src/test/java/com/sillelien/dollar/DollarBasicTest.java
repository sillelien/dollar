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

import com.sillelien.dollar.api.json.ImmutableJsonObject;
import com.sillelien.dollar.api.var;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.sillelien.dollar.api.DollarStatic.*;
import static org.junit.Assert.*;

public class DollarBasicTest {
    @Test
    public void testBuild() {
        var profile = $("name", "Neil")
                .$("age", 44)
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
                $("age", 44),
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
        var result = profile.$pipe(v -> v[0].$("weight", "none of your business"));
        String weight = result.$("weight").toHumanString();
        assertEquals(weight, "none of your business");
        assertTrue("Profile's state was mutated!!!", profile.$("weight").isVoid());
    }

    @Test
    public void testMapCreation() {
        Map map = new HashMap();
        map.put("foo", "bar");
        Map submap = new HashMap();
        submap.put("thing", 1);
        map.put("sub", submap);
        assertEquals("bar", $(map).$("foo").toJavaObject());
        assertEquals("bar", $(map).toMap().get("foo"));
        assertEquals(1L, $(map).$("sub").toMap().get("thing"));
        assertEquals("1", $(map).$("sub").$("thing").toHumanString());
        assertEquals("{\"thing\":1}", $(map).$("sub").toHumanString());
        assertEquals(1, $(map).$("sub").$("thing").toInteger().longValue());
    }

    @Test
    public void testNashorn() {
        int age = 44;
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
        assertEquals(age, (int) ageRetrieved.toInteger());
        assertEquals(age / 11, (int) profile.$eval("($['age'] / 11)").toInteger());
        assertEquals("male", profile.$eval("$.gender").toJavaObject());
        assertEquals(10, (int) profile.$eval("5*2").toInteger());
        assertEquals(10, (int) $eval("10").toInteger());
        assertEquals($("{\"name\":\"Dave\"}").$("name").toHumanString(), "Dave");
        assertEquals($().$eval("({name:'Dave'})").$("name").toHumanString(), "Dave");
    }

    @Test
    public void testNull() {
        assertNull($((Object) null).$("foo", "bar").$("foo").toJavaObject());
        assertTrue($((Object) null).isVoid());
        assertTrue($((Object) null).$("bar").isVoid());
        assertNull($((Object) null).toJavaObject());
        assertFalse($((Object) null).$("bar").$has("foo").isTrue());
        assertFalse($((Object) null).$has("foo").isTrue());
        assertTrue($((Object) null).$("foo", "bar").$("foo").isVoid());
        assertEquals("twasnull", $((Object) null).$default($(i -> $("twasnull"))).toHumanString());
    }


    @Test
    public void testStringCreation() {
        final ImmutableJsonObject jsonObject = $("{\"foo\":\"bar\"}").toJsonObject();
        System.out.println(jsonObject);
        assertEquals("bar", $("{\"foo\":\"bar\"}").$("foo").toJsonType());
        final String foo = jsonObject.getString("foo");
        assertEquals("bar", foo);
    }

}