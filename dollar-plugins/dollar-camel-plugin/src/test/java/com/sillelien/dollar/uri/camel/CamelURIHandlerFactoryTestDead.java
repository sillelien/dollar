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

package com.sillelien.dollar.uri.camel;

import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.uri.URI;
import com.sillelien.dollar.api.uri.URIHandler;
import com.sillelien.dollar.api.var;
import dollar.internal.camel.CamelContext;
import dollar.internal.camel.builder.RouteBuilder;
import dollar.internal.camel.impl.DefaultCamelContext;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static com.sillelien.dollar.api.DollarStatic.$void;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CamelURIHandlerFactoryTestDead {


    @Test
    public void testDispatch() {

    }

    @Test
    public void testListen() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        String vmListenURI = "camel:vm://testListen";
        CamelURIHandlerFactory camelURIHandlerFactory = new CamelURIHandlerFactory();
        final URIHandler camelIntegrationProvider = camelURIHandlerFactory.forURI("camel", URI.parse(vmListenURI));
        camelIntegrationProvider.subscribe((value) -> {
            try {
                System.out.println("***: " + value[0]);
                assertEquals("Listen Test", value[0].toString());
                countDownLatch.countDown();
                return $void();
            } catch (Exception e) {
                e.printStackTrace();
                return $void();
            }
        }, UUID.randomUUID().toString());
        camelURIHandlerFactory.start();
        camelIntegrationProvider.write(DollarStatic.$("Listen Test"), false, false);
        countDownLatch.await();
        camelURIHandlerFactory.stop();
    }

    @Test
    public void testPoll() {
        CamelURIHandlerFactory camelURIHandlerFactory = new CamelURIHandlerFactory();
        final var page = camelURIHandlerFactory.forURI("camel", URI.parse("camel:http://google.com")).read(true,
                                                                                                           false);
        assertTrue(page.$S().contains("html"));
    }

    @Test
    public void testPublish() {

    }

    @Test
    public void testSend() throws Exception {
        CamelContext context = new DefaultCamelContext();
        RouteBuilder routeBuilder = new RouteBuilder() {
            @Override
            public void configure() {
                from("direct-vm:test").process((e) -> {
                    e.getOut().setBody("RESULT");
                });
            }
        };
        context.addRoutes(routeBuilder);
        context.start();
        Thread.sleep(1000);
        CamelURIHandlerFactory camelURIHandlerFactory = new CamelURIHandlerFactory();

        final var result = camelURIHandlerFactory.forURI("camel", URI.parse("camel:direct-vm:test")).write(
                DollarStatic.$("test"), true, false);
        assertEquals("RESULT", result.$S());
        System.out.println(result);
        context.stop();
        camelURIHandlerFactory.stop();

    }

    @Test
    public void testWrite() {

    }

}
