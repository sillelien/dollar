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

import com.jayway.restassured.RestAssured;
import dollar.api.Type;
import dollar.api.types.DollarFactory;
import dollar.api.var;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Date;

import static dollar.api.DollarStatic.*;
import static org.junit.Assert.assertEquals;

public class DollarSerializationTest {

    private static var profile;

    static {
        RestAssured.port = 4567;
    }

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
        System.out.println(profile.$get($("progYears")));
        assertEquals(profile, deserialized);
        assertEquals(profile.$get($("progYears")), deserialized.$get($("progYears")));
        assertEquals(profile.$get($("blog")).$type(), Type._URI);
        assertEquals(profile.$get($("timestamp")).$type(), Type._DATE);
        assertEquals(profile.$get($("wroteDollar")).$type(), Type._BOOLEAN);
        assertEquals(profile.$get($("rating")).$type(), Type._DECIMAL);
        assertEquals(profile.$get($("projects")).$type(), Type._LIST);
        assertEquals(profile.$get($("empty")).$type(), Type._VOID);

    }

}
