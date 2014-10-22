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

package me.neilellis.dollar.integration.camel;

import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.var;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CamelIntegrationProviderTest {


    @Test
    public void testDispatch() throws Exception {

    }

    @Test
    public void testListen() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final CamelIntegrationProvider camelIntegrationProvider = new CamelIntegrationProvider();
        camelIntegrationProvider.listen("vm://testListen", (v) -> {
            try {
                System.out.println("***: " + v);
                assertEquals("Listen Test", v.$S());
                countDownLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        camelIntegrationProvider.start();
        camelIntegrationProvider.dispatch("vm://testListen", DollarStatic.$("Listen Test"));
        countDownLatch.await();
        camelIntegrationProvider.stop();
    }

    @Test
    public void testPublish() throws Exception {

    }

    @Test
    public void testPoll() throws Exception {
        final var page = new CamelIntegrationProvider().poll("http://google.com");
        assertTrue(page.$S().contains("html"));
    }

    @Test
    public void testSend() throws Exception {
        CamelContext context = new DefaultCamelContext();
        RouteBuilder routeBuilder = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct-vm:test").process((e) -> {
                    e.getOut().setBody("RESULT");
                });
            }
        };
        context.addRoutes(routeBuilder);
        context.start();
        Thread.sleep(1000);
        final CamelIntegrationProvider camelIntegrationProvider = new CamelIntegrationProvider();
        final var result = camelIntegrationProvider.send("direct-vm:test", DollarStatic.$("test"));
        assertEquals("RESULT", result.$S());
        System.out.println(result);
        context.stop();
        camelIntegrationProvider.stop();

    }

    @Test
    public void testWrite() throws Exception {

    }

}