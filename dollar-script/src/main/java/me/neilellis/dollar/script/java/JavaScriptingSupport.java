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

package me.neilellis.dollar.script.java;

import com.innowhere.relproxy.jproxy.JProxy;
import com.innowhere.relproxy.jproxy.JProxyConfig;
import com.innowhere.relproxy.jproxy.JProxyScriptEngineFactory;
import me.neilellis.dollar.DollarStatic;
import me.neilellis.dollar.script.ScriptScope;
import me.neilellis.dollar.var;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.Collections;
import java.util.List;

import static me.neilellis.dollar.DollarStatic.$;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class JavaScriptingSupport {

    public static var compile(var in, String java, ScriptScope scope) {
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

        JProxyScriptEngineFactory factory = JProxyScriptEngineFactory.create(jpConfig);

        ScriptEngineManager manager = new ScriptEngineManager();
//        manager.registerEngineName("j", factory);
//
//        manager.getBindings().put("in",in);
//
//        ScriptEngine engine = manager.getEngineByName("j");

        ScriptEngine scriptEngine = factory.getScriptEngine();
        Bindings bindings = scriptEngine.createBindings();
        bindings.put("in", in);
        bindings.put("scope", scope);

        StringBuilder code = new StringBuilder();
        code.append(" me.neilellis.dollar.var in = (me.neilellis.dollar.var)context.getAttribute(\"in\",javax.script.ScriptContext.ENGINE_SCOPE); \n");
        code.append(" me.neilellis.dollar.script.Scope scope = (me.neilellis.dollar.script.ScriptScope)context.getAttribute(\"scope\",javax.script.ScriptContext.ENGINE_SCOPE); \n");
        code.append(" me.neilellis.dollar.var out = me.neilellis.dollar.DollarStatic.$void();\n");
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
