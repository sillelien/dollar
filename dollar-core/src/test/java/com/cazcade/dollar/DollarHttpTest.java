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

import com.jayway.restassured.RestAssured;
import org.hamcrest.core.Is;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static com.cazcade.dollar.DollarStatic.*;
import static com.jayway.restassured.RestAssured.given;

public class DollarHttpTest {

    static {
        RestAssured.port = 4567;
    }

    private static var profile;

    @BeforeClass
    public static void setUp() {
        profile = $("name", "Neil")
                .$("age", new Date().getYear() + 1900 - 1970)
                .$("gender", "male")
                .$("projects", $jsonArray("snapito", "dollar"))
                .$("location",
                        $("city", "brighton")
                                .$("postcode", "bn1 6jj")
                                .$("number", 343)
                );
    }


    @Test
    public void testBasic() throws InterruptedException {
        $GET("/", (context) -> $("Hello World"));
        $GET("/headers", (context) -> context.headers());
        $GET("/profile", (context) -> profile);
        Thread.sleep(1000);
        given().port(4567).get("/profile").then().assertThat().body("location.number", Is.is(343));
        stopHttpServer();
    }


}