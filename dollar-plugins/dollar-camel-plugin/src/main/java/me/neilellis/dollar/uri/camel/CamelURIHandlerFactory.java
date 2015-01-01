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

package me.neilellis.dollar.uri.camel;

import me.neilellis.dollar.api.DollarStatic;
import me.neilellis.dollar.api.Pipeable;
import me.neilellis.dollar.api.types.DollarFactory;
import me.neilellis.dollar.api.uri.URI;
import me.neilellis.dollar.api.uri.URIHandler;
import me.neilellis.dollar.api.uri.URIHandlerFactory;
import me.neilellis.dollar.api.var;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.jetbrains.annotations.NotNull;

import static me.neilellis.dollar.api.DollarStatic.$void;

public class CamelURIHandlerFactory implements URIHandlerFactory {

    private final DefaultCamelContext context = new DefaultCamelContext();

    @NotNull @Override
    public URIHandlerFactory copy() {
        return new CamelURIHandlerFactory();
    }

    @NotNull @Override
    public URIHandler forURI(String scheme, @NotNull URI uri) {
        return new CamelURIHandler(uri, context);
    }

    @Override
    public boolean handlesScheme(@NotNull String scheme) {
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
        @NotNull private final URI uri;
        private CamelContext context;


        CamelURIHandler(@NotNull URI uri, DefaultCamelContext context) {
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

        @NotNull @Override
        public var all() {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override
        public var write(@NotNull var value, boolean blocking, boolean mutating) {
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

        @Override public void destroy() {
            //TODO
        }

        @NotNull @Override
        public var drain() {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @NotNull @Override
        public var get(var key) {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @Override public void init() {
            //TODO
        }

        @Override public void pause() {
            //TODO
        }

        @NotNull @Override
        public var publish(@NotNull var value) {
            try {
                producerTemplate.asyncSendBody(uri.asString(), value.$S());
            } catch (Exception e) {
                return DollarStatic.handleError(e, value);
            }
            return value;
        }

        @Override
        public var read(boolean blocking, boolean mutating) {
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

        @NotNull @Override
        public var remove(var v) {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @NotNull @Override
        public var removeValue(var v) {
            throw new UnsupportedOperationException("The Apache Camel uri handler does not support this yet.");
        }

        @NotNull @Override
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
        public void subscribe(@NotNull Pipeable consumer, String id) {
            try {
                context.addRoutes(
                        new RouteBuilder(context) {
                            @Override
                            public void configure() {
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
