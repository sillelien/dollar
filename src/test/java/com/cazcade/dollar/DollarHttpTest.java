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

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;

import static com.cazcade.dollar.DollarStatic.$;
import static com.cazcade.dollar.DollarStatic.*;

public class DollarHttpTest {

    private static $ profile;

    @BeforeClass
    public static void setUp() {
        profile = $("name", "Neil")
                .$("age", new Date().getYear() + 1900 - 1970)
                .$("gender", "male")
                .$("projects", $array("snapito", "dollar"))
                .$("location",
                        $("city", "brighton")
                                .$("postcode", "bn1 6jj")
                                .$("number", 343)
                );
    }


    @Test
    public void testBasic() throws InterruptedException {
        GET("/", (context) -> $("Hello World"));
        GET("/headers", (context) -> context.headers());
        GET("/profile", (context) -> profile);
        //TODO: Client tests here
        Thread.sleep(1000);
    }


}