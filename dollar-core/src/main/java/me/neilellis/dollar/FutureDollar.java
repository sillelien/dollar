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

package me.neilellis.dollar;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class FutureDollar<T> {

    private var requestDollar;
    private CompletableFuture<var> response = new CompletableFuture<>();


    public FutureDollar(var requestDollar) {
        this.requestDollar = requestDollar;
    }

    public void handle(Message<JsonObject> message) {
        response.complete(DollarStatic.$(message));

    }

    public var request() {
        return requestDollar;
    }

    public var then(Function<var, Void> handler) throws ExecutionException, InterruptedException {
        handler.apply(response.get());
        return response.get();
    }

    public var then() {
        try {
            return response.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return DollarNull.INSTANCE;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
