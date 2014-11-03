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
import me.neilellis.dollar.integration.IntegrationProvider;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.var;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.function.Consumer;

import static me.neilellis.dollar.DollarStatic.$void;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class CamelIntegrationProvider implements IntegrationProvider {

    private final ConsumerTemplate consumerTemplate;
    private final ProducerTemplate producerTemplate;
    private CamelContext context;


    public CamelIntegrationProvider() {
        context = new DefaultCamelContext();
        try {
            context.start();
        } catch (Exception e) {
            throw new Error(e);
        }
        producerTemplate = context.createProducerTemplate();
        consumerTemplate = context.createConsumerTemplate();
    }

    @Override
    public IntegrationProvider copy() {
        return new CamelIntegrationProvider();
    }

    @Override
    public var dispatch(String uri, var value) {
        try {
            producerTemplate.asyncSendBody(uri, value.$S());
            return value;
        } catch (Exception e) {
            return DollarStatic.handleError(e, value);
        }
    }

    @Override
    public var listen(String uri, Consumer<var> consumer) {
        try {
            context.addRoutes(
                    new RouteBuilder(context) {
                        @Override
                        public void configure() throws Exception {
                            from(uri).process((ex) -> {
                                try {
                                    consumer.accept(DollarFactory.fromStringValue(ex.getIn().getBody(String.class)));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            return DollarStatic.handleError(e, null);
        }
        return $void();
    }

    @Override
    public var poll(String uri) {
        try {
            return DollarFactory.fromStringValue(consumerTemplate.receiveBody(uri, String.class));
        } catch (Exception e) {
            return DollarStatic.handleError(e, $void());
        }
    }

    @Override
    public var publish(String uri, var value) {
        try {
            producerTemplate.asyncSendBody(uri, value.$S());
        } catch (Exception e) {
            return DollarStatic.handleError(e, value);
        }
        return value;
    }

    @Override
    public var send(String uri, var value) {
        try {
            return DollarFactory.fromStringValue(producerTemplate.sendBody(uri, ExchangePattern.InOut, value.$S())
                    .toString());
        } catch (CamelExecutionException e) {
            return DollarStatic.handleError(e, value);
        }
    }

    public CamelContext getContext() {
        return context;
    }

    public void setContext(CamelContext context) {
        this.context = context;
    }

    @Override
    public void start() throws Exception {
        context.start();
    }

    @Override
    public void stop() throws Exception {
        context.stop();
    }

}
