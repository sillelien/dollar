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
import dollar.api.json.ImmutableJsonObject;
import dollar.api.var;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static dollar.api.DollarStatic.$;
import static org.junit.Assert.*;

public class DollarBasicTest {
    @Test
    public void testBuild() {
        var profile = DollarStatic.$("name", "Neil")
                              .$("age", 44)
                              .$("gender", "male")
                              .$("projects", DollarStatic.$jsonArray("snapito", "dollar_vertx"))
                              .$("location",
                                 DollarStatic.$("city", "brighton")
                                         .$("postcode", "bn1 6jj")
                                         .$("number", 343)
                              );
        assertEquals(
                "{\"name\":\"Neil\",\"age\":44,\"gender\":\"male\",\"projects\":[\"snapito\",\"dollar_vertx\"],\"location\":{\"city\":\"brighton\",\"postcode\":\"bn1 6jj\",\"number\":343}}",
                profile.toString());
    }


    @Test
    public void testBuildAlt() {
        var profile = DollarStatic.$(
                DollarStatic.$("name", "Neil"),
                DollarStatic.$("age", 44),
                DollarStatic.$("gender", "male"),
                $("projects", DollarStatic.$jsonArray("snapito", "dollar_vertx")),
                DollarStatic.$("location",
                               DollarStatic.$("city", "brighton"),
                               DollarStatic.$("postcode", "bn1 6jj"),
                               DollarStatic.$("number", 343)
                ));
        assertEquals(
                "{\"name\":\"Neil\",\"age\":44,\"gender\":\"male\",\"projects\":[\"snapito\",\"dollar_vertx\"],\"location\":{\"city\":\"brighton\",\"postcode\":\"bn1 6jj\",\"number\":343}}",
                profile.toString());
    }


    @Test
    public void testEquality() {
        assertNotEquals(DollarStatic.$((String) null), DollarStatic.$("null"));
        assertNotEquals(DollarStatic.$("null"), DollarStatic.$((String) null));
        assertNotEquals(DollarStatic.$(1), DollarStatic.$((String) null));
        assertNotEquals(DollarStatic.$((Number) null), DollarStatic.$(1));
        assertNotEquals(DollarStatic.$(), DollarStatic.$((String) null));
        assertNotEquals(DollarStatic.$((String) null), DollarStatic.$());
        assertNotEquals(DollarStatic.$(""), DollarStatic.$());
        assertNotEquals(DollarStatic.$(), DollarStatic.$(""));
        assertNotEquals(DollarStatic.$(), DollarStatic.$(0));
        assertNotEquals(DollarStatic.$(0), DollarStatic.$());

        assertEquals(DollarStatic.$(), DollarStatic.$());
        assertEquals(DollarStatic.$("1"), DollarStatic.$(1));
        assertEquals(DollarStatic.$(1), DollarStatic.$("1"));
        assertEquals(DollarStatic.$((String) null), DollarStatic.$((String) null));
    }

    @Test
    public void testLambda() {
        var profile = DollarStatic.$("name", "Neil")
                              .$("age", 1)
                              .$("gender", "male")
                              .$("projects", DollarStatic.$jsonArray("snapito", "dollar_vertx"))
                              .$("location",
                                 DollarStatic.$("city", "brighton")
                                         .$("postcode", "bn1 6jj")
                                         .$("number", 343)
                              );
//        var result = profile.$pipe(v -> v[0].$("weight", "none of your business"));
//        String weight = result.$("weight").toHumanString();
//        assertEquals(weight, "none of your business");
//        assertTrue("Profile's state was mutated!!!", profile.$("weight").isVoid());
    }

    @Test
    public void testMapCreation() {
        Map map = new HashMap();
        map.put("foo", "bar");
        Map submap = new HashMap();
        submap.put("thing", 1);
        map.put("sub", submap);
        assertEquals("bar", DollarStatic.$(map).$get($("foo")).toJavaObject());
        assertEquals("bar", DollarStatic.$(map).toJavaMap().get("foo"));
        assertEquals(1L, DollarStatic.$(map).$get($("sub")).toJavaMap().get("thing"));
        assertEquals("1", DollarStatic.$(map).$get($("sub")).$get($("thing")).toHumanString());
        assertEquals("{\"thing\":1}", DollarStatic.$(map).$get($("sub")).toHumanString());
        assertEquals(1, DollarStatic.$(map).$get($("sub")).$get($("thing")).toInteger().longValue());
    }


    @Test
    public void testNull() {
        assertNull(DollarStatic.$((Object) null).$("foo", "bar").$get($("foo")).toJavaObject());
        assertTrue(DollarStatic.$((Object) null).isVoid());
        assertTrue(DollarStatic.$((Object) null).$get($("bar")).isVoid());
        assertNull(DollarStatic.$((Object) null).toJavaObject());
        assertFalse(DollarStatic.$((Object) null).$get($("bar")).$has("foo").isTrue());
        assertFalse(DollarStatic.$((Object) null).$has("foo").isTrue());
        assertTrue(DollarStatic.$((Object) null).$("foo", "bar").$get($("foo")).isVoid());
        assertEquals("twasnull", DollarStatic.$((Object) null).$default($(i -> DollarStatic.$("twasnull"))).toHumanString());
    }


    @Test
    public void testStringCreation() {
        final ImmutableJsonObject jsonObject = DollarStatic.$("{\"foo\":\"bar\"}").toJsonObject();
        System.out.println(jsonObject);
        assertEquals("bar", DollarStatic.$("{\"foo\":\"bar\"}").$get($("foo")).toJsonType());
        final String foo = jsonObject.getString("foo");
        assertEquals("bar", foo);
    }

}
