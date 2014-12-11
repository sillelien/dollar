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
import me.neilellis.dollar.Pipeable;
import me.neilellis.dollar.types.DollarFactory;
import me.neilellis.dollar.uri.URI;
import me.neilellis.dollar.uri.URIHandler;
import me.neilellis.dollar.uri.URIHandlerFactory;
import me.neilellis.dollar.var;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import static me.neilellis.dollar.DollarStatic.$void;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class CamelURIHandlerFactory implements URIHandlerFactory {

    private final DefaultCamelContext context = new DefaultCamelContext();

    @Override
    public URIHandlerFactory copy() {
        return new CamelURIHandlerFactory();
    }

    @Override
    public URIHandler forURI(String scheme, URI uri) {
        return new CamelURIHandler(uri, context);
    }

    @Override
    public boolean handlesScheme(String scheme) {
        return scheme.equals("camel");
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
        private final URI uri;
        private CamelContext context;


        CamelURIHandler(URI uri, DefaultCamelContext context) {
            this.uri = uri.sub();
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
        public var all() {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override public void destroy() {
            //TODO
        }

        @Override
        public var drain() {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override
        public var get(var key) {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override public void init() {
            //TODO
        }

        @Override public void pause() {
            //TODO
        }

        @Override
        public var publish(var value) {
            try {
                producerTemplate.asyncSendBody(uri.asString(), value.$S());
            } catch (Exception e) {
                return DollarStatic.handleError(e, value);
            }
            return value;
        }

        @Override
        public var send(var value, boolean blocking, boolean mutating) {
            try {
                if (blocking) {
                    return DollarFactory.fromStringValue(
                            producerTemplate.sendBody(uri.asString(), ExchangePattern.InOut, value.$S())
                                            .toString());
                } else {
                    producerTemplate.asyncSendBody(uri.asString(), value.$S());
                    return value;
                }
            } catch (CamelExecutionException e) {
                return DollarStatic.handleError(e, value);
            }
        }

        @Override
        public var receive(boolean blocking, boolean mutating) {
            try {
                if (blocking) {
                    return DollarFactory.fromStringValue(consumerTemplate.receiveBody(uri.asString(), String.class));
                } else {
                    return DollarFactory.fromStringValue(consumerTemplate.receiveBody(uri.asString(), 0, String.class));
                }
            } catch (Exception e) {
                return DollarStatic.handleError(e, $void());
            }
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
        public var set(var key, var value) {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override public void start() {
            //TODO
        }

        @Override public void stop() {
            //TODO
        }

        @Override
        public void subscribe(Pipeable consumer, String id) {
            try {
                context.addRoutes(
                        new RouteBuilder(context) {
                            @Override
                            public void configure() throws Exception {
                                from(uri.asString()).process((ex) -> {
                                    try {
                                        consumer.pipe(DollarFactory.fromStringValue(ex.getIn().getBody(String.class)));
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

        @Override public void unpause() {
            //TODO
        }

        @Override public void unsubscribe(String subId) {
            //TODO
        }

        public CamelContext getContext() {
            return context;
        }

        public void setContext(CamelContext context) {
            this.context = context;
        }


    }
}
