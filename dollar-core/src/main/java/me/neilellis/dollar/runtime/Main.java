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

package me.neilellis.dollar.runtime;

import com.innowhere.relproxy.jproxy.JProxy;
import com.innowhere.relproxy.jproxy.JProxyConfig;
import com.innowhere.relproxy.jproxy.JProxyDiagnosticsListener;
import me.neilellis.dollar.Script;
import me.neilellis.dollar.var;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.io.File;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author <a href="http://uk.linkedin.com/in/neilellis">Neil Ellis</a>
 */
public class Main extends Script {
    static {
        $THIS = Main.class;
    }

    @Override
    public var pipe(var in) throws Exception {

        JProxyDiagnosticsListener diagnosticsListener = new JProxyDiagnosticsListener() {
            public void onDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics) {
                List<Diagnostic<? extends JavaFileObject>> diagList = diagnostics.getDiagnostics();
                int i = 1;
                for (Diagnostic diagnostic : diagList) {
                    System.err.println("Diagnostic " + i);
                    System.err.println("  code: " + diagnostic.getCode());
                    System.err.println("  kind: " + diagnostic.getKind());
                    System.err.println("  line number: " + diagnostic.getLineNumber());
                    System.err.println("  column number: " + diagnostic.getColumnNumber());
                    System.err.println("  start position: " + diagnostic.getStartPosition());
                    System.err.println("  position: " + diagnostic.getPosition());
                    System.err.println("  end position: " + diagnostic.getEndPosition());
                    System.err.println("  source: " + diagnostic.getSource());
                    System.err.println("  message: " + diagnostic.getMessage(null));
                    i++;
                }
            }
        };
        JProxyConfig jpConfig = JProxy.createJProxyConfig();
        final File classCacheDir = new File(System.getProperty("user.home"), ".dollar/cache/classes");
        classCacheDir.mkdirs();
        jpConfig.setEnabled(true)
                .setRelProxyOnReloadListener((objOld, objNew, proxy, method, args1) -> System.out.println("Reloaded " + objNew + " Calling method: " + method))
                .setInputPath(in.$("dir").$default(".").S())
                .setScanPeriod(in.$("scan").$default(60).I())
                .setClassFolder(classCacheDir.getAbsolutePath())
                .setCompilationOptions(asList("-source", "1.8", "-target", "1.8"))
                .setJProxyDiagnosticsListener(diagnosticsListener);

        JProxy.init(jpConfig);
        return in;

    }
}
