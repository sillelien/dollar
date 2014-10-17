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
                return DollarStatic.handleError(e);
            }
            return result.S();
        };

//        DollarStatic.log("Adding route for "+path);
        addRoute(method, wrap(path, route));
    }
}
