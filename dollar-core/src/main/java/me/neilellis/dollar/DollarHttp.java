package me.neilellis.dollar;

import spark.Route;
import spark.SparkBase;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class DollarHttp extends SparkBase {

    public DollarHttp(String method, String path, DollarHttpHandler handler) {
        DollarThreadContext context = DollarStatic.context();

        Route route = (request, response) -> {
            DollarThreadContext childContext= context.child();
            childContext.pushLabel(method + ":" + path);
            var result = DollarStatic.$call(childContext,() -> DollarStatic.tracer().trace(DollarNull.INSTANCE,handler.handle(new DollarHttpContext(request, response)), StateTracer.Operations.HTTP_RESPONSE,method,path));
            response.type(result.$mimeType());
            return result.$$();
        };

//        DollarStatic.log("Adding route for "+path);
        addRoute(method, wrap(path, route));
    }
}
