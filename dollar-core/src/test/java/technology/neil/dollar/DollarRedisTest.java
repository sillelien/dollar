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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static technology.neil.dollar.DollarStatic.*;
import static org.junit.Assert.assertTrue;

public class DollarRedisTest {

    private static var profile;

    @BeforeClass
    public static void setUp() {
        profile = $("name", "Neil")
                .$("age", new Date().getYear() + 1900 - 1970)
                .$("gender", "male")
                .$("projects", $jsonArray("snapito", "dollar_vertx"))
                .$("location",
                        $("city", "brighton")
                                .$("postcode", "bn1 6jj")
                                .$("number", 343)
                );
    }


    @Test
    public void testPushPop() {
        profile.push("test.profile");
        var deser = profile.pop("test.profile", 10 * 1000);
        Assert.assertEquals(deser.$$(), profile.$$());
    }

    @Test
    public void testSet() throws InterruptedException {
        assertTrue(profile.save("test.profile.set").equals(profile));
        var deser = profile.load("test.profile.set");
        Assert.assertEquals(deser.$$(), profile.$$());
        Thread.sleep(2000);
        $dump();
    }


}