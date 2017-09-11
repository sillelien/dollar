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

package dollar.java;

import com.sillelien.jas.jproxy.JProxy;
import com.sillelien.jas.jproxy.JProxyConfig;
import com.sillelien.jas.jproxy.JProxyScriptEngine;
import com.sillelien.jas.jproxy.JProxyScriptEngineFactory;
import dollar.api.DollarStatic;
import dollar.api.Scope;
import dollar.api.plugin.ExtensionPoint;
import dollar.api.scripting.ScriptingLanguage;
import dollar.api.var;
import org.jetbrains.annotations.NotNull;
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

/**
 */
public final class JavaScriptingLanguage implements ScriptingLanguage {


    @NotNull
    private static final Logger log = LoggerFactory.getLogger("ScriptingSupport");

    @Override
    @NotNull
    public var compile(@NotNull String script, @NotNull Scope scope) {

        JProxyConfig jpConfig = JProxy.createJProxyConfig();
        jpConfig.setEnabled(true)
                .setRelProxyOnReloadListener((objOld, objNew, proxy, method, args) -> {
                    //TODO
                })
//                .setInputPath(".")
                .setScanPeriod(-1)
                .setImports(Arrays.asList("dollar.lang.*", "dollar.internal.runtime.script.api.*", "dollar.api.*", "java.io.*",
                                          "java.math.*", "java.net.*", "java.nio.file.*", "java.util.*", "java.util.concurrent.*",
                                          "java.util.function.*", "java.util.prefs.*", "java.util.regex.*", "java.util.stream.*"))
                .setStaticImports(Arrays.asList("dollar.api.DollarStatic.*", "dollar.java.JavaScriptingStaticImports.*"))
                .setClassFolder(System.getProperty("user.home") + "/.dollar/tmp/classes")
                .setCompilationOptions(Collections.emptyList())
                .setJProxyDiagnosticsListener(diagnostics -> {
                    List<Diagnostic<? extends JavaFileObject>> diagnosticList = diagnostics.getDiagnostics();
                    diagnosticList.stream()
                            .filter(diagnostic -> diagnostic.getKind().equals(Diagnostic.Kind.ERROR))
                            .forEach(i -> log.debug(i.toString()));
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
        bindings.put("in", scope.parametersAsVars());
        bindings.put("scope", scope);

        StringBuilder code = new StringBuilder();
        code.append(
                " /* in statement */ List<var> in = (List<var>)context.getAttribute(\"in\",javax.script.ScriptContext.ENGINE_SCOPE)" +
                        "; \n");
        code.append(
                " /* scope statement */ Scope scope = (Scope)context.getAttribute(\"scope\",javax.script.ScriptContext.ENGINE_SCOPE); \n");
        code.append(" /* out statement */ var out = $void();\n");
        code.append(script).append(" /* return statement */ \nreturn out;\n");

        log.debug(code.toString());
        var result = null;
        try {
            result = DollarStatic.$(scriptEngine.eval(code.toString(), bindings));
        } catch (ScriptException e) {
            log.debug(e.getMessage(), e);
            return scope.handleError(e);
        }
        return result;
    }

    @Override
    public boolean provides(@NotNull String language) {
        return "java".equals(language);
    }

    @NotNull
    @Override
    public ExtensionPoint copy() {
        return null;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }
}
