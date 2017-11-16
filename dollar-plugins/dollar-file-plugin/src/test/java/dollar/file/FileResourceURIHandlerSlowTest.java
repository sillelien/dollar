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

package dollar.file;

import dollar.api.Value;
import dollar.api.uri.URI;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;

import static dollar.api.DollarStatic.*;
import static org.junit.Assert.assertEquals;


class FileResourceURIHandlerSlowTest {
    @Test
    void all() {
        System.out.println(createRecord());
        FileResourceURIHandler handler = new FileResourceURIHandler("file+resource",
                                                                    URI.of("file+resource:/test_resource.json.txt"));
        Value read = handler.all();
        System.out.println(read);
    }

    @NotNull Value createRecord() {
        return $("name", "Neil")
                       .$("progYears", $range(1981, 2014))
                       .$("blog", "http://neilellis.me")
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
    void read() {
        FileResourceURIHandler handler = new FileResourceURIHandler("file+resource",
                                                                    URI.of("file+resource:/test_resource_read.json.txt"));
        Value read = handler.read(true, false);
        assertEquals("", "Neil", read.$get($("name")).toString());
        System.out.println(read);
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    void size() {
        FileResourceURIHandler handler = new FileResourceURIHandler("file+resource",
                                                                    URI.of("file+resource:/test_resource_size.json.txt"));
        assertEquals("", 8, handler.size());
    }


    @AfterEach
    void tearDown() {
    }


}
