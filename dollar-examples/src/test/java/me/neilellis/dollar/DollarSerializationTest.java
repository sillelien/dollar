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

import com.jayway.restassured.RestAssured;
import me.neilellis.dollar.types.DollarFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static me.neilellis.dollar.DollarStatic.*;

public class DollarSerializationTest {

    static {
        RestAssured.port = 4567;
    }

    private static var profile;

    @BeforeClass
    public static void setUp() {
        profile = $("name", "Neil")
                .$("progYears", $range(1981, 2014))
                .$("blog", $uri("http://neilellis.me"))
                .$("age", new Date().getYear() + 1900 - 1970)
                .$("timestamp", LocalDateTime.now())
                .$("wroteDollar", $(true))
                .$("empty", $void())
                .$("rating", 0.7)
                .$("gender", "male")
                .$("projects", $list("snapito", "dollar"))
                .$("location",
                   $("city", "brighton")
                           .$("postcode", "bn1 6jj")
                           .$("number", 343)
                );
    }


    @Test
    public void test() {
        final String serialized = DollarFactory.serialize(profile);
        System.out.println(serialized);
        final Type type = profile.$type();
        System.out.println(type);
        final var deserialized = DollarFactory.deserialize(serialized);
        System.out.println(deserialized);
        System.out.println(profile.$("progYears"));
        assertEquals(profile, deserialized);
        assertEquals(profile.$("progYears"), deserialized.$("progYears"));
        assertEquals(profile.$("blog").$type(), Type.URI);
        assertEquals(profile.$("timestamp").$type(), Type.DATE);
        assertEquals(profile.$("wroteDollar").$type(), Type.BOOLEAN);
        assertEquals(profile.$("rating").$type(), Type.DECIMAL);
        assertEquals(profile.$("projects").$type(), Type.LIST);
        assertEquals(profile.$("empty").$type(), Type.VOID);

    }

}