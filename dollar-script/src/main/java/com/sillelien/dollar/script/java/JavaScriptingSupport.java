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

package com.sillelien.dollar.script.java;

import com.sillelien.dollar.api.DollarStatic;
import com.sillelien.dollar.api.var;
import com.sillelien.jas.jproxy.JProxy;
import com.sillelien.jas.jproxy.JProxyConfig;
import com.sillelien.jas.jproxy.JProxyScriptEngine;
import com.sillelien.jas.jproxy.JProxyScriptEngineFactory;
import com.sillelien.dollar.script.api.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.script.Bindings;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.Collections;
import java.util.List;

import static com.sillelien.dollar.api.DollarStatic.$;

public class JavaScriptingSupport {

    @Nullable public static var compile(@NotNull var in, String java, @NotNull Scope scope) {
        JProxyConfig jpConfig = JProxy.createJProxyConfig();
        jpConfig.setEnabled(true)
                .setRelProxyOnReloadListener((objOld, objNew, proxy, method, args) -> {
                    //TODO
                })
//                .setInputPath(".")
                .setScanPeriod(-1)
                .setClassFolder(System.getProperty("user.home") + "/.dollar/tmp/classes")
                .setCompilationOptions(Collections.emptyList())
                .setJProxyDiagnosticsListener(diagnostics -> {
                    List<Diagnostic<? extends JavaFileObject>> diagnosticList = diagnostics.getDiagnostics();
                    diagnosticList.stream()
                                  .filter(diagnostic -> diagnostic.getKind().equals(Diagnostic.Kind.ERROR))
                                  .forEach(System.err::println);
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
        bindings.put("scope", scope);

        StringBuilder code = new StringBuilder();
        code.append(
                " /* in statement */ com.sillelien.dollar.api.var in = (com.sillelien.dollar.api.var)context.getAttribute(\"in\",javax" +
                ".script.ScriptContext.ENGINE_SCOPE); \n");
        code.append(
                " /* scope statement */ com.sillelien.dollar.script.api.Scope scope = (com.sillelien.dollar.script.api.Scope)context" +
                ".getAttribute(\"scope\",javax.script.ScriptContext.ENGINE_SCOPE); \n");
        code.append(" /* out statement */ com.sillelien.dollar.api.var out = com.sillelien.dollar.api.DollarStatic.$void();\n");
        code.append(java).append("\nreturn out;\n");


        var result = null;
        try {
            result = $(scriptEngine.eval(code.toString(), bindings));
        } catch (ScriptException e) {
            return DollarStatic.handleError(e, null);
        }
        return result;
    }
}
