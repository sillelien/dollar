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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static dollar.api.DollarStatic.*;
import static org.junit.Assert.assertEquals;


class FileURIHandlerSlowTest {
    @Test
    void all() {
        FileURIHandler handler = new FileURIHandler("file", URI.of("file:///tmp/test.json"));
        handler.writeAll(createRecord());
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
    void drain() {
        FileURIHandler handler = new FileURIHandler("file", URI.of("file:///tmp/test_drain.json.txt"));
        handler.drain();
        handler.write(createRecord(), false, true);
        handler.write(createRecord(), false, true);
        Value read = handler.drain();
        assertEquals("Wrong list size back from drain,", 2, read.size());
        System.out.println(read);
    }


    @Test
    void read() {
        FileURIHandler handler = new FileURIHandler("file", URI.of("file:///tmp/test_read.json.txt"));
        handler.drain();
        handler.write(createRecord(), false, true);
        Value read = handler.read(true, false);
        assertEquals("", "Neil", read.$get($("name")).toString());
        System.out.println(read);
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    void size() {
        FileURIHandler handler = new FileURIHandler("file", URI.of("file:///tmp/test_size.json.txt"));
        handler.drain();
        handler.write(createRecord(), false, true);
        handler.write(createRecord(), false, true);
        handler.write(createRecord(), false, true);
        assertEquals("", 3, handler.size());
    }


    @Test
    void subscribe() throws IOException, InterruptedException {
        FileURIHandler handler = new FileURIHandler("file", URI.of("file:/tmp/test_subscribe.json.txt"));
        handler.init();
        handler.start();
        AtomicInteger count = new AtomicInteger();
        handler.subscribe(i -> {
            System.out.println(i[0]);
            count.incrementAndGet();
            assertEquals("", "world", i[0].$get($("hello")).toString());
            return $void();
        }, UUID.randomUUID().toString());
        Thread.sleep(100);
        Files.write(new File("/tmp/test_subscribe.json.txt").toPath(), "{\"hello\":\"world\"}\n".getBytes());
        System.out.println("Wrote");
        Thread.sleep(4 * 1000);
        handler.stop();
        handler.destroy();
        assertEquals("", 1, count.get());
    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void unsubscribe() throws IOException, InterruptedException {
        FileURIHandler handler = new FileURIHandler("file", URI.of("file:/tmp/test_unsubscribe.json.txt"));
        handler.init();
        handler.start();
        AtomicInteger count = new AtomicInteger();
        String key = UUID.randomUUID().toString();
        handler.subscribe(i -> {
            System.err.println("subscription called");
            count.incrementAndGet();
            return $void();
        }, key);
        Thread.sleep(100);
        Files.write(new File("/tmp/test_unsubscribe.json.txt").toPath(), "{\"hello\":\"world\"}\n".getBytes());
        Thread.sleep(2000);
        Files.write(new File("/tmp/test_unsubscribe.json.txt").toPath(), "{\"hello\":\"world2\"}\n".getBytes());
        Thread.sleep(2000);
        handler.unsubscribe(key);
        Files.write(new File("/tmp/test_unsubscribe.json.txt").toPath(), "{\"hello\":\"world3\"}\n".getBytes());
        System.out.println("Wrote");
        Thread.sleep(2000);
        handler.stop();
        handler.destroy();
        assertEquals("", 2, count.get());
    }

    @Test
    void write() {
    }

    @Test
    void writeAll() {
    }

}
