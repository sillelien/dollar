package me.neilellis.dollar;

import spark.Route;
import spark.SparkBase;

import static me.neilellis.dollar.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarHttp extends SparkBase {

    public DollarHttp(String method, String path, DollarHttpHandler handler) {
        DollarThreadContext context = DollarStatic.context();

        Route route = (request, response) -> {
            var result = null;
            try {

                DollarThreadContext childContext = context.child();
                childContext.pushLabel(method + ":" + path);
                result = DollarStatic.$call(childContext, () -> DollarStatic.tracer().trace(DollarNull.INSTANCE, handler.handle(new DollarHttpContext(request, response)), StateTracer.Operations.HTTP_RESPONSE, method, path));
                if(result.hasErrors()) {
                    var errors = result.errors();
                    response.status(errors.$("httpCode").ifNull(() -> $(500)).$int());
                    return errors.$$();
                }
                response.type(result.$mimeType());
            } catch (Exception e) {
                return DollarStatic.handleError(e);
            }
            return result.$$();
        };

//        DollarStatic.log("Adding route for "+path);
        addRoute(method, wrap(path, route));
    }
}
