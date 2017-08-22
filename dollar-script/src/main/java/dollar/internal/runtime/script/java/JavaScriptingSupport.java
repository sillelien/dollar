/*
 *    Copyright (c) 2014-2017 Neil Ellis
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dollar.internal.runtime.script.java;

import com.sillelien.dollar.api.var;
import com.sillelien.jas.jproxy.JProxy;
import com.sillelien.jas.jproxy.JProxyConfig;
import com.sillelien.jas.jproxy.JProxyScriptEngine;
import com.sillelien.jas.jproxy.JProxyScriptEngineFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.sillelien.dollar.api.DollarStatic.$;
import static dollar.internal.runtime.script.DollarScriptSupport.currentScope;

/**
 */
public final class JavaScriptingSupport {

    @NotNull
    private static final Logger log = LoggerFactory.getLogger("JavaScriptingSupport");

    @Nullable
    public static var compile(@NotNull var in, @NotNull String java) {
        JProxyConfig jpConfig = JProxy.createJProxyConfig();
        jpConfig.setEnabled(true)
                .setRelProxyOnReloadListener((objOld, objNew, proxy, method, args) -> {
                    //TODO
                })
//                .setInputPath(".")
                .setScanPeriod(-1)
                .setImports(Arrays.asList("dollar.lang.*","dollar.internal.runtime.script.api.*","com.sillelien.dollar.api.*","java.io.*","java.math.*","java.net.*","java.nio.file.*","java.util.*","java.util.concurrent.*","java.util.function.*","java.util.prefs.*","java.util.regex.*","java.util.stream.*"))
                .setStaticImports(Arrays.asList("com.sillelien.dollar.api.DollarStatic.*","dollar.internal.runtime.script.java.JavaScriptingStaticImports.*"))
                .setClassFolder(System.getProperty("user.home") + "/.dollar/tmp/classes")
                .setCompilationOptions(Collections.emptyList())
                .setJProxyDiagnosticsListener(diagnostics -> {
                    List<Diagnostic<? extends JavaFileObject>> diagnosticList = diagnostics.getDiagnostics();
                    diagnosticList.stream()
                                  .filter(diagnostic -> diagnostic.getKind().equals(Diagnostic.Kind.ERROR))
                                  .forEach(i->log.debug(i.toString()));
                });

        JProxyScriptEngineFactory factory = JProxyScriptEngineFactory.create();

        ScriptEngineManager manager = new ScriptEngineManager();
//        manager.registerEngineName("j", factory);
//
//        manager.getBindings().putValue("in",in);
//
//        ScriptEngine engine = manager.getEngineByName("j");

        JProxyScriptEngine scriptEngine = (JProxyScriptEngine) factory.getScriptEngine();
        scriptEngine.init(jpConfig);
        Bindings bindings = scriptEngine.createBindings();
        bindings.put("in", in);
        bindings.put("scope", currentScope());

        StringBuilder code = new StringBuilder();
        code.append(" /* in statement */ var in = (var)context.getAttribute(\"in\",javax.script.ScriptContext.ENGINE_SCOPE); \n");
        code.append(" /* scope statement */ Scope scope = (Scope)context.getAttribute(\"scope\",javax.script.ScriptContext.ENGINE_SCOPE); \n");
        code.append(" /* out statement */ var out = $void();\n");
        code.append(java).append(" /* return statement */ \nreturn out;\n");

        log.debug(code.toString());
        var result = null;
        try {
            result = $(scriptEngine.eval(code.toString(), bindings));
        } catch (ScriptException e) {
            return currentScope().handleError(e);
        }
        return result;
    }
}
