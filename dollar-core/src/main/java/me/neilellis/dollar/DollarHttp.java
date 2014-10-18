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

package me.neilellis.dollar;

import me.neilellis.dollar.types.DollarVoid;
import org.jetbrains.annotations.NotNull;
import spark.Route;
import spark.SparkBase;

import static me.neilellis.dollar.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarHttp extends SparkBase {

    public DollarHttp(String method, String path, @NotNull DollarHttpHandler handler) {
        DollarThreadContext context = DollarStatic.context();

        Route route = (request, response) -> {
            var result = null;
            try {

                DollarThreadContext childContext = context.child();
                childContext.pushLabel(method + ":" + path);
                result = DollarStatic.$call(childContext, () -> DollarStatic.tracer().trace(DollarVoid.INSTANCE, handler.handle(new DollarHttpContext(request, response)), StateTracer.Operations.HTTP_RESPONSE, method, path));
                if(result.hasErrors()) {
                    var errors = result.$errors();
                    response.status(errors.$("httpCode").$void(() -> $(500)).I());
                    return errors.S();
                }
                response.type(result.$mimeType());
            } catch (Exception e) {
                return DollarStatic.handleError(e, result);
            }
            return result.S();
        };

//        DollarStatic.log("Adding route for "+path);
        addRoute(method, wrap(path, route));
    }
}
