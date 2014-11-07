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

package me.neilellis.dollar.uri.camel;

import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.uri.URIHandler;
import me.neilellis.dollar.uri.URIHandlerFactory;
import me.neilellis.dollar.var;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.function.Consumer;

import static me.neilellis.dollar.DollarStatic.$void;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class CamelURIHandlerFactory implements URIHandlerFactory {

    private DefaultCamelContext context = new DefaultCamelContext();

    @Override
    public URIHandlerFactory copy() {
        return new CamelURIHandlerFactory();
    }


    @Override
    public String getScheme() {
        return "camel";
    }

    @Override
    public URIHandler forURI(String uri) {
        return new CamelURIHandler(uri, context);
    }

    @Override
    public void start() throws Exception {
        context.start();
    }

    @Override
    public void stop() throws Exception {
        context.stop();
    }


    class CamelURIHandler implements URIHandler {

        private final ConsumerTemplate consumerTemplate;
        private final ProducerTemplate producerTemplate;
        private final String uri;
        private CamelContext context;


        CamelURIHandler(String uri, DefaultCamelContext context) {
            this.uri = uri;
            this.context = context;
            try {
                this.context.start();
            } catch (Exception e) {
                throw new Error(e);
            }
            producerTemplate = this.context.createProducerTemplate();
            consumerTemplate = this.context.createConsumerTemplate();
        }

        @Override
        public var dispatch(var value) {
            try {
                producerTemplate.asyncSendBody(uri, value.$S());
                return value;
            } catch (Exception e) {
                return DollarStatic.handleError(e, value);
            }
        }

        @Override
        public void subscribe(Consumer<var> consumer) {
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
                DollarStatic.handleError(e, null);
            }
        }

        @Override
        public var poll() {
            try {
                return DollarFactory.fromStringValue(consumerTemplate.receiveBody(uri, String.class));
            } catch (Exception e) {
                return DollarStatic.handleError(e, $void());
            }
        }

        @Override
        public var receive() {
            try {
                return DollarFactory.fromStringValue(consumerTemplate.receiveBody(uri, String.class));
            } catch (Exception e) {
                return DollarStatic.handleError(e, $void());
            }
        }

        @Override
        public var publish(var value) {
            try {
                producerTemplate.asyncSendBody(uri, value.$S());
            } catch (Exception e) {
                return DollarStatic.handleError(e, value);
            }
            return value;
        }

        @Override
        public var send(var value) {
            try {
                return DollarFactory.fromStringValue(producerTemplate.sendBody(uri, ExchangePattern.InOut, value.$S())
                        .toString());
            } catch (CamelExecutionException e) {
                return DollarStatic.handleError(e, value);
            }
        }

        @Override
        public var push(var value) {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override
        public var peek() {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override
        public var set(var key, var value) {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override
        public var get(var key) {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override
        public var all() {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override
        public var remove(var v) {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override
        public var removeValue(var v) {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override
        public var subscribe() {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override
        public var give(var value) {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override
        public var drain() {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }


        public CamelContext getContext() {
            return context;
        }

        public void setContext(CamelContext context) {
            this.context = context;
        }


    }
}
